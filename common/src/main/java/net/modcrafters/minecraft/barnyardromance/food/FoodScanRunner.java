package net.modcrafters.minecraft.barnyardromance.food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

// Runs FoodScan over the server's recipes and writes the result as a datapack for a pack maker to review.
public final class FoodScanRunner {
    public static final String PACK = "barnyard_romance_scan";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final TagKey<Item> FOODS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "foods"));

    private FoodScanRunner() {
    }

    public record ScanSummary(int recipes, int items, int tags, Map<String, Integer> skippedByType, Path folder) {
    }

    public static Path defaultFolder(MinecraftServer server) {
        return server.getServerDirectory().resolve("barnyard_romance").resolve("scan");
    }

    // Every tag a loaded diet refuses, with its members today.
    public static Map<String, Set<String>> liveGroups() {
        Map<String, Set<String>> groups = new TreeMap<>();
        for (Diet diet : FoodData.diets().values()) {
            for (ItemMatch refused : diet.refuses()) {
                if (refused.tag()) {
                    groups.computeIfAbsent(refused.id(), FoodScanRunner::members);
                }
            }
        }
        return groups;
    }

    public static ScanSummary run(MinecraftServer server, Map<String, Set<String>> groups, Path folder) throws IOException {
        Map<String, Integer> skipped = new TreeMap<>();
        List<FoodScan.ScanRecipe> recipes = new ArrayList<>();
        ContextMap context = SlotDisplayContext.fromLevel(server.overworld());
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();
            String type = String.valueOf(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
            String result = result(recipe, context);
            if (recipe.isSpecial() || recipe.placementInfo().isImpossibleToPlace() || result == null) {
                skipped.merge(type, 1, Integer::sum);
                continue;
            }
            List<Set<String>> ingredients = new ArrayList<>();
            for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
                Set<String> items = new HashSet<>();
                ingredient.items().forEach(item -> items.add(item.getRegisteredName()));
                ingredients.add(items);
            }
            recipes.add(new FoodScan.ScanRecipe(holder.id().identifier().toString(), type, result, ingredients));
        }
        FoodScan.ScanResult result = FoodScan.run(recipes, groups, FoodScanRunner::isFood);
        int items = write(result, skipped, folder);
        int tags = (int) result.added().values().stream().filter(added -> !added.isEmpty()).count();
        return new ScanSummary(recipes.size(), items, tags, skipped, folder);
    }

    private static String result(Recipe<?> recipe, ContextMap context) {
        for (RecipeDisplay display : recipe.display()) {
            ItemStack stack = display.result().resolveForFirstStack(context);
            if (!stack.isEmpty()) {
                return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            }
        }
        return null;
    }

    // Food is anything with a food component, anything tagged c:foods, or anything a diet eats.
    private static boolean isFood(String itemId) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)));
        if (stack.has(DataComponents.FOOD) || stack.is(FOODS)) {
            return true;
        }
        for (Diet diet : FoodData.diets().values()) {
            for (DietEntry entry : diet.eats()) {
                for (ItemMatch match : entry.items()) {
                    if (MinecraftItemFacts.matches(stack, match)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Set<String> members(String tagId) {
        Set<String> items = new LinkedHashSet<>();
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, Identifier.parse(tagId)))) {
            items.add(item.getRegisteredName());
        }
        return items;
    }

    private static int write(FoodScan.ScanResult result, Map<String, Integer> skipped, Path folder) throws IOException {
        Path pack = folder.resolve(PACK);
        Files.createDirectories(pack);
        PackFormat format = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA);
        JsonArray version = new JsonArray();
        version.add(format.major());
        version.add(format.minor());
        JsonObject meta = new JsonObject();
        meta.addProperty("description", "Barnyard Romance food tags found by /barnyard foods scan");
        meta.add("min_format", version);
        meta.add("max_format", version);
        JsonObject mcmeta = new JsonObject();
        mcmeta.add("pack", meta);
        Files.writeString(pack.resolve("pack.mcmeta"), GSON.toJson(mcmeta), StandardCharsets.UTF_8);

        JsonObject report = new JsonObject();
        int items = 0;
        for (Map.Entry<String, SortedSet<String>> group : result.added().entrySet()) {
            if (group.getValue().isEmpty()) {
                continue;
            }
            Identifier tag = Identifier.parse(group.getKey());
            JsonArray values = new JsonArray();
            JsonObject because = new JsonObject();
            for (String item : group.getValue()) {
                values.add(item);
                because.addProperty(item, result.because().get(item));
            }
            JsonObject tagFile = new JsonObject();
            tagFile.addProperty("replace", false);
            tagFile.add("values", values);
            Path file = pack.resolve("data").resolve(tag.getNamespace()).resolve("tags").resolve("item").resolve(tag.getPath() + ".json");
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(tagFile), StandardCharsets.UTF_8);
            report.add("#" + tag, because);
            items += group.getValue().size();
        }
        JsonObject unread = new JsonObject();
        skipped.forEach(unread::addProperty);
        report.add("skippedRecipeTypes", unread);
        Files.writeString(folder.resolve("report.json"), GSON.toJson(report), StandardCharsets.UTF_8);
        return items;
    }
}
