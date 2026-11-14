package net.modcrafters.minecraft.barnyardromance.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnimalTicksTest {
    // The order the breeding mixin's tick tails, the join handlers and the age boundary called these in before
    // features registered them. Changing it changes the game.
    @Test
    void orderIsPinned() {
        assertEquals(List.of("pregnancy.catch_up", "pregnancy.hearts", "core.age", "familiarity.care", "eggs.laying"),
                AnimalTicks.order(AnimalPhase.SERVER_AI_STEP));
        assertEquals(List.of("rivalry.courtship", "rivalry.fights", "rivalry.rival_rolls", "familiarity.escapes"),
                AnimalTicks.order(AnimalPhase.AI_STEP_SERVER));
        assertEquals(List.of("breeding.traits", "pregnancy.joined", "milk.joined"), AnimalTicks.order(AnimalPhase.JOINED));
        assertEquals(List.of("breeding.scale"), AnimalTicks.order(AnimalPhase.AGE_BOUNDARY));
    }

    // core.age is AnimalTicks' own hook and needs a real animal, so it's only checked by its place.
    @Test
    void hooksRunInTheWrittenOrderNotRegistrationOrder() {
        List<String> ran = new ArrayList<>();
        FeatureSet features = FeatureSet.of(List.of(feature("backwards", registrar -> {
            for (AnimalPhase phase : List.of(AnimalPhase.values()).reversed()) {
                for (String name : AnimalTicks.order(phase).reversed()) {
                    if (!name.equals("core.age")) {
                        registrar.hook(phase, name, (animal, level) -> ran.add(name));
                    }
                }
            }
        })));

        for (AnimalPhase phase : AnimalPhase.values()) {
            ran.clear();
            Map<String, AnimalHook> hooks = features.hooks(phase);
            hooks.forEach((name, hook) -> {
                if (!name.equals("core.age")) {
                    hook.run(null, null);
                }
            });

            assertEquals(AnimalTicks.order(phase), List.copyOf(hooks.keySet()), phase.name());
            assertEquals(AnimalTicks.order(phase).stream().filter(name -> !name.equals("core.age")).toList(), ran, phase.name());
        }
    }

    // A misspelt name, or a name under another phase, would otherwise never run.
    @Test
    void anUnplacedHookIsRejected() {
        IllegalStateException misspelt = assertThrows(IllegalStateException.class, () -> FeatureSet.of(QuietTickHooks.with(List.of(
                feature("milk", registrar -> registrar.hook(AnimalPhase.JOINED, "milk.joind", (animal, level) -> {
                }))))));
        assertEquals("Tick hook milk.joind has no place in AnimalTicks", misspelt.getMessage());

        IllegalStateException wrongPhase = assertThrows(IllegalStateException.class, () -> FeatureSet.of(QuietTickHooks.with(List.of(
                feature("milk", registrar -> registrar.hook(AnimalPhase.AGE_BOUNDARY, "milk.joined", (animal, level) -> {
                }))))));
        assertEquals("Tick hook milk.joined has no place in AnimalTicks", wrongPhase.getMessage());
    }

    // A forgotten registration would otherwise drop that step from every animal without a word.
    @Test
    void aPlacedHookNobodyRegistersIsRejected() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> FeatureSet.of(List.of(feature("all but laying", registrar -> {
            for (AnimalPhase phase : AnimalPhase.values()) {
                for (String name : AnimalTicks.order(phase)) {
                    if (!name.equals("core.age") && !name.equals("eggs.laying")) {
                        registrar.hook(phase, name, (animal, level) -> {
                        });
                    }
                }
            }
        }))));

        assertEquals("Nobody registers tick hook eggs.laying", thrown.getMessage());
    }

    // The second would quietly replace the first. core.age counts too: AnimalTicks registers it.
    @Test
    void aHookRegisteredTwiceIsRejected() {
        IllegalStateException twice = assertThrows(IllegalStateException.class, () -> FeatureSet.of(QuietTickHooks.with(List.of(
                feature("rivalry", registrar -> registrar.hook(AnimalPhase.AI_STEP_SERVER, "rivalry.fights", (animal, level) -> {
                }))))));
        assertEquals("Tick hook rivalry.fights is registered twice; the second is from quiet tick hooks", twice.getMessage());

        IllegalStateException age = assertThrows(IllegalStateException.class, () -> FeatureSet.of(QuietTickHooks.with(List.of(
                feature("ageing", registrar -> registrar.hook(AnimalPhase.SERVER_AI_STEP, "core.age", (animal, level) -> {
                }))))));
        assertEquals("Tick hook core.age is registered twice; the second is from ageing", age.getMessage());
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
