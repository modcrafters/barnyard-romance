package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.eggs.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import net.modcrafters.minecraft.barnyardromance.herd.ShearState;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import org.jspecify.annotations.Nullable;

// Everything the stat sheet can say about one animal. Start from builder(), which leaves every value "not shown".
public record AnimalFacts(
        String sizeLetter,
        AgeStage stage,
        // -1 when unknown.
        long ageTicks,
        // Counted on the overworld clock, like the once-a-day rule.
        boolean fedToday,
        // -1 when never fed.
        long ticksSinceFed,
        boolean pregnant,
        // How far along, 0 to 1.
        double pregnancy,
        long dueTicks,
        // Empty when there is no milk row.
        Text milk,
        double familiarity,
        // Negative when not shown; only grown females show it.
        double fertility,
        // -1 when the animal is loaded.
        long seenTicksAgo,
        Familiar familiar,
        // -1 while alive.
        long diedDay,
        ShearState shear,
        boolean grumpy,
        // What a hen lays tomorrow; null for everything else.
        @Nullable EggOutlook eggs,
        // The highest familiarity a wild-caught animal can reach; 1 for everything else.
        double cap,
        // Killed by another male in a fight.
        boolean killedByRival,
        // Ran off for want of care; the note stays until it is familiar again.
        boolean ranAway,
        // -1 hides the row.
        long growthTicks,
        // -1 hides the row.
        double weight,
        float health,
        // 0 or less hides the health row.
        float maxHealth,
        // -1 hides the row.
        double vigour,
        boolean vigourHungry,
        // Already labelled: "Eggs: 2/3 today" or "Rooster: ...".
        Text eggsToday,
        // The value only; empty hides the row.
        Text breeding,
        Text father,
        Text lastBirth
) {
    public static Builder builder(String sizeLetter, AgeStage stage) {
        return new Builder(sizeLetter, stage);
    }

    public static final class Builder {
        private final String sizeLetter;
        private final AgeStage stage;
        private long ageTicks = -1L;
        private boolean fedToday = false;
        private long ticksSinceFed = -1L;
        private boolean pregnant = false;
        private double pregnancy = -1.0;
        private long dueTicks = -1L;
        private Text milk = Text.EMPTY;
        private double familiarity = -1.0;
        private double fertility = -1.0;
        private long seenTicksAgo = -1L;
        private Familiar familiar = Familiar.FAMILIAR;
        private long diedDay = -1L;
        private ShearState shear = ShearState.NONE;
        private boolean grumpy = false;
        private @Nullable EggOutlook eggs = null;
        private double cap = 1.0;
        private boolean killedByRival = false;
        private boolean ranAway = false;
        private long growthTicks = -1L;
        private double weight = -1.0;
        private float health = -1.0F;
        private float maxHealth = -1.0F;
        private double vigour = -1.0;
        private boolean vigourHungry = false;
        private Text eggsToday = Text.EMPTY;
        private Text breeding = Text.EMPTY;
        private Text father = Text.EMPTY;
        private Text lastBirth = Text.EMPTY;

        private Builder(String sizeLetter, AgeStage stage) {
            this.sizeLetter = sizeLetter;
            this.stage = stage;
        }

        public Builder ageTicks(long ageTicks) {
            this.ageTicks = ageTicks;
            return this;
        }

        public Builder fedToday(boolean fedToday) {
            this.fedToday = fedToday;
            return this;
        }

        public Builder ticksSinceFed(long ticksSinceFed) {
            this.ticksSinceFed = ticksSinceFed;
            return this;
        }

        public Builder pregnant(boolean pregnant) {
            this.pregnant = pregnant;
            return this;
        }

        public Builder pregnancy(double pregnancy) {
            this.pregnancy = pregnancy;
            return this;
        }

        public Builder dueTicks(long dueTicks) {
            this.dueTicks = dueTicks;
            return this;
        }

        public Builder milk(Text milk) {
            this.milk = milk;
            return this;
        }

        public Builder familiarity(double familiarity) {
            this.familiarity = familiarity;
            return this;
        }

        public Builder fertility(double fertility) {
            this.fertility = fertility;
            return this;
        }

        public Builder seenTicksAgo(long seenTicksAgo) {
            this.seenTicksAgo = seenTicksAgo;
            return this;
        }

        public Builder familiar(Familiar familiar) {
            this.familiar = familiar;
            return this;
        }

        public Builder diedDay(long diedDay) {
            this.diedDay = diedDay;
            return this;
        }

        public Builder shear(ShearState shear) {
            this.shear = shear;
            return this;
        }

        public Builder grumpy(boolean grumpy) {
            this.grumpy = grumpy;
            return this;
        }

        public Builder eggs(@Nullable EggOutlook eggs) {
            this.eggs = eggs;
            return this;
        }

        public Builder cap(double cap) {
            this.cap = cap;
            return this;
        }

        public Builder killedByRival(boolean killedByRival) {
            this.killedByRival = killedByRival;
            return this;
        }

        public Builder ranAway(boolean ranAway) {
            this.ranAway = ranAway;
            return this;
        }

        public Builder growthTicks(long growthTicks) {
            this.growthTicks = growthTicks;
            return this;
        }

        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public Builder health(float health) {
            this.health = health;
            return this;
        }

        public Builder maxHealth(float maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        public Builder vigour(double vigour) {
            this.vigour = vigour;
            return this;
        }

        public Builder vigourHungry(boolean vigourHungry) {
            this.vigourHungry = vigourHungry;
            return this;
        }

        public Builder eggsToday(Text eggsToday) {
            this.eggsToday = eggsToday;
            return this;
        }

        public Builder breeding(Text breeding) {
            this.breeding = breeding;
            return this;
        }

        public Builder father(Text father) {
            this.father = father;
            return this;
        }

        public Builder lastBirth(Text lastBirth) {
            this.lastBirth = lastBirth;
            return this;
        }

        public AnimalFacts build() {
            return new AnimalFacts(sizeLetter, stage,
                    ageTicks,
                    fedToday,
                    ticksSinceFed,
                    pregnant,
                    pregnancy,
                    dueTicks,
                    milk,
                    familiarity,
                    fertility,
                    seenTicksAgo,
                    familiar,
                    diedDay,
                    shear,
                    grumpy,
                    eggs,
                    cap,
                    killedByRival,
                    ranAway,
                    growthTicks,
                    weight,
                    health,
                    maxHealth,
                    vigour,
                    vigourHungry,
                    eggsToday,
                    breeding,
                    father,
                    lastBirth);
        }
    }
}
