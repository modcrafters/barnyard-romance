package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

// The pail window: what the pail holds, the bucket slot and the Pour out button above the player's inventory. It is
// cut from vanilla's chest: its title bar, stretched to make room, one slot frame from its rows, and its inventory
// part. versions/1.21.1 has the same screen for 1.21.1's drawing calls.
public class PailScreen extends AbstractContainerScreen<PailMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
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
        super(menu, inventory, title, PailMenu.WIDTH, PailMenu.HEIGHT);
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, TITLE_BAR, 256, 256);
        for (int y = TITLE_BAR; y < PailMenu.TOP; y += PLAIN_ROWS) {
            int rows = Math.min(PLAIN_ROWS, PailMenu.TOP - y);
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos + y, 0.0F, PLAIN_V, this.imageWidth, rows, 256, 256);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos + PailMenu.SLOT_X - 1, this.topPos + PailMenu.SLOT_Y - 1, SLOT_U, SLOT_V, 18, 18, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos + PailMenu.TOP, 0.0F, INVENTORY_V, this.imageWidth, 96, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        ItemStack pail = this.menu.pail();
        if (pail.getItem() instanceof PailItem item) {
            graphics.text(this.font, item.contentsLine(pail), this.titleLabelX, PailMenu.LINE_Y, TEXT, false);
        }
    }
}
