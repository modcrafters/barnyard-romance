package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.feed.FeederMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

// The 1.21.1 copy of src/.../client/FeederScreen.
public class FeederScreen extends AbstractContainerScreen<FeederMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    // The chest texture's title bar and one row; a plain stretch of the title bar; where its inventory part starts.
    private static final int TOP = 17 + 18;
    private static final int PLAIN_V = 4;
    private static final int INVENTORY_V = 126;
    private static final int TEXT = 0xFF404040;

    public FeederScreen(FeederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = FeederMenu.WIDTH;
        this.imageHeight = FeederMenu.HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, TOP);
        graphics.blit(BACKGROUND, this.leftPos, this.topPos + TOP, 0, PLAIN_V, this.imageWidth, FeederMenu.EXTRA);
        graphics.blit(BACKGROUND, this.leftPos, this.topPos + TOP + FeederMenu.EXTRA, 0, INVENTORY_V, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font, line(), this.titleLabelX, FeederMenu.LINE_Y, TEXT, false);
    }

    private Component line() {
        if (this.menu.food() == 0) {
            return Component.translatable("gui.barnyard_romance.feeder.empty");
        }
        return Component.translatable("gui.barnyard_romance.feeder.days", this.menu.days(), this.menu.animals());
    }
}
