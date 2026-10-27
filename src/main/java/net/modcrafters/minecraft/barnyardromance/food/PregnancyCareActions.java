package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCare;
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
        Optional<Pregnancy> pregnancy = Services.DATA.getPregnancy(animal);
        Text refusal = refusal(pregnancy.orElse(null), eaten.effects(), now);
        if (refusal != null) {
            player.sendSystemMessage(TextComponents.of(refusal));
            return InteractionResult.CONSUME;
        }
        float heal = FoodHeal.amount(eaten.effects().heal(), new MinecraftItemFacts(animal, stack).nutrition(), eaten.factor(), BarnyardConfig.current().tuning());
        if (!player.hasInfiniteMaterials()) {
            stack.consume(1, player);
        }
        Services.DATA.setPregnancy(animal, pregnancy.get().withCare(apply(pregnancy.get(), eaten.effects(), eaten.factor(), now)));
        if (animal.getHealth() < animal.getMaxHealth() && heal > 0.0F) {
            animal.heal(heal);
        }
        VersionCompat.playEatingSound(animal, stack);
        return VersionCompat.SUCCESS_SERVER;
    }

    // A normal food with pregnancy effects (say a bad food that slows it) works on a pregnant animal too.
    public static void applyIfPregnant(Animal animal, FoodEffects effects, double factor) {
        if (!effects.needsPregnancy()) {
            return;
        }
        long now = animal.level().getGameTime();
        Services.DATA.getPregnancy(animal).ifPresent(pregnancy ->
                Services.DATA.setPregnancy(animal, pregnancy.withCare(apply(pregnancy, effects, factor, now))));
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
