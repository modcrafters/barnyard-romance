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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Barnyard Romance's events as Fabric API events. Each callback gets the public API's own event: change or cancel it
 * there. They fire after every listener on the API's own events, in the order callbacks registered. A callback that
 * throws is logged and the rest still run, as on the API's own events.
 */
public final class BarnyardFabricEvents {
    private static final Logger LOG = LoggerFactory.getLogger("barnyard_romance");

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
            callbacks -> event -> each("Taming", callbacks, callback -> callback.onTaming(event)));
    public static final Event<MilkingCallback> MILKING = EventFactory.createArrayBacked(MilkingCallback.class,
            callbacks -> event -> each("Milking", callbacks, callback -> callback.onMilking(event)));
    public static final Event<DeathDropCallback> DEATH_DROP = EventFactory.createArrayBacked(DeathDropCallback.class,
            callbacks -> event -> each("DeathDrop", callbacks, callback -> callback.onDeathDrop(event)));
    public static final Event<ShearYieldCallback> SHEAR_YIELD = EventFactory.createArrayBacked(ShearYieldCallback.class,
            callbacks -> event -> each("ShearYield", callbacks, callback -> callback.onShearYield(event)));
    public static final Event<FamiliarityChangeCallback> FAMILIARITY_CHANGE = EventFactory.createArrayBacked(FamiliarityChangeCallback.class,
            callbacks -> event -> each("FamiliarityChange", callbacks, callback -> callback.onFamiliarityChange(event)));
    public static final Event<BefriendedCallback> BEFRIENDED = EventFactory.createArrayBacked(BefriendedCallback.class,
            callbacks -> event -> each("Befriended", callbacks, callback -> callback.onBefriended(event)));
    public static final Event<BornCallback> BORN = EventFactory.createArrayBacked(BornCallback.class,
            callbacks -> event -> each("Born", callbacks, callback -> callback.onBorn(event)));
    public static final Event<HatchedCallback> HATCHED = EventFactory.createArrayBacked(HatchedCallback.class,
            callbacks -> event -> each("Hatched", callbacks, callback -> callback.onHatched(event)));

    private BarnyardFabricEvents() {
    }

    // As on the API's own events: a callback that throws is logged, and the ones after it still run.
    private static <C> void each(String name, C[] callbacks, Consumer<C> call) {
        for (C callback : callbacks) {
            try {
                call.accept(callback);
            } catch (Exception | LinkageError exception) {
                LOG.warn("A {} callback failed", name, exception);
            }
        }
    }
}
