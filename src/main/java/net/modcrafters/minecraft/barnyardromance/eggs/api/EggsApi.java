package net.modcrafters.minecraft.barnyardromance.eggs.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.core.config.EggSettings;

import java.util.Optional;
import java.util.UUID;

// What other features ask of eggs.
public interface EggsApi {
    // Whether the animal's species lays the mod's eggs (by the config's egg settings).
    boolean laysEggs(Animal animal);

    // Whether she lays now: an adult or old female of a species that lays. Chicks, young hens and roosters don't.
    boolean isLayingHen(Animal animal);

    // The best adult rooster in range of the hen, by whether he ate today (so he's fertile tomorrow).
    RoosterNear roosterNear(Animal hen, ServerLevel level);

    // Whether a nesting box in range of the hen has room for a fertilized egg.
    boolean hasBoxWithRoomNear(Animal hen, ServerLevel level);

    // The hen's eggs for the day she last planned, if she has planned one.
    Optional<Laying> laying(Animal hen);

    // Whether the animal is sitting in a nesting box, out of the world for now.
    boolean isInNest(UUID animal);

    // What a hen will lay tomorrow, and why it isn't better, for the herd book.
    EggOutlook eggsTomorrow(SizeClass size, boolean ateToday, boolean familiar, RoosterNear rooster, boolean boxWithRoom,
                            EggSettings settings, String speciesId);

    // How far past midnight the day is, in ticks, on the overworld clock: the time of day hens lay by.
    int ticksSinceMidnight(long clock);
}
