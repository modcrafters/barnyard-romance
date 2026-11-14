package net.modcrafters.minecraft.barnyardromance.neoforge.data;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.CoreAttachments;
//? if >=1.21.6
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

// One attachment type per key, added to the deferred register once the features are installed (see
// BarnyardRomanceNeoForge). Keys are constants, looked up by identity.
public final class NeoForgeAnimalData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            Constants.MOD_ID
    );
    private static final Map<AttachmentKey<?>, Supplier<AttachmentType<?>>> TYPES = new IdentityHashMap<>();

    private NeoForgeAnimalData() {
    }

    public static void register(IEventBus modBus) {
        CoreAttachments.ALL.forEach(NeoForgeAnimalData::add);
        FeaturesRepository.features().attachments().forEach(NeoForgeAnimalData::add);
        ATTACHMENT_TYPES.register(modBus);
    }

    @SuppressWarnings("unchecked")
    public static <T> AttachmentType<T> type(AttachmentKey<T> key) {
        Supplier<AttachmentType<?>> type = TYPES.get(key);
        if (type == null) {
            throw new IllegalArgumentException("The attachment " + key.id() + " isn't registered");
        }
        return (AttachmentType<T>) type.get();
    }

    private static <T> void add(AttachmentKey<T> key) {
        Supplier<AttachmentType<T>> type = ATTACHMENT_TYPES.register(
                key.id(),
                () -> AttachmentType.builder(key.defaultValue())
                        //? if >=1.21.6 {
                        .serialize(mapCodec(key))
                        //?} else
                        //.serialize(key.codec())
                        .sync(key.streamCodec())
                        .build()
        );
        TYPES.put(key, type::get);
    }

    //? if >=1.21.6 {
    // NeoForge saves attachments with a MapCodec from 1.21.6 on. Every key's codec is a MapCodec's codec(), so the
    // attachment is saved with that same MapCodec, as before keys.
    private static <T> MapCodec<T> mapCodec(AttachmentKey<T> key) {
        if (key.codec() instanceof MapCodec.MapCodecCodec<T> wrapped) {
            return wrapped.codec();
        }
        throw new IllegalArgumentException("The attachment " + key.id() + " needs a codec made with MapCodec.codec()");
    }
    //?}
}
