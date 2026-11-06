package net.modcrafters.minecraft.barnyardromance.rivalry;

// Who gets a female in love, from the eligible males sorted closest first. With two or more, a species that fights
// rolls once: on a fight the two closest fight, and a third, if there is one, mates with her while they do.
public final class CourtshipRules {
    private CourtshipRules() {
    }

    // Indices into the eligible list; -1 means none.
    public record Outcome(int mate, int fighterA, int fighterB) {
        public static final Outcome NONE = new Outcome(-1, -1, -1);

        public boolean fight() {
            return fighterA >= 0;
        }
    }

    public static Outcome decide(int eligible, boolean speciesFights, double roll, double fightChance) {
        if (eligible <= 0) {
            return Outcome.NONE;
        }
        if (eligible == 1 || !speciesFights || roll >= fightChance) {
            return new Outcome(0, -1, -1);
        }
        return new Outcome(eligible >= 3 ? 2 : -1, 0, 1);
    }
}
