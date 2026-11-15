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
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalSubject;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatsApi;
import net.modcrafters.minecraft.barnyardromance.herd.api.Familiar;
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
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        // An animal far from home goes on missing meals; the book shows the familiarity it has lost since it was seen.
        AnimalTraits traits = loaded || !record.alive() ? record.traits() : familiarityApi.afterMissedDays(record.traits(), GameDay.of(clock), record.baby());
        Upkeep upkeep = familiarityApi.upkeep(traits);
        AgeSettings age = config.ageFor(record.species());
        // The dead are shown as they were when they died.
        long at = record.alive() ? now : record.diedAt();
        AgeStage stage = Aging.stage(record.baby(), traits.bornTick(), traits.growthTicks(), at, age);
        boolean female = traits.sex() == Sex.FEMALE;
        Familiar familiar = familiarityApi.isFamiliar(traits)
                ? Familiar.FAMILIAR
                : node.everFamiliar() ? Familiar.NO_LONGER : Familiar.NOT_YET;
        // The contents page's icon is full for hand-feeding and half for a feeding block's meal.
        HerdEntry.Fed fed = familiarityApi.handFedToday(traits, clock)
                ? HerdEntry.Fed.HAND
                : upkeep.fedByFeederOn(GameDay.of(clock)) ? HerdEntry.Fed.FEEDER : HerdEntry.Fed.NOT_FED;
        String feeder = fed == HerdEntry.Fed.FEEDER ? upkeep.feeder() : "";
        Pregnancy pregnancy = record.alive() ? record.pregnancy() : null;
        double progress = pregnancy == null ? 0.0 : FeaturesRepository.get(PregnancyApi.class).progress(pregnancy, now);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, record.species()));
        Optional<MilkStatus> milk = female && record.milkable() && record.alive()
                ? FeaturesRepository.get(MilkApi.class).status(traits, size, now, clock)
                : Optional.empty();
        ShearState shear = record.alive()
                ? FeaturesRepository.get(ShearApi.class).shearState(record.shearable(), record.woolly(), record.baby(), traits, clock)
                : ShearState.NONE;
        EggOutlook eggs = eggs(record, traits, stage, clock, config);
        RecordSheet sheet = record.sheet();
        AnimalSubject subject = new Recorded(
                record.species(),
                traits,
                stage,
                record.baby(),
                record.alive(),
                sheet.needsTaming(),
                record.tamed(),
                at,
                sheet.health(),
                sheet.maxHealth(),
                pregnancy,
                milk,
                shear,
                familiar,
                familiarityApi.wildCap(traits, sheet.needsTaming() || record.tamed()),
                config.eggsFor(record.species()).enabled(),
                female && (stage == AgeStage.ADULT || stage == AgeStage.OLD),
                sheet.laying(),
                eggs,
                loaded || !record.alive() ? -1L : Math.max(0L, now - record.refreshedAt()),
                record.alive() ? -1L : diedDay(record),
                !record.alive() && record.cause() == DeathCause.RIVAL
        );
        AnimalFacts facts = FeaturesRepository.get(StatsApi.class).facts(subject, now, clock);
        return new View(stage, female, familiar, fed, feeder, pregnancy, progress, milk, shear, eggs, facts);
    }

    // The animal as the book last saw it, with the meals it has missed since.
    private record Recorded(String species, AnimalTraits traits, AgeStage stage, boolean baby, boolean alive, boolean needsTaming,
                            boolean tamed, long shownAt, float health, float maxHealth, @Nullable Pregnancy pregnancy,
                            Optional<MilkStatus> milk, ShearState shear, Familiar familiar, double cap, boolean laysEggs,
                            boolean layingHen, Laying laying, @Nullable EggOutlook eggsTomorrow, long seenTicksAgo, long diedDay,
                            boolean killedByRival) implements AnimalSubject {
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
