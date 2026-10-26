package net.modcrafters.minecraft.barnyardromance.food;

import java.util.List;

// A reusable diet: the species it applies to by default, what it eats, and what it refuses.
public record Diet(List<String> species, List<DietEntry> eats, List<ItemMatch> refuses) {
    public Diet {
        species = List.copyOf(species);
        eats = List.copyOf(eats);
        refuses = List.copyOf(refuses);
    }
}
