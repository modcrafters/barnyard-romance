package net.modcrafters.minecraft.barnyardromance.core.data;

import net.minecraft.SharedConstants;
//? if >=26.1 {
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.herd.HerdComponents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// A stack keeps what ItemData puts on it, through copies and saves, on every version: as an item component from
// 1.20.5 and in the stack's NBT tag on 1.20.1.
class ItemDataTest {
    private static final UUID BOOK = UUID.fromString("0a1b2c3d-0000-4000-8000-000000000004");
    private static final UUID OTHER_BOOK = UUID.fromString("0a1b2c3d-0000-4000-8000-000000000005");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // From 26.1 an item's own components are bound when the server loads its data.
        //? if >=26.1 {
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createWorldLookup())
                .forEach(DataComponentInitializers.PendingComponents::apply);
        //?}
    }

    private static ItemStack book(UUID id) {
        ItemStack stack = new ItemStack(Items.BOOK);
        ItemData.set(stack, HerdComponents.HERD_BOOK, id);
        return stack;
    }

    @Test
    void setThenGetReturnsTheValue() {
        ItemStack stack = book(BOOK);
        assertEquals(BOOK, ItemData.get(stack, HerdComponents.HERD_BOOK));
        assertTrue(ItemData.has(stack, HerdComponents.HERD_BOOK));
        assertEquals(BOOK, ItemData.getOrDefault(stack, HerdComponents.HERD_BOOK, OTHER_BOOK));
    }

    @Test
    void aPlainStackHasNoValue() {
        ItemStack stack = new ItemStack(Items.BOOK);
        assertNull(ItemData.get(stack, HerdComponents.HERD_BOOK));
        assertFalse(ItemData.has(stack, HerdComponents.HERD_BOOK));
        assertEquals(OTHER_BOOK, ItemData.getOrDefault(stack, HerdComponents.HERD_BOOK, OTHER_BOOK));
    }

    @Test
    void aCopyKeepsTheValue() {
        assertEquals(BOOK, ItemData.get(book(BOOK).copy(), HerdComponents.HERD_BOOK));
    }

    @Test
    void differentValuesMakeDifferentStacks() {
        //? if >=1.20.5 {
        assertFalse(ItemStack.isSameItemSameComponents(book(BOOK), book(OTHER_BOOK)));
        assertTrue(ItemStack.isSameItemSameComponents(book(BOOK), book(BOOK)));
        //?} else {
        /*assertFalse(ItemStack.isSameItemSameTags(book(BOOK), book(OTHER_BOOK)));
        assertTrue(ItemStack.isSameItemSameTags(book(BOOK), book(BOOK)));
        *///?}
    }

    @Test
    void theValueSurvivesSaving() {
        //? if >=1.20.5 {
        Tag saved = HerdComponents.HERD_BOOK.codecOrThrow().encodeStart(NbtOps.INSTANCE, ItemData.get(book(BOOK), HerdComponents.HERD_BOOK))
                .result().orElseThrow();
        ItemStack loaded = new ItemStack(Items.BOOK);
        ItemData.set(loaded, HerdComponents.HERD_BOOK, HerdComponents.HERD_BOOK.codecOrThrow().parse(NbtOps.INSTANCE, saved).result().orElseThrow());
        //?} else {
        /*CompoundTag saved = book(BOOK).save(new CompoundTag());
        ItemStack loaded = ItemStack.of(saved);
        assertTrue(saved.getCompound("tag").contains("barnyard_romance:herd_book"));
        *///?}
        assertEquals(BOOK, ItemData.get(loaded, HerdComponents.HERD_BOOK));
    }

    @Test
    void removeTakesTheValueOff() {
        ItemStack stack = book(BOOK);
        ItemData.remove(stack, HerdComponents.HERD_BOOK);
        assertFalse(ItemData.has(stack, HerdComponents.HERD_BOOK));
        assertNull(ItemData.get(stack, HerdComponents.HERD_BOOK));
        ItemStack cleared = book(BOOK);
        ItemData.set(cleared, HerdComponents.HERD_BOOK, null);
        assertFalse(ItemData.has(cleared, HerdComponents.HERD_BOOK));
        // Without its data the book stacks with a plain one again.
        //? if >=1.20.5 {
        assertTrue(ItemStack.isSameItemSameComponents(stack, new ItemStack(Items.BOOK)));
        //?} else {
        /*assertTrue(ItemStack.isSameItemSameTags(stack, new ItemStack(Items.BOOK)));
        assertNull(cleared.getTag());
        *///?}
    }
}
