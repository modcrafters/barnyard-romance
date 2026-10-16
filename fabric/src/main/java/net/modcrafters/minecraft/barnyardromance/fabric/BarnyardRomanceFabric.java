package net.modcrafters.minecraft.barnyardromance.fabric;

import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.fabric.data.FabricAnimalData;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.animal.Animal;

public class BarnyardRomanceFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricAnimalData.ANIMAL_TRAITS.identifier();
        FabricAnimalData.PREGNANCY.identifier();
        BarnyardRomance.init();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Animal animal) {
                AnimalSex.onJoin(animal);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> BarnyardCommands.register(dispatcher));
    }
}
