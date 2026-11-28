package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.forge.MilkingEvent;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

// The public API's events as Forge mods see them, on Forge's own bus: what NeoForgeApiGameTests checks on NeoForge.
@GameTestHolder(Constants.MOD_ID)
public final class ForgeApiGameTests {
    private static final String PEN = Constants.MOD_ID + ":gametest/pen";

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "forge_milking_event_can_cancel", ForgeApiGameTests::forgeMilkingEventCanCancel,
            "forge_sees_common_changes", ForgeApiGameTests::forgeSeesCommonChanges,
            "animal_tame_event_fires_for_a_familiarity_tame", ForgeApiGameTests::animalTameEventFiresForAFamiliarityTame,
            "a_forge_cancel_survives_a_later_throw", ForgeApiGameTests::aForgeCancelSurvivesALaterThrow
    );

    // One listener each, handing events to the test that owns the animal.
    private static final Map<UUID, Consumer<MilkingEvent>> MILKINGS = new ConcurrentHashMap<>();
    // Heard after MILKINGS, at a lower priority, cancelled or not.
    private static final Map<UUID, Consumer<MilkingEvent>> LATE_MILKINGS = new ConcurrentHashMap<>();
    private static final Map<UUID, Consumer<AnimalTameEvent>> TAMES = new ConcurrentHashMap<>();
    // Listeners on the API's own events, which run before the Forge ones.
    private static final Map<UUID, Consumer<Milking>> COMMON_MILKINGS = new ConcurrentHashMap<>();

    private ForgeApiGameTests() {
    }

    static void listen() {
        MinecraftForge.EVENT_BUS.addListener((MilkingEvent event) -> dispatch(MILKINGS, event.apiEvent().animal(), event));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, true, (MilkingEvent event) -> dispatch(LATE_MILKINGS, event.apiEvent().animal(), event));
        MinecraftForge.EVENT_BUS.addListener((AnimalTameEvent event) -> dispatch(TAMES, event.getAnimal(), event));
        BarnyardEvents.MILKING.register(milking -> dispatch(COMMON_MILKINGS, milking.animal(), milking));
    }

    private static <E> void dispatch(Map<UUID, Consumer<E>> handlers, Animal animal, E event) {
        Consumer<E> handler = handlers.get(animal.getUUID());
        if (handler != null) {
            handler.accept(event);
        }
    }

    // One batch per test, as the other Forge game tests run.
    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return TESTS.entrySet().stream()
                .map(test -> new TestFunction(test.getKey(), Constants.MOD_ID + "." + test.getKey(), PEN, 20, 0L, true, test.getValue()))
                .toList();
    }

    private static Animal female(GameTestHelper helper, EntityType<? extends Animal> type) {
        Animal animal = helper.spawn(type, 3, 1, 4);
        Services.DATA.set(animal, AnimalTraits.of(Sex.FEMALE, 1.0).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)));
        return animal;
    }

    // A cow that gives milk: familiar, and in lactation.
    private static Animal milkingCow(GameTestHelper helper) {
        Animal animal = female(helper, EntityType.COW);
        SizeSettings size = BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal));
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(animal, AnimalTraits.of(Sex.FEMALE, size.femaleScale()).with(CARE, FamiliarityState.NONE.fed(1.0, -1L))
                .with(LACTATION, Lactation.NONE.afterBirth(now, now + 24_000L, 72_000L)));
        return animal;
    }

    private static Player holding(GameTestHelper helper, Item item) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        return player;
    }

    private static void forgeMilkingEventCanCancel(GameTestHelper helper) {
        Animal cow = milkingCow(helper);
        Player player = holding(helper, Items.BUCKET);
        MILKINGS.put(cow.getUUID(), event -> event.setCanceled(true));
        try {
            cow.interact(player, InteractionHand.MAIN_HAND);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cancelled Forge milking left " + player.getMainHandItem());
        helper.succeed();
    }

    // Forge stops a post at a listener that throws, but a cancel from before it still counts.
    private static void aForgeCancelSurvivesALaterThrow(GameTestHelper helper) {
        Animal cow = milkingCow(helper);
        Player player = holding(helper, Items.BUCKET);
        MILKINGS.put(cow.getUUID(), event -> event.setCanceled(true));
        LATE_MILKINGS.put(cow.getUUID(), event -> {
            throw new IllegalStateException("a broken listener, on purpose");
        });
        try {
            cow.interact(player, InteractionHand.MAIN_HAND);
        } finally {
            MILKINGS.remove(cow.getUUID());
            LATE_MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cancelled Forge milking left " + player.getMainHandItem());
        helper.succeed();
    }

    // A common listener's change is what the Forge listener sees, as they run after.
    private static void forgeSeesCommonChanges(GameTestHelper helper) {
        Animal cow = milkingCow(helper);
        AtomicReference<Item> seen = new AtomicReference<>();
        COMMON_MILKINGS.put(cow.getUUID(), milking -> milking.setResult(new ItemStack(Items.HONEY_BOTTLE)));
        MILKINGS.put(cow.getUUID(), event -> seen.set(event.apiEvent().result().getItem()));
        try {
            cow.interact(holding(helper, Items.BUCKET), InteractionHand.MAIN_HAND);
        } finally {
            COMMON_MILKINGS.remove(cow.getUUID());
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(seen.get() == Items.HONEY_BOTTLE, "the Forge listener saw " + seen.get());
        helper.succeed();
    }

    // Cancelling Forge's AnimalTameEvent stops a familiarity tame.
    private static void animalTameEventFiresForAFamiliarityTame(GameTestHelper helper) {
        TamableAnimal wolf = (TamableAnimal) female(helper, EntityType.WOLF);
        Services.DATA.set(wolf, Services.DATA.get(wolf).orElseThrow()
                .with(CARE, FamiliarityState.NONE.fed(Familiarity.threshold() - 0.05, -1L)));
        TAMES.put(wolf.getUUID(), event -> event.setCanceled(true));
        try {
            Player player = holding(helper, Items.BEEF);
            player.setShiftKeyDown(true);
            FamiliarityActions.interact(wolf, player, InteractionHand.MAIN_HAND);
        } finally {
            TAMES.remove(wolf.getUUID());
        }

        helper.assertTrue(!wolf.isTame(), "a cancelled AnimalTameEvent tamed the wolf");
        helper.succeed();
    }
}
