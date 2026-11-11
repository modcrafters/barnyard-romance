package net.modcrafters.minecraft.barnyardromance.fabric.data;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.CoreAttachments;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;

// One attachment type per key, created once the features are installed (see BarnyardRomanceFabric). Keys are
// constants, looked up by identity.
public final class FabricAnimalData {
    private static final Map<AttachmentKey<?>, AttachmentType<?>> TYPES = new IdentityHashMap<>();

    private FabricAnimalData() {
    }

    public static void register() {
        CoreAttachments.ALL.forEach(FabricAnimalData::create);
        FeaturesRepository.features().attachments().forEach(FabricAnimalData::create);
    }

    @SuppressWarnings("unchecked")
    public static <T> AttachmentType<T> type(AttachmentKey<T> key) {
        AttachmentType<?> type = TYPES.get(key);
        if (type == null) {
            throw new IllegalArgumentException("The attachment " + key.id() + " isn't registered");
        }
        return (AttachmentType<T>) type;
    }

    private static <T> void create(AttachmentKey<T> key) {
        TYPES.put(key, AttachmentRegistry.<T>create(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, key.id()),
                builder -> builder.persistent(key.codec())
                        .syncWith(key.streamCodec(), AttachmentSyncPredicate.all())
        ));
    }
}
