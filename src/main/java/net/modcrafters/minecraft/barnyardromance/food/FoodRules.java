package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodEffects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
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

    // What a species eats, best first, as ids: "#c:crops" for a tag, "minecraft:wheat" for an item. Its food files'
    // everyday foods come first, then the vanilla foods (by item id) that nothing else names, then its diet by how much
    // good each does. Superfoods are treats, not meals, so they're left out.
    public static List<String> menu(String speciesId, String dietId, Diet diet, List<FoodFile> files, Map<String, ItemFacts> vanillaFoods) {
        List<FoodFile> own = files.stream().filter(file -> file.appliesTo(speciesId, dietId)).toList();
        List<DietEntry> eats = diet == null ? List.of() : diet.eats().stream()
                .sorted(Comparator.comparingDouble(DietEntry::factor).reversed())
                .toList();
        LinkedHashSet<String> menu = new LinkedHashSet<>();
        own.stream().filter(file -> !file.superfood()).forEach(file -> file.items().forEach(item -> menu.add(item.toString())));
        vanillaFoods.forEach((id, item) -> {
            boolean named = own.stream().anyMatch(file -> item.matchesAny(file.items()))
                    || eats.stream().anyMatch(entry -> item.matchesAny(entry.items()));
            if (!named) {
                menu.add(id);
            }
        });
        eats.forEach(entry -> entry.items().forEach(item -> menu.add(item.toString())));
        return new ArrayList<>(menu);
    }

    // The config's override when it exists, else the first diet (by id) that lists the species.
    public static Optional<Map.Entry<String, Diet>> dietFor(String speciesId, String override, Map<String, Diet> diets) {
        if (override != null && diets.containsKey(override)) {
            return Optional.of(Map.entry(override, diets.get(override)));
        }
        // Asked whenever an animal looks at what a player holds, so a map already sorted by id isn't copied again.
        Map<String, Diet> byId = diets instanceof SortedMap<String, Diet> sorted && sorted.comparator() == null ? diets : new TreeMap<>(diets);
        for (Map.Entry<String, Diet> entry : byId.entrySet()) {
            if (entry.getValue().species().contains(speciesId)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }
}
