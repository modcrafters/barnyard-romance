package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.nest.LayInNestGoal;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxes;
import net.modcrafters.minecraft.barnyardromance.pregnancy.LitterRoll;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
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
// tick after midnight, after her hunger is settled, and each egg is laid when its time comes round.
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
            if (Services.DATA.getPregnancy(animal).isPresent()) {
                Services.DATA.clearPregnancy(animal);
            }
            if (!isLayingHen(animal)) {
                return;
            }
            long clock = level.getOverworldClockTime();
            int sinceMidnight = EggSchedule.sinceMidnight(clock);
            Laying laying = Services.DATA.getLaying(animal).orElse(Laying.NONE);
            if (laying.day() != GameDay.of(clock)) {
                laying = plan(animal, GameDay.of(clock), sinceMidnight);
                Services.DATA.setLaying(animal, laying);
            }
            EggSchedule.Due due = EggSchedule.due(laying.times(), laying.next(), sinceMidnight);
            if (due.skip() == 0 && !due.layNow()) {
                return;
            }
            Services.DATA.setLaying(animal, laying.advance(due.skip() + (due.layNow() ? 1 : 0)));
            if (due.layNow()) {
                layDue(animal, level);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to lay a {}'s egg", AnimalSex.speciesId(animal), exception);
        }
    }

    private static Laying plan(Animal hen, long today, int sinceMidnight) {
        AnimalTraits traits = Services.DATA.get(hen).orElseThrow();
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor(AnimalSex.speciesId(hen)));
        int count = EggRules.perDay(size, traits.hunger().hungryYesterday(today), Familiarity.ready(traits.familiarity()), settings(hen));
        double[] rolls = new double[count];
        for (int i = 0; i < count; i++) {
            rolls[i] = hen.getRandom().nextDouble();
        }
        return new Laying(today, EggSchedule.plan(sinceMidnight, count, rolls), 0);
    }

    // The egg is fertilized if a fed rooster is near. A hen that can walk to a box with room goes and lays it there;
    // otherwise it's laid on the ground, where it's always plain: it has been out in the weather.
    public static void layDue(Animal hen, ServerLevel level) {
        ItemStack egg = fertilizedEgg(hen, level).orElseGet(() -> plainEgg(hen, level));
        if (LayInNestGoal.walks(hen) && headForBox(hen, level, egg, Set.of(), false)) {
            return;
        }
        layOnGround(hen, level);
    }

    public static boolean headForBox(Animal hen, ServerLevel level, ItemStack egg, Set<BlockPos> skip, boolean retried) {
        for (NestingBoxBlockEntity box : NestingBoxes.nearby(level, hen.blockPosition(), settings(hen).boxRange())) {
            if (skip.contains(box.getBlockPos()) || box.occupied() || box.canPlaceEgg(egg) < 0) {
                continue;
            }
            if (LayInNestGoal.headFor(hen, egg, box.getBlockPos(), level.getGameTime(), retried)) {
                return true;
            }
        }
        return false;
    }

    public static void layOnGround(Animal hen, ServerLevel level) {
        drop(hen, level, plainEgg(hen, level));
    }

    // An egg fathered by the closest adult rooster in range that wasn't hungry yesterday, or nothing.
    public static Optional<ItemStack> fertilizedEgg(Animal hen, ServerLevel level) {
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
            if (traits.isEmpty() || traits.get().sex() != Sex.MALE || traits.get().hunger().hungryYesterday(today)) {
                continue;
            }
            AgeStage stage = AgeController.stage(other, traits.get());
            if (stage == AgeStage.ADULT || stage == AgeStage.OLD) {
                father = other;
                fatherTraits = traits.get();
            }
        }
        if (father == null) {
            return Optional.empty();
        }
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
