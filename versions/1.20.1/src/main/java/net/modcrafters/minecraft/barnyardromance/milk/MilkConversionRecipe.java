package net.modcrafters.minecraft.barnyardromance.milk;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.modcrafters.minecraft.barnyardromance.core.Constants;

// The 1.20.1 copy of src/.../milk/MilkConversionRecipe: recipes here have their own id, read a CraftingContainer and
// load from JSON. One bucket of any milk in c:buckets/milk, alone in the grid, turns into vanilla's milk bucket. The
// recipe only loads while the milkConversion switch is on. Vanilla's own bucket is left out (it's in the tag, and
// converting it would be a free no-op), and the bucket the milk came in isn't handed back: the goat and sheep milk
// buckets have an empty bucket as their craft remainder, which would make every conversion a free bucket.
public class MilkConversionRecipe extends CustomRecipe {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "milk_conversion");
    private static final TagKey<Item> MILK_BUCKETS = TagKey.create(Registries.ITEM, new ResourceLocation("c", "buckets/milk"));
    public static final RecipeSerializer<MilkConversionRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(MilkConversionRecipe::new);

    public MilkConversionRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    // A recipe to call directly, for tests: the conversion is only in the recipe manager while the switch is on.
    public static MilkConversionRecipe create() {
        return new MilkConversionRecipe(ID, CraftingBookCategory.MISC);
    }

    // Whether the grid holds exactly one milk bucket other than vanilla's.
    public static boolean convertible(CraftingContainer input) {
        ItemStack found = ItemStack.EMPTY;
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
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
    public boolean matches(CraftingContainer input, Level level) {
        return convertible(input);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return result(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    private static ItemStack result(CraftingContainer input) {
        return convertible(input) ? new ItemStack(Items.MILK_BUCKET) : ItemStack.EMPTY;
    }

    // The milk buckets' own remainder is an empty bucket, which a conversion must not return.
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        return NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<MilkConversionRecipe> getSerializer() {
        return SERIALIZER;
    }
}
