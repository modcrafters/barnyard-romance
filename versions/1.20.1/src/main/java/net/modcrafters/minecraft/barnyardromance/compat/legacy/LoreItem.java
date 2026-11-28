package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

// An item with default lore, for Minecraft 1.20.1, which has no lore component (1.20.5): its properties
// (LoreProperties) carry the lines and it adds them to its tooltip, in the lore's purple italics.
public class LoreItem extends Item {
    private final List<Component> lore;

    public LoreItem(Item.Properties properties) {
        super(properties);
        this.lore = properties instanceof LoreProperties withLore ? withLore.lore() : List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        for (Component line : lore) {
            lines.add(line.copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
