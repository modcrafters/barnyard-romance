package net.modcrafters.minecraft.barnyardromance.compat.friendsandfoes;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Friends&Foes' moobloom is matched by id, so this does nothing without the mod. It is cattle (SpeciesPresets): it
// gives milk and, with a bowl, suspicious stew by the barnyard's rules, and shearing it into a cow keeps its record
// (MoobloomConversionMixin). The mod's crab and penguin stay out.
public final class FriendsAndFoesFeature implements Feature {
    public static final String MOOBLOOM = "friendsandfoes:moobloom";

    @Override
    public String name() {
        return "friends_and_foes";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.trackedByDefault(MOOBLOOM);
    }
}
