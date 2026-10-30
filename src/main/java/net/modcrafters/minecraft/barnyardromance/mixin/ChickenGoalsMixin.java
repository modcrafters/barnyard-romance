package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.nest.LayInNestGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hens walk to a nesting box to lay; only panicking and floating come first.
@Mixin(Chicken.class)
public class ChickenGoalsMixin {
    private static final int PRIORITY = 2;

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void barnyardromance$nestGoal(CallbackInfo callback) {
        Chicken self = (Chicken) (Object) this;
        ((MobGoalsAccessor) self).barnyardromance$goalSelector().addGoal(PRIORITY, new LayInNestGoal(self));
    }
}
