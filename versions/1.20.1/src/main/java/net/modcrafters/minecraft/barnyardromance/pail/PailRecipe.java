package net.modcrafters.minecraft.barnyardromance.pail;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.modcrafters.minecraft.barnyardromance.core.Constants;

// The 1.20.1 copy of src/.../pail/PailRecipe: recipes load from JSON and travel as FriendlyByteBuf here. It is
// normal shaped crafting, including the recipe book, with a metal pail definition in place of a result stack: the
// file is read as a shaped recipe whose result is the pail item, and the pail's name and tint go on that result. The
// client gets a plain shaped recipe with the finished result, which is all it needs to show and craft it.
final class PailRecipe extends ShapedRecipe {
    static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "pail_shaped");

    static final RecipeSerializer<ShapedRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public ShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            MetalPail pail = MetalPail.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(json, "pail"))
                    .getOrThrow(false, error -> {
                    });
            JsonObject shaped = json.deepCopy();
            shaped.remove("pail");
            JsonObject result = new JsonObject();
            result.addProperty("item", BuiltInRegistries.ITEM.getKey(pail.item()).toString());
            shaped.add("result", result);
            return new PailRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, shaped), pail);
        }

        @Override
        public ShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
        }
    };

    private PailRecipe(ShapedRecipe shaped, MetalPail pail) {
        super(shaped.getId(), shaped.getGroup(), shaped.category(), shaped.getWidth(), shaped.getHeight(),
                shaped.getIngredients(), result(pail), shaped.showNotification());
    }

    private static ItemStack result(MetalPail pail) {
        ItemStack stack = new ItemStack(pail.item());
        pail.applyTo(stack);
        return stack;
    }

    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return SERIALIZER;
    }
}
