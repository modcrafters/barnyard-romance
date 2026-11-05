package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.compat.NaturalistCompat;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.Hatching;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.eggs.RoosterNear;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests.boxBeside;
import static net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests.eggsNear;
import static net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests.once;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// These run with Naturalist loaded, through the runs in gradle/naturalist-runs.gradle. They find its creatures by id, so
// the mod still doesn't compile against it. Anywhere else they pass at once and prove nothing, except that a Naturalist
// run with no Naturalist in its mods folder fails instead of looking green.
public final class NaturalistGameTests {
    private static final String DEER = "naturalist:deer";
    private static final String BOAR = "naturalist:boar";
    private static final String DUCK = "naturalist:duck";
    private static final String TURKEY = "naturalist:turkey";
    private static final String OSTRICH = "naturalist:ostrich";
    private static final int WALK_TICKS = 400;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("naturalist_livestock_is_tracked", 20, NaturalistGameTests::livestockIsTracked),
            new BreedingGameTests.Case("naturalist_others_are_ignored", 20, NaturalistGameTests::othersAreIgnored),
            new BreedingGameTests.Case("naturalist_deer_and_boar_get_pregnant", 20, NaturalistGameTests::deerAndBoarGetPregnant),
            new BreedingGameTests.Case("naturalist_ostriches_need_a_male_and_a_female", 20, NaturalistGameTests::ostrichesNeedAMaleAndAFemale),
            new BreedingGameTests.Case("naturalist_food_does_not_breed_ducks_or_turkeys", 20, NaturalistGameTests::foodDoesNotBreedDucksOrTurkeys),
            new BreedingGameTests.Case("naturalist_duck_timer_is_held_off", 20, NaturalistGameTests::duckTimerIsHeldOff),
            new BreedingGameTests.Case("naturalist_excluded_duck_keeps_naturalists_timer", 20, NaturalistGameTests::excludedDuckKeepsNaturalistsTimer),
            new BreedingGameTests.Case("naturalist_duck_with_no_box_lays_plain_eggs_on_the_ground", 20, NaturalistGameTests::duckWithNoBoxLaysPlainEggsOnTheGround),
            new BreedingGameTests.Case("naturalist_duck_uses_a_box_and_eggs_are_fertilized", WALK_TICKS, NaturalistGameTests::duckUsesABoxAndEggsAreFertilized),
            new BreedingGameTests.Case("naturalist_turkey_lays_vanilla_eggs_with_no_male", WALK_TICKS, NaturalistGameTests::turkeyLaysVanillaEggsWithNoMale),
            new BreedingGameTests.Case("naturalist_turkey_hatches_turkeys", WALK_TICKS, NaturalistGameTests::turkeyHatchesTurkeys),
            new BreedingGameTests.Case("naturalist_rooster_does_not_fertilize_other_birds", 20, NaturalistGameTests::roosterDoesNotFertilizeOtherBirds),
            new BreedingGameTests.Case("naturalist_familiarity_tames_ducks_and_ostriches", 20, NaturalistGameTests::familiarityTamesDucksAndOstriches),
            new BreedingGameTests.Case("naturalist_duck_on_water_uses_a_box", WALK_TICKS, NaturalistGameTests::duckOnWaterUsesABox),
            new BreedingGameTests.Case("naturalist_sitting_duck_lays_where_she_sits", 20, NaturalistGameTests::sittingDuckLaysWhereSheSits)
    );

    private NaturalistGameTests() {
    }

    // True when the test should run.
    private static boolean naturalist(GameTestHelper helper) {
        if (Services.PLATFORM.isModLoaded("naturalist")) {
            return true;
        }
        if (Boolean.getBoolean("barnyard_romance.expect_naturalist")) {
            throw GameTestCompat.failure(helper, "this run should have Naturalist in its mods folder");
        }
        helper.succeed();
        return false;
    }

    public static void livestockIsTracked(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        int x = 1;
        for (String id : NaturalistCompat.LIVESTOCK) {
            helper.assertTrue(BarnyardConfig.current().speciesEnabled(id), id + " isn't enabled");
            Animal animal = GameTestCompat.spawn(helper, type(helper, id), x, 1, 4);
            x++;
            helper.assertTrue(Services.DATA.get(animal).isPresent(), id + " has no traits");
        }
        helper.succeed();
    }

    public static void othersAreIgnored(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        for (String id : List.of("naturalist:firefly", "naturalist:whale", "naturalist:capybara", "naturalist:zebra")) {
            helper.assertTrue(!BarnyardConfig.current().speciesEnabled(id), id + " is enabled");
        }
        Animal firefly = GameTestCompat.spawn(helper, type(helper, "naturalist:firefly"), 4, 1, 4);
        helper.assertTrue(Services.DATA.get(firefly).isEmpty(), "a firefly has traits");
        helper.succeed();
    }

    public static void deerAndBoarGetPregnant(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        for (String id : List.of(DEER, BOAR)) {
            Animal mother = BreedingGameTests.female(helper, type(helper, id));
            Animal father = BreedingGameTests.male(helper, type(helper, id));
            Animal otherMale = BreedingGameTests.male(helper, type(helper, id));
            mother.setInLove(null);
            father.setInLove(null);
            otherMale.setInLove(null);
            helper.assertTrue(!father.canMate(otherMale), "two male " + id + " can mate");
            helper.assertTrue(mother.canMate(father), "a " + id + " couple can't mate");

            mother.spawnChildFromBreeding(helper.getLevel(), father);

            helper.assertTrue(Services.DATA.getPregnancy(mother).isPresent(), "a " + id + " isn't pregnant");
        }
        helper.succeed();
    }

    public static void ostrichesNeedAMaleAndAFemale(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal female = BreedingGameTests.female(helper, type(helper, OSTRICH));
        Animal male = BreedingGameTests.male(helper, type(helper, OSTRICH));
        Animal otherMale = BreedingGameTests.male(helper, type(helper, OSTRICH));
        female.setInLove(null);
        male.setInLove(null);
        otherMale.setInLove(null);

        helper.assertTrue(!male.canMate(otherMale), "two male ostriches can mate");
        helper.assertTrue(female.canMate(male), "an ostrich couple can't mate");
        helper.succeed();
    }

    public static void foodDoesNotBreedDucksOrTurkeys(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 1.0, 3);
        Animal turkey = bird(helper, TURKEY, Sex.FEMALE, 1.0, 5);

        FamiliarityActions.interact(duck, holding(helper, Items.COD), InteractionHand.MAIN_HAND);
        FamiliarityActions.interact(turkey, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        duck.setInLove(null);
        turkey.setInLove(null);

        helper.assertTrue(!duck.isInLove(), "fish put a duck in love");
        helper.assertTrue(!turkey.isInLove(), "seeds put a turkey in love");
        helper.assertTrue(traits(helper, duck).lastFedTick() >= 0L, "fish didn't feed the duck");
        helper.assertTrue(traits(helper, turkey).lastFedTick() >= 0L, "seeds didn't feed the turkey");
        helper.succeed();
    }

    public static void duckTimerIsHeldOff(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        for (Sex sex : List.of(Sex.FEMALE, Sex.MALE)) {
            Animal duck = bird(helper, DUCK, sex, 0.5, 4);
            setEggTime(helper, duck, 1);

            duck.aiStep();

            helper.assertTrue(eggTime(helper, duck) > 1, "a " + sex + " duck's timer ran down");
            helper.assertTrue(eggsNear(helper, duck, duckEgg(helper)) == 0, "a " + sex + " duck laid by its own timer");
        }
        helper.succeed();
    }

    // Naturalist's own timer has to come back when the mod's rules stop applying, or ducks would never lay at all.
    public static void excludedDuckKeepsNaturalistsTimer(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 4);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            for (String json : List.of(
                    "{\"exclude\": [\"naturalist:duck\"]}",
                    "{\"species\": {\"naturalist:duck\": {\"eggs\": {\"enabled\": false}}}}")) {
                BarnyardConfig.set(BarnyardConfig.parse(json));
                setEggTime(helper, duck, 100);

                duck.aiStep();

                helper.assertTrue(eggTime(helper, duck) == 99, "with " + json + " the timer is at " + eggTime(helper, duck));
            }
        } finally {
            BarnyardConfig.set(config);
        }
        helper.succeed();
    }

    public static void duckWithNoBoxLaysPlainEggsOnTheGround(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 3);
        bird(helper, DUCK, Sex.MALE, 0.5, 5);

        inPen(DUCK, () -> EggLaying.layDue(duck, helper.getLevel(), 1));

        helper.assertTrue(eggsNear(helper, duck, duckEgg(helper)) == 1, "no duck egg on the ground");
        helper.assertTrue(eggsNear(helper, duck, BarnyardItems.FERTILIZED_EGG) == 0, "a fertilized egg on the ground");
        helper.succeed();
    }

    public static void duckUsesABoxAndEggsAreFertilized(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 2);
        Animal drake = bird(helper, DUCK, Sex.MALE, 0.5, 5);
        layInABox(helper, DUCK, duck, box -> {
            EggData egg = FertilizedEggItem.data(box.getItem(0))
                    .orElseThrow(() -> GameTestCompat.failure(helper, "the box holds " + box.getItem(0)));
            helper.assertTrue(DUCK.equals(egg.species()), "the egg is a " + egg.species());
            helper.assertTrue(drake.getUUID().equals(egg.father()), "the father is " + egg.father());
            helper.succeed();
        });
    }

    public static void turkeyLaysVanillaEggsWithNoMale(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal turkey = bird(helper, TURKEY, Sex.FEMALE, 0.5, 2);
        layInABox(helper, TURKEY, turkey, box -> {
            helper.assertTrue(box.getItem(0).is(Items.EGG), "the box holds " + box.getItem(0));
            helper.succeed();
        });
    }

    public static void turkeyHatchesTurkeys(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal turkey = bird(helper, TURKEY, Sex.FEMALE, 0.5, 2);
        bird(helper, TURKEY, Sex.MALE, 0.5, 5);
        layInABox(helper, TURKEY, turkey, box -> {
            EggData egg = FertilizedEggItem.data(box.getItem(0))
                    .orElseThrow(() -> GameTestCompat.failure(helper, "the box holds " + box.getItem(0)));
            Animal chick = Hatching.hatch(helper.getLevel(), egg, helper.absoluteVec(new Vec3(4.5, 2.0, 4.5)), helper.getLevel().getGameTime())
                    .orElseThrow(() -> GameTestCompat.failure(helper, "the egg didn't hatch"));
            helper.assertTrue(chick.getType() == type(helper, TURKEY), "it hatched a " + chick.getType());
            helper.succeed();
        });
    }

    public static void roosterDoesNotFertilizeOtherBirds(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 2);
        Animal turkey = bird(helper, TURKEY, Sex.FEMALE, 0.5, 3);
        EggGameTests.chicken(helper, Sex.MALE, 1.2, 0.5, 4);

        for (Animal hen : List.of(duck, turkey)) {
            inPen(BuiltInRegistries.ENTITY_TYPE.getKey(hen.getType()).toString(), () ->
                    helper.assertTrue(EggLaying.roosterNear(hen, helper.getLevel()) == RoosterNear.NONE, "a rooster counts for a " + hen.getType()));
        }
        helper.succeed();
    }

    // Naturalist's own taming sits behind the mod's feeding, so familiarity tames them, as it does wolves and cats.
    public static void familiarityTamesDucksAndOstriches(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 1.0, 2);
        Animal ostrich = bird(helper, OSTRICH, Sex.FEMALE, 1.0, 4);
        Animal wary = bird(helper, DUCK, Sex.FEMALE, 0.0, 6);

        FamiliarityActions.interact(duck, holding(helper, Items.COD), InteractionHand.MAIN_HAND);
        FamiliarityActions.interact(ostrich, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        FamiliarityActions.interact(wary, holding(helper, Items.COD), InteractionHand.MAIN_HAND);

        helper.assertTrue(((TamableAnimal) duck).isTame(), "a familiar duck didn't tame");
        helper.assertTrue(((TamableAnimal) ostrich).isTame(), "a familiar ostrich didn't tame");
        helper.assertTrue(!((TamableAnimal) wary).isTame(), "an unfamiliar duck tamed");
        helper.succeed();
    }

    public static void duckOnWaterUsesABox(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 2);
        NestingBoxBlockEntity[] placed = new NestingBoxBlockEntity[1];
        helper.runAfterDelay(EggGameTests.LANDED, () -> {
            BlockPos feet = duck.blockPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.getLevel().setBlockAndUpdate(feet.offset(dx, 0, dz), Blocks.WATER.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(feet.offset(dx, -1, dz), Blocks.WATER.defaultBlockState());
                }
            }
        });
        once(helper, () -> placed[0] == null && duck.isInWater() && !duck.onGround(), () -> {
            placed[0] = boxBeside(helper, duck, 5);
            inPen(DUCK, () -> EggLaying.layDue(duck, helper.getLevel(), 1));
        });
        once(helper, () -> placed[0] != null && placed[0].occupied(), helper::succeed);
    }

    public static void sittingDuckLaysWhereSheSits(GameTestHelper helper) {
        if (!naturalist(helper)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 2);
        ((TamableAnimal) duck).tame(holding(helper, Items.COD));
        ((TamableAnimal) duck).setOrderedToSit(true);
        NestingBoxBlockEntity box = boxBeside(helper, duck, 5);

        inPen(DUCK, () -> EggLaying.layDue(duck, helper.getLevel(), 1));

        helper.assertTrue(eggsNear(helper, duck, duckEgg(helper)) == 1, "a sitting duck laid nothing");
        helper.assertTrue(!box.occupied(), "a sitting duck reached a box");
        helper.succeed();
    }

    private static void layInABox(GameTestHelper helper, String species, Animal hen, Consumer<NestingBoxBlockEntity> then) {
        NestingBoxBlockEntity[] placed = new NestingBoxBlockEntity[1];
        helper.runAfterDelay(EggGameTests.LANDED, () -> {
            placed[0] = boxBeside(helper, hen, 5);
            inPen(species, () -> EggLaying.layDue(hen, helper.getLevel(), 1));
        });
        once(helper, () -> placed[0] != null && placed[0].occupied(), () -> {
            placed[0].finishSitting();
            then.accept(placed[0]);
        });
    }

    // Short ranges, because tests run side by side within 16 blocks.
    private static void inPen(String species, Runnable action) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"" + species + "\": {\"eggs\": {\"boxRange\": 8, \"roosterRange\": 4}}}}"));
            action.run();
        } finally {
            BarnyardConfig.set(config);
        }
    }

    private static Animal bird(GameTestHelper helper, String id, Sex sex, double familiarity, int x) {
        Animal bird = GameTestCompat.spawn(helper, type(helper, id), x, 1, 4);
        Services.DATA.set(bird, new AnimalTraits(sex, sex == Sex.MALE ? 1.2 : EggGameTests.MEDIUM_HEN, familiarity, -1L));
        Services.DATA.setLaying(bird, new Laying(GameDay.of(helper.getLevel().getOverworldClockTime()), List.of(), 0));
        return bird;
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Animal> type(GameTestHelper helper, String id) {
        return (EntityType<? extends Animal>) BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(id))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + id));
    }

    private static Item duckEgg(GameTestHelper helper) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.tryParse("naturalist:duck_egg"))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no duck egg"));
    }

    private static int eggTime(GameTestHelper helper, Animal duck) {
        try {
            return duck.getClass().getField("eggTime").getInt(duck);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "a duck has no eggTime: " + exception);
        }
    }

    private static void setEggTime(GameTestHelper helper, Animal duck, int ticks) {
        try {
            duck.getClass().getField("eggTime").setInt(duck, ticks);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "a duck has no eggTime: " + exception);
        }
    }
}
