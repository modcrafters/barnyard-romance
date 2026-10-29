package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.Aging;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.inspect.InspectorSheet;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.milk.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Fertility;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;

import java.util.Optional;

// Works out what the herd book shows for one animal at a given time, from its record.
// Everything stored as a time keeps counting while the animal is unloaded; a baby stays a baby until it is loaded again.
public final class HerdStatus {
    private HerdStatus() {
    }

    public static HerdEntry entry(HerdRecord record, HerdLedger.Node node, long now, long clock, boolean loaded) {
        BarnyardConfig config = BarnyardConfig.current();
        AnimalTraits traits = record.traits();
        AgeSettings age = config.ageFor(record.species());
        // The dead are shown as they were when they died.
        long at = record.alive() ? now : record.diedAt();
        AgeStage stage = Aging.stage(record.baby(), traits.bornTick(), traits.growthTicks(), at, age);
        boolean female = traits.sex() == Sex.FEMALE;
        Familiar familiar = Familiarity.ready(traits.familiarity())
                ? Familiar.FAMILIAR
                : node.everFamiliar() ? Familiar.NO_LONGER : Familiar.NOT_YET;
        boolean fedToday = Familiarity.fedToday(traits.lastFedTick(), clock);
        Pregnancy pregnancy = record.alive() ? record.pregnancy() : null;
        double progress = pregnancy == null ? 0.0 : PregnancyClock.progress(now, pregnancy);
        Optional<MilkStatus> milk = female && record.milkable() && record.alive()
                ? MilkActions.status(traits, SizeClass.of(traits.sex(), traits.scale(), config.sizeFor(record.species())), now, clock)
                : Optional.empty();
        HerdPageText.PageFacts facts = new HerdPageText.PageFacts(
                SizeClass.of(traits.sex(), traits.scale(), config.sizeFor(record.species())).name(),
                stage,
                traits.bornTick() < 0L ? -1L : Math.max(0L, at - traits.bornTick()),
                fedToday,
                traits.lastFedTick() < 0L ? -1L : Math.max(0L, clock - traits.lastFedTick()),
                pregnancy != null,
                progress,
                pregnancy == null ? 0L : PregnancyClock.remainingTicks(now, pregnancy),
                milk.map(InspectorSheet::milkText).orElse(Text.EMPTY),
                traits.familiarity(),
                female ? Fertility.current(traits, age, at) : -1.0,
                loaded || !record.alive() ? -1L : Math.max(0L, now - record.refreshedAt()),
                familiar,
                record.alive() ? -1L : record.diedAt() / GameDay.TICKS
        );
        return new HerdEntry(
                node.id(),
                record.species(),
                record.name(),
                female,
                stage == AgeStage.BABY || stage == AgeStage.YOUNG,
                record.alive(),
                familiar,
                fedToday,
                pregnancy == null ? -1 : PregnancyText.percent(progress),
                milk.map(status -> status.left() > 0).orElse(false),
                Optional.ofNullable(node.mother()),
                Optional.ofNullable(node.father()),
                HerdPageText.lines(facts).stream().map(TextComponents::of).toList()
        );
    }
}
