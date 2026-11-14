package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.core.ServerScoped;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

// An animal MorningFeeding sent walks over to its feeding block, eats one item there and walks off to make room. The
// meal counts the moment it starts; the animal then stands a few seconds with its head down in the block, crumbs of
// the food at its mouth, before it goes, and a scare or a lead cuts that short without undoing the meal. It keeps the
// goal while it walks off, so wandering can't turn it round at the block. If
// the crowd keeps it from the block for 30 seconds it eats where it stands when it's close, and otherwise rests a while
// before it's sent again. A panic stops the walk, and the next round sends it again. Every animal has this goal, and it
// does nothing until the animal is sent: checking for that is only a map lookup.
public class EatAtFeederGoal extends Goal {
    static final int GIVE_UP_TICKS = 600;
    // How long it stands eating at the block.
    public static final int EAT_TICKS = 70;
    // Crumbs at its mouth this often, a munch this often, and a sheep's grazing nod (40 ticks long) this often.
    private static final int CRUMB_TICKS = 4;
    private static final int MUNCH_TICKS = 25;
    private static final int GRAZE_TICKS = 35;
    // The client event that starts a sheep's grazing nod, as when it eats grass.
    private static final byte GRAZE_EVENT = 10;
    // The longest it keeps walking off before other goals may have it.
    private static final int WALK_OFF_TICKS = 100;
    // A trip whose walk never got going (another goal kept the animal busy, or it has no AI) is let go after this. A
    // trip that ends in a meal just before giving up runs on for the meal and the walk off, so this leaves room for them.
    private static final int FORGET_TICKS = GIVE_UP_TICKS + EAT_TICKS + WALK_OFF_TICKS + 2 * MorningFeeding.ROUND_TICKS;
    // After giving up from too far off, so an animal that can't get through doesn't take a walker's place each round.
    private static final int REST_TICKS = 600;
    // Between path searches while it's still on its way and its path has run out.
    private static final int PATH_TICKS = 40;
    private static final int STEP_TICKS = 20;
    // From the animal's body to the block (see within).
    public static final double REACH = 1.5;
    static final double CLOSE_ENOUGH = 4.0;
    // How far it walks off once it has eaten.
    private static final int WALK_OFF = 5;
    private static final double WALK_OFF_AT_LEAST = 3.0;
    private static final int WALK_OFF_TRIES = 5;
    private static final Map<Animal, Trip> TRIPS = ServerScoped.track(new WeakHashMap<>());
    private static final Map<Animal, Long> RESTING = ServerScoped.track(new WeakHashMap<>());

    private final Animal animal;
    // The game time of its last path search.
    private long pathed;
    // Fed and on its way off, until this game time at the latest. The goal runs on while it does, and stopping
    // leaves the walk be.
    private boolean walkingOff;
    private long walkingOffUntil;
    // Until this game time it steps straight at the block, having found no way on from where it stood.
    private long stepping;
    // While it eats: when it's done (-1 when it isn't eating), and what it's eating, for the crumbs.
    private long eatingUntil = -1L;
    private ItemStack food = ItemStack.EMPTY;

    // over is set once it has eaten or given up. The trip is only forgotten when the goal stops, so the animals still
    // counted as walking are never fewer than those with the goal running. One walking off after its meal doesn't take
    // a walker's place (see headingTo).
    private record Trip(BlockPos feeder, long since, boolean over) {
    }

