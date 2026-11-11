package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;

public final class EggsFeature implements Feature {
    // A hen's eggs for the day: when each is due and how many she has laid.
    public static final AttachmentKey<Laying> LAYING = new AttachmentKey<>("laying", Laying.CODEC, Laying.STREAM_CODEC,
            () -> Laying.NONE);

    @Override
    public String name() {
        return "eggs";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.attachment(LAYING);
        registrar.hook(AnimalPhase.SERVER_AI_STEP, "eggs.laying", EggLaying::serverTick);
    }
}
