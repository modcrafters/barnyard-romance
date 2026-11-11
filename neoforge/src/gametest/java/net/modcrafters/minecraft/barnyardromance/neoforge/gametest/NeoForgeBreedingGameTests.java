package net.modcrafters.minecraft.barnyardromance.neoforge.gametest;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.gametest.AdvancementGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FeederGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.gametest.MockPlayers;
import net.modcrafters.minecraft.barnyardromance.gametest.NaturalistGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.StatGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.VigourGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.UpkeepGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FightGameTests;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.HerdBookGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
//?} else {
/*import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
*///?}
//? if >=1.21.5 {
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
//?} else {
/*import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.Collection;
*///?}

// Registers the shared breeding tests, plus the ones that need NeoForge's BabyEntitySpawnEvent.
// Before 1.21.5 a game test is a TestFunction made by a generator method instead of a registered test instance.
//? if <1.21.5
//@GameTestHolder(Constants.MOD_ID)
public final class NeoForgeBreedingGameTests {
    private enum Answer {
        CANCEL,
        NOTHING,
        CALF
    }

    // Only mothers a test put here are touched, so the listener cannot affect anything else.
    private static final Map<UUID, Answer> ANSWERS = new ConcurrentHashMap<>();
    // Animals whose death a test has another mod cancel.
    private static final Set<UUID> SPARED = ConcurrentHashMap.newKeySet();

    private static final List<BreedingGameTests.Case> CASES = cases();

    private NeoForgeBreedingGameTests() {
    }

    public static void register(IEventBus modBus) {
        if (!GameTestHooks.isGametestEnabled()) {
            return;
        }
        //? if >=1.21.5 {
        DeferredRegister<Consumer<GameTestHelper>> functions = DeferredRegister.create(Registries.TEST_FUNCTION, Constants.MOD_ID);
        for (BreedingGameTests.Case test : CASES) {
            functions.register(test.name(), () -> test.body());
        }
        functions.register(modBus);
        //?}
        modBus.addListener(NeoForgeBreedingGameTests::registerTests);
        NeoForge.EVENT_BUS.addListener(NeoForgeBreedingGameTests::onBabySpawn);
        NeoForge.EVENT_BUS.addListener(NeoForgeBreedingGameTests::onDeath);
    }

