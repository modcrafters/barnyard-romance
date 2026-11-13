package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsFeature;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatKind;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatLine;
import net.modcrafters.minecraft.barnyardromance.stats.StatSheet;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.gametest.VigourGameTests.withVigour;
import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;

// In-game checks that the stat sheet reads a loaded animal correctly.
public final class StatGameTests {
    private static final long DAY = 10L;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_fed_grown_bull_shows_his_vigour", 20, StatGameTests::aFedGrownBullShowsHisVigour),
            new BreedingGameTests.Case("a_hungry_bull_notes_the_health_cost", 20, StatGameTests::aHungryBullNotesTheHealthCost),
            new BreedingGameTests.Case("a_wild_caught_cow_notes_her_cap", 20, StatGameTests::aWildCaughtCowNotesHerCap),
            new BreedingGameTests.Case("a_laying_hen_counts_todays_eggs", 20, StatGameTests::aLayingHenCountsTodaysEggs),
            new BreedingGameTests.Case("a_sheep_says_whether_it_can_be_shorn", 20, StatGameTests::aSheepSaysWhetherItCanBeShorn),
            new BreedingGameTests.Case("an_animal_without_traits_has_no_facts", 20, StatGameTests::anAnimalWithoutTraitsHasNoFacts)
    );

    private StatGameTests() {
    }

    public static void aFedGrownBullShowsHisVigour(GameTestHelper helper) {
        Animal bull = withVigour(helper, male(helper, EntityTypes.COW), 0.6, DAY - 1L);
        long clock = clock(DAY) + 2_000L;
        FamiliarityCare.markEaten(bull, clock(DAY) + 1_000L);

        List<StatLine> lines = lines(helper, bull, clock);

        helper.assertTrue(kinds(lines).contains(StatKind.VIGOUR), "a grown bull shows no Vigour: " + kinds(lines));
        helper.assertTrue(!kinds(lines).contains(StatKind.FERTILITY), "a bull shows fertility: " + kinds(lines));
        helper.assertTrue(text(lines, StatKind.VIGOUR).equals("Vigour 60%"), "his Vigour reads " + text(lines, StatKind.VIGOUR));
        helper.succeed();
    }

    public static void aHungryBullNotesTheHealthCost(GameTestHelper helper) {
        Animal bull = withVigour(helper, male(helper, EntityTypes.COW), 1.0, DAY - 2L);

        List<StatLine> lines = lines(helper, bull, clock(DAY) + 1_000L);

        int at = kinds(lines).indexOf(StatKind.VIGOUR);
        helper.assertTrue(at >= 0 && at + 1 < lines.size(), "a hungry bull shows no Vigour: " + kinds(lines));
        helper.assertTrue(lines.get(at + 1).kind() == StatKind.DETAIL, "no detail follows his Vigour: " + kinds(lines));
        helper.assertTrue(string(lines.get(at + 1)).equals("Hungry: mating costs health"), "the note reads " + string(lines.get(at + 1)));
        helper.succeed();
    }

    public static void aWildCaughtCowNotesHerCap(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Services.DATA.set(cow, traits(helper, cow).with(CARE, traits(helper, cow).get(CARE).withOrigin(Origin.WILD)));

        List<StatLine> lines = lines(helper, cow, clock(DAY));

        helper.assertTrue(lines.stream().anyMatch(line -> line.kind() == StatKind.DETAIL && string(line).equals("Wild: max 40%")),
                "a wild-caught cow has no cap note: " + lines.stream().map(StatGameTests::string).toList());
        helper.succeed();
    }

    public static void aLayingHenCountsTodaysEggs(GameTestHelper helper) {
        Animal hen = EggGameTests.chicken(helper, Sex.FEMALE, EggGameTests.mediumHen(), 0.5, 3);
        long clock = clock(DAY) + 2_000L;
        Services.DATA.set(hen, EggsFeature.LAYING, new Laying(GameDay.of(clock), List.of(5_000, 9_000), 1, 1));

        List<StatLine> lines = lines(helper, hen, clock);

        List<StatLine> eggs = lines.stream().filter(line -> line.kind() == StatKind.EGGS).toList();
        helper.assertTrue(eggs.size() == 1, "a hen has " + eggs.size() + " egg lines: " + kinds(lines));
        helper.assertTrue(string(eggs.get(0)).equals("Eggs: 1/2 today"), "her egg line reads " + string(eggs.get(0)));
        helper.succeed();
    }

    public static void aSheepSaysWhetherItCanBeShorn(GameTestHelper helper) {
        Animal sheep = female(helper, EntityTypes.SHEEP);

        List<StatLine> lines = lines(helper, sheep, clock(DAY));

        helper.assertTrue(kinds(lines).contains(StatKind.SHEAR), "a sheep has no shear line: " + kinds(lines));
        helper.assertTrue(text(lines, StatKind.SHEAR).equals("Ready to shear"), "her shear line reads " + text(lines, StatKind.SHEAR));
        helper.succeed();
    }

    public static void anAnimalWithoutTraitsHasNoFacts(GameTestHelper helper) {
        Animal horse = GameTestCompat.unspawned(helper, EntityTypes.COW);

        helper.assertTrue(Services.DATA.get(horse).isEmpty(), "a cow that never joined has traits");
        helper.assertTrue(LiveFacts.of(horse, helper.getLevel().getGameTime(), clock(DAY)).isEmpty(), "a cow that never joined has facts");
        helper.succeed();
    }

    private static List<StatLine> lines(GameTestHelper helper, Animal animal, long clock) {
        return StatSheet.lines(LiveFacts.of(animal, helper.getLevel().getGameTime(), clock).orElseThrow());
    }

    private static List<StatKind> kinds(List<StatLine> lines) {
        return lines.stream().map(StatLine::kind).toList();
    }

    private static String string(StatLine line) {
        return TextComponents.of(line.text()).getString();
    }

    private static String text(List<StatLine> lines, StatKind kind) {
        return lines.stream().filter(line -> line.kind() == kind).findFirst().map(StatGameTests::string).orElse("");
    }
}
