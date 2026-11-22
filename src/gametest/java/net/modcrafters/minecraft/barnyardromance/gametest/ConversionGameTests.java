package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
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
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;

import java.util.List;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;

// In-game checks for an animal that turns into another animal and keeps its record.
public final class ConversionGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_sheared_mooshroom_becomes_a_cow_with_its_record", 20, ConversionGameTests::aShearedMooshroomBecomesACowWithItsRecord)
    );

    private ConversionGameTests() {
    }

    public static void aShearedMooshroomBecomesACowWithItsRecord(GameTestHelper helper) {
        Animal mooshroom = CareGameTests.withFamiliarity(helper, female(helper, EntityTypes.MOOSHROOM), 0.8);
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(mooshroom, PregnancyFeature.PREGNANCY,
                Pregnancy.builder(now, 66_000L, "minecraft:mooshroom", 1.35).motherFamiliarity(1.0).build());
        AnimalTraits before = traits(helper, mooshroom);
        ItemStack book = new ItemStack(HerdItems.HERD_BOOK);
        HerdBooks.register(book, mooshroom);
        UUID bookId = book.get(HerdComponents.HERD_BOOK);
        UUID oldId = mooshroom.getUUID();
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));

        GameTestCompat.useOn(mooshroom, player);

        helper.assertTrue(mooshroom.isRemoved(), "the mooshroom was not sheared");
        List<Animal> cows = helper.getLevel().getEntitiesOfClass(Animal.class, mooshroom.getBoundingBox().inflate(4.0),
                animal -> animal.getType() == EntityTypes.COW);
        helper.assertTrue(cows.size() == 1, "the shearing left " + cows.size() + " cows");
        Animal cow = cows.get(0);
        AnimalTraits after = traits(helper, cow);
        helper.assertTrue(after.sex() == before.sex(), "the cow is " + after.sex() + ", the mooshroom was " + before.sex());
        helper.assertTrue(after.scale() == before.scale(), "the cow is size " + after.scale() + ", the mooshroom was " + before.scale());
        double floor = before.get(CARE).familiarity() - BarnyardConfig.current().tuning().familiarityShearingCost();
        helper.assertTrue(after.get(CARE).familiarity() >= floor - 1.0e-9, "the cow has familiarity " + after.get(CARE).familiarity() + ", under " + floor);
        helper.assertTrue(Services.DATA.get(cow, PregnancyFeature.PREGNANCY).isPresent(), "the cow lost the pregnancy");

        HerdBookData data = HerdBookData.get(helper.getLevel().getServer());
        HerdLedger.Node node = data.ledger().node(cow.getUUID());
        helper.assertTrue(node != null && node.alive(), "the book does not list the cow alive: " + node);
        helper.assertTrue(data.ledger().isMember(bookId, cow.getUUID()), "the cow is not in the book");
        helper.assertTrue(data.ledger().node(oldId) == null && !data.ledger().isMember(bookId, oldId), "the book still lists the mooshroom");
        HerdRecord record = data.record(cow.getUUID());
        helper.assertTrue(record != null && record.diedAt() < 0L, "the cow's record is " + record);
        helper.assertTrue(record.species().equals("minecraft:cow"), "the cow's record is for " + record.species());
        helper.assertTrue(data.record(oldId) == null, "the mooshroom's record is still kept");
        helper.assertTrue(Conversions.successor(mooshroom).orElse(null) == cow, "the mooshroom's successor is not the cow");
        helper.succeed();
    }
}
