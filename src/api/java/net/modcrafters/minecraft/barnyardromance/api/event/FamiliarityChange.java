package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.api.Cancellable;

/**
 * An animal's familiarity is about to change from before to after (both 0 to 1). A listener may set another after;
 * Barnyard Romance clamps it to 0 and the animal's cap (wild-caught animals have one), and keeps before for NaN.
 * Cancelled, familiarity stays at before.
 */
public final class FamiliarityChange implements Cancellable {
    /**
     * CARE: feeding, a feeding block or care raised it. NEGLECT: the daily settle let it fall. COST: taking a product
     * (milk, wool) cost some. OTHER: anything else, such as a command.
     */
    public enum Reason {
        CARE,
        NEGLECT,
        COST,
        OTHER
    }

    private final Animal animal;
    private final double before;
    private final Reason reason;
    private double after;
    private boolean cancelled;

    public FamiliarityChange(Animal animal, double before, double after, Reason reason) {
        this.animal = animal;
        this.before = before;
        this.after = after;
        this.reason = reason;
    }

    public Animal animal() {
        return animal;
    }

    public double before() {
        return before;
    }

    public double after() {
        return after;
    }

    public void setAfter(double after) {
        this.after = after;
    }

    public Reason reason() {
        return reason;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
