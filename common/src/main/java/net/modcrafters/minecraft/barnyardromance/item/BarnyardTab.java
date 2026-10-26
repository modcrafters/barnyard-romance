package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// The mod's own creative tab. Each loader brings its builder and fills it from contents(),
// because CreativeModeTab.Output is only widened in the loaders.
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
                .icon(BarnyardInspector::create);
    }

    public static List<ItemStack> contents() {
        return List.of(
                BarnyardInspector.create(),
                HerdStick.create(HerdTool.info()),
                HerdStick.create(HerdTool.familiarity(1)),
                HerdStick.create(HerdTool.familiarity(-1)),
                HerdStick.create(HerdTool.pregnancy(1)),
                HerdStick.create(HerdTool.pregnancy(-1)),
                new ItemStack(BarnyardItems.BREEDERS_MASH),
                new ItemStack(BarnyardItems.GOLDEN_BRAN)
        );
    }
}
