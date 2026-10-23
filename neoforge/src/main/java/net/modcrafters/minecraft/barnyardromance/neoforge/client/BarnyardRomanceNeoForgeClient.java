package net.modcrafters.minecraft.barnyardromance.neoforge.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHoverTooltip;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.client.PregnancyHeartParticle;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class BarnyardRomanceNeoForgeClient {
    private BarnyardRomanceNeoForgeClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerLayers);
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerParticles);
    }

    private static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_tooltip"),
                AnimalHoverTooltip::render
        );
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BarnyardParticles.PREGNANCY_HEART, PregnancyHeartParticle.Provider::new);
    }
}
