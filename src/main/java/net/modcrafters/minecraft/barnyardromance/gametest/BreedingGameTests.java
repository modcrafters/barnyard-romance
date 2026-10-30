package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCare;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyController;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

// In-game checks for breeding, run headless by each loader's game test server. Only registered when game tests are on.
public final class BreedingGameTests {
    public static final Identifier PEN = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "gametest/pen");
    public static final int VANILLA_REST = 6000;

    public record Case(String name, int maxTicks, Consumer<GameTestHelper> body) {
    }

    public static final List<Case> CASES = List.of(
            new Case("excluded_species_keep_their_traits", 20, BreedingGameTests::excludedSpeciesKeepTheirTraits),
            new Case("excluded_species_cannot_join_a_book", 20, BreedingGameTests::excludedSpeciesCannotJoinABook),
            new Case("excluded_members_are_not_refreshed", 20, BreedingGameTests::excludedMembersAreNotRefreshed),
            new Case("pigs_conceive_a_litter_and_rest", 200, BreedingGameTests::pigsConceiveALitterAndRest),
            new Case("cows_carry_one_calf_for_about_three_days", 20, BreedingGameTests::cowsCarryOneCalfForAboutThreeDays),
            new Case("time_skip_moves_pregnancies_on", 20, BreedingGameTests::timeSkipMovesPregnanciesOn),
            new Case("same_sex_pairs_make_nothing", 20, BreedingGameTests::sameSexPairsMakeNothing),
            new Case("unfamiliar_animals_do_not_fall_in_love", 20, BreedingGameTests::unfamiliarAnimalsDoNotFallInLove),
            new Case("hurting_an_animal_ends_its_love", 20, BreedingGameTests::hurtingAnAnimalEndsItsLove),
            new Case("pigs_in_love_breed_on_their_own", 400, BreedingGameTests::pigsInLoveBreedOnTheirOwn),
            new Case("worn_out_mothers_rest_without_conceiving", 20, BreedingGameTests::wornOutMothersRestWithoutConceiving),
            new Case("a_birth_costs_the_mother_fertility", 200, BreedingGameTests::aBirthCostsTheMotherFertility),
            new Case("young_animals_do_not_fall_in_love", 20, BreedingGameTests::youngAnimalsDoNotFallInLove),
            new Case("old_animals_die_of_old_age", 20, BreedingGameTests::oldAnimalsDieOfOldAge),
            new Case("animals_of_unknown_age_never_die", 20, BreedingGameTests::animalsOfUnknownAgeNeverDie),
            new Case("traits_survive_save_and_load", 20, BreedingGameTests::traitsSurviveSaveAndLoad),
            new Case("calves_remember_both_parents", 20, BreedingGameTests::calvesRememberBothParents)
    );

    private BreedingGameTests() {
    }

    public static void pigsConceiveALitterAndRest(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        long now = helper.getLevel().getGameTime();
        mother.setInLove(null);
        father.setInLove(null);
        helper.assertTrue(traits(helper, mother).loveUntil() == now + mother.getInLoveTime(), "love mode was not recorded on the mother");

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Pregnancy pregnancy = pregnancy(helper, mother);
        helper.assertTrue(pregnancy.litterSize() >= 2 && pregnancy.litterSize() <= 4, "pig litter " + pregnancy.litterSize() + " is outside 2-4");
        helper.assertTrue(pregnancy.durationTicks() >= 30_000 && pregnancy.durationTicks() <= 36_000,
                "pig pregnancy " + pregnancy.durationTicks() + " is outside 30000-36000");
        helper.assertTrue(pregnancy.litter().size() == pregnancy.litterSize(), "the stored litter does not match the litter size");
        helper.assertTrue(Services.DATA.getPregnancy(father).isEmpty(), "the father became pregnant");
        assertResting(helper, mother, now);
        assertResting(helper, father, now);
        helper.assertTrue(!helper.getEntities(EntityTypes.EXPERIENCE_ORB).isEmpty(), "breeding gave no XP");

        int litterSize = pregnancy.litterSize();
        Services.DATA.setPregnancy(mother, pregnancy.withConceivedAt(now - pregnancy.durationTicks()));
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the mother is still pregnant");
            List<? extends Animal> babies = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).toList();
            helper.assertTrue(babies.size() == litterSize, "expected " + litterSize + " piglets, found " + babies.size());
            for (Animal baby : babies) {
                helper.assertTrue(Services.DATA.get(baby).isPresent(), "a piglet was born without traits");
            }
        });
    }

    public static void cowsCarryOneCalfForAboutThreeDays(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        mother.setInLove(null);
        father.setInLove(null);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Pregnancy pregnancy = pregnancy(helper, mother);
        helper.assertTrue(pregnancy.litterSize() == 1, "cow litter " + pregnancy.litterSize() + " is not 1");
        helper.assertTrue(pregnancy.durationTicks() >= 60_000 && pregnancy.durationTicks() <= 72_000,
                "cow pregnancy " + pregnancy.durationTicks() + " is outside 60000-72000");
        helper.succeed();
    }

    public static void sameSexPairsMakeNothing(GameTestHelper helper) {
        Animal first = male(helper, EntityTypes.COW);
        Animal second = male(helper, EntityTypes.COW);
        first.setInLove(null);
        second.setInLove(null);

        first.spawnChildFromBreeding(helper.getLevel(), second);

        helper.assertTrue(Services.DATA.getPregnancy(first).isEmpty() && Services.DATA.getPregnancy(second).isEmpty(), "a same-sex pair conceived");
        helper.assertTrue(!first.isInLove() && !second.isInLove(), "a same-sex pair stayed in love");
        helper.assertTrue(traits(helper, first).loveUntil() < 0L && traits(helper, second).loveUntil() < 0L, "love was not cleared on a same-sex pair");
        helper.assertTrue(helper.getEntities(EntityTypes.EXPERIENCE_ORB).isEmpty(), "a same-sex pair gave XP");
        helper.succeed();
    }

    public static void unfamiliarAnimalsDoNotFallInLove(GameTestHelper helper) {
        Animal cow = GameTestCompat.spawn(helper, EntityTypes.COW, 4, 1, 4);
        Services.DATA.set(cow, new AnimalTraits(Sex.FEMALE, 1.0, 0.0, -1L));

        cow.setInLove(null);

        helper.assertTrue(!cow.isInLove(), "an unfamiliar cow fell in love");
        helper.assertTrue(traits(helper, cow).loveUntil() < 0L, "love was recorded on an unfamiliar cow");
        helper.succeed();
    }

    public static void hurtingAnAnimalEndsItsLove(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        cow.setInLove(null);
        helper.assertTrue(traits(helper, cow).loveUntil() > helper.getLevel().getGameTime(), "love mode was not recorded");

        //? if >=1.21.2 {
        cow.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1.0F);
        //?} else
        //cow.hurt(helper.getLevel().damageSources().generic(), 1.0F);

        helper.assertTrue(traits(helper, cow).loveUntil() < 0L, "love was not cleared after the cow was hurt");
        helper.succeed();
    }

    public static void pigsInLoveBreedOnTheirOwn(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isPresent(), "the pigs have not bred yet");
            helper.assertTrue(traits(helper, mother).cooldownUntil() > helper.getLevel().getGameTime(), "the mother is not resting");
        });
    }

    // The father starts the mating, so the worn-out check has to find the mother on the other side.
    public static void wornOutMothersRestWithoutConceiving(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(mother, traits(helper, mother).withFertility(0.1, now));
        mother.setInLove(null);
        father.setInLove(null);

        father.spawnChildFromBreeding(helper.getLevel(), mother);

        helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "a worn-out mother conceived");
        helper.assertTrue(helper.getEntities(EntityTypes.EXPERIENCE_ORB).isEmpty(), "a worn-out mating gave XP");
        assertResting(helper, mother, now);
        assertResting(helper, father, now);
        helper.succeed();
    }

    public static void aBirthCostsTheMotherFertility(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        long now = helper.getLevel().getGameTime();
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        Pregnancy pregnancy = pregnancy(helper, mother);

        Services.DATA.setPregnancy(mother, pregnancy.withConceivedAt(now - pregnancy.durationTicks()));
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the mother is still pregnant");
            AnimalTraits traits = traits(helper, mother);
            helper.assertTrue(Math.abs(traits.fertility() - 0.75) < 1.0e-6, "the mother's fertility is " + traits.fertility() + ", not 0.75");
            helper.assertTrue(traits.fertilityTick() >= now, "the birth was not dated");
            List<? extends Animal> babies = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).toList();
            helper.assertTrue(!babies.isEmpty(), "no piglets were born");
            for (Animal baby : babies) {
                AnimalTraits child = traits(helper, baby);
                helper.assertTrue(child.fertility() == 1.0, "a piglet was born with fertility " + child.fertility());
                helper.assertTrue(child.weight() >= 0.0 && child.weight() <= 1.0, "a piglet was born with weight " + child.weight());
                helper.assertTrue(child.bornTick() >= now, "a piglet was born with birth tick " + child.bornTick());
            }
        });
    }

    // Born this tick and already grown (spawned as an adult), so it is young for its growth time plus a day.
    public static void youngAnimalsDoNotFallInLove(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Services.DATA.set(cow, traits(helper, cow).withBornTick(helper.getLevel().getGameTime()));
        helper.assertTrue(AgeController.stage(cow) == AgeStage.YOUNG, "the cow is " + AgeController.stage(cow) + ", not young");

        cow.setInLove(null);

        helper.assertTrue(!cow.isInLove(), "a young cow fell in love");
        helper.assertTrue(traits(helper, cow).loveUntil() < 0L, "love was recorded on a young cow");
        helper.succeed();
    }

    // The game clock is only a few ticks in, so the check is asked about a tick far ahead instead of waiting.
    public static void oldAnimalsDieOfOldAge(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(cow, traits(helper, cow).withBornTick(now));
        long deathAfter = AgeSettings.DEFAULTS.deathAfterTicks();

        helper.assertTrue(!AgeController.checkDeath(cow, helper.getLevel(), now + deathAfter / 2), "a middle-aged cow died");
        helper.assertTrue(cow.isAlive(), "a middle-aged cow is dead");

        helper.assertTrue(AgeController.checkDeath(cow, helper.getLevel(), now + deathAfter * 2), "an ancient cow did not die");
        helper.assertTrue(!cow.isAlive(), "an ancient cow is still alive");
        DamageSource cause = cow.getLastDamageSource();
        helper.assertTrue(cause != null && cause.is(AgeController.OLD_AGE), "the cow died of " + cause + ", not old age");
        helper.succeed();
    }

    public static void animalsOfUnknownAgeNeverDie(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        helper.assertTrue(traits(helper, cow).bornTick() < 0L, "a test cow has a birth tick");

        helper.assertTrue(!AgeController.checkDeath(cow, helper.getLevel(), Long.MAX_VALUE / 2), "a cow of unknown age died");
        helper.assertTrue(cow.isAlive(), "a cow of unknown age is dead");
        helper.succeed();
    }

    // What the loader's data attachments write into the entity's save comes back when it loads.
    public static void traitsSurviveSaveAndLoad(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Lineage lineage = new Lineage(UUID.randomUUID(), UUID.randomUUID());
        Services.DATA.set(cow, new AnimalTraits(Sex.FEMALE, 1.2, 0.75, 42L).withLineage(lineage));
        Pregnancy pregnancy = new Pregnancy(100L, 72_000L, "minecraft:cow", 1.1, 2, 0.75, 24_000, 0.6, List.of(), PregnancyCare.NONE);
        Services.DATA.setPregnancy(cow, pregnancy);

        Animal loaded = GameTestCompat.reloaded(helper, cow);

        AnimalTraits traits = traits(helper, loaded);
        helper.assertTrue(traits.sex() == Sex.FEMALE && traits.scale() == 1.2, "the loaded cow is " + traits);
        helper.assertTrue(traits.familiarity() == 0.75 && traits.lastFedTick() == 42L, "the loaded cow forgot its feeding: " + traits);
        helper.assertTrue(traits.lineage().equals(lineage), "the loaded cow forgot its parents: " + traits.lineage());
        Pregnancy carried = pregnancy(helper, loaded);
        helper.assertTrue(carried.conceivedAt() == 100L && carried.durationTicks() == 72_000L && carried.litterSize() == 2,
                "the loaded pregnancy is " + carried);
        helper.succeed();
    }

    public static void calvesRememberBothParents(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Animal calf = forceBirth(helper, mother).getFirst();

        Lineage lineage = traits(helper, calf).lineage();
        helper.assertTrue(mother.getUUID().equals(lineage.mother()), "the calf's mother is " + lineage.mother());
        helper.assertTrue(father.getUUID().equals(lineage.father()), "the calf's father is " + lineage.father());
        helper.succeed();
    }

    // /time add and sleeping move the overworld clock without the game time; a pregnancy counts that time too, and
    // putting the clock back doesn't take it away again. The clock is put back within the tick so the other tests'
    // days stay where they were.
    public static void timeSkipMovesPregnanciesOn(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        long now = helper.getLevel().getGameTime();
        Services.DATA.setPregnancy(cow, new Pregnancy(now, 66_000L, "minecraft:cow", 1.0, 1, 0.5));
        PregnancyController.catchUp(cow, helper.getLevel());
        long before = helper.getLevel().getOverworldClockTime();
        try {
            GameTestCompat.setOverworldClock(helper, before + 24_000L);
            PregnancyController.catchUp(cow, helper.getLevel());
        } finally {
            GameTestCompat.setOverworldClock(helper, before);
        }
        PregnancyController.catchUp(cow, helper.getLevel());

        long remaining = PregnancyClock.remainingTicks(now, pregnancy(helper, cow));
        helper.assertTrue(remaining == 66_000L - 24_000L, "a day went by and " + remaining + " ticks are left of 66000");
        helper.succeed();
    }

    // Ends the mother's pregnancy now and returns the young that were born.
    public static List<Animal> forceBirth(GameTestHelper helper, Animal mother) {
        Pregnancy pregnancy = pregnancy(helper, mother);
        Services.DATA.setPregnancy(mother, pregnancy.withConceivedAt(helper.getLevel().getGameTime() - pregnancy.durationTicks()));
        List<Animal> before = List.copyOf(helper.getEntities(mother.getType()).stream().map(Animal.class::cast).toList());
        PregnancyController.catchUp(mother, helper.getLevel());
        List<Animal> young = helper.getEntities(mother.getType()).stream()
                .map(Animal.class::cast)
                .filter(animal -> !before.contains(animal))
                .toList();
        helper.assertTrue(!young.isEmpty(), "no young were born");
        return young;
    }

    // Excluding a species makes it vanilla without wiping what the mod saved; switching it back on brings it all back.
    public static void excludedSpeciesKeepTheirTraits(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        AnimalTraits before = traits(helper, cow).fed(0.7, -1L);
        Services.DATA.set(cow, before);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:cow\"]}"));
            AnimalSex.onJoin(cow);
            helper.assertTrue(AgeController.deathAt(cow) == Long.MAX_VALUE, "an excluded cow still ages");
            helper.assertTrue(!FamiliarityActions.denies(cow), "an excluded cow still asks for familiarity");
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(traits(helper, cow).equals(before), "the cow came back as " + traits(helper, cow));
        helper.succeed();
    }

    public static void excludedSpeciesCannotJoinABook(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        BarnyardConfig config = BarnyardConfig.current();
        HerdBooks.RegisterResult result;
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:*\"]}"));
            result = HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(result == HerdBooks.RegisterResult.NOT_TRACKED, "an excluded cow was " + result);
        helper.succeed();
    }

    // A member whose species is excluded later stays in the book as it was last recorded.
    public static void excludedMembersAreNotRefreshed(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        HerdBookData data = HerdBookData.get(helper.getLevel().getServer());
        long recorded = data.record(cow.getUUID()).traits().lastFedTick();
        Services.DATA.set(cow, traits(helper, cow).fed(1.0, 900L));
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:cow\"]}"));
            HerdBooks.refreshLoaded(helper.getLevel().getServer());
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(data.record(cow.getUUID()).traits().lastFedTick() == recorded, "an excluded cow's record was refreshed");
        helper.succeed();
    }

    public static Animal female(GameTestHelper helper, EntityType<? extends Animal> type) {
        Animal animal = GameTestCompat.spawn(helper, type, 3, 1, 4);
        Services.DATA.set(animal, new AnimalTraits(Sex.FEMALE, 1.0, 1.0, -1L));
        return animal;
    }

    public static Animal male(GameTestHelper helper, EntityType<? extends Animal> type) {
        Animal animal = GameTestCompat.spawn(helper, type, 5, 1, 4);
        Services.DATA.set(animal, new AnimalTraits(Sex.MALE, 1.0, 1.0, -1L));
        return animal;
    }

    public static AnimalTraits traits(GameTestHelper helper, Animal animal) {
        return Services.DATA.get(animal).orElseThrow(() -> GameTestCompat.failure(helper, animal.getType() + " has no traits"));
    }

    public static Pregnancy pregnancy(GameTestHelper helper, Animal mother) {
        return Services.DATA.getPregnancy(mother).orElseThrow(() -> GameTestCompat.failure(helper, "the mother did not become pregnant"));
    }

    // Vanilla's rest after breeding: the age cooldown, love cleared, and both mirrored into the synced traits.
    public static void assertResting(GameTestHelper helper, Animal parent, long now) {
        helper.assertTrue(parent.getAge() == VANILLA_REST, "a parent's age is " + parent.getAge() + ", not " + VANILLA_REST);
        helper.assertTrue(!parent.isInLove(), "a parent is still in love");
        AnimalTraits traits = traits(helper, parent);
        helper.assertTrue(traits.loveUntil() < 0L, "a parent still shows love");
        helper.assertTrue(traits.cooldownUntil() == now + VANILLA_REST, "a parent rests until " + traits.cooldownUntil() + ", not " + (now + VANILLA_REST));
    }
}
