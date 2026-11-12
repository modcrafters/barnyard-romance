package net.modcrafters.minecraft.barnyardromance.saves;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.modcrafters.minecraft.barnyardromance.herd.HerdTool;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.eggs.WildEgg;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Refusals;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;
import net.modcrafters.minecraft.barnyardromance.herd.HerdComponents;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCare;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCodecs;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.SkippedTime;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.vigour.Vigour;
import net.modcrafters.minecraft.barnyardromance.BarnyardFeatures;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.milk.MilkSlices;
import net.modcrafters.minecraft.barnyardromance.pregnancy.BreedingState;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancySlices;
import net.modcrafters.minecraft.barnyardromance.shear.ShearSlices;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Pins how 0.0.3 writes animal traits, pregnancies, layings, eggs and herd books to disk. The fixtures under
// src/test/resources/saves are never edited: a change that can't read or write them would break players' worlds.
class SaveFixturesTest {
    // The animal record's codecs take every feature's slice.
    @BeforeAll
    static void installFeatures() {
        BarnyardFeatures.ensureInstalled();
    }

    private static final UUID MOTHER = UUID.fromString("0a1b2c3d-0000-4000-8000-000000000001");
    private static final UUID FATHER = UUID.fromString("0a1b2c3d-0000-4000-8000-000000000002");
    private static final UUID BREEDER = UUID.fromString("0a1b2c3d-0000-4000-8000-000000000003");
    private static final UUID HERD_BOOK = UUID.fromString("0a1b2c3d-0000-4000-8000-000000000004");
    // Vanilla NbtOps writes an int list as a plain list on 26.3 but as an int array ([I;...]) on 1.21.1, so a laying's
    // times are written differently by each version. Each version must write its own form and read both.
    //? if >=1.21.5 {
    private static final String LAYING_AS_WRITTEN = "laying_full";
    //?} else
    //private static final String LAYING_AS_WRITTEN = "laying_full_1_21_1";

    // Every field away from its default.
    static AnimalTraits fullTraits() {
        return AnimalTraits.of(Sex.MALE, 1.125)
                .withGrowthTicks(30000)
                .withWeight(0.625)
                .withBornTick(1000L)
                .withLineage(new Lineage(MOTHER, FATHER))
                .with(FamiliaritySlices.CARE, new FamiliarityState(0.75, 12000L, new Hunger(5L, 4L, 3L), Origin.FARM, new Refusals(6L, 7L),
                        new Upkeep(true, true, 10L, "trough", true)))
                .with(PregnancySlices.BREEDING, new BreedingState(13000L, "minecraft:cow", 14000L, 15000L, 0.875, 16000L))
                .with(MilkSlices.LACTATION, new Lactation(17000L, 18000L, 3L, 2, true))
                .with(ShearSlices.LAST_SHEARED, 19000L)
                .with(VigourSlices.VIGOUR, new Vigour(0.375, 8L, 9L));
    }

    static AnimalTraits defaultTraits() {
        return AnimalTraits.of(Sex.FEMALE, 1.0);
    }

    static Pregnancy fullPregnancy() {
        CompoundTag calf = new CompoundTag();
        calf.putString("id", "minecraft:cow");
        calf.putInt("Age", -24000);
        CompoundTag twin = new CompoundTag();
        twin.putString("id", "minecraft:cow");
        twin.putInt("Age", -20000);
        return new Pregnancy(
                1000L,
                48000L,
                "minecraft:cow",
                1.125,
                2,
                0.625,
                30000,
                0.75,
                List.of(calf, twin),
                new PregnancyCare(100L, 2000L, 3000L, 1.5, 0.25),
                FATHER,
                new SkippedTime(4000L, 5000L),
                BREEDER
        );
    }

    static Laying fullLaying() {
        return new Laying(12L, List.of(1000, 6000, 12000), 2, 1);
    }

    static EggData eggData() {
        return new EggData("minecraft:chicken", MOTHER, FATHER, 1.125, 20000, 0.625, 0.75, 0.875, 26000, 0.375,
                "minecraft:warm", 24000L, 12000L);
    }

    static WildEgg wildEgg() {
        return new WildEgg("minecraft:chicken", "minecraft:cold", 24000L, 6000L);
    }

    static UUID herdBook() {
        return HERD_BOOK;
    }

    static HerdTool herdTool() {
        return new HerdTool(HerdTool.Kind.PREGNANCY, HerdTool.Change.SET, 40);
    }

    @Test
    void animalTraitsFullDecodes() {
        assertEquals(fullTraits(), decode(AnimalTraitCodecs.CODEC, "animal_traits_full"));
    }

