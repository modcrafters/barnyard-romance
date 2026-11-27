package net.modcrafters.minecraft.barnyardromance.core.compat;

//? if <1.21.1 {
/*import com.google.common.collect.Lists;
import java.util.NoSuchElementException;
*///?}
import java.util.List;

// The Java 21 methods the mod uses, so the same source builds for Minecraft 1.20.1, which runs on Java 17. Each is
// the real method where Java 21 has it and the same behaviour written out where it doesn't (1.20.1), down to the
// exceptions and their messages; JdkTest runs on both.
// The public API can't call this (it is a source set of its own), so it keeps its one clamp behind a version comment.
public final class Jdk {
    private Jdk() {
    }

    public static int clamp(long value, int min, int max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*if (min > max) {
            throw new IllegalArgumentException(min + " > " + max);
        }
        return (int) Math.min(max, Math.max(value, min));
        *///?}
    }

    public static long clamp(long value, long min, long max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*if (min > max) {
            throw new IllegalArgumentException(min + " > " + max);
        }
        return Math.min(max, Math.max(value, min));
        *///?}
    }

    public static double clamp(double value, double min, double max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*if (!(min < max)) {
            if (Double.isNaN(min)) {
                throw new IllegalArgumentException("min is NaN");
            }
            if (Double.isNaN(max)) {
                throw new IllegalArgumentException("max is NaN");
            }
            if (Double.compare(min, max) > 0) {
                throw new IllegalArgumentException(min + " > " + max);
            }
        }
        return Math.min(max, Math.max(value, min));
        *///?}
    }

    public static float clamp(float value, float min, float max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*if (!(min < max)) {
            if (Float.isNaN(min)) {
                throw new IllegalArgumentException("min is NaN");
            }
            if (Float.isNaN(max)) {
                throw new IllegalArgumentException("max is NaN");
            }
            if (Float.compare(min, max) > 0) {
                throw new IllegalArgumentException(min + " > " + max);
            }
        }
        return Math.min(max, Math.max(value, min));
        *///?}
    }

    public static <T> T first(List<T> list) {
        //? if >=1.21.1 {
        return list.getFirst();
        //?} else {
        /*if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
        *///?}
    }

    public static <T> T last(List<T> list) {
        //? if >=1.21.1 {
        return list.getLast();
        //?} else {
        /*if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(list.size() - 1);
        *///?}
    }

    public static <T> T removeFirst(List<T> list) {
        //? if >=1.21.1 {
        return list.removeFirst();
        //?} else {
        /*if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.remove(0);
        *///?}
    }

    public static <T> T removeLast(List<T> list) {
        //? if >=1.21.1 {
        return list.removeLast();
        //?} else {
        /*if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.remove(list.size() - 1);
        *///?}
    }

    // A live view in the opposite order, as List.reversed is.
    public static <T> List<T> reversed(List<T> list) {
        //? if >=1.21.1 {
        return list.reversed();
        //?} else {
        /*return Lists.reverse(list);
        *///?}
    }

    public static StringBuilder repeat(StringBuilder builder, CharSequence text, int count) {
        //? if >=1.21.1 {
        return builder.repeat(text, count);
        //?} else {
        /*if (count < 0) {
            throw new IllegalArgumentException("count is negative: " + count);
        }
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder;
        *///?}
    }
}
