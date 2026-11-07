package net.modcrafters.minecraft.barnyardromance.compat.jade;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.compat.JadeBridge;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

// Jade finds this itself: NeoForge by the annotation, Fabric by the "jade" entrypoint in fabric.mod.json. Nothing else
// refers to this package, so no Jade class loads when Jade isn't installed.
@WailaPlugin
public final class BarnyardJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new AnimalStatsProvider(), Animal.class);
        // Only now is Jade showing our stats, so only now does our own card stand down.
        JadeBridge.markActive();
    }
}
