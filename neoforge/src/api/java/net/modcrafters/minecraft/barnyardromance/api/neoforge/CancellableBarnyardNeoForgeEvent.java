package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.Cancellable;
import net.neoforged.bus.api.ICancellableEvent;

// A cancellable event: cancelling it cancels the API event too. One a listener on the API's own events already
// cancelled arrives cancelled; NeoForge hands it only to listeners that receive cancelled events.
public abstract class CancellableBarnyardNeoForgeEvent<E extends Cancellable> extends BarnyardNeoForgeEvent<E> implements ICancellableEvent {
    protected CancellableBarnyardNeoForgeEvent(E apiEvent) {
        super(apiEvent);
        if (apiEvent.isCancelled()) {
            setCanceled(true);
        }
    }
}
