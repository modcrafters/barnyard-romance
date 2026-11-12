package net.modcrafters.minecraft.barnyardromance.core.text;

import java.util.List;

// Text for players, kept free of Minecraft classes so pure helpers can build it and unit tests can read it.
// TextComponents turns it into a Component. Arguments are Text, String or Number.
public sealed interface Text permits Text.Key, Text.Plain, Text.Join {
    Text EMPTY = new Plain("");

    // A line from the lang file, with its %s filled in order.
    record Key(String key, List<Object> args) implements Text {
        public Key {
            args = List.copyOf(args);
        }
    }

    // Shown as it is: names, numbers, file paths.
    record Plain(String value) implements Text {
    }

    record Join(String separator, List<Text> parts) implements Text {
        public Join {
            parts = List.copyOf(parts);
        }
    }

    static Text of(String key, Object... args) {
        return new Key(key, List.of(args));
    }

    static Text plain(String value) {
        return new Plain(value);
    }

    static Text join(String separator, List<Text> parts) {
        return new Join(separator, parts);
    }

    // A value from 0 to 1 as a whole percent.
    static Text percent(double value) {
        long points = Math.round(Math.clamp(value, 0.0, 1.0) * 100.0);
        return of("gui.barnyard_romance.percent", points);
    }

    default boolean isEmpty() {
        return this instanceof Plain plain && plain.value().isEmpty();
    }
}
