package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.text.Text;

public record HerdTool(Kind kind, Change change, int percent) {
    public enum Kind {
        INFO,
        FAMILIARITY,
        PREGNANCY
    }

    public enum Change {
        INSPECT,
        UP,
        DOWN,
        SET
    }

    public static HerdTool info() {
        return new HerdTool(Kind.INFO, Change.INSPECT, 0);
    }

    public static HerdTool familiarity(int direction) {
        return new HerdTool(Kind.FAMILIARITY, direction < 0 ? Change.DOWN : Change.UP, 0);
    }

    public static HerdTool familiaritySet(int percent) {
        return new HerdTool(Kind.FAMILIARITY, Change.SET, percent);
    }

    public static HerdTool pregnancy(int direction) {
        return new HerdTool(Kind.PREGNANCY, direction < 0 ? Change.DOWN : Change.UP, 0);
    }

    public static HerdTool pregnancySet(int percent) {
        return new HerdTool(Kind.PREGNANCY, Change.SET, percent);
    }

    public boolean sets() {
        return change == Change.SET;
    }

    public boolean inspects() {
        return change == Change.INSPECT;
    }

    public int direction() {
        return change == Change.DOWN ? -1 : 1;
    }

    public Text label() {
        if (kind == Kind.INFO) {
            return Text.of("item.barnyard_romance.herd_stick.inspect");
        }
        if (kind == Kind.FAMILIARITY) {
            return switch (change) {
                case SET -> Text.of("item.barnyard_romance.herd_stick.familiarity_set", percent);
                case DOWN -> Text.of("item.barnyard_romance.herd_stick.familiarity_down");
                default -> Text.of("item.barnyard_romance.herd_stick.familiarity_up");
            };
        }
        return switch (change) {
            case SET -> Text.of("item.barnyard_romance.herd_stick.pregnancy_set", percent);
            case DOWN -> Text.of("item.barnyard_romance.herd_stick.pregnancy_down");
            default -> Text.of("item.barnyard_romance.herd_stick.pregnancy_up");
        };
    }
}
