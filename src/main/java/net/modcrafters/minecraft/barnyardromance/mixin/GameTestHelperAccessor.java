package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// The test a helper runs, so a mock player can leave when it's over (see gametest.MockPlayers).
@Mixin(GameTestHelper.class)
public interface GameTestHelperAccessor {
    @Accessor("testInfo")
    GameTestInfo barnyardromance$testInfo();
}
