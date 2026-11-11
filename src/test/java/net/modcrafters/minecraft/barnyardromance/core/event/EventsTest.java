package net.modcrafters.minecraft.barnyardromance.core.event;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.core.FeatureSet;
import net.modcrafters.minecraft.barnyardromance.core.QuietTickHooks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventsTest {
    record Ping(String note) implements AnimalEvent {
    }

    @Test
    void aThrowingListenerDoesNotStopTheNext() {
        List<String> heard = new ArrayList<>();
        FeatureSet features = FeatureSet.of(QuietTickHooks.with(List.of(new Feature() {
            @Override
            public String name() {
                return "listener";
            }

            @Override
            public void register(FeatureRegistrar registrar) {
                registrar.listen(Ping.class, ping -> heard.add("before " + ping.note()));
                registrar.listen(Ping.class, ping -> {
                    throw new IllegalStateException("a listener broke");
                });
                registrar.listen(Ping.class, ping -> heard.add("after " + ping.note()));
            }
        })));

        Events.post(features, new Ping("moo"));

        assertEquals(List.of("before moo", "after moo"), heard);
    }
}
