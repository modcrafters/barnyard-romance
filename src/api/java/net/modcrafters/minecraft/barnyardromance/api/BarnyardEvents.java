package net.modcrafters.minecraft.barnyardromance.api;

import net.modcrafters.minecraft.barnyardromance.api.event.Befriended;
import net.modcrafters.minecraft.barnyardromance.api.event.Born;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;
import net.modcrafters.minecraft.barnyardromance.api.event.Hatched;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import net.modcrafters.minecraft.barnyardromance.api.event.Taming;

/**
 * Every event Barnyard Romance fires. They fire on the server only, and only for animals it tracks.
 */
public final class BarnyardEvents {
    public static final BarnyardEvent<Taming> TAMING = new BarnyardEvent<>("Taming");
    public static final BarnyardEvent<Milking> MILKING = new BarnyardEvent<>("Milking");
    public static final BarnyardEvent<DeathDrop> DEATH_DROP = new BarnyardEvent<>("DeathDrop");
    public static final BarnyardEvent<ShearYield> SHEAR_YIELD = new BarnyardEvent<>("ShearYield");
    public static final BarnyardEvent<FamiliarityChange> FAMILIARITY_CHANGE = new BarnyardEvent<>("FamiliarityChange");
    public static final BarnyardEvent<Befriended> BEFRIENDED = new BarnyardEvent<>("Befriended");
    public static final BarnyardEvent<Born> BORN = new BarnyardEvent<>("Born");
    public static final BarnyardEvent<Hatched> HATCHED = new BarnyardEvent<>("Hatched");

    private BarnyardEvents() {
    }
}
