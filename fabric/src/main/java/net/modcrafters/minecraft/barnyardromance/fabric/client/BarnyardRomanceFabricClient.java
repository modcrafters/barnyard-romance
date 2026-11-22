package net.modcrafters.minecraft.barnyardromance.fabric.client;

import net.modcrafters.minecraft.barnyardromance.client.FeederScreen;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxRenderer;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxScreen;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsBlocks;
import net.modcrafters.minecraft.barnyardromance.feed.FeedBlocks;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailFluids;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHoverTooltip;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.client.HerdBookClient;
import net.modcrafters.minecraft.barnyardromance.client.OldHeartParticle;
import net.modcrafters.minecraft.barnyardromance.client.PregnancyHeartParticle;
import net.modcrafters.minecraft.barnyardromance.core.config.ClientConfig;
import net.modcrafters.minecraft.barnyardromance.look.HeadShapes;
import net.modcrafters.minecraft.barnyardromance.look.HornShapes;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;

public class BarnyardRomanceFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance-client.json"));
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(HornShapes.ID, HornShapes.listener());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(HeadShapes.ID, HeadShapes.listener());
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
        MenuScreens.register(EggsBlocks.NESTING_BOX_MENU, NestingBoxScreen::new);
        MenuScreens.register(FeedBlocks.FEEDER_MENU, FeederScreen::new);
        BlockEntityRenderers.register(EggsBlocks.NESTING_BOX_ENTITY, NestingBoxRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(HerdBookPayloads.ShowBook.TYPE, (payload, context) -> HerdBookClient.show(payload));
        // Milk can't be placed, so this is for other mods' tanks, which draw a fluid's still and flowing textures.
        for (Fluid milk : MilkFluids.own()) {
            FluidRenderingRegistry.register(milk, new FluidModel.Unbaked(new Material(MilkFluids.STILL_TEXTURE),
                    new Material(MilkFluids.FLOW_TEXTURE), null, BlockTintSources.constant(0xFF000000 | PailFluids.colour(milk))));
        }
    }
}
