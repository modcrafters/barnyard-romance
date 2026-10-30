package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

// A hen with eggs due walks to the nesting box EggLaying picked and goes in to lay them all. If the box is taken or full when
// she gets there she looks once more; if she can't get anywhere in time she lays on the ground where she is.
public class LayInNestGoal extends Goal {
    private static final int GIVE_UP_TICKS = 600;
    // From the box to anywhere she can stop on a spot beside it.
    private static final double REACH_SQR = 2.0 * 2.0;
    private static final Map<Animal, Trip> TRIPS = new WeakHashMap<>();

    private final Animal hen;

    // planned is false until she has a path, which can only be worked out once she's on the ground.
    private record Trip(List<ItemStack> eggs, BlockPos box, long since, boolean retried, boolean planned) {
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
    public static boolean headFor(Animal hen, List<ItemStack> eggs, BlockPos box, long now, boolean retried) {
        Path path = hen.onGround() ? pathTo(hen, box) : null;
        if (hen.onGround() && path == null) {
            return false;
        }
        TRIPS.put(hen, new Trip(List.copyOf(eggs), box.immutable(), now, retried, path != null));
        if (path != null) {
            hen.getNavigation().moveTo(path, 1.0);
        }
        return true;
    }

    // A path to a spot beside the box: the one in front of the hole if she can get there, else any other side. Never
    // the box itself: vanilla paths to a solid block by aiming at the open space above it, which no hen can reach
    // once a fence or anything else stands on the box.
    private static @Nullable Path pathTo(Animal hen, BlockPos box) {
        BlockState state = hen.level().getBlockState(box);
        Direction front = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        Path path = hen.getNavigation().createPath(box.relative(front), 0);
        if (path != null && path.canReach()) {
            return path;
        }
        Set<BlockPos> sides = new HashSet<>();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != front) {
                sides.add(box.relative(side));
            }
        }
        path = hen.getNavigation().createPath(sides, 0);
        return path != null && path.canReach() ? path : null;
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
        // Put on a lead, or ridden, on her way: she lays where she is.
        if (!walks(hen)) {
            TRIPS.remove(hen);
            EggLaying.resume(hen, level);
            EggLaying.layOnGround(hen, level, trip.eggs().size());
            return;
        }
        if (now - trip.since() > GIVE_UP_TICKS) {
            TRIPS.remove(hen);
            EggLaying.resume(hen, level);
            EggLaying.layOnGround(hen, level, trip.eggs().size());
            return;
        }
        if (!trip.planned()) {
            if (!hen.onGround()) {
                return;
            }
            Path path = pathTo(hen, trip.box());
            if (path == null) {
                TRIPS.remove(hen);
                lookElsewhere(level, trip);
                return;
            }
            TRIPS.put(hen, new Trip(trip.eggs(), trip.box(), trip.since(), trip.retried(), true));
            hen.getNavigation().moveTo(path, 1.0);
            return;
        }
        if (hen.position().distanceToSqr(Vec3.atBottomCenterOf(trip.box())) > REACH_SQR) {
            if (hen.getNavigation().isDone()) {
                Path again = pathTo(hen, trip.box());
                if (again != null) {
                    hen.getNavigation().moveTo(again, 1.0);
                }
            }
            return;
        }
        TRIPS.remove(hen);
        if (level.getBlockEntity(trip.box()) instanceof NestingBoxBlockEntity box && box.enter(hen, trip.eggs(), now)) {
            return;
        }
        lookElsewhere(level, trip);
    }

    // The box can't be reached, or was taken or filled on the way: one more look, then the ground.
    private void lookElsewhere(ServerLevel level, Trip trip) {
        if (trip.retried() || !EggLaying.headForBox(hen, level, trip.eggs(), Set.of(trip.box()), true)) {
            EggLaying.resume(hen, level);
            EggLaying.layOnGround(hen, level, trip.eggs().size());
        }
    }

    @Override
    public void stop() {
        hen.getNavigation().stop();
    }
}
