package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.client.InspectorScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class BarnyardInspector extends Item {
    public BarnyardInspector(Properties properties) {
        super(properties);
    }

    public static ItemStack create() {
        ItemStack stack = new ItemStack(BarnyardItems.INSPECTOR);
        stack.set(net.minecraft.core.component.DataComponents.ITEM_NAME, Component.literal("Barnyard Inspector"));
        stack.set(net.minecraft.core.component.DataComponents.LORE, new ItemLore(List.of(Component.literal("Use on an animal"))));
        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Animal animal)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            InspectorScreen.open(animal);
            return InteractionResult.SUCCESS;
        }
        return VersionCompat.SUCCESS_SERVER;
    }
}
