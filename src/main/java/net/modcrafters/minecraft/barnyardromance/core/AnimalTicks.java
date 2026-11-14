package net.modcrafters.minecraft.barnyardromance.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// The steps run on every animal, phase by phase. Features register their steps by name (FeatureRegistrar.hook), and
// each phase runs them in the order written here, whatever order the features registered in.
public final class AnimalTicks {
    private static final Map<AnimalPhase, List<String>> ORDER = Map.of(
            AnimalPhase.SERVER_AI_STEP, List.of("pregnancy.catch_up", "pregnancy.hearts", "core.age", "familiarity.care", "eggs.laying"),
            AnimalPhase.AI_STEP_SERVER, List.of("rivalry.courtship", "rivalry.fights", "rivalry.rival_rolls", "familiarity.escapes"),
            // Traits first: milk's first join reads the sex they give.
            AnimalPhase.JOINED, List.of("breeding.traits", "pregnancy.joined", "milk.joined"),
            AnimalPhase.AGE_BOUNDARY, List.of("breeding.scale")
    );
    // Steps core runs itself: every animal ages, whatever features there are.
    private static final Map<String, AnimalHook> CORE_HOOKS = Map.of("core.age", AgeController::serverTick);

    private AnimalTicks() {
    }

    public static List<String> order(AnimalPhase phase) {
        return ORDER.get(phase);
    }

    public static void run(AnimalPhase phase, Animal animal, ServerLevel level) {
        for (AnimalHook hook : FeaturesRepository.features().hooks(phase).values()) {
            hook.run(animal, level);
        }
    }

    // Joining a client level runs nothing.
    public static void joined(Animal animal) {
        if (animal.level() instanceof ServerLevel level) {
            run(AnimalPhase.JOINED, animal, level);
        }
    }

    static Map<String, AnimalHook> coreHooks() {
        return CORE_HOOKS;
    }

    // Throws unless the name has a place in the phase, so a misspelt hook can't sit unrun.
    static void checkPlaced(AnimalPhase phase, String name) {
        if (!ORDER.get(phase).contains(name)) {
            throw new IllegalStateException("Tick hook " + name + " has no place in AnimalTicks");
        }
    }

    // Each phase's hooks, by name in the written order. Throws if a placed name has no hook, so a forgotten
    // registration can't silently drop a step.
    static Map<AnimalPhase, Map<String, AnimalHook>> arrange(Map<String, AnimalHook> registered) {
        Map<AnimalPhase, Map<String, AnimalHook>> arranged = new EnumMap<>(AnimalPhase.class);
        for (AnimalPhase phase : AnimalPhase.values()) {
            Map<String, AnimalHook> hooks = new LinkedHashMap<>();
            for (String name : ORDER.get(phase)) {
                AnimalHook hook = registered.get(name);
                if (hook == null) {
                    throw new IllegalStateException("Nobody registers tick hook " + name);
                }
                hooks.put(name, hook);
            }
            arranged.put(phase, Collections.unmodifiableMap(hooks));
        }
        return Collections.unmodifiableMap(arranged);
    }
}
