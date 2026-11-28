package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.minecraftforge.eventbus.api.Event;

/**
 * One of Barnyard Romance's events on Forge's bus (MinecraftForge.EVENT_BUS). It wraps the public API's event, so what a
 * listener changes on apiEvent() is what the mod uses. These fire after every listener on the API's own events.
 * Forge's own rule holds for a listener that throws: the post stops there and the later listeners don't hear it.
 * Barnyard Romance logs it and carries on with what the listeners before it changed or cancelled.
 */
public abstract class BarnyardForgeEvent<E> extends Event {
    private final E apiEvent;

    protected BarnyardForgeEvent(E apiEvent) {
        this.apiEvent = apiEvent;
    }

    public E apiEvent() {
        return apiEvent;
    }
}
