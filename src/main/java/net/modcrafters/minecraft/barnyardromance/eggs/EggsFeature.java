package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;

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
        registrar.provide(EggsApi.class, new EggsApiImpl());
        registrar.attachment(LAYING);
        registrar.hook(AnimalPhase.SERVER_AI_STEP, "eggs.laying", EggLaying::serverTick);
        // Lambdas, so the holders (and the blocks and items in them) load only once the registries are open.
        registrar.content(sink -> {
            EggsComponents.register(sink);
            EggsItems.register(sink);
            EggsBlocks.register(sink);
        });
    }
}
