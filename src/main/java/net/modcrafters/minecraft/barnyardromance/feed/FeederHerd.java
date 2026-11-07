package net.modcrafters.minecraft.barnyardromance.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// A feeding block's herd: the animals that eat from it, saved with the block. Each one costs a path search to find
// (HerdRefresh), so the herd is found again now and then, never every tick: once a minute on the block's own tick, as
// herd books are, and once when the block is placed. Plain Java, so the unit tests can load it.
public final class FeederHerd {
    public static final int REFRESH_TICKS = 1200;

    private final List<UUID> members = new ArrayList<>();
    private final Set<UUID> lookup = new HashSet<>();

    public List<UUID> members() {
        return Collections.unmodifiableList(members);
    }

    public boolean contains(UUID id) {
        return lookup.contains(id);
    }

    public int size() {
        return members.size();
    }

    void set(Collection<UUID> ids) {
        members.clear();
        lookup.clear();
        for (UUID id : ids) {
            if (lookup.add(id)) {
                members.add(id);
            }
        }
    }

    // Spread over the minute by the block's position, so a farm's blocks don't all search on the same tick.
    public static int refreshSlot(int positionHash) {
        return Math.floorMod(positionHash, REFRESH_TICKS);
    }

    public static boolean due(long gameTime, int slot) {
        return Math.floorMod(gameTime, (long) REFRESH_TICKS) == slot;
    }
}
