package net.modcrafters.minecraft.barnyardromance.stats;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.core.config.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalSubject;
import net.modcrafters.minecraft.barnyardromance.vigour.api.VigourApi;

// Works out every row of the stat sheet from a subject, so a loaded animal's card and its herd book page agree.
// now is the game time, clock the overworld clock time.
final class FactSheet {
    private FactSheet() {
    }

    static AnimalFacts of(AnimalSubject subject, long now, long clock) {
        AnimalTraits traits = subject.traits();
        String species = subject.species();
        AgeSettings age = BarnyardConfig.current().ageFor(species);
        AgeStage stage = subject.stage();
        boolean female = traits.sex() == Sex.FEMALE;
        boolean male = traits.sex() == Sex.MALE;
        boolean grown = stage == AgeStage.ADULT || stage == AgeStage.OLD;
        // The dead are shown as they were when they died.
        long at = subject.shownAt();
        long today = GameDay.of(clock);
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        PregnancyApi pregnancies = FeaturesRepository.get(PregnancyApi.class);
        VigourApi vigourApi = FeaturesRepository.get(VigourApi.class);
        double familiarity = familiarityApi.familiarity(traits);
        Upkeep upkeep = familiarityApi.upkeep(traits);
        long lastHandFed = familiarityApi.lastHandFed(traits);
        long lastAteDay = familiarityApi.lastAteDay(traits);
        // The traits are as last synced or saved, so Vigour is settled here the way the server would.
        double vigour = vigourApi.vigour(traits, lastAteDay, today);
        boolean hungryMale = male && vigourApi.hungry(lastAteDay, today);
        Pregnancy pregnancy = subject.pregnancy();
        Text breeding = StatText.breeding(
                subject.needsTaming(),
                subject.tamed(),
                familiarity,
                stage == AgeStage.YOUNG,
                female && pregnancies.isWornOut(traits, age, at),
                pregnancies.inLoveUntil(traits),
                pregnancies.restingUntil(traits),
                now
        );
        return AnimalFacts.builder(SizeClass.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, species)).name(), stage)
                .ageTicks(traits.bornTick() < 0L ? -1L : Math.max(0L, at - traits.bornTick()))
                .fedToday(fedToday(traits, clock))
                .fedBy(StatText.feeder(upkeep, today))
                .ticksSinceFed(lastHandFed < 0L ? -1L : Math.max(0L, clock - lastHandFed))
                .pregnant(pregnancy != null)
                .pregnancy(pregnancy == null ? 0.0 : pregnancies.progress(pregnancy, now))
                .dueTicks(pregnancy == null ? 0L : pregnancies.ticksUntilDue(pregnancy, now))
                .milk(subject.milk().map(StatText::milkText).orElse(Text.EMPTY))
                .familiarity(familiarity)
                .fertility(female ? pregnancies.fertility(traits, age, at) : -1.0)
                .seenTicksAgo(subject.seenTicksAgo())
                .familiar(subject.familiar())
                .diedDay(subject.diedDay())
                .shear(subject.shear())
                .grumpy(subject.alive() && subject.tamed() && !familiarityApi.isFamiliar(traits))
                .eggs(subject.eggsTomorrow())
                .cap(subject.cap())
                .killedByRival(subject.killedByRival())
                .ranAway(upkeep.ranAway())
                .growthTicks(traits.growthTicks())
                .weight(traits.weight())
                .health(subject.health())
                .maxHealth(subject.maxHealth())
                .vigour(male && grown ? vigour : -1.0)
                .vigourHungry(hungryMale)
                .eggsToday(subject.alive() ? eggsToday(subject, stage, vigour, hungryMale, today) : Text.EMPTY)
                .breeding(breeding)
                .father(pregnancy == null ? Text.EMPTY : StatText.father(
                        SizeClass.of(Sex.MALE, pregnancy.fatherScale(), SizeRanges.orCurrent(pregnancy.fatherSizeRange(), pregnancy.fatherSpecies())).name(),
                        pregnancy.fatherSpecies()))
                .lastBirth(StatText.lastBirth(offspring(traits), pregnancies.lastBirthTick(traits), now))
                .build();
    }

    // The FED row's "fed today": by hand, or by a feeding block, which never writes lastFedTick.
    static boolean fedToday(AnimalTraits traits, long clock) {
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        return familiarityApi.upkeep(traits).fedByFeederOn(GameDay.of(clock)) || familiarityApi.handFedToday(traits, clock);
    }

    // The name of what the animal last bore, or empty.
    static String offspring(AnimalTraits traits) {
        String species = FeaturesRepository.get(PregnancyApi.class).lastBirthSpecies(traits);
        Identifier id = Identifier.tryParse(species);
        if (id == null || species.isBlank()) {
            return "";
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .map(type -> type.getDescription().getString())
                .orElse("");
    }

    // A hen counts the eggs she laid today; a rooster says whether he makes them fertile today.
    private static Text eggsToday(AnimalSubject subject, AgeStage stage, double vigour, boolean hungryMale, long today) {
        if (subject.baby() || !subject.laysEggs()) {
            return Text.EMPTY;
        }
        AnimalTraits traits = subject.traits();
        Text value;
        if (traits.sex() == Sex.MALE) {
            value = StatText.roosterText(stage == AgeStage.YOUNG, vigour, hungryMale, BarnyardConfig.current().tuning().vigourPerEgg());
        } else if (subject.layingHen()) {
            Laying laying = subject.laying().day() == today ? subject.laying() : Laying.NONE;
            value = StatText.eggsText(laying.laid(), laying.planned(), FeaturesRepository.get(FamiliarityApi.class).wasHungryYesterday(traits, today));
        } else {
            return Text.EMPTY;
        }
        return value.isEmpty() ? Text.EMPTY : StatText.labelled(StatText.eggRowLabel(traits.sex() == Sex.FEMALE, subject.species()), value);
    }
}
