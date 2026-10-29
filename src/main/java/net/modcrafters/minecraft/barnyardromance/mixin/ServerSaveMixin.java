package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Herd book records of loaded animals are refreshed once a minute; a save refreshes them first, so a save
// (autosave, /save-all, or stopping) never holds a record that is behind the animal.
@Mixin(MinecraftServer.class)
public class ServerSaveMixin {
    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    private void barnyardromance$refreshHerdBooks(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> callback) {
        try {
            HerdBooks.refreshLoaded((MinecraftServer) (Object) this);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to refresh herd books before saving", exception);
        }
    }
}
