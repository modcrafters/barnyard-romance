package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.config.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.herd.api.Familiar;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearApi;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.vigour.api.VigourApi;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;

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
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        boolean ready = familiarityApi.isFamiliar(traits);
        long lastHandFed = familiarityApi.lastHandFed(traits);
        PregnancyApi pregnancies = FeaturesRepository.get(PregnancyApi.class);
        Optional<Pregnancy> pregnancy = pregnancies.pregnancy(animal);
        double fertility = pregnancies.fertility(traits, age, now);
        Text breeding = StatText.breeding(
                needsTaming(animal),
                familiarityApi.isTamed(animal),
                familiarityApi.familiarity(traits),
                stage == AgeStage.YOUNG,
                female && pregnancies.isWornOut(traits, age, now),
                pregnancies.inLoveUntil(traits),
                pregnancies.restingUntil(traits),
                now
        );
        boolean hungryMale = traits.sex() == Sex.MALE && FeaturesRepository.get(VigourApi.class).hungry(familiarityApi.lastAteDay(traits), GameDay.of(clock));
        return Optional.of(AnimalFacts.builder(SizeClass.of(traits.sex(), traits.scale(), config.sizeFor(species)).name(), stage)
                .ageTicks(traits.bornTick() < 0L ? -1L : Math.max(0L, now - traits.bornTick()))
                .fedToday(fedToday(traits, clock))
                .fedBy(StatText.feeder(familiarityApi.upkeep(traits), GameDay.of(clock)))
                .ticksSinceFed(lastHandFed < 0L ? -1L : Math.max(0L, clock - lastHandFed))
                .pregnant(pregnancy.isPresent())
                .pregnancy(pregnancy.map(current -> pregnancies.progress(current, now)).orElse(0.0))
                .dueTicks(pregnancy.map(current -> pregnancies.ticksUntilDue(current, now)).orElse(0L))
                .milk(FeaturesRepository.get(MilkApi.class).status(animal, traits, now, clock).map(StatText::milkText).orElse(Text.EMPTY))
                .familiarity(familiarityApi.familiarity(traits))
                .fertility(female ? fertility : -1.0)
                .familiar(ready ? Familiar.FAMILIAR : Familiar.NOT_YET)
                .shear(FeaturesRepository.get(ShearApi.class).shearState(
                        animal instanceof Sheep, !(animal instanceof Sheep sheep && sheep.isSheared()), animal.isBaby(), traits, clock))
                .grumpy(familiarityApi.isTamed(animal) && !ready)
                .cap(familiarityApi.wildCap(animal, traits))
                .ranAway(familiarityApi.upkeep(traits).ranAway())
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
                .lastBirth(StatText.lastBirth(offspring(traits), pregnancies.lastBirthTick(traits), now))
                .build());
    }

    // The FED row's "fed today": by hand, or by a feeding block, which never writes lastFedTick.
    public static boolean fedToday(AnimalTraits traits, long clock) {
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        return familiarityApi.upkeep(traits).fedByFeederOn(GameDay.of(clock)) || familiarityApi.handFedToday(traits, clock);
    }

    // These only breed when tamed; ocelots trust instead, which never blocks breeding.
    public static boolean needsTaming(Animal animal) {
        return animal instanceof AbstractHorse || animal instanceof Wolf || animal instanceof Cat;
    }

    // The name of what the animal last bore, or empty.
    public static String offspring(AnimalTraits traits) {
        String species = FeaturesRepository.get(PregnancyApi.class).lastBirthSpecies(traits);
        Identifier id = Identifier.tryParse(species);
        if (id == null || species.isBlank()) {
            return "";
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .map(type -> type.getDescription().getString())
                .orElse("");
    }

    // A hen counts today's eggs; a rooster says whether he makes them fertile today.
    private static Text eggsToday(Animal animal, AnimalTraits traits, String species, long clock) {
        EggsApi eggs = FeaturesRepository.get(EggsApi.class);
        if (animal.isBaby() || !eggs.laysEggs(animal)) {
            return Text.EMPTY;
        }
        long today = GameDay.of(clock);
        Text value;
        if (traits.sex() == Sex.MALE) {
            value = StatText.roosterText(AgeController.stage(animal, traits) == AgeStage.YOUNG, vigour(traits, clock),
                    FeaturesRepository.get(VigourApi.class).hungry(FeaturesRepository.get(FamiliarityApi.class).lastAteDay(traits), today),
                    BarnyardConfig.current().tuning().vigourPerEgg());
        } else if (eggs.isLayingHen(animal)) {
            Laying laying = eggs.laying(animal).filter(current -> current.day() == today).orElse(Laying.NONE);
            value = StatText.eggsText(laying.laid(), laying.planned(), FeaturesRepository.get(FamiliarityApi.class).wasHungryYesterday(traits, today));
        } else {
            return Text.EMPTY;
        }
        return value.isEmpty() ? Text.EMPTY : StatText.labelled(StatText.eggRowLabel(traits.sex() == Sex.FEMALE, species), value);
    }

    // The traits are as last synced, so Vigour is settled here the way the server would.
    private static double vigour(AnimalTraits traits, long clock) {
        return FeaturesRepository.get(VigourApi.class).vigour(traits, FeaturesRepository.get(FamiliarityApi.class).lastAteDay(traits), GameDay.of(clock));
    }
}
