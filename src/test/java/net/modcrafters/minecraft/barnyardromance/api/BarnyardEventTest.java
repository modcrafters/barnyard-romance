package net.modcrafters.minecraft.barnyardromance.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarnyardEventTest {
    // A stand-in event: the tests only need something to pass around and cancel.
    private static final class Probe implements Cancellable {
        final List<Integer> seen = new ArrayList<>();
        private boolean cancelled;

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

    @Test
    void listenersRunInRegistrationOrder() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.register(probe -> probe.seen.add(1));
        event.register(probe -> probe.seen.add(2));
        event.register(probe -> probe.seen.add(3));

        assertEquals(List.of(1, 2, 3), event.post(new Probe()).seen);
    }

    @Test
    void lateListenersRunAfterOrdinaryOnes() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.registerLate(probe -> probe.seen.add(9));
        event.register(probe -> probe.seen.add(1));
        event.register(probe -> probe.seen.add(2));

        assertEquals(List.of(1, 2, 9), event.post(new Probe()).seen);
    }

    @Test
    void aThrowingListenerIsSkipped() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.register(probe -> probe.seen.add(1));
        event.register(probe -> {
            throw new IllegalStateException("a broken listener");
        });
        event.register(probe -> probe.seen.add(3));

        assertEquals(List.of(1, 3), event.post(new Probe()).seen);
    }

    // As when a listener uses a class from a mod that isn't installed.
    @Test
    void aListenerMissingAClassIsSkipped() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.register(probe -> {
            throw new NoClassDefFoundError("some/other/Mod");
        });
        event.register(probe -> probe.seen.add(2));

        assertEquals(List.of(2), event.post(new Probe()).seen);
    }

    @Test
    void aSneakilyThrownCheckedExceptionIsSkipped() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.register(probe -> BarnyardEventTest.<RuntimeException>sneaky(new java.io.IOException("a broken listener")));
        event.register(probe -> probe.seen.add(2));

        assertEquals(List.of(2), event.post(new Probe()).seen);
    }

    // The JVM itself failing is no listener's fault to log and carry on from.
    @Test
    void anOutOfMemoryErrorIsNotSwallowed() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.register(probe -> {
            throw new OutOfMemoryError("on purpose");
        });

        assertThrows(OutOfMemoryError.class, () -> event.post(new Probe()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneaky(Throwable throwable) throws T {
        throw (T) throwable;
    }

    @Test
    void postReturnsTheEvent() {
        BarnyardEvent<Probe> event = new BarnyardEvent<>("Probe");
        event.register(Probe::cancel);
        Probe probe = new Probe();

        Probe posted = event.post(probe);

        assertSame(probe, posted);
        assertTrue(posted.isCancelled());
    }
}
