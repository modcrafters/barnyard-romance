package net.modcrafters.minecraft.barnyardromance.inspect;

// The inspector's stat rows, top to bottom. A row that does not apply is left out and the rest move up.
public enum InspectorRow {
    FAMILIAR,
    // A wild-caught animal's highest familiarity, under its familiarity bar.
    WILD,
    FEED,
    GROWTH,
    WEIGHT,
    HEALTH,
    FERTILITY,
    // A grown male's Vigour; females and the young have none to show.
    VIGOUR,
    // Under the Vigour bar: a hungry male pays for mating with health.
    VIGOUR_HUNGRY,
    MILK,
    EGGS,
    BREEDING,
    DETAIL
}
