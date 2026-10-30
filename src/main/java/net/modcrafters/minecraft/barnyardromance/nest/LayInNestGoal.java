package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.animal.chicken.Chicken;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

// A hen with an egg due walks to the nesting box EggLaying picked and goes in. If the box is taken or full when
// she gets there she looks once more; if she can't get anywhere in time she lays on the ground where she is.
public class LayInNestGoal extends Goal {
    private static final int GIVE_UP_TICKS = 600;
    private static final double REACH_SQR = 1.6 * 1.6;
    private static final Map<Animal, Trip> TRIPS = new WeakHashMap<>();

    private final Animal hen;

    // planned is false until she has a path, which can only be worked out once she's on the ground.
    private record Trip(ItemStack egg, BlockPos box, long since, boolean retried, boolean planned) {
    }

    public LayInNestGoal(Animal hen) {
        this.hen = hen;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    // Only chickens have this goal, and a hen that's leashed, ridden or riding stays where she is.
    public static boolean walks(Animal hen) {
        return hen instanceof Chicken && !hen.isPassenger() && !hen.isVehicle() && !hen.isLeashed();
    }

    // Starts her off towards the box if she can get there. A hen flapping in the air can't plan a path yet, so she
    // sets off anyway and finds her way once she lands, or gives up in time.
    public static boolean headFor(Animal hen, ItemStack egg, BlockPos box, long now, boolean retried) {
        Path path = hen.onGround() ? hen.getNavigation().createPath(box, 1) : null;
        if (hen.onGround() && (path == null || !path.canReach())) {
            return false;
        }
        TRIPS.put(hen, new Trip(egg, box.immutable(), now, retried, path != null));
        if (path != null) {
            hen.getNavigation().moveTo(path, 1.0);
        }
        return true;
    }

    public static Optional<BlockPos> target(Animal hen) {
        return Optional.ofNullable(TRIPS.get(hen)).map(Trip::box);
    }

    @Override
    public boolean canUse() {
        return TRIPS.containsKey(hen);
    }

    @Override
    public boolean canContinueToUse() {
        return TRIPS.containsKey(hen) && hen.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Trip trip = TRIPS.get(hen);
        if (trip == null || !(hen.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        if (now - trip.since() > GIVE_UP_TICKS) {
            TRIPS.remove(hen);
            EggLaying.layOnGround(hen, level);
            return;
        }
        if (!trip.planned()) {
            if (!hen.onGround()) {
                return;
            }
            Path path = hen.getNavigation().createPath(trip.box(), 1);
            if (path == null || !path.canReach()) {
                TRIPS.remove(hen);
                lookElsewhere(level, trip);
                return;
            }
            TRIPS.put(hen, new Trip(trip.egg(), trip.box(), trip.since(), trip.retried(), true));
            hen.getNavigation().moveTo(path, 1.0);
            return;
        }
        if (hen.position().distanceToSqr(Vec3.atBottomCenterOf(trip.box())) > REACH_SQR) {
            if (hen.getNavigation().isDone()) {
                hen.getNavigation().moveTo(trip.box().getX() + 0.5, trip.box().getY(), trip.box().getZ() + 0.5, 1.0);
            }
            return;
        }
        TRIPS.remove(hen);
        if (level.getBlockEntity(trip.box()) instanceof NestingBoxBlockEntity box && box.enter(hen, trip.egg(), now)) {
            return;
        }
        lookElsewhere(level, trip);
    }

    // The box can't be reached, or was taken or filled on the way: one more look, then the ground.
    private void lookElsewhere(ServerLevel level, Trip trip) {
        if (trip.retried() || !EggLaying.headForBox(hen, level, trip.egg(), Set.of(trip.box()), true)) {
            EggLaying.layOnGround(hen, level);
        }
    }

    @Override
    public void stop() {
        hen.getNavigation().stop();
    }
}
