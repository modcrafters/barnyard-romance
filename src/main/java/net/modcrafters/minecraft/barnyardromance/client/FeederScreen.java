package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.feed.FeederMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// A feeding block's window: vanilla's one-row chest, stretched by a strip of its title bar under the row to make
// room for how long the food lasts. versions/1.21.1 has the same screen for 1.21.1's drawing calls.
public class FeederScreen extends AbstractContainerScreen<FeederMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    // The chest texture's title bar and one row; a plain stretch of the title bar; where its inventory part starts.
    private static final int TOP = 17 + 18;
    private static final int PLAIN_V = 4;
    private static final int INVENTORY_V = 126;
    private static final int TEXT = 0xFF404040;

    public FeederScreen(FeederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, FeederMenu.WIDTH, FeederMenu.HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, TOP, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos + TOP, 0.0F, PLAIN_V, this.imageWidth, FeederMenu.EXTRA, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos + TOP + FeederMenu.EXTRA, 0.0F, INVENTORY_V, this.imageWidth, 96, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, line(), this.titleLabelX, FeederMenu.LINE_Y, TEXT, false);
    }

    private Component line() {
        if (this.menu.food() == 0) {
            return Component.translatable("gui.barnyard_romance.feeder.empty");
        }
        return Component.translatable("gui.barnyard_romance.feeder.days", this.menu.days(), this.menu.animals());
    }
}
