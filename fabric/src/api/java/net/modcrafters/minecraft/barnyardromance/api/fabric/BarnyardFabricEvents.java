package net.modcrafters.minecraft.barnyardromance.api.fabric;

import net.modcrafters.minecraft.barnyardromance.api.event.Befriended;
import net.modcrafters.minecraft.barnyardromance.api.event.Born;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;
import net.modcrafters.minecraft.barnyardromance.api.event.Hatched;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import net.modcrafters.minecraft.barnyardromance.api.event.Taming;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Barnyard Romance's events as Fabric API events. Each callback gets the public API's own event: change or cancel it
 * there. They fire after every listener on the API's own events, in the order callbacks registered.
 */
public final class BarnyardFabricEvents {
    /** Receives a {@link Taming}: change or cancel it there. */
    @FunctionalInterface
    public interface TamingCallback {
        void onTaming(Taming event);
    }

    /** Receives a {@link Milking}: change or cancel it there. */
    @FunctionalInterface
    public interface MilkingCallback {
        void onMilking(Milking event);
    }

    /** Receives a {@link DeathDrop}: change or cancel it there. */
    @FunctionalInterface
    public interface DeathDropCallback {
        void onDeathDrop(DeathDrop event);
    }

    /** Receives a {@link ShearYield}: change or cancel it there. */
    @FunctionalInterface
    public interface ShearYieldCallback {
        void onShearYield(ShearYield event);
    }

    /** Receives a {@link FamiliarityChange}: change or cancel it there. */
    @FunctionalInterface
    public interface FamiliarityChangeCallback {
        void onFamiliarityChange(FamiliarityChange event);
    }

    /** Receives a {@link Befriended}: change or cancel it there. */
    @FunctionalInterface
    public interface BefriendedCallback {
        void onBefriended(Befriended event);
    }

    /** Receives a {@link Born}: change or cancel it there. */
    @FunctionalInterface
    public interface BornCallback {
        void onBorn(Born event);
    }

    /** Receives a {@link Hatched}: change or cancel it there. */
    @FunctionalInterface
    public interface HatchedCallback {
        void onHatched(Hatched event);
    }

    public static final Event<TamingCallback> TAMING = EventFactory.createArrayBacked(TamingCallback.class,
            callbacks -> event -> {
                for (TamingCallback callback : callbacks) {
                    callback.onTaming(event);
                }
            });
    public static final Event<MilkingCallback> MILKING = EventFactory.createArrayBacked(MilkingCallback.class,
            callbacks -> event -> {
                for (MilkingCallback callback : callbacks) {
                    callback.onMilking(event);
                }
            });
    public static final Event<DeathDropCallback> DEATH_DROP = EventFactory.createArrayBacked(DeathDropCallback.class,
            callbacks -> event -> {
                for (DeathDropCallback callback : callbacks) {
                    callback.onDeathDrop(event);
                }
            });
    public static final Event<ShearYieldCallback> SHEAR_YIELD = EventFactory.createArrayBacked(ShearYieldCallback.class,
            callbacks -> event -> {
                for (ShearYieldCallback callback : callbacks) {
                    callback.onShearYield(event);
                }
            });
    public static final Event<FamiliarityChangeCallback> FAMILIARITY_CHANGE = EventFactory.createArrayBacked(FamiliarityChangeCallback.class,
            callbacks -> event -> {
                for (FamiliarityChangeCallback callback : callbacks) {
                    callback.onFamiliarityChange(event);
                }
            });
    public static final Event<BefriendedCallback> BEFRIENDED = EventFactory.createArrayBacked(BefriendedCallback.class,
            callbacks -> event -> {
                for (BefriendedCallback callback : callbacks) {
                    callback.onBefriended(event);
                }
            });
    public static final Event<BornCallback> BORN = EventFactory.createArrayBacked(BornCallback.class,
            callbacks -> event -> {
                for (BornCallback callback : callbacks) {
                    callback.onBorn(event);
                }
            });
    public static final Event<HatchedCallback> HATCHED = EventFactory.createArrayBacked(HatchedCallback.class,
            callbacks -> event -> {
                for (HatchedCallback callback : callbacks) {
                    callback.onHatched(event);
                }
            });

    private BarnyardFabricEvents() {
    }
}
