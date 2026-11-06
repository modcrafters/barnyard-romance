package net.modcrafters.minecraft.barnyardromance.stats;

import java.util.List;

// The sheet's lines for Jade's tooltip. Jade already shows the name and health, so health is left out. Normally only
// the short lines show, as on our own short card; the whole sheet shows while Jade's details key is held.
public final class JadeLines {
    private JadeLines() {
    }

    public static List<StatLine> of(List<StatLine> full, boolean details) {
        return full.stream().filter(line -> details ? line.kind() != StatKind.HEALTH : isShort(line.kind())).toList();
    }

    private static boolean isShort(StatKind kind) {
        return kind == StatKind.SIZE || kind == StatKind.FAMILIARITY || kind == StatKind.FED || kind == StatKind.NOT_FED;
    }
}
