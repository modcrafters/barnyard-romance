package com.barnyardromance.neoforge;

import com.barnyardromance.BarnyardRomance;
import com.barnyardromance.Constants;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class BarnyardRomanceNeoForge {
    public BarnyardRomanceNeoForge() {
        BarnyardRomance.init();
    }
}
