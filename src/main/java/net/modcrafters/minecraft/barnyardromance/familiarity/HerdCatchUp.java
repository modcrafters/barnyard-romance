package net.modcrafters.minecraft.barnyardromance.familiarity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

// Spreads a feeder's food over the days a herd missed, earliest day first, so a short store keeps the early days
// fed and the late days go hungry.
public final class HerdCatchUp {
    private HerdCatchUp() {
    }

    public record Member(UUID id, long fromDay) {
    }

    // Entry i lists who ate on day (earliest fromDay + i), for every day before today.
    public static List<List<UUID>> eaters(List<Member> members, long today, int food, Predicate<UUID> eats) {
        List<List<UUID>> days = new ArrayList<>();
        if (members.isEmpty()) {
            return days;
        }
        long first = Long.MAX_VALUE;
        for (Member member : members) {
            first = Math.min(first, member.fromDay());
        }
        int left = food;
        for (long day = first; day < today; day++) {
            List<UUID> fed = new ArrayList<>();
            for (Member member : members) {
                if (left <= 0) {
                    break;
                }
                if (day >= member.fromDay() && eats.test(member.id())) {
                    fed.add(member.id());
                    left--;
                }
            }
            days.add(fed);
        }
        return days;
    }
}
