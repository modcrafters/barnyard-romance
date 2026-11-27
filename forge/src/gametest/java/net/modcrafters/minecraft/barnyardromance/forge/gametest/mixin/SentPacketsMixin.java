package net.modcrafters.minecraft.barnyardromance.forge.gametest.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.modcrafters.minecraft.barnyardromance.forge.gametest.SentPackets;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Every packet on its way to a player passes here, so a test can see what a mock player was sent (see SentPackets).
@Mixin(Connection.class)
public class SentPacketsMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"))
    private void barnyardromance$capture(Packet<?> packet, @Nullable PacketSendListener listener, CallbackInfo callback) {
        SentPackets.sent((Connection) (Object) this, packet);
    }
}
