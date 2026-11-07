package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.feed.FeederMenu;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// In-game checks for the feeding blocks' storage: what each takes, by hand and by hopper, and that nothing takes
// the food back out. The Manger's grazers eat seeds, so beef is the plain food it turns away.
public final class FeederGameTests {
    public static final BlockPos FEEDER = new BlockPos(2, 2, 2);
    // A hopper moves an item every 8 ticks; this is long enough for eight.
    static final int HOPPER_TICKS = 100;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("feeder_takes_wheat_but_not_beef", 20, FeederGameTests::feederTakesWheatButNotBeef),
            new BreedingGameTests.Case("feeder_refuses_superfoods", 20, FeederGameTests::feederRefusesSuperfoods),
            new BreedingGameTests.Case("bird_feeder_takes_seeds", 20, FeederGameTests::birdFeederTakesSeeds),
            new BreedingGameTests.Case("a_hopper_fills_a_manger_and_keeps_what_it_refuses", HOPPER_TICKS + 40,
                    FeederGameTests::aHopperFillsAMangerAndKeepsWhatItRefuses),
            new BreedingGameTests.Case("a_hopper_cannot_empty_a_manger", HOPPER_TICKS + 40, FeederGameTests::aHopperCannotEmptyAManger),
            new BreedingGameTests.Case("breaking_a_manger_drops_its_food", 20, FeederGameTests::breakingAMangerDropsItsFood),
            new BreedingGameTests.Case("a_meal_comes_from_the_first_full_slot", 20, FeederGameTests::aMealComesFromTheFirstFullSlot)
    );

    private FeederGameTests() {
    }

    // By hand too: the window's slots ask the block.
    public static void feederTakesWheatButNotBeef(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        Player player = holding(helper, Items.BEEF);
        FeederMenu menu = new FeederMenu(1, player.getInventory(), manger);

        helper.assertTrue(manger.canPlaceItem(0, new ItemStack(Items.WHEAT)), "the Manger refuses wheat");
        helper.assertTrue(!manger.canPlaceItem(0, new ItemStack(Items.BEEF)), "the Manger takes beef");
        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.WHEAT)), "the Manger's window refuses wheat");
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(Items.BEEF)), "the Manger's window takes beef");
        helper.succeed();
    }

    public static void feederRefusesSuperfoods(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);

        helper.assertTrue(!manger.canPlaceItem(0, new ItemStack(BarnyardItems.STUD_MASH)), "the Manger takes Stud Mash");
        helper.assertTrue(!manger.canPlaceItem(0, new ItemStack(BarnyardItems.BREEDERS_MASH)), "the Manger takes Breeder's Mash");
        helper.succeed();
    }

    public static void birdFeederTakesSeeds(GameTestHelper helper) {
        FeederBlockEntity birds = feeder(helper, BarnyardBlocks.BIRD_FEEDER, FEEDER);

        helper.assertTrue(birds.canPlaceItem(0, new ItemStack(Items.WHEAT_SEEDS)), "the Bird Feeder refuses seeds");
        helper.assertTrue(!birds.canPlaceItem(0, new ItemStack(Items.BEEF)), "the Bird Feeder takes beef");
        helper.succeed();
    }

    // The refused beef sits first in the hopper, so the hopper has to pass it over to move the wheat.
    public static void aHopperFillsAMangerAndKeepsWhatItRefuses(GameTestHelper helper) {
        BlockPos below = FEEDER.below();
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, below);
        helper.setBlock(FEEDER, Blocks.HOPPER.defaultBlockState());
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(FEEDER));
        hopper.setItem(0, new ItemStack(Items.BEEF, 4));
        hopper.setItem(1, new ItemStack(Items.WHEAT, 4));

        helper.runAfterDelay(HOPPER_TICKS, () -> {
            helper.assertTrue(manger.food() == 4 && count(manger, Items.WHEAT) == 4, "the Manger holds " + contents(manger));
            helper.assertTrue(count(hopper, Items.BEEF) == 4 && count(hopper, Items.WHEAT) == 0, "the hopper holds " + contents(hopper));
            helper.succeed();
        });
    }

    public static void aHopperCannotEmptyAManger(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        manger.setItem(0, new ItemStack(Items.WHEAT, 4));
        helper.setBlock(FEEDER.below(), Blocks.HOPPER.defaultBlockState());
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(FEEDER.below()));

        helper.runAfterDelay(HOPPER_TICKS, () -> {
            helper.assertTrue(count(manger, Items.WHEAT) == 4, "the Manger holds " + contents(manger));
            helper.assertTrue(hopper.isEmpty(), "the hopper took " + contents(hopper));
            helper.succeed();
        });
    }

    public static void breakingAMangerDropsItsFood(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        manger.setItem(0, new ItemStack(Items.WHEAT, 5));
        manger.setItem(4, new ItemStack(Items.HAY_BLOCK, 2));

        // As a player breaks it, so its loot table drops the block too.
        helper.getLevel().destroyBlock(helper.absolutePos(FEEDER), true);

        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(FEEDER)).inflate(2.0));
        helper.assertTrue(dropped(dropped, Items.WHEAT) == 5, "breaking the Manger dropped " + dropped(dropped, Items.WHEAT) + " wheat");
        helper.assertTrue(dropped(dropped, Items.HAY_BLOCK) == 2, "breaking the Manger dropped " + dropped(dropped, Items.HAY_BLOCK) + " hay");
        helper.assertTrue(dropped(dropped, BarnyardBlocks.MANGER_ITEM) == 1, "breaking the Manger didn't drop the Manger");
        helper.succeed();
    }

    public static void aMealComesFromTheFirstFullSlot(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        manger.setItem(2, new ItemStack(Items.WHEAT, 1));
        manger.setItem(5, new ItemStack(Items.CARROT, 3));

        boolean first = manger.takeOne(helper.getLevel());
        boolean second = manger.takeOne(helper.getLevel());
        int left = manger.food();
        manger.takeOne(helper.getLevel());
        manger.takeOne(helper.getLevel());
        boolean empty = manger.takeOne(helper.getLevel());

        helper.assertTrue(first && second, "a stocked Manger gave no meal");
        helper.assertTrue(left == 2 && manger.getItem(2).isEmpty(), "after two meals the Manger holds " + contents(manger));
        helper.assertTrue(!empty && manger.food() == 0, "an empty Manger gave a meal");
        helper.succeed();
    }

    public static FeederBlockEntity feeder(GameTestHelper helper, Block block, BlockPos pos) {
        helper.setBlock(pos, block.defaultBlockState());
        return (FeederBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static int count(Container container, Item item) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).is(item)) {
                count += container.getItem(slot).getCount();
            }
        }
        return count;
    }

    private static int dropped(List<ItemEntity> dropped, Item item) {
        return dropped.stream().filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static String contents(Container container) {
        StringBuilder text = new StringBuilder("[");
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                text.append(text.length() > 1 ? ", " : "").append(container.getItem(slot));
            }
        }
        return text.append("]").toString();
    }
}
