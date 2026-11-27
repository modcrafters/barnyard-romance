package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.feed.FeedBlocks;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.gametest.AdvancementGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.ApiGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.ConversionGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.DataLoadGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FeederGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FightGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.gametest.HerdBookGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.MockPlayers;
import net.modcrafters.minecraft.barnyardromance.gametest.PailGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.StatGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.TrackingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.UpkeepGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.VigourGameTests;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// The shared game tests (src/gametest) that run on Forge 1.20.1, each made into a test function as NeoForge 1.21.1
// does: one batch per test, in the shared pen. A batch's tests stand side by side, and an animal in love would court a
// neighbour's. The compat test classes (Naturalist, the farm mods) run only in the compat runs, as on NeoForge.
// Below them, the tests NeoForge's wrapper adds for its own events, on Forge's events of the same name.
@GameTestHolder(Constants.MOD_ID)
public final class ForgeSharedGameTests {
    private static final String PEN = Constants.MOD_ID + ":gametest/pen";

    private enum Answer {
        CANCEL,
        NOTHING,
        CALF
    }

    // Only mothers a test put here are touched, so the listener cannot affect anything else.
    private static final Map<UUID, Answer> ANSWERS = new ConcurrentHashMap<>();
    // Animals whose death a test has another mod cancel.
    private static final Set<UUID> SPARED = ConcurrentHashMap.newKeySet();
    // Sheep whose shearing another mod adds a string to (see mixin/SheepExtraDropMixin).
    private static final Set<UUID> STRING_ON_SHEARING = ConcurrentHashMap.newKeySet();

    private static final List<BreedingGameTests.Case> CASES = cases();

    private ForgeSharedGameTests() {
    }

    public static void listen() {
        MinecraftForge.EVENT_BUS.addListener(ForgeSharedGameTests::onBabySpawn);
        MinecraftForge.EVENT_BUS.addListener(ForgeSharedGameTests::onDeath);
    }

