package net.modcrafters.minecraft.barnyardromance.food;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

// Carries group tags (like contains_meat) through the recipe graph: if sugar is anywhere in a food's recipe tree, it is sweet.
public final class FoodScan {
    private static final String GROUP_PREFIX = "barnyard_romance:contains_";

    private FoodScan() {
    }

    // One recipe: each ingredient is the set of items it accepts.
    public record ScanRecipe(String id, String type, String result, List<Set<String>> ingredients) {
    }

    // The food items each group gained, and the recipe that first put each item in a group.
    public record ScanResult(Map<String, SortedSet<String>> added, Map<String, String> because) {
    }

    // The groups to scan: every tag a diet refuses, plus our own contains_* tags a diet eats (like contains_fish).
    public static SortedSet<String> groupTags(Collection<Diet> diets) {
        SortedSet<String> groups = new TreeSet<>();
        for (Diet diet : diets) {
            for (ItemMatch refused : diet.refuses()) {
                if (refused.tag()) {
                    groups.add(refused.id());
                }
            }
            for (DietEntry entry : diet.eats()) {
                for (ItemMatch eaten : entry.items()) {
                    if (eaten.tag() && eaten.id().startsWith(GROUP_PREFIX)) {
                        groups.add(eaten.id());
                    }
                }
            }
        }
        return groups;
    }

    // An ingredient carries a group only when every item it accepts is in it; any one recipe is enough.
    public static ScanResult run(List<ScanRecipe> recipes, Map<String, Set<String>> groups, Predicate<String> food) {
        Map<String, Set<String>> members = new HashMap<>();
        groups.forEach((group, items) -> members.put(group, new HashSet<>(items)));
        Map<String, String> because = new LinkedHashMap<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ScanRecipe recipe : recipes) {
                for (Set<String> group : members.values()) {
                    if (!group.contains(recipe.result()) && carries(recipe, group)) {
                        group.add(recipe.result());
                        because.putIfAbsent(recipe.result(), recipe.id());
                        changed = true;
                    }
                }
            }
        }
        Map<String, SortedSet<String>> added = new TreeMap<>();
        Map<String, String> reasons = new TreeMap<>();
        members.forEach((group, items) -> {
            SortedSet<String> found = new TreeSet<>();
            for (String item : items) {
                if (!groups.get(group).contains(item) && food.test(item)) {
                    found.add(item);
                    reasons.put(item, because.get(item));
                }
            }
            added.put(group, found);
        });
        return new ScanResult(added, reasons);
    }

    private static boolean carries(ScanRecipe recipe, Set<String> group) {
        for (Set<String> ingredient : recipe.ingredients()) {
            if (!ingredient.isEmpty() && group.containsAll(ingredient)) {
                return true;
            }
        }
        return false;
    }
}
