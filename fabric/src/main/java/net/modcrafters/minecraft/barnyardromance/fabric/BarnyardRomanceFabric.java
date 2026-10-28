package net.modcrafters.minecraft.barnyardromance.fabric;

import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.food.FoodReloadListeners;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.fabric.data.FabricAnimalData;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.animal.Animal;

public class BarnyardRomanceFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricAnimalData.ANIMAL_TRAITS.identifier();
        FabricAnimalData.PREGNANCY.identifier();
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_tool"),
                BarnyardComponents.HERD_TOOL
        );
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book"),
                BarnyardComponents.HERD_BOOK
        );
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.HERD_STICK_ID.identifier(), BarnyardItems.HERD_STICK);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.INSPECTOR_ID.identifier(), BarnyardItems.INSPECTOR);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.HERD_BOOK_ID.identifier(), BarnyardItems.HERD_BOOK);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.BREEDERS_MASH_ID.identifier(), BarnyardItems.BREEDERS_MASH);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.GOLDEN_BRAN_ID.identifier(), BarnyardItems.GOLDEN_BRAN);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.SHEEP_MILK_BUCKET_ID.identifier(), BarnyardItems.SHEEP_MILK_BUCKET);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.GOAT_MILK_BUCKET_ID.identifier(), BarnyardItems.GOAT_MILK_BUCKET);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BarnyardTab.ID, BarnyardTab.titled(FabricCreativeModeTab.builder())
                .displayItems((parameters, output) -> BarnyardTab.contents().forEach(output::accept))
                .build());
        CreativeModeTabEvents.modifyOutputEvent(BarnyardItems.FOOD_AND_DRINKS).register(output -> {
            output.accept(BarnyardItems.BREEDERS_MASH);
            output.accept(BarnyardItems.GOLDEN_BRAN);
        });
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pregnancy_heart"),
                BarnyardParticles.PREGNANCY_HEART
        );
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "old_heart"),
                BarnyardParticles.OLD_HEART
        );
        BarnyardRomance.init();
        ResourceLoader data = ResourceLoader.get(PackType.SERVER_DATA);
        data.registerReloadListener(FoodReloadListeners.DIETS_ID, FoodReloadListeners.diets());
        data.registerReloadListener(FoodReloadListeners.FOODS_ID, FoodReloadListeners.foods());
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Animal animal) {
                AnimalSex.onJoin(animal);
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Animal animal && entity.getRemovalReason() != null) {
                HerdBooks.leftLevel(animal, entity.getRemovalReason());
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> HerdBooks.tick(server, server.getTickCount()));
        PayloadTypeRegistry.clientboundPlay().register(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(HerdBookPayloads.RemoveFromBook.TYPE, HerdBookPayloads.RemoveFromBook.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(HerdBookPayloads.RemoveFromBook.TYPE,
                (payload, context) -> HerdBooks.removeRequested(context.player(), payload));
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> BarnyardCommands.register(dispatcher));
    }
}