    private static List<BreedingGameTests.Case> cases() {
        List<BreedingGameTests.Case> cases = new ArrayList<>(BreedingGameTests.CASES);
        cases.addAll(FoodGameTests.CASES);
        cases.addAll(ProductGameTests.CASES);
        cases.addAll(HerdBookGameTests.CASES);
        cases.addAll(CareGameTests.CASES);
        cases.addAll(EggGameTests.CASES);
        cases.addAll(NaturalistGameTests.CASES);
        cases.addAll(VigourGameTests.CASES);
        cases.addAll(UpkeepGameTests.CASES);
        cases.addAll(FeederGameTests.CASES);
        cases.addAll(StatGameTests.CASES);
        cases.addAll(CourtshipGameTests.CASES);
        cases.addAll(FightGameTests.CASES);
        cases.addAll(AdvancementGameTests.CASES);
        cases.add(new BreedingGameTests.Case("unload_event_refreshes_the_record", 20,
                helper -> HerdBookGameTests.unloadEventRefreshesTheRecord(helper,
                        cow -> NeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(cow, helper.getLevel())))));
        cases.add(new BreedingGameTests.Case("cancelled_baby_event_leaves_no_pregnancy", 20,
                helper -> refusedBreeding(helper, Answer.CANCEL)));
        cases.add(new BreedingGameTests.Case("baby_swapped_for_nothing_leaves_no_pregnancy", 20,
                helper -> refusedBreeding(helper, Answer.NOTHING)));
        cases.add(new BreedingGameTests.Case("swapped_baby_is_the_one_carried", 20,
                NeoForgeBreedingGameTests::swappedBabyIsTheOneCarried));
        cases.add(new BreedingGameTests.Case("a_death_another_mod_cancels_is_no_dinner", 20,
                NeoForgeBreedingGameTests::aDeathAnotherModCancelsIsNoDinner));
        cases.add(new BreedingGameTests.Case("pipes_put_food_in_a_manger_but_take_none_out", 20,
                NeoForgeBreedingGameTests::pipesPutFoodInAMangerButTakeNoneOut));
        return List.copyOf(cases);
    }

    //? if >=1.21.5 {
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("default"));
        for (BreedingGameTests.Case test : CASES) {
            event.registerTest(id(test.name()), new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, id(test.name())),
                    new TestData<>(environment, BreedingGameTests.PEN, test.maxTicks(), 0, true)
            ));
        }
    }
    //?} else {
    /*private static void registerTests(RegisterGameTestsEvent event) {
        event.register(NeoForgeBreedingGameTests.class);
    }

    // One batch per test: a batch's tests stand side by side, and an animal in love would court a neighbour's.
    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return CASES.stream()
                .map(test -> new TestFunction(test.name(), Constants.MOD_ID + "." + test.name(), BreedingGameTests.PEN.toString(),
                        test.maxTicks(), 0L, true, test.body()))
                .toList();
    }
    *///?}

    private static void onBabySpawn(BabyEntitySpawnEvent event) {
        Answer answer = ANSWERS.remove(event.getParentA().getUUID());
        if (answer == null) {
            return;
        }
        switch (answer) {
            case CANCEL -> event.setCanceled(true);
            case NOTHING -> event.setChild(null);
            //? if >=1.21.2 {
            case CALF -> event.setChild(EntityTypes.COW.create(event.getParentA().level(), EntitySpawnReason.BREEDING));
            //?} else
            //case CALF -> event.setChild(EntityTypes.COW.create(event.getParentA().level()));
        }
    }

    private static void onDeath(LivingDeathEvent event) {
        if (SPARED.remove(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    // A mod that cancels LivingDeathEvent (a totem-like revive) leaves the cow alive, so killing it isn't dinner.
    private static void aDeathAnotherModCancelsIsNoDinner(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        ServerPlayer player = MockPlayers.inLevel(helper);
        SPARED.add(cow.getUUID());

        cow.die(helper.getLevel().damageSources().playerAttack(player));

        helper.assertTrue(!SPARED.contains(cow.getUUID()), "LivingDeathEvent did not fire");
        helper.assertTrue(!AdvancementGameTests.earned(player, BarnyardAdvancements.NOT_YOU_ITS_DINNER),
                "a cancelled death earned the dinner advancement");
        helper.succeed();
    }

    private static void refusedBreeding(GameTestHelper helper, Answer answer) {
        Animal mother = BreedingGameTests.female(helper, EntityTypes.PIG);
        Animal father = BreedingGameTests.male(helper, EntityTypes.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        long now = helper.getLevel().getGameTime();
        ANSWERS.put(mother.getUUID(), answer);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        helper.assertTrue(!ANSWERS.containsKey(mother.getUUID()), "BabyEntitySpawnEvent did not fire");
        helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the mother became pregnant anyway");
        helper.assertTrue(helper.getEntities(EntityTypes.EXPERIENCE_ORB).isEmpty(), "a refused breeding gave XP");
        BreedingGameTests.assertResting(helper, mother, now);
        BreedingGameTests.assertResting(helper, father, now);
        helper.succeed();
    }

    private static void swappedBabyIsTheOneCarried(GameTestHelper helper) {
        Animal mother = BreedingGameTests.female(helper, EntityTypes.PIG);
        Animal father = BreedingGameTests.male(helper, EntityTypes.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        ANSWERS.put(mother.getUUID(), Answer.CALF);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Pregnancy pregnancy = BreedingGameTests.pregnancy(helper, mother);
        //? if >=1.21.5 {
        String first = pregnancy.litter().getFirst().getStringOr("id", "");
        //?} else
        //String first = pregnancy.litter().getFirst().getString("id");
        helper.assertTrue("minecraft:cow".equals(first), "the first baby is " + first + ", not the swapped calf");
        helper.succeed();
    }

    // Other mods' pipes use the item capability rather than the container, so it must keep the block's rules: from a
    // side or from no side at all, wheat goes in, beef doesn't, and nothing comes out.
    private static void pipesPutFoodInAMangerButTakeNoneOut(GameTestHelper helper) {
        FeederBlockEntity manger = FeederGameTests.feeder(helper, BarnyardBlocks.MANGER, FeederGameTests.FEEDER);
        BlockPos at = helper.absolutePos(FeederGameTests.FEEDER);
        for (Direction side : new Direction[]{Direction.UP, Direction.NORTH, null}) {
            int[] moved = throughThePipe(helper, at, side);
            helper.assertTrue(moved[0] == 2, "a pipe from " + side + " put in " + moved[0] + " of 2 wheat");
            helper.assertTrue(moved[1] == 0, "a pipe from " + side + " put in " + moved[1] + " beef");
            helper.assertTrue(moved[2] == 0, "a pipe from " + side + " took out " + moved[2] + " wheat");
        }
        helper.assertTrue(manger.food() == 6, "the Manger holds " + manger.food() + " items, not 6 wheat");
        helper.succeed();
    }

    // Wheat put in, beef put in, and wheat taken out.
    private static int[] throughThePipe(GameTestHelper helper, BlockPos at, Direction side) {
        //? if >=1.21.9 {
        ResourceHandler<ItemResource> pipe = helper.getLevel().getCapability(Capabilities.Item.BLOCK, at, side);
        if (pipe == null) {
            throw GameTestCompat.failure(helper, "the Manger has no item capability from " + side);
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int[] moved = {
                    pipe.insert(ItemResource.of(Items.WHEAT), 2, transaction),
                    pipe.insert(ItemResource.of(Items.BEEF), 2, transaction),
                    pipe.extract(ItemResource.of(Items.WHEAT), 2, transaction)
            };
            transaction.commit();
            return moved;
        }
        //?} else {
        /*IItemHandler pipe = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, at, side);
        if (pipe == null) {
            throw GameTestCompat.failure(helper, "the Manger has no item capability from " + side);
        }
        int wheat = 2 - ItemHandlerHelper.insertItem(pipe, new ItemStack(Items.WHEAT, 2), false).getCount();
        int beef = 2 - ItemHandlerHelper.insertItem(pipe, new ItemStack(Items.BEEF, 2), false).getCount();
        int out = 0;
        for (int slot = 0; slot < pipe.getSlots(); slot++) {
            out += pipe.extractItem(slot, 2, false).getCount();
        }
        return new int[]{wheat, beef, out};
        *///?}
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
