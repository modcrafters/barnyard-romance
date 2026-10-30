package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.food.Feed;
import net.modcrafters.minecraft.barnyardromance.food.FoodHeal;
import net.modcrafters.minecraft.barnyardromance.food.MinecraftItemFacts;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.5 {
import net.minecraft.world.entity.animal.cow.AbstractCow;
//?} else
//import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;

import java.util.Optional;

// Milking tracked cows, mooshrooms, goats and sheep: only mothers in milk, a few times a day by size.
public final class MilkActions {
    private MilkActions() {
    }

    public static Optional<Item> milkFor(Animal animal) {
        //? if >=1.21.5 {
        if (animal instanceof AbstractCow) {
        //?} else
        //if (animal instanceof Cow) {
            return Optional.of(Items.MILK_BUCKET);
        }
        if (animal instanceof Goat) {
            return Optional.of(BarnyardItems.GOAT_MILK_BUCKET);
        }
        if (animal instanceof Sheep) {
            return Optional.of(BarnyardItems.SHEEP_MILK_BUCKET);
        }
        return Optional.empty();
    }

    // Null leaves the bucket to vanilla: not a bucket, not a milk animal, a baby, or an untracked animal.
    public static InteractionResult milk(Animal animal, Player player, InteractionHand hand) {
        try {
            return tryMilk(animal, player, hand);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to milk a {}, leaving it to vanilla", AnimalSex.speciesId(animal), exception);
            return null;
        }
    }

    private static InteractionResult tryMilk(Animal animal, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<Item> milk = milkFor(animal);
        if (!stack.is(Items.BUCKET) || milk.isEmpty() || animal.isBaby()) {
            return null;
        }
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty()) {
            return null;
        }
        AnimalTraits traits = FamiliarityCare.settle(animal).orElse(tracked.get());
        Tuning tuning = BarnyardConfig.current().tuning();
        long now = animal.level().getGameTime();
        long clock = animal.level().getOverworldClockTime();
        MilkRules.Check check = MilkRules.check(traits.sex(), sizeClass(animal, traits), traits.lactation(), now, clock, tuning,
                traits.hunger().hungryYesterday(GameDay.of(clock)));
        if (check != MilkRules.Check.OK) {
            if (!animal.level().isClientSide()) {
                VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.text(check)));
            }
            return InteractionResult.CONSUME;
        }
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // Played by the server for everyone: Player.playSound would skip the milker, whose client never milks.
        animal.level().playSound(null, animal.getX(), animal.getY(), animal.getZ(), milkingSound(animal), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(milk.get())));
        Services.DATA.set(animal, traits.withLactation(traits.lactation().milked(now, clock, tuning.milkKeepsFlowingTicks())));
        FamiliarityCare.spend(animal, tuning.familiarityMilkingCost());
        return VersionCompat.SUCCESS_SERVER;
    }

    // Dairy Mash starts a dry, familiar female's milk. Like the other superfoods it skips the once-a-day feed,
    // and it counts as her meal for the day.
    public static InteractionResult startMilk(Animal animal, Player player, InteractionHand hand, Feed.Eaten eaten) {
        ItemStack stack = player.getItemInHand(hand);
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty()) {
            return null;
        }
        AnimalTraits traits = tracked.get();
        Tuning tuning = BarnyardConfig.current().tuning();
        long now = animal.level().getGameTime();
        MilkRules.Start start = MilkRules.start(traits.sex(), animal.isBaby(), milkFor(animal).isPresent(),
                traits.lactation().hasMilk(now, tuning.milkKeepsFlowingTicks()), Familiarity.ready(traits.familiarity()));
        if (start != MilkRules.Start.OK) {
            VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.text(start)));
            return InteractionResult.CONSUME;
        }
        float heal = FoodHeal.amount(eaten.effects().heal(), new MinecraftItemFacts(animal, stack).nutrition(), eaten.factor(), tuning);
        // Dairy Mash comes in a bowl, and the player gets it back, as with a stew.
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL)));
        Services.DATA.set(animal, traits.withLactation(traits.lactation().started(now, tuning.milkKeepsFlowingTicks())));
        if (animal.getHealth() < animal.getMaxHealth() && heal > 0.0F) {
            animal.heal(heal);
        }
        VersionCompat.playEatingSound(animal, stack);
        FamiliarityCare.markEaten(animal);
        return VersionCompat.SUCCESS_SERVER;
    }

    // An ordinary food with startsMilk (a mod's or a pack's) starts a dry, familiar female's milk when she eats it, and
    // says nothing when it can't: it was a meal either way.
    public static void startMilkWithMeal(Animal animal) {
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty() || animal.level().isClientSide()) {
            return;
        }
        AnimalTraits traits = tracked.get();
        Tuning tuning = BarnyardConfig.current().tuning();
        long now = animal.level().getGameTime();
        MilkRules.Start start = MilkRules.start(traits.sex(), animal.isBaby(), milkFor(animal).isPresent(),
                traits.lactation().hasMilk(now, tuning.milkKeepsFlowingTicks()), Familiarity.ready(traits.familiarity()));
        if (start == MilkRules.Start.OK) {
            Services.DATA.set(animal, traits.withLactation(traits.lactation().started(now, tuning.milkKeepsFlowingTicks())));
        }
    }

    // Females from before Stage 5 and wild spawns get their milk on their first join (see MilkRules.firstJoin).
    public static void onJoin(Animal animal) {
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty() || tracked.get().lactation().graceGiven()) {
            return;
        }
        AnimalTraits traits = tracked.get();
        Lactation lactation = MilkRules.firstJoin(
                traits.sex(),
                animal.isBaby(),
                traits.lastBirthTick(),
                traits.lactation(),
                animal.level().getGameTime(),
                BarnyardConfig.current().tuning()
        );
        Services.DATA.set(animal, traits.withLactation(lactation));
    }

    public static Optional<MilkStatus> status(Animal animal, AnimalTraits traits, long now, long clock) {
        if (traits.sex() != Sex.FEMALE || milkFor(animal).isEmpty() || tracked(animal).isEmpty()) {
            return Optional.empty();
        }
        return status(traits, sizeClass(animal, traits), now, clock);
    }

    // The same for a female whose species gives milk, from her traits alone (the herd book's records).
    public static Optional<MilkStatus> status(AnimalTraits traits, SizeClass size, long now, long clock) {
        Tuning tuning = BarnyardConfig.current().tuning();
        Lactation lactation = traits.lactation();
        if (!lactation.hasMilk(now, tuning.milkKeepsFlowingTicks())) {
            return Optional.empty();
        }
        int perDay = MilkRules.perDay(size, tuning);
        // A female that went hungry yesterday can't be milked today.
        boolean hungry = traits.hunger().hungryYesterday(GameDay.of(clock));
        return Optional.of(new MilkStatus(
                lactation.nursing(now),
                lactation.endsAt(tuning.milkKeepsFlowingTicks()) - now,
                hungry ? 0 : lactation.milkingsLeft(clock, perDay),
                perDay
        ));
    }

    private static SizeClass sizeClass(Animal animal, AnimalTraits traits) {
        return SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal)));
    }

    private static SoundEvent milkingSound(Animal animal) {
        if (animal instanceof Goat goat) {
            return goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
        }
        return SoundEvents.COW_MILK;
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
