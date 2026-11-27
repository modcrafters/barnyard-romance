package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The game test mod. Only the game test runs load it, so none of the tests ship in the mod's jar. Each test class
// is registered here.
@Mod(BarnyardRomanceGameTestsForge.MOD_ID)
public class BarnyardRomanceGameTestsForge {
    public static final String MOD_ID = "barnyard_romance_gametest";

    public BarnyardRomanceGameTestsForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(BarnyardRomanceGameTestsForge::registerTests);
        ForgeApiGameTests.listen();
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        event.register(ForgeDataGameTests.class);
        event.register(ForgeContainerGameTests.class);
        event.register(ForgeApiGameTests.class);
    }
}
