package net.modcrafters.minecraft.barnyardromance.forge.gametest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.20.1's game test starts the tick after its structure is placed, while the chunks the test area reaches into may
// still be on their way to ticking entities: an animal a test adds there is hidden, so the test can't find it and it
// never moves. From 1.20.5 a test waits until every chunk its structure touches ticks entities; this waits the same
// way here, moving the start (and so the timeout) on by a tick at a time.
@Mixin(GameTestInfo.class)
public abstract class GameTestChunksMixin {
    @Shadow
    @Final
    private ServerLevel level;
    @Shadow
    private long startTick;
    @Shadow
    private boolean started;

    @Shadow
    public abstract AABB getStructureBounds();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$waitForChunks(CallbackInfo callback) {
        if (this.started) {
            return;
        }
        AABB bounds = getStructureBounds();
        if (bounds == null) {
            return;
        }
        for (int x = (int) Math.floor(bounds.minX) >> 4; x <= (int) Math.floor(bounds.maxX) >> 4; x++) {
            for (int z = (int) Math.floor(bounds.minZ) >> 4; z <= (int) Math.floor(bounds.maxZ) >> 4; z++) {
                if (!this.level.isPositionEntityTicking(new BlockPos(x << 4, (int) bounds.minY, z << 4))) {
                    this.startTick++;
                    callback.cancel();
                    return;
                }
            }
        }
    }
}
