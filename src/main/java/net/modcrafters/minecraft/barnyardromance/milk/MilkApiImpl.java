package net.modcrafters.minecraft.barnyardromance.milk;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

final class MilkApiImpl implements MilkApi {
    @Override
    public boolean givesMilk(Animal animal) {
        return MilkActions.milkFor(animal).isPresent();
    }

    @Override
    public Optional<MilkStatus> status(Animal animal, AnimalTraits traits, long now, long clock) {
        return MilkActions.status(animal, traits, now, clock);
    }

    @Override
    public Optional<MilkStatus> status(AnimalTraits traits, SizeClass size, long now, long clock) {
        return MilkActions.status(traits, size, now, clock);
    }

    @Override
    public AnimalTraits inMilkAfterBirth(AnimalTraits mother, long now, long youngUntil) {
        return mother.with(LACTATION, mother.get(LACTATION).afterBirth(now, youngUntil, BarnyardConfig.current().tuning().lactationTicks()));
    }

    @Override
    public @Nullable InteractionResult startMilk(Animal animal, Player player, InteractionHand hand, Feed.Eaten eaten) {
        return MilkActions.startMilk(animal, player, hand, eaten);
    }

    @Override
    public void startMilkWithMeal(Animal animal) {
        MilkActions.startMilkWithMeal(animal);
    }
}
