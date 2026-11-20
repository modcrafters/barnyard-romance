package net.modcrafters.minecraft.barnyardromance.core.compat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

// Registers the mixins that target another mod's classes only when that mod is there. Mixin warns about a missing
// target even when it's marked @Pseudo, and a player without the mod shouldn't see that on every start. This runs too
// early to ask the loader which mods are installed, so each mixin names one class file of its mod to look for instead.
public final class CompatMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        ClassLoader loader = CompatMixinPlugin.class.getClassLoader();
        return CompatMixins.mixinsFor(probe -> loader.getResource(probe) != null);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
