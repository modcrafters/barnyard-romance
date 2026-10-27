package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.food.Feed;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.food.FoodHeal;
import net.modcrafters.minecraft.barnyardromance.food.MinecraftItemFacts;
import net.modcrafters.minecraft.barnyardromance.food.PregnancyCareActions;
import net.modcrafters.minecraft.barnyardromance.mixin.OcelotAccessor;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.5 {
import net.minecraft.world.entity.animal.cow.AbstractCow;
//?} else
//import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class FamiliarityActions {
    public static final String TOO_SHY = "Sneak so you don't scare this animal.";
    public static final String WONT_EAT = "It won't eat that.";
    public static final String WANTS_MEAT = "Feed it meat to earn its trust.";
    public static final String NOT_FAMILIAR = "It doesn't know you well enough yet.";

    private FamiliarityActions() {
    }

    public static InteractionResult interact(Animal animal, Player player, InteractionHand hand) {
        try {
            return feed(animal, player, hand);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to feed a {}, leaving it to vanilla", AnimalSex.speciesId(animal), exception);
            return null;
        }
    }

    private static InteractionResult feed(Animal animal, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return null;
        }
        Optional<AnimalTraits> traits = tracked(animal);
        if (traits.isEmpty()) {
            return null;
        }
        Feed feed = FoodData.resolve(animal, stack);
        if (feed instanceof Feed.Refused) {
            if (!animal.level().isClientSide()) {
                player.sendSystemMessage(Component.literal(WONT_EAT));
            }
            return InteractionResult.CONSUME;
        }
        if (!(feed instanceof Feed.Eaten eaten)) {
            return null;
        }
        if (eaten.superfood()) {
            return PregnancyCareActions.feed(animal, player, stack, eaten);
        }
        AnimalTraits current = traits.get();
        long now = animal.level().getOverworldClockTime();
        boolean familiar = Familiarity.ready(current.familiarity());
        boolean hurt = animal.getHealth() < animal.getMaxHealth();
        Familiarity.Response response = Familiarity.respond(true, current.lastFedTick(), now);
        Familiarity.Click click = Familiarity.click(player.isShiftKeyDown(), familiar, response, hurt);
        if (click == Familiarity.Click.SCARE) {
            if (!animal.level().isClientSide()) {
                player.sendSystemMessage(Component.literal(TOO_SHY));
                scare(animal, player);
            }
            return InteractionResult.CONSUME;
        }
        if (click == Familiarity.Click.WAIT) {
            if (!animal.level().isClientSide()) {
                long remaining = Familiarity.ticksUntilFeed(current.lastFedTick(), now);
                player.sendSystemMessage(Component.literal(Familiarity.nextFeed(remaining)));
            }
            return InteractionResult.CONSUME;
        }
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        float heal = FoodHeal.amount(eaten.effects().heal(), new MinecraftItemFacts(animal, stack).nutrition(), eaten.factor(), BarnyardConfig.current().tuning());
        if (!player.hasInfiniteMaterials()) {
            stack.consume(1, player);
        }
        // Wild cats and ocelots despawn until tamed or trusting, and familiarity takes days; vanilla keeps fed ones too.
        if (animal instanceof Cat || animal instanceof Ocelot) {
            animal.setPersistenceRequired();
        }
        if (hurt && heal > 0.0F) {
            animal.heal(heal);
        }
        VersionCompat.playEatingSound(animal);
        if (click == Familiarity.Click.HEAL_ONLY) {
            return VersionCompat.SUCCESS_SERVER;
        }
        PregnancyCareActions.applyIfPregnant(animal, eaten.effects(), eaten.factor());
        double amount = eaten.effects().familiarity().orElseGet(Familiarity::gain) * eaten.factor();
        double gained = Familiarity.gain(current.familiarity(), amount);
        Services.DATA.set(animal, current.fed(gained, now));
        tameIfFamiliar(animal, player, gained);
        if (!animal.isBaby() && Familiarity.allowsLove(gained, Services.DATA.getPregnancy(animal).isPresent())) {
            animal.setInLove(player);
        }
        return VersionCompat.SUCCESS_SERVER;
    }

    // Any food the animal eats draws it after a player holding it, as its vanilla food does.
    public static boolean tempts(Animal animal, ItemStack stack) {
        try {
            return !ignoresFood(animal) && FoodData.eats(animal, stack);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to check a {}'s diet", AnimalSex.speciesId(animal), exception);
            return false;
        }
    }

    // Horses, donkeys, mules, llamas, camels, wolves and cats only breed when tamed, so familiarity tames them.
    // Ocelots never become pets in vanilla; familiarity gives them the same trust their fish roll would.
    public static void tameIfFamiliar(Animal animal, Player player, double familiarity) {
        if (animal instanceof AbstractHorse horse && Familiarity.tames(horse.isTamed(), familiarity)) {
            horse.tameWithName(player);
        } else if ((animal instanceof Wolf || animal instanceof Cat) && animal instanceof TamableAnimal pet
                && Familiarity.tames(pet.isTame(), familiarity)) {
            pet.tame(player);
            pet.getNavigation().stop();
            pet.setTarget(null);
            pet.setOrderedToSit(true);
            pet.level().broadcastEntityEvent(pet, (byte) 7);
        } else if (animal instanceof Ocelot ocelot && ocelot instanceof OcelotAccessor trust
                && Familiarity.tames(trust.barnyardromance$isTrusting(), familiarity)) {
            trust.barnyardromance$setTrusting(true);
            ocelot.level().broadcastEntityEvent(ocelot, (byte) 41);
        }
    }

    // Bones would tame a wolf behind familiarity's back, so a tracked wild wolf turns them down.
    public static InteractionResult blocksVanillaTaming(Animal animal, Player player, ItemStack stack) {
        if (!(animal instanceof Wolf wolf) || wolf.isTame() || !stack.is(Items.BONE) || tracked(animal).isEmpty()) {
            return null;
        }
        if (!animal.level().isClientSide()) {
            VersionCompat.sendOverlay(player, Component.literal(WANTS_MEAT));
        }
        return InteractionResult.CONSUME;
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
                animal.level().getOverworldClockTime(),
                Familiarity.ready(current.familiarity()),
                animal.getHealth() < animal.getMaxHealth()
        );
    }

    public static boolean allowsLove(Animal animal) {
        Optional<AnimalTraits> traits = tracked(animal);
        if (traits.isEmpty()) {
            return true;
        }
        return Familiarity.allowsLove(traits.get().familiarity(), Services.DATA.getPregnancy(animal).isPresent())
                && AgeController.allowsLove(animal);
    }

    // Milk, wool and stew wait for familiarity; the player is told why.
    public static InteractionResult refusesProduct(Animal animal, Player player, ItemStack stack) {
        if (!deniesProduct(animal, stack)) {
            return null;
        }
        if (!animal.level().isClientSide()) {
            VersionCompat.sendOverlay(player, Component.literal(NOT_FAMILIAR));
        }
        return InteractionResult.FAIL;
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
        //? if >=1.21.5 {
        return stack.is(Items.BUCKET) && (animal instanceof AbstractCow || animal instanceof Goat || animal instanceof Sheep);
        //?} else
        //return stack.is(Items.BUCKET) && (animal instanceof Cow || animal instanceof Goat || animal instanceof Sheep);
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
