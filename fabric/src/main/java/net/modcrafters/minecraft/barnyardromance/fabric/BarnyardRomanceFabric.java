package net.modcrafters.minecraft.barnyardromance.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.food.FoodReloadListeners;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.fabric.data.FabricAnimalData;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.animal.Animal;

import java.util.Objects;
import java.util.function.Supplier;

public class BarnyardRomanceFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricAnimalData.ANIMAL_TRAITS.identifier();
        FabricAnimalData.PREGNANCY.identifier();
        FabricAnimalData.LAYING.identifier();
        BarnyardRegistrations.registerAll(BarnyardRomanceFabric::register, () -> BarnyardTab.titled(FabricCreativeModeTab.builder())
                .displayItems((parameters, output) -> BarnyardTab.contents().forEach(output::accept))
                .build());
        CreativeModeTabEvents.modifyOutputEvent(BarnyardItems.FOOD_AND_DRINKS).register(output -> BarnyardTab.foodAndDrinks().forEach(output::accept));
        BarnyardRomance.init();
        BarnyardRomance.setup();
        ResourceLoader data = ResourceLoader.get(PackType.SERVER_DATA);
        data.registerReloadListener(FoodReloadListeners.DIETS_ID, FoodReloadListeners.diets());
        data.registerReloadListener(FoodReloadListeners.FOODS_ID, FoodReloadListeners.foods());
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> FoodData.tagsReloaded());
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Animal animal) {
                AnimalSex.onJoin(animal);
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Animal animal) {
                HerdBooks.leftLevel(animal, entity.getRemovalReason());
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> HerdBooks.tick(server, server.getTickCount()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> BarnyardRomance.serverStopped());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> BarnyardAdvancements.loggedIn(handler.player));
        PayloadTypeRegistry.clientboundPlay().registerLarge(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.STREAM_CODEC,
                HerdBookPayloads.ShowBook.MAX_BYTES);
        PayloadTypeRegistry.serverboundPlay().register(HerdBookPayloads.RemoveFromBook.TYPE, HerdBookPayloads.RemoveFromBook.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(HerdBookPayloads.RemoveFromBook.TYPE,
                (payload, context) -> HerdBooks.removeRequested(context.player(), payload));
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> BarnyardCommands.register(dispatcher));
    }

    @SuppressWarnings("unchecked")
    private static <T> void register(ResourceKey<? extends Registry<T>> registry, Identifier id, Supplier<? extends T> value) {
        Registry<T> target = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
        Registry.register(Objects.requireNonNull(target, registry::toString), id, value.get());
    }
}
