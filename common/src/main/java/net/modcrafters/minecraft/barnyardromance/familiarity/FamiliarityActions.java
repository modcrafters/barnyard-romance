package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.mixin.AnimalEating;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class FamiliarityActions {
    public static final String TOO_SHY = "Sneak so you don't scare this animal.";

    private FamiliarityActions() {
    }

    public static InteractionResult interact(Animal animal, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!animal.isFood(stack)) {
            return null;
        }
        Optional<AnimalTraits> traits = tracked(animal);
        if (traits.isEmpty()) {
            return null;
        }
        AnimalTraits current = traits.get();
        boolean familiar = Familiarity.ready(current.familiarity());
        Familiarity.Response response = Familiarity.respond(true, current.lastFedTick(), animal.level().getGameTime());
        Familiarity.Click click = Familiarity.click(player.isShiftKeyDown(), familiar, response);
        if (click == Familiarity.Click.SCARE) {
            if (!animal.level().isClientSide()) {
                player.sendSystemMessage(Component.literal(TOO_SHY));
                scare(animal, player);
            }
            return InteractionResult.CONSUME;
        }
        if (click == Familiarity.Click.WAIT) {
            if (!animal.level().isClientSide()) {
                long remaining = Familiarity.ticksUntilFeed(current.lastFedTick(), animal.level().getGameTime());
                player.sendSystemMessage(Component.literal(Familiarity.nextFeed(remaining)));
            }
            return InteractionResult.CONSUME;
        }
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!player.hasInfiniteMaterials()) {
            stack.consume(1, player);
        }
        double gained = Familiarity.gain(current.familiarity());
        Services.DATA.set(animal, current.fed(gained, animal.level().getGameTime()));
        if (animal instanceof AnimalEating eating) {
            eating.barnyardromance$playEatingSound();
        }
        if (!animal.isBaby() && Familiarity.allowsLove(gained, Services.DATA.getPregnancy(animal).isPresent())) {
            animal.setInLove(player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void scare(Animal animal, Player player) {
        Vec3 away = animal.position().subtract(player.position());
        if (away.horizontalDistanceSqr() < 1.0E-4) {
            away = new Vec3(1.0, 0.0, 0.0);
        }
        away = new Vec3(away.x, 0.0, away.z).normalize().scale(6.0);
        animal.getNavigation().stop();
        animal.getNavigation().moveTo(animal.getX() + away.x, animal.getY(), animal.getZ() + away.z, 1.4);
        animal.playAmbientSound();
    }

    public static boolean ignoresFood(Animal animal) {
        Optional<AnimalTraits> traits = tracked(animal);
        if (traits.isEmpty()) {
            return false;
        }
        AnimalTraits current = traits.get();
        return !Familiarity.interested(
                true,
                current.lastFedTick(),
                animal.level().getGameTime(),
                Familiarity.ready(current.familiarity())
        );
    }

    public static boolean allowsLove(Animal animal) {
        Optional<AnimalTraits> traits = tracked(animal);
        if (traits.isEmpty()) {
            return true;
        }
        return Familiarity.allowsLove(traits.get().familiarity(), Services.DATA.getPregnancy(animal).isPresent());
    }

    public static boolean deniesProduct(Animal animal, ItemStack stack) {
        if (!isProduct(animal, stack)) {
            return false;
        }
        return denies(animal);
    }

    public static boolean denies(Animal animal) {
        Optional<AnimalTraits> traits = tracked(animal);
        if (traits.isEmpty()) {
            return false;
        }
        return ProductGate.blocked(true, true, traits.get().familiarity());
    }

    private static boolean isProduct(Animal animal, ItemStack stack) {
        if (animal.isBaby()) {
            return false;
        }
        if (stack.is(Items.SHEARS) && animal instanceof Shearable) {
            return true;
        }
        if (stack.is(Items.BOWL) && animal instanceof MushroomCow) {
            return true;
        }
        return stack.is(Items.BUCKET) && (animal instanceof AbstractCow || animal instanceof Goat);
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
