package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// The pail window: one slot that trades buckets with the pail in the player's hand, a Pour out button, then the
// player's inventory and hotbar. The pail isn't copied into the window: the menu reads and writes the stack in the hand,
// so the window closes as soon as that stack leaves the hand.
public class PailMenu extends AbstractContainerMenu {
    public static final int BUCKET_SLOT = 0;
    // The Pour out button's id for clickMenuButton.
    public static final int POUR_OUT = 0;
    // What a bucket holds.
    public static final int BUCKET = 1000;

    public static final int WIDTH = 176;
    public static final int LINE_Y = 20;
    public static final int SLOT_X = 8;
    public static final int SLOT_Y = 35;
    public static final int BUTTON_WIDTH = 70;
    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_X = WIDTH - 8 - BUTTON_WIDTH;
    public static final int BUTTON_Y = SLOT_Y - 2;
    // The part above the inventory: the title, the contents line and the slot's row. The rest is laid out as a
    // chest's is under its rows.
    public static final int TOP = SLOT_Y + 22;
    public static final int HEIGHT = TOP + 97;
    public static final int INVENTORY_Y = TOP + 14;

    // The hand the pail is in, 0 for the main hand and 1 for the offhand, sent to the client's copy.
    private static final int MAIN_HAND = 0;
    private static final int OFF_HAND = 1;

    private final Player player;
    private final Container bucket = new SimpleContainer(1);
    private final ContainerData hand;
    // The very stack the window opened on, on the server. The client's copy has none and reads the hand, which it
    // changes as the server does by playing out the same clicks.
    private final @Nullable ItemStack opened;

    // The client's copy, filled in by the server.
    public PailMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainerData(1), null);
    }

    public PailMenu(int containerId, Inventory inventory, InteractionHand hand) {
        this(containerId, inventory, handData(hand), inventory.player.getItemInHand(hand));
    }

    private PailMenu(int containerId, Inventory inventory, ContainerData hand, @Nullable ItemStack opened) {
        super(PailItems.PAIL_MENU, containerId);
        checkContainerDataCount(hand, 1);
        this.player = inventory.player;
        this.hand = hand;
        this.opened = opened;
        this.addSlot(new PailSlot(this, bucket, 0, SLOT_X, SLOT_Y));
        this.addDataSlots(hand);
        //? if >=1.21.2 {
        this.addStandardInventorySlots(inventory, 8, INVENTORY_Y);
        //?} else {
        /*for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }
        *///?}
    }

    private static ContainerData handData(InteractionHand hand) {
        ContainerData data = new SimpleContainerData(1);
        data.set(0, hand == InteractionHand.OFF_HAND ? OFF_HAND : MAIN_HAND);
        return data;
    }

    public InteractionHand hand() {
        return hand.get(0) == OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    // The pail the window works on, or EMPTY once the one it opened on has left the hand.
    public ItemStack pail() {
        ItemStack held = player.getItemInHand(hand());
        return opened == null || held == opened ? held : ItemStack.EMPTY;
    }

    // Picking the pail up with the cursor leaves the same stack object in the hand with nothing in it, so an empty
    // stack counts as gone too.
    @Override
    public boolean stillValid(Player player) {
        return opened == null || (player.getItemInHand(hand()) == opened && !opened.isEmpty());
    }

    // Whether this bucket would trade with the pail: an empty one when the pail has a full bucket of a fluid that has a
    // bucket, or a full one of an accepted fluid when the pail has room for all of it.
    boolean trades(ItemStack stack) {
        ItemStack pail = pail();
        if (stack.is(Items.BUCKET)) {
            return filled(pail).isPresent();
        }
        Optional<Fluid> fluid = PailFluids.forBucket(stack.getItem(), PailItem.contents(pail).fluid());
        return fluid.isPresent() && PailTransfer.insert(pail, fluid.get(), BUCKET, true) == BUCKET;
    }

    // Trades a bucket put in the slot with the pail: an empty bucket comes back full, a full one comes back empty.
    // Anything that doesn't trade comes back as it was. The pail changes in place, so it stays the stack in the hand.
    ItemStack trade(ItemStack stack) {
        if (stack.getCount() != 1 || !trades(stack)) {
            return stack;
        }
        ItemStack pail = pail();
        if (stack.is(Items.BUCKET)) {
            Item full = filled(pail).orElseThrow();
            PailTransfer.extract(pail, null, BUCKET, false);
            return new ItemStack(full);
        }
        PailTransfer.insert(pail, PailFluids.forBucket(stack.getItem(), PailItem.contents(pail).fluid()).orElseThrow(), BUCKET, false);
        return new ItemStack(Items.BUCKET);
    }

    // The full bucket an empty one becomes, if the pail has a whole bucket of a fluid that has one.
    private static Optional<Item> filled(ItemStack pail) {
        return PailTransfer.fluid(pail)
                .filter(fluid -> PailTransfer.extract(pail, fluid, BUCKET, true) == BUCKET)
                .flatMap(PailFluids::bucketFor);
    }

    // Pour out: the pail is emptied on the spot, whatever it holds, even a fluid nothing accepts any more.
    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack pail = pail();
        if (id != POUR_OUT || PailItem.contents(pail).isEmpty()) {
            return false;
        }
        PailItem.setContents(pail, PailContents.EMPTY);
        if (!player.level().isClientSide()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        if (index == BUCKET_SLOT) {
            if (!this.moveItemStackTo(stack, BUCKET_SLOT + 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, BUCKET_SLOT, BUCKET_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return moved;
    }

    // The bucket in the slot goes back to the player, or on the ground. The server does this; the client's copy
    // hears about it from the server.
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            this.clearContainer(player, bucket);
        }
    }
}
