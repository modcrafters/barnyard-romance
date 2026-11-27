package net.modcrafters.minecraft.barnyardromance.forge.mixin;

import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// The capabilities an entity holds, whether or not Forge still hands them out (it stops once the entity is removed).
// See ForgeAnimalData.of. Forge's own class, so its names aren't remapped.
@Mixin(value = CapabilityProvider.class, remap = false)
public interface CapabilityProviderAccessor {
    @Invoker("getCapabilities")
    @Nullable CapabilityDispatcher barnyardromance$capabilities();
}
