package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Map;

// In-game checks for diets, food files and superfoods, run by each loader's game test server.
public final class FoodGameTests {
    private static final TagKey<Item> CONTAINS_MEAT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "contains_meat"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("built_in_diets_load", 20, FoodGameTests::builtInDietsLoad),
            new BreedingGameTests.Case("group_tag_holds_vanilla_meat", 20, FoodGameTests::groupTagHoldsVanillaMeat),
            new BreedingGameTests.Case("grazers_refuse_meat", 20, FoodGameTests::grazersRefuseMeat),
            new BreedingGameTests.Case("diet_food_feeds", 20, FoodGameTests::dietFoodFeeds),
            new BreedingGameTests.Case("carnivores_get_little_from_salad", 20, FoodGameTests::carnivoresGetLittleFromSalad),
            new BreedingGameTests.Case("hurt_animals_heal_from_food_any_time", 20, FoodGameTests::hurtAnimalsHealFromFoodAnyTime),
            new BreedingGameTests.Case("horses_heal_like_vanilla", 20, FoodGameTests::horsesHealLikeVanilla)
    );

    private FoodGameTests() {
    }

    public static void builtInDietsLoad(GameTestHelper helper) {
        String cow = FoodData.dietFor("minecraft:cow").map(Map.Entry::getKey).orElse("none");
        String wolf = FoodData.dietFor("minecraft:wolf").map(Map.Entry::getKey).orElse("none");
        helper.assertTrue(cow.equals("barnyard_romance:grazer"), "a cow eats as " + cow);
        helper.assertTrue(wolf.equals("barnyard_romance:carnivore"), "a wolf eats as " + wolf);
        helper.succeed();
    }

    public static void groupTagHoldsVanillaMeat(GameTestHelper helper) {
        for (Item item : List.of(Items.RABBIT_STEW, Items.BEEF, Items.COD)) {
            helper.assertTrue(new ItemStack(item).is(CONTAINS_MEAT), item + " is not in contains_meat");
        }
        helper.assertTrue(!new ItemStack(Items.WHEAT).is(CONTAINS_MEAT), "wheat is in contains_meat");
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
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(cow, BreedingGameTests.traits(helper, cow).fed(0.4, now));
        cow.setHealth(cow.getMaxHealth() - 4.0F);
        Player player = holding(helper, Items.CARROT);

        FamiliarityActions.interact(cow, player, InteractionHand.MAIN_HAND);

        float expected = cow.getMaxHealth() - 2.5F;
        helper.assertTrue(Math.abs(cow.getHealth() - expected) < 1.0e-4F, "the cow has " + cow.getHealth() + " health, not " + expected);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the hurt cow did not eat");
        assertFamiliarity(helper, cow, 0.4);
        helper.assertTrue(BreedingGameTests.traits(helper, cow).lastFedTick() == now, "healing reset the feed clock");
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
}
