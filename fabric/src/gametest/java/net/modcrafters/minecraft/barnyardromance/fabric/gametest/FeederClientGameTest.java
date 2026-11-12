package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.client.FeederScreen;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.feed.FeederKind;
import net.modcrafters.minecraft.barnyardromance.feed.FeedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// The feeding blocks in a real client: the five placeholder blocks in a row, and a stocked Manger's window.
public class FeederClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("gamerule advance_time false");
            world.getServer().runCommand("tp @a 0.5 ~ 0.5 -90 25");
            world.getConnection().waitForClientboundPackets();
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                BlockPos ahead = player.blockPosition().east(3);
                for (FeederKind kind : FeederKind.values()) {
                    level.setBlockAndUpdate(ahead.south(kind.ordinal() - 2), FeedBlocks.block(kind).defaultBlockState());
                }
                FeederBlockEntity manger = (FeederBlockEntity) level.getBlockEntity(ahead.south(FeederKind.MANGER.ordinal() - 2));
                manger.setItem(0, new ItemStack(Items.WHEAT, 64));
                manger.setItem(1, new ItemStack(Items.HAY_BLOCK, 12));
                player.getInventory().setItem(0, new ItemStack(FeedBlocks.MANGER_ITEM));
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(5);
            context.takeScreenshot("feeding_blocks");
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                BlockPos manger = player.blockPosition().east(3).south(FeederKind.MANGER.ordinal() - 2);
                player.openMenu((FeederBlockEntity) player.level().getBlockEntity(manger));
            });
            world.getConnection().waitForClientboundPackets();
            context.waitFor(client -> client.gui.screen() instanceof FeederScreen);
            context.waitTicks(5);
            context.takeScreenshot("manger_window");
            context.setScreen(() -> null);
        }
    }
}
