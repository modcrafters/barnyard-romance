package net.modcrafters.minecraft.barnyardromance.core.registry;

import java.util.List;
import java.util.Set;

// The names of the tab entries BarnyardTab.contents shows: stacks a feature registers (FeatureRegistrar.tabEntry)
// because they need more than an item id. Like AnimalTicks' hook names, they're checked both ways at start-up, so a
// misspelt or forgotten entry stops the game there instead of when someone opens the creative tab.
public final class TabEntries {
    private static final List<String> NAMES = List.of("herd_sticks");

    private TabEntries() {
    }

    public static List<String> names() {
        return NAMES;
    }

    // Throws unless BarnyardTab shows an entry of this name, so a registered entry can't sit unshown.
    public static void checkNamed(String name) {
        if (!NAMES.contains(name)) {
            throw new IllegalStateException("Tab entry " + name + " has no place in BarnyardTab");
        }
    }

    // Throws if BarnyardTab shows an entry nobody registered.
    public static void checkRegistered(Set<String> registered) {
        for (String name : NAMES) {
            if (!registered.contains(name)) {
                throw new IllegalStateException("Nobody registers tab entry " + name);
            }
        }
    }
}
