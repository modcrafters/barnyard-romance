package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

// The 1.21.1 copy of src/.../client/PailScreen.
public class PailScreen extends AbstractContainerScreen<PailMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    // The chest texture's title bar; the plain rows inside it that stretch it; its first slot frame; where its
    // inventory part starts.
    private static final int TITLE_BAR = 17;
    private static final int PLAIN_V = 4;
    private static final int PLAIN_ROWS = 13;
    private static final int SLOT_U = 7;
    private static final int SLOT_V = 17;
    private static final int INVENTORY_V = 126;
    private static final int TEXT = 0xFF404040;

    private Button pourOut;

    public PailScreen(PailMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PailMenu.WIDTH;
        this.imageHeight = PailMenu.HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.pourOut = this.addRenderableWidget(Button.builder(Component.translatable("gui.barnyard_romance.pail.pour_out"), button -> pourOut())
                .bounds(this.leftPos + PailMenu.BUTTON_X, this.topPos + PailMenu.BUTTON_Y, PailMenu.BUTTON_WIDTH, PailMenu.BUTTON_HEIGHT)
                .build());
        this.pourOut.active = !PailItem.contents(this.menu.pail()).isEmpty();
    }

    // Nothing to pour from an empty pail.
    @Override
    protected void containerTick() {
        super.containerTick();
        this.pourOut.active = !PailItem.contents(this.menu.pail()).isEmpty();
    }

    // The client empties its copy straight away, as the server will.
    private void pourOut() {
        if (this.menu.clickMenuButton(this.minecraft.player, PailMenu.POUR_OUT)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, PailMenu.POUR_OUT);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, TITLE_BAR);
        for (int y = TITLE_BAR; y < PailMenu.TOP; y += PLAIN_ROWS) {
            int rows = Math.min(PLAIN_ROWS, PailMenu.TOP - y);
            graphics.blit(BACKGROUND, this.leftPos, this.topPos + y, 0, PLAIN_V, this.imageWidth, rows);
        }
        graphics.blit(BACKGROUND, this.leftPos + PailMenu.SLOT_X - 1, this.topPos + PailMenu.SLOT_Y - 1, SLOT_U, SLOT_V, 18, 18);
        graphics.blit(BACKGROUND, this.leftPos, this.topPos + PailMenu.TOP, 0, INVENTORY_V, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ItemStack pail = this.menu.pail();
        if (pail.getItem() instanceof PailItem item) {
            graphics.drawString(this.font, item.contentsLine(pail), this.titleLabelX, PailMenu.LINE_Y, TEXT, false);
        }
    }
}
