package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvent;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.Cancellable;
import net.modcrafters.minecraft.barnyardromance.api.event.Taming;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Function;

/**
 * Re-fires every public API event on Forge's event bus, after the API's own listeners. The mod never listens here.
 */
public final class ForgeBridge {
    private ForgeBridge() {
    }

    /**
     * Called once by Barnyard Romance as it starts.
     */
    public static void register() {
        cancellable(BarnyardEvents.TAMING, TamingEvent::new);
        // A familiarity tame is a tame like any other to Forge mods, horses included; a trusting ocelot isn't.
        BarnyardEvents.TAMING.registerLate(taming -> {
            if (!taming.isCancelled() && taming.kind() == Taming.Kind.TAME && ForgeEventFactory.onAnimalTame(taming.animal(), taming.player())) {
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

    // Forge stops a post at a listener that throws, and the later listeners never hear it; a cancel from before the
    // throw still counts.
    private static <E extends Cancellable> void cancellable(BarnyardEvent<E> event, Function<E, CancellableBarnyardForgeEvent<E>> wrap) {
        event.registerLate(fired -> {
            CancellableBarnyardForgeEvent<E> wrapped = wrap.apply(fired);
            try {
                MinecraftForge.EVENT_BUS.post(wrapped);
            } finally {
                if (wrapped.isCanceled()) {
                    fired.cancel();
                }
            }
        });
    }

    private static <E> void announce(BarnyardEvent<E> event, Function<E, BarnyardForgeEvent<E>> wrap) {
        event.registerLate(fired -> MinecraftForge.EVENT_BUS.post(wrap.apply(fired)));
    }
}
