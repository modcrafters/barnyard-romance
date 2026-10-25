package net.modcrafters.minecraft.barnyardromance.command;

public final class HerdToolRules {
    public static final String CREATIVE_ONLY = "Set is only available in creative mode.";

    private HerdToolRules() {
    }

    public static boolean allowed(HerdTool tool, boolean creative) {
        return creative || !tool.sets();
    }

    public static boolean consumed(HerdTool tool, boolean creative) {
        return !creative && !tool.inspects();
    }
}
