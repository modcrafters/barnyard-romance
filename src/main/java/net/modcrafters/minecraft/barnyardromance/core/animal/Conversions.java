package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.ServerScoped;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.CoreAttachments;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// An animal turned into another animal (a sheared mooshroom into a cow) keeps its record: sex, size, familiarity,
// pregnancy and its place in the herd books. The conversion hook calls carry before the new animal joins the level.
public final class Conversions {
    // What an animal turned into, and the game tick it happened on. Only that tick's conversions are ever asked about,
    // so each new conversion first drops the older ones.
    private record Conversion(Animal to, long tick) {
    }

    private static final Map<UUID, Conversion> RECENT = ServerScoped.track(new HashMap<>());

    private Conversions() {
    }

    // Copies the record when the old animal has one and the new species is switched on. Either way the conversion is
    // remembered for this tick, so the old animal leaving isn't taken for a death.
    public static void carry(Animal from, Animal to) {
        if (from.level().isClientSide()) {
            return;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(from);
        if (traits.isEmpty() || !BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(to))) {
            return;
        }
        long tick = to.level().getGameTime();
        RECENT.values().removeIf(conversion -> conversion.tick() != tick);
        for (AttachmentKey<?> key : CoreAttachments.ALL) {
            copy(from, to, key);
        }
        for (AttachmentKey<?> key : FeaturesRepository.features().attachments()) {
            copy(from, to, key);
        }
        RECENT.put(from.getUUID(), new Conversion(to, tick));
        Events.post(new AnimalEvent.Converted(from, to));
    }

    // What the animal turned into on this tick, if its record was carried over.
    public static Optional<Animal> successor(Animal from) {
        return Optional.ofNullable(RECENT.get(from.getUUID()))
                .filter(conversion -> conversion.tick() == conversion.to().level().getGameTime())
                .map(Conversion::to);
    }

    // Whether the animal with this id was turned into another one on this tick.
    public static boolean converted(UUID id) {
        Conversion conversion = RECENT.get(id);
        return conversion != null && conversion.tick() == conversion.to().level().getGameTime();
    }

    private static <T> void copy(Animal from, Animal to, AttachmentKey<T> key) {
        try {
            Services.DATA.get(from, key).ifPresent(value -> Services.DATA.set(to, key, value));
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to carry {} to the animal it became", key.id(), exception);
        }
    }
}
