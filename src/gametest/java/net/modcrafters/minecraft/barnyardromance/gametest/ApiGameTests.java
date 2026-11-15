package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;

import java.util.List;

// The public API, as another mod sees it.
public final class ApiGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("an_empty_milking_result_is_refused", 20, ApiGameTests::anEmptyMilkingResultIsRefused),
            new BreedingGameTests.Case("an_empty_death_drop_is_refused", 20, ApiGameTests::anEmptyDeathDropIsRefused),
            new BreedingGameTests.Case("an_empty_shear_yield_is_refused", 20, ApiGameTests::anEmptyShearYieldIsRefused)
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
