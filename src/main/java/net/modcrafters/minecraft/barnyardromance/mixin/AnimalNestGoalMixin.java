package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.nest.LayInNestGoal;
import net.modcrafters.minecraft.barnyardromance.rivalry.FightGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Every animal gets the nesting goal, so turning eggs on for a species in the config works without a respawn. The goal
// does nothing until EggLaying sends an animal on a trip, and checking for one is only a map lookup. The fight goal is
// the same: it waits for Fights to give the animal a rival, and comes before everything else so nothing pulls him away.
@Mixin(Animal.class)
public class AnimalNestGoalMixin {
    private static final int PRIORITY = 2;
    private static final int FIGHT_PRIORITY = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void barnyardromance$nestGoal(CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        if (!self.level().isClientSide()) {
            ((MobGoalsAccessor) self).barnyardromance$goalSelector().addGoal(PRIORITY, new LayInNestGoal(self));
            ((MobGoalsAccessor) self).barnyardromance$goalSelector().addGoal(FIGHT_PRIORITY, new FightGoal(self));
        }
    }
}
