package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.nest.NestOccupant;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

// The client's copy of the hen sitting in a box, made from the saved hen the box sends and kept until she leaves.
// She faces out of the hole.
public final class NestingBoxHen {
    // The top of the hay inside the box.
    static final double HAY_TOP = 5.0 / 16.0;
    private static final Map<NestingBoxBlockEntity, Copy> COPIES = new WeakHashMap<>();

    private record Copy(CompoundTag from, Entity hen) {
    }

    private NestingBoxHen() {
    }

    public static @Nullable Entity sitting(NestingBoxBlockEntity box) {
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
            // She's never ticked, so her last turn is set too: she's drawn between the two every frame.
            hen.setYRot(yaw);
            hen.yRotO = yaw;
            hen.setXRot(0.0F);
            if (hen instanceof LivingEntity living) {
                living.yBodyRot = yaw;
                living.yBodyRotO = yaw;
                living.yHeadRot = yaw;
                living.yHeadRotO = yaw;
            }
            copy = new Copy(occupant.hen(), hen);
            COPIES.put(box, copy);
        }
        return copy.hen();
    }

    static float fit(Entity hen) {
        return sittingFit(hen).scale();
    }

    static double legs(Entity hen) {
        return sittingFit(hen).sinkBy();
    }

    private static SittingFit sittingFit(Entity hen) {
        double scale = hen instanceof LivingEntity living ? living.getScale() : 1.0;
        return SittingFit.of(BuiltInRegistries.ENTITY_TYPE.getKey(hen.getType()).toString(), hen.getBbHeight(), scale);
    }
}
