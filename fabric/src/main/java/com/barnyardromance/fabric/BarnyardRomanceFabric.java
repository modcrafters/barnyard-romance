package com.barnyardromance.fabric;

import com.barnyardromance.BarnyardRomance;
import net.fabricmc.api.ModInitializer;

public class BarnyardRomanceFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BarnyardRomance.init();
    }
}