    @Test
    void animalTraitsFullEncodes() {
        assertEquals(Snbt.fixture("animal_traits_full"), encode(AnimalTraitCodecs.CODEC, fullTraits()));
    }

    @Test
    void animalTraitsDefaultDecodes() {
        assertEquals(defaultTraits(), decode(AnimalTraitCodecs.CODEC, "animal_traits_default"));
    }

    @Test
    void animalTraitsDefaultEncodes() {
        assertEquals(Snbt.fixture("animal_traits_default"), encode(AnimalTraitCodecs.CODEC, defaultTraits()));
    }

    // A 0.0.1 save had only these four fields; everything since reads as unknown or none.
    @Test
    void animalTraits001Decodes() {
        AnimalTraits traits = decode(AnimalTraitCodecs.CODEC, "animal_traits_0_0_1");

        assertEquals(Upkeep.UNKNOWN, traits.get(FamiliaritySlices.CARE).upkeep());
        assertEquals(Vigour.NONE, traits.get(VigourSlices.VIGOUR));
        assertEquals(Growth.wild(), traits.growthTicks());
        assertEquals(Hunger.NONE, traits.get(FamiliaritySlices.CARE).hunger());
        assertEquals(Origin.UNKNOWN, traits.get(FamiliaritySlices.CARE).origin());
        assertEquals(AnimalTraits.of(Sex.FEMALE, 0.875).with(FamiliaritySlices.CARE, FamiliarityState.NONE.fed(0.5, 1200L).withUpkeep(Upkeep.UNKNOWN)), traits);
    }

    // The stream layout may change, but what goes in must come back out.
    @Test
    void animalTraitsStreamRoundTrips() {
        ByteBuf buf = Unpooled.buffer();
        AnimalTraitCodecs.STREAM_CODEC.encode(buf, fullTraits());

        assertEquals(fullTraits(), AnimalTraitCodecs.STREAM_CODEC.decode(buf));
    }

    @Test
    void pregnancyFullDecodes() {
        assertEquals(fullPregnancy(), decode(PregnancyCodecs.CODEC, "pregnancy_full"));
    }

    @Test
    void pregnancyFullEncodes() {
        assertEquals(Snbt.fixture("pregnancy_full"), encode(PregnancyCodecs.CODEC, fullPregnancy()));
    }

    @Test
    void layingFullDecodes() {
        assertEquals(fullLaying(), decode(Laying.CODEC, "laying_full"));
    }

    @Test
    void layingFull1211Decodes() {
        assertEquals(fullLaying(), decode(Laying.CODEC, "laying_full_1_21_1"));
    }

    @Test
    void layingFullEncodes() {
        assertEquals(Snbt.fixture(LAYING_AS_WRITTEN), encode(Laying.CODEC, fullLaying()));
    }

    @Test
    void eggDataDecodes() {
        assertEquals(eggData(), decode(EggsComponents.EGG_DATA.codecOrThrow(), "egg_data"));
    }

    @Test
    void eggDataEncodes() {
        assertEquals(Snbt.fixture("egg_data"), encode(EggsComponents.EGG_DATA.codecOrThrow(), eggData()));
    }

    @Test
    void wildEggDecodes() {
        assertEquals(wildEgg(), decode(EggsComponents.WILD_EGG.codecOrThrow(), "wild_egg"));
    }

    @Test
    void wildEggEncodes() {
        assertEquals(Snbt.fixture("wild_egg"), encode(EggsComponents.WILD_EGG.codecOrThrow(), wildEgg()));
    }

    @Test
    void herdBookDecodes() {
        assertEquals(herdBook(), decode(HerdComponents.HERD_BOOK.codecOrThrow(), "herd_book"));
    }

    @Test
    void herdBookEncodes() {
        assertEquals(Snbt.fixture("herd_book"), encode(HerdComponents.HERD_BOOK.codecOrThrow(), herdBook()));
    }

    @Test
    void herdToolDecodes() {
        assertEquals(herdTool(), decode(HerdComponents.HERD_TOOL.codecOrThrow(), "herd_tool"));
    }

    @Test
    void herdToolEncodes() {
        assertEquals(Snbt.fixture("herd_tool"), encode(HerdComponents.HERD_TOOL.codecOrThrow(), herdTool()));
    }

    static <T> Tag encode(Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
    }

    private static <T> T decode(Codec<T> codec, String fixture) {
        return codec.parse(NbtOps.INSTANCE, Snbt.fixture(fixture)).getOrThrow();
    }
}
