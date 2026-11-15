package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.api.AnimalInfo;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardApi;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvent;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;

// The public API, as another mod sees it.
public final class ApiGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("an_empty_milking_result_is_refused", 20, ApiGameTests::anEmptyMilkingResultIsRefused),
            new BreedingGameTests.Case("an_empty_death_drop_is_refused", 20, ApiGameTests::anEmptyDeathDropIsRefused),
            new BreedingGameTests.Case("an_empty_shear_yield_is_refused", 20, ApiGameTests::anEmptyShearYieldIsRefused),
            new BreedingGameTests.Case("info_agrees_with_the_stat_card", 20, ApiGameTests::infoAgreesWithTheStatCard),
            new BreedingGameTests.Case("untracked_animals_have_no_info", 20, ApiGameTests::untrackedAnimalsHaveNoInfo)
    );

    private static final String GIVE_NOTHING = "use cancel() to give nothing";

    private ApiGameTests() {
    }

    // "Nothing" has one meaning, cancel(); an empty stack is refused rather than quietly meaning the same.
    public static void anEmptyMilkingResultIsRefused(GameTestHelper helper) {
        Milking milking = new Milking(null, null, new ItemStack(Items.MILK_BUCKET));
        assertRefusesEmpty(helper, () -> milking.setResult(ItemStack.EMPTY));
        helper.succeed();
    }

    public static void anEmptyDeathDropIsRefused(GameTestHelper helper) {
        DeathDrop drop = new DeathDrop(null, new ItemStack(Items.BEEF));
        assertRefusesEmpty(helper, () -> drop.setStack(ItemStack.EMPTY));
        helper.succeed();
    }

    public static void anEmptyShearYieldIsRefused(GameTestHelper helper) {
        ShearYield yield = new ShearYield(null, new ItemStack(Items.STRING));
        assertRefusesEmpty(helper, () -> yield.setStack(ItemStack.EMPTY));
        helper.succeed();
    }

    // What another mod would read about a familiar pregnant cow is what her stat card shows.
    public static void infoAgreesWithTheStatCard(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        long now = helper.getLevel().getGameTime();
        long clock = helper.getLevel().getOverworldClockTime();

        AnimalFacts facts = LiveFacts.of(mother, now, clock).orElseThrow();
        AnimalInfo info = BarnyardApi.animals().info(mother).orElseThrow(() -> GameTestCompat.failure(helper, "a tracked cow has no info"));

        helper.assertTrue(BarnyardApi.animals().tracked(mother), "a tracked cow isn't tracked");
        helper.assertTrue(info.sex() == traits(helper, mother).sex(), "info's sex is " + info.sex());
        helper.assertTrue(info.sizeClass().name().equals(facts.sizeLetter()), "info's size is " + info.sizeClass() + ", the card's " + facts.sizeLetter());
        helper.assertTrue(info.ageStage() == facts.stage(), "info's stage is " + info.ageStage() + ", the card's " + facts.stage());
        helper.assertTrue(info.familiarity() == facts.familiarity(), "info's familiarity is " + info.familiarity() + ", the card's " + facts.familiarity());
        helper.assertTrue(info.pregnancy().isPresent() == facts.pregnant(), "info's pregnancy is " + info.pregnancy());
        helper.assertTrue(info.pregnancy().map(p -> p.ticksUntilDue() == facts.dueTicks()).orElse(false),
                "info's due ticks are " + info.pregnancy() + ", the card's " + facts.dueTicks());
        helper.assertTrue(info.pregnancy().map(p -> p.fatherSpecies().equals("minecraft:cow")).orElse(false), "the father isn't a cow");
        helper.succeed();
    }

    public static void untrackedAnimalsHaveNoInfo(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        BarnyardConfig config = BarnyardConfig.current();
        boolean tracked;
        Optional<AnimalInfo> info;
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:cow\"]}"));
            tracked = BarnyardApi.animals().tracked(cow);
            info = BarnyardApi.animals().info(cow);
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(!tracked, "an excluded cow is tracked");
        helper.assertTrue(info.isEmpty(), "an excluded cow has info " + info);
        helper.succeed();
    }

    // Listeners on the public events are global and last as long as the server, so each test hears only its own
    // animals: one listener per event, registered once, hands each event to the handler a test added for that animal.
    public static final class Handlers<E> {
        private final Map<UUID, Consumer<E>> byAnimal = new ConcurrentHashMap<>();

        public Handlers(BarnyardEvent<E> event, Function<E, UUID> key) {
            event.register(fired -> {
                Consumer<E> handler = byAnimal.get(key.apply(fired));
                if (handler != null) {
                    handler.accept(fired);
                }
            });
        }

        public void add(UUID animal, Consumer<E> handler) {
            byAnimal.put(animal, handler);
        }

        public void remove(UUID animal) {
            byAnimal.remove(animal);
        }
    }

    private static void assertRefusesEmpty(GameTestHelper helper, Runnable setEmpty) {
        try {
            setEmpty.run();
        } catch (IllegalArgumentException refused) {
            helper.assertTrue(GIVE_NOTHING.equals(refused.getMessage()), "refused with the wrong message: " + refused.getMessage());
            return;
        }
        helper.fail("an empty stack was accepted");
    }
}
