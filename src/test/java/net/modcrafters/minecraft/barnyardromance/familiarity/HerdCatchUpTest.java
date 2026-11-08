package net.modcrafters.minecraft.barnyardromance.familiarity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HerdCatchUpTest {
    private static final UUID A = new UUID(0, 1);
    private static final UUID B = new UUID(0, 2);
    private static final UUID C = new UUID(0, 3);

    private static List<HerdCatchUp.Member> three() {
        return List.of(new HerdCatchUp.Member(A, 0), new HerdCatchUp.Member(B, 0), new HerdCatchUp.Member(C, 0));
    }

    @Test
    void foodForTwoDaysOfTenFeedsEveryoneTwoDays() {
        List<List<UUID>> days = HerdCatchUp.eaters(three(), 10, 6, id -> true);
        assertEquals(10, days.size());
        assertEquals(List.of(A, B, C), days.get(0));
        assertEquals(List.of(A, B, C), days.get(1));
        for (int i = 2; i < days.size(); i++) {
            assertEquals(List.of(), days.get(i));
        }
    }

    @Test
    void foodRunningOutMidDayFeedsTheFirstOnes() {
        List<List<UUID>> days = HerdCatchUp.eaters(three(), 10, 4, id -> true);
        assertEquals(List.of(A, B, C), days.get(0));
        assertEquals(List.of(A), days.get(1));
        assertEquals(List.of(), days.get(2));
    }

    @Test
    void aLateMemberJoinsFromItsOwnDay() {
        List<HerdCatchUp.Member> members = List.of(new HerdCatchUp.Member(A, 0), new HerdCatchUp.Member(B, 2));
        List<List<UUID>> days = HerdCatchUp.eaters(members, 4, 100, id -> true);
        assertEquals(List.of(A), days.get(0));
        assertEquals(List.of(A), days.get(1));
        assertEquals(List.of(A, B), days.get(2));
        assertEquals(List.of(A, B), days.get(3));
    }

    @Test
    void notEatingMembersAreSkipped() {
        List<List<UUID>> days = HerdCatchUp.eaters(three(), 3, 4, id -> !id.equals(B));
        assertEquals(List.of(A, C), days.get(0));
        assertEquals(List.of(A, C), days.get(1));
        assertEquals(List.of(), days.get(2));
    }
}
