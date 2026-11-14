package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
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

import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

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
            return Optional.of(MilkItems.GOAT_MILK_BUCKET);
        }
        if (animal instanceof Sheep) {
            return Optional.of(MilkItems.SHEEP_MILK_BUCKET);
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
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        AnimalTraits traits = familiarityApi.settle(animal).orElse(tracked.get());
        Tuning tuning = BarnyardConfig.current().tuning();
        long now = animal.level().getGameTime();
        long clock = animal.level().getOverworldClockTime();
        // Whether she went hungry yesterday isn't known until her days are settled, which for one in a feeding
        // block's herd may take a few ticks; until then the bucket does nothing.
        if (!animal.level().isClientSide() && familiarityApi.firstUnsettledDay(traits) < GameDay.of(clock)) {
            return InteractionResult.CONSUME;
        }
        MilkRules.Check check = MilkRules.check(traits.sex(), sizeClass(animal, traits), traits.get(LACTATION), now, clock, tuning,
                familiarityApi.wasHungryYesterday(traits, GameDay.of(clock)));
        if (check != MilkRules.Check.OK) {
            if (!animal.level().isClientSide()) {
                VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.text(check)));
            }
            return InteractionResult.CONSUME;
        }
        if (familiarityApi.milkRefusedOn(traits, GameDay.of(clock))) {
            if (!animal.level().isClientSide()) {
                VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.REFUSED_TODAY_TEXT));
            }
            return InteractionResult.CONSUME;
        }
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // Only once every other reason has passed, so a refusal never covers up a dry or already milked cow.
        if (familiarityApi.refuses(traits, animal.getRandom().nextDouble())) {
            refuseMilking(animal, player);
            return InteractionResult.CONSUME;
        }
        // Played by the server for everyone: Player.playSound would skip the milker, whose client never milks.
        animal.level().playSound(null, animal.getX(), animal.getY(), animal.getZ(), milkingSound(animal), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(milk.get())));
        Services.DATA.set(animal, traits.with(LACTATION, traits.get(LACTATION).milked(now, clock, tuning.milkKeepsFlowingTicks())));
        familiarityApi.spendFamiliarity(animal, tuning.familiarityMilkingCost());
        if (familiarityApi.isFamiliar(traits)) {
            Events.post(new AnimalEvent.Milked(animal, player));
        }
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
                traits.get(LACTATION).hasMilk(now, tuning.milkKeepsFlowingTicks()), FeaturesRepository.get(FamiliarityApi.class).isFamiliar(traits));
        if (start != MilkRules.Start.OK) {
            VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.text(start)));
            return InteractionResult.CONSUME;
        }
        float heal = FeaturesRepository.get(FoodApi.class).heals(animal, stack, eaten);
        // Dairy Mash comes in a bowl, and the player gets it back, as with a stew.
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL)));
        Services.DATA.set(animal, traits.with(LACTATION, traits.get(LACTATION).started(now, tuning.milkKeepsFlowingTicks())));
        if (animal.getHealth() < animal.getMaxHealth() && heal > 0.0F) {
            animal.heal(heal);
        }
        VersionCompat.playEatingSound(animal, stack);
        FeaturesRepository.get(FamiliarityApi.class).recordMeal(animal);
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
                traits.get(LACTATION).hasMilk(now, tuning.milkKeepsFlowingTicks()), FeaturesRepository.get(FamiliarityApi.class).isFamiliar(traits));
        if (start == MilkRules.Start.OK) {
            Services.DATA.set(animal, traits.with(LACTATION, traits.get(LACTATION).started(now, tuning.milkKeepsFlowingTicks())));
        }
    }

    // Females from before Stage 5 and wild spawns get their milk on their first join (see MilkRules.firstJoin).
    public static void onJoin(Animal animal) {
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty() || tracked.get().get(LACTATION).graceGiven()) {
            return;
        }
        AnimalTraits traits = tracked.get();
        Lactation lactation = MilkRules.firstJoin(
                traits.sex(),
                animal.isBaby(),
                FeaturesRepository.get(PregnancyApi.class).lastBirthTick(traits),
                traits.get(LACTATION),
                animal.level().getGameTime(),
                BarnyardConfig.current().tuning()
        );
        Services.DATA.set(animal, traits.with(LACTATION, lactation));
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
        Lactation lactation = traits.get(LACTATION);
        if (!lactation.hasMilk(now, tuning.milkKeepsFlowingTicks())) {
            return Optional.empty();
        }
        int perDay = MilkRules.perDay(size, tuning);
        // A female that went hungry yesterday can't be milked today.
        boolean hungry = FeaturesRepository.get(FamiliarityApi.class).wasHungryYesterday(traits, GameDay.of(clock));
        return Optional.of(new MilkStatus(
                lactation.nursing(now),
                lactation.endsAt(tuning.milkKeepsFlowingTicks()) - now,
                hungry ? 0 : lactation.milkingsLeft(clock, perDay),
                perDay
        ));
    }

    private static SizeClass sizeClass(Animal animal, AnimalTraits traits) {
        return SizeClass.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, AnimalSex.speciesId(animal)));
    }

    private static SoundEvent milkingSound(Animal animal) {
        if (animal instanceof Goat goat) {
            return goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
        }
        return SoundEvents.COW_MILK;
    }

    // A refused milking gives nothing and costs no familiarity, but the animal's milking is over until midnight. A
    // mooshroom's stew counts as milking.
    public static void refuseMilking(Animal animal, @Nullable Player player) {
        long today = GameDay.of(animal.level().getOverworldClockTime());
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        tracked(animal).ifPresent(traits -> Services.DATA.set(animal, familiarityApi.withMilkRefusedOn(traits, today)));
        if (player != null) {
            VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.REFUSED_TEXT));
        }
        familiarityApi.showRefusal(animal);
    }

    // A bowl held out to a mooshroom: null lets vanilla give the stew. Unfamiliar mooshrooms are turned away earlier.
    public static @Nullable InteractionResult refusesStew(Animal animal, Player player) {
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty() || animal.isBaby()) {
            return null;
        }
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        AnimalTraits traits = familiarityApi.settle(animal).orElse(tracked.get());
        long clock = animal.level().getOverworldClockTime();
        if (familiarityApi.milkRefusedOn(traits, GameDay.of(clock))) {
            if (!animal.level().isClientSide()) {
                VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.REFUSED_TODAY_TEXT));
            }
            return InteractionResult.CONSUME;
        }
        if (!animal.level().isClientSide() && familiarityApi.isFamiliar(traits)
                && familiarityApi.refuses(traits, animal.getRandom().nextDouble())) {
            refuseMilking(animal, player);
            return InteractionResult.CONSUME;
        }
        return null;
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
