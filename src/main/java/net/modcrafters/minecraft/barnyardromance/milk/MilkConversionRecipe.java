package net.modcrafters.minecraft.barnyardromance.milk;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
//? if >=1.21.2 {
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
*///?}

// One bucket of any milk in c:buckets/milk, alone in the grid, turns into vanilla's milk bucket. The recipe only loads
// while the milkConversion switch is on. Vanilla's own bucket is left out (it's in the tag on both loaders, and
// converting it would be a free no-op), and the bucket the milk came in isn't handed back: the goat and sheep milk
// buckets have an empty bucket as their craft remainder, which would make every conversion a free bucket.
public class MilkConversionRecipe extends CustomRecipe {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "milk_conversion");
    private static final TagKey<Item> MILK_BUCKETS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "buckets/milk"));
    //? if >=1.21.2 {
    public static final MilkConversionRecipe INSTANCE = new MilkConversionRecipe();
    public static final RecipeSerializer<MilkConversionRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(INSTANCE), StreamCodec.<RegistryFriendlyByteBuf, MilkConversionRecipe>unit(INSTANCE));
    //?} else {
    /*public static final RecipeSerializer<MilkConversionRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(MilkConversionRecipe::new);

    public MilkConversionRecipe(CraftingBookCategory category) {
        super(category);
    }
    *///?}

    // A recipe to call directly, for tests: the conversion is only in the recipe manager while the switch is on.
    public static MilkConversionRecipe create() {
        //? if >=1.21.2 {
        return INSTANCE;
        //?} else
        //return new MilkConversionRecipe(CraftingBookCategory.MISC);
    }

    // Whether the grid holds exactly one milk bucket other than vanilla's.
    public static boolean convertible(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return false;
            }
            found = stack;
        }
        return found.is(MILK_BUCKETS) && !found.is(Items.MILK_BUCKET);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return convertible(input);
    }

    //? if >=1.21.2 {
    @Override
    public ItemStack assemble(CraftingInput input) {
        return result(input);
    }
    //?} else {
    /*@Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }
    *///?}

    private static ItemStack result(CraftingInput input) {
        return convertible(input) ? new ItemStack(Items.MILK_BUCKET) : ItemStack.EMPTY;
    }

    // The milk buckets' own remainder is an empty bucket, which a conversion must not return.
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<MilkConversionRecipe> getSerializer() {
        return SERIALIZER;
    }
}
