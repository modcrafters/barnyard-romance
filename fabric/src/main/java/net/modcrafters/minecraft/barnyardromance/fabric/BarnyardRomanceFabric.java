package net.modcrafters.minecraft.barnyardromance.fabric;

import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.fabric.data.FabricAnimalData;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.HERD_STICK_ID.identifier(), BarnyardItems.HERD_STICK);
        Registry.register(BuiltInRegistries.ITEM, BarnyardItems.INSPECTOR_ID.identifier(), BarnyardItems.INSPECTOR);
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pregnancy_heart"),
                BarnyardParticles.PREGNANCY_HEART
        );
        BarnyardRomance.init();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Animal animal) {
                AnimalSex.onJoin(animal);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> BarnyardCommands.register(dispatcher));
    }
}
