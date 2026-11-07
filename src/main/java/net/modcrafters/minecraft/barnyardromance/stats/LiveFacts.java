package net.modcrafters.minecraft.barnyardromance.stats;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Fertility;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.shear.ShearRule;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourRules;

import java.util.Optional;

// Fills the stat sheet from an animal that is loaded right now, on the client or the server.
public final class LiveFacts {
    private LiveFacts() {
    }

    // Empty when the animal has no traits. now is the game time, clock the overworld clock time.
    public static Optional<AnimalFacts> of(Animal animal, long now, long clock) {
        Optional<AnimalTraits> found = Services.DATA.get(animal);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        AnimalTraits traits = found.get();
        BarnyardConfig config = BarnyardConfig.current();
        String species = AnimalSex.speciesId(animal);
        AgeSettings age = config.ageFor(species);
        AgeStage stage = AgeController.stage(animal, traits);
        boolean female = traits.sex() == Sex.FEMALE;
        boolean grown = stage == AgeStage.ADULT || stage == AgeStage.OLD;
        boolean ready = Familiarity.ready(traits.familiarity());
        Optional<Pregnancy> pregnancy = Services.DATA.getPregnancy(animal);
        double fertility = Fertility.current(traits, age, now);
        Text breeding = StatText.breeding(
                needsTaming(animal),
                FamiliarityCare.tamed(animal),
                traits.familiarity(),
                stage == AgeStage.YOUNG,
                female && Fertility.wornOut(traits, age, now),
                traits.loveUntil(),
                traits.cooldownUntil(),
                now
        );
        boolean hungryMale = traits.sex() == Sex.MALE && VigourRules.hungry(traits.hunger().lastAteDay(), GameDay.of(clock));
        return Optional.of(AnimalFacts.builder(SizeClass.of(traits.sex(), traits.scale(), config.sizeFor(species)).name(), stage)
                .ageTicks(traits.bornTick() < 0L ? -1L : Math.max(0L, now - traits.bornTick()))
                .fedToday(fedToday(traits, clock))
                .fedBy(StatText.feeder(traits.upkeep(), GameDay.of(clock)))
                .ticksSinceFed(traits.lastFedTick() < 0L ? -1L : Math.max(0L, clock - traits.lastFedTick()))
                .pregnant(pregnancy.isPresent())
                .pregnancy(pregnancy.map(current -> PregnancyClock.progress(now, current)).orElse(0.0))
                .dueTicks(pregnancy.map(current -> PregnancyClock.remainingTicks(now, current)).orElse(0L))
                .milk(MilkActions.status(animal, traits, now, clock).map(StatText::milkText).orElse(Text.EMPTY))
                .familiarity(traits.familiarity())
                .fertility(female ? fertility : -1.0)
                .familiar(ready ? Familiar.FAMILIAR : Familiar.NOT_YET)
                .shear(ShearRule.state(animal instanceof Sheep, !(animal instanceof Sheep sheep && sheep.isSheared()), animal.isBaby(), traits, clock))
                .grumpy(FamiliarityCare.tamed(animal) && !ready)
                .cap(FamiliarityCare.cap(animal, traits))
                .ranAway(traits.upkeep().ranAway())
                .growthTicks(traits.growthTicks())
                .weight(traits.weight())
                .health(animal.getHealth())
                .maxHealth(animal.getMaxHealth())
                .vigour(traits.sex() == Sex.MALE && grown ? vigour(traits, clock) : -1.0)
                .vigourHungry(hungryMale)
                .eggsToday(eggsToday(animal, traits, species, clock))
                .breeding(breeding)
                .father(pregnancy.map(current -> StatText.father(
                        SizeClass.of(Sex.MALE, current.fatherScale(), config.sizeFor(current.fatherSpecies())).name(),
                        current.fatherSpecies())).orElse(Text.EMPTY))
                .lastBirth(StatText.lastBirth(offspring(traits), traits.lastBirthTick(), now))
                .build());
    }

    // The FED row's "fed today": by hand, or by a feeding block, which never writes lastFedTick.
    public static boolean fedToday(AnimalTraits traits, long clock) {
        return traits.upkeep().fedByFeederOn(GameDay.of(clock)) || Familiarity.fedToday(traits.lastFedTick(), clock);
    }

    // These only breed when tamed; ocelots trust instead, which never blocks breeding.
    public static boolean needsTaming(Animal animal) {
        return animal instanceof AbstractHorse || animal instanceof Wolf || animal instanceof Cat;
    }

    // The name of what the animal last bore, or empty.
    public static String offspring(AnimalTraits traits) {
        Identifier id = Identifier.tryParse(traits.lastBirthSpecies());
        if (id == null || traits.lastBirthSpecies().isBlank()) {
            return "";
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .map(type -> type.getDescription().getString())
                .orElse("");
    }

    // A hen counts today's eggs; a rooster says whether he makes them fertile today.
    private static Text eggsToday(Animal animal, AnimalTraits traits, String species, long clock) {
        if (animal.isBaby() || !EggLaying.laysEggs(animal)) {
            return Text.EMPTY;
        }
        long today = GameDay.of(clock);
        Text value;
        if (traits.sex() == Sex.MALE) {
            value = StatText.roosterText(AgeController.stage(animal, traits) == AgeStage.YOUNG, vigour(traits, clock),
                    VigourRules.hungry(traits.hunger().lastAteDay(), today), BarnyardConfig.current().tuning().vigourPerEgg());
        } else if (EggLaying.isLayingHen(animal)) {
            Laying laying = Services.DATA.getLaying(animal).filter(current -> current.day() == today).orElse(Laying.NONE);
            value = StatText.eggsText(laying.laid(), laying.planned(), traits.hunger().hungryYesterday(today));
        } else {
            return Text.EMPTY;
        }
        return value.isEmpty() ? Text.EMPTY : StatText.labelled(StatText.eggRowLabel(traits.sex() == Sex.FEMALE, species), value);
    }

    // The traits are as last synced, so Vigour is settled here the way the server would.
    private static double vigour(AnimalTraits traits, long clock) {
        return VigourRules.settle(traits.vigour(), traits.hunger().lastAteDay(), GameDay.of(clock)).value();
    }
}
