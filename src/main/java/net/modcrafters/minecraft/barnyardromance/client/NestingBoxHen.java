package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.nest.NestOccupant;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

// The client's copy of the hen sitting in a box, made from the saved hen the box sends and kept until she leaves.
// She faces out of the hole.
final class NestingBoxHen {
    // The top of the hay inside the box.
    static final double HAY_TOP = 5.0 / 16.0;
    // The hollow above the hay is ten pixels tall; a hen taller than most of it is drawn smaller.
    private static final double ROOM = 9.0 / 16.0;
    // A chicken's legs are this share of its height; they go in the hay.
    private static final double LEGS = 0.4;
    private static final Map<NestingBoxBlockEntity, Copy> COPIES = new WeakHashMap<>();

    private record Copy(CompoundTag from, Entity hen) {
    }

    private NestingBoxHen() {
    }

    static @Nullable Entity sitting(NestingBoxBlockEntity box) {
        NestOccupant occupant = box.occupant();
        if (occupant == null || box.getLevel() == null) {
            COPIES.remove(box);
            return null;
        }
        Copy copy = COPIES.get(box);
        if (copy == null || copy.from() != occupant.hen()) {
            Entity hen = box.loadSittingHen(occupant).orElse(null);
            if (hen == null) {
                return null;
            }
            // She never joins the client's world, so she gets an id no real entity has: rendering asks for one.
            hen.setId(-1 - (box.getBlockPos().hashCode() & Integer.MAX_VALUE));
            float yaw = box.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
            hen.setYRot(yaw);
            hen.yRotO = yaw;
            hen.setYHeadRot(yaw);
            hen.setYBodyRot(yaw);
            hen.setXRot(0.0F);
            copy = new Copy(occupant.hen(), hen);
            COPIES.put(box, copy);
        }
        return copy.hen();
    }

    static float fit(Entity hen) {
        double above = hen.getBbHeight() * (1.0 - LEGS);
        return above > ROOM ? (float) (ROOM / above) : 1.0F;
    }

    static double legs(Entity hen) {
        return hen.getBbHeight() * LEGS;
    }
}
