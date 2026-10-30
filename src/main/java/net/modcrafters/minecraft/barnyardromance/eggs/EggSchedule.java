package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;

import java.util.ArrayList;
import java.util.List;

// When a hen lays today's eggs, in ticks since midnight on the overworld clock (GameDay): sunrise is 6,000 and
// sunset 18,000. Eggs come at random times through the daylight; a hen first seen after sunset uses what's left.
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

    public static List<Integer> plan(int sinceMidnight, int count, double[] rolls) {
        int start = sinceMidnight < SUNSET ? Math.max(sinceMidnight, SUNRISE) : sinceMidnight;
        int end = sinceMidnight < SUNSET ? SUNSET : DAY;
        List<Integer> times = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double roll = i < rolls.length ? Math.clamp(rolls[i], 0.0, 1.0) : 0.5;
            times.add(Math.min(end - 1, start + (int) Math.floor(roll * (end - start))));
        }
        times.sort(null);
        return List.copyOf(times);
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
