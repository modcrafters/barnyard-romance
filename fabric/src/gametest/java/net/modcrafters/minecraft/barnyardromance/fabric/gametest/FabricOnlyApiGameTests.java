package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.fabric.BarnyardFabricEvents;
import net.modcrafters.minecraft.barnyardromance.gametest.ApiGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

// The public API's events as Fabric mods see them, as Fabric API events.
public class FabricOnlyApiGameTests {
    // As ApiGameTests.Handlers: one callback, handing events to the test that owns the animal.
    private static final Map<UUID, Consumer<Milking>> MILKINGS = new ConcurrentHashMap<>();

    static {
        BarnyardFabricEvents.MILKING.register(milking -> {
            Consumer<Milking> handler = MILKINGS.get(milking.animal().getUUID());
            if (handler != null) {
                handler.accept(milking);
            }
        });
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void fabricMilkingCallbackCanCancel(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.put(cow.getUUID(), Milking::cancel);
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cancelled Fabric milking left " + player.getMainHandItem());
        helper.succeed();
    }

    // A common listener's change is what the Fabric callback sees, as it runs after.
    @GameTest(structure = PEN, maxTicks = 20)
    public void fabricSeesCommonChanges(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        AtomicReference<Item> seen = new AtomicReference<>();
        ApiGameTests.MILKINGS.add(cow.getUUID(), milking -> milking.setResult(new ItemStack(Items.HONEY_BOTTLE)));
        MILKINGS.put(cow.getUUID(), milking -> seen.set(milking.result().getItem()));
        try {
            GameTestCompat.interact(cow, FoodGameTests.holding(helper, Items.BUCKET));
        } finally {
            ApiGameTests.MILKINGS.remove(cow.getUUID());
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(seen.get() == Items.HONEY_BOTTLE, "the Fabric callback saw " + seen.get());
        helper.succeed();
    }
}
