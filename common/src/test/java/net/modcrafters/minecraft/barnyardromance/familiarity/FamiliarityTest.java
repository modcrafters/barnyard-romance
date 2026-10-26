package net.modcrafters.minecraft.barnyardromance.familiarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamiliarityTest {
    @Test
    void aWildAnimalStartsUnfamiliar() {
        assertFalse(Familiarity.ready(0.0));
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

    @Test
    void feedingIsAcceptedOncePerDay() {
        assertEquals(Familiarity.Response.ACCEPT, Familiarity.respond(true, -1L, 0L));
        assertEquals(Familiarity.Response.REJECT, Familiarity.respond(true, 0L, Familiarity.dayTicks() - 1));
        assertEquals(Familiarity.Response.ACCEPT, Familiarity.respond(true, 0L, Familiarity.dayTicks()));
        assertEquals(Familiarity.Response.IGNORE, Familiarity.respond(false, -1L, 0L));
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
        assertEquals(Familiarity.dayTicks(), Familiarity.ticksUntilFeed(0L, 0L));
        assertEquals(1L, Familiarity.ticksUntilFeed(0L, Familiarity.dayTicks() - 1));
        assertEquals(0L, Familiarity.ticksUntilFeed(0L, Familiarity.dayTicks()));
        assertEquals("Can be fed again in 20m 0s.", Familiarity.nextFeed(Familiarity.dayTicks()));
        assertEquals("Can be fed again in 30s.", Familiarity.nextFeed(600L));
        assertEquals(1.0, Familiarity.feedReady(-1L, 0L), 1.0e-9);
        assertEquals(0.0, Familiarity.feedReady(0L, 0L), 1.0e-9);
        assertEquals(0.5, Familiarity.feedReady(0L, Familiarity.dayTicks() / 2), 1.0e-9);
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
}
