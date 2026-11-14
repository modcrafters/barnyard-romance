package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.Range;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodEffects;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;

import java.util.Optional;

import static net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancySlices.BREEDING;

final class PregnancyApiImpl implements PregnancyApi {
    @Override
    public Optional<Pregnancy> pregnancy(Animal animal) {
        return Services.DATA.get(animal, PregnancyFeature.PREGNANCY);
    }

    @Override
    public boolean isPregnant(Animal animal) {
        return Services.DATA.get(animal, PregnancyFeature.PREGNANCY).isPresent();
    }

    @Override
    public void setPregnancy(Animal animal, Pregnancy pregnancy) {
        Services.DATA.set(animal, PregnancyFeature.PREGNANCY, pregnancy);
    }

    @Override
    public void endPregnancy(Animal animal) {
        Services.DATA.remove(animal, PregnancyFeature.PREGNANCY);
    }

    @Override
    public double progress(Pregnancy pregnancy, long now) {
        return PregnancyClock.progress(now, pregnancy);
    }

    @Override
    public long ticksUntilDue(Pregnancy pregnancy, long now) {
        return PregnancyClock.remainingTicks(now, pregnancy);
    }

    @Override
    public int percentShown(double progress) {
        return PregnancyText.percent(progress);
    }

    @Override
    public Text progressLine(double progress, long ticksUntilDue) {
        return PregnancyText.describe(progress, ticksUntilDue);
    }

    @Override
    public Text progressLine(Pregnancy pregnancy, long now, boolean withFather) {
        return PregnancyText.line(pregnancy, now, withFather);
    }

    @Override
    public Text dueClock(long ticksUntilDue) {
        return PregnancyText.clock(ticksUntilDue);
    }

    @Override
    public double fertility(AnimalTraits traits, AgeSettings age, long now) {
        return Fertility.current(traits, age, now);
    }

    @Override
    public boolean isWornOut(AnimalTraits traits, AgeSettings age, long now) {
        return Fertility.wornOut(traits, age, now);
    }

    @Override
    public long inLoveUntil(AnimalTraits traits) {
        return traits.get(BREEDING).loveUntil();
    }

    @Override
    public long restingUntil(AnimalTraits traits) {
        return traits.get(BREEDING).cooldownUntil();
    }

    @Override
    public long lastBirthTick(AnimalTraits traits) {
        return traits.get(BREEDING).lastBirthTick();
    }

    @Override
    public String lastBirthSpecies(AnimalTraits traits) {
        return traits.get(BREEDING).lastBirthSpecies();
    }

    @Override
    public long pickDuration(Range range, double motherWeight, double roll) {
        return LitterRoll.pickTicks(range, motherWeight, roll);
    }

    @Override
    public InteractionResult feedMother(Animal animal, Player player, ItemStack stack, Feed.Eaten eaten) {
        return PregnancyCareActions.feed(animal, player, stack, eaten);
    }

    @Override
    public void feedPregnancy(Animal animal, FoodEffects effects, double factor) {
        PregnancyCareActions.applyIfPregnant(animal, effects, factor);
    }
}
