package net.modcrafters.minecraft.barnyardromance.core.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TextComponents {
    private TextComponents() {
    }

    public static Component of(Text text) {
        if (text instanceof Text.Key key) {
            return Component.translatable(key.key(), key.args().stream().map(TextComponents::argument).toArray());
        }
        if (text instanceof Text.Plain plain) {
            return Component.literal(plain.value());
        }
        if (text instanceof Text.Join join) {
            MutableComponent joined = Component.empty();
            for (int index = 0; index < join.parts().size(); index++) {
                if (index > 0) {
                    joined.append(join.separator());
                }
                joined.append(of(join.parts().get(index)));
            }
            return joined;
        }
        throw new IllegalArgumentException("Unknown text " + text);
    }

    private static Object argument(Object argument) {
        return argument instanceof Text text ? of(text) : argument;
    }
}
