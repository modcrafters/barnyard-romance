package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.DataLoadGameTests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// The shared game tests (src/gametest) that run on Forge 1.20.1, each made into a test function as NeoForge 1.21.1
// does: one batch per test, in the shared pen.
@GameTestHolder(Constants.MOD_ID)
public final class ForgeSharedGameTests {
    private static final String PEN = Constants.MOD_ID + ":gametest/pen";

    private static final List<BreedingGameTests.Case> CASES = cases();

    private ForgeSharedGameTests() {
    }

    private static List<BreedingGameTests.Case> cases() {
        List<BreedingGameTests.Case> cases = new ArrayList<>(DataLoadGameTests.CASES);
        return List.copyOf(cases);
    }

    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return CASES.stream()
                .map(test -> new TestFunction(test.name(), Constants.MOD_ID + "." + test.name(), PEN, test.maxTicks(), 0L, true, test.body()))
                .toList();
    }
}
