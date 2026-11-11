package net.modcrafters.minecraft.barnyardromance.core.data;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;

import java.util.List;

// The attachments core keeps on every animal; features declare their own (see FeatureRegistrar.attachment).
public final class CoreAttachments {
    public static final AttachmentKey<AnimalTraits> ANIMAL_TRAITS = new AttachmentKey<>("animal_traits",
            AnimalTraitCodecs.CODEC, AnimalTraitCodecs.STREAM_CODEC, () -> new AnimalTraits(Sex.MALE, 1.0));
    public static final List<AttachmentKey<?>> ALL = List.of(ANIMAL_TRAITS);

    private CoreAttachments() {
    }
}
