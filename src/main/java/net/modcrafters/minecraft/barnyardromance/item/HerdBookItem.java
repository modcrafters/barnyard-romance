package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
//import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
//? if >=1.21.5 {
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
//?} else
//import java.util.List;
import java.util.Locale;
import java.util.UUID;

// Use it on a familiar animal to add it to this book; use it in the air to open it. A book with a herd shows a short
// mark of its ID, which its copies share (HerdBookCopyRecipe).
public class HerdBookItem extends Item {
    public HerdBookItem(Properties properties) {
        super(properties);
    }

    public static String mark(UUID id) {
        return id.toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private static Component herdLine(UUID id) {
        return Component.translatable("item.barnyard_romance.herd_book.herd", mark(id)).withStyle(ChatFormatting.GRAY);
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        UUID id = stack.get(BarnyardComponents.HERD_BOOK);
        if (id != null) {
            builder.accept(herdLine(id));
        }
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        UUID id = stack.get(BarnyardComponents.HERD_BOOK);
        if (id != null) {
            lines.add(herdLine(id));
        }
    }
    *///?}

    //? if >=1.21.2 {
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            HerdBooks.show(serverPlayer, player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS;
    }
    //?} else {
    /*@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            HerdBooks.show(serverPlayer, player.getItemInHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
    *///?}

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Animal animal)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // The stack passed in can be a copy in creative mode, so the ID goes on the one in the hand.
        ItemStack held = player.getItemInHand(hand);
        HerdBooks.Use use = HerdBooks.use(held, animal);
        switch (use.result()) {
            case ADDED -> {
                BarnyardAdvancements.registered(animal, player);
                VersionCompat.sendOverlay(player, Component.translatable("message.barnyard_romance.herd_book.added", animal.getName()));
                if (player instanceof ServerPlayer serverPlayer) {
                    HerdBooks.show(serverPlayer, held, true, use.focus());
                }
            }
            case ALREADY -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    HerdBooks.show(serverPlayer, held, true, use.focus());
                }
            }
            case NOT_FAMILIAR -> VersionCompat.sendOverlay(player, Component.translatable("message.barnyard_romance.herd_book.not_familiar", animal.getName()));
            case NOT_TRACKED -> VersionCompat.sendOverlay(player, Component.translatable("message.barnyard_romance.herd_book.not_tracked", animal.getName()));
        }
        return VersionCompat.SUCCESS_SERVER;
    }
}
