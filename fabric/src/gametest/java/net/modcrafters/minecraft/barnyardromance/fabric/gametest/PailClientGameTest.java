package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.client.PailScreen;
import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;
import net.modcrafters.minecraft.barnyardromance.milk.MilkItems;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import net.modcrafters.minecraft.barnyardromance.pail.PailMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// The pail window in a real client: a right click at the sky opens it, a shift-clicked stack of buckets puts one in,
// and a click on the Pour out button empties the pail.
public class PailClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("gamerule advance_time false");
            // Looking straight up, so the use goes to no block.
            world.getServer().runCommand("tp @a 0.5 ~ 0.5 0 -90");
            world.getServer().runOnServer(server -> {
                ServerPlayer player = Jdk.first(server.getPlayerList().getPlayers());
                ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
                PailItem.setContents(pail, new PailContents(MilkFluids.GOAT_ID, 2500));
                player.getInventory().setItem(0, pail);
                player.getInventory().setItem(1, new ItemStack(Items.BUCKET, 16));
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(5);

            context.getInput().pressKey(options -> options.keyUse);
            context.waitFor(client -> client.gui.screen() instanceof PailScreen);
            context.waitTicks(5);
            context.takeScreenshot("pail_window");

            // The second hotbar slot, shift-clicked. The test's mouse clicks carry no modifier keys, so this goes in
            // where the window's shift-click does, on the client, which plays it out and sends it to the server.
            context.runOnClient(client -> client.gameMode.handleContainerInput(client.player.containerMenu.containerId,
                    client.player.containerMenu.slots.size() - 9 + 1, 0, ContainerInput.QUICK_MOVE, client.player));
            context.waitTicks(2);
            world.getConnection().waitForClientboundPackets();
            expect(world, 1500, 15);
            context.takeScreenshot("pail_window_bucket_out");

            click(context, PailMenu.BUTTON_X + PailMenu.BUTTON_WIDTH / 2, PailMenu.BUTTON_Y + PailMenu.BUTTON_HEIGHT / 2);
            world.getConnection().waitForClientboundPackets();
            expect(world, 0, 15);
            context.takeScreenshot("pail_window_poured_out");
            context.setScreen(() -> null);
        }
    }

    // A left click at a point in the window, in its own coordinates.
    private static void click(ClientGameTestContext context, int windowX, int windowY) {
        double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
        int left = context.computeOnClient(client -> (client.getWindow().getGuiScaledWidth() - PailMenu.WIDTH) / 2);
        int top = context.computeOnClient(client -> (client.getWindow().getGuiScaledHeight() - PailMenu.HEIGHT) / 2);
        context.getInput().setCursorPos((left + windowX) * scale, (top + windowY) * scale);
        context.waitTicks(1);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(2);
    }

    // What the server has: the pail's amount, the empty buckets left in the hotbar, and the goat milk bucket in the slot.
    private static void expect(TestSingleplayerContext world, int amount, int buckets) {
        String wrong = world.getServer().computeOnServer(server -> {
            ServerPlayer player = Jdk.first(server.getPlayerList().getPlayers());
            PailContents contents = PailItem.contents(player.getMainHandItem());
            ItemStack hotbar = player.getInventory().getItem(1);
            boolean slotRight = player.containerMenu.getSlot(PailMenu.BUCKET_SLOT).getItem().is(MilkItems.GOAT_MILK_BUCKET);
            return contents.amount() == amount && hotbar.is(Items.BUCKET) && hotbar.getCount() == buckets && slotRight ? null
                    : "the pail holds " + contents + ", the hotbar " + hotbar + ", the slot " + player.containerMenu.getSlot(PailMenu.BUCKET_SLOT).getItem();
        });
        if (wrong != null) {
            throw new AssertionError(wrong);
        }
    }
}
