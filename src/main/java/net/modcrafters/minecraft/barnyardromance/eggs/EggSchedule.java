package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// When a hen lays today's eggs, in ticks since midnight on the overworld clock (GameDay): sunrise is 6,000 and
// sunset 18,000. She lays them all in one sitting, at a random time in the daylight; a hen first seen after sunset
// uses what's left of the day.
public final class EggSchedule {
    public static final int SUNRISE = 6_000;
    public static final int SUNSET = 18_000;
    // An egg this late means the hen wasn't loaded when it was due; it's skipped rather than made up.
    public static final int LATE_TICKS = 200;
    private static final int DAY = (int) GameDay.TICKS;

    private EggSchedule() {
    }

    // How far past midnight the day is; the clock wakes sleepers at 0 and reaches midnight at 18,000.
    public static int sinceMidnight(long clock) {
        return (int) Math.floorMod(clock + (GameDay.TICKS - 18_000L), GameDay.TICKS);
    }

    public static List<Integer> plan(int sinceMidnight, int count, double roll) {
        int start = sinceMidnight < SUNSET ? Math.max(sinceMidnight, SUNRISE) : sinceMidnight;
        int end = sinceMidnight < SUNSET ? SUNSET : DAY;
        int time = Math.min(end - 1, start + (int) Math.floor(Math.clamp(roll, 0.0, 1.0) * (end - start)));
        return List.copyOf(Collections.nCopies(count, time));
    }

    // For a hen back from being busy in a nesting box: eggs that came due meanwhile are due now instead of missed.
    public static List<Integer> resume(List<Integer> times, int next, int sinceMidnight) {
        List<Integer> resumed = new ArrayList<>(times);
        for (int i = next; i < resumed.size(); i++) {
            if (resumed.get(i) < sinceMidnight) {
                resumed.set(i, sinceMidnight);
            }
        }
        return List.copyOf(resumed);
    }

    // skip is how many eggs from next on were missed; layNow is whether the egg after them is due now.
    public record Due(int skip, boolean layNow) {
    }

    public static Due due(List<Integer> times, int next, int sinceMidnight) {
        int skip = 0;
        for (int i = next; i < times.size(); i++) {
            int time = times.get(i);
            if (time > sinceMidnight) {
                break;
            }
            if (sinceMidnight - time > LATE_TICKS) {
                skip++;
                continue;
            }
            return new Due(skip, true);
        }
        return new Due(skip, false);
    }
}
