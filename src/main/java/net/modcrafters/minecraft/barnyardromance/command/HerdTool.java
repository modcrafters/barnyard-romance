package net.modcrafters.minecraft.barnyardromance.command;

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

    public String label() {
        if (kind == Kind.INFO) {
            return "Inspect animal";
        }
        String name = kind == Kind.FAMILIARITY ? "Familiarity" : "Pregnancy";
        if (change == Change.SET) {
            return name + " " + percent + "%";
        }
        return name + (change == Change.DOWN ? " -10%" : " +10%");
    }
}
