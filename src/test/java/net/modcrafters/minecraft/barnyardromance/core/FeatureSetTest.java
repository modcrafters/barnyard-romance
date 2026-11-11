package net.modcrafters.minecraft.barnyardromance.core;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureSetTest {
    interface GreetingApi {
        String greet();
    }

    record Ping(String note) implements AnimalEvent {
    }

    @Test
    void providedApiIsFound() {
        GreetingApi hello = () -> "hello";
        FeatureSet features = FeatureSet.of(List.of(feature("greeter", registrar -> registrar.provide(GreetingApi.class, hello))));

        assertSame(hello, features.api(GreetingApi.class));
    }

    @Test
    void missingApiThrowsNamingTheInterface() {
        FeatureSet features = FeatureSet.of(List.of(feature("quiet", registrar -> {
        })));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> features.api(GreetingApi.class));
        assertTrue(thrown.getMessage().contains(GreetingApi.class.getName()), thrown.getMessage());
    }

    @Test
    void secondProviderOfAnApiIsRejected() {
        List<Feature> features = List.of(
                feature("first", registrar -> registrar.provide(GreetingApi.class, () -> "hello")),
                feature("second", registrar -> registrar.provide(GreetingApi.class, () -> "hi")));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> FeatureSet.of(features));
        assertTrue(thrown.getMessage().contains(GreetingApi.class.getName()), thrown.getMessage());
    }

    @Test
    void registeringAfterCloseThrows() {
        AtomicReference<FeatureRegistrar> kept = new AtomicReference<>();
        FeatureSet.of(List.of(feature("late", kept::set)));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> kept.get().provide(GreetingApi.class, () -> "too late"));
        assertEquals("Feature registration is closed", thrown.getMessage());
        assertThrows(IllegalStateException.class, () -> kept.get().listen(Ping.class, ping -> {
        }));
        assertThrows(IllegalStateException.class, () -> kept.get().attachment(key("late")));
        assertThrows(IllegalStateException.class, () -> kept.get().content(sink -> {
        }));
        assertThrows(IllegalStateException.class, () -> kept.get().tabEntry("late", List::of));
        assertThrows(IllegalStateException.class, () -> kept.get().ignoredByDefault(species -> true));
    }

    @Test
    void listenersRunInRegistrationOrder() {
        List<String> heard = new ArrayList<>();
        FeatureSet features = FeatureSet.of(List.of(
                feature("first", registrar -> registrar.listen(Ping.class, ping -> heard.add("first " + ping.note()))),
                feature("second", registrar -> {
                    registrar.listen(Ping.class, ping -> heard.add("second " + ping.note()));
                    registrar.listen(Ping.class, ping -> heard.add("third " + ping.note()));
                })));

        Ping ping = new Ping("moo");
        features.listeners(Ping.class).forEach(listener -> listener.accept(ping));

        assertEquals(List.of("first moo", "second moo", "third moo"), heard);
    }

    @Test
    void twoAttachmentsWithTheSameIdAreRejected() {
        List<Feature> features = List.of(
                feature("first", registrar -> registrar.attachment(key("laying"))),
                feature("second", registrar -> registrar.attachment(key("laying"))));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> FeatureSet.of(features));
        assertTrue(thrown.getMessage().contains("laying"), thrown.getMessage());
    }

    private static AttachmentKey<Integer> key(String id) {
        return new AttachmentKey<>(id, Codec.INT, ByteBufCodecs.VAR_INT, () -> 0);
    }

    private static Feature feature(String name, Consumer<FeatureRegistrar> register) {
        return new Feature() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void register(FeatureRegistrar registrar) {
                register.accept(registrar);
            }
        };
    }
}
