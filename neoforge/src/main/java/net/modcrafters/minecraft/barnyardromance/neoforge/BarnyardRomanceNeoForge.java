package net.modcrafters.minecraft.barnyardromance.neoforge;

import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.food.FoodReloadListeners;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
//? if <1.21.6
//import net.modcrafters.minecraft.barnyardromance.client.HerdBookClient;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.neoforge.client.BarnyardRomanceNeoForgeClient;
import net.modcrafters.minecraft.barnyardromance.neoforge.data.NeoForgeAnimalData;
import net.modcrafters.minecraft.barnyardromance.neoforge.gametest.NeoForgeBreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
//? if >=1.21.2 {
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
//?} else
//import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class BarnyardRomanceNeoForge {
    public BarnyardRomanceNeoForge(IEventBus modBus) {
        NeoForgeAnimalData.ATTACHMENT_TYPES.register(modBus);
        modBus.addListener(BarnyardRomanceNeoForge::registerContent);
        modBus.addListener(BarnyardRomanceNeoForge::addToCreativeTabs);
        modBus.addListener(BarnyardRomanceNeoForge::registerPayloads);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(BarnyardRomance::setup));
        NeoForgeBreedingGameTests.register(modBus);
        //? if >=1.21.9 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
        //?} else
        //if (FMLEnvironment.dist == Dist.CLIENT) {
            BarnyardRomanceNeoForgeClient.register(modBus);
        }
        BarnyardRomance.init();
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onServerTick);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onAddReloadListeners);
    }

    private static void registerContent(RegisterEvent event) {
        event.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_tool"),
                () -> BarnyardComponents.HERD_TOOL
        );
        event.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book"),
                () -> BarnyardComponents.HERD_BOOK
        );
        event.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "egg_data"),
                () -> BarnyardComponents.EGG_DATA
        );
        event.register(Registries.ITEM, BarnyardItems.HERD_STICK_ID.identifier(), () -> BarnyardItems.HERD_STICK);
        event.register(Registries.ITEM, BarnyardItems.INSPECTOR_ID.identifier(), () -> BarnyardItems.INSPECTOR);
        event.register(Registries.ITEM, BarnyardItems.HERD_BOOK_ID.identifier(), () -> BarnyardItems.HERD_BOOK);
        event.register(Registries.ITEM, BarnyardItems.BREEDERS_MASH_ID.identifier(), () -> BarnyardItems.BREEDERS_MASH);
        event.register(Registries.ITEM, BarnyardItems.GOLDEN_BRAN_ID.identifier(), () -> BarnyardItems.GOLDEN_BRAN);
        event.register(Registries.ITEM, BarnyardItems.DAIRY_MASH_ID.identifier(), () -> BarnyardItems.DAIRY_MASH);
        event.register(Registries.ITEM, BarnyardItems.SHEEP_MILK_BUCKET_ID.identifier(), () -> BarnyardItems.SHEEP_MILK_BUCKET);
        event.register(Registries.ITEM, BarnyardItems.GOAT_MILK_BUCKET_ID.identifier(), () -> BarnyardItems.GOAT_MILK_BUCKET);
        event.register(Registries.ITEM, BarnyardItems.FERTILIZED_EGG_ID.identifier(), () -> BarnyardItems.FERTILIZED_EGG);
        event.register(Registries.CREATIVE_MODE_TAB, BarnyardTab.ID.identifier(), () -> BarnyardTab.titled(CreativeModeTab.builder())
                .displayItems((parameters, output) -> BarnyardTab.contents().forEach(output::accept))
                .build());
        event.register(
                Registries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pregnancy_heart"),
                () -> BarnyardParticles.PREGNANCY_HEART
        );
        event.register(
                Registries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "old_heart"),
                () -> BarnyardParticles.OLD_HEART
        );
    }

    // From 1.21.6 on, the client registers its own handler for ShowBook (see BarnyardRomanceNeoForgeClient).
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        //? if >=1.21.6 {
        registrar.playToClient(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.STREAM_CODEC);
        //?} else
        //registrar.playToClient(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.STREAM_CODEC, (payload, context) -> HerdBookClient.show(payload));
        registrar.playToServer(HerdBookPayloads.RemoveFromBook.TYPE, HerdBookPayloads.RemoveFromBook.STREAM_CODEC,
                (payload, context) -> HerdBooks.removeRequested(context.player(), payload));
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BarnyardItems.FOOD_AND_DRINKS) {
            event.accept(BarnyardItems.BREEDERS_MASH);
            event.accept(BarnyardItems.GOLDEN_BRAN);
            event.accept(BarnyardItems.DAIRY_MASH);
        }
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        AnimalSex.onJoin(animal);
    }

    private static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        HerdBooks.leftLevel(animal, animal.getRemovalReason());
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        HerdBooks.tick(event.getServer(), event.getServer().getTickCount());
    }

    //? if >=1.21.2 {
    private static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(FoodReloadListeners.DIETS_ID, FoodReloadListeners.diets());
        event.addListener(FoodReloadListeners.FOODS_ID, FoodReloadListeners.foods());
    }
    //?} else {
    /*private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FoodReloadListeners.diets());
        event.addListener(FoodReloadListeners.foods());
    }
    *///?}

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        BarnyardCommands.register(event.getDispatcher());
    }
}
