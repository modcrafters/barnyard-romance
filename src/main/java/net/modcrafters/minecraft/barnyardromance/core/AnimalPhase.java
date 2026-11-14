package net.modcrafters.minecraft.barnyardromance.core;

// When an animal's hooks run (see AnimalTicks for each phase's order).
public enum AnimalPhase {
    // The end of the animal's customServerAiStep.
    SERVER_AI_STEP,
    // The end of the animal's aiStep, on the server only: some mods' animals (Naturalist's ostrich) override
    // customServerAiStep without calling it, but every animal runs aiStep.
    AI_STEP_SERVER,
    // The animal joined a server level: loaded, spawned or just born.
    JOINED,
    // The animal grew up, or became a baby again.
    AGE_BOUNDARY
}
