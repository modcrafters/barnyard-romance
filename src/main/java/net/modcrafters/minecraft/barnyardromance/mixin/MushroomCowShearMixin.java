package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.animal.cow.MushroomCow;
import org.spongepowered.asm.mixin.Mixin;

// Nothing to do here: from 1.21.2 a sheared mooshroom goes through Mob.convertTo, which MobConversionMixin covers.
// versions/1.21.1 has its own copy, where it doesn't.
@Mixin(MushroomCow.class)
public class MushroomCowShearMixin {
}
