package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.food.api.FoodEffects;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodApi;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCare;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

// What food does to a pregnancy. Superfoods skip the daily feed, never touch familiarity, and only help a pregnant mother.
public final class PregnancyCareActions {
    public static final Text NEEDS_MOTHER = Text.of("message.barnyard_romance.superfood.needs_mother");
    public static final Text ALREADY_ON_MASH = Text.of("message.barnyard_romance.superfood.already_on_mash");
    public static final Text CANT_EAT_MORE = Text.of("message.barnyard_romance.superfood.cant_eat_more");

    private PregnancyCareActions() {
    }

    public static InteractionResult feed(Animal animal, Player player, ItemStack stack, Feed.Eaten eaten) {
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        long now = animal.level().getGameTime();
        Optional<Pregnancy> pregnancy = Services.DATA.get(animal, PregnancyFeature.PREGNANCY);
        Text refusal = refusal(pregnancy.orElse(null), eaten.effects(), now);
        if (refusal != null) {
            player.sendSystemMessage(TextComponents.of(refusal));
            return InteractionResult.CONSUME;
        }
        float heal = FeaturesRepository.get(FoodApi.class).heals(animal, stack, eaten);
        if (!player.hasInfiniteMaterials()) {
            stack.consume(1, player);
        }
        Services.DATA.set(animal, PregnancyFeature.PREGNANCY, pregnancy.get().withCare(apply(pregnancy.get(), eaten.effects(), eaten.factor(), now)));
        if (animal.getHealth() < animal.getMaxHealth() && heal > 0.0F) {
            animal.heal(heal);
        }
        VersionCompat.playEatingSound(animal, stack);
        FamiliarityCare.markEaten(animal);
        return VersionCompat.SUCCESS_SERVER;
    }

    // A normal food with pregnancy effects (say a bad food that slows it) works on a pregnant animal too.
    public static void applyIfPregnant(Animal animal, FoodEffects effects, double factor) {
        if (!effects.needsPregnancy()) {
            return;
        }
        long now = animal.level().getGameTime();
        Services.DATA.get(animal, PregnancyFeature.PREGNANCY).ifPresent(pregnancy ->
                Services.DATA.set(animal, PregnancyFeature.PREGNANCY, pregnancy.withCare(apply(pregnancy, effects, factor, now))));
    }

    static Text refusal(Pregnancy pregnancy, FoodEffects effects, long now) {
        if (pregnancy == null) {
            return NEEDS_MOTHER;
        }
        if (effects.pregnancySpeed().isPresent() && pregnancy.care().boosting(now)) {
            return ALREADY_ON_MASH;
        }
        if (effects.babyImprove() > 0.0 && pregnancy.motherFamiliarity() + pregnancy.care().nutritionBonus() >= 1.0) {
            return CANT_EAT_MORE;
        }
        return null;
    }

    // Only one speed boost runs at a time; a second one is skipped.
    static PregnancyCare apply(Pregnancy pregnancy, FoodEffects effects, double factor, long now) {
        PregnancyCare care = pregnancy.care();
        if (effects.pregnancySpeed().isPresent() && !care.boosting(now)) {
            FoodEffects.PregnancySpeed speed = effects.pregnancySpeed().get();
            care = care.boost(now, 1.0 + (speed.factor() - 1.0) * factor, speed.ticks());
        }
        if (effects.pregnancyProgress() != 0.0) {
            care = care.progress(pregnancy.durationTicks(), effects.pregnancyProgress() * factor);
        }
        if (effects.babyImprove() > 0.0) {
            care = care.improve(effects.babyImprove() * factor);
        }
        return care;
    }
}
