package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;

public final class PregnancyFeature implements Feature {
    // A mother's pregnancy, from conception to birth.
    public static final AttachmentKey<Pregnancy> PREGNANCY = new AttachmentKey<>("pregnancy", PregnancyCodecs.CODEC,
            PregnancyCodecs.STREAM_CODEC, () -> new Pregnancy(0L, 1L, "", 1.0, 1, 0.0));

    @Override
    public String name() {
        return "pregnancy";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.slice(PregnancySlices.BREEDING);
        registrar.attachment(PREGNANCY);
        registrar.hook(AnimalPhase.SERVER_AI_STEP, "pregnancy.catch_up", PregnancyController::catchUp);
        registrar.hook(AnimalPhase.SERVER_AI_STEP, "pregnancy.hearts", PregnancyHearts::serverTick);
    }
}
