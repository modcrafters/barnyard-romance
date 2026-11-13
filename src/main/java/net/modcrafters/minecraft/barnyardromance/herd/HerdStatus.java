package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearState;
import net.modcrafters.minecraft.barnyardromance.core.config.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.age.Aging;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearApi;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatsApi;
import net.modcrafters.minecraft.barnyardromance.vigour.api.VigourApi;
import net.modcrafters.minecraft.barnyardromance.herd.api.Familiar;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// Works out what the herd book shows for one animal at a given time, from its record.
// Everything stored as a time keeps counting while the animal is unloaded; a baby stays a baby until it is loaded again.
public final class HerdStatus {
    private HerdStatus() {
    }

    public static HerdEntry entry(HerdRecord record, HerdLedger.Node node, long now, long clock, boolean loaded) {
        View view = view(record, node, now, clock, loaded);
        Component eggTip = view.eggs() == null ? Component.empty() : TextComponents.of(view.eggs().tooltip());
        if (view.eggs() != null && !view.facts().eggsToday().isEmpty()) {
            eggTip = Component.empty().append(eggTip).append(Component.literal("\n")).append(TextComponents.of(view.facts().eggsToday()));
        }
        return new HerdEntry(
                node.id(),
                record.species(),
                record.name(),
                view.female(),
                view.stage() == AgeStage.BABY || view.stage() == AgeStage.YOUNG,
                record.alive(),
                view.familiar(),
                view.fed(),
                view.feeder(),
                view.pregnancy() == null ? -1 : FeaturesRepository.get(PregnancyApi.class).percentShown(view.progress()),
                view.milk().map(status -> status.left() > 0).orElse(false),
                view.shear(),
                Optional.ofNullable(view.eggs()).map(EggOutlook::kind),
                eggTip,
                Optional.ofNullable(node.mother()),
                Optional.ofNullable(node.father()),
                FeaturesRepository.get(StatsApi.class).lines(view.facts()).stream()
                        .map(line -> new HerdEntry.Line(line.kind(), TextComponents.of(line.text()), (float) line.fill()))
                        .toList()
        );
    }

    // The stat sheet's values for a record, worked out the way LiveFacts works them out for the animal itself.
    public static AnimalFacts facts(HerdRecord record, HerdLedger.Node node, long now, long clock, boolean loaded) {
        return view(record, node, now, clock, loaded).facts();
    }

    // Everything worked out once, for the page's lines and for the parts of the entry that sit beside them.
    private record View(AgeStage stage, boolean female, Familiar familiar, HerdEntry.Fed fed, String feeder, @Nullable Pregnancy pregnancy,
                        double progress, Optional<MilkStatus> milk, ShearState shear, @Nullable EggOutlook eggs, AnimalFacts facts) {
    }

