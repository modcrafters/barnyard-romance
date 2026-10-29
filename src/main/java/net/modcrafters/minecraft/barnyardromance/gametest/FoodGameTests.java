package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.food.FoodScanRunner;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCare;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// In-game checks for diets, food files and superfoods, run by each loader's game test server.
public final class FoodGameTests {
    private static final TagKey<Item> CONTAINS_MEAT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "contains_meat"));
    private static final TagKey<Item> CONTAINS_FISH = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "contains_fish"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("built_in_diets_load", 20, FoodGameTests::builtInDietsLoad),
            //? if <1.21.2
            //new BreedingGameTests.Case("fed_animals_make_their_eating_sound", 20, FoodGameTests::fedAnimalsMakeTheirEatingSound),
            new BreedingGameTests.Case("group_tag_holds_vanilla_meat", 20, FoodGameTests::groupTagHoldsVanillaMeat),
            new BreedingGameTests.Case("grazers_refuse_meat", 20, FoodGameTests::grazersRefuseMeat),
            new BreedingGameTests.Case("diet_food_feeds", 20, FoodGameTests::dietFoodFeeds),
            new BreedingGameTests.Case("carnivores_get_little_from_salad", 20, FoodGameTests::carnivoresGetLittleFromSalad),
            new BreedingGameTests.Case("hurt_animals_heal_from_food_any_time", 20, FoodGameTests::hurtAnimalsHealFromFoodAnyTime),
            new BreedingGameTests.Case("horses_heal_like_vanilla", 20, FoodGameTests::horsesHealLikeVanilla),
            new BreedingGameTests.Case("old_pregnancy_loads_without_care", 20, FoodGameTests::oldPregnancyLoadsWithoutCare),
            new BreedingGameTests.Case("pregnancy_care_saves_and_syncs", 20, FoodGameTests::pregnancyCareSavesAndSyncs),
            new BreedingGameTests.Case("breeders_mash_speeds_a_pregnancy", 20, FoodGameTests::breedersMashSpeedsAPregnancy),
            new BreedingGameTests.Case("golden_bran_feeds_the_litter", 20, FoodGameTests::goldenBranFeedsTheLitter),
            new BreedingGameTests.Case("superfoods_need_a_pregnant_mother", 20, FoodGameTests::superfoodsNeedAPregnantMother),
            new BreedingGameTests.Case("superfood_recipes_load", 20, FoodGameTests::superfoodRecipesLoad),
            new BreedingGameTests.Case("dairy_mash_starts_milk", 20, FoodGameTests::dairyMashStartsMilk),
            new BreedingGameTests.Case("dairy_mash_is_refused_by_a_milking_cow", 20, FoodGameTests::dairyMashIsRefusedByAMilkingCow),
            new BreedingGameTests.Case("dairy_mash_is_refused_by_a_bull", 20, FoodGameTests::dairyMashIsRefusedByABull),
            new BreedingGameTests.Case("dairy_mash_needs_familiarity", 20, FoodGameTests::dairyMashNeedsFamiliarity),
            new BreedingGameTests.Case("food_scan_reads_real_recipes", 100, FoodGameTests::foodScanReadsRealRecipes),
            new BreedingGameTests.Case("our_creative_tab_holds_every_item", 20, FoodGameTests::ourCreativeTabHoldsEveryItem)
    );

    private FoodGameTests() {
    }

    public static void builtInDietsLoad(GameTestHelper helper) {
        String cow = FoodData.dietFor("minecraft:cow").map(Map.Entry::getKey).orElse("none");
        String wolf = FoodData.dietFor("minecraft:wolf").map(Map.Entry::getKey).orElse("none");
        String cat = FoodData.dietFor("minecraft:cat").map(Map.Entry::getKey).orElse("none");
        helper.assertTrue(cow.equals("barnyard_romance:grazer"), "a cow eats as " + cow);
        helper.assertTrue(wolf.equals("barnyard_romance:carnivore"), "a wolf eats as " + wolf);
        helper.assertTrue(cat.equals("barnyard_romance:feline"), "a cat eats as " + cat);
        helper.succeed();
    }

    public static void groupTagHoldsVanillaMeat(GameTestHelper helper) {
        for (Item item : List.of(Items.RABBIT_STEW, Items.BEEF, Items.COD)) {
            helper.assertTrue(new ItemStack(item).is(CONTAINS_MEAT), item + " is not in contains_meat");
        }
        helper.assertTrue(!new ItemStack(Items.WHEAT).is(CONTAINS_MEAT), "wheat is in contains_meat");
        helper.assertTrue(new ItemStack(Items.COD).is(CONTAINS_FISH), "cod is not in contains_fish");
        helper.assertTrue(!new ItemStack(Items.BEEF).is(CONTAINS_FISH), "beef is in contains_fish");
        helper.succeed();
    }

    public static void grazersRefuseMeat(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        AnimalTraits before = BreedingGameTests.traits(helper, cow);
        Player player = holding(helper, Items.BEEF);

        InteractionResult result = FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(result == InteractionResult.CONSUME, "offering beef gave " + result);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the cow ate the beef");
        helper.assertTrue(BreedingGameTests.traits(helper, cow).equals(before), "refused beef changed the cow");
        helper.succeed();
    }

    // A carrot isn't cow food in vanilla, but grazers eat vegetables.
    public static void dietFoodFeeds(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, BreedingGameTests.female(helper, EntityTypes.COW), 0.4);

        FamiliarityActions.interact(cow, holding(helper, Items.CARROT), InteractionHand.MAIN_HAND);

        assertFamiliarity(helper, cow, 0.6);
        helper.succeed();
    }

    public static void carnivoresGetLittleFromSalad(GameTestHelper helper) {
        Animal wolf = withFamiliarity(helper, BreedingGameTests.female(helper, EntityTypes.WOLF), 0.4);

        FamiliarityActions.interact(wolf, holding(helper, Items.CARROT), InteractionHand.MAIN_HAND);

        assertFamiliarity(helper, wolf, 0.45);
        helper.succeed();
    }

    public static void hurtAnimalsHealFromFoodAnyTime(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        long fedAt = helper.getLevel().getOverworldClockTime();
        Services.DATA.set(cow, BreedingGameTests.traits(helper, cow).fed(0.4, fedAt));
        cow.setHealth(cow.getMaxHealth() - 4.0F);
        Player player = holding(helper, Items.CARROT);

        FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        float expected = cow.getMaxHealth() - 2.5F;
        helper.assertTrue(Math.abs(cow.getHealth() - expected) < 1.0e-4F, "the cow has " + cow.getHealth() + " health, not " + expected);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the hurt cow did not eat");
        assertFamiliarity(helper, cow, 0.4);
        helper.assertTrue(BreedingGameTests.traits(helper, cow).lastFedTick() == fedAt, "healing reset the feed clock");
        helper.succeed();
    }

    public static void horsesHealLikeVanilla(GameTestHelper helper) {
        Animal horse = BreedingGameTests.female(helper, EntityTypes.HORSE);
        horse.setHealth(horse.getMaxHealth() - 5.0F);

        FamiliarityActions.interact(horse, holding(helper, Items.WHEAT), InteractionHand.MAIN_HAND);

        float expected = horse.getMaxHealth() - 3.0F;
        helper.assertTrue(Math.abs(horse.getHealth() - expected) < 1.0e-4F, "the horse has " + horse.getHealth() + " health, not " + expected);
        helper.succeed();
    }

    static Player holding(GameTestHelper helper, Item item) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        return player;
    }

    static Animal withFamiliarity(GameTestHelper helper, Animal animal, double familiarity) {
        Services.DATA.set(animal, BreedingGameTests.traits(helper, animal).fed(familiarity, -1L));
        return animal;
    }

    static void assertFamiliarity(GameTestHelper helper, Animal animal, double expected) {
        double familiarity = BreedingGameTests.traits(helper, animal).familiarity();
        helper.assertTrue(Math.abs(familiarity - expected) < 1.0e-6, animal.getType() + " familiarity is " + familiarity + ", not " + expected);
    }

    // A pregnancy saved before Stage 4 has no care and carries on as before.
    public static void oldPregnancyLoadsWithoutCare(GameTestHelper helper) {
        Pregnancy saved = new Pregnancy(10L, 24_000L, "minecraft:cow", 1.0, 1, 0.5);
        CompoundTag tag = (CompoundTag) PregnancyCodecs.CODEC.encodeStart(NbtOps.INSTANCE, saved).getOrThrow();
        tag.remove("care");

        Pregnancy loaded = PregnancyCodecs.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();

        helper.assertTrue(loaded.care().equals(PregnancyCare.NONE), "an old pregnancy loaded with " + loaded.care());
        helper.assertTrue(loaded.conceivedAt() == 10L && loaded.durationTicks() == 24_000L, "an old pregnancy lost its clock");
        helper.succeed();
    }

    public static void pregnancyCareSavesAndSyncs(GameTestHelper helper) {
        PregnancyCare care = PregnancyCare.NONE.boost(100L, 2.0, 24_000L).progress(24_000L, -0.1).improve(0.25);
        Pregnancy pregnancy = new Pregnancy(10L, 24_000L, "minecraft:cow", 1.0, 1, 0.5).withCare(care);

        Pregnancy saved = PregnancyCodecs.CODEC.parse(NbtOps.INSTANCE, PregnancyCodecs.CODEC.encodeStart(NbtOps.INSTANCE, pregnancy).getOrThrow()).getOrThrow();
        ByteBuf buffer = Unpooled.buffer();
        PregnancyCodecs.STREAM_CODEC.encode(buffer, pregnancy);
        Pregnancy synced = PregnancyCodecs.STREAM_CODEC.decode(buffer);

        helper.assertTrue(saved.care().equals(care), "saved care came back as " + saved.care());
        helper.assertTrue(synced.care().equals(care), "synced care came back as " + synced.care());
        helper.succeed();
    }

    public static void breedersMashSpeedsAPregnancy(GameTestHelper helper) {
        Animal cow = pregnantCow(helper, 0.5);
        Player player = holding(helper, BarnyardItems.BREEDERS_MASH);
        player.getMainHandItem().setCount(2);
        long now = helper.getLevel().getGameTime();

        InteractionResult first = FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);
        InteractionResult second = FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        PregnancyCare care = BreedingGameTests.pregnancy(helper, cow).care();
        helper.assertTrue(first == VersionCompat.SUCCESS_SERVER, "the first mash gave " + first);
        helper.assertTrue(care.boosting(now) && care.boostFactor() == 2.0, "the mash did not boost the pregnancy: " + care);
        helper.assertTrue(second == InteractionResult.CONSUME, "the second mash gave " + second);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the second mash was eaten");
        helper.succeed();
    }

    public static void goldenBranFeedsTheLitter(GameTestHelper helper) {
        Animal cow = pregnantCow(helper, 0.5);

        FamiliarityActions.interact(cow, holding(helper, BarnyardItems.GOLDEN_BRAN), InteractionHand.MAIN_HAND);

        double bonus = BreedingGameTests.pregnancy(helper, cow).care().nutritionBonus();
        helper.assertTrue(Math.abs(bonus - 0.25) < 1.0e-9, "golden bran gave a nutrition bonus of " + bonus);
        helper.succeed();
    }

    public static void superfoodsNeedAPregnantMother(GameTestHelper helper) {
        Animal bull = BreedingGameTests.male(helper, EntityTypes.COW);
        Player player = holding(helper, BarnyardItems.BREEDERS_MASH);

        InteractionResult result = FamiliarityActions.interact(bull, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(result == InteractionResult.CONSUME, "mash for a bull gave " + result);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the bull ate the mash");
        helper.succeed();
    }

    // A dry, familiar cow given Dairy Mash has milk for the short window, and it counts as her meal.
    public static void dairyMashStartsMilk(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        Player player = holding(helper, BarnyardItems.DAIRY_MASH);

        FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(player.getMainHandItem().is(Items.BOWL), "feeding the dairy mash left " + player.getMainHandItem() + " in hand, not its bowl");
        AnimalTraits traits = BreedingGameTests.traits(helper, cow);
        long now = helper.getLevel().getGameTime();
        helper.assertTrue(traits.lactation().milkUntil() == now + BarnyardConfig.current().tuning().milkKeepsFlowingTicks(),
                "the dairy mash gave milk until " + traits.lactation().milkUntil());
        helper.assertTrue(traits.hunger().lastAteDay() == GameDay.of(helper.getLevel().getOverworldClockTime()), "the dairy mash didn't count as eating");
        ProductGameTests.assertMilks(helper, cow, Items.MILK_BUCKET);
        helper.succeed();
    }

    public static void dairyMashIsRefusedByAMilkingCow(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Player player = holding(helper, BarnyardItems.DAIRY_MASH);

        FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(player.getMainHandItem().is(BarnyardItems.DAIRY_MASH), "a cow that has milk ate the dairy mash");
        helper.succeed();
    }

    public static void dairyMashIsRefusedByABull(GameTestHelper helper) {
        Animal bull = BreedingGameTests.male(helper, EntityTypes.COW);
        Player player = holding(helper, BarnyardItems.DAIRY_MASH);

        FamiliarityActions.interact(bull, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(player.getMainHandItem().is(BarnyardItems.DAIRY_MASH), "a bull ate the dairy mash");
        helper.assertTrue(!BreedingGameTests.traits(helper, bull).lactation().hasMilk(helper.getLevel().getGameTime(), 72_000L), "a bull has milk");
        helper.succeed();
    }

    public static void dairyMashNeedsFamiliarity(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, BreedingGameTests.female(helper, EntityTypes.COW), 0.1);
        Player player = holding(helper, BarnyardItems.DAIRY_MASH);
        player.setShiftKeyDown(true);

        FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(player.getMainHandItem().is(BarnyardItems.DAIRY_MASH), "an unfamiliar cow ate the dairy mash");
        helper.succeed();
    }

    public static void superfoodRecipesLoad(GameTestHelper helper) {
        for (String name : List.of("breeders_mash", "golden_bran", "dairy_mash")) {
            //? if >=1.21.2 {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
            //?} else
            //Identifier key = Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
            helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(key).isPresent(), "the " + name + " recipe is missing");
        }
        // Crafted stacks carry the tooltip too, because it is the item's default lore.
        for (Item item : List.of(BarnyardItems.BREEDERS_MASH, BarnyardItems.GOLDEN_BRAN, BarnyardItems.DAIRY_MASH)) {
            ItemLore lore = new ItemStack(item).get(DataComponents.LORE);
            helper.assertTrue(lore != null && lore.lines().size() == 2, item + " has no tooltip");
        }
        helper.succeed();
    }

    static Animal pregnantCow(GameTestHelper helper, double motherFamiliarity) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        Services.DATA.setPregnancy(cow, new Pregnancy(helper.getLevel().getGameTime(), 66_000L, "minecraft:cow", 1.0, 1, motherFamiliarity));
        return cow;
    }

    // The shipped meat tag already holds vanilla's only meat dish, so a test-only sugar group checks real recipes.
    public static void foodScanReadsRealRecipes(GameTestHelper helper) {
        try {
            Path folder = Files.createTempDirectory("barnyard_scan");
            FoodScanRunner.ScanSummary summary = FoodScanRunner.run(
                    helper.getLevel().getServer(),
                    Map.of("barnyard_romance:test_sugar", Set.of("minecraft:sugar")),
                    folder
            );
            Path tag = folder.resolve(FoodScanRunner.PACK).resolve("data/barnyard_romance/tags/item/test_sugar.json");
            String written = Files.readString(tag);

            helper.assertTrue(summary.recipes() > 1000, "only " + summary.recipes() + " recipes were read");
            helper.assertTrue(written.contains("minecraft:cake") && written.contains("minecraft:pumpkin_pie"), "the scan missed sweet foods: " + written);
            helper.assertTrue(!written.contains("\"minecraft:sugar\""), "the scan wrote sugar itself: " + written);
            helper.assertTrue(!written.contains("minecraft:fermented_spider_eye"), "the scan wrote a non-food: " + written);
            helper.assertTrue(Files.exists(folder.resolve("report.json")), "no report.json");
            helper.assertTrue(Files.exists(folder.resolve(FoodScanRunner.PACK).resolve("pack.mcmeta")), "no pack.mcmeta");
            try (Stream<Path> files = Files.walk(folder)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            }
        } catch (IOException exception) {
            throw GameTestCompat.failure(helper, "the scan could not write: " + exception.getMessage());
        }
        helper.succeed();
    }

    public static void ourCreativeTabHoldsEveryItem(GameTestHelper helper) {
        //? if >=1.21.2 {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(BarnyardTab.ID);
        //?} else
        //CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(BarnyardTab.ID);
        helper.assertTrue(tab != null, "the Barnyard Romance tab is not registered");
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess()));
        for (Item item : List.of(BarnyardItems.INSPECTOR, BarnyardItems.HERD_STICK, BarnyardItems.BREEDERS_MASH, BarnyardItems.GOLDEN_BRAN)) {
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(item)), item + " is not in the Barnyard Romance tab");
        }
        helper.succeed();
    }

    //? if <1.21.2 {
    /*// 1.21.1 has no shared eating sound, so VersionCompat picks the sound each animal makes when vanilla feeds it.
    public static void fedAnimalsMakeTheirEatingSound(GameTestHelper helper) {
        ItemStack food = new ItemStack(Items.WHEAT);
        Animal cat = BreedingGameTests.female(helper, EntityTypes.CAT);
        Animal fox = BreedingGameTests.male(helper, EntityTypes.FOX);
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        helper.assertTrue(VersionCompat.eatingSound(cat, food) == net.minecraft.sounds.SoundEvents.CAT_EAT, "a cat ate silently");
        helper.assertTrue(VersionCompat.eatingSound(fox, food) == net.minecraft.sounds.SoundEvents.FOX_EAT, "a fox ate silently");
        helper.assertTrue(VersionCompat.eatingSound(cow, food) == null, "a cow made an eating sound vanilla doesn't give it");
        helper.succeed();
    }
    *///?}
}
