package net.modcrafters.minecraft.barnyardromance.neoforge.gametest.mixin;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A game test's mock player joins over a bare connection that never negotiated NeoForge's payload channels. A mod that
// sends a payload when a player joins (Friends&Foes does, on its datapack sync) then throws and fails every test that
// makes a mock player. This gives that connection every registered channel, as NeoForge does for its own mock
// connections.
@Mixin(PlayerList.class)
public class MockConnectionMixin {
    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void barnyardromance$configureMockConnection(Connection connection, ServerPlayer player,
                                                         CommonListenerCookie cookie, CallbackInfo callback) {
        if (connection.channel() instanceof EmbeddedChannel) {
            NetworkRegistry.configureMockConnection(connection);
        }
    }
}
