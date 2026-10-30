package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxLayout;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// The nesting box window. Each nest is a straw bowl: the back of the bowl is in the background, the egg is drawn
// over it, and the front rim goes over the egg, so the egg sits in the straw. A fertilized egg's countdown is
// written under its nest. versions/1.21.1 has the same screen for 1.21.1's drawing calls.
public class NestingBoxScreen extends AbstractContainerScreen<NestingBoxMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/nesting_box.png");
    private static final Identifier NEST_FRONT = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "nesting_box/nest_front");
    private static final int TIMER = 0xFF404040;
    private static final int COUNT = 0xFFFFFFFF;

    public NestingBoxScreen(NestingBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, NestingBoxLayout.WIDTH, NestingBoxLayout.HEIGHT);
        this.inventoryLabelY = NestingBoxLayout.INVENTORY_LABEL_Y;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractSlots(graphics, mouseX, mouseY);
        long now = this.minecraft.level == null ? 0L : this.minecraft.level.getGameTime();
        for (int index = 0; index < NestingBoxBlockEntity.SLOTS; index++) {
            Slot slot = this.menu.getSlot(index);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NEST_FRONT, slot.x, slot.y, 16, 16);
            ItemStack stack = slot.getItem();
            if (stack.getCount() > 1) {
                String count = Integer.toString(stack.getCount());
                graphics.text(this.font, count, slot.x + 17 - this.font.width(count), slot.y + 9, COUNT, true);
            }
            FertilizedEggItem.data(stack).ifPresent(data -> {
                String timer = NestingBoxLayout.timer(data.remaining(now));
                graphics.text(this.font, timer, slot.x + 8 - this.font.width(timer) / 2, slot.y + 19, TIMER, false);
            });
        }
    }
}
