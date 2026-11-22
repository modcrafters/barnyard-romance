package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import java.util.Arrays;

// Which of the farm-animal mods a game test needs is loaded. The farm-mods runs name the mods they should have in
// barnyard_romance.expect_mods, a comma list, so a jar missing from one fails its tests instead of skipping them.
public final class FarmMods {
    private FarmMods() {
    }

    // True when the test should run. A mod that's missing but expected fails the test; one that's missing and not
    // expected (a normal run) succeeds the test, so it doesn't count as skipped, and returns false.
    public static boolean present(GameTestHelper helper, String modId) {
        if (Services.PLATFORM.isModLoaded(modId)) {
            return true;
        }
        if (expected(System.getProperty("barnyard_romance.expect_mods", ""), modId)) {
            throw GameTestCompat.failure(helper, "this run should have " + modId + " in its mods folder");
        }
        helper.succeed();
        return false;
    }

    static boolean expected(String expectMods, String modId) {
        return Arrays.stream(expectMods.split(",")).map(String::trim).anyMatch(modId::equals);
    }
}
