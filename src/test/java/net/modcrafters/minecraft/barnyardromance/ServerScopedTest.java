package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerScopedTest {
    @Test
    void tracksAndClearsMapsAndSets() {
        Map<String, Long> map = new HashMap<>();
        Set<String> set = new HashSet<>();
        assertSame(map, ServerScoped.track(map));
        assertSame(set, ServerScoped.track(set));
        map.put("truce", 10L);
        set.add("hen");

        ServerScoped.clearAll();

        assertTrue(map.isEmpty());
        assertTrue(set.isEmpty());
    }

    @Test
    void runsRegisteredClears() {
        AtomicInteger cleared = new AtomicInteger();
        ServerScoped.onStop(cleared::incrementAndGet);

        ServerScoped.clearAll();
        ServerScoped.clearAll();

        assertEquals(2, cleared.get());
    }
}
