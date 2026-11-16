package net.modcrafters.minecraft.barnyardromance.mixin.client;

import net.modcrafters.minecraft.barnyardromance.look.AnimalLookState;
import net.modcrafters.minecraft.barnyardromance.look.HornDraw;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// 26.3 only: carries the belly, the horns and the model that wears them from the renderer to the later model pass.
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements AnimalLookState {
    @Unique
    private float barnyardromance$belly = 1.0f;
    @Unique
    private HornDraw barnyardromance$horns;
    @Unique
    private Object barnyardromance$hornModel;

    @Override
    public float barnyardromance$belly() {
        return barnyardromance$belly;
    }

    @Override
    public void barnyardromance$setBelly(float belly) {
        barnyardromance$belly = belly;
    }

    @Override
    public HornDraw barnyardromance$horns() {
        return barnyardromance$horns;
    }

    @Override
    public void barnyardromance$setHorns(HornDraw horns) {
        barnyardromance$horns = horns;
    }

    @Override
    public Object barnyardromance$hornModel() {
        return barnyardromance$hornModel;
    }

    @Override
    public void barnyardromance$setHornModel(Object model) {
        barnyardromance$hornModel = model;
    }
}
