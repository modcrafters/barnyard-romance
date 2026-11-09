package net.modcrafters.minecraft.barnyardromance.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
//? if >=1.21.5 {
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
*///?}

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// The advancement credit ledger, saved once per world (on the overworld).
public final class CreditData extends SavedData {
    private record Player(UUID id, Map<String, Integer> births, List<CreditLedger.Held> held) {
    }

    private static final Codec<CreditLedger.Held> HELD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("advancement").forGetter(CreditLedger.Held::advancement),
            Codec.STRING.fieldOf("criterion").forGetter(CreditLedger.Held::criterion)
    ).apply(instance, CreditLedger.Held::new));
    private static final Codec<Player> PLAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Player::id),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("births", Map.of()).forGetter(Player::births),
            HELD_CODEC.listOf().optionalFieldOf("held", List.of()).forGetter(Player::held)
    ).apply(instance, Player::new));
    public static final Codec<CreditData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PLAYER_CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(CreditData::players)
    ).apply(instance, CreditData::new));
    //? if >=1.21.5 {
    public static final SavedDataType<CreditData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "advancement_credit"),
            CreditData::new,
            CODEC,
            null
    );
    //?} else {
    /*private static final String NAME = Constants.MOD_ID + "_advancement_credit";
    private static final SavedData.Factory<CreditData> FACTORY = new SavedData.Factory<>(CreditData::new, CreditData::load, null);
    *///?}

    private final CreditLedger ledger;

    public CreditData() {
        this.ledger = new CreditLedger();
    }

    private CreditData(List<Player> players) {
        Map<UUID, Map<String, Integer>> births = new LinkedHashMap<>();
        Map<UUID, List<CreditLedger.Held>> held = new LinkedHashMap<>();
        for (Player player : players) {
            births.put(player.id(), player.births());
            held.put(player.id(), player.held());
        }
        this.ledger = CreditLedger.of(births, held);
    }

    public static CreditData get(MinecraftServer server) {
        //? if >=1.21.5 {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        //?} else
        //return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    //? if <1.21.5 {
    /*private static CreditData load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get("credit"))
                .resultOrPartial(error -> Constants.LOG.error("Failed to load advancement credit: {}", error))
                .orElseGet(CreditData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(error -> Constants.LOG.error("Failed to save advancement credit: {}", error))
                .ifPresent(credit -> tag.put("credit", credit));
        return tag;
    }
    *///?}

    // Changes go through the ledger directly; callers mark the data dirty.
    public CreditLedger ledger() {
        return ledger;
    }

    private List<Player> players() {
        Map<UUID, Map<String, Integer>> births = ledger.births();
        Map<UUID, List<CreditLedger.Held>> held = ledger.held();
        Map<UUID, Player> players = new LinkedHashMap<>();
        births.forEach((id, counts) -> players.put(id, new Player(id, counts, held.getOrDefault(id, List.of()))));
        held.forEach((id, grants) -> players.putIfAbsent(id, new Player(id, Map.of(), grants)));
        return List.copyOf(players.values());
    }
}
