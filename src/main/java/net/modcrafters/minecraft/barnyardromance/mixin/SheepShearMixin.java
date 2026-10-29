package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

// versions/1.21.1 has its own copy: shearing rolled the wool count itself there.
@Mixin(Sheep.class)
public class SheepShearMixin {
    // Shears and dispensers both end up here, so this is where the once-a-day shearing is counted.
    @Inject(method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void barnyardromance$countShearing(CallbackInfo callback) {
        ShearActions.recordShearing((Sheep) (Object) this);
    }

    // Wool from shears and dispensers is sized before vanilla spawns it one at a time.
    @ModifyArg(
            method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/sheep/Sheep;dropFromShearingLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/item/ItemInstance;Ljava/util/function/BiConsumer;)V"
            ),
            index = 3
    )
    private BiConsumer<ServerLevel, ItemStack> barnyardromance$sizedWool(BiConsumer<ServerLevel, ItemStack> original) {
        Sheep sheep = (Sheep) (Object) this;
        return (level, drop) -> {
            ItemStack sized = drop;
            try {
                sized = ProductDrops.resize(sheep, drop, sheep.getRandom());
            } catch (RuntimeException exception) {
                Constants.LOG.error("Failed to size a sheep's wool, leaving it to vanilla", exception);
            }
            original.accept(level, sized);
        };
    }
}
