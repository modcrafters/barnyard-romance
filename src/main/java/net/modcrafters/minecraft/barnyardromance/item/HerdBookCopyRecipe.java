package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
//? if >=1.21.2 {
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
*///?}

// A herd book with a herd, a book, a feather and an ink sac, in any order, make a copy of the herd book. The copy has
// the same ID, so both books show the same herd and stay linked; the herd book itself stays in the grid. A fresh book
// has no herd yet, so there's nothing to copy.
public class HerdBookCopyRecipe extends CustomRecipe {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book_copying");
    //? if >=1.21.2 {
    public static final HerdBookCopyRecipe INSTANCE = new HerdBookCopyRecipe();
    public static final RecipeSerializer<HerdBookCopyRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(INSTANCE), StreamCodec.<RegistryFriendlyByteBuf, HerdBookCopyRecipe>unit(INSTANCE));
    //?} else {
    /*public static final RecipeSerializer<HerdBookCopyRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(HerdBookCopyRecipe::new);

    public HerdBookCopyRecipe(CraftingBookCategory category) {
        super(category);
    }
    *///?}

    // The slot of the herd book to copy, or -1 when the grid holds anything other than exactly one herd book with a
    // herd, one book, one feather and one ink sac.
    static int source(CraftingInput input) {
        int book = -1;
        int plain = 0;
        int feathers = 0;
        int ink = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(BarnyardItems.HERD_BOOK) && stack.has(BarnyardComponents.HERD_BOOK) && book < 0) {
                book = slot;
            } else if (stack.is(Items.BOOK)) {
                plain++;
            } else if (stack.is(Items.FEATHER)) {
                feathers++;
            } else if (stack.is(Items.INK_SAC)) {
                ink++;
            } else {
                return -1;
            }
        }
        return plain == 1 && feathers == 1 && ink == 1 ? book : -1;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return source(input) >= 0;
    }

    //? if >=1.21.2 {
    @Override
    public ItemStack assemble(CraftingInput input) {
        return copy(input);
    }
    //?} else {
    /*@Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return copy(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }
    *///?}

    private static ItemStack copy(CraftingInput input) {
        int slot = source(input);
        return slot < 0 ? ItemStack.EMPTY : input.getItem(slot).copyWithCount(1);
    }

    // The herd book goes back where it was.
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(input);
        int slot = source(input);
        if (slot >= 0) {
            remaining.set(slot, input.getItem(slot).copyWithCount(1));
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<HerdBookCopyRecipe> getSerializer() {
        return SERIALIZER;
    }
}