    private static View view(HerdRecord record, HerdLedger.Node node, long now, long clock, boolean loaded) {
        BarnyardConfig config = BarnyardConfig.current();
        StatsApi stats = FeaturesRepository.get(StatsApi.class);
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        // An animal far from home goes on missing meals; the book shows the familiarity it has lost since it was seen.
        AnimalTraits traits = loaded || !record.alive() ? record.traits() : familiarityApi.afterMissedDays(record.traits(), GameDay.of(clock), record.baby());
        double familiarity = familiarityApi.familiarity(traits);
        Upkeep upkeep = familiarityApi.upkeep(traits);
        long lastHandFed = familiarityApi.lastHandFed(traits);
        AgeSettings age = config.ageFor(record.species());
        // The dead are shown as they were when they died.
        long at = record.alive() ? now : record.diedAt();
        AgeStage stage = Aging.stage(record.baby(), traits.bornTick(), traits.growthTicks(), at, age);
        boolean female = traits.sex() == Sex.FEMALE;
        boolean grown = stage == AgeStage.ADULT || stage == AgeStage.OLD;
        Familiar familiar = familiarityApi.isFamiliar(traits)
                ? Familiar.FAMILIAR
                : node.everFamiliar() ? Familiar.NO_LONGER : Familiar.NOT_YET;
        // The contents page's icon is full for hand-feeding and half for a feeding block's meal.
        HerdEntry.Fed fed = familiarityApi.handFedToday(traits, clock)
                ? HerdEntry.Fed.HAND
                : upkeep.fedByFeederOn(GameDay.of(clock)) ? HerdEntry.Fed.FEEDER : HerdEntry.Fed.NOT_FED;
        String feeder = fed == HerdEntry.Fed.FEEDER ? upkeep.feeder() : "";
        PregnancyApi pregnancies = FeaturesRepository.get(PregnancyApi.class);
        Pregnancy pregnancy = record.alive() ? record.pregnancy() : null;
        double progress = pregnancy == null ? 0.0 : pregnancies.progress(pregnancy, now);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, record.species()));
        Optional<MilkStatus> milk = female && record.milkable() && record.alive()
                ? FeaturesRepository.get(MilkApi.class).status(traits, size, now, clock)
                : Optional.empty();
        ShearState shear = record.alive()
                ? FeaturesRepository.get(ShearApi.class).shearState(record.shearable(), record.woolly(), record.baby(), traits, clock)
                : ShearState.NONE;
        EggOutlook eggs = eggs(record, traits, stage, clock, config);
        RecordSheet sheet = record.sheet();
        long today = GameDay.of(clock);
        VigourApi vigourApi = FeaturesRepository.get(VigourApi.class);
        long lastAteDay = familiarityApi.lastAteDay(traits);
        double vigour = vigourApi.vigour(traits, lastAteDay, today);
        boolean showsVigour = traits.sex() == Sex.MALE && grown;
        Text breeding = stats.breedingText(
                sheet.needsTaming(),
                record.tamed(),
                familiarity,
                stage == AgeStage.YOUNG,
                female && pregnancies.isWornOut(traits, age, at),
                pregnancies.inLoveUntil(traits),
                pregnancies.restingUntil(traits),
                now
        );
        AnimalFacts facts = AnimalFacts.builder(size.name(), stage)
                .ageTicks(traits.bornTick() < 0L ? -1L : Math.max(0L, at - traits.bornTick()))
                .fedToday(stats.fedToday(traits, clock))
                .fedBy(stats.fedByText(upkeep, today))
                .ticksSinceFed(lastHandFed < 0L ? -1L : Math.max(0L, clock - lastHandFed))
                .pregnant(pregnancy != null)
                .pregnancy(progress)
                .dueTicks(pregnancy == null ? 0L : pregnancies.ticksUntilDue(pregnancy, now))
                .milk(milk.map(stats::milkText).orElse(Text.EMPTY))
                .familiarity(familiarity)
                .fertility(female ? pregnancies.fertility(traits, age, at) : -1.0)
                .seenTicksAgo(loaded || !record.alive() ? -1L : Math.max(0L, now - record.refreshedAt()))
                .familiar(familiar)
                .diedDay(record.alive() ? -1L : diedDay(record))
                .shear(shear)
                .grumpy(record.alive() && record.tamed() && !familiarityApi.isFamiliar(traits))
                .eggs(eggs)
                .cap(familiarityApi.wildCap(traits, sheet.needsTaming() || record.tamed()))
                .killedByRival(!record.alive() && record.cause() == DeathCause.RIVAL)
                .ranAway(upkeep.ranAway())
                .growthTicks(traits.growthTicks())
                .weight(traits.weight())
                .health(sheet.health())
                .maxHealth(sheet.maxHealth())
                .vigour(showsVigour ? vigour : -1.0)
                .vigourHungry(traits.sex() == Sex.MALE && vigourApi.hungry(lastAteDay, today))
                .eggsToday(record.alive() ? eggsToday(record, traits, stage, vigour, today, config) : Text.EMPTY)
                .breeding(breeding)
                .father(pregnancy == null ? Text.EMPTY : stats.fatherText(
                        SizeClass.of(Sex.MALE, pregnancy.fatherScale(), SizeRanges.orCurrent(pregnancy.fatherSizeRange(), pregnancy.fatherSpecies())).name(), pregnancy.fatherSpecies()))
                .lastBirth(stats.lastBirthText(stats.lastOffspringName(traits), pregnancies.lastBirthTick(traits), now))
                .build();
        return new View(stage, female, familiar, fed, feeder, pregnancy, progress, milk, shear, eggs, facts);
    }

    // A hen counts the eggs she laid on the day the book last saw her, if that is today; a rooster says whether he makes them fertile.
    private static Text eggsToday(HerdRecord record, AnimalTraits traits, AgeStage stage, double vigour, long today, BarnyardConfig config) {
        if (record.baby() || !config.eggsFor(record.species()).enabled()) {
            return Text.EMPTY;
        }
        StatsApi stats = FeaturesRepository.get(StatsApi.class);
        Text value;
        if (traits.sex() == Sex.MALE) {
            value = stats.roosterText(stage == AgeStage.YOUNG, vigour,
                    FeaturesRepository.get(VigourApi.class).hungry(FeaturesRepository.get(FamiliarityApi.class).lastAteDay(traits), today),
                    config.tuning().vigourPerEgg());
        } else if (traits.sex() == Sex.FEMALE && (stage == AgeStage.ADULT || stage == AgeStage.OLD)) {
            Laying laying = record.sheet().laying().day() == today ? record.sheet().laying() : Laying.NONE;
            value = stats.eggsText(laying.laid(), laying.planned(), FeaturesRepository.get(FamiliarityApi.class).wasHungryYesterday(traits, today));
        } else {
            return Text.EMPTY;
        }
        return value.isEmpty() ? Text.EMPTY : stats.labelled(stats.eggRowLabel(traits.sex() == Sex.FEMALE, record.species()), value);
    }

    // What a living adult hen lays tomorrow, from what was around her when the book last saw her; null for others.
    private static @Nullable EggOutlook eggs(HerdRecord record, AnimalTraits traits, AgeStage stage, long clock, BarnyardConfig config) {
        EggSettings settings = config.eggsFor(record.species());
        if (!record.alive() || !settings.enabled() || traits.sex() != Sex.FEMALE || (stage != AgeStage.ADULT && stage != AgeStage.OLD)) {
            return null;
        }
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        return FeaturesRepository.get(EggsApi.class).eggsTomorrow(
                SizeClass.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, record.species())),
                familiarityApi.lastAteDay(traits) == GameDay.of(clock),
                familiarityApi.isFamiliar(traits),
                record.nest().rooster(),
                record.nest().boxWithRoom(),
                settings,
                record.species()
        );
    }

    // Deaths from before the clock was recorded fall back to game time.
    private static long diedDay(HerdRecord record) {
        return record.diedClock() >= 0L ? GameDay.of(record.diedClock()) : record.diedAt() / GameDay.TICKS;
    }
}
