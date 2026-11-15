package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalSubject;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatLine;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatsApi;

import java.util.List;

final class StatsApiImpl implements StatsApi {
    @Override
    public List<StatLine> lines(AnimalFacts facts) {
        return StatSheet.lines(facts);
    }

    @Override
    public AnimalFacts facts(AnimalSubject subject, long now, long clock) {
        return FactSheet.of(subject, now, clock);
    }

    @Override
    public Text milkText(MilkStatus status) {
        return StatText.milkText(status);
    }
}
