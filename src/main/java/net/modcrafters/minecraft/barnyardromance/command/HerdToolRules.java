package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.text.Text;

public final class HerdToolRules {
    public static final Text CREATIVE_ONLY = Text.of("message.barnyard_romance.herd_stick.creative_only");

    private HerdToolRules() {
    }

    public static boolean allowed(HerdTool tool, boolean creative) {
        return creative || !tool.sets();
    }

    public static boolean consumed(HerdTool tool, boolean creative) {
        return !creative && !tool.inspects();
    }
}
