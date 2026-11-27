package net.modcrafters.minecraft.barnyardromance.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHoverTooltip;
import net.modcrafters.minecraft.barnyardromance.client.FeederScreen;
import net.modcrafters.minecraft.barnyardromance.client.HerdBookClient;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxRenderer;
import net.modcrafters.minecraft.barnyardromance.client.NestingBoxScreen;
import net.modcrafters.minecraft.barnyardromance.client.OldHeartParticle;
import net.modcrafters.minecraft.barnyardromance.client.PailScreen;
import net.modcrafters.minecraft.barnyardromance.client.PregnancyHeartParticle;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.LegacyGui;
import net.modcrafters.minecraft.barnyardromance.core.config.ClientConfig;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsBlocks;
import net.modcrafters.minecraft.barnyardromance.feed.FeedBlocks;
import net.modcrafters.minecraft.barnyardromance.forge.data.AnimalDataSync;
import net.modcrafters.minecraft.barnyardromance.forge.platform.ForgeNetworkService;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.look.HeadShapes;
import net.modcrafters.minecraft.barnyardromance.look.HornShapes;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import org.jspecify.annotations.Nullable;

public final class BarnyardRomanceForgeClient {
    // The metal pails' own tints when a stack has none, as the newer versions' item definitions give them.
    private static final int COPPER_TINT = 0xFFFF9C70;
    private static final int IRON_TINT = 0xFFCCD1DE;

    private BarnyardRomanceForgeClient() {
    }

    public static void register(IEventBus modBus) {
        ClientConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance-client.json"));
        modBus.addListener(BarnyardRomanceForgeClient::registerReloadListeners);
        modBus.addListener(BarnyardRomanceForgeClient::registerOverlays);
        modBus.addListener(BarnyardRomanceForgeClient::registerParticles);
        modBus.addListener(BarnyardRomanceForgeClient::registerRenderers);
        modBus.addListener(BarnyardRomanceForgeClient::registerItemColours);
        // Forge 1.20.1 has no event for menu screens; they're registered in client setup.
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(BarnyardRomanceForgeClient::registerScreens));
        ForgeNetworkService.onClient(HerdBookPayloads.ShowBook.TYPE, HerdBookClient::show);
        ForgeNetworkService.onClient(AnimalDataSync.Payload.TYPE,
                payload -> AnimalDataSync.apply(payload, level() == null ? null : level().getEntity(payload.entityId())));
    }

    // The client's registries, for reading payloads when there's no server in this process.
    public static @Nullable RegistryAccess registryAccess() {
        return level() == null ? null : level().registryAccess();
    }

    private static net.minecraft.client.multiplayer.@Nullable ClientLevel level() {
        return Minecraft.getInstance().level;
    }

    // 1.20.1 has no data-driven item tints: a pail's tint is in its tag's display.color (see MetalPail), as a dyed item's
    // colour is, and the item colour reads it from there.
    private static void registerItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? tint(stack, COPPER_TINT) : -1, PailItems.COPPER_PAIL);
        event.register((stack, tintIndex) -> tintIndex == 0 ? tint(stack, IRON_TINT) : -1, PailItems.IRON_PAIL);
    }

    private static int tint(ItemStack stack, int fallback) {
        CompoundTag display = stack.getTagElement("display");
        return display != null && display.contains("color", Tag.TAG_ANY_NUMERIC) ? 0xFF000000 | display.getInt("color") : fallback;
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("animal_tooltip", (gui, graphics, partialTick, width, height) -> AnimalHoverTooltip.render(graphics, partialTick));
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(HornShapes.listener());
        event.registerReloadListener(HeadShapes.listener());
        event.registerReloadListener(LegacyGui.reloadListener());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(EggsBlocks.NESTING_BOX_ENTITY, NestingBoxRenderer::new);
    }

    private static void registerScreens() {
        MenuScreens.register(EggsBlocks.NESTING_BOX_MENU, NestingBoxScreen::new);
        MenuScreens.register(FeedBlocks.FEEDER_MENU, FeederScreen::new);
        MenuScreens.register(PailItems.PAIL_MENU, PailScreen::new);
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BarnyardParticles.PREGNANCY_HEART, PregnancyHeartParticle.Provider::new);
        event.registerSpriteSet(BarnyardParticles.OLD_HEART, OldHeartParticle.Provider::new);
    }
}
