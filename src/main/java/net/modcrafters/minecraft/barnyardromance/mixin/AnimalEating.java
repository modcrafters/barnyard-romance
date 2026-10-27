package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Animal.class)
public interface AnimalEating {
    @Invoker("playEatingSound")
    void barnyardromance$playEatingSound();
}
