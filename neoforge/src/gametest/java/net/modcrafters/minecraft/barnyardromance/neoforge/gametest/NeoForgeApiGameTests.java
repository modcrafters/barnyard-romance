package net.modcrafters.minecraft.barnyardromance.neoforge.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.api.neoforge.MilkingEvent;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.gametest.ApiGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

// The public API's events as NeoForge mods see them, on NeoForge's own bus.
public final class NeoForgeApiGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("neo_forge_milking_event_can_cancel", 20, NeoForgeApiGameTests::neoForgeMilkingEventCanCancel),
            new BreedingGameTests.Case("neo_forge_sees_common_changes", 20, NeoForgeApiGameTests::neoForgeSeesCommonChanges),
            new BreedingGameTests.Case("animal_tame_event_fires_for_a_familiarity_tame", 20, NeoForgeApiGameTests::animalTameEventFiresForAFamiliarityTame),
            new BreedingGameTests.Case("a_neo_forge_cancel_survives_a_later_throw", 20, NeoForgeApiGameTests::aNeoForgeCancelSurvivesALaterThrow)
    );

    // As ApiGameTests.Handlers: one listener each, handing events to the test that owns the animal.
    private static final Map<UUID, Consumer<MilkingEvent>> MILKINGS = new ConcurrentHashMap<>();
    // Heard after MILKINGS, at a lower priority, cancelled or not.
    private static final Map<UUID, Consumer<MilkingEvent>> LATE_MILKINGS = new ConcurrentHashMap<>();
    private static final Map<UUID, Consumer<AnimalTameEvent>> TAMES = new ConcurrentHashMap<>();

    private NeoForgeApiGameTests() {
    }

    static void listen() {
        NeoForge.EVENT_BUS.addListener((MilkingEvent event) -> dispatch(MILKINGS, event.apiEvent().animal(), event));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, MilkingEvent.class, event -> dispatch(LATE_MILKINGS, event.apiEvent().animal(), event));
        NeoForge.EVENT_BUS.addListener((AnimalTameEvent event) -> dispatch(TAMES, event.getAnimal(), event));
    }

    private static <E> void dispatch(Map<UUID, Consumer<E>> handlers, Animal animal, E event) {
        Consumer<E> handler = handlers.get(animal.getUUID());
        if (handler != null) {
            handler.accept(event);
        }
    }

    public static void neoForgeMilkingEventCanCancel(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.put(cow.getUUID(), event -> event.setCanceled(true));
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cancelled NeoForge milking left " + player.getMainHandItem());
        helper.succeed();
    }

    // NeoForge stops a post at a listener that throws, but a cancel from before it still counts.
    public static void aNeoForgeCancelSurvivesALaterThrow(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.put(cow.getUUID(), event -> event.setCanceled(true));
        LATE_MILKINGS.put(cow.getUUID(), event -> {
            throw new IllegalStateException("a broken listener, on purpose");
        });
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
            LATE_MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cancelled NeoForge milking left " + player.getMainHandItem());
        helper.succeed();
    }

    // A common listener's change is what the NeoForge listener sees, as they run after.
    public static void neoForgeSeesCommonChanges(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        AtomicReference<Item> seen = new AtomicReference<>();
        ApiGameTests.MILKINGS.add(cow.getUUID(), milking -> milking.setResult(new ItemStack(Items.HONEY_BOTTLE)));
        MILKINGS.put(cow.getUUID(), event -> seen.set(event.apiEvent().result().getItem()));
        try {
            GameTestCompat.interact(cow, FoodGameTests.holding(helper, Items.BUCKET));
        } finally {
            ApiGameTests.MILKINGS.remove(cow.getUUID());
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(seen.get() == Items.HONEY_BOTTLE, "the NeoForge listener saw " + seen.get());
        helper.succeed();
    }

    public static void animalTameEventFiresForAFamiliarityTame(GameTestHelper helper) {
        TamableAnimal wolf = (TamableAnimal) CareGameTests.withFamiliarity(helper, BreedingGameTests.female(helper, EntityTypes.WOLF), Familiarity.threshold() - 0.05);
        TAMES.put(wolf.getUUID(), event -> event.setCanceled(true));
        try {
            ApiGameTests.feed(helper, wolf, Items.BEEF);
        } finally {
            TAMES.remove(wolf.getUUID());
        }

        helper.assertTrue(!wolf.isTame(), "a cancelled AnimalTameEvent tamed the wolf");
        helper.succeed();
    }
}
