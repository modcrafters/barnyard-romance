package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.UUID;

// The nesting box in a real client: the block in the world, its window with eggs in the nests, and the new items.
public class NestingBoxClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("gamerule advance_time false");
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                Direction facing = player.getDirection();
                BlockPos at = player.blockPosition().relative(facing, 2);
                server.overworld().setBlockAndUpdate(at, BarnyardBlocks.NESTING_BOX.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing.getOpposite()));
                NestingBoxBlockEntity box = (NestingBoxBlockEntity) server.overworld().getBlockEntity(at);
                box.placeEgg(egg(20_000L));
                box.placeEgg(new ItemStack(Items.EGG, 3));
                box.placeEgg(egg(6_500L));
                box.placeEgg(new ItemStack(Items.BROWN_EGG, 16));
                player.getInventory().setItem(0, new ItemStack(BarnyardBlocks.NESTING_BOX_ITEM));
                player.getInventory().setItem(1, egg(12_000L));
            });
            context.runOnClient(client -> client.player.setXRot(30.0F));
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(10);
            context.takeScreenshot("nesting_box_block");
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                BlockPos at = player.blockPosition().relative(player.getDirection(), 2);
                player.openMenu((NestingBoxBlockEntity) server.overworld().getBlockEntity(at));
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(5);
            context.takeScreenshot("nesting_box_window");
        }
    }

    private static ItemStack egg(long ticks) {
        return FertilizedEggItem.create(new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), 0.9, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", ticks, ticks, -1L));
    }
}
