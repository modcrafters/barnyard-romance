package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdLedger;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.forceBirth;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;

// In-game checks for the herd book: registering, births, deaths and the server's records.
public final class HerdBookGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("wild_animals_cannot_join_a_book", 20, HerdBookGameTests::wildAnimalsCannotJoinABook),
            new BreedingGameTests.Case("familiar_animals_join_once", 20, HerdBookGameTests::familiarAnimalsJoinOnce),
            new BreedingGameTests.Case("newborns_join_their_mothers_books", 20, HerdBookGameTests::newbornsJoinTheirMothersBooks),
            new BreedingGameTests.Case("newborns_join_frozen", 20, HerdBookGameTests::newbornsJoinFrozen),
            new BreedingGameTests.Case("a_death_marks_the_record", 20, HerdBookGameTests::aDeathMarksTheRecord),
            new BreedingGameTests.Case("a_dead_mother_stays_for_her_calf", 20, HerdBookGameTests::aDeadMotherStaysForHerCalf),
            new BreedingGameTests.Case("unloading_refreshes_the_record", 20, HerdBookGameTests::unloadingRefreshesTheRecord),
            new BreedingGameTests.Case("records_refresh_while_loaded", 20, HerdBookGameTests::recordsRefreshWhileLoaded),
            new BreedingGameTests.Case("frozen_records_keep_their_values", 20, HerdBookGameTests::frozenRecordsKeepTheirValues),
            new BreedingGameTests.Case("herd_books_survive_save_and_load", 20, HerdBookGameTests::herdBooksSurviveSaveAndLoad)
    );

    private HerdBookGameTests() {
    }

    public static void wildAnimalsCannotJoinABook(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Services.DATA.set(cow, traits(helper, cow).fed(0.1, -1L));
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);

        HerdBooks.RegisterResult result = HerdBooks.register(book, cow);

        helper.assertTrue(result == HerdBooks.RegisterResult.NOT_FAMILIAR, "a wild cow was " + result);
        helper.assertTrue(book.get(BarnyardComponents.HERD_BOOK) == null, "a refused cow gave the book an ID");
        helper.succeed();
    }

    public static void familiarAnimalsJoinOnce(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);

        helper.assertTrue(HerdBooks.register(book, cow) == HerdBooks.RegisterResult.ADDED, "a familiar cow was not added");
        helper.assertTrue(HerdBooks.register(book, cow) == HerdBooks.RegisterResult.ALREADY, "a cow was added twice");
        UUID id = bookId(helper, book);
        helper.assertTrue(data(helper).ledger().members(id).equals(java.util.Set.of(cow.getUUID())), "the book holds " + data(helper).ledger().members(id));
        helper.assertTrue(data(helper).record(cow.getUUID()) != null, "the cow has no record");
        helper.succeed();
    }

    public static void newbornsJoinTheirMothersBooks(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        UUID id = bookId(helper, book);

        List<Animal> young = breed(helper, mother, father);

        for (Animal calf : young) {
            helper.assertTrue(data(helper).ledger().isMember(id, calf.getUUID()), "a calf did not join its mother's book");
            helper.assertTrue(data(helper).record(calf.getUUID()) != null, "a calf has no record");
        }
        helper.assertTrue(!data(helper).ledger().isMember(id, father.getUUID()), "the father joined the book");
        helper.succeed();
    }

    public static void newbornsJoinFrozen(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        Services.DATA.set(mother, traits(helper, mother).fed(0.4, -1L));
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);

        Animal calf = breed(helper, mother, father).getFirst();

        HerdLedger.Node node = data(helper).ledger().node(calf.getUUID());
        helper.assertTrue(node != null && !node.everFamiliar(), "a calf at " + traits(helper, calf).familiarity() + " joined as familiar");
        helper.succeed();
    }

    public static void aDeathMarksTheRecord(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);
        UUID id = bookId(helper, book);

        cow.discard();

        helper.assertTrue(!data(helper).ledger().isMember(id, cow.getUUID()), "a dead cow with no young is still in the book");
        helper.assertTrue(data(helper).record(cow.getUUID()) == null, "a dropped cow still has a record");
        helper.succeed();
    }

    public static void aDeadMotherStaysForHerCalf(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        UUID id = bookId(helper, book);
        breed(helper, mother, father);

        mother.discard();

        helper.assertTrue(data(helper).ledger().isMember(id, mother.getUUID()), "the dead mother left her calf's book");
        HerdRecord record = data(helper).record(mother.getUUID());
        helper.assertTrue(record != null && record.diedAt() >= 0L, "the dead mother's record is " + record);
        helper.succeed();
    }

    public static void unloadingRefreshesTheRecord(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        Services.DATA.set(cow, traits(helper, cow).fed(1.0, 500L));

        HerdBooks.leftLevel(cow, Entity.RemovalReason.UNLOADED_TO_CHUNK);

        HerdRecord record = data(helper).record(cow.getUUID());
        helper.assertTrue(record.traits().lastFedTick() == 500L, "the record was fed at " + record.traits().lastFedTick());
        helper.assertTrue(record.diedAt() < 0L, "an unloaded cow was marked dead");
        helper.succeed();
    }

    public static void recordsRefreshWhileLoaded(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        Services.DATA.set(cow, traits(helper, cow).fed(1.0, 700L));

        HerdBooks.tick(server(helper), HerdBooks.REFRESH_TICKS);

        helper.assertTrue(data(helper).record(cow.getUUID()).traits().lastFedTick() == 700L, "the timer did not refresh a loaded cow");
        helper.succeed();
    }

    public static void frozenRecordsKeepTheirValues(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        AnimalTraits before = data(helper).record(cow.getUUID()).traits();
        Services.DATA.set(cow, traits(helper, cow).fed(0.1, 900L));

        HerdBooks.leftLevel(cow, Entity.RemovalReason.UNLOADED_TO_CHUNK);

        AnimalTraits frozen = data(helper).record(cow.getUUID()).traits();
        helper.assertTrue(frozen.lastFedTick() == before.lastFedTick(), "a frozen record took the new feeding time " + frozen.lastFedTick());
        helper.assertTrue(frozen.familiarity() == 0.1, "a frozen record has familiarity " + frozen.familiarity());
        helper.succeed();
    }

    public static void herdBooksSurviveSaveAndLoad(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        breed(helper, mother, father);
        HerdBookData data = data(helper);

        Tag saved = HerdBookData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        HerdBookData loaded = HerdBookData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();

        helper.assertTrue(loaded.ledger().books().equals(data.ledger().books()), "the loaded books differ");
        helper.assertTrue(loaded.ledger().nodes().equals(data.ledger().nodes()), "the loaded lineage differs");
        HerdRecord record = loaded.record(mother.getUUID());
        helper.assertTrue(record != null && record.species().equals("minecraft:cow"), "the loaded mother's record is " + record);
        helper.succeed();
    }

    private static List<Animal> breed(GameTestHelper helper, Animal mother, Animal father) {
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        return forceBirth(helper, mother);
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }

    private static HerdBookData data(GameTestHelper helper) {
        return HerdBookData.get(server(helper));
    }

    private static UUID bookId(GameTestHelper helper, ItemStack book) {
        UUID id = book.get(BarnyardComponents.HERD_BOOK);
        if (id == null) {
            throw GameTestCompat.failure(helper, "the book has no ID");
        }
        return id;
    }
}
