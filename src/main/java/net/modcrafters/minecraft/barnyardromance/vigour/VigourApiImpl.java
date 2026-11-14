package net.modcrafters.minecraft.barnyardromance.vigour;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.vigour.api.VigourApi;

import static net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices.VIGOUR;

final class VigourApiImpl implements VigourApi {
    @Override
    public boolean canPay(Animal male, double cost, double healthCost) {
        return VigourActions.canPay(male, cost, healthCost);
    }

    @Override
    public void pay(Animal male, double cost, double healthCost) {
        VigourActions.pay(male, cost, healthCost);
    }

    @Override
    public InteractionResult feedMash(Animal animal, Player player, ItemStack stack, Feed.Eaten eaten) {
        return VigourActions.feedMash(animal, player, stack, eaten);
    }

    @Override
    public boolean settled(AnimalTraits traits, long today) {
        return traits.get(VIGOUR).day() >= today;
    }

    @Override
    public AnimalTraits settle(AnimalTraits traits, long lastAteDay, long today) {
        Vigour vigour = VigourRules.settle(traits.get(VIGOUR), lastAteDay, today);
        return vigour.equals(traits.get(VIGOUR)) ? traits : traits.with(VIGOUR, vigour);
    }

    @Override
    public AnimalTraits afterMeal(AnimalTraits traits, long lastAteDayBefore, long today) {
        return traits.with(VIGOUR, VigourRules.onMeal(traits.get(VIGOUR), lastAteDayBefore, today));
    }

    @Override
    public long lastRivalRoll(AnimalTraits traits) {
        return traits.get(VIGOUR).rollDay();
    }

    @Override
    public AnimalTraits rolledForRivals(AnimalTraits traits, long day) {
        return traits.with(VIGOUR, traits.get(VIGOUR).withRollDay(day));
    }

    @Override
    public double vigour(AnimalTraits traits, long lastAteDay, long today) {
        return VigourRules.settle(traits.get(VIGOUR), lastAteDay, today).value();
    }

    @Override
    public boolean hungry(long lastAteDay, long today) {
        return VigourRules.hungry(lastAteDay, today);
    }
}
