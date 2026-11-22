package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.familiarity.api.Refusable;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClasses;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.MilkEntry;
import net.modcrafters.minecraft.barnyardromance.core.config.MilkResult;
import net.modcrafters.minecraft.barnyardromance.core.config.MilkSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodApi;
import net.modcrafters.minecraft.barnyardromance.pail.api.PailApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.api.SizeClass;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

// Milking tracked animals the species' milk list names (built in: cows, mooshrooms, goats and sheep): only mothers in milk, a few times a day by size.
public final class MilkActions {
    private static final String BUCKET = "minecraft:bucket";

    private MilkActions() {
    }

    // What milking gives, and the sound it makes.
    public record Milk(Item result, SoundEvent sound) {
    }

    // What the held item milks the animal into: the item has to be one of the species' milking items and the entry's
    // items have to exist (an entry for another mod's item resolves to nothing without that mod). Entries are tried in
    // the order of names, so an entry naming the item beats one naming its tag, wherever each is in the list.
    public static Optional<Milk> milkFor(Animal animal, ItemStack held) {
        String speciesId = AnimalSex.speciesId(animal);
        MilkSettings settings = BarnyardConfig.current().milkFor(speciesId);
        if (settings.entries().isEmpty() || held.isEmpty()) {
            return Optional.empty();
        }
        for (Predicate<MilkEntry> name : names(held)) {
            for (MilkEntry entry : settings.entries()) {
                if (name.test(entry)) {
                    Optional<Milk> milk = resolve(animal, speciesId, settings, entry);
                    if (milk.isPresent()) {
                        return milk;
                    }
                }
            }
        }
        return Optional.empty();
    }

    // How a milk list can name the held item, in the order they're tried: by its id, then by a tag it's in, and for a
    // pail, last, by the empty bucket, so every species milked with a bucket fills a pail with the same milk.
    private static List<Predicate<MilkEntry>> names(ItemStack held) {
        String heldId = itemId(held);
        List<Predicate<MilkEntry>> names = new ArrayList<>(3);
        names.add(entry -> entry.bucket().equals(heldId));
        names.add(entry -> entry.namesTagOf(held));
        if (FeaturesRepository.get(PailApi.class).isPail(held)) {
            names.add(entry -> entry.bucket().equals(BUCKET));
        }
        return names;
    }

    // Whether any of the species' milk entries resolves: the animal is a milk animal, whatever it's milked with.
    public static boolean givesMilk(Animal animal) {
        String speciesId = AnimalSex.speciesId(animal);
        MilkSettings settings = BarnyardConfig.current().milkFor(speciesId);
        for (MilkEntry entry : settings.entries()) {
            if (resolve(animal, speciesId, settings, entry).isPresent()) {
                return true;
            }
        }
        return false;
    }

    // A tracked animal with a milk list that doesn't have this item, though the item milks something somewhere: the
    // list replaced the built-in one, so the item does nothing, and vanilla's (or another mod's) milking mustn't answer.
    public static boolean blocksMilkingItem(Animal animal, ItemStack held) {
        if (held.isEmpty() || Tracked.traits(animal).isEmpty()) {
            return false;
        }
        BarnyardConfig config = BarnyardConfig.current();
        // A bowl on a stew animal is the stew's, even when a pack milks some other species with a bowl.
        if (held.is(Items.BOWL) && givesStew(animal)) {
            return false;
        }
        MilkSettings settings = config.milkFor(AnimalSex.speciesId(animal));
        // A list from the config counts even when empty: it says the species gives no milk, with anything.
        if (settings.entries().isEmpty() && !settings.fromConfig() || !isMilkingItem(config, held)) {
            return false;
        }
        List<Predicate<MilkEntry>> names = names(held);
        return settings.entries().stream().noneMatch(entry -> names.stream().anyMatch(name -> name.test(entry)));
    }

    // A pail milks wherever the empty bucket does, so it is a milking item whenever the bucket is.
    private static boolean isMilkingItem(BarnyardConfig config, ItemStack held) {
        return config.isMilkingItem(held) || FeaturesRepository.get(PailApi.class).isPail(held) && config.milkingItems().contains(BUCKET);
    }

