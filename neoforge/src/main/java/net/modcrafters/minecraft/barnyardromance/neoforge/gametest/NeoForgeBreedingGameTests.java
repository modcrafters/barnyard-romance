package net.modcrafters.minecraft.barnyardromance.neoforge.gametest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    }

    private static List<BreedingGameTests.Case> cases() {
        List<BreedingGameTests.Case> cases = new ArrayList<>(BreedingGameTests.CASES);
        cases.addAll(FoodGameTests.CASES);
        cases.addAll(ProductGameTests.CASES);
        cases.add(new BreedingGameTests.Case("cancelled_baby_event_leaves_no_pregnancy", 20,
                helper -> refusedBreeding(helper, Answer.CANCEL)));
        cases.add(new BreedingGameTests.Case("baby_swapped_for_nothing_leaves_no_pregnancy", 20,
                helper -> refusedBreeding(helper, Answer.NOTHING)));
        cases.add(new BreedingGameTests.Case("swapped_baby_is_the_one_carried", 20,
                NeoForgeBreedingGameTests::swappedBabyIsTheOneCarried));
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

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
