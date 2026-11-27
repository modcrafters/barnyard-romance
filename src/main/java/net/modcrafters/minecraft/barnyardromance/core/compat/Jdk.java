package net.modcrafters.minecraft.barnyardromance.core.compat;

import java.util.List;

// The Java 21 methods the mod uses, so the same source builds for Minecraft 1.20.1, which runs on Java 17. Each is
// the real method where Java 21 has it and the same behaviour written out where it doesn't, down to the exceptions.
// The public API can't call this (it is a source set of its own), so it keeps its one clamp behind a version comment.
public final class Jdk {
    private Jdk() {
    }

    public static int clamp(long value, int min, int max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static long clamp(long value, long min, long max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static double clamp(double value, double min, double max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static float clamp(float value, float min, float max) {
        //? if >=1.21.1 {
        return Math.clamp(value, min, max);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> T first(List<T> list) {
        //? if >=1.21.1 {
        return list.getFirst();
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> T last(List<T> list) {
        //? if >=1.21.1 {
        return list.getLast();
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> T removeFirst(List<T> list) {
        //? if >=1.21.1 {
        return list.removeFirst();
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> T removeLast(List<T> list) {
        //? if >=1.21.1 {
        return list.removeLast();
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    // A live view in the opposite order, as List.reversed is.
    public static <T> List<T> reversed(List<T> list) {
        //? if >=1.21.1 {
        return list.reversed();
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static StringBuilder repeat(StringBuilder builder, CharSequence text, int count) {
        //? if >=1.21.1 {
        return builder.repeat(text, count);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }
}
