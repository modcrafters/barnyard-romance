package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.drops.MeatDrop;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.Optional;

@Mixin(LivingEntity.class)
public class LivingEntityDropsMixin {
    @Inject(
            method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void barnyardromance$sizedMeat(
            ServerLevel level,
            DamageSource source,
            boolean playerKilled,
            ResourceKey<LootTable> lootTable,
            CallbackInfo callback
    ) {
        if (!((Object) this instanceof Animal animal)) {
            return;
        }
        BarnyardConfig config = BarnyardConfig.current();
        String speciesId = AnimalSex.speciesId(animal);
        if (!config.speciesEnabled(speciesId)) {
            return;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty()) {
            return;
        }
        callback.cancel();
        SizeSettings range = config.defaultSize();
        double scale = traits.get().scale();
        animal.dropFromLootTable(level, source, playerKilled, lootTable, (ItemStack stack) -> {
            String itemId = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            int resized = MeatDrop.resizedCount(
                    itemId,
                    stack.getCount(),
                    scale,
                    range.smallest(),
                    range.largest(),
                    1.0,
                    animal.getRandom().nextDouble(),
                    animal.getRandom().nextDouble()
            );
            stack.setCount(resized);
            animal.spawnAtLocation(level, stack);
        });
    }
}
