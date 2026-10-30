package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

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
        if (mother.level() instanceof ServerLevel level) {
            born(level, mother.getUUID(), young);
        }
    }

    // By the mother's id, for chicks hatching while she's away or after she has died.
    public static void born(ServerLevel level, UUID mother, List<Animal> young) {
        if (young.isEmpty()) {
            return;
        }
        HerdBookData data = HerdBookData.get(level.getServer());
        if (data.ledger().node(mother) == null) {
            return;
        }
        List<HerdLedger.Node> nodes = new ArrayList<>();
        for (Animal child : young) {
            Services.DATA.get(child).ifPresent(traits -> nodes.add(node(child, traits, Familiarity.ready(traits.familiarity()))));
        }
        if (data.ledger().born(mother, nodes).isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        for (Animal child : young) {
            HerdRecords.capture(child, now).ifPresent(record -> data.put(child.getUUID(), record));
        }
        Animal loaded = find(level.getServer(), mother);
        if (loaded != null) {
            refresh(data, loaded, now);
        }
        data.setDirty();
    }

    // Killed and discarded animals (deaths, and conversions to a new animal) are dead to the book;
    // any other way of leaving just refreshes the record. A chunk unload arrives before the entity has
    // a removal reason, so a missing reason is an unload.
    public static void leftLevel(Animal animal, Entity.@Nullable RemovalReason reason) {
        if (!(animal.level() instanceof ServerLevel level)) {
            return;
        }
        HerdBookData data = HerdBookData.get(level.getServer());
        if (data.ledger().node(animal.getUUID()) == null) {
            return;
        }
        long now = level.getGameTime();
        refresh(data, animal, now);
        if (reason != null && reason.shouldDestroy()) {
            HerdRecord record = data.record(animal.getUUID());
            if (record != null) {
                data.put(animal.getUUID(), record.died(now, level.getOverworldClockTime()));
            }
            data.ledger().died(animal.getUUID());
            data.forgetOrphans();
        }
        data.setDirty();
    }

    public static void tick(MinecraftServer server, int tickCount) {
        if (tickCount % REFRESH_TICKS == 0) {
            refreshLoaded(server);
        }
    }

    // Also run when the world saves, so a force-loaded animal's record is never a minute behind the save.
    public static void refreshLoaded(MinecraftServer server) {
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

    // Sends the whole book to the player and opens it: loaded members are recorded first, so they show as they are now.
    public static void show(ServerPlayer player, ItemStack book) {
        show(player, book, true);
    }

    // open is false for a reply, which only refreshes a book the player still has open.
    public static void show(ServerPlayer player, ItemStack book, boolean open) {
        UUID id = book.get(BarnyardComponents.HERD_BOOK);
        MinecraftServer server = player.level().getServer();
        List<HerdEntry> entries = id == null ? List.of() : entries(server, id, player.level().getGameTime());
        Services.NETWORK.sendToPlayer(player, new HerdBookPayloads.ShowBook(Optional.ofNullable(id), entries, open));
    }

    public static List<HerdEntry> entries(MinecraftServer server, UUID book, long now) {
        HerdBookData data = HerdBookData.get(server);
        long clock = server.overworld().getOverworldClockTime();
        List<HerdEntry> entries = new ArrayList<>();
        for (UUID member : List.copyOf(data.ledger().members(book))) {
            Animal animal = find(server, member);
            if (animal != null) {
                refresh(data, animal, now);
            }
            HerdRecord record = data.record(member);
            HerdLedger.Node node = data.ledger().node(member);
            if (record != null && node != null) {
                entries.add(HerdStatus.entry(record, node, now, clock, animal != null));
            }
        }
        return entries;
    }

    // Only a player holding this book can take an animal out of it.
    public static boolean removeRequested(Player player, HerdBookPayloads.RemoveFromBook request) {
        return removeRequested(player, request, held -> {
            if (player instanceof ServerPlayer serverPlayer) {
                show(serverPlayer, held, false);
            }
        });
    }

    // The player holding the book gets it back whether or not the removal worked, so their page is never stale.
    public static boolean removeRequested(Player player, HerdBookPayloads.RemoveFromBook request, Consumer<ItemStack> reply) {
        ItemStack held = heldBook(player, request.book());
        if (held == null || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        HerdBookData data = HerdBookData.get(level.getServer());
        boolean removed = data.ledger().remove(request.book(), request.animal());
        if (removed) {
            data.forgetOrphans();
            data.setDirty();
        }
        reply.accept(held);
        return removed;
    }

    private static @Nullable ItemStack heldBook(Player player, UUID book) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (book.equals(stack.get(BarnyardComponents.HERD_BOOK))) {
                return stack;
            }
        }
        return null;
    }

    public static @Nullable Animal find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof Animal animal && animal.isAlive()) {
                return animal;
            }
        }
        return null;
    }

    // A member whose species has been switched off or excluded keeps the record it had.
    static void refresh(HerdBookData data, Animal animal, long now) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return;
        }
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
