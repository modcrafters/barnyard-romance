package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.mixin.GameTestHelperAccessor;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

// A mock player in the level that leaves (as if it logged off) once its test is over. Clearing a test's area spares
// players, and every test here stands on the same spot by the world spawn, where vanilla puts a new mock player: one
// left behind stood in a later test's pen, and a cow walking into it pushed against it for good.
public final class MockPlayers {
    private MockPlayers() {
    }

    public static ServerPlayer inLevel(GameTestHelper helper) {
        //? if >=1.20.5 {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        //?} else
        //ServerPlayer player = GameTestCompat.mockServerPlayer(helper);
        ((GameTestHelperAccessor) helper).barnyardromance$testInfo().addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo info) {
            }

            //? if >=1.20.5 {
            @Override
            public void testPassed(GameTestInfo info, GameTestRunner runner) {
                leave(player);
            }

            @Override
            public void testFailed(GameTestInfo info, GameTestRunner runner) {
                leave(player);
            }

            @Override
            public void testAddedForRerun(GameTestInfo original, GameTestInfo copy, GameTestRunner runner) {
            }
            //?} else {
            /*@Override
            public void testPassed(GameTestInfo info) {
                leave(player);
            }

            @Override
            public void testFailed(GameTestInfo info) {
                leave(player);
            }
            *///?}
        });
        return player;
    }

    // A test may have sent it off already.
    private static void leave(ServerPlayer player) {
        PlayerList players = player.level().getServer().getPlayerList();
        if (players.getPlayer(player.getUUID()) == player) {
            players.remove(player);
        }
    }
}
