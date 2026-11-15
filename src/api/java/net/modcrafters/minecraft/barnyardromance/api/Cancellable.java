package net.modcrafters.minecraft.barnyardromance.api;

/**
 * An event a listener can stop. What a cancel means is on each event.
 */
public interface Cancellable {
    void cancel();

    boolean isCancelled();
}
