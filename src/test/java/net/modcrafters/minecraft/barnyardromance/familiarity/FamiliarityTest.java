package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.text.English;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamiliarityTest {
    @Test
    void aWildAnimalStartsUnfamiliar() {
        assertFalse(Familiarity.ready(0.0));
    }

    // Fourteen 5% losses from 100% leave a hair under 0.3 in doubles; that is still 30%, so still familiar.
    @Test
    void fourteenLossesFromFullAreStillFamiliar() {
        double familiarity = 1.0;
        for (int day = 0; day < 14; day++) {
            familiarity -= 0.05;
        }

        assertTrue(familiarity < 0.3);
        assertTrue(Familiarity.ready(familiarity));
        assertFalse(Familiarity.ready(familiarity - 0.05));
    }

    @Test
    void twoFeedsAreEnoughToWorkAndFiveFillTheBar() {
        double once = Familiarity.gain(0.0);
        double twice = Familiarity.gain(once);

        assertEquals(0.2, once, 1.0e-9);
        assertFalse(Familiarity.ready(once));
        assertTrue(Familiarity.ready(twice));
        assertEquals(1.0, Familiarity.gain(Familiarity.gain(Familiarity.gain(twice))), 1.0e-9);
    }

    // Clock times: 0 is waking up, 18,000 is midnight, when a new day starts.
    @Test
    void feedingIsAcceptedOncePerDay() {
        assertEquals(Familiarity.Response.ACCEPT, Familiarity.respond(true, -1L, 0L));
        assertEquals(Familiarity.Response.REJECT, Familiarity.respond(true, 1_000L, 17_999L));
        assertEquals(Familiarity.Response.ACCEPT, Familiarity.respond(true, 1_000L, 18_000L));
        assertEquals(Familiarity.Response.IGNORE, Familiarity.respond(false, -1L, 0L));
    }

    @Test
    void anAnimalFedAtDuskCanBeFedAgainAfterMidnight() {
        assertEquals(Familiarity.Response.ACCEPT, Familiarity.respond(true, 17_000L, 18_000L));
        assertEquals(Familiarity.Response.REJECT, Familiarity.respond(true, 18_000L, 41_999L));
    }

    @Test
    void settingTheClockBackStartsANewDay() {
        assertEquals(Familiarity.Response.ACCEPT, Familiarity.respond(true, 100_000L, 1_000L));
    }

    @Test
    void loveHeartsNeedFamiliarityAndAnEmptyWomb() {
        assertFalse(Familiarity.allowsLove(0.2, false));
        assertTrue(Familiarity.allowsLove(0.3, false));
        assertFalse(Familiarity.allowsLove(1.0, true));
    }

    @Test
    void aNewbornKeepsHalfOfItsMothersFamiliarity() {
        assertEquals(0.5, Familiarity.inherited(1.0), 1.0e-9);
        assertEquals(0.15, Familiarity.inherited(0.3), 1.0e-9);
    }

    @Test
    void aFedAnimalShowsTheTimeUntilTheNextFeed() {
        assertEquals(0L, Familiarity.ticksUntilFeed(-1L, 100L));
        assertEquals(18_000L, Familiarity.ticksUntilFeed(0L, 0L));
        assertEquals(1L, Familiarity.ticksUntilFeed(0L, 17_999L));
        assertEquals(0L, Familiarity.ticksUntilFeed(0L, 18_000L));
        assertEquals("Can be fed again in 20m 0s.", English.of(Familiarity.nextFeed(24_000L)));
        assertEquals("Can be fed again in 30s.", English.of(Familiarity.nextFeed(600L)));
        assertEquals("75%", English.of(Familiarity.percent(0.75)));
        assertEquals(1.0, Familiarity.feedReady(-1L, 0L), 1.0e-9);
        assertEquals(0.25, Familiarity.feedReady(0L, 0L), 1.0e-9);
        assertEquals(0.5, Familiarity.feedReady(0L, 6_000L), 1.0e-9);
    }

    @Test
    void anUnfamiliarAnimalDoesNotChaseFood() {
        assertTrue(Familiarity.interested(false, 0L, 0L, false));
        assertFalse(Familiarity.interested(true, -1L, 0L, false));
        assertTrue(Familiarity.interested(true, -1L, 0L, true));
        assertFalse(Familiarity.interested(true, 0L, 0L, true));
    }

    @Test
    void aShyAnimalMustBeSneakFed() {
        assertEquals(Familiarity.Click.SCARE, Familiarity.click(false, false, Familiarity.Response.ACCEPT));
        assertEquals(Familiarity.Click.FEED, Familiarity.click(true, false, Familiarity.Response.ACCEPT));
        assertEquals(Familiarity.Click.FEED, Familiarity.click(false, true, Familiarity.Response.ACCEPT));
    }

    @Test
    void eitherClickWaitsWhenTheAnimalWasFedToday() {
        assertEquals(Familiarity.Click.WAIT, Familiarity.click(true, false, Familiarity.Response.REJECT));
        assertEquals(Familiarity.Click.WAIT, Familiarity.click(false, true, Familiarity.Response.REJECT));
        assertEquals(Familiarity.Click.SCARE, Familiarity.click(false, false, Familiarity.Response.REJECT));
    }

    @Test
    void hurtAnimalsHealInsideTheWait() {
        assertEquals(Familiarity.Click.HEAL_ONLY, Familiarity.click(true, false, Familiarity.Response.REJECT, true));
        assertEquals(Familiarity.Click.HEAL_ONLY, Familiarity.click(false, true, Familiarity.Response.REJECT, true));
        assertEquals(Familiarity.Click.FEED, Familiarity.click(false, true, Familiarity.Response.ACCEPT, true));
        assertEquals(Familiarity.Click.WAIT, Familiarity.click(false, true, Familiarity.Response.REJECT, false));
    }

    @Test
    void hurtButShyStillScares() {
        assertEquals(Familiarity.Click.SCARE, Familiarity.click(false, false, Familiarity.Response.REJECT, true));
        assertEquals(Familiarity.Click.SCARE, Familiarity.click(false, false, Familiarity.Response.ACCEPT, true));
    }

    @Test
    void hurtAnimalsFollowFoodInsideTheWait() {
        long fed = 1_000L;
        assertFalse(Familiarity.interested(true, fed, fed + 10L, true, false));
        assertTrue(Familiarity.interested(true, fed, fed + 10L, true, true));
        assertFalse(Familiarity.interested(true, fed, fed + 10L, false, true));
    }

    @Test
    void familiarityTamesAWildHorseOnceItCanBreed() {
        assertFalse(Familiarity.tames(false, Familiarity.gain(0.0)));
        assertTrue(Familiarity.tames(false, Familiarity.threshold()));
        assertTrue(Familiarity.tames(false, 1.0));
        assertFalse(Familiarity.tames(true, 1.0));
    }

    @Test
    void aTamedAnimalBelowTheThresholdIsGrumpy() {
        assertEquals("20% · Grumpy", English.of(Familiarity.text(0.2, true)));
        assertEquals("20%", English.of(Familiarity.text(0.2, false)));
        assertEquals("50%", English.of(Familiarity.text(0.5, true)));
    }
}
