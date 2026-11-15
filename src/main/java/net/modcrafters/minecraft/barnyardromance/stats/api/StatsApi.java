package net.modcrafters.minecraft.barnyardromance.stats.api;

import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;

import java.util.List;

// What other features ask the stat sheet: its lines for an animal's facts, and the facts for an animal the herd book
// keeps a record of.
public interface StatsApi {
    // The sheet's lines for the facts, in order; lines that don't apply are left out.
    List<StatLine> lines(AnimalFacts facts);

    // The facts for the subject, the way the card works them out for a loaded animal. now is the game time, clock the
    // overworld clock time.
    AnimalFacts facts(AnimalSubject subject, long now, long clock);

    // The milk row's value.
    Text milkText(MilkStatus status);
}
