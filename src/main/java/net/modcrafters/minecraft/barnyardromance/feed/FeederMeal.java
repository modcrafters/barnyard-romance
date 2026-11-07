package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.food.Feed;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.food.FoodHeal;
import net.modcrafters.minecraft.barnyardromance.food.MinecraftItemFacts;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

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
        if (!GameDay.afterSunrise(clock) || AgeController.stage(animal) == AgeStage.BABY || FamiliarityCare.unsettled(animal, clock)) {
            return false;
        }
        Optional<AnimalTraits> traits = FamiliarityCare.tracked(animal);
        return traits.isPresent() && traits.get().hunger().lastAteDay() < GameDay.of(clock)
                && Familiarity.ready(traits.get().familiarity());
    }

    // Today's meal: one item from the block, if it has one.
    static boolean eat(Animal animal, ServerLevel level, FeederBlockEntity feeder, long clock) {
        ItemStack food = feeder.takeOne(level);
        if (food.isEmpty()) {
            return false;
        }
        FamiliarityCare.markFedBy(animal, clock, feeder.kind().id());
        heal(animal, food);
        VersionCompat.playEatingSound(animal, food);
        return true;
    }

    // As the food heals when fed by hand (see FamiliarityActions). A block serving several species may hand an animal
    // something its kind doesn't eat by hand; that heals the default.
    static void heal(Animal animal, ItemStack food) {
        if (animal.getHealth() >= animal.getMaxHealth()) {
            return;
        }
        Feed feed = FoodData.resolve(animal, food);
        float heal = feed instanceof Feed.Eaten eaten
                ? FoodHeal.amount(eaten.effects().heal(), new MinecraftItemFacts(animal, food).nutrition(), eaten.factor(), BarnyardConfig.current().tuning())
                : FoodHeal.amount(Optional.empty(), new MinecraftItemFacts(animal, food).nutrition(), 1.0, BarnyardConfig.current().tuning());
        if (heal > 0.0F) {
            animal.heal(heal);
        }
    }
}
