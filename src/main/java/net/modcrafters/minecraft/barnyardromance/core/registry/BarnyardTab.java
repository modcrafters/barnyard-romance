package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// The mod's own creative tab. Each loader brings its builder and fills it from contents(),
// because CreativeModeTab.Output is only widened in the loaders. An item a feature registers is named by its id, and
// looked up when the tab is filled, after registration. Stacks that need more than an id are a tab entry the feature
// registers under a name (see FeatureRegistrar.tabEntry), also looked up when the tab is filled.
public final class BarnyardTab {
    public static final ResourceKey<CreativeModeTab> ID = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID)
    );
    // Vanilla keeps its tab keys private; the superfoods sit with the other food.
    public static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.withDefaultNamespace("food_and_drinks")
    );

    private BarnyardTab() {
    }

    public static CreativeModeTab.Builder titled(CreativeModeTab.Builder builder) {
        return builder
                .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                .icon(() -> new ItemStack(item("herd_book")));
    }

    // The superfoods the loaders add to vanilla's Food & Drinks tab, as well as to ours.
    public static List<Item> foodAndDrinks() {
        return List.of(item("breeders_mash"), item("golden_bran"), item("dairy_mash"), item("stud_mash"));
    }

    // The loaders call this each time the tab is built, so an entry that follows the config (the Herd Sticks, there
    // only while herdSticks is on) reads the loaded config, never a default.
    public static List<ItemStack> contents() {
        List<ItemStack> contents = new ArrayList<>();
        contents.add(new ItemStack(item("herd_book")));
        contents.add(new ItemStack(item("nesting_box")));
        contents.add(new ItemStack(item("bird_feeder")));
        contents.add(new ItemStack(item("trough")));
        contents.add(new ItemStack(item("feeder")));
        contents.add(new ItemStack(item("manger")));
        contents.add(new ItemStack(item("pet_bowl")));
        contents.addAll(entry("herd_sticks"));
        contents.add(new ItemStack(item("breeders_mash")));
        contents.add(new ItemStack(item("golden_bran")));
        contents.add(new ItemStack(item("dairy_mash")));
        contents.add(new ItemStack(item("stud_mash")));
        contents.add(new ItemStack(item("sheep_milk_bucket")));
        contents.add(new ItemStack(item("goat_milk_bucket")));
        return contents;
    }

    private static Item item(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
        return BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> new IllegalStateException("No item " + id + " is registered"));
    }

    private static List<ItemStack> entry(String name) {
        Supplier<List<ItemStack>> stacks = FeaturesRepository.features().tabEntries().get(name);
        if (stacks == null) {
            throw new IllegalStateException("No feature registers the tab entry " + name);
        }
        return stacks.get();
    }
}
