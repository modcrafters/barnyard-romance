package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.neoforged.bus.api.Event;

/**
 * One of Barnyard Romance's events on NeoForge's bus (NeoForge.EVENT_BUS). It wraps the public API's event, so what a
 * listener changes on apiEvent() is what the mod uses. These fire after every listener on the API's own events.
 */
public abstract class BarnyardNeoForgeEvent<E> extends Event {
    private final E apiEvent;

    protected BarnyardNeoForgeEvent(E apiEvent) {
        this.apiEvent = apiEvent;
    }

    public E apiEvent() {
        return apiEvent;
    }
}
