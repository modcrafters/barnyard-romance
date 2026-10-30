package net.modcrafters.minecraft.barnyardromance.eggs;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.5 {
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.chicken.Chicken;
//?}

// What every chick hatched from an egg gets: a baby's age, its mother's look, and a place to stand.
public final class Hatching {
    private Hatching() {
    }

    public static void place(Animal chick, EggData data, int growthTicks, Vec3 position) {
        chick.setBaby(true);
        chick.setAge(-growthTicks);
        //? if >=1.21.5 {
        Identifier variant = Identifier.tryParse(data.variant());
        if (chick instanceof Chicken chicken && variant != null && !data.variant().isEmpty()) {
            chicken.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT)
                    .get(ResourceKey.create(Registries.CHICKEN_VARIANT, variant))
                    .ifPresent(chicken::setVariant);
        }
        //?}
        float yaw = chick.getRandom().nextFloat() * 360.0F;
        //? if >=1.21.5 {
        chick.snapTo(position.x, position.y, position.z, yaw, 0.0F);
        //?} else
        //chick.moveTo(position.x, position.y, position.z, yaw, 0.0F);
    }
}
