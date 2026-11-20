package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import java.util.List;
import java.util.Map;

// Runs in every game test run. A farm-mods run names the mods it should have in barnyard_romance.expect_mods; one whose
// jar is missing fails here, even before any test of that mod exists. A run with no mods named passes.
public final class FarmModsGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("expected_farm_mods_are_loaded", 20, FarmModsGameTests::expectedFarmModsAreLoaded),
            new BreedingGameTests.Case("farm_mod_ladders_load", 20, FarmModsGameTests::farmModLaddersLoad)
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

    // The ladders each farm mod brings, which load only while that mod's animal is registered and tracked. The duck
    // ladder is shared, so it loads with either duck mod.
    private static final Map<String, List<String>> LADDERS = Map.of(
            "untitledduckmod", List.of("goose_10", "goose_100", "duck_10", "duck_100"),
            "friendsandfoes", List.of("moobloom_10", "moobloom_100"),
            "meadow", List.of("water_buffalo_10", "water_buffalo_100", "wooly_cow_10", "wooly_cow_100"),
            "autumnity", List.of("turkey_10", "turkey_100"),
            "environmental", List.of("yak_10", "yak_100", "duck_10", "duck_100")
    );

    public static void farmModLaddersLoad(GameTestHelper helper) {
        for (Map.Entry<String, List<String>> mod : LADDERS.entrySet()) {
            if (!Services.PLATFORM.isModLoaded(mod.getKey())) {
                continue;
            }
            for (String name : mod.getValue()) {
                int split = name.lastIndexOf('_');
                helper.assertTrue(AdvancementGameTests.ladderLoaded(helper, name.substring(0, split), Integer.parseInt(name.substring(split + 1))),
                        "ladder/" + name + " isn't loaded, and " + mod.getKey() + " is");
            }
        }
        helper.succeed();
    }
}
