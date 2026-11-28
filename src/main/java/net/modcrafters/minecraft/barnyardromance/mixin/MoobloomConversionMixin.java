package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Conversions;
import net.modcrafters.minecraft.barnyardromance.core.compat.MoobloomSwap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A sheared Friends&Foes moobloom turns into a cow in its private transformToCow(ServerLevel), which does not go
// through Mob.convertTo. Read from the jars of 1.21.1 (NeoForge) and 26.3 (NeoForge and Fabric), the order is the same
// in all three: particles, this.discard(), create the Cow (EntityType.create), copy health, position, name and
// persistence, then level.addFreshEntity(cow). The discard comes first, and it takes the moobloom out of the level
// (and out of the herd book's tracking, which would write a death) before the cow exists. So the cow is made here, at
// the head of the method, and the record is carried onto it while the moobloom is still alive; then the mod's own
// create call is given that cow instead of making another, and everything it copies and its addFreshEntity go on as
// they were. require = 0 loses a hook the mod renames instead of crashing the game;
// the plugin only lists this when the mod is there. The record is carried only when CompatMixinPlugin saw the swap
// woven in (MoobloomSwap.applied()): a carry without the swap would move the herd book to a cow that never joins. If
// the swap is missing, one warning is logged and the sheared moobloom just loses its record. The swap is a plain Mixin
// @Redirect because MixinExtras' injectors are woven after the plugin's postApply, where the check could not see them.
// On Forge 1.20.1 Minecraft's methods have SRG names in a real game (Mojang's in the dev runs), so that build has the
// refmap remap the create call (remap = true on its @At); transformToCow is the mod's own name in both.
@Pseudo
@Mixin(targets = "com.faboslav.friendsandfoes.common.entity.MoobloomEntity", remap = false)
public class MoobloomConversionMixin {
    @Unique
    private Animal barnyardromance$cow;
    @Unique
    private static boolean barnyardromance$warned;

    @Inject(method = "transformToCow", at = @At("HEAD"), require = 0)
    private void barnyardromance$carryRecord(ServerLevel level, CallbackInfo callback) {
        barnyardromance$cow = null;
        if (!MoobloomSwap.applied()) {
            if (!barnyardromance$warned) {
                barnyardromance$warned = true;
                Constants.LOG.warn("Friends&Foes' moobloom no longer makes its cow where expected, so a sheared moobloom loses its record");
            }
            return;
        }
        try {
            //? if >=1.21.2 {
            Animal cow = EntityTypes.COW.create(level, EntitySpawnReason.CONVERSION);
            //?} else
            //Animal cow = EntityTypes.COW.create(level);
            if (cow != null) {
                // Set first, so a carry that fails partway still leaves this cow the one that is added.
                barnyardromance$cow = cow;
                Conversions.carry((Animal) (Object) this, cow);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to carry a moobloom's record to the cow it became", exception);
        }
    }

    // A plain Mixin redirect rather than MixinExtras' injector, so it is already woven when CompatMixinPlugin looks.
    @Redirect(
            method = "transformToCow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;"
                    //? if >=1.21.2 {
                    + "Lnet/minecraft/world/entity/EntitySpawnReason;"
                    //?}
                    + ")Lnet/minecraft/world/entity/Entity;"
                    //? if <1.20.5
                    //, remap = true
            ),
            require = 0
    )
    //? if >=1.21.2 {
    private Entity barnyardromance$sameCow(EntityType<?> type, Level level, EntitySpawnReason reason) {
        Animal ours = barnyardromance$cow;
        barnyardromance$cow = null;
        return ours != null ? ours : type.create(level, reason);
    }
    //?} else {
    /*private Entity barnyardromance$sameCow(EntityType<?> type, Level level) {
        Animal ours = barnyardromance$cow;
        barnyardromance$cow = null;
        return ours != null ? ours : type.create(level);
    }
    *///?}
}
