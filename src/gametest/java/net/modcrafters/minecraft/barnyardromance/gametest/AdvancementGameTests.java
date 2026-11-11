package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.advancement.CreditData;
import net.modcrafters.minecraft.barnyardromance.advancement.CreditLedger;
import net.modcrafters.minecraft.barnyardromance.advancement.Grants;
import net.modcrafters.minecraft.barnyardromance.advancement.Ladders;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.Hatching;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.modcrafters.minecraft.barnyardromance.eggs.WildEgg;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.rivalry.Fights;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.withFamiliarity;

// In-game checks for the Barnyard Romance advancement tab: each moment grants its advancement to the right player,
// and a player who is offline gets theirs at the next login.
public final class AdvancementGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("befriending_a_wild_animal_earns_the_root", 20, AdvancementGameTests::befriendingAWildAnimalEarnsTheRoot),
            new BreedingGameTests.Case("feeding_an_already_familiar_animal_does_not_earn_the_root", 20, AdvancementGameTests::feedingAnAlreadyFamiliarAnimalDoesNotEarnTheRoot),
            new BreedingGameTests.Case("held_grants_are_given_at_login", 20, AdvancementGameTests::heldGrantsAreGivenAtLogin),
            new BreedingGameTests.Case("granting_to_an_offline_player_holds_it", 20, AdvancementGameTests::grantingToAnOfflinePlayerHoldsIt),
            new BreedingGameTests.Case("two_by_two_counts_a_calf_born_from_a_pregnancy", 100, AdvancementGameTests::twoByTwoCountsACalfBornFromAPregnancy),
            new BreedingGameTests.Case("mating_earns_a_bun_in_the_oven", 20, AdvancementGameTests::matingEarnsABunInTheOven),
            new BreedingGameTests.Case("a_birth_earns_special_delivery_for_the_breeder", 100, AdvancementGameTests::aBirthEarnsSpecialDeliveryForTheBreeder),
            new BreedingGameTests.Case("offline_breeder_is_credited_at_login", 100, AdvancementGameTests::offlineBreederIsCreditedAtLogin),
            new BreedingGameTests.Case("a_breeder_who_left_before_mating_is_remembered", 20, AdvancementGameTests::aBreederWhoLeftBeforeMatingIsRemembered),
            new BreedingGameTests.Case("a_pregnancy_without_a_breeder_credits_nobody", 100, AdvancementGameTests::aPregnancyWithoutABreederCreditsNobody),
            new BreedingGameTests.Case("pregnancy_off_still_earns_both", 20, AdvancementGameTests::pregnancyOffStillEarnsBoth),
            new BreedingGameTests.Case("a_grandchild_earns_family_tree", 100, AdvancementGameTests::aGrandchildEarnsFamilyTree),
            new BreedingGameTests.Case("a_calf_of_wild_parents_is_no_family_tree", 100, AdvancementGameTests::aCalfOfWildParentsIsNoFamilyTree),
            new BreedingGameTests.Case("ten_piglets_earn_the_first_pig_step", 100, AdvancementGameTests::tenPigletsEarnTheFirstPigStep),
            new BreedingGameTests.Case("a_donkey_foal_counts_for_the_horse_ladder", 100, AdvancementGameTests::aDonkeyFoalCountsForTheHorseLadder),
            new BreedingGameTests.Case("naturalist_ladders_load_only_with_naturalist", 20, AdvancementGameTests::naturalistLaddersLoadOnlyWithNaturalist),
            new BreedingGameTests.Case("a_bred_egg_hatching_credits_players_nearby", 20, AdvancementGameTests::aBredEggHatchingCreditsPlayersNearby),
            new BreedingGameTests.Case("a_wild_egg_earns_which_came_first_but_no_ladder", 20, AdvancementGameTests::aWildEggEarnsWhichCameFirstButNoLadder),
            new BreedingGameTests.Case("a_thrown_bred_egg_counts", 20, AdvancementGameTests::aThrownBredEggCounts),
            new BreedingGameTests.Case("milking_a_familiar_cow_earns_udder_trust", 20, AdvancementGameTests::milkingAFamiliarCowEarnsUdderTrust),
            new BreedingGameTests.Case("hand_shearing_earns_fleece_navidad", 20, AdvancementGameTests::handShearingEarnsFleeceNavidad),
            new BreedingGameTests.Case("dispenser_shearing_credits_nobody", 20, AdvancementGameTests::dispenserShearingCreditsNobody),
            new BreedingGameTests.Case("killing_a_familiar_cow_is_dinner", 20, AdvancementGameTests::killingAFamiliarCowIsDinner),
            new BreedingGameTests.Case("a_cow_burning_to_death_after_a_hit_is_dinner", 20, AdvancementGameTests::aCowBurningToDeathAfterAHitIsDinner),
            new BreedingGameTests.Case("killing_a_tamed_pet_is_not_dinner", 20, AdvancementGameTests::killingATamedPetIsNotDinner),
            new BreedingGameTests.Case("killing_a_familiar_fox_is_not_dinner", 20, AdvancementGameTests::killingAFamiliarFoxIsNotDinner),
            new BreedingGameTests.Case("writing_an_animal_into_a_herd_book_earns_paper_trail", 20, AdvancementGameTests::writingAnAnimalIntoAHerdBookEarnsPaperTrail),
            new BreedingGameTests.Case("a_fight_starting_earns_locking_horns", 20, AdvancementGameTests::aFightStartingEarnsLockingHorns)
    );
    private static final Vec3 NEST = new Vec3(4.5, 2.0, 4.5);
    static final Identifier TWO_BY_TWO = Identifier.fromNamespaceAndPath("minecraft", "husbandry/bred_all_animals");

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
        ServerPlayer player = MockPlayers.inLevel(helper);
        CreditLedger ledger = CreditData.get(helper.getLevel().getServer()).ledger();
        ledger.hold(player.getUUID(), new CreditLedger.Held(BarnyardAdvancements.ROOT.toString(), Grants.CRITERION));

        BarnyardAdvancements.loggedIn(player);

        helper.assertTrue(earned(player, BarnyardAdvancements.ROOT), "a held grant wasn't given at login");
        helper.assertTrue(ledger.held(player.getUUID()).isEmpty(), "a grant given at login is still held");
        // The client shows no toast for anything in the first advancement packet, which resets its list.
        helper.assertTrue(!firstPacketPending(helper, player), "a held grant went out in the login reset, with no toast");
        helper.succeed();
    }

    // PlayerAdvancements.isFirstPacket: whether the next update the player gets is the reset one sent at login.
    static boolean firstPacketPending(GameTestHelper helper, ServerPlayer player) {
        try {
            java.lang.reflect.Field field = player.getAdvancements().getClass().getDeclaredField("isFirstPacket");
            field.setAccessible(true);
            return field.getBoolean(player.getAdvancements());
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't read isFirstPacket: " + exception);
        }
    }

    public static void grantingToAnOfflinePlayerHoldsIt(GameTestHelper helper) {
        UUID offline = UUID.randomUUID();

        Grants.grant(helper.getLevel().getServer(), offline, BarnyardAdvancements.ROOT, Grants.CRITERION);

        List<CreditLedger.Held> held = CreditData.get(helper.getLevel().getServer()).ledger().held(offline);
        helper.assertTrue(held.equals(List.of(new CreditLedger.Held(BarnyardAdvancements.ROOT.toString(), Grants.CRITERION))),
                "an offline player's grant is held as " + held);
        helper.succeed();
    }

    public static void twoByTwoCountsACalfBornFromAPregnancy(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        Animal mother = bredCow(helper, breeder);

        due(helper, mother);
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the cow is still pregnant");
            helper.assertTrue(criterionDone(breeder, TWO_BY_TWO, "minecraft:cow"), "Two by Two didn't count the calf");
        });
    }

    public static void matingEarnsABunInTheOven(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);

        bredCow(helper, breeder);

        helper.assertTrue(earned(breeder, BarnyardAdvancements.BUN_IN_THE_OVEN), "mating didn't earn A Bun in the Oven");
        helper.assertTrue(!earned(breeder, BarnyardAdvancements.SPECIAL_DELIVERY), "mating earned Special Delivery before the birth");
        helper.succeed();
    }

    public static void aBirthEarnsSpecialDeliveryForTheBreeder(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        ServerPlayer bystander = MockPlayers.inLevel(helper);
        Animal mother = bredCow(helper, breeder);

        due(helper, mother);
        helper.succeedWhen(() -> {
            helper.assertTrue(earned(breeder, BarnyardAdvancements.SPECIAL_DELIVERY), "the breeder didn't earn Special Delivery");
            helper.assertTrue(!earned(bystander, BarnyardAdvancements.SPECIAL_DELIVERY), "a bystander earned Special Delivery");
        });
    }

    public static void offlineBreederIsCreditedAtLogin(GameTestHelper helper) {
        UUID offline = UUID.randomUUID();
        Animal mother = bredCow(helper, null);
        Services.DATA.setPregnancy(mother, BreedingGameTests.pregnancy(helper, mother).withBreederId(offline));
        int litter = BreedingGameTests.pregnancy(helper, mother).litterSize();

        due(helper, mother);
        helper.succeedWhen(() -> {
            CreditLedger ledger = CreditData.get(helper.getLevel().getServer()).ledger();
            helper.assertTrue(ledger.births(offline, "cow") == litter,
                    "an offline breeder's cow births are " + ledger.births(offline, "cow") + ", not " + litter);
            List<CreditLedger.Held> held = ledger.held(offline);
            helper.assertTrue(held.contains(new CreditLedger.Held(BarnyardAdvancements.SPECIAL_DELIVERY.toString(), Grants.CRITERION)),
                    "Special Delivery isn't held for an offline breeder: " + held);
            helper.assertTrue(held.contains(new CreditLedger.Held(TWO_BY_TWO.toString(), "minecraft:cow")),
                    "Two by Two isn't held for an offline breeder: " + held);
        });
    }

    // A player who feeds a cow into love and logs off (or leaves the dimension) before the bull reaches her is still
    // her breeder; vanilla's getLoveCause only finds players in the cow's level.
    public static void aBreederWhoLeftBeforeMatingIsRemembered(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        mother.setInLove(breeder);
        father.setInLove(null);
        helper.getLevel().getServer().getPlayerList().remove(breeder);
        helper.assertTrue(mother.getLoveCause() == null, "vanilla still finds a breeder who left");

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        UUID recorded = BreedingGameTests.pregnancy(helper, mother).breederId();
        helper.assertTrue(breeder.getUUID().equals(recorded), "the pregnancy's breeder is " + recorded + ", not the player who left");
        helper.succeed();
    }

    public static void aPregnancyWithoutABreederCreditsNobody(GameTestHelper helper) {
        ServerPlayer bystander = MockPlayers.inLevel(helper);
        Animal mother = bredCow(helper, null);
        helper.assertTrue(BreedingGameTests.pregnancy(helper, mother).breederId() == null, "a pregnancy without a player has a breeder");

        due(helper, mother);
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the cow is still pregnant");
            helper.assertTrue(!earned(bystander, BarnyardAdvancements.SPECIAL_DELIVERY), "a bystander earned Special Delivery");
        });
    }

    public static void pregnancyOffStillEarnsBoth(GameTestHelper helper) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"pregnancyEnabled\": false}"));
            ServerPlayer breeder = MockPlayers.inLevel(helper);
            Animal mother = female(helper, EntityTypes.COW);
            Animal father = male(helper, EntityTypes.COW);
            mother.setInLove(breeder);
            father.setInLove(null);

            mother.spawnChildFromBreeding(helper.getLevel(), father);

            helper.assertTrue(earned(breeder, BarnyardAdvancements.BUN_IN_THE_OVEN), "vanilla breeding didn't earn A Bun in the Oven");
            helper.assertTrue(earned(breeder, BarnyardAdvancements.SPECIAL_DELIVERY), "vanilla breeding didn't earn Special Delivery");
        } finally {
            BarnyardConfig.set(config);
        }
        helper.succeed();
    }

    public static void aGrandchildEarnsFamilyTree(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        Animal mother = bredCow(helper, breeder);
        Services.DATA.set(mother, BreedingGameTests.traits(helper, mother).withLineage(new Lineage(UUID.randomUUID(), null)));

        due(helper, mother);
        helper.succeedWhen(() -> helper.assertTrue(earned(breeder, BarnyardAdvancements.FAMILY_TREE), "a grandchild didn't earn Family Tree"));
    }

    public static void aCalfOfWildParentsIsNoFamilyTree(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        Animal mother = bredCow(helper, breeder);

        due(helper, mother);
        helper.succeedWhen(() -> {
            helper.assertTrue(earned(breeder, BarnyardAdvancements.SPECIAL_DELIVERY), "the calf isn't born yet");
            helper.assertTrue(!earned(breeder, BarnyardAdvancements.FAMILY_TREE), "a calf of wild parents earned Family Tree");
        });
    }

    public static void tenPigletsEarnTheFirstPigStep(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        CreditLedger ledger = CreditData.get(helper.getLevel().getServer()).ledger();
        ledger.addBirths(breeder.getUUID(), "pig", 8);
        Animal mother = bred(helper, EntityTypes.PIG, breeder);
        int litter = BreedingGameTests.pregnancy(helper, mother).litterSize();

        due(helper, mother);
        helper.succeedWhen(() -> {
            helper.assertTrue(ledger.births(breeder.getUUID(), "pig") == 8 + litter,
                    "pig births are " + ledger.births(breeder.getUUID(), "pig") + ", not " + (8 + litter));
            helper.assertTrue(earned(breeder, Ladders.advancement("pig", 10)), "ten piglets didn't earn the first pig step");
            helper.assertTrue(!earned(breeder, Ladders.advancement("pig", 100)), "ten piglets earned the second pig step");
        });
    }

    public static void aDonkeyFoalCountsForTheHorseLadder(GameTestHelper helper) {
        ServerPlayer breeder = MockPlayers.inLevel(helper);
        Animal mother = bred(helper, EntityTypes.DONKEY, breeder);

        due(helper, mother);
        helper.succeedWhen(() -> {
            int births = CreditData.get(helper.getLevel().getServer()).ledger().births(breeder.getUUID(), "horse");
            helper.assertTrue(births == 1, "a donkey foal made " + births + " horse births, not 1");
        });
    }

    public static void naturalistLaddersLoadOnlyWithNaturalist(GameTestHelper helper) {
        boolean loaded = Services.PLATFORM.isModLoaded("naturalist");
        boolean present = helper.getLevel().getServer().getAdvancements().get(Ladders.advancement("deer", 10)) != null;

        helper.assertTrue(present == loaded, "the deer ladder is " + (present ? "" : "not ") + "loaded, and Naturalist is "
                + (loaded ? "" : "not ") + "installed");
        helper.assertTrue(helper.getLevel().getServer().getAdvancements().get(Ladders.advancement("cow", 10)) != null, "the cow ladder isn't loaded");
        helper.succeed();
    }

    public static void aBredEggHatchingCreditsPlayersNearby(GameTestHelper helper) {
        ServerPlayer near = playerAt(helper, new Vec3(2.5, 2.0, 2.5));
        ServerPlayer far = playerAt(helper, new Vec3(34.5, 2.0, 4.5));

        Hatching.hatch(helper.getLevel(), EggGameTests.eggData(1.0, 1.0, 0L), helper.absoluteVec(NEST), helper.getLevel().getGameTime());

        assertCountedChick(helper, near);
        helper.assertTrue(!earned(far, BarnyardAdvancements.WHICH_CAME_FIRST), "a player 30 blocks away earned Which Came First?");
        helper.assertTrue(births(helper, far, "chicken") == 0, "a player 30 blocks away got a chicken birth");
        helper.succeed();
    }

    public static void aWildEggEarnsWhichCameFirstButNoLadder(GameTestHelper helper) {
        ServerPlayer near = playerAt(helper, new Vec3(2.5, 2.0, 2.5));

        Hatching.hatchWild(helper.getLevel(), WildEgg.of("minecraft:chicken", ""), helper.absoluteVec(NEST), helper.getLevel().getGameTime());

        helper.assertTrue(earned(near, BarnyardAdvancements.WHICH_CAME_FIRST), "a wild egg's hatching didn't earn Which Came First?");
        helper.assertTrue(births(helper, near, "chicken") == 0, "a wild egg's chick counted towards the chicken ladder");
        helper.assertTrue(!criterionDone(near, TWO_BY_TWO, "minecraft:chicken"), "a wild egg's chick counted for Two by Two");
        helper.succeed();
    }

    public static void aThrownBredEggCounts(GameTestHelper helper) {
        ServerPlayer near = playerAt(helper, new Vec3(2.5, 2.0, 2.5));

        ThrownEggs.hatchTainted(helper.getLevel(), helper.absoluteVec(NEST), EggGameTests.eggData(1.0, 1.0, 0L), helper.getLevel().getRandom());

        assertCountedChick(helper, near);
        helper.succeed();
    }

    public static void milkingAFamiliarCowEarnsUdderTrust(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        ServerPlayer player = MockPlayers.inLevel(helper);
        // A creative player keeps the empty bucket.
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));

        GameTestCompat.interact(cow, player);

        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "the cow wasn't milked: " + player.getMainHandItem());
        helper.assertTrue(earned(player, BarnyardAdvancements.UDDER_TRUST), "milking a familiar cow didn't earn Udder Trust");
        helper.succeed();
    }

    public static void handShearingEarnsFleeceNavidad(GameTestHelper helper) {
        Sheep sheep = (Sheep) female(helper, EntityTypes.SHEEP);
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));

        GameTestCompat.useOn(sheep, player);

        helper.assertTrue(sheep.isSheared(), "the sheep wasn't sheared");
        helper.assertTrue(earned(player, BarnyardAdvancements.FLEECE_NAVIDAD), "shearing a familiar sheep didn't earn Fleece Navidad");
        helper.succeed();
    }

    // Shears let through on a sheep with no wool shear nothing; a dispenser shearing her later is nobody's.
    public static void dispenserShearingCreditsNobody(GameTestHelper helper) {
        Sheep sheep = (Sheep) female(helper, EntityTypes.SHEEP);
        sheep.setSheared(true);
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        GameTestCompat.useOn(sheep, player);

        helper.runAfterDelay(1, () -> {
            sheep.setSheared(false);
            //? if >=1.21.2 {
            sheep.shear(helper.getLevel(), SoundSource.BLOCKS, new ItemStack(Items.SHEARS));
            //?} else
            //sheep.shear(SoundSource.BLOCKS);
            helper.assertTrue(sheep.isSheared(), "the dispenser didn't shear the sheep");
            helper.assertTrue(!earned(player, BarnyardAdvancements.FLEECE_NAVIDAD), "a dispenser's shearing earned Fleece Navidad");
            helper.succeed();
        });
    }

    public static void killingAFamiliarCowIsDinner(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ServerPlayer player = MockPlayers.inLevel(helper);

        cow.die(helper.getLevel().damageSources().playerAttack(player));

        helper.assertTrue(earned(player, BarnyardAdvancements.NOT_YOU_ITS_DINNER), "killing a familiar cow didn't earn the dinner advancement");
        helper.succeed();
    }

    // Fire Aspect: the hit is the player's, but the burning that finishes the cow names no attacker.
    public static void aCowBurningToDeathAfterAHitIsDinner(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ServerPlayer player = MockPlayers.inLevel(helper);
        //? if >=1.21.2 {
        cow.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
        //?} else
        //cow.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);

        cow.die(helper.getLevel().damageSources().onFire());

        helper.assertTrue(earned(player, BarnyardAdvancements.NOT_YOU_ITS_DINNER), "a cow the player set burning didn't earn the dinner advancement");
        helper.succeed();
    }

    public static void killingATamedPetIsNotDinner(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.inLevel(helper);
        TamableAnimal wolf = (TamableAnimal) female(helper, EntityTypes.WOLF);
        wolf.tame(player);
        AbstractHorse horse = (AbstractHorse) female(helper, EntityTypes.HORSE);
        horse.setTamed(true);

        wolf.die(helper.getLevel().damageSources().playerAttack(player));
        horse.die(helper.getLevel().damageSources().playerAttack(player));

        helper.assertTrue(!earned(player, BarnyardAdvancements.NOT_YOU_ITS_DINNER), "killing a tamed pet earned the dinner advancement");
        helper.succeed();
    }

    // A fox isn't livestock, however familiar.
    public static void killingAFamiliarFoxIsNotDinner(GameTestHelper helper) {
        Animal fox = female(helper, EntityTypes.FOX);
        ServerPlayer player = MockPlayers.inLevel(helper);
        helper.assertTrue(Services.DATA.get(fox).isPresent(), "the fox isn't tracked, so this proves nothing");

        fox.die(helper.getLevel().damageSources().playerAttack(player));

        helper.assertTrue(!earned(player, BarnyardAdvancements.NOT_YOU_ITS_DINNER), "killing a familiar fox earned the dinner advancement");
        helper.succeed();
    }

    public static void writingAnAnimalIntoAHerdBookEarnsPaperTrail(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BarnyardItems.HERD_BOOK));

        try {
            GameTestCompat.useOn(cow, player);
        } catch (RuntimeException exception) {
            // The book opens after the cow is written in, and NeoForge won't send its screen to a mock player.
        }

        helper.assertTrue(earned(player, BarnyardAdvancements.PAPER_TRAIL), "writing a cow into a herd book didn't earn Paper Trail");
        helper.succeed();
    }

    public static void aFightStartingEarnsLockingHorns(GameTestHelper helper) {
        ServerPlayer near = playerAt(helper, new Vec3(2.5, 2.0, 2.5));
        ServerPlayer far = playerAt(helper, new Vec3(34.5, 2.0, 4.5));
        Animal first = male(helper, EntityTypes.COW);
        Animal second = GameTestCompat.spawn(helper, EntityTypes.COW, 6, 1, 4);
        Services.DATA.set(second, BreedingGameTests.traits(helper, first));

        Fights.start(first, second, null);

        helper.assertTrue(earned(near, BarnyardAdvancements.LOCKING_HORNS), "a fight didn't earn Locking Horns nearby");
        helper.assertTrue(!earned(far, BarnyardAdvancements.LOCKING_HORNS), "a player 30 blocks away earned Locking Horns");
        helper.succeed();
    }

    static void assertCountedChick(GameTestHelper helper, ServerPlayer player) {
        helper.assertTrue(earned(player, BarnyardAdvancements.WHICH_CAME_FIRST), "a hatching didn't earn Which Came First? nearby");
        helper.assertTrue(births(helper, player, "chicken") == 1, "a bred egg's chick made " + births(helper, player, "chicken") + " chicken births, not 1");
        helper.assertTrue(criterionDone(player, TWO_BY_TWO, "minecraft:chicken"), "a bred egg's chick didn't count for Two by Two");
    }

    static ServerPlayer playerAt(GameTestHelper helper, Vec3 at) {
        ServerPlayer player = MockPlayers.inLevel(helper);
        Vec3 absolute = helper.absoluteVec(at);
        player.setPos(absolute.x, absolute.y, absolute.z);
        return player;
    }

    static int births(GameTestHelper helper, ServerPlayer player, String ladder) {
        return CreditData.get(helper.getLevel().getServer()).ledger().births(player.getUUID(), ladder);
    }

    // A cow and a bull mated, with the cow put in love by the breeder (null for no player).
    static Animal bredCow(GameTestHelper helper, @Nullable ServerPlayer breeder) {
        return bred(helper, EntityTypes.COW, breeder);
    }

    static Animal bred(GameTestHelper helper, EntityType<? extends Animal> type, @Nullable ServerPlayer breeder) {
        Animal mother = female(helper, type);
        Animal father = male(helper, type);
        mother.setInLove(breeder);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        BreedingGameTests.pregnancy(helper, mother);
        return mother;
    }

    // The pregnancy is moved back so the birth comes on the next tick.
    static void due(GameTestHelper helper, Animal mother) {
        Pregnancy pregnancy = BreedingGameTests.pregnancy(helper, mother);
        Services.DATA.setPregnancy(mother, pregnancy.withConceivedAt(helper.getLevel().getGameTime() - pregnancy.durationTicks()));
    }

    // A player sneaking up with wheat, as one wins over a wild animal.
    static ServerPlayer feeding(GameTestHelper helper, Animal animal) {
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        player.setShiftKeyDown(true);
        FamiliarityActions.interact(animal, player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        return player;
    }

    public static boolean earned(ServerPlayer player, Identifier id) {
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
