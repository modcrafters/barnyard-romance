package net.modcrafters.minecraft.barnyardromance.naturalist;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

// Registers the mixins that target Naturalist's classes only when Naturalist is there. Mixin warns about a missing target
// even when it's marked @Pseudo, and a player without Naturalist shouldn't see that on every start. This runs too early
// to ask the loader which mods are installed, so it looks for the class file instead.
public final class NaturalistMixinPlugin implements IMixinConfigPlugin {
    private static final String DUCK = "com/crispytwig/naturalist/world/entity/animal/duck/Duck.class";

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
        return NaturalistMixinPlugin.class.getClassLoader().getResource(DUCK) != null
                ? List.of("NaturalistDuckEggTimerMixin", "NaturalistOstrichHatchMixin")
                : List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
