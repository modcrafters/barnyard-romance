package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// The 1.21.1 copy of src/.../mixin/SheepShearMixin. Shearing rolls 1-3 wool and spawns them one at a time; the
// roll is sized here instead (any wool stands in for the colour: the sized_drops tag holds #minecraft:wool).
@Mixin(Sheep.class)
public class SheepShearMixin {
    @ModifyVariable(method = "shear(Lnet/minecraft/sounds/SoundSource;)V", at = @At("STORE"), ordinal = 0)
    private int barnyardromance$sizedWool(int count) {
        Sheep sheep = (Sheep) (Object) this;
        try {
            return ProductDrops.resize(sheep, new ItemStack(Items.WHITE_WOOL, count), sheep.getRandom()).getCount();
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to size a sheep's wool, leaving it to vanilla", exception);
            return count;
        }
    }
}
