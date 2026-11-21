package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Conversions;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdComponents;
import net.modcrafters.minecraft.barnyardromance.herd.HerdItems;
import net.modcrafters.minecraft.barnyardromance.herd.HerdLedger;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;

import java.util.List;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.assertFamiliarity;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.withFamiliarity;
import static net.modcrafters.minecraft.barnyardromance.shear.ShearSlices.LAST_SHEARED;

// These run with Friends&Foes loaded, through the farm-mods runs (gradle/farm-mods-runs.gradle). They find its
// creatures by id, so the mod still doesn't compile against it. In a run without it they pass at once and prove
// nothing, except that a farm-mods run with no jar in its mods folder fails instead of looking green (FarmMods.present).
public final class FriendsAndFoesGameTests {
    private static final String MOD = "friendsandfoes";
    private static final String MOOBLOOM = "friendsandfoes:moobloom";

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_sheared_moobloom_keeps_its_record", 20, FriendsAndFoesGameTests::aShearedMoobloomKeepsItsRecord),
            new BreedingGameTests.Case("a_moobloom_milks_and_gives_stew_by_our_rules", 20, FriendsAndFoesGameTests::aMoobloomMilksAndGivesStewByOurRules),
            new BreedingGameTests.Case("moobloom_stew_is_refused_today_after_a_refusal", 20, FriendsAndFoesGameTests::moobloomStewIsRefusedTodayAfterARefusal),
            new BreedingGameTests.Case("crab_and_penguin_are_out", 20, FriendsAndFoesGameTests::crabAndPenguinAreOut)
    );

    private FriendsAndFoesGameTests() {
    }

    // The mooshroom's test (ConversionGameTests) on the moobloom, whose mod turns it into a cow by its own method.
    public static void aShearedMoobloomKeepsItsRecord(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal moobloom = withFamiliarity(helper, female(helper, type(helper, MOOBLOOM)), 0.8);
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(moobloom, PregnancyFeature.PREGNANCY,
                Pregnancy.builder(now, 66_000L, MOOBLOOM, 1.35).motherFamiliarity(1.0).build());
        AnimalTraits before = traits(helper, moobloom);
        ItemStack book = new ItemStack(HerdItems.HERD_BOOK);
        HerdBooks.register(book, moobloom);
        UUID bookId = book.get(HerdComponents.HERD_BOOK);
        UUID oldId = moobloom.getUUID();
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));

        GameTestCompat.useOn(moobloom, player);

        helper.assertTrue(moobloom.isRemoved(), "the moobloom was not sheared");
        List<Animal> cows = helper.getLevel().getEntitiesOfClass(Animal.class, moobloom.getBoundingBox().inflate(4.0),
                animal -> animal.getType() == EntityTypes.COW);
        helper.assertTrue(cows.size() == 1, "the shearing left " + cows.size() + " cows");
        Animal cow = cows.get(0);
        AnimalTraits after = traits(helper, cow);
        helper.assertTrue(after.sex() == before.sex(), "the cow is " + after.sex() + ", the moobloom was " + before.sex());
        helper.assertTrue(after.scale() == before.scale(), "the cow is size " + after.scale() + ", the moobloom was " + before.scale());
        double floor = before.get(CARE).familiarity() - BarnyardConfig.current().tuning().familiarityShearingCost();
        helper.assertTrue(after.get(CARE).familiarity() >= floor - 1.0e-9, "the cow has familiarity " + after.get(CARE).familiarity() + ", under " + floor);
        helper.assertTrue(Services.DATA.get(cow, PregnancyFeature.PREGNANCY).isPresent(), "the cow lost the pregnancy");
        helper.assertTrue(Conversions.successor(moobloom).orElse(null) == cow, "the moobloom's successor is not the cow");
        helper.assertTrue(after.get(LAST_SHEARED) == helper.getLevel().getOverworldClockTime(),
                "the cow does not count as sheared today: " + after.get(LAST_SHEARED));

        // No death: the moobloom was carried before it left, so the book never saw it go.
        HerdBookData data = HerdBookData.get(helper.getLevel().getServer());
        HerdLedger.Node node = data.ledger().node(cow.getUUID());
        helper.assertTrue(node != null && node.alive(), "the book does not list the cow alive: " + node);
        helper.assertTrue(data.ledger().isMember(bookId, cow.getUUID()), "the cow is not in the book");
        helper.assertTrue(data.ledger().node(oldId) == null && !data.ledger().isMember(bookId, oldId), "the book still lists the moobloom");
        HerdRecord record = data.record(cow.getUUID());
        helper.assertTrue(record != null && record.diedAt() < 0L, "the cow's record is " + record);
        helper.assertTrue(record.species().equals("minecraft:cow"), "the cow's record is for " + record.species());
        helper.assertTrue(data.record(oldId) == null, "the moobloom's record, a death, is still kept");
        helper.succeed();
    }

    // Cattle rules: a mother gives milk to a bucket, and a bowl's stew costs familiarity like milking does.
    public static void aMoobloomMilksAndGivesStewByOurRules(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal moobloom = ProductGameTests.mother(helper, type(helper, MOOBLOOM), 0.5);
        Player player = holding(helper, Items.BUCKET);
        GameTestCompat.interact(moobloom, player);
        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "a moobloom mother gave " + player.getMainHandItem());

        withFamiliarity(helper, moobloom, 0.8);
        Player bowl = holding(helper, Items.BOWL);
        GameTestCompat.interact(moobloom, bowl);
        helper.assertTrue(bowl.getMainHandItem().is(Items.SUSPICIOUS_STEW), "the moobloom gave " + bowl.getMainHandItem());
        assertFamiliarity(helper, moobloom, 0.75);
        helper.succeed();
    }

    public static void moobloomStewIsRefusedTodayAfterARefusal(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal moobloom = withFamiliarity(helper, female(helper, type(helper, MOOBLOOM)), 0.8);
        Player player = holding(helper, Items.BOWL);

        MilkActions.refuseMilking(moobloom, null);
        GameTestCompat.interact(moobloom, player);

        helper.assertTrue(player.getMainHandItem().is(Items.BOWL), "a moobloom that refused today gave " + player.getMainHandItem());
        assertFamiliarity(helper, moobloom, 0.8);
        helper.succeed();
    }

    public static void crabAndPenguinAreOut(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        int x = 2;
        for (String id : List.of("friendsandfoes:crab", "friendsandfoes:penguin")) {
            helper.assertTrue(!BarnyardConfig.current().speciesEnabled(id), id + " is tracked by default");
            Animal animal = GameTestCompat.spawn(helper, type(helper, id), x++, 1, 4);
            helper.assertTrue(Services.DATA.get(animal).isEmpty(), "a " + id + " has traits");
        }
        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Animal> type(GameTestHelper helper, String id) {
        return (EntityType<? extends Animal>) BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(id))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + id));
    }
}
