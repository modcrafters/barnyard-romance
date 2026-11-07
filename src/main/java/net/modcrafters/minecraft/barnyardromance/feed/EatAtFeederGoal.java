package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
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

// An animal MorningFeeding sent walks over to its feeding block, eats one item there and walks off to make room. If
// the crowd keeps it from the block for 30 seconds it eats where it stands when it's close, and otherwise rests a while
// before it's sent again. A panic stops the walk, and the next round sends it again. Every animal has this goal, and it
// does nothing until the animal is sent: checking for that is only a map lookup.
public class EatAtFeederGoal extends Goal {
    static final int GIVE_UP_TICKS = 600;
    // A trip whose walk never got going (another goal kept the animal busy, or it has no AI) is let go after this.
    private static final int FORGET_TICKS = GIVE_UP_TICKS + 2 * MorningFeeding.ROUND_TICKS;
    // After giving up from too far off, so an animal that can't get through doesn't take a walker's place each round.
    private static final int REST_TICKS = 600;
    // Between path searches while it's still on its way and its path has run out.
    private static final int PATH_TICKS = 40;
    // From the animal's body to the block (see within).
    static final double REACH = 1.5;
    static final double CLOSE_ENOUGH = 4.0;
    // How far it walks off once it has eaten.
    private static final int WALK_OFF = 5;
    private static final Map<Animal, Trip> TRIPS = new WeakHashMap<>();
    private static final Map<Animal, Long> RESTING = new WeakHashMap<>();

    private final Animal animal;
    // The game time of its last path search.
    private long pathed;
    // Fed and on its way off, which the goal stopping leaves be.
    private boolean walkingOff;

    // over is set once it has eaten or given up. The trip is only forgotten when the goal stops, so the animals still
    // counted as walking (MorningFeeding's limit) are never fewer than those with the goal running.
    private record Trip(BlockPos feeder, long since, boolean over) {
    }

    public EatAtFeederGoal(Animal animal) {
        this.animal = animal;
        this.setFlags(EnumSet.of(Flag.MOVE));
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

    static boolean headingTo(Animal animal, BlockPos feeder) {
        Trip trip = TRIPS.get(animal);
        return trip != null && trip.feeder().equals(feeder);
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

    public static void clear() {
        TRIPS.clear();
        RESTING.clear();
    }

    @Override
    public boolean canUse() {
        Trip trip = TRIPS.get(animal);
        return trip != null && !trip.over() && !animal.isPanicking();
    }

    @Override
    public boolean canContinueToUse() {
        Trip trip = TRIPS.get(animal);
        return trip != null && !trip.over() && animal.isAlive() && !animal.isPanicking();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // It looks for a path on its first tick.
    @Override
    public void start() {
        pathed = animal.level().getGameTime() - PATH_TICKS;
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
        long clock = feeder.clock(level);
        // Fed by hand on the way, or the day ended (at midnight, or by sleeping): no meal from this trip.
        if (!FeederMeal.due(animal, clock)) {
            end(trip);
            return;
        }
        if (within(animal, trip.feeder(), REACH)) {
            eat(level, feeder, clock);
            end(trip);
            return;
        }
        if (now - trip.since() >= GIVE_UP_TICKS) {
            if (within(animal, trip.feeder(), CLOSE_ENOUGH)) {
                eat(level, feeder, clock);
            } else {
                RESTING.put(animal, now + REST_TICKS);
            }
            end(trip);
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
                // No way on from where it stands, as when it's wedged in a fence corner: a few steps straight at the
                // block, and a new search from wherever that gets it.
                Vec3 block = Vec3.atBottomCenterOf(trip.feeder());
                animal.getMoveControl().setWantedPosition(block.x, animal.getY(), block.z, 1.0);
            }
        }
    }

    @Override
    public void stop() {
        if (walkingOff) {
            walkingOff = false;
            TRIPS.remove(animal);
            return;
        }
        animal.getNavigation().stop();
        TRIPS.remove(animal);
    }

    // It eats, then walks off a little way to make room at the block for the next one.
    private void eat(ServerLevel level, FeederBlockEntity feeder, long clock) {
        if (!FeederMeal.eat(animal, level, feeder, clock)) {
            return;
        }
        Vec3 away = DefaultRandomPos.getPosAway(animal, WALK_OFF, 2, Vec3.atCenterOf(feeder.getBlockPos()));
        if (away != null) {
            animal.getNavigation().stop();
            walkingOff = animal.getNavigation().moveTo(away.x, away.y, away.z, 1.0);
        }
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
