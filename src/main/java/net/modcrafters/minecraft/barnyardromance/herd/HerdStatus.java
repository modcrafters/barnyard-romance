package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.Aging;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.eggs.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.WildCaught;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.milk.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Fertility;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.shear.ShearRule;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.stats.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourRules;
import net.modcrafters.minecraft.barnyardromance.stats.StatSheet;
import net.modcrafters.minecraft.barnyardromance.stats.StatText;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
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
                view.fedToday(),
                view.pregnancy() == null ? -1 : PregnancyText.percent(view.progress()),
                view.milk().map(status -> status.left() > 0).orElse(false),
                view.shear(),
                Optional.ofNullable(view.eggs()).map(EggOutlook::kind),
                eggTip,
                Optional.ofNullable(node.mother()),
                Optional.ofNullable(node.father()),
                StatSheet.lines(view.facts()).stream()
                        .map(line -> new HerdEntry.Line(line.kind(), TextComponents.of(line.text()), (float) line.fill()))
                        .toList()
        );
    }

    // The stat sheet's values for a record, worked out the way LiveFacts works them out for the animal itself.
    public static AnimalFacts facts(HerdRecord record, HerdLedger.Node node, long now, long clock, boolean loaded) {
        return view(record, node, now, clock, loaded).facts();
    }

    // Everything worked out once, for the page's lines and for the parts of the entry that sit beside them.
    private record View(AgeStage stage, boolean female, Familiar familiar, boolean fedToday, @Nullable Pregnancy pregnancy,
                        double progress, Optional<MilkStatus> milk, ShearState shear, @Nullable EggOutlook eggs, AnimalFacts facts) {
    }

    private static View view(HerdRecord record, HerdLedger.Node node, long now, long clock, boolean loaded) {
        BarnyardConfig config = BarnyardConfig.current();
        AnimalTraits traits = loaded || !record.alive() ? record.traits() : hungry(record, clock, config);
        AgeSettings age = config.ageFor(record.species());
        // The dead are shown as they were when they died.
        long at = record.alive() ? now : record.diedAt();
        AgeStage stage = Aging.stage(record.baby(), traits.bornTick(), traits.growthTicks(), at, age);
        boolean female = traits.sex() == Sex.FEMALE;
        boolean grown = stage == AgeStage.ADULT || stage == AgeStage.OLD;
        Familiar familiar = Familiarity.ready(traits.familiarity())
                ? Familiar.FAMILIAR
                : node.everFamiliar() ? Familiar.NO_LONGER : Familiar.NOT_YET;
        boolean fedToday = Familiarity.fedToday(traits.lastFedTick(), clock);
        Pregnancy pregnancy = record.alive() ? record.pregnancy() : null;
        double progress = pregnancy == null ? 0.0 : PregnancyClock.progress(now, pregnancy);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), config.sizeFor(record.species()));
        Optional<MilkStatus> milk = female && record.milkable() && record.alive()
                ? MilkActions.status(traits, size, now, clock)
                : Optional.empty();
        ShearState shear = record.alive()
                ? ShearRule.state(record.shearable(), record.woolly(), record.baby(), traits, clock)
                : ShearState.NONE;
        EggOutlook eggs = eggs(record, traits, stage, clock, config);
        RecordSheet sheet = record.sheet();
        long today = GameDay.of(clock);
        double vigour = VigourRules.settle(traits.vigour(), traits.hunger().lastAteDay(), today).value();
        boolean showsVigour = traits.sex() == Sex.MALE && grown;
        Text breeding = StatText.breeding(
                sheet.needsTaming(),
                record.tamed(),
                traits.familiarity(),
                stage == AgeStage.YOUNG,
                female && Fertility.wornOut(traits, age, at),
                traits.loveUntil(),
                traits.cooldownUntil(),
                now
        );
        AnimalFacts facts = AnimalFacts.builder(size.name(), stage)
                .ageTicks(traits.bornTick() < 0L ? -1L : Math.max(0L, at - traits.bornTick()))
                .fedToday(fedToday)
                .ticksSinceFed(traits.lastFedTick() < 0L ? -1L : Math.max(0L, clock - traits.lastFedTick()))
                .pregnant(pregnancy != null)
                .pregnancy(progress)
                .dueTicks(pregnancy == null ? 0L : PregnancyClock.remainingTicks(now, pregnancy))
                .milk(milk.map(StatText::milkText).orElse(Text.EMPTY))
                .familiarity(traits.familiarity())
                .fertility(female ? Fertility.current(traits, age, at) : -1.0)
                .seenTicksAgo(loaded || !record.alive() ? -1L : Math.max(0L, now - record.refreshedAt()))
                .familiar(familiar)
                .diedDay(record.alive() ? -1L : diedDay(record))
                .shear(shear)
                .grumpy(record.alive() && record.tamed() && !Familiarity.ready(traits.familiarity()))
                .eggs(eggs)
                .cap(WildCaught.cap(traits.origin(), sheet.needsTaming() || record.tamed(), config.tuning()))
                .killedByRival(!record.alive() && record.cause() == DeathCause.RIVAL)
                .growthTicks(traits.growthTicks())
                .weight(traits.weight())
                .health(sheet.health())
                .maxHealth(sheet.maxHealth())
                .vigour(showsVigour ? vigour : -1.0)
                .vigourHungry(traits.sex() == Sex.MALE && VigourRules.hungry(traits.hunger().lastAteDay(), today))
                .eggsToday(record.alive() ? eggsToday(record, traits, stage, vigour, today, config) : Text.EMPTY)
                .breeding(breeding)
                .father(pregnancy == null ? Text.EMPTY : StatText.father(
                        SizeClass.of(Sex.MALE, pregnancy.fatherScale(), config.sizeFor(pregnancy.fatherSpecies())).name(), pregnancy.fatherSpecies()))
                .lastBirth(StatText.lastBirth(LiveFacts.offspring(traits), traits.lastBirthTick(), now))
                .build();
        return new View(stage, female, familiar, fedToday, pregnancy, progress, milk, shear, eggs, facts);
    }

    // A hen counts the eggs she laid on the day the book last saw her, if that is today; a rooster says whether he makes them fertile.
    private static Text eggsToday(HerdRecord record, AnimalTraits traits, AgeStage stage, double vigour, long today, BarnyardConfig config) {
        if (record.baby() || !config.eggsFor(record.species()).enabled()) {
            return Text.EMPTY;
        }
        Text value;
        if (traits.sex() == Sex.MALE) {
            value = StatText.roosterText(stage == AgeStage.YOUNG, vigour, VigourRules.hungry(traits.hunger().lastAteDay(), today),
                    config.tuning().vigourPerEgg());
        } else if (traits.sex() == Sex.FEMALE && (stage == AgeStage.ADULT || stage == AgeStage.OLD)) {
            Laying laying = record.sheet().laying().day() == today ? record.sheet().laying() : Laying.NONE;
            value = StatText.eggsText(laying.laid(), laying.planned(), traits.hunger().hungryYesterday(today));
        } else {
            return Text.EMPTY;
        }
        return value.isEmpty() ? Text.EMPTY : StatText.labelled(StatText.eggRowLabel(traits.sex() == Sex.FEMALE, record.species()), value);
    }

    // What a living adult hen lays tomorrow, from what was around her when the book last saw her; null for others.
    private static @Nullable EggOutlook eggs(HerdRecord record, AnimalTraits traits, AgeStage stage, long clock, BarnyardConfig config) {
        EggSettings settings = config.eggsFor(record.species());
        if (!record.alive() || !settings.enabled() || traits.sex() != Sex.FEMALE || (stage != AgeStage.ADULT && stage != AgeStage.OLD)) {
            return null;
        }
        return EggOutlook.tomorrow(
                SizeClass.of(traits.sex(), traits.scale(), config.sizeFor(record.species())),
                traits.hunger().lastAteDay() == GameDay.of(clock),
                Familiarity.ready(traits.familiarity()),
                record.nest().rooster(),
                record.nest().boxWithRoom(),
                settings,
                record.species()
        );
    }

    // An animal far from home goes on missing meals; the book shows the familiarity it has lost since it was seen.
    private static AnimalTraits hungry(HerdRecord record, long clock, BarnyardConfig config) {
        AnimalTraits traits = record.traits();
        Hunger.Settled settled = Hunger.settle(traits.hunger(), GameDay.of(clock), record.baby());
        double familiarity = Hunger.afterHunger(traits.familiarity(), settled.hungryDays(), config.tuning().familiarityHungerLoss());
        return traits.fed(familiarity, traits.lastFedTick()).withHunger(settled.hunger());
    }

    // Deaths from before the clock was recorded fall back to game time.
    private static long diedDay(HerdRecord record) {
        return record.diedClock() >= 0L ? GameDay.of(record.diedClock()) : record.diedAt() / GameDay.TICKS;
    }
}
