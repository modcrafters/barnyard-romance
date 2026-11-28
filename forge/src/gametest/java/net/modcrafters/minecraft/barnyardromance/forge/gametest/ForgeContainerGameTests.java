package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsBlocks;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsItems;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.NestingBoxBlockEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Forge 1.20.1 gives every container block entity one item handler that ignores sides, and its hoppers use that
// handler before the container's own face rules. The mod's sided containers keep their rules there too.
@GameTestHolder(Constants.MOD_ID)
public final class ForgeContainerGameTests {
    private static final String PEN = Constants.MOD_ID + ":gametest/pen";
    private static final BlockPos BOX = new BlockPos(2, 2, 2);

    private ForgeContainerGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return List.of(
                new TestFunction("forge_hopper_pulls_only_plain_eggs", Constants.MOD_ID + ".forge_hopper_pulls_only_plain_eggs",
                        PEN, 20, 0L, true, ForgeContainerGameTests::hopperPullsOnlyPlainEggs),
                new TestFunction("forge_hopper_puts_nothing_in_a_nesting_box", Constants.MOD_ID + ".forge_hopper_puts_nothing_in_a_nesting_box",
                        PEN, 20, 0L, true, ForgeContainerGameTests::hopperPutsNothingInANestingBox)
        );
    }

    // As the shared hopper_pulls_only_plain_eggs: a hopper under the box takes the plain eggs, not the fertilized one.
    private static void hopperPullsOnlyPlainEggs(GameTestHelper helper) {
        helper.setBlock(BOX.below(), Blocks.HOPPER.defaultBlockState());
        NestingBoxBlockEntity box = box(helper);
        box.placeEgg(new ItemStack(Items.EGG, 2));
        box.placeEgg(FertilizedEggItem.create(eggData()));
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(BOX.below()));

        for (int i = 0; i < 4; i++) {
            HopperBlockEntity.suckInItems(helper.getLevel(), hopper);
        }

        helper.assertTrue(box.getItem(0).isEmpty(), "the hopper left plain eggs: " + box.getItem(0));
        helper.assertTrue(box.getItem(1).is(EggsItems.FERTILIZED_EGG), "the hopper took the fertilized egg");
        helper.succeed();
    }

    // A hopper above the box pushing down puts nothing in, neither a plain egg nor a fertilized one.
    private static void hopperPutsNothingInANestingBox(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper);
        BlockPos above = BOX.above();
        BlockState down = Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN);
        helper.setBlock(above, down);
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(above));
        hopper.setItem(0, new ItemStack(Items.EGG, 4));
        hopper.setItem(1, FertilizedEggItem.create(eggData()));

        for (int i = 0; i < 4; i++) {
            HopperBlockEntity.pushItemsTick(helper.getLevel(), helper.absolutePos(above), down, hopper);
            hopper.setCooldown(0);
        }

        helper.assertTrue(box.isEmpty(), "a hopper above put " + box.getItem(0) + " in the nesting box");
        helper.assertTrue(hopper.getItem(0).getCount() == 4 && hopper.getItem(1).is(EggsItems.FERTILIZED_EGG),
                "the hopper above lost its eggs: " + hopper.getItem(0) + ", " + hopper.getItem(1));
        helper.succeed();
    }

    private static NestingBoxBlockEntity box(GameTestHelper helper) {
        helper.setBlock(BOX, EggsBlocks.NESTING_BOX.defaultBlockState());
        return (NestingBoxBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(BOX));
    }

    private static EggData eggData() {
        return new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), 1.0, Growth.wild(), 0.5, 0.5,
                1.0, Growth.wild(), 0.5, "", 1_000L, 1_000L);
    }
}
