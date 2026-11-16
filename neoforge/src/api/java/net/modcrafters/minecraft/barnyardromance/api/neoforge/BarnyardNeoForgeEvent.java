package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.neoforged.bus.api.Event;

/**
 * One of Barnyard Romance's events on NeoForge's bus (NeoForge.EVENT_BUS). It wraps the public API's event, so what a
 * listener changes on apiEvent() is what the mod uses. These fire after every listener on the API's own events.
 * NeoForge's own rule holds for a listener that throws: the post stops there and the later listeners don't hear it.
 * Barnyard Romance logs it and carries on with what the listeners before it changed or cancelled.
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
