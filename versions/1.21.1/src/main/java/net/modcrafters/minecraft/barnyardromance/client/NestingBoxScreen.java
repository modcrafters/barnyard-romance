package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxLayout;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// The 1.21.1 copy of src/.../client/NestingBoxScreen. Labels are drawn after the slots here, so the front rims
// and countdowns go with them.
public class NestingBoxScreen extends AbstractContainerScreen<NestingBoxMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/nesting_box.png");
    private static final ResourceLocation NEST_FRONT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "nesting_box/nest_front");
    private static final int TIMER = 0xFF404040;
    private static final int COUNT = 0xFFFFFFFF;

    public NestingBoxScreen(NestingBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = NestingBoxLayout.WIDTH;
        this.imageHeight = NestingBoxLayout.HEIGHT;
        this.inventoryLabelY = NestingBoxLayout.INVENTORY_LABEL_Y;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        long now = this.minecraft.level == null ? 0L : this.minecraft.level.getGameTime();
        for (int index = 0; index < NestingBoxBlockEntity.SLOTS; index++) {
            Slot slot = this.menu.getSlot(index);
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.blitSprite(NEST_FRONT, slot.x, slot.y, 16, 16);
            ItemStack stack = slot.getItem();
            if (stack.getCount() > 1) {
                String count = Integer.toString(stack.getCount());
                graphics.drawString(this.font, count, slot.x + 17 - this.font.width(count), slot.y + 9, COUNT, true);
            }
            graphics.pose().popPose();
            FertilizedEggItem.data(stack).ifPresent(data -> {
                String timer = NestingBoxLayout.timer(data.remaining(now));
                graphics.drawString(this.font, timer, slot.x + 8 - this.font.width(timer) / 2, slot.y + 19, TIMER, false);
            });
        }
        super.renderLabels(graphics, mouseX, mouseY);
    }
}
