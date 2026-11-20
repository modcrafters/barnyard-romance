package net.modcrafters.minecraft.barnyardromance.naturalist;

import java.util.Set;

// Naturalist's creatures are matched by id, so this does nothing without the mod. Only its livestock is tracked by
// default; the rest, and any it adds later, stay Naturalist's unless a config entry or the included tag switches them on.
public final class NaturalistCompat {
    public static final Set<String> LIVESTOCK = Set.of(
            "naturalist:boar",
            "naturalist:duck",
            "naturalist:turkey",
            "naturalist:ostrich"
    );

    private NaturalistCompat() {
    }
}
