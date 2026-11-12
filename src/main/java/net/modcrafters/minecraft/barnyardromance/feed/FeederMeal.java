package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodApi;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

// A meal from a feeding block. It is the day's meal, as any food is, and it heals as the same food would by hand, but
// it is upkeep only: it never wins an animal over, puts it in love, or does what a superfood or a pregnancy meal does.
// The herd walking over (EatAtFeederGoal), a sitting pet (MorningFeeding) and catch-up (FeederCatchUp) all heal here.
public final class FeederMeal {
    private FeederMeal() {
    }

    // Whether the animal still wants a meal from a block now: it's past sunrise, its days are settled (so catch-up is
    // done with them), it hasn't eaten today, and it's a familiar animal past nursing.
    static boolean due(Animal animal, long clock) {
        if (!GameDay.afterSunrise(clock) || AgeController.stage(animal) == AgeStage.BABY) {
            return false;
        }
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        if (familiarityApi.isUnsettled(animal, clock)) {
            return false;
        }
        Optional<AnimalTraits> traits = BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal)) ? Services.DATA.get(animal) : Optional.empty();
        return traits.isPresent() && familiarityApi.lastAteDay(traits.get()) < GameDay.of(clock) && familiarityApi.isFamiliar(traits.get());
    }

    // Today's meal: one item from the block, if it has one. Returns what it ate, or an empty stack.
    static ItemStack eat(Animal animal, ServerLevel level, FeederBlockEntity feeder, long clock) {
        ItemStack food = feeder.takeOne(level);
        if (food.isEmpty()) {
            return ItemStack.EMPTY;
        }
        FeaturesRepository.get(FamiliarityApi.class).recordFeederMeal(animal, clock, feeder.kind().id());
        heal(animal, food);
        VersionCompat.playEatingSound(animal, food);
        return food;
    }

    // Crumbs of the food at the animal's mouth: just in front of its eyes and a little below, the way its head points.
    static void crumbs(ServerLevel level, Animal animal, ItemStack food, int count) {
        Vec3 facing = Vec3.directionFromRotation(animal.getXRot(), animal.getYHeadRot());
        Vec3 mouth = animal.getEyePosition().add(facing.scale(animal.getBbWidth() * 0.5 + 0.15)).add(0.0, -0.15, 0.0);
        //? if >=1.21.2 {
        ItemParticleOption crumb = new ItemParticleOption(ParticleTypes.ITEM, food.getItem());
        //?} else
        //ItemParticleOption crumb = new ItemParticleOption(ParticleTypes.ITEM, food);
        level.sendParticles(crumb, mouth.x, mouth.y, mouth.z, count, 0.08, 0.04, 0.08, 0.04);
    }

    // As the food heals when fed by hand (see FamiliarityActions). A block serving several species may hand an animal
    // something its kind doesn't eat by hand; that heals the default.
    static void heal(Animal animal, ItemStack food) {
        if (animal.getHealth() >= animal.getMaxHealth()) {
            return;
        }
        FoodApi foods = FeaturesRepository.get(FoodApi.class);
        float heal = foods.heals(animal, food, foods.offered(animal, food));
        if (heal > 0.0F) {
            animal.heal(heal);
        }
    }
}
