package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.UUID;

// The nesting box in a real client: the block with a hen sitting in it, its window with eggs in the nests and her
// nest marked, the new items, and a rooster beside a hen.
public class NestingBoxClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("gamerule advance_time false");
            // Facing east and a little down, at a box three blocks ahead with its hole towards the camera.
            world.getServer().runCommand("tp @a 0.5 ~ 0.5 -90 25");
            world.getConnection().waitForClientboundPackets();
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                BlockPos at = player.blockPosition().east(3);
                level.setBlockAndUpdate(at, BarnyardBlocks.NESTING_BOX.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
                NestingBoxBlockEntity box = (NestingBoxBlockEntity) level.getBlockEntity(at);
                box.placeEgg(egg(20_000L));
                box.placeEgg(new ItemStack(Items.EGG, 3));
                box.placeEgg(egg(6_500L));
                box.placeEgg(new ItemStack(Items.BROWN_EGG, 16));
                box.enter(chicken(level, player, Sex.FEMALE, 0.85, 0.0), new ItemStack(Items.EGG), level.getGameTime());
                player.getInventory().setItem(0, new ItemStack(BarnyardBlocks.NESTING_BOX_ITEM));
                player.getInventory().setItem(1, egg(12_000L));
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(10);
            context.takeScreenshot("nesting_box_hen");
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.openMenu((NestingBoxBlockEntity) player.level().getBlockEntity(player.blockPosition().east(3)));
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(5);
            context.takeScreenshot("nesting_box_window");
            context.setScreen(() -> null);
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                level.removeBlock(player.blockPosition().east(3), false);
                // Breaking the box let its hen out and dropped its eggs; clear them for the next picture.
                level.getEntities(EntityTypes.ITEM, item -> true).forEach(Entity::discard);
                level.getEntities(EntityTypes.CHICKEN, chicken -> true).forEach(Entity::discard);
                chicken(level, player, Sex.MALE, 1.25, 0.9);
                chicken(level, player, Sex.FEMALE, 0.75, -0.9);
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(10);
            context.takeScreenshot("rooster_and_hen");
        }
    }

    // A grown chicken three blocks ahead, a little to one side, still and turned towards the camera.
    private static Animal chicken(ServerLevel level, ServerPlayer player, Sex sex, double scale, double side) {
        Animal chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.COMMAND);
        chicken.snapTo(player.getX() + 3.0, player.getY(), player.getZ() + side, 90.0F, 0.0F);
        chicken.setYHeadRot(90.0F);
        chicken.setYBodyRot(90.0F);
        chicken.setNoAi(true);
        level.addFreshEntity(chicken);
        Services.DATA.set(chicken, new AnimalTraits(sex, scale, 0.5, -1L));
        AttributeInstance size = chicken.getAttribute(Attributes.SCALE);
        if (size != null) {
            size.setBaseValue(scale);
        }
        return chicken;
    }

    private static ItemStack egg(long ticks) {
        return FertilizedEggItem.create(new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), 0.9, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", ticks, ticks));
    }
}
