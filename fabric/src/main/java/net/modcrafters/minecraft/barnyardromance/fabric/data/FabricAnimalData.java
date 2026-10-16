package net.modcrafters.minecraft.barnyardromance.fabric.data;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class FabricAnimalData {
    public static final AttachmentType<AnimalTraits> ANIMAL_TRAITS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_traits"),
            builder -> builder.persistent(AnimalTraitCodecs.CODEC)
                    .syncWith(AnimalTraitCodecs.STREAM_CODEC, AttachmentSyncPredicate.all())
    );
    public static final AttachmentType<Pregnancy> PREGNANCY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pregnancy"),
            builder -> builder.persistent(PregnancyCodecs.CODEC)
                    .syncWith(PregnancyCodecs.STREAM_CODEC, AttachmentSyncPredicate.all())
    );

    private FabricAnimalData() {
    }
}
