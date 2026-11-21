package net.modcrafters.minecraft.barnyardromance.stats;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.api.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.herd.api.Familiar;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearApi;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearState;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalSubject;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// Fills the stat sheet from an animal that is loaded right now, on the client or the server.
public final class LiveFacts {
    private LiveFacts() {
    }

    // Empty when the animal isn't tracked: it has no traits, or its species was switched off. now is the game time,
    // clock the overworld clock time.
    public static Optional<AnimalFacts> of(Animal animal, long now, long clock) {
        return Tracked.traits(animal).map(traits -> FactSheet.of(new Live(animal, traits, now, clock), now, clock));
    }

    // The animal as it is now; what only a loaded animal can say comes from the entity itself.
    private record Live(Animal animal, AnimalTraits traits, long now, long clock) implements AnimalSubject {
        @Override
        public String species() {
            return AnimalSex.speciesId(animal);
        }

        @Override
        public AgeStage stage() {
            return AgeController.stage(animal, traits);
        }

        @Override
        public boolean baby() {
            return animal.isBaby();
        }

        @Override
        public boolean alive() {
            return true;
        }

        @Override
        public boolean needsTaming() {
            return AnimalSex.needsTaming(animal);
        }

        @Override
        public boolean tamed() {
            return AnimalSex.tamed(animal);
        }

        @Override
        public long shownAt() {
            return now;
        }

        @Override
        public float health() {
            return animal.getHealth();
        }

        @Override
        public float maxHealth() {
            return animal.getMaxHealth();
        }

        @Override
        public @Nullable Pregnancy pregnancy() {
            return FeaturesRepository.get(PregnancyApi.class).pregnancy(animal).orElse(null);
        }

        @Override
        public Optional<MilkStatus> milk() {
            return FeaturesRepository.get(MilkApi.class).status(animal, traits, now, clock);
        }

        @Override
        public ShearState shear() {
            ShearApi shear = FeaturesRepository.get(ShearApi.class);
            return shear.shearState(shear.tracksShearing(animal), shear.readyForShearing(animal), animal.isBaby(), traits, clock);
        }

        // A loaded animal is either familiar or not yet; only the herd book remembers one that was.
        @Override
        public Familiar familiar() {
            return FeaturesRepository.get(FamiliarityApi.class).isFamiliar(traits) ? Familiar.FAMILIAR : Familiar.NOT_YET;
        }

        @Override
        public double cap() {
            return FeaturesRepository.get(FamiliarityApi.class).wildCap(animal, traits);
        }

        @Override
        public boolean laysEggs() {
            return FeaturesRepository.get(EggsApi.class).laysEggs(animal);
        }

        @Override
        public boolean layingHen() {
            return FeaturesRepository.get(EggsApi.class).isLayingHen(animal);
        }

        @Override
        public Laying laying() {
            return FeaturesRepository.get(EggsApi.class).laying(animal).orElse(Laying.NONE);
        }
    }
}
