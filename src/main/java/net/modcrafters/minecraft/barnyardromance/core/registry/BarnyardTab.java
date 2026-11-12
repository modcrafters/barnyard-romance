package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.HerdStick;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
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

// The mod's own creative tab. Each loader brings its builder and fills it from contents(),
// because CreativeModeTab.Output is only widened in the loaders. An item a feature registers is named by its id, and
// looked up when the tab is filled, after registration.
public final class BarnyardTab {
    public static final ResourceKey<CreativeModeTab> ID = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID)
    );

    private BarnyardTab() {
    }

    public static CreativeModeTab.Builder titled(CreativeModeTab.Builder builder) {
        return builder
                .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                .icon(() -> new ItemStack(BarnyardItems.HERD_BOOK));
    }

    // The superfoods the loaders add to vanilla's Food & Drinks tab, as well as to ours.
    public static List<Item> foodAndDrinks() {
        return List.of(item("breeders_mash"), item("golden_bran"), item("dairy_mash"), item("stud_mash"));
    }

    // The Herd Sticks are there only while the config's herdSticks is on. The loaders call this each time the tab is
    // built, so it reads the loaded config, never a default.
    public static List<ItemStack> contents() {
        return contents(BarnyardConfig.current().herdSticks());
    }

    public static List<ItemStack> contents(boolean herdSticks) {
        List<ItemStack> contents = new ArrayList<>();
        contents.add(new ItemStack(BarnyardItems.HERD_BOOK));
        contents.add(new ItemStack(item("nesting_box")));
        contents.add(new ItemStack(item("bird_feeder")));
        contents.add(new ItemStack(item("trough")));
        contents.add(new ItemStack(item("feeder")));
        contents.add(new ItemStack(item("manger")));
        contents.add(new ItemStack(item("pet_bowl")));
        if (herdSticks) {
            contents.add(HerdStick.create(HerdTool.info()));
            contents.add(HerdStick.create(HerdTool.familiarity(1)));
            contents.add(HerdStick.create(HerdTool.familiarity(-1)));
            contents.add(HerdStick.create(HerdTool.pregnancy(1)));
            contents.add(HerdStick.create(HerdTool.pregnancy(-1)));
        }
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
}
