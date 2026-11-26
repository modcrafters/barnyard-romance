package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;
import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

// These run with Let's Do Meadow loaded, through the farm-mods runs (gradle/farm-mods-runs.gradle). They find its
// creatures and items by id and reflection, so the mod still doesn't compile against it. In a run without it they pass at
// once and prove nothing, except that a farm-mods run with no jar in its mods folder fails instead of looking green
// (FarmMods.present).
public final class MeadowGameTests {
    private static final String MOD = "meadow";
    private static final String WOODEN_BUCKET = "meadow:wooden_bucket";
    private static final String WOOLY_COW = "meadow:wooly_cow";
    private static final String WATER_BUFFALO = "meadow:water_buffalo";
    private static final String ENVIRONMENTAL = "environmental";
    private static final String YAK = "environmental:yak";

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_bull_refuses_the_wooden_bucket", 20, MeadowGameTests::aBullRefusesTheWoodenBucket),
            new BreedingGameTests.Case("a_dry_cow_refuses_the_wooden_bucket", 20, MeadowGameTests::aDryCowRefusesTheWoodenBucket),
            new BreedingGameTests.Case("a_milked_cow_gives_wooden_milk", 20, MeadowGameTests::aMilkedCowGivesWoodenMilk),
            new BreedingGameTests.Case("a_water_buffalo_gives_buffalo_milk_once_a_day", 20, MeadowGameTests::aWaterBuffaloGivesBuffaloMilkOnceADay),
            new BreedingGameTests.Case("a_warped_wooly_cow_gives_warped_milk", 20, MeadowGameTests::aWarpedWoolyCowGivesWarpedMilk),
            new BreedingGameTests.Case("taking_the_wooden_bucket_off_cows_closes_it", 20, MeadowGameTests::takingTheWoodenBucketOffCowsClosesIt),
            new BreedingGameTests.Case("a_wooly_cow_is_sheared_once_a_day", 20, MeadowGameTests::aWoolyCowIsShearedOnceADay),
            new BreedingGameTests.Case("a_plain_wooly_cow_fills_a_pail_with_cow_milk", 20, MeadowGameTests::aPlainWoolyCowFillsAPailWithCowMilk),
            new BreedingGameTests.Case("a_warped_wooly_cow_gives_a_pail_nothing", 20, MeadowGameTests::aWarpedWoolyCowGivesAPailNothing),
            new BreedingGameTests.Case("a_warped_wooly_cow_swallows_a_pail_silently", 20, MeadowGameTests::aWarpedWoolyCowSwallowsAPailSilently),
            new BreedingGameTests.Case("a_wooly_cow_still_takes_the_wooden_bucket", 20, MeadowGameTests::aWoolyCowStillTakesTheWoodenBucket),
            new BreedingGameTests.Case("an_iron_bucket_still_does_nothing_on_a_wooly_cow", 20, MeadowGameTests::anIronBucketStillDoesNothingOnAWoolyCow),
            new BreedingGameTests.Case("water_buffalo_and_yak_give_cow_milk", 20, MeadowGameTests::waterBuffaloAndYakGiveCowMilk)
    );

    private MeadowGameTests() {
    }

    public static void aBullRefusesTheWoodenBucket(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        for (EntityType<? extends Animal> type : List.of(EntityTypes.COW, type(helper, WATER_BUFFALO), type(helper, WOOLY_COW))) {
            Animal bull = male(helper, type);
            Player player = holding(helper, item(helper, WOODEN_BUCKET));
            GameTestCompat.interact(bull, player);
            helper.assertTrue(player.getMainHandItem().is(item(helper, WOODEN_BUCKET)), "a bull " + type + " gave " + player.getMainHandItem());
        }
        helper.succeed();
    }

    public static void aDryCowRefusesTheWoodenBucket(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        for (EntityType<? extends Animal> type : List.of(EntityTypes.COW, type(helper, WATER_BUFFALO), type(helper, WOOLY_COW))) {
            Animal cow = female(helper, type);
            Player player = holding(helper, item(helper, WOODEN_BUCKET));
            InteractionResult result = GameTestCompat.interact(cow, player);
            helper.assertTrue(player.getMainHandItem().is(item(helper, WOODEN_BUCKET)), "a dry " + type + " gave " + player.getMainHandItem());
            helper.assertTrue(result == InteractionResult.CONSUME, "a dry " + type + " answered " + result);
        }
        helper.succeed();
    }

    public static void aMilkedCowGivesWoodenMilk(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        // A small cow is milked once a day; the second bucket today is refused like any cow's.
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.0);
        ProductGameTests.assertMilks(helper, cow, item(helper, WOODEN_BUCKET), item(helper, "meadow:wooden_milk_bucket"));
        ProductGameTests.assertMilks(helper, cow, item(helper, WOODEN_BUCKET), item(helper, WOODEN_BUCKET));
        helper.succeed();
    }

    public static void aWaterBuffaloGivesBuffaloMilkOnceADay(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal buffalo = ProductGameTests.mother(helper, type(helper, WATER_BUFFALO), 0.0);
        ProductGameTests.assertMilks(helper, buffalo, item(helper, WOODEN_BUCKET), item(helper, "meadow:wooden_buffalo_milk_bucket"));
        ProductGameTests.assertMilks(helper, buffalo, item(helper, WOODEN_BUCKET), item(helper, WOODEN_BUCKET));
        helper.succeed();
    }

    public static void aWarpedWoolyCowGivesWarpedMilk(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal plain = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, plain, "HIGHLAND");
        ProductGameTests.assertMilks(helper, plain, item(helper, WOODEN_BUCKET), item(helper, "meadow:wooden_milk_bucket"));

        Animal warped = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, warped, "WARPED");
        ProductGameTests.assertMilks(helper, warped, item(helper, WOODEN_BUCKET), item(helper, "meadow:wooden_warped_milk_bucket"));
        helper.succeed();
    }

    // A pack's milk list for cows with only the iron bucket: the wooden bucket then does nothing, and Meadow's own
    // milking, which would give it unlimited, never answers.
    public static void takingTheWoodenBucketOffCowsClosesIt(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        UpkeepGameTests.withConfig("{\"species\": {\"minecraft:cow\": {\"milk\": "
                + "[{\"bucket\": \"minecraft:bucket\", \"result\": \"minecraft:milk_bucket\"}]}}}", () -> {
            Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
            Player player = holding(helper, item(helper, WOODEN_BUCKET));
            GameTestCompat.interact(cow, player);
            helper.assertTrue(player.getMainHandItem().is(item(helper, WOODEN_BUCKET)), "the wooden bucket milked a cow it was taken off: " + player.getMainHandItem());
            ProductGameTests.assertMilks(helper, cow, Items.BUCKET, Items.MILK_BUCKET);
        });
        helper.succeed();
    }

    // The wooly cow shears in its own mobInteract, which the once-a-day count still sees: the first shearing counts, and
    // the second the same day is refused with the "enough" message.
    public static void aWoolyCowIsShearedOnceADay(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal cow = female(helper, type(helper, WOOLY_COW));
        Shearable shearable = (Shearable) cow;
        helper.assertTrue(shearable.readyForShearing(), "a wooly cow was not ready for shearing");

        GameTestCompat.useOn(cow, holding(helper, Items.SHEARS));
        helper.assertTrue(!shearable.readyForShearing(), "a familiar wooly cow was not sheared");
        helper.assertTrue(ShearActions.shearedToday(cow), "the shearing was not counted");

        regrow(helper, cow);
        helper.assertTrue(shearable.readyForShearing(), "the wool did not grow back");
        Player player = holding(helper, Items.SHEARS);
        InteractionResult refused = ShearActions.refuses(cow, player, player.getMainHandItem());
        helper.assertTrue(refused == InteractionResult.FAIL, "the second shearing was answered " + refused + ", not refused with ENOUGH");
        GameTestCompat.useOn(cow, player);
        helper.assertTrue(shearable.readyForShearing(), "a wooly cow was sheared twice in one day");
        helper.succeed();
    }

    public static void aPlainWoolyCowFillsAPailWithCowMilk(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal cow = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, cow, "HIGHLAND");
        assertFillsAPail(helper, cow);
        helper.succeed();
    }

    // Warped milk is Meadow's wooden-bucket item with no fluid behind it, so the pail stays empty and she stays unmilked.
    public static void aWarpedWoolyCowGivesAPailNothing(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal cow = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, cow, "WARPED");
        Lactation before = BreedingGameTests.traits(helper, cow).get(LACTATION);
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
        Player player = PailGameTests.holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());

        GameTestCompat.interact(cow, player);

        PailGameTests.assertHolds(helper, player.getMainHandItem(), pail, PailContents.EMPTY);
        helper.assertTrue(BreedingGameTests.traits(helper, cow).get(LACTATION).equals(before), "a warped wooly cow was counted as milked");
        helper.succeed();
    }

    // The pail entry names the pail but gives the warped cow nothing, so the click ends there, silently and costing
    // nothing: consumed, it never reaches Meadow's own interaction or the pail's use.
    public static void aWarpedWoolyCowSwallowsAPailSilently(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal cow = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, cow, "WARPED");
        AnimalTraits before = BreedingGameTests.traits(helper, cow);
        ItemStack pail = PailGameTests.pail(PailItems.IRON_PAIL, MilkFluids.COW_ID, 1000);
        List<Component> messages = new ArrayList<>();
        Player player = PailGameTests.holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, messages);

        InteractionResult result = GameTestCompat.useOn(cow, player);

        helper.assertTrue(result == InteractionResult.CONSUME, "a pail on a warped wooly cow answered " + result);
        PailGameTests.assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.COW_ID, 1000));
        helper.assertTrue(messages.isEmpty(), "a pail on a warped wooly cow was told " + messages);
        helper.assertTrue(BreedingGameTests.traits(helper, cow).equals(before), "a pail on a warped wooly cow changed her record");
        helper.succeed();
    }

    public static void aWoolyCowStillTakesTheWoodenBucket(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal cow = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, cow, "UMBRA");
        ProductGameTests.assertMilks(helper, cow, item(helper, WOODEN_BUCKET), item(helper, "meadow:wooden_milk_bucket"));
        helper.succeed();
    }

    // The pail entry doesn't open the wooly cow to the iron bucket.
    public static void anIronBucketStillDoesNothingOnAWoolyCow(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal cow = ProductGameTests.mother(helper, type(helper, WOOLY_COW), 0.5);
        setVariant(helper, cow, "HIGHLAND");
        Lactation before = BreedingGameTests.traits(helper, cow).get(LACTATION);
        Player player = holding(helper, Items.BUCKET);

        InteractionResult result = GameTestCompat.interact(cow, player);

        helper.assertTrue(result == InteractionResult.CONSUME, "the iron bucket on a wooly cow answered " + result);
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "the iron bucket became " + player.getMainHandItem());
        helper.assertTrue(BreedingGameTests.traits(helper, cow).get(LACTATION).equals(before), "the iron bucket milked a wooly cow");
        helper.succeed();
    }

    // Both are milked with the iron bucket into vanilla's milk bucket, so a pail takes their milk as cow milk.
    public static void waterBuffaloAndYakGiveCowMilk(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD) || !FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        assertFillsAPail(helper, ProductGameTests.mother(helper, type(helper, WATER_BUFFALO), 0.5));
        assertFillsAPail(helper, ProductGameTests.mother(helper, type(helper, YAK), 0.5));
        helper.succeed();
    }

    private static void assertFillsAPail(GameTestHelper helper, Animal animal) {
        Lactation before = BreedingGameTests.traits(helper, animal).get(LACTATION);
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
        Player player = PailGameTests.holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());

        GameTestCompat.interact(animal, player);

        PailGameTests.assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.COW_ID, 1000));
        helper.assertTrue(!BreedingGameTests.traits(helper, animal).get(LACTATION).equals(before), animal.getType() + " wasn't counted as milked");
    }

    // The variant is Meadow's enum: the constant is found by name on the enum class getVariant returns.
    private static void setVariant(GameTestHelper helper, Animal cow, String name) {
        try {
            Class<?> variants = cow.getClass().getMethod("getVariant").getReturnType();
            Object variant = null;
            for (Object constant : variants.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(name)) {
                    variant = constant;
                }
            }
            helper.assertTrue(variant != null, "no wooly cow variant " + name);
            cow.getClass().getMethod("setVariant", variants).invoke(cow, variant);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't set a wooly cow's variant: " + exception);
        }
    }

    private static void regrow(GameTestHelper helper, Animal cow) {
        try {
            Method setSheared = cow.getClass().getMethod("setSheared", boolean.class);
            setSheared.invoke(cow, false);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't regrow a wooly cow's wool: " + exception);
        }
    }

    private static Item item(GameTestHelper helper, String id) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(id)).orElseThrow(() -> GameTestCompat.failure(helper, "no item " + id));
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Animal> type(GameTestHelper helper, String id) {
        return (EntityType<? extends Animal>) BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(id))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + id));
    }
}
