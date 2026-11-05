package net.modcrafters.minecraft.barnyardromance.compat;

import java.util.Set;

// Naturalist's creatures are matched by id, so this does nothing without the mod. Only its livestock follows the
// barnyard's rules; the rest, and any it adds later, stay Naturalist's unless a config entry switches them on.
public final class NaturalistCompat {
    public static final Set<String> LIVESTOCK = Set.of(
            "naturalist:deer",
            "naturalist:boar",
            "naturalist:duck",
            "naturalist:turkey",
            "naturalist:ostrich"
    );
    private static final String NAMESPACE = "naturalist:";

    private NaturalistCompat() {
    }

    public static boolean ignoredByDefault(String speciesId) {
        return speciesId.startsWith(NAMESPACE) && !LIVESTOCK.contains(speciesId);
    }
}
