package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

// A feeding block item's tooltip: "Feeds: Cow, Sheep, …", the animals in its feeds_from tag, a few to a line so a long
// list stays narrow. Species turned off in the config still show: a client can't see a server's config.
public final class FeederTooltip {
    public static final String KEY = "item.barnyard_romance.feeding_block.feeds";
    static final int NAMES_PER_LINE = 4;

    private FeederTooltip() {
    }

    public static List<Component> lines(FeederKind kind) {
        List<Component> names = new ArrayList<>();
        for (Holder<EntityType<?>> holder : BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(kind.animals())) {
            names.add(holder.value().getDescription());
        }
        return lines(names);
    }

    // Every line but the last ends with a comma, as the list runs on to the next.
    public static List<Component> lines(List<Component> names) {
        List<Component> lines = new ArrayList<>();
        for (int from = 0; from < names.size(); from += NAMES_PER_LINE) {
            int to = Math.min(from + NAMES_PER_LINE, names.size());
            MutableComponent row = Component.empty();
            for (int i = from; i < to; i++) {
                if (i > from) {
                    row.append(", ");
                }
                row.append(names.get(i));
            }
            if (to < names.size()) {
                row.append(",");
            }
            MutableComponent line = from == 0 ? Component.translatable(KEY, row) : row;
            lines.add(line.withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
