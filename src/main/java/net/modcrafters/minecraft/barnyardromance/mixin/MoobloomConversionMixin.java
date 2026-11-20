package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Conversions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A sheared Friends&Foes moobloom turns into a cow in its private transformToCow(ServerLevel), which does not go
// through Mob.convertTo. Read from the jars of 1.21.1 (NeoForge) and 26.3 (NeoForge and Fabric), the order is the same
// in all three: particles, this.discard(), create the Cow (EntityType.create), copy health, position, name and
// persistence, then level.addFreshEntity(cow). The discard comes first, and it takes the moobloom out of the level
// (and out of the herd book's tracking, which would write a death) before the cow exists. So the cow is made here, at
// the head of the method, and the record is carried onto it while the moobloom is still alive; then the mod's own
// create call is given that cow instead of making another, and everything it copies and its addFreshEntity go on as
// they were. Should the create call ever not be found, the cow made here is simply never added and the mod's own cow
// has no record, which only loses the record. require = 0 loses a hook the mod renames instead of crashing the game;
// the plugin only lists this when the mod is there.
@Pseudo
@Mixin(targets = "com.faboslav.friendsandfoes.common.entity.MoobloomEntity", remap = false)
public class MoobloomConversionMixin {
    @Unique
    private Animal barnyardromance$cow;

    @Inject(method = "transformToCow", at = @At("HEAD"), require = 0)
    private void barnyardromance$carryRecord(ServerLevel level, CallbackInfo callback) {
        barnyardromance$cow = null;
        try {
            //? if >=1.21.2 {
            Animal cow = EntityTypes.COW.create(level, EntitySpawnReason.CONVERSION);
            //?} else
            //Animal cow = EntityTypes.COW.create(level);
            if (cow != null) {
                Conversions.carry((Animal) (Object) this, cow);
                barnyardromance$cow = cow;
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to carry a moobloom's record to the cow it became", exception);
        }
    }

    @ModifyExpressionValue(
            method = "transformToCow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;"
                    //? if >=1.21.2 {
                    + "Lnet/minecraft/world/entity/EntitySpawnReason;"
                    //?}
                    + ")Lnet/minecraft/world/entity/Entity;"),
            require = 0
    )
    private Entity barnyardromance$sameCow(Entity made) {
        Animal ours = barnyardromance$cow;
        barnyardromance$cow = null;
        return ours != null ? ours : made;
    }
}
