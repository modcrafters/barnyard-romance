package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.data.ItemData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

// The 1.20.1 copy of src/.../herd/HerdBookCopyRecipe: recipes here have their own id, read a CraftingContainer and
// load from JSON. A herd book with a herd, a book, a feather and an ink sac, in any order, make a copy of the herd
// book. The copy has the same ID, so both books show the same herd and stay linked; the herd book itself stays in the
// grid. A fresh book has no herd yet, so there's nothing to copy.
public class HerdBookCopyRecipe extends CustomRecipe {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "herd_book_copying");
    public static final RecipeSerializer<HerdBookCopyRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(HerdBookCopyRecipe::new);

    public HerdBookCopyRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    // The slot of the herd book to copy, or -1 when the grid holds anything other than exactly one herd book with a
    // herd, one book, one feather and one ink sac.
    static int source(CraftingContainer input) {
        int book = -1;
        int plain = 0;
        int feathers = 0;
        int ink = 0;
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(HerdItems.HERD_BOOK) && ItemData.has(stack, HerdComponents.HERD_BOOK) && book < 0) {
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
    public boolean matches(CraftingContainer input, Level level) {
        return source(input) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return copy(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    private static ItemStack copy(CraftingContainer input) {
        int slot = source(input);
        return slot < 0 ? ItemStack.EMPTY : input.getItem(slot).copyWithCount(1);
    }

    // The herd book goes back where it was.
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
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
