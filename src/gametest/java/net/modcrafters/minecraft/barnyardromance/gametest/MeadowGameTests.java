package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;

import java.lang.reflect.Method;
import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// These run with Let's Do Meadow loaded, through the farm-mods runs (gradle/farm-mods-runs.gradle). They find its
// creatures and items by id and reflection, so the mod still doesn't compile against it. In a run without it they pass at
// once and prove nothing, except that a farm-mods run with no jar in its mods folder fails instead of looking green
// (FarmMods.present).
public final class MeadowGameTests {
    private static final String MOD = "meadow";
    private static final String WOODEN_BUCKET = "meadow:wooden_bucket";
    private static final String WOOLY_COW = "meadow:wooly_cow";
    private static final String WATER_BUFFALO = "meadow:water_buffalo";

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_bull_refuses_the_wooden_bucket", 20, MeadowGameTests::aBullRefusesTheWoodenBucket),
            new BreedingGameTests.Case("a_dry_cow_refuses_the_wooden_bucket", 20, MeadowGameTests::aDryCowRefusesTheWoodenBucket),
            new BreedingGameTests.Case("a_milked_cow_gives_wooden_milk", 20, MeadowGameTests::aMilkedCowGivesWoodenMilk),
            new BreedingGameTests.Case("a_water_buffalo_gives_buffalo_milk_once_a_day", 20, MeadowGameTests::aWaterBuffaloGivesBuffaloMilkOnceADay),
            new BreedingGameTests.Case("a_warped_wooly_cow_gives_warped_milk", 20, MeadowGameTests::aWarpedWoolyCowGivesWarpedMilk),
            new BreedingGameTests.Case("taking_the_wooden_bucket_off_cows_closes_it", 20, MeadowGameTests::takingTheWoodenBucketOffCowsClosesIt),
            new BreedingGameTests.Case("a_wooly_cow_is_sheared_once_a_day", 20, MeadowGameTests::aWoolyCowIsShearedOnceADay)
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
