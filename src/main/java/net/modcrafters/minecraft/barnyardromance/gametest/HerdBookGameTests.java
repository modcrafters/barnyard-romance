package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
import com.mojang.serialization.Dynamic;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.herd.RecordSheet;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.StatKind;
import net.modcrafters.minecraft.barnyardromance.stats.StatLine;
import net.modcrafters.minecraft.barnyardromance.stats.StatSheet;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.herd.ShearState;
import net.modcrafters.minecraft.barnyardromance.herd.HerdStatus;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.herd.HerdLedger;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecords;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.forceBirth;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;

// In-game checks for the herd book: registering, births, deaths and the server's records.
public final class HerdBookGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("wild_animals_cannot_join_a_book", 20, HerdBookGameTests::wildAnimalsCannotJoinABook),
            new BreedingGameTests.Case("familiar_animals_join_once", 20, HerdBookGameTests::familiarAnimalsJoinOnce),
            new BreedingGameTests.Case("tamed_cats_join_without_sitting", 20, HerdBookGameTests::tamedCatsJoinWithoutSitting),
            new BreedingGameTests.Case("newborns_join_their_mothers_books", 20, HerdBookGameTests::newbornsJoinTheirMothersBooks),
            new BreedingGameTests.Case("newborns_join_frozen", 20, HerdBookGameTests::newbornsJoinFrozen),
            new BreedingGameTests.Case("a_death_marks_the_record", 20, HerdBookGameTests::aDeathMarksTheRecord),
            new BreedingGameTests.Case("a_dead_mother_stays_for_her_calf", 20, HerdBookGameTests::aDeadMotherStaysForHerCalf),
            new BreedingGameTests.Case("unloading_refreshes_the_record", 20, HerdBookGameTests::unloadingRefreshesTheRecord),
            new BreedingGameTests.Case("records_refresh_while_loaded", 20, HerdBookGameTests::recordsRefreshWhileLoaded),
            new BreedingGameTests.Case("frozen_records_keep_their_values", 20, HerdBookGameTests::frozenRecordsKeepTheirValues),
            new BreedingGameTests.Case("herd_books_survive_save_and_load", 20, HerdBookGameTests::herdBooksSurviveSaveAndLoad),
            new BreedingGameTests.Case("show_sends_every_living_member", 20, HerdBookGameTests::showSendsEveryLivingMember),
            new BreedingGameTests.Case("unloaded_status_is_projected", 20, HerdBookGameTests::unloadedStatusIsProjected),
            new BreedingGameTests.Case("removal_needs_the_book_in_hand", 20, HerdBookGameTests::removalNeedsTheBookInHand),
            new BreedingGameTests.Case("show_book_survives_the_network_codec", 20, HerdBookGameTests::showBookSurvivesTheNetworkCodec),
            new BreedingGameTests.Case("a_big_book_fits_the_payload_limit", 20, HerdBookGameTests::aBigBookFitsThePayloadLimit),
            new BreedingGameTests.Case("failed_removal_sends_the_book", 20, HerdBookGameTests::failedRemovalSendsTheBook),
            new BreedingGameTests.Case("died_day_uses_the_overworld_clock", 20, HerdBookGameTests::diedDayUsesTheOverworldClock),
            new BreedingGameTests.Case("vanilla_birth_records_parents", 20, HerdBookGameTests::vanillaBirthRecordsParents),
            new BreedingGameTests.Case("save_refreshes_loaded_members", 20, HerdBookGameTests::saveRefreshesLoadedMembers),
            new BreedingGameTests.Case("member_order_survives_save", 20, HerdBookGameTests::memberOrderSurvivesSave),
            new BreedingGameTests.Case("herd_book_record_keeps_the_fresh_origin", 20, HerdBookGameTests::herdBookRecordKeepsTheFreshOrigin),
            new BreedingGameTests.Case("the_book_and_the_animal_agree", 20, HerdBookGameTests::theBookAndTheAnimalAgree),
            new BreedingGameTests.Case("a_book_page_shows_health_and_vigour", 20, HerdBookGameTests::aBookPageShowsHealthAndVigour),
            new BreedingGameTests.Case("an_old_record_without_a_sheet_loads", 20, HerdBookGameTests::anOldRecordWithoutASheetLoads),
            new BreedingGameTests.Case("the_egg_tip_says_what_she_laid_today", 20, HerdBookGameTests::theEggTipSaysWhatSheLaidToday),
            new BreedingGameTests.Case("using_the_book_on_a_member_focuses_it", 20, HerdBookGameTests::usingTheBookOnAMemberFocusesIt),
            new BreedingGameTests.Case("a_member_no_longer_familiar_still_opens", 20, HerdBookGameTests::aMemberNoLongerFamiliarStillOpens),
            new BreedingGameTests.Case("adding_an_animal_focuses_it", 20, HerdBookGameTests::addingAnAnimalFocusesIt),
            new BreedingGameTests.Case("an_unfamiliar_stranger_gets_no_focus", 20, HerdBookGameTests::anUnfamiliarStrangerGetsNoFocus),
            new BreedingGameTests.Case("an_animal_in_another_book_is_added_to_this_one", 20, HerdBookGameTests::anAnimalInAnotherBookIsAddedToThisOne)
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

    // A tamed cat sits or stands up when its owner right-clicks it; with the book in hand it joins the book instead.
    public static void tamedCatsJoinWithoutSitting(GameTestHelper helper) {
        TamableAnimal cat = (TamableAnimal) female(helper, EntityTypes.CAT);
        Player player = FoodGameTests.holding(helper, BarnyardItems.HERD_BOOK);
        cat.tame(player);
        boolean sitting = cat.isOrderedToSit();

        GameTestCompat.useOn(cat, player);

        helper.assertTrue(cat.isOrderedToSit() == sitting, "the cat sat or stood up instead of joining the book");
        UUID id = bookId(helper, player.getMainHandItem());
        helper.assertTrue(data(helper).ledger().isMember(id, cat.getUUID()), "the cat did not join the book");
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

    // A chunk unload reaches the loader's leave-level event before the entity has a removal reason,
    // so this fires the real event on a live cow, the way an unload does.
    public static void unloadEventRefreshesTheRecord(GameTestHelper helper, Consumer<Animal> fireUnload) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        Services.DATA.set(cow, traits(helper, cow).fed(1.0, 600L));

        fireUnload.accept(cow);

        HerdRecord record = data(helper).record(cow.getUUID());
        helper.assertTrue(record.traits().lastFedTick() == 600L, "the unload event left the record fed at " + record.traits().lastFedTick());
        helper.assertTrue(record.diedAt() < 0L, "an unloaded cow was marked dead");
        helper.succeed();
    }

    public static void recordsRefreshWhileLoaded(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        Services.DATA.set(cow, traits(helper, cow).fed(1.0, 700L));
        // Each member has its own tick in the minute, so a big herd isn't all refreshed in one tick.
        int slot = HerdBooks.refreshSlot(cow.getUUID());

        HerdBooks.tick(server(helper), HerdBooks.REFRESH_TICKS + slot + 1);
        helper.assertTrue(data(helper).record(cow.getUUID()).traits().lastFedTick() != 700L, "the timer refreshed a cow outside her tick");
        HerdBooks.tick(server(helper), HerdBooks.REFRESH_TICKS + slot);

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

    public static void showSendsEveryLivingMember(GameTestHelper helper) {
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        Animal first = female(helper, EntityTypes.COW);
        Animal second = male(helper, EntityTypes.COW);
        Animal third = female(helper, EntityTypes.PIG);
        HerdBooks.register(book, first);
        HerdBooks.register(book, second);
        HerdBooks.register(book, third);
        third.discard();

        List<HerdEntry> entries = HerdBooks.entries(server(helper), bookId(helper, book), helper.getLevel().getGameTime());

        helper.assertTrue(entries.size() == 2, "the book shows " + entries.size() + " animals, not 2");
        helper.assertTrue(entries.stream().allMatch(HerdEntry::alive), "a dead animal with no young was shown");
        HerdEntry cow = entries.stream().filter(entry -> entry.id().equals(first.getUUID())).findFirst().orElseThrow();
        helper.assertTrue(cow.female() && !cow.kid() && cow.species().equals("minecraft:cow"), "the cow's entry is " + cow);
        helper.assertTrue(!cow.lines().isEmpty(), "the cow's page has no lines");
        helper.succeed();
    }

    public static void unloadedStatusIsProjected(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        long now = helper.getLevel().getGameTime();
        Services.DATA.setPregnancy(mother, BreedingGameTests.pregnancy(helper, mother).withConceivedAt(now - 10_000L));
        HerdBooks.leftLevel(mother, Entity.RemovalReason.UNLOADED_TO_CHUNK);
        // The cow is still in the level, so this checks the projection from the record by moving time on instead.
        Pregnancy recorded = data(helper).record(mother.getUUID()).pregnancy();

        HerdEntry entry = HerdStatus.entry(data(helper).record(mother.getUUID()), data(helper).ledger().node(mother.getUUID()), now + 5_000L, now + 5_000L, false);

        int expected = PregnancyText.percent(PregnancyClock.progress(now + 5_000L, recorded));
        helper.assertTrue(entry.pregnancyPercent() == expected, "the projected pregnancy is " + entry.pregnancyPercent() + "%, not " + expected + "%");
        helper.succeed();
    }

    public static void removalNeedsTheBookInHand(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);
        UUID id = bookId(helper, book);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        HerdBookPayloads.RemoveFromBook request = new HerdBookPayloads.RemoveFromBook(id, cow.getUUID());

        helper.assertTrue(!HerdBooks.removeRequested(player, request), "a removal without the book in hand worked");
        helper.assertTrue(data(helper).ledger().isMember(id, cow.getUUID()), "the cow left without the book in hand");
        player.setItemInHand(InteractionHand.OFF_HAND, book);

        helper.assertTrue(HerdBooks.removeRequested(player, request), "a removal with the book in hand failed");
        helper.assertTrue(!data(helper).ledger().isMember(id, cow.getUUID()), "the cow is still in the book");
        helper.succeed();
    }

    public static void showBookSurvivesTheNetworkCodec(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);
        UUID id = bookId(helper, book);
        HerdBookPayloads.ShowBook sent = new HerdBookPayloads.ShowBook(Optional.of(id), HerdBooks.entries(server(helper), id, helper.getLevel().getGameTime()), false, Optional.of(cow.getUUID()));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());

        HerdBookPayloads.ShowBook.STREAM_CODEC.encode(buffer, sent);
        HerdBookPayloads.ShowBook received = HerdBookPayloads.ShowBook.STREAM_CODEC.decode(buffer);

        helper.assertTrue(received.equals(sent), "the book came back as " + received);
        helper.succeed();
    }

    // Books grow by themselves as calves are born, so a big farm's book is past vanilla's 1 MiB packet limit.
    public static void aBigBookFitsThePayloadLimit(GameTestHelper helper) {
        List<HerdEntry.Line> lines = List.of(
                new HerdEntry.Line(StatKind.SIZE, Component.translatable("gui.barnyard_romance.herd_book.size_age", "XL",
                        Component.translatable("gui.barnyard_romance.age.adult"), "17d"), 0.0F),
                new HerdEntry.Line(StatKind.FED, Component.translatable("gui.barnyard_romance.herd_book.fed_today"), 0.0F),
                new HerdEntry.Line(StatKind.PREGNANT, Component.translatable("gui.barnyard_romance.pregnancy.line", 61,
                        Component.translatable("gui.barnyard_romance.time.minutes_seconds", 21, 40)), 0.61F),
                new HerdEntry.Line(StatKind.FAMILIARITY, Component.translatable("gui.barnyard_romance.herd_book.familiarity",
                        Component.translatable("gui.barnyard_romance.percent", 80)), 0.8F),
                new HerdEntry.Line(StatKind.FERTILITY, Component.translatable("gui.barnyard_romance.herd_book.fertility",
                        Component.translatable("gui.barnyard_romance.percent", 90)), 0.9F),
                new HerdEntry.Line(StatKind.NOTE, Component.translatable("gui.barnyard_romance.herd_book.seen",
                        Component.translatable("gui.barnyard_romance.time.days", 12)), 0.0F));
        List<HerdEntry> entries = new java.util.ArrayList<>();
        for (int i = 0; i < 1_500; i++) {
            entries.add(new HerdEntry(UUID.randomUUID(), "minecraft:cow", "Daisy the " + i + "th", true, false, true, Familiar.FAMILIAR,
                    true, 61, false, ShearState.NONE, Optional.empty(), net.minecraft.network.chat.Component.empty(),
                    Optional.of(UUID.randomUUID()), Optional.of(UUID.randomUUID()), lines));
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());

        HerdBookPayloads.ShowBook.STREAM_CODEC.encode(buffer, new HerdBookPayloads.ShowBook(Optional.of(UUID.randomUUID()), entries, true, Optional.empty()));

        helper.assertTrue(buffer.readableBytes() > 1_048_576, "1,500 animals took only " + buffer.readableBytes() + " bytes");
        helper.assertTrue(buffer.readableBytes() < HerdBookPayloads.ShowBook.MAX_BYTES, "1,500 animals took " + buffer.readableBytes() + " bytes");
        helper.succeed();
    }

    // Whatever happened to the request, the player holding the book gets it back, so the page never goes stale.
    public static void failedRemovalSendsTheBook(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);
        UUID id = bookId(helper, book);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, book);
        List<ItemStack> replies = new java.util.ArrayList<>();
        HerdBookPayloads.RemoveFromBook stranger = new HerdBookPayloads.RemoveFromBook(id, UUID.randomUUID());

        helper.assertTrue(!HerdBooks.removeRequested(player, stranger, replies::add), "removing an animal that is not in the book worked");

        helper.assertTrue(replies.size() == 1 && replies.getFirst() == book, "a failed removal sent " + replies.size() + " books back");
        helper.succeed();
    }

    // The overworld clock is not game time: sleeping and /time move it on, so a death's day comes from the clock.
    public static void diedDayUsesTheOverworldClock(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);
        breed(helper, cow, father);

        cow.discard();

        HerdRecord record = data(helper).record(cow.getUUID());
        long clockNow = helper.getLevel().getOverworldClockTime();
        helper.assertTrue(record != null && record.diedClock() == clockNow, "the dead cow's clock is " + (record == null ? null : record.diedClock()));
        // Setting the clock differs between versions, so the record is built with a clock five days ahead of game time.
        long clock = 5L * GameDay.TICKS + 1_000L;
        HerdRecord ahead = record.died(100L, clock);
        HerdEntry entry = HerdStatus.entry(ahead, data(helper).ledger().node(cow.getUUID()), 200L, clock, false);
        String died = entry.lines().stream().filter(line -> line.kind() == StatKind.NOTE).findFirst().orElseThrow().text().getString();
        helper.assertTrue(died.contains(Long.toString(GameDay.of(clock))), "the died line is " + died);
        helper.succeed();
    }

    // With pregnancy off, calves come from vanilla breeding; they still remember their parents and join the book.
    public static void vanillaBirthRecordsParents(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        UUID id = bookId(helper, book);
        BarnyardConfig before = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"pregnancyEnabled\": false}"));
            father.setInLove(null);
            mother.setInLove(null);
            father.spawnChildFromBreeding(helper.getLevel(), mother);
        } finally {
            BarnyardConfig.set(before);
        }

        Animal calf = helper.getLevel().getEntitiesOfClass(Animal.class, mother.getBoundingBox().inflate(8.0), Animal::isBaby).stream()
                .findFirst().orElseThrow(() -> GameTestCompat.failure(helper, "vanilla breeding made no calf"));
        Lineage lineage = traits(helper, calf).lineage();
        helper.assertTrue(mother.getUUID().equals(lineage.mother()) && father.getUUID().equals(lineage.father()), "the calf's lineage is " + lineage);
        helper.assertTrue(data(helper).ledger().isMember(id, calf.getUUID()), "the calf did not join its mother's book");
        helper.succeed();
    }

    public static void saveRefreshesLoadedMembers(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        Services.DATA.set(cow, traits(helper, cow).fed(1.0, 800L));

        HerdBooks.refreshLoaded(server(helper));

        helper.assertTrue(data(helper).record(cow.getUUID()).traits().lastFedTick() == 800L, "refreshing on save left the record behind");
        helper.succeed();
    }

    public static void memberOrderSurvivesSave(GameTestHelper helper) {
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        List<UUID> order = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Animal cow = i % 2 == 0 ? female(helper, EntityTypes.COW) : male(helper, EntityTypes.COW);
            HerdBooks.register(book, cow);
            order.add(cow.getUUID());
        }
        UUID id = bookId(helper, book);

        Tag saved = HerdBookData.CODEC.encodeStart(NbtOps.INSTANCE, data(helper)).getOrThrow();
        HerdBookData loaded = HerdBookData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();

        helper.assertTrue(List.copyOf(loaded.ledger().members(id)).equals(order), "the members came back as " + loaded.ledger().members(id));
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

    // A record frozen below the threshold keeps its old values, but the origin comes from the animal as it is now.
    public static void herdBookRecordKeepsTheFreshOrigin(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Services.DATA.set(cow, traits(helper, cow).fed(0.5, -1L));
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        HerdRecord record = data(helper).record(cow.getUUID());
        HerdRecord old = withTraits(record, record.traits().fed(0.5, 100L).withOrigin(Origin.UNKNOWN));
        HerdRecord fresh = withTraits(record, record.traits().fed(0.2, 200L).withOrigin(Origin.WILD));

        HerdRecord kept = HerdRecords.refreshed(old, fresh);

        helper.assertTrue(kept.traits().lastFedTick() == 100L, "the frozen record took the fresh feeding time");
        helper.assertTrue(kept.traits().origin() == Origin.WILD, "the frozen record kept origin " + kept.traits().origin());
        helper.succeed();
    }

    // The book works a record out the way the animal works itself out, so a page reads the same either way.
    public static void theBookAndTheAnimalAgree(GameTestHelper helper) {
        Animal bull = male(helper, EntityTypes.COW);
        Animal cow = female(helper, EntityTypes.COW);
        long clock = clock(10L) + 2_000L;
        FamiliarityCare.markEaten(bull, clock - 1_000L);
        cow.setInLove(null);
        bull.setInLove(null);
        cow.spawnChildFromBreeding(helper.getLevel(), bull);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, bull);
        HerdBooks.register(book, cow);
        // A wild-caught horse that was never tamed: its cap is the pet one, on the card and in the book alike.
        Animal horse = female(helper, EntityTypes.HORSE);
        Services.DATA.set(horse, traits(helper, horse).fed(0.5, -1L).withOrigin(Origin.WILD));
        HerdBooks.register(book, horse);
        long now = helper.getLevel().getGameTime();

        for (Animal animal : List.of(bull, cow, horse)) {
            List<String> live = describe(StatSheet.lines(LiveFacts.of(animal, now, clock).orElseThrow()));
            HerdRecord record = data(helper).record(animal.getUUID());
            List<String> recorded = describe(StatSheet.lines(HerdStatus.facts(record, data(helper).ledger().node(animal.getUUID()), now, clock, true)));
            helper.assertTrue(live.equals(recorded), "the animal reads " + live + " but the book reads " + recorded);
        }
        helper.assertTrue(describe(StatSheet.lines(LiveFacts.of(cow, now, clock).orElseThrow())).stream().anyMatch(line -> line.startsWith("PREGNANT")),
                "the cow is not pregnant");
        helper.assertTrue(describe(StatSheet.lines(LiveFacts.of(horse, now, clock).orElseThrow())).stream().anyMatch(line -> line.contains("80%")),
                "the wild horse's cap is not 80%");
        helper.succeed();
    }

    public static void aBookPageShowsHealthAndVigour(GameTestHelper helper) {
        Animal bull = male(helper, EntityTypes.COW);
        long now = helper.getLevel().getGameTime();
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), bull);

        HerdEntry entry = HerdStatus.entry(data(helper).record(bull.getUUID()), data(helper).ledger().node(bull.getUUID()), now, clock(10L), true);

        List<String> lines = entry.lines().stream().map(line -> line.kind() + ":" + line.text().getString()).toList();
        helper.assertTrue(lines.contains("HEALTH:Health 10/10"), "the page reads " + lines);
        helper.assertTrue(lines.stream().anyMatch(line -> line.startsWith("VIGOUR:")), "the page has no Vigour: " + lines);
        helper.succeed();
    }

    // A record saved before the sheet was kept has no sheet field; it loads and shows no Health row.
    public static void anOldRecordWithoutASheetLoads(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        Tag saved = HerdBookData.CODEC.encodeStart(NbtOps.INSTANCE, data(helper)).getOrThrow();
        Dynamic<Tag> old = new Dynamic<>(NbtOps.INSTANCE, saved).update("animals", animals ->
                animals.createList(animals.asStream().map(entry -> entry.update("record", record -> record.remove("sheet")))));

        HerdBookData loaded = HerdBookData.CODEC.parse(NbtOps.INSTANCE, old.getValue()).getOrThrow();

        HerdRecord record = loaded.record(cow.getUUID());
        helper.assertTrue(record != null && record.sheet().equals(RecordSheet.NONE), "the old record has sheet " + (record == null ? null : record.sheet()));
        List<StatKind> kinds = StatSheet.lines(HerdStatus.facts(record, loaded.ledger().node(cow.getUUID()), helper.getLevel().getGameTime(), clock(10L), false))
                .stream().map(StatLine::kind).toList();
        helper.assertTrue(!kinds.contains(StatKind.HEALTH), "an old record shows health: " + kinds);
        helper.succeed();
    }

    public static void theEggTipSaysWhatSheLaidToday(GameTestHelper helper) {
        Animal hen = EggGameTests.chicken(helper, Sex.FEMALE, EggGameTests.MEDIUM_HEN, 0.5, 3);
        FamiliarityCare.markEaten(hen);
        Services.DATA.setLaying(hen, new Laying(GameDay.of(helper.getLevel().getOverworldClockTime()), List.of(5_000, 9_000), 1, 1));

        HerdEntry entry = EggGameTests.bookEntry(helper, hen);

        helper.assertTrue(entry.eggTip().getString().endsWith("Eggs: 1/2 today"), "the egg tip reads " + entry.eggTip().getString());
        helper.assertTrue(entry.lines().stream().anyMatch(line -> line.kind() == StatKind.DETAIL && line.text().getString().equals("Eggs: 1/2 today")),
                "the page has no egg note: " + entry.lines().stream().map(line -> line.text().getString()).toList());
        helper.succeed();
    }

    public static void usingTheBookOnAMemberFocusesIt(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);

        HerdBooks.Use use = HerdBooks.use(book, cow);

        helper.assertTrue(use.result() == HerdBooks.RegisterResult.ALREADY, "a member was " + use.result());
        helper.assertTrue(use.focus().equals(Optional.of(cow.getUUID())), "the focus was " + use.focus());
        helper.succeed();
    }

    public static void aMemberNoLongerFamiliarStillOpens(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, cow);
        Services.DATA.set(cow, traits(helper, cow).fed(0.1, -1L));

        HerdBooks.Use use = HerdBooks.use(book, cow);

        helper.assertTrue(use.result() == HerdBooks.RegisterResult.ALREADY, "a decayed member was " + use.result());
        helper.assertTrue(use.focus().equals(Optional.of(cow.getUUID())), "the focus was " + use.focus());
        helper.succeed();
    }

    public static void addingAnAnimalFocusesIt(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);

        HerdBooks.Use use = HerdBooks.use(new ItemStack(BarnyardItems.HERD_BOOK), cow);

        helper.assertTrue(use.result() == HerdBooks.RegisterResult.ADDED, "a familiar cow was " + use.result());
        helper.assertTrue(use.focus().equals(Optional.of(cow.getUUID())), "the focus was " + use.focus());
        helper.succeed();
    }

    public static void anUnfamiliarStrangerGetsNoFocus(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Services.DATA.set(cow, traits(helper, cow).fed(0.1, -1L));

        HerdBooks.Use use = HerdBooks.use(new ItemStack(BarnyardItems.HERD_BOOK), cow);

        helper.assertTrue(use.result() == HerdBooks.RegisterResult.NOT_FAMILIAR, "a stranger was " + use.result());
        helper.assertTrue(use.focus().isEmpty(), "the focus was " + use.focus());
        helper.succeed();
    }

    // Membership is per book: a cow in book A is new to book B.
    public static void anAnimalInAnotherBookIsAddedToThisOne(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        ItemStack bookA = new ItemStack(BarnyardItems.HERD_BOOK);
        ItemStack bookB = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(bookA, cow);

        HerdBooks.Use use = HerdBooks.use(bookB, cow);

        helper.assertTrue(use.result() == HerdBooks.RegisterResult.ADDED, "a cow from another book was " + use.result());
        helper.assertTrue(data(helper).ledger().members(bookId(helper, bookB)).contains(cow.getUUID()), "the second book does not hold her");
        helper.succeed();
    }

    private static List<String> describe(List<StatLine> lines) {
        return lines.stream().map(line -> line.kind() + ":" + TextComponents.of(line.text()).getString() + ":" + line.fill()).toList();
    }

    private static HerdRecord withTraits(HerdRecord record, AnimalTraits traits) {
        return new HerdRecord(record.species(), record.name(), traits, record.pregnancy(), record.baby(), record.milkable(), record.shearable(),
                record.woolly(), record.refreshedAt(), record.diedAt(), record.diedClock(), record.tamed(), record.nest());
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
