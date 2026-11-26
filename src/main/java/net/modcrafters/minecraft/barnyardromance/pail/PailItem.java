package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
//import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
//? if >=1.21.5 {
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
//?} else
//import java.util.List;

// A pail: stacks to one, holds one fluid up to its capacity in millilitres, and shows how full it is as an item bar.
// Used where no animal or block takes it, it opens its window (PailMenu).
public class PailItem extends Item {
    private static final int BAR_WIDTH = 13;

    private final int capacity;

    public PailItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    // EMPTY when the pail has no component, so a new pail needs no setup.
    public static PailContents contents(ItemStack stack) {
        PailContents contents = stack.get(PailComponents.CONTENTS);
        return contents == null ? PailContents.EMPTY : contents;
    }

    // An empty pail carries no component, so it stacks and compares like a new one.
    public static void setContents(ItemStack stack, PailContents contents) {
        if (contents.isEmpty()) {
            stack.remove(PailComponents.CONTENTS);
        } else {
            stack.set(PailComponents.CONTENTS, contents);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !contents(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // At least one pixel, so a nearly empty pail still shows its bar.
        return Math.clamp(Math.round((float) contents(stack).amount() * BAR_WIDTH / capacity), 1, BAR_WIDTH);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Identifier fluid = contents(stack).fluid();
        // White for an empty pail, though its bar is hidden then.
        return fluid == null ? 0xFFFFFF : PailFluids.colour(fluid);
    }

    // Milking runs before this, so an animal that takes the pail never gets here; a baby, a pig or the sky does.
    //? if >=1.21.2 {
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        openWindow(level, player, hand);
        return InteractionResult.SUCCESS;
    }
    //?} else {
    /*@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        openWindow(level, player, hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
    *///?}

    private static void openWindow(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new PailMenu(id, inventory, hand), player.getItemInHand(hand).getHoverName()));
        }
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(contentsLine(stack).copy().withStyle(ChatFormatting.GRAY));
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(contentsLine(stack).copy().withStyle(ChatFormatting.GRAY));
    }
    *///?}

    // "Goat Milk 1500 / 5000 ml" or "Empty (5000 ml)", for the tooltip and the pail's window.
    public Component contentsLine(ItemStack stack) {
        PailContents contents = contents(stack);
        return contents.isEmpty()
                ? Component.translatable("item.barnyard_romance.pail.empty", capacity)
                : Component.translatable("item.barnyard_romance.pail.contents", PailFluids.name(contents.fluid()), contents.amount(), capacity);
    }
}
