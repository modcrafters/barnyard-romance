package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
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

    public static void layDue(Animal hen, ServerLevel level) {
        drop(hen, level, plainEgg(hen, level));
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
