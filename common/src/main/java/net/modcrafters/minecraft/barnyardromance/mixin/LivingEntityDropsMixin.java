package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityDropsMixin {
    @Inject(
            method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void barnyardromance$sizedDrops(
            ServerLevel level,
            DamageSource source,
            boolean playerKilled,
            ResourceKey<LootTable> lootTable,
            CallbackInfo callback
    ) {
        if (!((Object) this instanceof Animal animal)) {
            return;
        }
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal)) || Services.DATA.get(animal).isEmpty()) {
            return;
        }
        callback.cancel();
        animal.dropFromLootTable(level, source, playerKilled, lootTable, (ItemStack stack) -> {
            ItemStack sized = stack;
            try {
                sized = ProductDrops.resize(animal, stack, animal.getRandom());
            } catch (RuntimeException exception) {
                Constants.LOG.error("Failed to size a {} drop, leaving it to vanilla", AnimalSex.speciesId(animal), exception);
            }
            animal.spawnAtLocation(level, sized);
        });
    }
}
