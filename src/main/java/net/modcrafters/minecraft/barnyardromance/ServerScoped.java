package net.modcrafters.minecraft.barnyardromance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

// State that belongs to one running server: the animals, boxes and feeders of the world it has open. All of it is
// cleared when the server stops, so a world opened next in the same game starts with none of it (entity UUIDs and
// game times carry over between worlds, so stale entries would match). Wrap a static cache where it's declared:
//     private static final Map<UUID, State> STATES = ServerScoped.track(new ConcurrentHashMap<>());
// A class registers when it first loads, and until then it has nothing to clear. ServerScopedScanTest fails on a
// static collection that's neither tracked nor listed as outliving the server.
public final class ServerScoped {
    private static final List<Runnable> CLEARS = new CopyOnWriteArrayList<>();

    private ServerScoped() {
    }

    public static <M extends Map<?, ?>> M track(M map) {
        CLEARS.add(map::clear);
        return map;
    }

    public static <C extends Collection<?>> C track(C collection) {
        CLEARS.add(collection::clear);
        return collection;
    }

    // For state a class has to clear itself, such as under its own lock.
    public static void onStop(Runnable clear) {
        CLEARS.add(clear);
    }

    public static void clearAll() {
        CLEARS.forEach(Runnable::run);
    }
}
