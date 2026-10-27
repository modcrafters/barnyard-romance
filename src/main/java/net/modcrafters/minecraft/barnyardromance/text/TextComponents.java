package net.modcrafters.minecraft.barnyardromance.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TextComponents {
    private TextComponents() {
    }

    public static Component of(Text text) {
        return switch (text) {
            case Text.Key key -> Component.translatable(key.key(), key.args().stream().map(TextComponents::argument).toArray());
            case Text.Plain plain -> Component.literal(plain.value());
            case Text.Join join -> {
                MutableComponent joined = Component.empty();
                for (int index = 0; index < join.parts().size(); index++) {
                    if (index > 0) {
                        joined.append(join.separator());
                    }
                    joined.append(of(join.parts().get(index)));
                }
                yield joined;
            }
        };
    }

    private static Object argument(Object argument) {
        return argument instanceof Text text ? of(text) : argument;
    }
}
