package net.modcrafters.minecraft.barnyardromance.neoforge.gametest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// The game test mod. Only the game test runs load it, so none of the tests ship in the mod's jar.
@Mod(BarnyardRomanceGameTestsNeoForge.MOD_ID)
public class BarnyardRomanceGameTestsNeoForge {
    public static final String MOD_ID = "barnyard_romance_gametest";

    public BarnyardRomanceGameTestsNeoForge(IEventBus modBus) {
        NeoForgeBreedingGameTests.register(modBus);
    }
}
