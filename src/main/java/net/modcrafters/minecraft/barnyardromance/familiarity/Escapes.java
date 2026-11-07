package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.rivalry.Reach;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// A neglected animal running off. Penned in, it walks up to the fence or wall in the way and leaps it; free, it just
// wanders away. Where "outside" is takes a few path searches, made once when it runs (at most once a day); after that
// only the leap is looked after tick by tick. A runaway in progress lives only while the animal is loaded.
public final class Escapes {
    // Spots are sampled on rings this far out, 16 to a ring.
    private static final int[] RINGS = {4, 8, 12};
    private static final int SAMPLES = 16;
    // The farthest ring, with room to climb or drop a little on the way.
    private static final int RANGE = 16;
    private static final double WALK_SPEED = 1.0;
    // A fence is a block and a half high, so a normal jump won't do. The sideways speed is held while it rises, which
    // carries it about three blocks: over a fence two blocks off, or one it's pressed against.
    private static final double LEAP_UP = 0.6;
    private static final double LEAP_SPEED = 0.3;
    // It leaps once it's this close to where its walk toward the outside ends, beside the fence.
    private static final double LEAP_FROM = 1.5;
    private static final long GIVE_UP_TICKS = 200L;

    private enum Stage {
        // Off the ground when it ran, where no path can be found: it waits to land.
        LANDING,
        APPROACH,
        LEAP
    }

    private record Escape(Stage stage, @Nullable BlockPos outside, @Nullable Path path, Vec3 heading, boolean catchingUp, long until) {
    }

    private record Way(BlockPos spot, boolean penned) {
    }

    private static final Map<UUID, Escape> RUNNING = new ConcurrentHashMap<>();

    private Escapes() {
    }

    // catchingUp is true when the escape was worked out for days spent away, with no one there to see it go.
    public static void runAway(Animal animal, ServerLevel level, boolean catchingUp) {
        Services.DATA.get(animal).ifPresent(traits -> Services.DATA.set(animal, traits.withUpkeep(traits.upkeep().runAway())));
        long now = level.getGameTime();
        // Escapes cut short by an unload or a death are never ticked again.
        RUNNING.values().removeIf(escape -> now > escape.until());
        // A lead or a rider holds it where it is.
        if (animal.isLeashed() || animal.isPassenger() || animal.isVehicle()) {
            return;
        }
        if (!canSearch(animal)) {
            RUNNING.put(animal.getUUID(), new Escape(Stage.LANDING, null, null, Vec3.ZERO, catchingUp, now + GIVE_UP_TICKS));
            return;
        }
        setOff(animal, level, catchingUp);
    }

    // Every tick, for every animal; it does nothing unless this one is running away.
    public static void tick(Animal animal, ServerLevel level) {
        if (RUNNING.isEmpty()) {
            return;
        }
        Escape escape = RUNNING.get(animal.getUUID());
        if (escape == null) {
            return;
        }
        boolean late = level.getGameTime() > escape.until();
        if (!animal.isAlive() || late && escape.stage() != Stage.APPROACH) {
            RUNNING.remove(animal.getUUID());
            return;
        }
        switch (escape.stage()) {
            case LANDING -> {
                if (canSearch(animal)) {
                    RUNNING.remove(animal.getUUID());
                    setOff(animal, level, escape.catchingUp());
                }
            }
            case APPROACH -> approach(animal, level, escape, late);
            case LEAP -> fly(animal, escape);
        }
    }

