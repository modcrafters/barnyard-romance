package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvent;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.Cancellable;
import net.modcrafters.minecraft.barnyardromance.api.event.Taming;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;

import java.util.function.Function;

// Re-fires every public API event on NeoForge's bus, after the API's own listeners. The mod never listens here.
public final class NeoForgeBridge {
    private NeoForgeBridge() {
    }

    // Called once by Barnyard Romance as it starts.
    public static void register() {
        cancellable(BarnyardEvents.TAMING, TamingEvent::new);
        // A familiarity tame is a tame like any other to NeoForge mods, horses included; a trusting ocelot isn't.
        BarnyardEvents.TAMING.registerLate(taming -> {
            if (!taming.isCancelled() && taming.kind() == Taming.Kind.TAME && EventHooks.onAnimalTame(taming.animal(), taming.player())) {
                taming.cancel();
            }
        });
        cancellable(BarnyardEvents.MILKING, MilkingEvent::new);
        cancellable(BarnyardEvents.DEATH_DROP, DeathDropEvent::new);
        cancellable(BarnyardEvents.SHEAR_YIELD, ShearYieldEvent::new);
        cancellable(BarnyardEvents.FAMILIARITY_CHANGE, FamiliarityChangeEvent::new);
        announce(BarnyardEvents.BEFRIENDED, BefriendedEvent::new);
        announce(BarnyardEvents.BORN, BornEvent::new);
        announce(BarnyardEvents.HATCHED, HatchedEvent::new);
    }

    private static <E extends Cancellable> void cancellable(BarnyardEvent<E> event, Function<E, CancellableBarnyardNeoForgeEvent<E>> wrap) {
        event.registerLate(fired -> {
            if (NeoForge.EVENT_BUS.post(wrap.apply(fired)).isCanceled()) {
                fired.cancel();
            }
        });
    }

    private static <E> void announce(BarnyardEvent<E> event, Function<E, BarnyardNeoForgeEvent<E>> wrap) {
        event.registerLate(fired -> NeoForge.EVENT_BUS.post(wrap.apply(fired)));
    }
}
