package net.modcrafters.minecraft.barnyardromance.naturalist;

import net.modcrafters.minecraft.barnyardromance.naturalist.api.NaturalistApi;

final class NaturalistApiImpl implements NaturalistApi {
    @Override
    public boolean isLivestock(String speciesId) {
        return NaturalistCompat.LIVESTOCK.contains(speciesId);
    }
}
