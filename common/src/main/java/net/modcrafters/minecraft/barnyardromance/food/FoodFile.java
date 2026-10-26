package net.modcrafters.minecraft.barnyardromance.food;

import java.util.List;

// One food file: which animals it is for (by species or diet), which items, and what they do.
public record FoodFile(List<String> species, List<String> diets, List<ItemMatch> items, boolean superfood, FoodEffects effects) {
    public FoodFile {
        species = List.copyOf(species);
        diets = List.copyOf(diets);
        items = List.copyOf(items);
    }

    public boolean appliesTo(String speciesId, String dietId) {
        return species.contains(speciesId) || dietId != null && diets.contains(dietId);
    }
}
