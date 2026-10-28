package net.modcrafters.minecraft.barnyardromance.item;

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
import net.minecraft.world.level.Level;

// Use it on a familiar animal to add it to this book; use it in the air to open it.
public class HerdBookItem extends Item {
    public HerdBookItem(Properties properties) {
        super(properties);
    }

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
        HerdBooks.RegisterResult result = HerdBooks.register(player.getItemInHand(hand), animal);
        VersionCompat.sendOverlay(player, Component.translatable(switch (result) {
            case ADDED -> "message.barnyard_romance.herd_book.added";
            case ALREADY -> "message.barnyard_romance.herd_book.already";
            case NOT_FAMILIAR -> "message.barnyard_romance.herd_book.not_familiar";
            case NOT_TRACKED -> "message.barnyard_romance.herd_book.not_tracked";
        }, animal.getName()));
        return VersionCompat.SUCCESS_SERVER;
    }
}
