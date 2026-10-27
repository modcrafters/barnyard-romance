package net.modcrafters.minecraft.barnyardromance.food;

import java.util.List;

// Foods a diet eats, and how much of the default effects they give.
public record DietEntry(List<ItemMatch> items, double factor) {
    public DietEntry {
        items = List.copyOf(items);
    }
}
