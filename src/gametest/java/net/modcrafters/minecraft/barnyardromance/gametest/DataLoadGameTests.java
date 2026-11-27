package net.modcrafters.minecraft.barnyardromance.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;
import net.modcrafters.minecraft.barnyardromance.advancement.SpeciesTracked;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.ConfigEnabled;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// The mod's data pack loads on every version and loader. Folder names and JSON shapes changed between versions, and
// a file the game can't read is only logged, so these check what actually loaded.
public final class DataLoadGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("mod_data_loads", 20, DataLoadGameTests::modDataLoads),
            new BreedingGameTests.Case("vanilla_foods_in_c_tags", 20, DataLoadGameTests::vanillaFoodsInCTags)
    );

    // The keys each loader reads a file's load conditions from: NeoForge's, Fabric's and Forge 1.20.1's.
    private static final List<String> CONDITION_KEYS = List.of("neoforge:conditions", "fabric:load_conditions", "conditions");

    // The kinds of file checked, each under its folder names: 1.21 dropped the plural, and 1.20.1 still reads it.
    private enum Kind {
        RECIPE("recipe", "recipes"),
        ADVANCEMENT("advancement", "advancements"),
        LOOT_TABLE("loot_table", "loot_tables");

        private final List<String> folders;

        Kind(String... folders) {
            this.folders = List.of(folders);
        }
    }

    private DataLoadGameTests() {
    }

    // Every recipe, advancement and loot table file the mod ships loaded. The files are listed from the server's own
    // resources under both folder names, so a file in a folder this version doesn't read counts as not loaded too. A
    // file whose load conditions fail in this run (a species that isn't tracked, a switch that's off) is left out by
    // design, and must not load either.
    public static void modDataLoads(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        List<String> unwanted = new ArrayList<>();
        Map<Kind, Integer> files = new EnumMap<>(Kind.class);
        for (Kind kind : Kind.values()) {
            for (String folder : kind.folders) {
                Map<Identifier, Resource> found = server.getResourceManager().listResources(folder, path -> path.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> file : found.entrySet()) {
                    Identifier path = file.getKey();
                    if (!path.getNamespace().equals(Constants.MOD_ID)) {
                        continue;
                    }
                    files.merge(kind, 1, Integer::sum);
                    String name = path.getPath().substring(folder.length() + 1, path.getPath().length() - ".json".length());
                    Identifier id = Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
                    boolean wanted = conditionsPass(helper, path, file.getValue());
                    boolean loaded = loaded(server, kind, id);
                    String shown = Constants.MOD_ID + ":" + folder + "/" + name;
                    if (wanted && !loaded) {
                        missing.add(shown);
                    } else if (!wanted && loaded) {
                        unwanted.add(shown);
                    }
                }
            }
        }
        for (Kind kind : Kind.values()) {
            helper.assertTrue(files.getOrDefault(kind, 0) > 0, "no " + kind + " files were found, so this proves nothing");
        }
        missing.sort(null);
        unwanted.sort(null);
        helper.assertTrue(missing.isEmpty(), missing.size() + " of the mod's data files didn't load: " + missing);
        helper.assertTrue(unwanted.isEmpty(), unwanted.size() + " data files loaded though their conditions fail: " + unwanted);
        helper.succeed();
    }

    // The c: tags the diets name hold the vanilla foods, whichever loader fills them.
    public static void vanillaFoodsInCTags(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        expect(wrong, Items.APPLE, "foods/fruit");
        expect(wrong, Items.CARROT, "foods/vegetable");
        expect(wrong, Items.BEEF, "foods/raw_meat");
        expect(wrong, Items.EGG, "eggs");
        expect(wrong, Items.MILK_BUCKET, "buckets/milk");
        helper.assertTrue(wrong.isEmpty(), "missing from their c: tags: " + wrong);
        helper.succeed();
    }

    private static void expect(List<String> wrong, Item item, String tag) {
        TagKey<Item> key = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", tag));
        if (!new ItemStack(item).is(key)) {
            wrong.add(item + " in c:" + tag);
        }
    }

    private static boolean loaded(MinecraftServer server, Kind kind, Identifier id) {
        return switch (kind) {
            //? if >=1.21.2 {
            case RECIPE -> server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent();
            //?} else
            //case RECIPE -> server.getRecipeManager().byKey(id).isPresent();
            //? if >=1.20.5 {
            case ADVANCEMENT -> server.getAdvancements().get(id) != null;
            case LOOT_TABLE -> server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id)) != LootTable.EMPTY;
            //?} else {
            /*case ADVANCEMENT -> server.getAdvancements().getAdvancement(id) != null;
            case LOOT_TABLE -> server.getLootData().getLootTable(id) != LootTable.EMPTY;
            *///?}
        };
    }

    // Whether every load condition in the file passes in this run, under whichever loader's key it's written.
    private static boolean conditionsPass(GameTestHelper helper, Identifier path, Resource resource) {
        JsonObject json;
        try (Reader reader = resource.openAsReader()) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            throw GameTestCompat.failure(helper, path + " can't be read: " + exception);
        }
        for (String key : CONDITION_KEYS) {
            JsonArray conditions = json.has(key) && json.get(key).isJsonArray() ? json.getAsJsonArray(key) : new JsonArray();
            for (JsonElement element : conditions) {
                if (!conditionPasses(helper, path, element.getAsJsonObject())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean conditionPasses(GameTestHelper helper, Identifier path, JsonObject condition) {
        String type = condition.has("type") ? condition.get("type").getAsString() : condition.get("condition").getAsString();
        if (type.equals(Constants.MOD_ID + ":" + SpeciesTracked.ID)) {
            List<String> species = new ArrayList<>();
            condition.getAsJsonArray("species").forEach(element -> species.add(element.getAsString()));
            return SpeciesTracked.test(species);
        }
        if (type.equals(Constants.MOD_ID + ":" + ConfigEnabled.ID)) {
            return ConfigEnabled.test(condition.get("key").getAsString());
        }
        throw GameTestCompat.failure(helper, path + " has a load condition this test doesn't know: " + type);
    }
}
