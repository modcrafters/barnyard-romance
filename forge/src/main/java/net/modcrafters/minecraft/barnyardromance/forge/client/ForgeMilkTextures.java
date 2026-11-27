package net.modcrafters.minecraft.barnyardromance.forge.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;

// The textures other mods' tanks draw for one of the mod's milks: the shared milk texture in the milk's colour. Forge
// 1.20.1 asks each fluid type for these itself (FluidType.initializeClient), only on the client.
public final class ForgeMilkTextures {
    private ForgeMilkTextures() {
    }

    public static IClientFluidTypeExtensions tinted(int tint) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return MilkFluids.STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return MilkFluids.FLOW_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return tint;
            }
        };
    }
}
