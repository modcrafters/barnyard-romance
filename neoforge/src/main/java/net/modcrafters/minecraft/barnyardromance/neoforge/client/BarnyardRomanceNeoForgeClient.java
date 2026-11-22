package net.modcrafters.minecraft.barnyardromance.neoforge.client;

import net.modcrafters.minecraft.barnyardromance.client.FeederScreen;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxRenderer;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxScreen;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsBlocks;
import net.modcrafters.minecraft.barnyardromance.feed.FeedBlocks;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailFluids;
import net.minecraft.world.level.material.Fluid;
//? if >=26.1 {
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
//?} else {
/*import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
*///?}
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
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
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
//? if >=1.21.6
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
//? if <1.21.2 {
/*import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
*///?}
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
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerScreens);
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerRenderers);
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerMilkTextures);
        //? if >=1.21.6
        modBus.addListener(BarnyardRomanceNeoForgeClient::registerPayloads);
        //? if <1.21.2
        //modBus.addListener(BarnyardRomanceNeoForgeClient::registerItemColours);
    }

    // 1.21.2 and later tint in the item's own file (items/copper_pail.json); before that the client tints in code.
    // The numbers match those files.
    //? if <1.21.2 {
    /*private static void registerItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | 0xC15A36 : -1, PailItems.COPPER_PAIL);
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | 0xD8D8D8 : -1, PailItems.IRON_PAIL);
    }
    *///?}

    // Milk can't be placed, so this is for other mods' tanks, which draw a fluid's still and flowing textures. 26.1 moved
    // them from the fluid type's client extension to a fluid model.
    //? if >=26.1 {
    private static void registerMilkTextures(RegisterFluidModelsEvent event) {
        for (Fluid milk : MilkFluids.own()) {
            event.register(new FluidModel.Unbaked(new Material(MilkFluids.STILL_TEXTURE), new Material(MilkFluids.FLOW_TEXTURE), null,
                    FluidTintSources.constant(0xFF000000 | PailFluids.colour(milk))), milk);
        }
    }
    //?} else {
    /*private static void registerMilkTextures(RegisterClientExtensionsEvent event) {
        for (Fluid milk : MilkFluids.own()) {
            int tint = 0xFF000000 | PailFluids.colour(milk);
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public Identifier getStillTexture() {
                    return MilkFluids.STILL_TEXTURE;
                }

                @Override
                public Identifier getFlowingTexture() {
                    return MilkFluids.FLOW_TEXTURE;
                }

                @Override
                public int getTintColor() {
                    return tint;
                }
            }, milk.getFluidType());
        }
    }
    *///?}

    //? if >=1.21.6 {
    private static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(HerdBookPayloads.ShowBook.TYPE, (payload, context) -> HerdBookClient.show(payload));
    }
    //?}

    private static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_tooltip"),
                AnimalHoverTooltip::render
        );
    }

    //? if >=1.21.2 {
    private static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(HornShapes.ID, HornShapes.listener());
        event.addListener(HeadShapes.ID, HeadShapes.listener());
    }
    //?} else {
    /*private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(HornShapes.listener());
        event.registerReloadListener(HeadShapes.listener());
    }
    *///?}

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(EggsBlocks.NESTING_BOX_ENTITY, NestingBoxRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(EggsBlocks.NESTING_BOX_MENU, NestingBoxScreen::new);
        event.register(FeedBlocks.FEEDER_MENU, FeederScreen::new);
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BarnyardParticles.PREGNANCY_HEART, PregnancyHeartParticle.Provider::new);
        event.registerSpriteSet(BarnyardParticles.OLD_HEART, OldHeartParticle.Provider::new);
    }
}
