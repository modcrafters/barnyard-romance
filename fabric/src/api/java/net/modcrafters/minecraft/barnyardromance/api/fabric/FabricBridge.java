package net.modcrafters.minecraft.barnyardromance.api.fabric;

import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;

// Re-fires every public API event as its Fabric API event, after the API's own listeners. The mod never listens here.
public final class FabricBridge {
    private FabricBridge() {
    }

    // Called once by Barnyard Romance as it starts.
    public static void register() {
        BarnyardEvents.TAMING.registerLate(event -> BarnyardFabricEvents.TAMING.invoker().onTaming(event));
        BarnyardEvents.MILKING.registerLate(event -> BarnyardFabricEvents.MILKING.invoker().onMilking(event));
        BarnyardEvents.DEATH_DROP.registerLate(event -> BarnyardFabricEvents.DEATH_DROP.invoker().onDeathDrop(event));
        BarnyardEvents.SHEAR_YIELD.registerLate(event -> BarnyardFabricEvents.SHEAR_YIELD.invoker().onShearYield(event));
        BarnyardEvents.FAMILIARITY_CHANGE.registerLate(event -> BarnyardFabricEvents.FAMILIARITY_CHANGE.invoker().onFamiliarityChange(event));
        BarnyardEvents.BEFRIENDED.registerLate(event -> BarnyardFabricEvents.BEFRIENDED.invoker().onBefriended(event));
        BarnyardEvents.BORN.registerLate(event -> BarnyardFabricEvents.BORN.invoker().onBorn(event));
        BarnyardEvents.HATCHED.registerLate(event -> BarnyardFabricEvents.HATCHED.invoker().onHatched(event));
    }
}
