package net.modcrafters.minecraft.barnyardromance.food;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

// The first match wins: refused, a food file, vanilla food, the diet, or not food at all.
public final class FoodRules {
    private FoodRules() {
    }

    public static Feed resolve(String speciesId, String dietId, Diet diet, List<FoodFile> files, ItemFacts item) {
        boolean vanilla = item.vanillaFood();
        // Vanilla food is never refused, so vanilla breeding can't break.
        if (!vanilla && diet != null && item.matchesAny(diet.refuses())) {
            return Feed.REFUSED;
        }
        for (FoodFile file : files) {
            if (file.appliesTo(speciesId, dietId) && item.matchesAny(file.items())) {
                return new Feed.Eaten(file.effects(), 1.0, file.superfood());
            }
        }
        if (vanilla) {
            return new Feed.Eaten(FoodEffects.NONE, 1.0, false);
        }
        if (diet != null) {
            for (DietEntry entry : diet.eats()) {
                if (item.matchesAny(entry.items())) {
                    return new Feed.Eaten(FoodEffects.NONE, entry.factor(), false);
                }
            }
        }
        return Feed.NOT_FOOD;
    }

    // The config's override when it exists, else the first diet (by id) that lists the species.
    public static Optional<Map.Entry<String, Diet>> dietFor(String speciesId, String override, Map<String, Diet> diets) {
        if (override != null && diets.containsKey(override)) {
            return Optional.of(Map.entry(override, diets.get(override)));
        }
        for (Map.Entry<String, Diet> entry : new TreeMap<>(diets).entrySet()) {
            if (entry.getValue().species().contains(speciesId)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }
}
