package net.modcrafters.minecraft.barnyardromance.neoforge.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHoverTooltip;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.client.OldHeartParticle;
import net.modcrafters.minecraft.barnyardromance.client.PregnancyHeartParticle;
import net.modcrafters.minecraft.barnyardromance.config.ClientConfig;
import net.modcrafters.minecraft.barnyardromance.look.HornShapes;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
//? if >=1.21.2 {
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
//?} else
//import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public final class BarnyardRomanceNeoForgeClient {
    private BarnyardRomanceNeoForgeClient() {
    }

    public static void register(IEventBus modBus) {
        ClientConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance-client.json"));
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerReloadListeners);
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerLayers);
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerParticles);
    }

    private static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_tooltip"),
                AnimalHoverTooltip::render
        );
    }

    //? if >=1.21.2 {
    private static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(HornShapes.ID, HornShapes.listener());
    }
    //?} else {
    /*private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(HornShapes.listener());
    }
    *///?}

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BarnyardParticles.PREGNANCY_HEART, PregnancyHeartParticle.Provider::new);
        event.registerSpriteSet(BarnyardParticles.OLD_HEART, OldHeartParticle.Provider::new);
    }
}
