package net.modcrafters.minecraft.barnyardromance.pail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
//? if >=26.3 {
import net.minecraft.world.item.ItemStackTemplate;
//?} else
//import net.minecraft.world.item.ItemStack;

// Uses normal shaped crafting, including the recipe book, with a metal pail definition in place of a result stack.
final class PailRecipe extends ShapedRecipe {
    static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pail_shaped");

    private record Definition(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                              boolean showNotification, MetalPail pail) {
        private static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(Definition::group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(Definition::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(Definition::pattern),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(Definition::showNotification),
                MetalPail.CODEC.fieldOf("pail").forGetter(Definition::pail)
        ).apply(instance, Definition::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, Definition> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Definition::group,
                CraftingBookCategory.STREAM_CODEC, Definition::category,
                ShapedRecipePattern.STREAM_CODEC, Definition::pattern,
                ByteBufCodecs.BOOL, Definition::showNotification,
                ByteBufCodecs.fromCodec(MetalPail.CODEC), Definition::pail,
                Definition::new);
    }

    private static final MapCodec<ShapedRecipe> CODEC = Definition.CODEC.xmap(PailRecipe::new, PailRecipe::definition);
    private static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC =
            Definition.STREAM_CODEC.map(PailRecipe::new, PailRecipe::definition);

    //? if >=26.3 {
    static final RecipeSerializer<ShapedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    //?} else {
    /*static final RecipeSerializer<ShapedRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<ShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };
    *///?}

    private final Definition definition;

    private PailRecipe(Definition definition) {
        //? if >=26.3 {
        // Recipes load before item components are bound, so the result is a template until crafting.
        super(new CommonInfo(definition.showNotification()), new CraftingBookInfo(definition.category(), definition.group()),
                definition.pattern(), new ItemStackTemplate(definition.pail().item(), definition.pail().components()));
        //?} else {
        /*super(definition.group(), definition.category(), definition.pattern(),
                new ItemStack(definition.pail().item().builtInRegistryHolder(), 1, definition.pail().components()),
                definition.showNotification());
        *///?}
        this.definition = definition;
    }

    private static Definition definition(ShapedRecipe recipe) {
        return ((PailRecipe) recipe).definition;
    }

    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return SERIALIZER;
    }
}
