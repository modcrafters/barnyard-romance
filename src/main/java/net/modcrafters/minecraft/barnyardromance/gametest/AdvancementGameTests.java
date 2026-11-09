package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.advancement.CreditData;
import net.modcrafters.minecraft.barnyardromance.advancement.CreditLedger;
import net.modcrafters.minecraft.barnyardromance.advancement.Grants;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.withFamiliarity;

// In-game checks for the Barnyard Romance advancement tab: each moment grants its advancement to the right player,
// and a player who is offline gets theirs at the next login.
public final class AdvancementGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("befriending_a_wild_animal_earns_the_root", 20, AdvancementGameTests::befriendingAWildAnimalEarnsTheRoot),
            new BreedingGameTests.Case("feeding_an_already_familiar_animal_does_not_earn_the_root", 20, AdvancementGameTests::feedingAnAlreadyFamiliarAnimalDoesNotEarnTheRoot),
            new BreedingGameTests.Case("held_grants_are_given_at_login", 20, AdvancementGameTests::heldGrantsAreGivenAtLogin),
            new BreedingGameTests.Case("granting_to_an_offline_player_holds_it", 20, AdvancementGameTests::grantingToAnOfflinePlayerHoldsIt)
    );

    private AdvancementGameTests() {
    }

    public static void befriendingAWildAnimalEarnsTheRoot(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, female(helper, EntityTypes.COW), Familiarity.threshold() - 0.01);
        ServerPlayer player = feeding(helper, cow);

        helper.assertTrue(earned(player, BarnyardAdvancements.ROOT), "befriending a cow didn't earn the root");
        helper.succeed();
    }

    public static void feedingAnAlreadyFamiliarAnimalDoesNotEarnTheRoot(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, female(helper, EntityTypes.COW), 0.8);
        ServerPlayer player = feeding(helper, cow);

        helper.assertTrue(!earned(player, BarnyardAdvancements.ROOT), "feeding a familiar cow earned the root");
        helper.succeed();
    }

    public static void heldGrantsAreGivenAtLogin(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CreditLedger ledger = CreditData.get(helper.getLevel().getServer()).ledger();
        ledger.hold(player.getUUID(), new CreditLedger.Held(BarnyardAdvancements.ROOT.toString(), Grants.CRITERION));

        BarnyardAdvancements.loggedIn(player);

        helper.assertTrue(earned(player, BarnyardAdvancements.ROOT), "a held grant wasn't given at login");
        helper.assertTrue(ledger.held(player.getUUID()).isEmpty(), "a grant given at login is still held");
        helper.succeed();
    }

    public static void grantingToAnOfflinePlayerHoldsIt(GameTestHelper helper) {
        UUID offline = UUID.randomUUID();

        Grants.grant(helper.getLevel().getServer(), offline, BarnyardAdvancements.ROOT, Grants.CRITERION);

        List<CreditLedger.Held> held = CreditData.get(helper.getLevel().getServer()).ledger().held(offline);
        helper.assertTrue(held.equals(List.of(new CreditLedger.Held(BarnyardAdvancements.ROOT.toString(), Grants.CRITERION))),
                "an offline player's grant is held as " + held);
        helper.succeed();
    }

    // A player sneaking up with wheat, as one wins over a wild animal.
    static ServerPlayer feeding(GameTestHelper helper, Animal animal) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        player.setShiftKeyDown(true);
        FamiliarityActions.interact(animal, player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        return player;
    }

    static boolean earned(ServerPlayer player, Identifier id) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(id);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    // Whether one criterion of an advancement is done, such as one animal of vanilla's Two by Two.
    static boolean criterionDone(ServerPlayer player, Identifier id, String criterion) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(id);
        if (holder == null) {
            return false;
        }
        var progress = player.getAdvancements().getOrStartProgress(holder).getCriterion(criterion);
        return progress != null && progress.isDone();
    }
}
