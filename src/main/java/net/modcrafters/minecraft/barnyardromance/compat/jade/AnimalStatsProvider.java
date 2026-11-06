package net.modcrafters.minecraft.barnyardromance.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.stats.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.JadeLines;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.StatKind;
import net.modcrafters.minecraft.barnyardromance.stats.StatLine;
import net.modcrafters.minecraft.barnyardromance.stats.StatSheet;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

// Our stat sheet as lines of Jade's tooltip, text only. Jade shows the name and health itself.
final class AnimalStatsProvider implements IEntityComponentProvider {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_stats");
    // The short card's colours for the sex mark.
    private static final int FEMALE = 0xF27BB0;
    private static final int MALE = 0x7EB6F2;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Animal animal)) {
            return;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        Optional<AnimalFacts> facts = LiveFacts.of(animal, animal.level().getGameTime(), animal.level().getOverworldClockTime());
        if (traits.isEmpty() || facts.isEmpty()) {
            return;
        }
        for (StatLine line : JadeLines.of(StatSheet.lines(facts.get()), accessor.showDetails())) {
            Component text = TextComponents.of(line.text());
            tooltip.add(line.kind() == StatKind.SIZE ? withSexMark(traits.get().sex(), text) : text);
        }
    }

    // The mark is a sibling, not the parent, so its colour stays off the size text.
    private static Component withSexMark(Sex sex, Component size) {
        boolean female = sex == Sex.FEMALE;
        Component mark = Component.literal(female ? "♀" : "♂").withColor(female ? FEMALE : MALE);
        return Component.empty().append(mark).append(" ").append(size);
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
