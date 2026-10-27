package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The 1.21.1 copy of src/.../mixin/LivingEntityDropsMixin: dropFromLootTable rolls the loot itself and spawns
// every stack directly, so this rolls it the same way and passes each stack through ProductDrops.dropSized.
@Mixin(LivingEntity.class)
public abstract class LivingEntityDropsMixin {
    @Shadow
    protected Player lastHurtByPlayer;

    @Shadow
    public abstract ResourceKey<LootTable> getLootTable();

    @Shadow
    public abstract long getLootTableSeed();

    @Inject(
            method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void barnyardromance$sizedDrops(DamageSource source, boolean playerKilled, CallbackInfo callback) {
        if (!((Object) this instanceof Animal animal) || !(animal.level() instanceof ServerLevel level)) {
            return;
        }
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal)) || Services.DATA.get(animal).isEmpty()) {
            return;
        }
        callback.cancel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(getLootTable());
        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, animal)
                .withParameter(LootContextParams.ORIGIN, animal.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
        if (playerKilled && lastHurtByPlayer != null) {
            params = params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, lastHurtByPlayer).withLuck(lastHurtByPlayer.getLuck());
        }
        table.getRandomItems(params.create(LootContextParamSets.ENTITY), getLootTableSeed(),
                stack -> ProductDrops.dropSized(animal, stack, animal::spawnAtLocation));
    }
}
