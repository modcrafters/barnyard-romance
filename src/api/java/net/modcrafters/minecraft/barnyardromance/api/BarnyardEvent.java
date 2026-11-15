package net.modcrafters.minecraft.barnyardromance.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * One of Barnyard Romance's events and its listeners. They run synchronously on the server thread, in the order they
 * registered. A listener that throws is logged and the rest still run; whatever it changed or cancelled before it
 * threw stays. Register listeners while your mod starts up.
 */
public final class BarnyardEvent<E> {
    private static final Logger LOG = LoggerFactory.getLogger("barnyard_romance");

    private final String name;
    private final List<Consumer<? super E>> listeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<? super E>> late = new CopyOnWriteArrayList<>();

    public BarnyardEvent(String name) {
        this.name = name;
    }

    public void register(Consumer<? super E> listener) {
        listeners.add(listener);
    }

    /**
     * For Barnyard Romance's loader layers, which re-fire the event as a NeoForge or Fabric event: they run after
     * every ordinary listener, so they see what those changed.
     */
    public void registerLate(Consumer<? super E> listener) {
        late.add(listener);
    }

    /**
     * For Barnyard Romance only: other mods listen, they don't post.
     */
    public E post(E event) {
        deliver(listeners, event);
        deliver(late, event);
        return event;
    }

    private void deliver(List<Consumer<? super E>> to, E event) {
        for (Consumer<? super E> listener : to) {
            try {
                listener.accept(event);
            } catch (RuntimeException exception) {
                LOG.warn("A {} listener failed", name, exception);
            }
        }
    }
}
