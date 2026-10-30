package net.modcrafters.minecraft.barnyardromance.nest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Finding nesting boxes near a hen, and remembering which hens are sitting in one. Boxes are found by looking
// through the loaded chunks in range when an egg is due, a few times a day per hen, so nothing has to be kept
// in step with boxes being placed, broken, loaded or unloaded.
public final class NestingBoxes {
    // Hens that left the world to sit in a box: they haven't died, whatever their removal says.
    private static final Set<UUID> IN_NEST = new HashSet<>();

    private NestingBoxes() {
    }

    // Loaded boxes within range of the centre, nearest first.
    public static List<NestingBoxBlockEntity> nearby(ServerLevel level, BlockPos center, int range) {
        List<NestingBoxBlockEntity> found = new ArrayList<>();
        long rangeSqr = (long) range * range;
        for (int chunkX = SectionPos.blockToSectionCoord(center.getX() - range); chunkX <= SectionPos.blockToSectionCoord(center.getX() + range); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(center.getZ() - range); chunkZ <= SectionPos.blockToSectionCoord(center.getZ() + range); chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity instanceof NestingBoxBlockEntity box && !box.isRemoved() && box.getBlockPos().distSqr(center) <= rangeSqr) {
                        found.add(box);
                    }
                }
            }
        }
        found.sort(Comparator.comparingDouble(box -> box.getBlockPos().distSqr(center)));
        return found;
    }

    public static boolean inNest(UUID hen) {
        return IN_NEST.contains(hen);
    }

    static void entered(UUID hen) {
        IN_NEST.add(hen);
    }

    static void left(UUID hen) {
        IN_NEST.remove(hen);
    }
}
