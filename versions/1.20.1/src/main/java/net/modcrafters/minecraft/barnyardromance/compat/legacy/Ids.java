package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import net.minecraft.resources.ResourceLocation;

// The id factories 1.21 added, for Minecraft 1.20.1, which builds ids with constructors that check them the same
// way. Stonecutter writes these in place of the factories on 1.20.1.
public final class Ids {
    private Ids() {
    }

    public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation withDefaultNamespace(String path) {
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
    }

    public static ResourceLocation parse(String id) {
        return new ResourceLocation(id);
    }
}
