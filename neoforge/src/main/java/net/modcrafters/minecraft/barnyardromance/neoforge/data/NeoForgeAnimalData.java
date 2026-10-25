package net.modcrafters.minecraft.barnyardromance.neoforge.data;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class NeoForgeAnimalData {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            Constants.MOD_ID
    );
    public static final Supplier<AttachmentType<AnimalTraits>> ANIMAL_TRAITS = ATTACHMENT_TYPES.register(
            "animal_traits",
            () -> AttachmentType.builder(() -> new AnimalTraits(Sex.MALE, 1.0))
                    .serialize(AnimalTraitCodecs.MAP_CODEC)
                    .sync(AnimalTraitCodecs.STREAM_CODEC)
                    .build()
    );
    public static final Supplier<AttachmentType<Pregnancy>> PREGNANCY = ATTACHMENT_TYPES.register(
            "pregnancy",
            () -> AttachmentType.builder(() -> new Pregnancy(0L, 1L, "", 1.0, 1, 0.0))
                    .serialize(PregnancyCodecs.MAP_CODEC)
                    .sync(PregnancyCodecs.STREAM_CODEC)
                    .build()
    );

    private NeoForgeAnimalData() {
    }
}
