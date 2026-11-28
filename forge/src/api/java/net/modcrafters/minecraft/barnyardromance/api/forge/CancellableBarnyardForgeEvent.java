package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.Cancellable;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * A cancellable event: cancelling it cancels the API event too. One a listener on the API's own events already
 * cancelled arrives cancelled; Forge hands it only to listeners that receive cancelled events.
 */
@Cancelable
public abstract class CancellableBarnyardForgeEvent<E extends Cancellable> extends BarnyardForgeEvent<E> {
    protected CancellableBarnyardForgeEvent(E apiEvent) {
        super(apiEvent);
        if (apiEvent.isCancelled()) {
            setCanceled(true);
        }
    }
}
