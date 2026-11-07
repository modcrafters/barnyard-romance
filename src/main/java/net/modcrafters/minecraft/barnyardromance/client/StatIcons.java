package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.stats.StatKind;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

// The icon and bar colour for each kind of stat line, shared by the herd book and the sneak card.
public final class StatIcons {
    public static final int FAMILIAR_BAR = 0xFFD6523A;
    public static final int PREGNANT_BAR = 0xFFE07096;
    public static final int FERTILE_BAR = 0xFF78A840;
    public static final int GROWTH_BAR = 0xFF5B8FC4;
    public static final int WEIGHT_BAR = 0xFFB08A4A;
    public static final int VIGOUR_BAR = 0xFFE0A030;

    private static final Identifier HEART = sprite("inspector/heart");
    private static final Identifier GOLD_HEART = sprite("inspector/gold_heart");
    private static final Identifier BREEDING = sprite("inspector/breeding");
    private static final Identifier PREGNANT = sprite("herd_book/pregnant");
    private static final Identifier WHEAT = sprite("inspector/wheat");
    private static final Identifier HOURGLASS = sprite("inspector/hourglass");
    private static final Identifier WEIGHT = sprite("inspector/weight");
    private static final Identifier FERTILITY = sprite("inspector/fertility");
    private static final Identifier VIGOUR = sprite("inspector/vigour");
    private static final Identifier MILK = sprite("inspector/milk");
    private static final Identifier EGG = sprite("inspector/egg");
    private static final Identifier SHEARS = sprite("herd_book/shears");

    private StatIcons() {
    }

    private static Identifier sprite(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    public static @Nullable Identifier of(StatKind kind) {
        return switch (kind) {
            case SIZE, NOTE, DETAIL -> null;
            case FAMILIARITY -> HEART;
            case FED, NOT_FED -> WHEAT;
            case GROWTH -> HOURGLASS;
            case WEIGHT -> WEIGHT;
            case HEALTH -> GOLD_HEART;
            case BREEDING -> BREEDING;
            case FERTILITY -> FERTILITY;
            case VIGOUR -> VIGOUR;
            case MILK -> MILK;
            case SHEAR -> SHEARS;
            case EGGS -> EGG;
            case PREGNANT -> PREGNANT;
        };
    }

    // Whether lines of this kind carry a bar, as the stat line's fill describes.
    public static boolean hasBar(StatKind kind) {
        return switch (kind) {
            case FAMILIARITY, GROWTH, WEIGHT, HEALTH, FERTILITY, VIGOUR, PREGNANT -> true;
            default -> false;
        };
    }

    public static int barColor(StatKind kind) {
        return switch (kind) {
            case PREGNANT -> PREGNANT_BAR;
            case FAMILIARITY, HEALTH -> FAMILIAR_BAR;
            case GROWTH -> GROWTH_BAR;
            case WEIGHT -> WEIGHT_BAR;
            case VIGOUR -> VIGOUR_BAR;
            default -> FERTILE_BAR;
        };
    }
}
