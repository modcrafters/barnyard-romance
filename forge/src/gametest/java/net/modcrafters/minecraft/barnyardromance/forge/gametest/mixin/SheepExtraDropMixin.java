package net.modcrafters.minecraft.barnyardromance.forge.gametest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.modcrafters.minecraft.barnyardromance.forge.gametest.ForgeSharedGameTests;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// Another mod that adds its own drop to what shears take off a sheep, for the sheep a test names (see
// ForgeSharedGameTests). Its priority is below the mod's, so it adds the drop before the mod sizes the wool.
@Mixin(value = Sheep.class, priority = 500)
public class SheepExtraDropMixin {
    @Inject(method = "onSheared", remap = false, at = @At("RETURN"))
    private void barnyardromance$anotherModsDrop(@Nullable Player player, ItemStack item, Level level, BlockPos pos, int fortune,
                                                 CallbackInfoReturnable<List<ItemStack>> callback) {
        ItemStack extra = ForgeSharedGameTests.extraShearDrop((Sheep) (Object) this);
        if (!extra.isEmpty()) {
            callback.getReturnValue().add(extra);
        }
    }
}