    public EatAtFeederGoal(Animal animal) {
        this.animal = animal;
        // LOOK too, so a look-at-player goal doesn't lift its head out of the block while it eats.
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // Sends the animal off to the block; it plans its path once the goal starts.
    static void send(Animal animal, BlockPos feeder, long now) {
        RESTING.remove(animal);
        TRIPS.put(animal, new Trip(feeder.immutable(), now, false));
    }

    // Sent to a block and not yet back: the walk is under way, or about to be.
    public static boolean walking(Animal animal) {
        return TRIPS.containsKey(animal);
    }

    // The same, letting go of a trip whose walk never started.
    static boolean walking(Animal animal, long now) {
        Trip trip = TRIPS.get(animal);
        if (trip != null && now - trip.since() > FORGET_TICKS) {
            TRIPS.remove(animal);
            return false;
        }
        return trip != null;
    }

    // Sent to a block and not done there: on its way, or eating. One walking off after its meal isn't.
    public static boolean onTheWay(Animal animal) {
        Trip trip = TRIPS.get(animal);
        return trip != null && !trip.over();
    }

    static boolean headingTo(Animal animal, BlockPos feeder) {
        Trip trip = TRIPS.get(animal);
        return trip != null && !trip.over() && trip.feeder().equals(feeder);
    }

    static boolean resting(Animal animal, long now) {
        Long until = RESTING.get(animal);
        return until != null && now < until;
    }

    // A leashed, ridden or riding animal, or a pet told to sit, stays where it is.
    static boolean walks(Animal animal) {
        return !animal.isPassenger() && !animal.isVehicle() && !animal.isLeashed()
                && !(animal instanceof TamableAnimal pet && pet.isOrderedToSit());
    }

    // From the animal's body to the block, so a big animal and a small one stand as near.
    public static boolean within(Animal animal, BlockPos feeder, double distance) {
        AABB body = animal.getBoundingBox();
        double x = gap(body.minX, body.maxX, feeder.getX());
        double y = gap(body.minY, body.maxY, feeder.getY());
        double z = gap(body.minZ, body.maxZ, feeder.getZ());
        return x * x + y * y + z * z <= distance * distance;
    }

    // Between a span and the block's span from start to start + 1.
    private static double gap(double min, double max, int start) {
        return Math.max(0.0, Math.max(start - max, min - (start + 1)));
    }

    @Override
    public boolean canUse() {
        Trip trip = TRIPS.get(animal);
        return trip != null && !trip.over() && !animal.isPanicking();
    }

    @Override
    public boolean canContinueToUse() {
        Trip trip = TRIPS.get(animal);
        if (trip == null || !animal.isAlive() || animal.isPanicking()) {
            return false;
        }
        if (!trip.over()) {
            return true;
        }
        return walkingOff && walks(animal) && !animal.getNavigation().isDone() && animal.level().getGameTime() < walkingOffUntil;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // It looks for a path on its first tick.
    @Override
    public void start() {
        pathed = animal.level().getGameTime() - PATH_TICKS;
        stepping = 0L;
        eatingUntil = -1L;
        food = ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        Trip trip = TRIPS.get(animal);
        if (trip == null || trip.over() || !(animal.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        // The block is gone, or the animal was put on a lead, mounted or told to sit on the way.
        if (!(level.getBlockEntity(trip.feeder()) instanceof FeederBlockEntity feeder) || !walks(animal)) {
            end(trip);
            return;
        }
        if (eatingUntil >= 0L) {
            if (now >= eatingUntil) {
                walkOff(trip.feeder());
                walkingOffUntil = now + WALK_OFF_TICKS;
                end(trip);
            } else {
                chew(level, trip.feeder(), now);
            }
            return;
        }
        long clock = feeder.clock(level);
        // Fed by hand on the way, or the day ended (at midnight, or by sleeping): no meal from this trip.
        if (!FeederMeal.due(animal, clock)) {
            end(trip);
            return;
        }
        if (within(animal, trip.feeder(), REACH)) {
            if (!eat(level, feeder, clock, now)) {
                end(trip);
            }
            return;
        }
        if (now - trip.since() >= GIVE_UP_TICKS) {
            if (within(animal, trip.feeder(), CLOSE_ENOUGH)) {
                if (eat(level, feeder, clock, now)) {
                    return;
                }
            } else {
                RESTING.put(animal, now + REST_TICKS);
            }
            end(trip);
            return;
        }
        if (now < stepping) {
            // The move control only takes one tick's step at a time.
            Vec3 block = Vec3.atBottomCenterOf(trip.feeder());
            animal.getMoveControl().setWantedPosition(block.x, animal.getY(), block.z, 1.0);
            return;
        }
        if (animal.getNavigation().isDone() && now - pathed >= PATH_TICKS && canPlan(animal)) {
            pathed = now;
            Path path = pathTo(animal, trip.feeder());
            // Navigation keeps a finished path that has the same nodes as the new one, and so wouldn't move.
            animal.getNavigation().stop();
            if (path != null && (path.canReach() || path.getNodeCount() > 1)) {
                animal.getNavigation().moveTo(path, 1.0);
            } else {
                // No way on from where it stands, as when it's wedged in a fence corner: a second's steps straight at
                // the block, and a new search from wherever that gets it.
                stepping = now + STEP_TICKS;
            }
        }
    }

    @Override
    public void stop() {
        eatingUntil = -1L;
        food = ItemStack.EMPTY;
        if (walkingOff) {
            walkingOff = false;
            TRIPS.remove(animal);
            return;
        }
        animal.getNavigation().stop();
        TRIPS.remove(animal);
    }

    // Its meal, if the block has food: it stops where it is and starts eating.
    private boolean eat(ServerLevel level, FeederBlockEntity feeder, long clock, long now) {
        ItemStack eaten = FeederMeal.eat(animal, level, feeder, clock);
        if (eaten.isEmpty()) {
            return false;
        }
        food = eaten;
        Events.post(new AnimalEvent.FeederMeal(animal));
        eatingUntil = now + EAT_TICKS;
        stepping = 0L;
        animal.getNavigation().stop();
        chew(level, feeder.getBlockPos(), now);
        return true;
    }

    // A tick of eating: head down into the block (a sheep nods down to graze), crumbs of the food at its mouth, and
    // now and then a munch. The meal's first munch came with the meal.
    private void chew(ServerLevel level, BlockPos feeder, long now) {
        long into = EAT_TICKS - (eatingUntil - now);
        animal.getLookControl().setLookAt(feeder.getX() + 0.5, feeder.getY() + 0.4, feeder.getZ() + 0.5, 10.0F, animal.getMaxHeadXRot());
        if (animal instanceof Sheep && into % GRAZE_TICKS == 0) {
            level.broadcastEntityEvent(animal, GRAZE_EVENT);
        }
        if (into > 0 && into % MUNCH_TICKS == 0) {
            VersionCompat.playEatingSound(animal, food);
        }
        if (into % CRUMB_TICKS == 0) {
            FeederMeal.crumbs(level, animal, food, 3);
        }
    }

    // Done eating, it walks off a little way to make room at the block for the next one: to a random spot at least
    // WALK_OFF_AT_LEAST from the block and from where it stands, that it can get all the way to, or, when no such spot
    // turns up (as on 26.3's barrier-floored test arena, where none ever does), straight away from it. A spot measured
    // only from the block could lie a step away from an animal that ate on the far side, which then barely moved.
    private void walkOff(BlockPos feeder) {
        animal.getNavigation().stop();
        Vec3 block = Vec3.atCenterOf(feeder);
        for (int i = 0; i < WALK_OFF_TRIES; i++) {
            Vec3 spot = DefaultRandomPos.getPosAway(animal, WALK_OFF, 2, block);
            if (spot == null || Math.hypot(spot.x - block.x, spot.z - block.z) < WALK_OFF_AT_LEAST
                    || Math.hypot(spot.x - animal.getX(), spot.z - animal.getZ()) < WALK_OFF_AT_LEAST) {
                continue;
            }
            // A spot past a fence would leave it pressed against the fence, as close to the block as it was.
            Path path = animal.getNavigation().createPath(BlockPos.containing(spot), 0);
            if (path != null && path.canReach() && animal.getNavigation().moveTo(path, 1.0)) {
                walkingOff = true;
                return;
            }
        }
        Vec3 off = new Vec3(animal.getX() - block.x, 0.0, animal.getZ() - block.z);
        if (off.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 away = animal.position().add(off.normalize().scale(WALK_OFF));
        walkingOff = animal.getNavigation().moveTo(away.x, away.y, away.z, 1.0);
    }

    private void end(Trip trip) {
        TRIPS.put(animal, new Trip(trip.feeder(), trip.since(), true));
    }

    // A chicken flapping in the air can't plan a path yet; a duck on a pond plans from the water.
    private static boolean canPlan(Animal animal) {
        return animal.onGround() || animal.isInWater() && !(animal instanceof Chicken);
    }

    // A path to any spot around the block, beside it or a step down for one up on something. A path that falls short
    // is still taken: closer is better, and close enough will do in the end. Only asked while it's on its way and its
    // path has run out, every PATH_TICKS at most.
    private static @Nullable Path pathTo(Animal animal, BlockPos feeder) {
        Set<BlockPos> spots = new HashSet<>();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos beside = feeder.relative(side);
            spots.add(beside);
            spots.add(beside.below());
            spots.add(beside.relative(side.getClockWise()));
        }
        // A spot another animal stands on is left to it, unless every one is taken.
        List<Animal> others = animal.level().getEntitiesOfClass(Animal.class, new AABB(feeder).inflate(1.0), other -> other != animal);
        Set<BlockPos> free = new HashSet<>();
        for (BlockPos spot : spots) {
            AABB room = new AABB(spot);
            if (others.stream().noneMatch(other -> other.getBoundingBox().intersects(room))) {
                free.add(spot);
            }
        }
        return animal.getNavigation().createPath(free.isEmpty() ? spots : free, 0);
    }
}
