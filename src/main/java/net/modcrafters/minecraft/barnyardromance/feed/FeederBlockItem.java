package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;

//? if >=1.21.5
import java.util.function.Consumer;
//? if <1.21.5
//import java.util.List;

// A feeding block's item, whose tooltip says which animals the block feeds (FeederTooltip).
public class FeederBlockItem extends BlockItem {
    private final FeederKind kind;

    public FeederBlockItem(Block block, FeederKind kind, Properties properties) {
        super(block, properties);
        this.kind = kind;
    }

    public FeederKind kind() {
        return kind;
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        FeederTooltip.lines(kind).forEach(builder);
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.addAll(FeederTooltip.lines(kind));
        super.appendHoverText(stack, context, lines, flag);
    }
    *///?}
}
