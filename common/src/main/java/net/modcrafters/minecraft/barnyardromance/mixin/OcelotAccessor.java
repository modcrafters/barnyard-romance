package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.animal.feline.Ocelot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Vanilla keeps an ocelot's trust private; familiarity sets it instead of the one-in-three fish roll.
@Mixin(Ocelot.class)
public interface OcelotAccessor {
    @Invoker("isTrusting")
    boolean barnyardromance$isTrusting();

    @Invoker("setTrusting")
    void barnyardromance$setTrusting(boolean trusting);
}
