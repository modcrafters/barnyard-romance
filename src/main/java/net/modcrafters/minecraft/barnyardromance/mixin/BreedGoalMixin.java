package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Some breed goals override breed() without ever calling spawnChildFromBreeding (vanilla turtles, Naturalist's egg
// layers such as the ostrich), so the male is charged here, just before breed() runs. Courtship charges a pair once.
@Mixin(BreedGoal.class)
public class BreedGoalMixin {
    @Shadow
    @Final
    protected Animal animal;
    @Shadow
    protected Animal partner;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/BreedGoal;breed()V"))
    private void barnyardromance$mated(CallbackInfo callback) {
        if (this.partner != null) {
            Courtship.mated(this.animal, this.partner);
        }
    }
}
