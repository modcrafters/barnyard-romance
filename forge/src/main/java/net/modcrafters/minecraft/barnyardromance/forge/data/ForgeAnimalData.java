package net.modcrafters.minecraft.barnyardromance.forge.data;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.CoreAttachments;
import net.modcrafters.minecraft.barnyardromance.forge.mixin.CapabilityProviderAccessor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Forge 1.20.1 has no data attachments, so every key's value lives in one capability on each animal,
// barnyard_romance:animal_data, which saves each key under its id with its codec. The keys are those NeoForge registers
// one attachment type for: the core ones and every feature's, looked up by identity.
public final class ForgeAnimalData {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "animal_data");
    public static final Capability<Values> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    // By id, in the order they were added, for saving and for the sync payload.
    private static final Map<String, AttachmentKey<?>> KEYS = new LinkedHashMap<>();

    private ForgeAnimalData() {
    }

    public static void register(IEventBus modBus) {
        CoreAttachments.ALL.forEach(ForgeAnimalData::add);
        FeaturesRepository.features().attachments().forEach(ForgeAnimalData::add);
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.register(Values.class));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, ForgeAnimalData::attach);
    }

    public static Optional<AttachmentKey<?>> key(String id) {
        return Optional.ofNullable(KEYS.get(id));
    }

    // As NeoForge refuses a key it has no attachment type for.
    public static void requireRegistered(AttachmentKey<?> key) {
        if (KEYS.get(key.id()) != key) {
            throw new IllegalArgumentException("The attachment " + key.id() + " isn't registered");
        }
    }

    // The animal's values. Forge stops handing out an entity's capabilities once it's removed, but the mod still reads
    // an animal as it leaves (a death, a conversion), as NeoForge's attachments let it; so this asks the capabilities
    // the animal holds directly.
    public static @Nullable Values of(Animal animal) {
        CapabilityDispatcher capabilities = ((CapabilityProviderAccessor) animal).barnyardromance$capabilities();
        return capabilities == null ? null : capabilities.getCapability(CAPABILITY).resolve().orElse(null);
    }

    private static void add(AttachmentKey<?> key) {
        AttachmentKey<?> previous = KEYS.putIfAbsent(key.id(), key);
        if (previous != null && previous != key) {
            throw new IllegalArgumentException("Two animal data keys share the id " + key.id());
        }
    }

    private static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Animal animal) {
            event.addCapability(ID, new Provider(animal));
        }
    }

    // What one animal holds, by key. Only touched on its level's thread.
    public static final class Values {
        private final Map<AttachmentKey<?>, Object> values = new IdentityHashMap<>();

        @SuppressWarnings("unchecked")
        public <T> @Nullable T get(AttachmentKey<T> key) {
            return (T) values.get(key);
        }

        public <T> void set(AttachmentKey<T> key, T value) {
            values.put(key, value);
        }

        public void remove(AttachmentKey<?> key) {
            values.remove(key);
        }

        public Collection<AttachmentKey<?>> keys() {
            return Collections.unmodifiableSet(values.keySet());
        }
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final Animal animal;
        private final Values values = new Values();
        private final LazyOptional<Values> optional = LazyOptional.of(() -> values);

        Provider(Animal animal) {
            this.animal = animal;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            return CAPABILITY.orEmpty(capability, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag saved = new CompoundTag();
            DynamicOps<Tag> ops = ops();
            for (AttachmentKey<?> key : KEYS.values()) {
                save(saved, ops, key);
            }
            return saved;
        }

        private <T> void save(CompoundTag saved, DynamicOps<Tag> ops, AttachmentKey<T> key) {
            T value = values.get(key);
            if (value == null) {
                return;
            }
            key.codec().encodeStart(ops, value)
                    .resultOrPartial(error -> Constants.LOG.error("Couldn't save {} on {}: {}", key.id(), animal, error))
                    .ifPresent(tag -> saved.put(key.id(), tag));
        }

        @Override
        public void deserializeNBT(CompoundTag saved) {
            DynamicOps<Tag> ops = ops();
            for (AttachmentKey<?> key : KEYS.values()) {
                Tag tag = saved.get(key.id());
                if (tag != null) {
                    load(ops, key, tag);
                }
            }
        }

        private <T> void load(DynamicOps<Tag> ops, AttachmentKey<T> key, Tag tag) {
            key.codec().parse(ops, tag)
                    .resultOrPartial(error -> Constants.LOG.error("Couldn't load {} on {}: {}", key.id(), animal, error))
                    .ifPresent(value -> values.set(key, value));
        }

        // As NeoForge saves attachments: with the registries, for codecs that name registry entries.
        private DynamicOps<Tag> ops() {
            return RegistryOps.create(NbtOps.INSTANCE, animal.level().registryAccess());
        }
    }
}
