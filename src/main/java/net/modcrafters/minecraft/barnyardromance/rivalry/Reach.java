package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

// Whether one animal can walk to another: within range, and with a path that really gets there, so a fence or a wall
// between pens keeps them apart. It's a path search, so it's only asked now and then, never every tick.
public final class Reach {
    private Reach() {
    }

    public static boolean canWalk(Animal from, Animal to, int range) {
        if (from.distanceToSqr(to) > (double) range * range) {
            return false;
        }
        Path path = from.getNavigation().createPath(to, 0);
        return path != null && (path.canReach() || endsBeside(path.getEndNode(), to.blockPosition()));
    }

    // Some mods' navigation aims a path somewhere other than the animal's own block (Naturalist's ostrich does), so a
    // path ending right beside her counts too. A fence keeps the male at least two blocks off, so it still keeps him out.
    private static boolean endsBeside(Node end, BlockPos target) {
        return end != null && Math.abs(end.x - target.getX()) <= 1 && Math.abs(end.z - target.getZ()) <= 1 && Math.abs(end.y - target.getY()) <= 1;
    }
}
