package net.modcrafters.minecraft.barnyardromance.fabric.client;

import net.modcrafters.minecraft.barnyardromance.client.NestingBoxRenderer;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxScreen;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHoverTooltip;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.client.HerdBookClient;
import net.modcrafters.minecraft.barnyardromance.client.OldHeartParticle;
import net.modcrafters.minecraft.barnyardromance.client.PregnancyHeartParticle;
import net.modcrafters.minecraft.barnyardromance.config.ClientConfig;
import net.modcrafters.minecraft.barnyardromance.look.HornShapes;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;

public class BarnyardRomanceFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance-client.json"));
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(HornShapes.ID, HornShapes.listener());
        ParticleProviderRegistry.getInstance().register(
                BarnyardParticles.PREGNANCY_HEART,
                PregnancyHeartParticle.Provider::new
        );
        ParticleProviderRegistry.getInstance().register(
                BarnyardParticles.OLD_HEART,
                OldHeartParticle.Provider::new
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_tooltip"),
                AnimalHoverTooltip::render
        );
        MenuScreens.register(BarnyardBlocks.NESTING_BOX_MENU, NestingBoxScreen::new);
        BlockEntityRenderers.register(BarnyardBlocks.NESTING_BOX_ENTITY, NestingBoxRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(HerdBookPayloads.ShowBook.TYPE, (payload, context) -> HerdBookClient.show(payload));
    }
}
