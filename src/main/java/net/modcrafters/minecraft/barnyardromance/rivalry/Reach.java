package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Whether one animal can walk to another: within range, and with a path that really gets there, so a fence or a wall
// between pens keeps them apart. It's a path search, so it's only asked now and then, never every tick.
public final class Reach {
    private Reach() {
    }

    public static boolean canWalk(Animal from, Animal to, int range) {
        if (from.distanceToSqr(to) > (double) range * range) {
            return false;
        }
        Path path = path(from, to);
        return path != null && (path.canReach() || endsBeside(path.getEndNode(), to.blockPosition()));
    }

    // The same for a block: a feeder, or a spot a runaway is headed for.
    public static boolean canWalkTo(Animal from, BlockPos to, int range) {
        if (from.distanceToSqr(Vec3.atBottomCenterOf(to)) > (double) range * range) {
            return false;
        }
        Path path = path(from, to);
        return path != null && (path.canReach() || endsBeside(path.getEndNode(), to));
    }

    // For a moment that can't wait for a better one, such as an egg being laid: only a path that is found and falls
    // short rules him out. A path can't be found at all for a mob in the air (a fluttering rooster), and that alone
    // shouldn't cost a hen her egg.
    public static boolean blocked(Animal from, Animal to, int range) {
        if (from.distanceToSqr(to) > (double) range * range) {
            return true;
        }
        Path path = path(from, to);
        return path != null && !path.canReach() && !endsBeside(path.getEndNode(), to.blockPosition());
    }

    // A fresh ground navigation for each search: the animal's own hands back the path it's already walking when asked
    // about the same target twice, and asking it would also change where it thinks it's headed. Some mods' own
    // navigation aims somewhere other than the animal's block, too (Naturalist's ostrich does).
    private static @Nullable Path path(Animal from, Animal to) {
        return new GroundPathNavigation(from, from.level()).createPath(to, 0);
    }

    private static @Nullable Path path(Animal from, BlockPos to) {
        return new GroundPathNavigation(from, from.level()).createPath(to, 0);
    }

    // A path ending right beside her counts too. A fence keeps the male at least two blocks off, so it still keeps him out.
    private static boolean endsBeside(@Nullable Node end, BlockPos target) {
        return end != null && Math.abs(end.x - target.getX()) <= 1 && Math.abs(end.z - target.getZ()) <= 1 && Math.abs(end.y - target.getY()) <= 1;
    }
}
