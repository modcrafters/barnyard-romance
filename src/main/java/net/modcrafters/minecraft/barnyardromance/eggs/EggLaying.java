package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.rivalry.Reach;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourActions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.nest.LayInNestGoal;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxes;
import net.modcrafters.minecraft.barnyardromance.pregnancy.LitterRoll;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
//? if >=1.21.5 {
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
//?}

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Hens lay a few eggs a day instead of breeding (see EggRules for how many). The day is planned on the hen's first
// tick after midnight, after her hunger is settled, and when her time comes round she lays all of them at once.
public final class EggLaying {
    private EggLaying() {
    }

    public static boolean laysEggs(Animal animal) {
        return settings(animal).enabled();
    }

    public static EggSettings settings(Animal animal) {
        return BarnyardConfig.current().eggsFor(AnimalSex.speciesId(animal));
    }

    // Adult and old females lay; chicks, young hens and roosters don't.
    public static boolean isLayingHen(Animal animal) {
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty() || traits.get().sex() != Sex.FEMALE || animal.isBaby() || !laysEggs(animal)) {
            return false;
        }
        AgeStage stage = AgeController.stage(animal, traits.get());
        return stage == AgeStage.ADULT || stage == AgeStage.OLD;
    }

    public static void serverTick(Animal animal, ServerLevel level) {
        try {
            if (!laysEggs(animal)) {
                return;
            }
            // Birds from older saves may still be carrying a pregnancy from before they laid eggs.
            if (Services.DATA.get(animal, PregnancyFeature.PREGNANCY).isPresent()) {
                Services.DATA.remove(animal, PregnancyFeature.PREGNANCY);
            }
            if (!isLayingHen(animal)) {
                return;
            }
            long clock = level.getOverworldClockTime();
            int sinceMidnight = EggSchedule.sinceMidnight(clock);
            Laying laying = Services.DATA.get(animal, EggsFeature.LAYING).orElse(Laying.NONE);
            if (laying.day() != GameDay.of(clock)) {
                // The day is planned from yesterday's hunger, so it waits until her days are settled. A hen in a
                // feeding block's herd may wait a few ticks for the block to settle them.
                if (FamiliarityCare.unsettled(animal, clock)) {
                    return;
                }
                laying = plan(animal, GameDay.of(clock), sinceMidnight);
                Services.DATA.set(animal, EggsFeature.LAYING, laying);
            }
            // While she's on her way to a box, the next egg waits for her.
            if (LayInNestGoal.target(animal).isPresent()) {
                return;
            }
            EggSchedule.Due due = EggSchedule.due(laying.times(), laying.next(), sinceMidnight);
            if (due.skip() == 0 && !due.layNow()) {
                return;
            }
            int eggs = due.layNow() ? laying.planned() - laying.next() - due.skip() : 0;
            Services.DATA.set(animal, EggsFeature.LAYING, laying.advance(due.skip(), eggs));
            if (eggs > 0) {
                layDue(animal, level, eggs);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to lay a {}'s egg", AnimalSex.speciesId(animal), exception);
        }
    }

    private static Laying plan(Animal hen, long today, int sinceMidnight) {
        AnimalTraits traits = Services.DATA.get(hen).orElseThrow();
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor(AnimalSex.speciesId(hen)));
        int count = EggRules.perDay(size, traits.hunger().hungryYesterday(today), Familiarity.ready(traits.familiarity()), settings(hen));
        return new Laying(today, EggSchedule.plan(sinceMidnight, count, hen.getRandom().nextDouble()), 0);
    }

    // The eggs are fertilized if a fed rooster is near. A hen that can walk to a box with room goes and lays them all
    // there in one sitting; otherwise they're laid on the ground, where they're always plain: they've been out in the
    // weather.
    // Each rooster pays for the eggs he fathers only once she's on her way to a box; eggs left on the ground are plain
    // and cost him nothing.
    public static void layDue(Animal hen, ServerLevel level, int count) {
        List<ItemStack> eggs = new ArrayList<>(count);
        Map<UUID, Integer> fathered = new HashMap<>();
        for (int i = 0; i < count; i++) {
            eggs.add(fertilizedEgg(hen, level, fathered).orElseGet(() -> plainEgg(hen, level)));
        }
        if (LayInNestGoal.walks(hen) && headForBox(hen, level, eggs, Set.of(), false)) {
            Tuning tuning = BarnyardConfig.current().tuning();
            fathered.forEach((father, eggCount) -> {
                if (level.getEntity(father) instanceof Animal rooster) {
                    VigourActions.pay(rooster, tuning.vigourPerEgg() * eggCount, tuning.hungryEggHealth() * eggCount);
                }
            });
            return;
        }
        layOnGround(hen, level, count);
    }

    // A box only needs room for her first egg; what doesn't fit is left in front of it when she comes out.
    public static boolean headForBox(Animal hen, ServerLevel level, List<ItemStack> eggs, Set<BlockPos> skip, boolean retried) {
        for (NestingBoxBlockEntity box : NestingBoxes.nearby(level, hen.blockPosition(), settings(hen).boxRange())) {
            if (skip.contains(box.getBlockPos()) || box.occupied() || box.canPlaceEgg(eggs.getFirst()) < 0) {
                continue;
            }
            if (LayInNestGoal.headFor(hen, eggs, box.getBlockPos(), level.getGameTime(), retried)) {
                return true;
            }
        }
        return false;
    }

    // Back from a box or a trip to one: eggs that came due while she was busy are laid now, not skipped.
    public static void resume(Animal hen, ServerLevel level) {
        long clock = level.getOverworldClockTime();
        Services.DATA.get(hen, EggsFeature.LAYING).filter(laying -> laying.day() == GameDay.of(clock)).ifPresent(laying -> Services.DATA.set(hen, EggsFeature.LAYING,
                laying.withTimes(EggSchedule.resume(laying.times(), laying.next(), EggSchedule.sinceMidnight(clock)))));
    }

    // For the herd book: the best adult rooster in range, by whether he ate today (so he's fertile tomorrow).
    public static RoosterNear roosterNear(Animal hen, ServerLevel level) {
        int range = settings(hen).roosterRange();
        long today = GameDay.of(level.getOverworldClockTime());
        RoosterNear best = RoosterNear.NONE;
        for (Animal other : level.getEntitiesOfClass(Animal.class, hen.getBoundingBox().inflate(range),
                other -> other != hen && other.getType() == hen.getType() && other.isAlive() && !other.isBaby()
                        && other.distanceToSqr(hen) <= (double) range * range)) {
            Optional<AnimalTraits> traits = Services.DATA.get(other);
            if (traits.isEmpty() || traits.get().sex() != Sex.MALE) {
                continue;
            }
            AgeStage stage = AgeController.stage(other, traits.get());
            if (stage != AgeStage.ADULT && stage != AgeStage.OLD) {
                continue;
            }
            if (traits.get().hunger().lastAteDay() == today) {
                return RoosterNear.FED;
            }
            best = RoosterNear.HUNGRY;
        }
        return best;
    }

    private static boolean canFather(Animal rooster, Animal hen, int eggs, EggSettings settings) {
        Tuning tuning = BarnyardConfig.current().tuning();
        return VigourActions.canPay(rooster, tuning.vigourPerEgg() * eggs, tuning.hungryEggHealth() * eggs)
                && !Reach.blocked(rooster, hen, settings.roosterRange());
    }

    public static boolean boxWithRoomNear(Animal hen, ServerLevel level) {
        ItemStack egg = new ItemStack(BarnyardItems.FERTILIZED_EGG);
        return NestingBoxes.nearby(level, hen.blockPosition(), settings(hen).boxRange()).stream()
                .anyMatch(box -> !box.occupied() && box.canPlaceEgg(egg) >= 0);
    }

    public static void layOnGround(Animal hen, ServerLevel level, int count) {
        drop(hen, level, plainEgg(hen, level).copyWithCount(count));
    }

    // An egg fathered by the closest adult rooster in range who can walk to her and still has the Vigour for it (and,
    // if he's hungry, the health), or nothing. Nobody pays here; see layDue.
    public static Optional<ItemStack> fertilizedEgg(Animal hen, ServerLevel level) {
        return fertilizedEgg(hen, level, new HashMap<>());
    }

    // fathered counts the eggs each rooster is already down for in this sitting, and gains this one.
    public static Optional<ItemStack> fertilizedEgg(Animal hen, ServerLevel level, Map<UUID, Integer> fathered) {
        Optional<AnimalTraits> mother = Services.DATA.get(hen);
        EggSettings settings = settings(hen);
        if (mother.isEmpty() || !settings.enabled()) {
            return Optional.empty();
        }
        long today = GameDay.of(level.getOverworldClockTime());
        double rangeSqr = (double) settings.roosterRange() * settings.roosterRange();
        Animal father = null;
        AnimalTraits fatherTraits = null;
        for (Animal other : level.getEntitiesOfClass(Animal.class, hen.getBoundingBox().inflate(settings.roosterRange()),
                other -> other != hen && other.getType() == hen.getType() && other.isAlive() && !other.isBaby())) {
            double distance = other.distanceToSqr(hen);
            if (distance > rangeSqr || (father != null && distance >= father.distanceToSqr(hen))) {
                continue;
            }
            Optional<AnimalTraits> traits = FamiliarityCare.settle(other);
            if (traits.isEmpty() || traits.get().sex() != Sex.MALE) {
                continue;
            }
            AgeStage stage = AgeController.stage(other, traits.get());
            if ((stage == AgeStage.ADULT || stage == AgeStage.OLD) && canFather(other, hen, fathered.getOrDefault(other.getUUID(), 0) + 1, settings)) {
                father = other;
                fatherTraits = traits.get();
            }
        }
        if (father == null) {
            return Optional.empty();
        }
        fathered.merge(father.getUUID(), 1, Integer::sum);
        AnimalTraits motherTraits = mother.get();
        long hatchTicks = LitterRoll.pickTicks(settings.hatchTicks(), motherTraits.weight(), hen.getRandom().nextDouble());
        return Optional.of(FertilizedEggItem.create(new EggData(
                AnimalSex.speciesId(hen),
                hen.getUUID(),
                father.getUUID(),
                motherTraits.scale(),
                motherTraits.growthTicks(),
                motherTraits.weight(),
                motherTraits.familiarity(),
                fatherTraits.scale(),
                fatherTraits.growthTicks(),
                fatherTraits.weight(),
                variant(hen),
                hatchTicks,
                hatchTicks
        )));
    }

    private static String variant(Animal hen) {
        //? if >=1.21.5 {
        if (hen instanceof Chicken chicken) {
            return chicken.getVariant().unwrapKey().map(key -> key.identifier().toString()).orElse("");
        }
        //?}
        return "";
    }

    // Laid where she stands, the way vanilla lays.
    static void drop(Animal hen, ServerLevel level, ItemStack egg) {
        //? if >=1.21.2 {
        hen.spawnAtLocation(level, egg);
        //?} else
        //hen.spawnAtLocation(egg);
        hen.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (hen.getRandom().nextFloat() - hen.getRandom().nextFloat()) * 0.2F + 1.0F);
        hen.gameEvent(GameEvent.ENTITY_PLACE);
    }

    // The species' own egg item if the config names one, else vanilla's egg for the hen's variant.
    public static ItemStack plainEgg(Animal hen, ServerLevel level) {
        String configured = settings(hen).plainEgg();
        if (configured != null) {
            Identifier id = Identifier.tryParse(configured);
            if (id != null) {
                return BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElseGet(() -> new ItemStack(Items.EGG));
            }
        }
        //? if >=1.21.5 {
        if (hen instanceof Chicken) {
            List<ItemStack> laid = new ArrayList<>();
            hen.dropFromGiftLootTable(level, BuiltInLootTables.CHICKEN_LAY, (dropLevel, stack) -> laid.add(stack));
            if (!laid.isEmpty()) {
                return laid.getFirst();
            }
        }
        //?}
        return new ItemStack(Items.EGG);
    }
}
