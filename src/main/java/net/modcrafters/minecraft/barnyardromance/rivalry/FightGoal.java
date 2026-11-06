package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

// A male in a fight walks up to his rival and butts heads with him every FightRules.CLASH_TICKS until one drops. It
// runs first of all his goals, so nothing pulls him away, and the rival damage has no attacker, so nothing panics or
// calls for help. Goats and other brain-driven animals would still panic and wander, so their brain is kept quiet.
public class FightGoal extends Goal {
    private static final int FIRST_CLASH_TICKS = 20;
    private static final double SPEED = 1.2;
    private static final double PUSH = 0.4;
    // How far past touching a clash reaches, and how close the rival is when the walk is steered by hand.
    private static final double REACH = 1.0;
    private static final double STEER_DISTANCE = 3.0;

    private final Animal mob;
    private int cooldown;

    public FightGoal(Animal mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return Fights.opponent(this.mob).isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.cooldown = FIRST_CLASH_TICKS;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Optional<Animal> found = Fights.opponent(this.mob);
        if (found.isEmpty() || !(this.mob.level() instanceof ServerLevel level)) {
            return;
        }
        Animal rival = found.get();
        quietBrain();
        this.mob.getLookControl().setLookAt(rival, 30.0F, 30.0F);
        double reach = this.mob.getBbWidth() / 2.0 + rival.getBbWidth() / 2.0 + REACH;
        double distance = this.mob.distanceTo(rival);
        if (distance > STEER_DISTANCE) {
            this.mob.getNavigation().moveTo(rival, SPEED);
        } else if (distance > reach) {
            // Navigation counts the block next to him as arrived and stops, so the last steps are steered by hand.
            this.mob.getNavigation().stop();
            this.mob.getMoveControl().setWantedPosition(rival.getX(), rival.getY(), rival.getZ(), SPEED);
        } else {
            this.mob.getNavigation().stop();
        }
        if (--this.cooldown > 0 || distance > reach + 0.5) {
            return;
        }
        this.cooldown = FightRules.CLASH_TICKS;
        Optional<DamageSource> damage = Fights.damage(level);
        if (damage.isEmpty()) {
            return;
        }
        double scale = Services.DATA.get(this.mob).map(AnimalTraits::scale).orElse(1.0);
        //? if >=1.21.2 {
        rival.hurtServer(level, damage.get(), FightRules.clashDamage(scale));
        //?} else
        //rival.hurt(damage.get(), FightRules.clashDamage(scale));
        Vec3 away = rival.position().subtract(this.mob.position()).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() > 1.0e-6) {
            away = away.normalize().scale(PUSH);
            rival.push(away.x, 0.1, away.z);
        }
        this.mob.playSound(SoundEvents.GOAT_RAM_IMPACT, 0.8F, 0.8F + this.mob.getRandom().nextFloat() * 0.4F);
    }

    // Hurt goats panic and wander off through their brain; a fighter stays put.
    private void quietBrain() {
        Brain<?> brain = this.mob.getBrain();
        brain.eraseMemory(MemoryModuleType.HURT_BY);
        brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        brain.eraseMemory(MemoryModuleType.IS_PANICKING);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }
}
