package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
//? if >=1.21.5 {
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
//?} else
//import java.util.List;

// A pail: stacks to one, holds one fluid up to its capacity in millilitres, and shows how full it is as an item bar.
public class PailItem extends Item {
    private static final int BAR_WIDTH = 13;
    // White until fluids have colours of their own.
    private static final int BAR_COLOUR = 0xFFFFFF;

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
        return BAR_COLOUR;
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(line(stack));
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(line(stack));
    }
    *///?}

    private Component line(ItemStack stack) {
        PailContents contents = contents(stack);
        Component line = contents.isEmpty()
                ? Component.translatable("item.barnyard_romance.pail.empty", capacity)
                : Component.translatable("item.barnyard_romance.pail.contents", fluidName(contents.fluid()), contents.amount(), capacity);
        return line.copy().withStyle(ChatFormatting.GRAY);
    }

    // The fluid's bucket name until fluids have names of their own. A fluid that isn't registered any more shows its id.
    private static Component fluidName(Identifier id) {
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        Item bucket = fluid.getBucket();
        return fluid == Fluids.EMPTY || bucket == Items.AIR
                ? Component.literal(id.toString())
                : bucket.getDefaultInstance().getHoverName();
    }
}
