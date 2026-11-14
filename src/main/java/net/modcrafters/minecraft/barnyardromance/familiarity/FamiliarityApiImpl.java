package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.HungerSettings;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.UpkeepOutcome;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;

final class FamiliarityApiImpl implements FamiliarityApi {
    @Override
    public double familiarity(AnimalTraits traits) {
        return traits.get(CARE).familiarity();
    }

    @Override
    public boolean isFamiliar(AnimalTraits traits) {
        return Familiarity.ready(traits.get(CARE).familiarity());
    }

    @Override
    public boolean isFamiliarAt(double familiarity) {
        return Familiarity.ready(familiarity);
    }

    @Override
    public double familiarThreshold() {
        return Familiarity.threshold();
    }

    @Override
    public double wildCap(Animal animal, AnimalTraits traits) {
        return FamiliarityCare.cap(animal, traits);
    }

    @Override
    public double wildCap(AnimalTraits traits, boolean pet) {
        return WildCaught.cap(traits.get(CARE).origin(), pet, BarnyardConfig.current().tuning());
    }

    @Override
    public void tameIfFamiliar(Animal animal, Player player, double familiarity) {
        FamiliarityActions.tameIfFamiliar(animal, player, familiarity);
    }

    @Override
    public AnimalTraits withNewbornFamiliarity(AnimalTraits newborn, double nutrition) {
        double familiarity = Familiarity.inherited(nutrition);
        // Born into your care: farm-raised as soon as it has any familiarity at all.
        return newborn.with(CARE, FamiliarityState.NONE.fed(familiarity, -1L).withOrigin(familiarity > 0.0 ? Origin.FARM : Origin.UNKNOWN));
    }

    @Override
    public AnimalTraits withFamiliaritySetTo(Animal animal, AnimalTraits traits, double familiarity) {
        FamiliarityState care = traits.get(CARE);
        return familiarity > care.familiarity()
                ? FamiliarityCare.raise(animal, traits, familiarity, care.lastFedTick())
                : traits.with(CARE, care.withFamiliarity(familiarity));
    }

    @Override
    public AnimalTraits withFamiliarityFrom(AnimalTraits kept, AnimalTraits fresh) {
        // The hunger goes with the familiarity it was settled into, so the book doesn't count those days twice.
        FamiliarityState freshCare = fresh.get(CARE);
        return kept.with(CARE, kept.get(CARE).withFamiliarity(freshCare.familiarity()).withHunger(freshCare.hunger()).withOrigin(freshCare.origin()));
    }

    @Override
    public long lastAteDay(AnimalTraits traits) {
        return traits.get(CARE).hunger().lastAteDay();
    }

    @Override
    public boolean wasHungryYesterday(AnimalTraits traits, long today) {
        return traits.get(CARE).hunger().hungryYesterday(today);
    }

    @Override
    public long lastHandFed(AnimalTraits traits) {
        return traits.get(CARE).lastFedTick();
    }

    @Override
    public boolean handFedToday(AnimalTraits traits, long clock) {
        return Familiarity.fedToday(traits.get(CARE).lastFedTick(), clock);
    }

    @Override
    public Upkeep upkeep(AnimalTraits traits) {
        return traits.get(CARE).upkeep();
    }

    @Override
    public void recordMeal(Animal animal) {
        FamiliarityCare.markEaten(animal);
    }

    @Override
    public void recordFeederMeal(Animal animal, long clock, String feeder) {
        FamiliarityCare.markFedBy(animal, clock, feeder);
    }

    @Override
    public void spendFamiliarity(Animal animal, double cost) {
        FamiliarityCare.spend(animal, cost);
    }

    @Override
    public Optional<AnimalTraits> settle(Animal animal) {
        return FamiliarityCare.settle(animal);
    }

    @Override
    public Optional<AnimalTraits> settle(Animal animal, long clock) {
        return FamiliarityCare.settle(animal, clock);
    }

    @Override
    public boolean isUnsettled(Animal animal, long clock) {
        return FamiliarityCare.unsettled(animal, clock);
    }

    @Override
    public long firstUnsettledDay(AnimalTraits traits) {
        return traits.get(CARE).hunger().settledDay();
    }

    @Override
    public AnimalTraits afterMissedDays(AnimalTraits traits, long today, boolean nursing) {
        FamiliarityState care = traits.get(CARE);
        Hunger.Settled settled = Hunger.settle(care.hunger(), today, nursing);
        double familiarity = Hunger.afterHunger(care.familiarity(), settled.hungryDays(), BarnyardConfig.current().tuning().familiarityHungerLoss());
        return traits.with(CARE, care.withFamiliarity(familiarity).withHunger(settled.hunger()));
    }

    @Override
    public long daysCounted(long missedDays, HungerSettings.CatchUp mode) {
        return DailyUpkeep.daysToSettle(missedDays, mode);
    }

    @Override
    public AnimalTraits skippingDaysBefore(AnimalTraits traits, long day) {
        Hunger hunger = traits.get(CARE).hunger();
        return traits.with(CARE, traits.get(CARE).withHunger(new Hunger(day, hunger.lastAteDay(), hunger.lastHungryDay())));
    }

    @Override
    public UpkeepOutcome settleHerdDay(Animal animal, ServerLevel level, AnimalTraits traits, long day, boolean ate, @Nullable String feeder,
                                       float owed) {
        return FamiliarityCare.settleDay(animal, level, traits, day, ate, feeder, owed);
    }

    @Override
    public void takeToll(Animal animal, ServerLevel level, float damage, boolean escaped, boolean catchingUp) {
        FamiliarityCare.toll(animal, level, damage, escaped, catchingUp);
    }

    @Override
    public AnimalTraits withInHerd(AnimalTraits traits, boolean inHerd) {
        Upkeep upkeep = traits.get(CARE).upkeep();
        // An old save's upkeep is worked out on its next settled day. A member is familiar, so it would count as cared
        // for then; it does now, rather than lose the mark that tells it's from an old save.
        Upkeep known = upkeep.equals(Upkeep.UNKNOWN) ? Upkeep.NONE.caredFor() : upkeep;
        return traits.with(CARE, traits.get(CARE).withUpkeep(known.withInHerd(inHerd)));
    }

    @Override
    public boolean deniesProducts(Animal animal) {
        return FamiliarityActions.denies(animal);
    }

    @Override
    public boolean refuses(AnimalTraits traits, double roll) {
        return Refusal.refuses(traits.get(CARE).familiarity(), roll, BarnyardConfig.current().tuning());
    }

    @Override
    public boolean milkRefusedOn(AnimalTraits traits, long day) {
        return traits.get(CARE).refusals().milkRefused(day);
    }

    @Override
    public boolean shearRefusedOn(AnimalTraits traits, long day) {
        return traits.get(CARE).refusals().shearRefused(day);
    }

    @Override
    public AnimalTraits withMilkRefusedOn(AnimalTraits traits, long day) {
        return traits.with(CARE, traits.get(CARE).withRefusals(traits.get(CARE).refusals().withMilk(day)));
    }

    @Override
    public AnimalTraits withShearRefusedOn(AnimalTraits traits, long day) {
        return traits.with(CARE, traits.get(CARE).withRefusals(traits.get(CARE).refusals().withShear(day)));
    }

    @Override
    public void showRefusal(Animal animal) {
        FamiliarityActions.refusalCue(animal);
    }
}