    private static List<BreedingGameTests.Case> cases() {
        List<BreedingGameTests.Case> cases = new ArrayList<>(BreedingGameTests.CASES);
        cases.addAll(FoodGameTests.CASES);
        cases.addAll(ProductGameTests.CASES);
        cases.addAll(HerdBookGameTests.CASES);
        cases.addAll(CareGameTests.CASES);
        cases.addAll(EggGameTests.CASES);
        cases.addAll(TrackingGameTests.CASES);
        cases.addAll(ConversionGameTests.CASES);
        cases.addAll(VigourGameTests.CASES);
        cases.addAll(UpkeepGameTests.CASES);
        cases.addAll(FeederGameTests.CASES);
        cases.addAll(StatGameTests.CASES);
        cases.addAll(PailGameTests.CASES);
        cases.addAll(CourtshipGameTests.CASES);
        cases.addAll(FightGameTests.CASES);
        cases.addAll(AdvancementGameTests.CASES);
        cases.addAll(ApiGameTests.CASES);
        cases.addAll(DataLoadGameTests.CASES);
        cases.addAll(ForgePailFluidTests.CASES);
        cases.add(new BreedingGameTests.Case("unload_event_refreshes_the_record", 20,
                helper -> HerdBookGameTests.unloadEventRefreshesTheRecord(helper,
                        cow -> MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(cow, helper.getLevel())))));
        cases.add(new BreedingGameTests.Case("cancelled_baby_event_leaves_no_pregnancy", 20,
                helper -> refusedBreeding(helper, Answer.CANCEL)));
        cases.add(new BreedingGameTests.Case("baby_swapped_for_nothing_leaves_no_pregnancy", 20,
                helper -> refusedBreeding(helper, Answer.NOTHING)));
        cases.add(new BreedingGameTests.Case("swapped_baby_is_the_one_carried", 20,
                ForgeSharedGameTests::swappedBabyIsTheOneCarried));
        cases.add(new BreedingGameTests.Case("a_death_another_mod_cancels_is_no_dinner", 20,
                ForgeSharedGameTests::aDeathAnotherModCancelsIsNoDinner));
        cases.add(new BreedingGameTests.Case("pipes_put_food_in_a_manger_but_take_none_out", 20,
                ForgeSharedGameTests::pipesPutFoodInAMangerButTakeNoneOut));
        cases.add(new BreedingGameTests.Case("forge_shears_size_only_the_wool", 20,
                ForgeSharedGameTests::forgeShearsSizeOnlyTheWool));
        return List.copyOf(cases);
    }

    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return CASES.stream()
                .map(test -> new TestFunction(test.name(), Constants.MOD_ID + "." + test.name(), PEN, test.maxTicks(), 0L, true, test.body()))
                .toList();
    }

    // What another mod adds to the drops shears take off this sheep: a string for a sheep a test named, else nothing.
    public static ItemStack extraShearDrop(Sheep sheep) {
        return STRING_ON_SHEARING.contains(sheep.getUUID()) ? new ItemStack(Items.STRING) : ItemStack.EMPTY;
    }

    private static void onBabySpawn(BabyEntitySpawnEvent event) {
        Answer answer = ANSWERS.remove(event.getParentA().getUUID());
        if (answer == null) {
            return;
        }
        switch (answer) {
            case CANCEL -> event.setCanceled(true);
            case NOTHING -> event.setChild(null);
            case CALF -> event.setChild(EntityType.COW.create(event.getParentA().level()));
        }
    }

    private static void onDeath(LivingDeathEvent event) {
        if (SPARED.remove(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    // A mod that cancels LivingDeathEvent (a totem-like revive) leaves the cow alive, so killing it isn't dinner.
    private static void aDeathAnotherModCancelsIsNoDinner(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityType.COW);
        ServerPlayer player = MockPlayers.inLevel(helper);
        SPARED.add(cow.getUUID());

        cow.die(helper.getLevel().damageSources().playerAttack(player));

        helper.assertTrue(!SPARED.contains(cow.getUUID()), "LivingDeathEvent did not fire");
        helper.assertTrue(!AdvancementGameTests.earned(player, BarnyardAdvancements.NOT_YOU_ITS_DINNER),
                "a cancelled death earned the dinner advancement");
        helper.succeed();
    }

    private static void refusedBreeding(GameTestHelper helper, Answer answer) {
        Animal mother = BreedingGameTests.female(helper, EntityType.PIG);
        Animal father = BreedingGameTests.male(helper, EntityType.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        long now = helper.getLevel().getGameTime();
        ANSWERS.put(mother.getUUID(), answer);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        helper.assertTrue(!ANSWERS.containsKey(mother.getUUID()), "BabyEntitySpawnEvent did not fire");
        helper.assertTrue(Services.DATA.get(mother, PregnancyFeature.PREGNANCY).isEmpty(), "the mother became pregnant anyway");
        helper.assertTrue(GameTestCompat.entities(helper, EntityType.EXPERIENCE_ORB).isEmpty(), "a refused breeding gave XP");
        BreedingGameTests.assertResting(helper, mother, now);
        BreedingGameTests.assertResting(helper, father, now);
        helper.succeed();
    }

    private static void swappedBabyIsTheOneCarried(GameTestHelper helper) {
        Animal mother = BreedingGameTests.female(helper, EntityType.PIG);
        Animal father = BreedingGameTests.male(helper, EntityType.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        ANSWERS.put(mother.getUUID(), Answer.CALF);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Pregnancy pregnancy = BreedingGameTests.pregnancy(helper, mother);
        String first = pregnancy.litter().get(0).getString("id");
        helper.assertTrue("minecraft:cow".equals(first), "the first baby is " + first + ", not the swapped calf");
        helper.succeed();
    }

    // Shears in a player's hand take their drops from Forge's onSheared, and the mod sizes the wool in that list. Another
    // mod's drop in the same list stays what it is: the wool is sized, and the string is still one string.
    private static void forgeShearsSizeOnlyTheWool(GameTestHelper helper) {
        Sheep sheep = (Sheep) BreedingGameTests.female(helper, EntityType.SHEEP);
        STRING_ON_SHEARING.add(sheep.getUUID());
        List<ItemStack> drops;
        try {
            drops = sheep.onSheared(null, new ItemStack(Items.SHEARS), helper.getLevel(), sheep.blockPosition(), 0);
        } finally {
            STRING_ON_SHEARING.remove(sheep.getUUID());
        }

        int strings = drops.stream().filter(drop -> drop.is(Items.STRING)).mapToInt(ItemStack::getCount).sum();
        int wool = drops.stream().filter(drop -> drop.is(ItemTags.WOOL)).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(strings == 1, "the shears dropped " + strings + " of another mod's string, not 1: " + drops);
        helper.assertTrue(wool > 0, "the shears dropped no wool: " + drops);
        helper.assertTrue(drops.stream().allMatch(drop -> drop.is(Items.STRING) || drop.is(ItemTags.WOOL)),
                "the shears dropped something else: " + drops);
        helper.succeed();
    }

    // Other mods' pipes use the item capability rather than the container, so it must keep the block's rules: from a
    // side or from no side at all, wheat goes in, beef doesn't, and nothing comes out.
    private static void pipesPutFoodInAMangerButTakeNoneOut(GameTestHelper helper) {
        FeederBlockEntity manger = FeederGameTests.feeder(helper, FeedBlocks.MANGER, FeederGameTests.FEEDER);
        BlockPos at = helper.absolutePos(FeederGameTests.FEEDER);
        for (Direction side : new Direction[]{Direction.UP, Direction.NORTH, null}) {
            int[] moved = throughThePipe(helper, at, side);
            helper.assertTrue(moved[0] == 2, "a pipe from " + side + " put in " + moved[0] + " of 2 wheat");
            helper.assertTrue(moved[1] == 0, "a pipe from " + side + " put in " + moved[1] + " beef");
            helper.assertTrue(moved[2] == 0, "a pipe from " + side + " took out " + moved[2] + " wheat");
        }
        helper.assertTrue(manger.food() == 6, "the Manger holds " + manger.food() + " items, not 6 wheat");
        helper.succeed();
    }

    // Wheat put in, beef put in, and wheat taken out.
    private static int[] throughThePipe(GameTestHelper helper, BlockPos at, Direction side) {
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(at);
        IItemHandler pipe = blockEntity == null ? null
                : blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve().orElse(null);
        if (pipe == null) {
            throw GameTestCompat.failure(helper, "the Manger has no item capability from " + side);
        }
        int wheat = 2 - ItemHandlerHelper.insertItem(pipe, new ItemStack(Items.WHEAT, 2), false).getCount();
        int beef = 2 - ItemHandlerHelper.insertItem(pipe, new ItemStack(Items.BEEF, 2), false).getCount();
        int out = 0;
        for (int slot = 0; slot < pipe.getSlots(); slot++) {
            out += pipe.extractItem(slot, 2, false).getCount();
        }
        return new int[]{wheat, beef, out};
    }
}
