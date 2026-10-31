package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.milk.MilkRules;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
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
public class DairyMashItem extends Item {
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
    }
    *///?}

    private static Component effectLine() {
        return TextComponents.of(MilkRules.dairyMashEffect(BarnyardConfig.current().tuning().milkKeepsFlowingTicks())).copy()
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
    }
}
