package net.modcrafters.minecraft.barnyardromance.herd;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraitCodecs;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;
//? if >=1.21.5 {
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
*///?}

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Every herd book's list and the records of the animals in them, saved once per world (on the overworld).
public final class HerdBookData extends SavedData {
    private record Book(UUID id, List<UUID> members) {
    }

    private record Entry(HerdLedger.Node node, HerdRecord record) {
    }

    private static final Codec<HerdRecord> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("species").forGetter(HerdRecord::species),
            Codec.STRING.optionalFieldOf("name", "").forGetter(HerdRecord::name),
            AnimalTraitCodecs.CODEC.fieldOf("traits").forGetter(HerdRecord::traits),
            PregnancyCodecs.CODEC.optionalFieldOf("pregnancy").forGetter(record -> Optional.ofNullable(record.pregnancy())),
            Codec.BOOL.optionalFieldOf("baby", false).forGetter(HerdRecord::baby),
            Codec.BOOL.optionalFieldOf("milkable", false).forGetter(HerdRecord::milkable),
            Codec.BOOL.optionalFieldOf("shearable", false).forGetter(HerdRecord::shearable),
            Codec.BOOL.optionalFieldOf("woolly", false).forGetter(HerdRecord::woolly),
            Codec.LONG.fieldOf("refreshedAt").forGetter(HerdRecord::refreshedAt),
            Codec.LONG.optionalFieldOf("diedAt", -1L).forGetter(HerdRecord::diedAt)
    ).apply(instance, (species, name, traits, pregnancy, baby, milkable, shearable, woolly, refreshedAt, diedAt) ->
            new HerdRecord(species, name, traits, pregnancy.orElse(null), baby, milkable, shearable, woolly, refreshedAt, diedAt)));
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(entry -> entry.node().id()),
            UUIDUtil.CODEC.optionalFieldOf("motherId").forGetter(entry -> Optional.ofNullable(entry.node().mother())),
            UUIDUtil.CODEC.optionalFieldOf("fatherId").forGetter(entry -> Optional.ofNullable(entry.node().father())),
            Codec.BOOL.fieldOf("alive").forGetter(entry -> entry.node().alive()),
            Codec.BOOL.optionalFieldOf("everFamiliar", false).forGetter(entry -> entry.node().everFamiliar()),
            RECORD_CODEC.fieldOf("record").forGetter(Entry::record)
    ).apply(instance, (id, mother, father, alive, everFamiliar, record) ->
            new Entry(new HerdLedger.Node(id, mother.orElse(null), father.orElse(null), alive, everFamiliar), record)));
    private static final Codec<Book> BOOK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Book::id),
            UUIDUtil.CODEC.listOf().fieldOf("members").forGetter(Book::members)
    ).apply(instance, Book::new));
    public static final Codec<HerdBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BOOK_CODEC.listOf().optionalFieldOf("books", List.of()).forGetter(HerdBookData::books),
            ENTRY_CODEC.listOf().optionalFieldOf("animals", List.of()).forGetter(HerdBookData::entries)
    ).apply(instance, HerdBookData::new));
    //? if >=1.21.5 {
    public static final SavedDataType<HerdBookData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_books"),
            HerdBookData::new,
            CODEC,
            null
    );
    //?} else {
    /*private static final String NAME = Constants.MOD_ID + "_herd_books";
    private static final SavedData.Factory<HerdBookData> FACTORY = new SavedData.Factory<>(HerdBookData::new, HerdBookData::load, null);
    *///?}

    private final HerdLedger ledger;
    private final Map<UUID, HerdRecord> records = new LinkedHashMap<>();

    public HerdBookData() {
        this.ledger = new HerdLedger();
    }

    private HerdBookData(List<Book> books, List<Entry> entries) {
        Map<UUID, Set<UUID>> lists = new LinkedHashMap<>();
        books.forEach(book -> lists.put(book.id(), new LinkedHashSet<>(book.members())));
        Map<UUID, HerdLedger.Node> nodes = new LinkedHashMap<>();
        for (Entry entry : entries) {
            nodes.put(entry.node().id(), entry.node());
            records.put(entry.node().id(), entry.record());
        }
        this.ledger = HerdLedger.of(lists, nodes);
    }

    public static HerdBookData get(MinecraftServer server) {
        //? if >=1.21.5 {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        //?} else
        //return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    //? if <1.21.5 {
    /*private static HerdBookData load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get("herd"))
                .resultOrPartial(error -> Constants.LOG.error("Failed to load herd books: {}", error))
                .orElseGet(HerdBookData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(error -> Constants.LOG.error("Failed to save herd books: {}", error))
                .ifPresent(herd -> tag.put("herd", herd));
        return tag;
    }
    *///?}

    // Changes go through the ledger directly; callers mark the data dirty.
    public HerdLedger ledger() {
        return ledger;
    }

    public @Nullable HerdRecord record(UUID animal) {
        return records.get(animal);
    }

    public void put(UUID animal, HerdRecord record) {
        records.put(animal, record);
        setDirty();
    }

    // Drops the records of animals that no book holds any more.
    public void forgetOrphans() {
        Set<UUID> orphans = ledger.orphans();
        if (orphans.isEmpty()) {
            return;
        }
        orphans.forEach(records::remove);
        ledger.forget(orphans);
        setDirty();
    }

    private List<Book> books() {
        List<Book> books = new ArrayList<>();
        ledger.books().forEach((id, members) -> books.add(new Book(id, List.copyOf(members))));
        return books;
    }

    private List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        ledger.nodes().forEach((id, node) -> {
            HerdRecord record = records.get(id);
            if (record != null) {
                entries.add(new Entry(node, record));
            }
        });
        return entries;
    }
}
