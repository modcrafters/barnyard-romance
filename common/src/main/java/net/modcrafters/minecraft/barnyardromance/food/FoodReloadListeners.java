package net.modcrafters.minecraft.barnyardromance.food;

import com.google.gson.JsonElement;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

// Loads data/<ns>/barnyard_romance/diets and .../foods. A file that fails to parse is logged and skipped.
public final class FoodReloadListeners {
    public static final Identifier DIETS_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "diets");
    public static final Identifier FOODS_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "foods");

    private FoodReloadListeners() {
    }

    public static SimpleJsonResourceReloadListener<JsonElement> diets() {
        return new Listener("barnyard_romance/diets", files -> {
            Map<String, Diet> diets = new LinkedHashMap<>();
            parse(files, "diet", FoodJson::dietFrom).forEach((id, diet) -> diets.put(id.toString(), diet));
            FoodData.replaceDiets(diets);
            Constants.LOG.info("Loaded {} barnyard diets", diets.size());
        });
    }

    public static SimpleJsonResourceReloadListener<JsonElement> foods() {
        return new Listener("barnyard_romance/foods", files -> {
            List<FoodFile> foods = new ArrayList<>(parse(files, "food file", FoodJson::foodFileFrom).values());
            FoodData.replaceFoods(foods);
            Constants.LOG.info("Loaded {} barnyard food files", foods.size());
        });
    }

    // Sorted by id, so the first match is the same on every server.
    private static <T> Map<Identifier, T> parse(Map<Identifier, JsonElement> files, String what, Function<JsonElement, T> parser) {
        Map<Identifier, T> parsed = new TreeMap<>();
        files.forEach((id, json) -> {
            try {
                parsed.put(id, parser.apply(json));
            } catch (RuntimeException exception) {
                Constants.LOG.error("Skipped barnyard {} {}: {}", what, id, exception.getMessage());
            }
        });
        return parsed;
    }

    private static final class Listener extends SimpleJsonResourceReloadListener<JsonElement> {
        private final Consumer<Map<Identifier, JsonElement>> apply;

        Listener(String directory, Consumer<Map<Identifier, JsonElement>> apply) {
            super(ExtraCodecs.JSON, FileToIdConverter.json(directory));
            this.apply = apply;
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            apply.accept(files);
        }
    }
}
