package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.ServerScoped;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// The feeding blocks loaded now and the herds they hold, so an animal can tell whether a block is about to settle its
// missed days for it (see FamiliarityCare.settle). It's kept in memory only: blocks add themselves as they load and
// take themselves out as they unload or break.
public final class FeederIndex {
    private static final Map<GlobalPos, FeederBlockEntity> LOADED = new HashMap<>();
    // The herd each block was indexed with, so it can be taken out again exactly.
    private static final Map<GlobalPos, List<UUID>> INDEXED = new HashMap<>();
    private static final Map<UUID, Set<GlobalPos>> FEEDERS = new HashMap<>();

    private FeederIndex() {
    }

    // Adds the block with its herd as it is now, in place of what was there before.
    static synchronized void add(FeederBlockEntity feeder) {
        GlobalPos pos = feeder.globalPos();
        if (pos == null) {
            return;
        }
        drop(pos);
        List<UUID> herd = List.copyOf(feeder.herd());
        LOADED.put(pos, feeder);
        INDEXED.put(pos, herd);
        for (UUID id : herd) {
            FEEDERS.computeIfAbsent(id, key -> new HashSet<>()).add(pos);
        }
    }

    // Only this block: a block that has since taken its place stays.
    static synchronized void remove(FeederBlockEntity feeder) {
        GlobalPos pos = feeder.globalPos();
        if (pos != null && LOADED.get(pos) == feeder) {
            drop(pos);
        }
    }

    // When the server stops: its blocks are never taken out one by one, and would hold on to the whole world.
    static {
        ServerScoped.onStop(FeederIndex::clear);
    }

    private static synchronized void clear() {
        LOADED.clear();
        INDEXED.clear();
        FEEDERS.clear();
    }

    public static synchronized Set<GlobalPos> feedersOf(UUID id) {
        Set<GlobalPos> feeders = FEEDERS.get(id);
        return feeders == null ? Set.of() : Set.copyOf(feeders);
    }

    // A block that holds this animal, is running and hasn't settled the days before today yet is about to do it.
    public static synchronized boolean waiting(Animal animal, long today) {
        Set<GlobalPos> feeders = FEEDERS.get(animal.getUUID());
        if (feeders == null) {
            return false;
        }
        for (GlobalPos pos : feeders) {
            FeederBlockEntity feeder = LOADED.get(pos);
            if (feeder != null && feeder.running(animal) && feeder.settledDay() < today) {
                return true;
            }
        }
        return false;
    }

    // Whether a running block holds this animal. One that is loaded but not running (at the edge of the loaded area,
    // outside the world border, or waiting for the animals around it to load) may never get to the animal, so it
    // doesn't count.
    public static synchronized boolean heldByRunning(Animal animal) {
        Set<GlobalPos> feeders = FEEDERS.get(animal.getUUID());
        if (feeders == null) {
            return false;
        }
        for (GlobalPos pos : feeders) {
            FeederBlockEntity feeder = LOADED.get(pos);
            if (feeder != null && feeder.running(animal)) {
                return true;
            }
        }
        return false;
    }

    private static void drop(GlobalPos pos) {
        LOADED.remove(pos);
        List<UUID> herd = INDEXED.remove(pos);
        if (herd == null) {
            return;
        }
        for (UUID id : herd) {
            Set<GlobalPos> feeders = FEEDERS.get(id);
            if (feeders != null) {
                feeders.remove(pos);
                if (feeders.isEmpty()) {
                    FEEDERS.remove(id);
                }
            }
        }
    }
}
