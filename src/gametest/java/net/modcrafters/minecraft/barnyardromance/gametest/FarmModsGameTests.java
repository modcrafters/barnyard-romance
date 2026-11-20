package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import java.util.List;

// Runs in every game test run. A farm-mods run names the mods it should have in barnyard_romance.expect_mods; one whose
// jar is missing fails here, even before any test of that mod exists. A run with no mods named passes.
public final class FarmModsGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("expected_farm_mods_are_loaded", 20, FarmModsGameTests::expectedFarmModsAreLoaded)
    );

    private FarmModsGameTests() {
    }

    public static void expectedFarmModsAreLoaded(GameTestHelper helper) {
        for (String id : System.getProperty("barnyard_romance.expect_mods", "").split(",")) {
            String mod = id.trim();
            if (!mod.isEmpty() && !Services.PLATFORM.isModLoaded(mod)) {
                throw GameTestCompat.failure(helper, "this run should have " + mod + " in its mods folder, and it isn't loaded");
            }
        }
        helper.succeed();
    }
}
