package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

// Filters each player out of the goal's search, so one player a shy animal accepts doesn't hide another it would run
// from. Subclasses, such as Naturalist's boar goal, call through to this canUse.
@Mixin(AvoidEntityGoal.class)
public class AvoidPlayerMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @ModifyArg(
            method = "canUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"),
            index = 2
    )
    private Predicate<Object> barnyardromance$onlyPlayersItFears(Predicate<Object> original) {
        return entity -> original.test(entity)
                && !(this.mob instanceof Animal animal && entity instanceof Player player && !FamiliarityActions.runsFrom(animal, player));
    }
}
