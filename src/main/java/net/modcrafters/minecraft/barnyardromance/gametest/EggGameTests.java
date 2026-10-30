package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.5 {
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
//?}

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// In-game checks for egg layers. Days are driven by setting the overworld clock and ticking the hen by hand,
// then setting the clock back, all within one tick.
public final class EggGameTests {
    // Chickens use the default sizes: females 0.7 to 0.9, so 0.76 is M and 0.9 is XL.
    static final double MEDIUM_HEN = 0.76;
    static final double LARGE_HEN = 0.9;
    static final TagKey<Item> COMMON_EGGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "eggs"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("hen_lays_her_daily_eggs_on_the_ground", 20, EggGameTests::henLaysHerDailyEggsOnTheGround),
            new BreedingGameTests.Case("large_happy_hen_plans_three", 20, EggGameTests::largeHappyHenPlansThree),
            new BreedingGameTests.Case("hungry_hen_plans_none", 20, EggGameTests::hungryHenPlansNone),
            new BreedingGameTests.Case("rooster_and_chick_lay_nothing", 20, EggGameTests::roosterAndChickLayNothing),
            new BreedingGameTests.Case("vanilla_egg_timer_is_off", 20, EggGameTests::vanillaEggTimerIsOff),
            new BreedingGameTests.Case("seeds_never_breed_chickens", 20, EggGameTests::seedsNeverBreedChickens),
            new BreedingGameTests.Case("old_chicken_pregnancy_is_cleared", 20, EggGameTests::oldChickenPregnancyIsCleared),
            new BreedingGameTests.Case("excluded_chickens_lay_like_vanilla", 20, EggGameTests::excludedChickensLayLikeVanilla),
            new BreedingGameTests.Case("fertilized_eggs_do_not_stack", 20, EggGameTests::fertilizedEggsDoNotStack),
            new BreedingGameTests.Case("fertilized_egg_is_an_egg", 20, EggGameTests::fertilizedEggIsAnEgg),
            new BreedingGameTests.Case("thrown_plain_egg_never_hatches", 20, EggGameTests::thrownPlainEggNeverHatches),
            new BreedingGameTests.Case("thrown_fertilized_egg_hatches_tainted", 20, EggGameTests::thrownFertilizedEggHatchesTainted),
            new BreedingGameTests.Case("thrown_fertilized_egg_uses_the_chance", 20, EggGameTests::thrownFertilizedEggUsesTheChance),
            new BreedingGameTests.Case("excluded_chickens_thrown_eggs_hatch", 20, EggGameTests::excludedChickensThrownEggsHatch)
            //? if >=1.21.5 {
            , new BreedingGameTests.Case("warm_hen_lays_brown_eggs", 20, EggGameTests::warmHenLaysBrownEggs)
            //?}
    );

    private EggGameTests() {
    }

    public static void henLaysHerDailyEggsOnTheGround(GameTestHelper helper) {
        Animal hen = hen(helper, MEDIUM_HEN, 0.0);

        Laying planned = tickAt(helper, hen, afterMidnight(10L));
        helper.assertTrue(planned.day() == 10L && planned.times().size() == 1, "an M hen planned " + planned);
        Laying after = tickAt(helper, hen, at(10L, planned.times().getFirst()));

        helper.assertTrue(after.next() == 1, "the egg wasn't counted: " + after);
        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 1, "an M hen laid " + eggsNear(helper, hen, Items.EGG) + " eggs");
        helper.succeed();
    }

    public static void largeHappyHenPlansThree(GameTestHelper helper) {
        Animal hen = hen(helper, LARGE_HEN, 0.5);

        Laying planned = tickAt(helper, hen, afterMidnight(10L));

        helper.assertTrue(planned.times().size() == 3, "an XL happy hen planned " + planned);
        helper.succeed();
    }

    public static void hungryHenPlansNone(GameTestHelper helper) {
        Animal hen = hen(helper, LARGE_HEN, 0.5);
        Services.DATA.set(hen, traits(helper, hen).withHunger(new Hunger(10L, -1L, 9L)));

        Laying planned = tickAt(helper, hen, afterMidnight(10L));

        helper.assertTrue(planned.times().isEmpty(), "a hen that went hungry yesterday planned " + planned);
        helper.succeed();
    }

    public static void roosterAndChickLayNothing(GameTestHelper helper) {
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 3);
        Animal chick = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 5);
        chick.setBaby(true);

        Laying roosterPlan = tickAt(helper, rooster, afterMidnight(10L));
        Laying chickPlan = tickAt(helper, chick, afterMidnight(10L));

        helper.assertTrue(roosterPlan.times().isEmpty(), "a rooster planned " + roosterPlan);
        helper.assertTrue(chickPlan.times().isEmpty(), "a chick planned " + chickPlan);
        helper.succeed();
    }

    public static void vanillaEggTimerIsOff(GameTestHelper helper) {
        Chicken hen = (Chicken) hen(helper, MEDIUM_HEN, 0.0);
        hen.eggTime = 1;

        hen.aiStep();

        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 0, "vanilla's egg timer laid an egg");
        helper.succeed();
    }

    public static void seedsNeverBreedChickens(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 1.0, 3);
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 1.0, 5);

        FamiliarityActions.interact(hen, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        FamiliarityActions.interact(rooster, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        hen.setInLove(null);

        helper.assertTrue(!hen.isInLove() && !rooster.isInLove(), "seeds put chickens in love");
        helper.assertTrue(traits(helper, hen).lastFedTick() >= 0L, "seeds didn't feed the hen");
        helper.succeed();
    }

    public static void oldChickenPregnancyIsCleared(GameTestHelper helper) {
        Animal hen = hen(helper, MEDIUM_HEN, 0.5);
        Services.DATA.setPregnancy(hen, new Pregnancy(0L, 1L, "minecraft:chicken", 1.2, 1, 0.5));

        EggLaying.serverTick(hen, helper.getLevel());

        helper.assertTrue(Services.DATA.getPregnancy(hen).isEmpty(), "a hen kept her pregnancy");
        helper.succeed();
    }

    // Vanilla only starts love for a real server player, which a mock player isn't, so love is set directly:
    // the mod's love gate is what's being checked.
    public static void excludedChickensLayLikeVanilla(GameTestHelper helper) {
        Chicken hen = (Chicken) hen(helper, MEDIUM_HEN, 0.0);
        Chicken other = (Chicken) chicken(helper, Sex.MALE, 1.2, 1.0, 5);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:chicken\"]}"));
            hen.eggTime = 1;
            hen.aiStep();
            hen.setInLove(null);
            other.setInLove(null);
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 1, "an excluded chicken didn't lay by vanilla's timer");
        helper.assertTrue(hen.isInLove() && other.isInLove(), "excluded chickens couldn't fall in love");
        helper.succeed();
    }

    //? if >=1.21.5 {
    public static void warmHenLaysBrownEggs(GameTestHelper helper) {
        Chicken hen = (Chicken) hen(helper, MEDIUM_HEN, 0.0);
        hen.setVariant(helper.getLevel().registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.WARM));

        Laying planned = tickAt(helper, hen, afterMidnight(10L));
        tickAt(helper, hen, at(10L, planned.times().getFirst()));

        helper.assertTrue(eggsNear(helper, hen, Items.BROWN_EGG) == 1, "a warm hen didn't lay a brown egg");
        helper.succeed();
    }
    //?}

    public static void fertilizedEggsDoNotStack(GameTestHelper helper) {
        ItemStack first = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));
        ItemStack second = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));
        SimpleContainer container = new SimpleContainer(2);

        container.addItem(first);
        container.addItem(second);

        helper.assertTrue(first.getMaxStackSize() == 1, "a fertilized egg stacks to " + first.getMaxStackSize());
        helper.assertTrue(container.getItem(0).getCount() == 1 && container.getItem(1).getCount() == 1, "two fertilized eggs shared a slot");
        helper.succeed();
    }

    public static void fertilizedEggIsAnEgg(GameTestHelper helper) {
        ItemStack egg = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));
        CraftingInput input = CraftingInput.of(3, 3, List.of(
                new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET),
                new ItemStack(Items.SUGAR), egg, new ItemStack(Items.SUGAR),
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT)
        ));

        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());

        helper.assertTrue(egg.is(COMMON_EGGS), "a fertilized egg isn't in c:eggs");
        helper.assertTrue(recipe.isPresent(), "cake doesn't take a fertilized egg");
        helper.succeed();
    }

    public static void thrownPlainEggNeverHatches(GameTestHelper helper) {
        for (int i = 0; i < 64; i++) {
            throwAndLand(helper, new ItemStack(Items.EGG));
        }

        helper.assertTrue(chickensIn(helper) == 0, "a thrown plain egg hatched " + chickensIn(helper) + " chickens");
        helper.succeed();
    }

    public static void thrownFertilizedEggHatchesTainted(GameTestHelper helper) {
        EggData data = eggData(LARGE_HEN, 0.7, 1_000L);

        Animal chick = ThrownEggs.hatchTainted(helper.getLevel(), helper.absoluteVec(new Vec3(4.5, 2.0, 4.5)), data, helper.getLevel().getRandom())
                .orElseThrow(() -> GameTestCompat.failure(helper, "no chick hatched"));

        AnimalTraits traits = traits(helper, chick);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor("minecraft:chicken"));
        helper.assertTrue(chick.isBaby(), "a tainted chick was born grown");
        helper.assertTrue(size == SizeClass.S || size == SizeClass.M, "a tainted chick is " + size);
        helper.assertTrue(traits.weight() == 0.25, "a tainted chick weighs " + traits.weight());
        helper.assertTrue(traits.lineage().equals(Lineage.UNKNOWN), "a tainted chick knows its parents: " + traits.lineage());
        helper.assertTrue(traits.bornTick() >= 0L, "a tainted chick has no birth time");
        helper.succeed();
    }

    public static void thrownFertilizedEggUsesTheChance(GameTestHelper helper) {
        BarnyardConfig config = BarnyardConfig.current();
        int always;
        int never;
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"thrownHatchChance\": 1.0}}}}"));
            throwAndLand(helper, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
            always = chickensIn(helper);
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"thrownHatchChance\": 0.0}}}}"));
            throwAndLand(helper, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
            never = chickensIn(helper) - always;
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(always == 1, "a sure fertilized egg hatched " + always + " chicks");
        helper.assertTrue(never == 0, "a fertilized egg that can't hatch made " + never + " chicks");
        helper.succeed();
    }

    public static void excludedChickensThrownEggsHatch(GameTestHelper helper) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:chicken\"]}"));
            for (int i = 0; i < 64; i++) {
                throwAndLand(helper, new ItemStack(Items.EGG));
            }
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(chickensIn(helper) > 0, "64 eggs thrown with chickens excluded hatched nothing");
        helper.succeed();
    }

    // An egg dropped onto the pen's floor and ticked by hand until it breaks, all within this tick.
    static void throwAndLand(GameTestHelper helper, ItemStack stack) {
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 3.0, 4.5));
        //? if >=1.21.2 {
        ThrownEgg egg = new ThrownEgg(helper.getLevel(), at.x, at.y, at.z, stack);
        //?} else {
        /*ThrownEgg egg = new ThrownEgg(helper.getLevel(), at.x, at.y, at.z);
        egg.setItem(stack);
        *///?}
        egg.setDeltaMovement(0.0, -1.0, 0.0);
        helper.getLevel().addFreshEntity(egg);
        for (int i = 0; i < 40 && !egg.isRemoved(); i++) {
            egg.tick();
        }
    }

    static int chickensIn(GameTestHelper helper) {
        AABB pen = new AABB(helper.absoluteVec(Vec3.ZERO), helper.absoluteVec(new Vec3(9.0, 5.0, 9.0)));
        return helper.getLevel().getEntitiesOfClass(Chicken.class, pen).size();
    }

    static EggData eggData(double motherScale, double fatherScale, long ticks) {
        return new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), motherScale, Growth.wild(), 0.5, 0.5,
                fatherScale, Growth.wild(), 0.5, "", ticks, ticks, -1L);
    }

    static Animal hen(GameTestHelper helper, double scale, double familiarity) {
        return chicken(helper, Sex.FEMALE, scale, familiarity, 3);
    }

    static Animal chicken(GameTestHelper helper, Sex sex, double scale, double familiarity, int x) {
        Animal chicken = GameTestCompat.spawn(helper, EntityTypes.CHICKEN, x, 1, 4);
        Services.DATA.set(chicken, new AnimalTraits(sex, scale, familiarity, -1L));
        return chicken;
    }

    // One tick of the hen's day at the given overworld clock time: hunger is settled first, as the real tick does.
    static Laying tickAt(GameTestHelper helper, Animal hen, long clock) {
        long before = helper.getLevel().getOverworldClockTime();
        try {
            GameTestCompat.setOverworldClock(helper, clock);
            FamiliarityCare.settle(hen, clock);
            EggLaying.serverTick(hen, helper.getLevel());
        } finally {
            GameTestCompat.setOverworldClock(helper, before);
        }
        return Services.DATA.getLaying(hen).orElse(Laying.NONE);
    }

    static long afterMidnight(long day) {
        return at(day, 1);
    }

    // The overworld clock time on a day, given in ticks since that day's midnight.
    static long at(long day, int sinceMidnight) {
        return clock(day) - (GameDay.TICKS - 18_000L) + sinceMidnight;
    }

    static int eggsNear(GameTestHelper helper, Animal animal, Item egg) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, animal.getBoundingBox().inflate(4.0)).stream()
                .filter(item -> item.getItem().is(egg))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
    }
}