    // Whether a bowl held out to this animal gives stew: the species' stew setting (built in: the mooshroom).
    public static boolean givesStew(Animal animal) {
        return BarnyardConfig.current().stewFor(AnimalSex.speciesId(animal));
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    // A built-in entry goes through the feature's milk result for the species if it registered one; a config entry names
    // its item outright.
    private static Optional<Milk> resolve(Animal animal, String speciesId, MilkSettings settings, MilkEntry entry) {
        Optional<MilkResult> provider = settings.fromConfig() ? Optional.empty() : FeaturesRepository.milkResult(speciesId);
        Optional<Item> result = provider.isPresent() ? provider.get().resultFor(animal, entry) : item(entry.result());
        return result.map(item -> new Milk(item, milkingSound(animal, entry)));
    }

    private static Optional<Item> item(String id) {
        Identifier parsed = Identifier.tryParse(id);
        return parsed == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(parsed).filter(item -> item != Items.AIR);
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
        if (animal.isBaby()) {
            return null;
        }
        Optional<Milk> milk = milkFor(animal, stack);
        if (milk.isEmpty()) {
            // The species' list has no use for this item, so nothing happens: no vanilla milking, no message.
            return blocksMilkingItem(animal, stack) ? InteractionResult.CONSUME : null;
        }
        Optional<AnimalTraits> tracked = Tracked.traits(animal);
        if (tracked.isEmpty()) {
            return null;
        }
        PailApi pails = FeaturesRepository.get(PailApi.class);
        boolean pail = pails.isPail(stack);
        // A pail that can't take this milk, being full or holding another, is turned away before any rule runs, so the
        // animal keeps her milking, her familiarity and her chance to refuse.
        if (pail && !pails.fits(stack, milk.get().result())) {
            if (!animal.level().isClientSide()) {
                pails.refusal(stack, milk.get().result()).ifPresent(message -> VersionCompat.sendOverlay(player, message));
            }
            return InteractionResult.CONSUME;
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
        if (familiarityApi.refusedOn(traits, Refusable.MILKING, GameDay.of(clock))) {
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
        // Other mods may stop the milking or change what the bucket becomes; stopped, nothing happens at all. A result
        // a listener emptied in place, rather than cancelling, stops it too, so it doesn't use up the bucket for nothing.
        Milking milking = BarnyardEvents.MILKING.post(new Milking(animal, player, new ItemStack(milk.get().result())));
        if (milking.isCancelled() || milking.result().isEmpty()) {
            return InteractionResult.CONSUME;
        }
        if (pail) {
            // The pail fills where it is, in either hand and in creative mode too, where a bucket would stay as it is
            // and a new milk bucket be handed over. A listener may have changed the milk to one the pail can't take:
            // then nothing happens, as if it had cancelled.
            if (!pails.fill(stack, milking.result().getItem())) {
                return InteractionResult.CONSUME;
            }
        } else {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, milking.result().copy()));
        }
        // Played by the server for everyone: Player.playSound would skip the milker, whose client never milks.
        animal.level().playSound(null, animal.getX(), animal.getY(), animal.getZ(), milk.get().sound(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
        Optional<AnimalTraits> tracked = Tracked.traits(animal);
        if (tracked.isEmpty()) {
            return null;
        }
        AnimalTraits traits = tracked.get();
        Tuning tuning = BarnyardConfig.current().tuning();
        long now = animal.level().getGameTime();
        MilkRules.Start start = MilkRules.start(traits.sex(), animal.isBaby(), givesMilk(animal),
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
        Optional<AnimalTraits> tracked = Tracked.traits(animal);
        if (tracked.isEmpty() || animal.level().isClientSide()) {
            return;
        }
        AnimalTraits traits = tracked.get();
        Tuning tuning = BarnyardConfig.current().tuning();
        long now = animal.level().getGameTime();
        MilkRules.Start start = MilkRules.start(traits.sex(), animal.isBaby(), givesMilk(animal),
                traits.get(LACTATION).hasMilk(now, tuning.milkKeepsFlowingTicks()), FeaturesRepository.get(FamiliarityApi.class).isFamiliar(traits));
        if (start == MilkRules.Start.OK) {
            Services.DATA.set(animal, traits.with(LACTATION, traits.get(LACTATION).started(now, tuning.milkKeepsFlowingTicks())));
        }
    }

    // Females from before Stage 5 and wild spawns get their milk on their first join (see MilkRules.firstJoin).
    public static void onJoin(Animal animal) {
        Optional<AnimalTraits> tracked = Tracked.traits(animal);
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
        if (traits.sex() != Sex.FEMALE || !givesMilk(animal) || Tracked.traits(animal).isEmpty()) {
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
        return SizeClasses.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, AnimalSex.speciesId(animal)));
    }

    // The entry's own sound if it names one, else the goat's, else the cow's.
    private static SoundEvent milkingSound(Animal animal, MilkEntry entry) {
        if (entry.sound() != null) {
            Identifier id = Identifier.tryParse(entry.sound());
            Optional<SoundEvent> sound = id == null ? Optional.empty() : BuiltInRegistries.SOUND_EVENT.getOptional(id);
            if (sound.isPresent()) {
                return sound.get();
            }
        }
        if (animal instanceof Goat goat) {
            return goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
        }
        return SoundEvents.COW_MILK;
    }

    // A refused milking gives nothing and costs no familiarity, but the animal's milking is over until midnight. A
    // stew animal's stew counts as milking.
    public static void refuseMilking(Animal animal, @Nullable Player player) {
        long today = GameDay.of(animal.level().getOverworldClockTime());
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        Tracked.traits(animal).ifPresent(traits -> Services.DATA.set(animal, familiarityApi.withRefusedOn(traits, Refusable.MILKING, today)));
        if (player != null) {
            VersionCompat.sendOverlay(player, TextComponents.of(MilkRules.REFUSED_TEXT));
        }
        familiarityApi.showRefusal(animal);
    }

    // A bowl held out to an animal that gives stew: null lets the stew come. Unfamiliar animals are turned away earlier.
    public static @Nullable InteractionResult refusesStew(Animal animal, Player player) {
        Optional<AnimalTraits> tracked = Tracked.traits(animal);
        if (tracked.isEmpty() || animal.isBaby()) {
            return null;
        }
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        AnimalTraits traits = familiarityApi.settle(animal).orElse(tracked.get());
        long clock = animal.level().getOverworldClockTime();
        if (familiarityApi.refusedOn(traits, Refusable.MILKING, GameDay.of(clock))) {
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
}
