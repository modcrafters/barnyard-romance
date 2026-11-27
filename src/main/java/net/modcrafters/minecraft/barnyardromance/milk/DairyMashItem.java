package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;

//? if >=1.21.5
import java.util.function.Consumer;
//? if <1.21.5
//import java.util.List;

// Dairy Mash says how long the milk it starts lasts, from the config (milkKeepsFlowingTicks), in the lore's style.
//? if >=1.20.5 {
public class DairyMashItem extends Item {
//?} else
//public class DairyMashItem extends net.modcrafters.minecraft.barnyardromance.compat.legacy.LoreItem {
    public DairyMashItem(Properties properties) {
        super(properties);
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(effectLine());
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(effectLine());
        // The lore line after it, as the lore component comes after the item's own lines.
        super.appendHoverText(stack, context, lines, flag);
    }
    *///?}

    private static Component effectLine() {
        return TextComponents.of(MilkRules.dairyMashEffect(BarnyardConfig.current().tuning().milkKeepsFlowingTicks())).copy()
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
    }
}
