package net.modcrafters.minecraft.barnyardromance.core.event;

import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.event.Befriended;
import net.modcrafters.minecraft.barnyardromance.api.event.Born;
import net.modcrafters.minecraft.barnyardromance.api.event.Hatched;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;

import java.util.List;

// The internal events other mods also hear, as the public API's after-the-fact events. They go out once the mod's
// own listeners have run, so the herd book and advancements see a birth before any other mod does.
final class PublicEvents {
    private PublicEvents() {
    }

    static void forward(AnimalEvent event) {
        switch (event) {
            case AnimalEvent.Born born when Tracked.traits(born.mother()).isPresent() ->
                    BarnyardEvents.BORN.post(new Born(born.mother(), List.copyOf(born.young()), born.breeder()));
            case AnimalEvent.Hatched hatched when Tracked.traits(hatched.chick()).isPresent() -> {
                Lineage parents = hatched.parents();
                BarnyardEvents.HATCHED.post(new Hatched(hatched.chick(), parents == null ? null : parents.mother(), parents == null ? null : parents.father()));
            }
            case AnimalEvent.Befriended befriended when Tracked.traits(befriended.animal()).isPresent() ->
                    BarnyardEvents.BEFRIENDED.post(new Befriended(befriended.animal(), befriended.player()));
            default -> {
            }
        }
    }
}
