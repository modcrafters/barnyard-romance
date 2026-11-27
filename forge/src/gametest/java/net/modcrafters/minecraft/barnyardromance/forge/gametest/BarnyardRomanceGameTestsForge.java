package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
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
        MinecraftForge.EVENT_BUS.addListener(BarnyardRomanceGameTestsForge::testRules);
        ForgeApiGameTests.listen();
        ForgeSharedGameTests.listen();
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        event.register(ForgeDataGameTests.class);
        event.register(ForgeContainerGameTests.class);
        event.register(ForgeApiGameTests.class);
        event.register(ForgeSharedGameTests.class);
    }

    // Forge 1.20.1 starts its game test server on a dedicated server's world settings (see forge/build.gradle for the
    // world) with every game rule at its default. These are the rules the newer versions' game test server sets: no mobs
    // spawning into the pens, no weather, nothing growing or spreading by random ticks, and no fire spreading.
    private static void testRules(ServerStartingEvent event) {
        GameRules rules = event.getServer().getGameRules();
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, event.getServer());
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, event.getServer());
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, event.getServer());
        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, event.getServer());
    }
}
