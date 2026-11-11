package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.HerdStick;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.feed.FeederKind;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
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
        return List.of(BarnyardItems.BREEDERS_MASH, BarnyardItems.GOLDEN_BRAN, item("dairy_mash"), item("stud_mash"));
    }

    // The Herd Sticks are there only while the config's herdSticks is on. The loaders call this each time the tab is
    // built, so it reads the loaded config, never a default.
    public static List<ItemStack> contents() {
        return contents(BarnyardConfig.current().herdSticks());
    }

    public static List<ItemStack> contents(boolean herdSticks) {
        List<ItemStack> contents = new ArrayList<>();
        contents.add(new ItemStack(BarnyardItems.HERD_BOOK));
        contents.add(new ItemStack(BarnyardBlocks.NESTING_BOX_ITEM));
        for (FeederKind kind : FeederKind.values()) {
            contents.add(new ItemStack(BarnyardBlocks.item(kind)));
        }
        if (herdSticks) {
            contents.add(HerdStick.create(HerdTool.info()));
            contents.add(HerdStick.create(HerdTool.familiarity(1)));
            contents.add(HerdStick.create(HerdTool.familiarity(-1)));
            contents.add(HerdStick.create(HerdTool.pregnancy(1)));
            contents.add(HerdStick.create(HerdTool.pregnancy(-1)));
        }
        contents.add(new ItemStack(BarnyardItems.BREEDERS_MASH));
        contents.add(new ItemStack(BarnyardItems.GOLDEN_BRAN));
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
