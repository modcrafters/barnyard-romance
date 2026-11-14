package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A goat's long jump picks a spot it can't walk to, which takes it over a pen's fence. With goatLongJumps off, a goat
// the mod tracks never starts one. It gets a fresh cooldown the way vanilla's does when it can't jump, so its brain
// leaves the long jump activity and goes back to idling. Frogs use the same behaviour and keep it.
@Mixin(LongJumpToRandomPos.class)
public class GoatLongJumpMixin {
    @Shadow
    @Final
    private UniformInt timeBetweenLongJumps;

    @Inject(method = "checkExtraStartConditions", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$stayInThePen(ServerLevel level, Mob body, CallbackInfoReturnable<Boolean> callback) {
        BarnyardConfig config = BarnyardConfig.current();
        if (body instanceof Goat goat && !config.goatLongJumps() && config.speciesEnabled(AnimalSex.speciesId(goat))) {
            body.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, timeBetweenLongJumps.sample(level.getRandom()));
            callback.setReturnValue(false);
        }
    }
}