    private static void setOff(Animal animal, ServerLevel level, boolean catchingUp) {
        Way way = way(animal, level);
        if (way == null) {
            return;
        }
        BlockPos spot = way.spot();
        PathNavigation navigation = animal.getNavigation();
        if (!way.penned()) {
            navigation.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, WALK_SPEED);
            return;
        }
        if (catchingUp) {
            navigation.stop();
            animal.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
            return;
        }
        // Its walk toward the spot ends at the fence in the way, which is where it leaps from.
        navigation.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, WALK_SPEED);
        Path path = navigation.getPath();
        if (path == null || path.isDone() || nearEnd(animal, path)) {
            leap(animal, level, spot);
        } else {
            RUNNING.put(animal.getUUID(), new Escape(Stage.APPROACH, spot, path, Vec3.ZERO, false, level.getGameTime() + GIVE_UP_TICKS));
        }
    }

    // Walks the path it was given; another goal that takes the navigation over is given the same path back, which
    // needs no new search. If it can't get there in time it leaps from where it is.
    private static void approach(Animal animal, ServerLevel level, Escape escape, boolean late) {
        Path path = escape.path();
        BlockPos outside = escape.outside();
        if (path == null || outside == null) {
            RUNNING.remove(animal.getUUID());
            return;
        }
        if (late || path.isDone() || nearEnd(animal, path)) {
            leap(animal, level, outside);
        } else if (animal.getNavigation().getPath() != path) {
            animal.getNavigation().moveTo(path, WALK_SPEED);
        }
    }

    private static void leap(Animal animal, ServerLevel level, BlockPos outside) {
        Vec3 heading = new Vec3(outside.getX() + 0.5 - animal.getX(), 0.0, outside.getZ() + 0.5 - animal.getZ());
        if (heading.lengthSqr() < 1.0e-6) {
            RUNNING.remove(animal.getUUID());
            return;
        }
        heading = heading.normalize().scale(LEAP_SPEED);
        animal.getNavigation().stop();
        animal.setDeltaMovement(heading.x, LEAP_UP, heading.z);
        //? if >=26.1 {
        animal.needsSync = true;
        //?} else
        //animal.hasImpulse = true;
        RUNNING.put(animal.getUUID(), new Escape(Stage.LEAP, outside, null, heading, false, level.getGameTime() + GIVE_UP_TICKS));
    }

    // While it rises it keeps its speed, even after bumping into the fence low down; once it lands (or splashes down)
    // it walks on.
    private static void fly(Animal animal, Escape escape) {
        Vec3 moving = animal.getDeltaMovement();
        if (moving.y > 0.0 && !animal.isInLiquid()) {
            animal.setDeltaMovement(escape.heading().x, moving.y, escape.heading().z);
        } else if (canSearch(animal)) {
            RUNNING.remove(animal.getUUID());
            BlockPos outside = escape.outside();
            if (outside != null) {
                animal.getNavigation().moveTo(outside.getX() + 0.5, outside.getY(), outside.getZ() + 0.5, WALK_SPEED);
            }
        }
    }

    // It's free if it can walk to any spot on the farthest ring; then it wanders off there, in no particular direction.
    // Otherwise it's penned in, and the nearest spot it can't walk to is outside. With nothing outside and nothing far,
    // it wanders to the farthest spot it can reach.
    private static @Nullable Way way(Animal animal, ServerLevel level) {
        List<List<BlockPos>> rings = new ArrayList<>();
        for (int radius : RINGS) {
            rings.add(ring(animal, level, radius));
        }
        List<BlockPos> far = rings.getLast();
        int start = far.isEmpty() ? 0 : animal.getRandom().nextInt(far.size());
        for (int i = 0; i < far.size(); i++) {
            BlockPos spot = far.get((start + i) % far.size());
            if (Reach.canWalkTo(animal, spot, RANGE)) {
                return new Way(spot, false);
            }
        }
        @Nullable BlockPos farthest = null;
        for (int ring = 0; ring < rings.size(); ring++) {
            boolean last = ring == rings.size() - 1;
            List<BlockPos> outside = new ArrayList<>();
            for (BlockPos spot : rings.get(ring)) {
                // Nothing on the far ring could be reached, or it would have been taken above.
                if (last || !Reach.canWalkTo(animal, spot, RANGE)) {
                    outside.add(spot);
                } else if (farthest == null || distance(animal, spot) > distance(animal, farthest)) {
                    farthest = spot;
                }
            }
            if (!outside.isEmpty()) {
                return new Way(nearest(animal, level, outside), true);
            }
        }
        return farthest == null ? null : new Way(farthest, false);
    }

    // The nearest of the spots, taking first those with nothing taller than a fence in the way, so it leaps the fence
    // rather than into the house wall behind it.
    private static BlockPos nearest(Animal animal, ServerLevel level, List<BlockPos> spots) {
        return spots.stream()
                .min(Comparator.<BlockPos>comparingInt(spot -> leapable(animal, level, spot) ? 0 : 1)
                        .thenComparingDouble(spot -> distance(animal, spot)))
                .orElseThrow();
    }

    // The two blocks above its feet are clear all the way there, so what's in the way is no higher than a fence.
    private static boolean leapable(Animal animal, ServerLevel level, BlockPos spot) {
        double dx = spot.getX() + 0.5 - animal.getX();
        double dz = spot.getZ() + 0.5 - animal.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        int y = animal.getBlockY();
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (double step = 0.5; step < length; step += 0.5) {
            int x = Mth.floor(animal.getX() + dx * step / length);
            int z = Mth.floor(animal.getZ() + dz * step / length);
            if (!free(level, at.set(x, y + 1, z)) || !free(level, at.set(x, y + 2, z))) {
                return false;
            }
        }
        return true;
    }

    // Up to 16 spots sampled evenly around the ring, each where it could stand in that column, near its own height.
    private static List<BlockPos> ring(Animal animal, ServerLevel level, int radius) {
        Set<BlockPos> spots = new LinkedHashSet<>();
        for (int i = 0; i < SAMPLES; i++) {
            double angle = Math.PI * 2.0 * i / SAMPLES;
            BlockPos spot = standable(level, Mth.floor(animal.getX() + radius * Math.cos(angle)), animal.getBlockY(),
                    Mth.floor(animal.getZ() + radius * Math.sin(angle)));
            if (spot != null) {
                spots.add(spot);
            }
        }
        return new ArrayList<>(spots);
    }

    // Looking down from two above to three below: solid ground with two free blocks over it.
    private static @Nullable BlockPos standable(ServerLevel level, int x, int y, int z) {
        for (int dy = 2; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            if (!level.isLoaded(pos)) {
                return null;
            }
            BlockPos below = pos.below();
            if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && free(level, pos) && free(level, pos.above())) {
                return pos;
            }
        }
        return null;
    }

    private static boolean free(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    private static boolean nearEnd(Animal animal, Path path) {
        Node end = path.getEndNode();
        if (end == null) {
            return true;
        }
        double dx = end.x + 0.5 - animal.getX();
        double dz = end.z + 0.5 - animal.getZ();
        return dx * dx + dz * dz <= LEAP_FROM * LEAP_FROM;
    }

    // A path can only be found from the ground (or swimming).
    private static boolean canSearch(Animal animal) {
        return animal.onGround() || animal.isInLiquid();
    }

    private static double distance(Animal animal, BlockPos spot) {
        return animal.distanceToSqr(Vec3.atBottomCenterOf(spot));
    }
}
