package net.modcrafters.minecraft.barnyardromance.eggs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.core.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.eggs.api.RoosterNear;

import java.util.Optional;
import java.util.UUID;

final class EggsApiImpl implements EggsApi {
    @Override
    public boolean laysEggs(Animal animal) {
        return EggLaying.laysEggs(animal);
    }

    @Override
    public boolean isLayingHen(Animal animal) {
        return EggLaying.isLayingHen(animal);
    }

    @Override
    public RoosterNear roosterNear(Animal hen, ServerLevel level) {
        return EggLaying.roosterNear(hen, level);
    }

    @Override
    public boolean hasBoxWithRoomNear(Animal hen, ServerLevel level) {
        return EggLaying.boxWithRoomNear(hen, level);
    }

    @Override
    public Optional<Laying> laying(Animal hen) {
        return Services.DATA.get(hen, EggsFeature.LAYING);
    }

    @Override
    public boolean isInNest(UUID animal) {
        return NestingBoxes.inNest(animal);
    }

    @Override
    public EggOutlook eggsTomorrow(SizeClass size, boolean ateToday, boolean familiar, RoosterNear rooster, boolean boxWithRoom,
                                   EggSettings settings, String speciesId) {
        return EggRules.tomorrow(size, ateToday, familiar, rooster, boxWithRoom, settings, speciesId);
    }
}
