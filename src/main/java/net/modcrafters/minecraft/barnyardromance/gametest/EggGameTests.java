package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxMenu;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
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
    static final BlockPos BOX = new BlockPos(2, 2, 2);
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
            new BreedingGameTests.Case("excluded_chickens_thrown_eggs_hatch", 20, EggGameTests::excludedChickensThrownEggsHatch),
            new BreedingGameTests.Case("box_takes_ten_eggs", 20, EggGameTests::boxTakesTenEggs),
            new BreedingGameTests.Case("plain_eggs_stack_in_one_slot", 20, EggGameTests::plainEggsStackInOneSlot),
            new BreedingGameTests.Case("plain_eggs_cannot_be_put_in", 20, EggGameTests::plainEggsCannotBePutIn),
            new BreedingGameTests.Case("hopper_pulls_only_plain_eggs", 20, EggGameTests::hopperPullsOnlyPlainEggs),
            new BreedingGameTests.Case("countdown_pauses_outside_the_box", 20, EggGameTests::countdownPausesOutsideTheBox),
            new BreedingGameTests.Case("box_drops_eggs_when_broken", 20, EggGameTests::boxDropsEggsWhenBroken),
            new BreedingGameTests.Case("box_recipe", 20, EggGameTests::boxRecipe),
            new BreedingGameTests.Case("egg_hatches_in_the_box", 40, EggGameTests::eggHatchesInTheBox),
            new BreedingGameTests.Case("hatched_chick_inherits", 20, EggGameTests::hatchedChickInherits),
            new BreedingGameTests.Case("hatched_chick_joins_mothers_book", 20, EggGameTests::hatchedChickJoinsMothersBook),
            new BreedingGameTests.Case("blocked_front_hatches_on_top", 20, EggGameTests::blockedFrontHatchesOnTop),
            new BreedingGameTests.Case("hatching_catches_up_after_unload", 20, EggGameTests::hatchingCatchesUpAfterUnload)
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

    public static void boxTakesTenEggs(GameTestHelper helper) {
        NestingBoxBlockEntity fertile = box(helper, BOX);
        NestingBoxBlockEntity plain = box(helper, BOX.east(2));

        for (int i = 0; i < NestingBoxBlockEntity.SLOTS; i++) {
            helper.assertTrue(fertile.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L))), "fertilized egg " + (i + 1) + " didn't fit");
        }
        boolean eleventh = fertile.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        for (int i = 0; i < NestingBoxBlockEntity.SLOTS * 16; i++) {
            helper.assertTrue(plain.placeEgg(new ItemStack(Items.EGG)), "plain egg " + (i + 1) + " didn't fit");
        }
        boolean extra = plain.placeEgg(new ItemStack(Items.EGG));

        helper.assertTrue(!eleventh, "an eleventh fertilized egg fit");
        helper.assertTrue(!extra, "a 161st plain egg fit");
        helper.succeed();
    }

    public static void plainEggsStackInOneSlot(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);

        for (int i = 0; i < 3; i++) {
            box.placeEgg(new ItemStack(Items.EGG));
        }

        helper.assertTrue(box.getItem(0).getCount() == 3 && box.getItem(1).isEmpty(), "three plain eggs: " + box.getItem(0) + ", " + box.getItem(1));
        helper.succeed();
    }

    public static void plainEggsCannotBePutIn(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(new ItemStack(Items.EGG));
        Player player = holding(helper, Items.EGG);
        NestingBoxMenu menu = new NestingBoxMenu(1, player.getInventory(), box);
        ItemStack fertile = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));

        boolean plainAllowed = menu.getSlot(0).mayPlace(new ItemStack(Items.EGG));
        boolean fertileAllowed = menu.getSlot(1).mayPlace(fertile);
        boolean ontoAnEgg = menu.getSlot(0).mayPlace(fertile);
        //? if >=1.21.5 {
        int held = menu.slots.size() - 9 + player.getInventory().getSelectedSlot();
        //?} else
        //int held = menu.slots.size() - 9 + player.getInventory().selected;
        menu.quickMoveStack(player, held);

        helper.assertTrue(!plainAllowed, "a plain egg can be put in a nest by hand");
        helper.assertTrue(fertileAllowed, "a fertilized egg can't be put in an empty nest");
        helper.assertTrue(!ontoAnEgg, "a fertilized egg can be put on top of another egg");
        helper.assertTrue(box.getItem(0).getCount() == 1 && box.getItem(1).isEmpty(), "shift-clicking moved a plain egg into the box");
        helper.succeed();
    }

    public static void hopperPullsOnlyPlainEggs(GameTestHelper helper) {
        helper.setBlock(BOX.below(), Blocks.HOPPER.defaultBlockState());
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(new ItemStack(Items.EGG, 2));
        box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(BOX.below()));

        for (int i = 0; i < 4; i++) {
            HopperBlockEntity.suckInItems(helper.getLevel(), hopper);
        }

        helper.assertTrue(box.getItem(0).isEmpty(), "the hopper left plain eggs: " + box.getItem(0));
        helper.assertTrue(box.getItem(1).is(BarnyardItems.FERTILIZED_EGG), "the hopper took the fertilized egg");
        helper.succeed();
    }

    public static void countdownPausesOutsideTheBox(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        long placed = helper.getLevel().getGameTime();
        box.setItem(0, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        long hatchAt = FertilizedEggItem.data(box.getItem(0)).orElseThrow().hatchAt();
        helper.assertTrue(hatchAt == placed + 1_000L, "an egg put in at " + placed + " hatches at " + hatchAt);

        helper.runAfterDelay(5, () -> {
            long taken = helper.getLevel().getGameTime();
            EggData out = FertilizedEggItem.data(box.removeItemNoUpdate(0)).orElseThrow();
            helper.assertTrue(!out.inBox() && out.remainingTicks() == 1_000L - (taken - placed), "an egg taken out after " + (taken - placed) + " ticks: " + out);
            helper.succeed();
        });
    }

    public static void boxDropsEggsWhenBroken(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(new ItemStack(Items.EGG, 3));
        box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));

        helper.destroyBlock(BOX);

        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BOX)).inflate(2.0));
        int plain = dropped.stream().filter(item -> item.getItem().is(Items.EGG)).mapToInt(item -> item.getItem().getCount()).sum();
        List<EggData> fertile = dropped.stream().map(item -> FertilizedEggItem.data(item.getItem())).flatMap(Optional::stream).toList();
        helper.assertTrue(plain == 3, "breaking the box dropped " + plain + " plain eggs");
        helper.assertTrue(fertile.size() == 1 && !fertile.getFirst().inBox(), "breaking the box dropped fertilized eggs " + fertile);
        helper.succeed();
    }

    public static void boxRecipe(GameTestHelper helper) {
        ItemStack plank = new ItemStack(Items.OAK_PLANKS);
        CraftingInput input = CraftingInput.of(3, 3, List.of(
                plank.copy(), ItemStack.EMPTY, plank.copy(),
                plank.copy(), new ItemStack(Items.WHEAT), plank.copy(),
                plank.copy(), plank.copy(), plank.copy()
        ));

        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());

        helper.assertTrue(recipe.isPresent(), "planks round wheat craft nothing");
        //? if >=1.21.2 {
        ItemStack result = recipe.get().value().assemble(input);
        //?} else
        //ItemStack result = recipe.get().value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(BarnyardBlocks.NESTING_BOX_ITEM), "planks round wheat craft " + result);
        helper.succeed();
    }

    public static void eggHatchesInTheBox(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.setItem(0, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 5L)));

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(box.getItem(0).isEmpty(), "the egg is still in the box: " + box.getItem(0));
            List<Chicken> chicks = helper.getLevel().getEntitiesOfClass(Chicken.class, new AABB(helper.absolutePos(BOX.north())));
            helper.assertTrue(chicks.size() == 1 && chicks.getFirst().isBaby(), "in front of the hole: " + chicks);
            helper.succeed();
        });
    }

    public static void hatchedChickInherits(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        EggData data = new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), LARGE_HEN, Growth.wild(), 1.0, 0.5,
                1.3, Growth.wild(), 1.0, "", 1_000L, 0L, helper.getLevel().getGameTime() - 1L);
        box.setItem(0, FertilizedEggItem.create(data));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        Animal chick = chickIn(helper, BOX.north());
        AnimalTraits traits = traits(helper, chick);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor("minecraft:chicken"));
        helper.assertTrue(traits.lineage().equals(new Lineage(data.mother(), data.father())), "the chick's parents are " + traits.lineage());
        helper.assertTrue(traits.bornTick() == helper.getLevel().getGameTime(), "the chick was born at " + traits.bornTick());
        helper.assertTrue(traits.familiarity() == Familiarity.inherited(0.5), "the chick starts at familiarity " + traits.familiarity());
        helper.assertTrue(size != SizeClass.S, "two XL parents hatched an S chick");
        helper.succeed();
    }

    public static void hatchedChickJoinsMothersBook(GameTestHelper helper) {
        Animal mother = hen(helper, LARGE_HEN, 0.5);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        UUID bookId = book.get(BarnyardComponents.HERD_BOOK);
        UUID motherId = mother.getUUID();
        mother.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        NestingBoxBlockEntity box = box(helper, BOX);
        EggData data = new EggData("minecraft:chicken", motherId, null, LARGE_HEN, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", 1_000L, 0L, helper.getLevel().getGameTime() - 1L);
        box.setItem(0, FertilizedEggItem.create(data));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        Animal chick = chickIn(helper, BOX.north());
        HerdBookData herd = HerdBookData.get(helper.getLevel().getServer());
        helper.assertTrue(herd.ledger().isMember(bookId, chick.getUUID()), "the chick didn't join its mother's book");
        helper.succeed();
    }

    public static void blockedFrontHatchesOnTop(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        helper.setBlock(BOX.north(), Blocks.STONE.defaultBlockState());
        box.setItem(0, FertilizedEggItem.create(new EggData("minecraft:chicken", UUID.randomUUID(), null, LARGE_HEN, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", 1_000L, 0L, helper.getLevel().getGameTime() - 1L)));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        chickIn(helper, BOX.above());
        helper.succeed();
    }

    // An egg whose time came while the box was unloaded hatches on the box's first tick back.
    public static void hatchingCatchesUpAfterUnload(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.setItem(0, FertilizedEggItem.create(new EggData("minecraft:chicken", UUID.randomUUID(), null, LARGE_HEN, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", 1_000L, 0L, helper.getLevel().getGameTime() - 50_000L)));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        helper.assertTrue(box.getItem(0).isEmpty(), "an overdue egg didn't hatch");
        chickIn(helper, BOX.north());
        helper.succeed();
    }

    static Animal chickIn(GameTestHelper helper, BlockPos pos) {
        List<Chicken> chicks = helper.getLevel().getEntitiesOfClass(Chicken.class, new AABB(helper.absolutePos(pos)));
        if (chicks.size() != 1) {
            throw GameTestCompat.failure(helper, "expected one chick at " + pos + ", found " + chicks.size());
        }
        return chicks.getFirst();
    }

    static NestingBoxBlockEntity box(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BarnyardBlocks.NESTING_BOX.defaultBlockState());
        return (NestingBoxBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
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
