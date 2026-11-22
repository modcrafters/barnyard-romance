package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.animal.equine.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Vanilla keeps a llama's strength private; trader llamas are given theirs when they first get traits.
@Mixin(Llama.class)
public interface LlamaAccessor {
    @Invoker("setStrength")
    void barnyardromance$setStrength(int strength);
}
