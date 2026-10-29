package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.HerdBookGameTests;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

// Fabric runs these through the fabric-gametest entrypoint; the bodies are shared in common.
public class FabricHerdBookGameTests {
    static final String PEN = "barnyard_romance:gametest/pen";

    @GameTest(structure = PEN, maxTicks = 20)
    public void unloadEventRefreshesTheRecord(GameTestHelper helper) {
        HerdBookGameTests.unloadEventRefreshesTheRecord(helper, cow -> ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(cow, helper.getLevel()));
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wildAnimalsCannotJoinABook(GameTestHelper helper) {
        HerdBookGameTests.wildAnimalsCannotJoinABook(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void familiarAnimalsJoinOnce(GameTestHelper helper) {
        HerdBookGameTests.familiarAnimalsJoinOnce(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void tamedCatsJoinWithoutSitting(GameTestHelper helper) {
        HerdBookGameTests.tamedCatsJoinWithoutSitting(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void newbornsJoinTheirMothersBooks(GameTestHelper helper) {
        HerdBookGameTests.newbornsJoinTheirMothersBooks(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void newbornsJoinFrozen(GameTestHelper helper) {
        HerdBookGameTests.newbornsJoinFrozen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aDeathMarksTheRecord(GameTestHelper helper) {
        HerdBookGameTests.aDeathMarksTheRecord(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aDeadMotherStaysForHerCalf(GameTestHelper helper) {
        HerdBookGameTests.aDeadMotherStaysForHerCalf(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void unloadingRefreshesTheRecord(GameTestHelper helper) {
        HerdBookGameTests.unloadingRefreshesTheRecord(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void recordsRefreshWhileLoaded(GameTestHelper helper) {
        HerdBookGameTests.recordsRefreshWhileLoaded(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void frozenRecordsKeepTheirValues(GameTestHelper helper) {
        HerdBookGameTests.frozenRecordsKeepTheirValues(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void herdBooksSurviveSaveAndLoad(GameTestHelper helper) {
        HerdBookGameTests.herdBooksSurviveSaveAndLoad(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void showSendsEveryLivingMember(GameTestHelper helper) {
        HerdBookGameTests.showSendsEveryLivingMember(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void unloadedStatusIsProjected(GameTestHelper helper) {
        HerdBookGameTests.unloadedStatusIsProjected(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void removalNeedsTheBookInHand(GameTestHelper helper) {
        HerdBookGameTests.removalNeedsTheBookInHand(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aBigBookFitsThePayloadLimit(GameTestHelper helper) {
        HerdBookGameTests.aBigBookFitsThePayloadLimit(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void showBookSurvivesTheNetworkCodec(GameTestHelper helper) {
        HerdBookGameTests.showBookSurvivesTheNetworkCodec(helper);
    }
}
