package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// The server side of herd books: registering, births, deaths, and keeping the records fresh.
public final class HerdBooks {
    // Loaded animals in a book are re-recorded once a minute, so a save never holds old values for long.
    public static final int REFRESH_TICKS = 1200;

    public enum RegisterResult {
        ADDED,
        ALREADY,
        NOT_FAMILIAR,
        NOT_TRACKED
    }

    private HerdBooks() {
    }

    public static RegisterResult register(ItemStack book, Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) {
            return RegisterResult.NOT_TRACKED;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty() || !BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return RegisterResult.NOT_TRACKED;
        }
        if (!Familiarity.ready(traits.get().familiarity())) {
            return RegisterResult.NOT_FAMILIAR;
        }
        HerdBookData data = HerdBookData.get(level.getServer());
        UUID id = book.get(BarnyardComponents.HERD_BOOK);
        if (id == null) {
            id = UUID.randomUUID();
            book.set(BarnyardComponents.HERD_BOOK, id);
        }
        if (!data.ledger().register(id, node(animal, traits.get(), true))) {
            return RegisterResult.ALREADY;
        }
        HerdRecords.capture(animal, level.getGameTime()).ifPresent(record -> data.put(animal.getUUID(), record));
        data.setDirty();
        return RegisterResult.ADDED;
    }

    public static void born(Animal mother, List<Animal> young) {
        if (young.isEmpty() || !(mother.level() instanceof ServerLevel level)) {
            return;
        }
        HerdBookData data = HerdBookData.get(level.getServer());
        if (data.ledger().node(mother.getUUID()) == null) {
            return;
        }
        List<HerdLedger.Node> nodes = new ArrayList<>();
        for (Animal child : young) {
            Services.DATA.get(child).ifPresent(traits -> nodes.add(node(child, traits, Familiarity.ready(traits.familiarity()))));
        }
        if (data.ledger().born(mother.getUUID(), nodes).isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        for (Animal child : young) {
            HerdRecords.capture(child, now).ifPresent(record -> data.put(child.getUUID(), record));
        }
        refresh(data, mother, now);
        data.setDirty();
    }

    // Killed and discarded animals (deaths, and conversions to a new animal) are dead to the book;
    // any other way of leaving (unloading, changing dimension) just refreshes the record.
    public static void leftLevel(Animal animal, Entity.RemovalReason reason) {
        if (!(animal.level() instanceof ServerLevel level)) {
            return;
        }
        HerdBookData data = HerdBookData.get(level.getServer());
        if (data.ledger().node(animal.getUUID()) == null) {
            return;
        }
        long now = level.getGameTime();
        refresh(data, animal, now);
        if (reason.shouldDestroy()) {
            HerdRecord record = data.record(animal.getUUID());
            if (record != null) {
                data.put(animal.getUUID(), record.died(now));
            }
            data.ledger().died(animal.getUUID());
            data.forgetOrphans();
        }
        data.setDirty();
    }

    public static void tick(MinecraftServer server, int tickCount) {
        if (tickCount % REFRESH_TICKS != 0) {
            return;
        }
        HerdBookData data = HerdBookData.get(server);
        for (HerdLedger.Node node : List.copyOf(data.ledger().nodes().values())) {
            if (!node.alive()) {
                continue;
            }
            Animal animal = find(server, node.id());
            if (animal != null) {
                refresh(data, animal, animal.level().getGameTime());
            }
        }
    }

    public static @Nullable Animal find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof Animal animal && animal.isAlive()) {
                return animal;
            }
        }
        return null;
    }

    static void refresh(HerdBookData data, Animal animal, long now) {
        try {
            HerdRecords.capture(animal, now).ifPresent(fresh -> {
                data.put(animal.getUUID(), HerdRecords.refreshed(data.record(animal.getUUID()), fresh));
                data.ledger().familiar(animal.getUUID(), Familiarity.ready(fresh.traits().familiarity()));
            });
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to record {} for the herd book", AnimalSex.speciesId(animal), exception);
        }
    }

    private static HerdLedger.Node node(Animal animal, AnimalTraits traits, boolean familiar) {
        Lineage lineage = traits.lineage();
        return new HerdLedger.Node(animal.getUUID(), lineage.mother(), lineage.father(), true, familiar);
    }
}
