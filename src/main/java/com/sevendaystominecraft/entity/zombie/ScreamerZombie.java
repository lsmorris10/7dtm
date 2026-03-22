package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.client.particle.ModParticles;
import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.EnumSet;

public class ScreamerZombie extends BaseSevenDaysZombie {

    private static final EntityDataAccessor<Boolean> DATA_SCREAMING =
            SynchedEntityData.defineId(ScreamerZombie.class, EntityDataSerializers.BOOLEAN);

    private int screamCooldown = 0;
    private int totalScreams = 0;
    private boolean hasFled = false;
    private int screamVfxTicks = 0;
    private static final int MAX_SCREAMS = 3;
    private static final int SCREAM_COOLDOWN_TICKS = 600;
    private static final int FLEE_DURATION_TICKS = 100;

    public ScreamerZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.SCREAMER);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCREAMING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new ScreamerFleeGoal(this));
        goalSelector.addGoal(2, new ScreamGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.screamerHP.get();
        double damage = cfg.screamerDamage.get();
        double speed = convertSpeedToAttribute(cfg.screamerSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (screamCooldown > 0) screamCooldown--;

        if (!level().isClientSide() && entityData.get(DATA_SCREAMING)) {
            screamVfxTicks--;
            if (screamVfxTicks <= 0) {
                entityData.set(DATA_SCREAMING, false);
            }
        }

        if (level().isClientSide() && entityData.get(DATA_SCREAMING) && tickCount % 2 == 0) {
            int ringCount = 5;
            float radius = ((tickCount % 20) + 1) * 0.5f;
            for (int i = 0; i < ringCount; i++) {
                double angle = (Math.PI * 2.0 / ringCount) * i + (tickCount * 0.3);
                double px = getX() + Math.cos(angle) * radius;
                double pz = getZ() + Math.sin(angle) * radius;
                level().addParticle(ModParticles.SONIC_PULSE.get(),
                        px, getY() + getBbHeight() * 0.5, pz,
                        Math.cos(angle) * 0.1, 0, Math.sin(angle) * 0.1);
            }
        }

        if (level().isClientSide() && entityData.get(DATA_SCREAMING) && tickCount % 6 == 0) {
            level().addParticle(ParticleTypes.NOTE,
                    getX() + (random.nextFloat() - 0.5) * 0.5,
                    getY() + getBbHeight() + 0.2,
                    getZ() + (random.nextFloat() - 0.5) * 0.5,
                    random.nextFloat(), 0, 0);
        }
    }

    private void performScream() {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) return;

        playSound(SoundEvents.GHAST_SCREAM, 3.0f, 1.5f);
        screamVfxTicks = 20;
        entityData.set(DATA_SCREAMING, true);

        ZombieConfig cfg = ZombieConfig.INSTANCE;
        int spawnCount = cfg.screamerSpawnMin.get() +
                random.nextInt(cfg.screamerSpawnMax.get() - cfg.screamerSpawnMin.get() + 1);

        for (int i = 0; i < spawnCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int dist = 8 + random.nextInt(12);
            int x = (int) (getX() + Math.cos(angle) * dist);
            int z = (int) (getZ() + Math.sin(angle) * dist);
            int y = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            BaseSevenDaysZombie walker = ModEntities.WALKER.get()
                    .create(serverLevel, EntitySpawnReason.EVENT);
            if (walker != null) {
                walker.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360f, 0f);
                walker.setPersistenceRequired();
                if (getTarget() != null) walker.setTarget(getTarget());
                serverLevel.addFreshEntity(walker);
            }
        }

        screamCooldown = SCREAM_COOLDOWN_TICKS;
        totalScreams++;
        hasFled = false;

        SevenDaysToMinecraft.LOGGER.info("[BZHS] Screamer screamed! Spawned {} zombies (scream {}/{})",
                spawnCount, totalScreams, MAX_SCREAMS);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 1.9)
                .add(Attributes.MOVEMENT_SPEED, 0.15);
    }

    private static class ScreamGoal extends Goal {
        private final ScreamerZombie screamer;

        ScreamGoal(ScreamerZombie screamer) {
            this.screamer = screamer;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = screamer.getTarget();
            if (target == null || !target.isAlive()) return false;
            return screamer.screamCooldown <= 0
                    && screamer.totalScreams < MAX_SCREAMS
                    && screamer.distanceTo(target) < 16.0;
        }

        @Override
        public void start() {
            screamer.performScream();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    private static class ScreamerFleeGoal extends Goal {
        private final ScreamerZombie screamer;
        private int fleeTicks = 0;

        ScreamerFleeGoal(ScreamerZombie screamer) {
            this.screamer = screamer;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (screamer.hasFled) return false;
            if (screamer.totalScreams <= 0) return false;
            if (screamer.screamCooldown <= 0) return false;
            LivingEntity target = screamer.getTarget();
            return target != null && target.isAlive() && screamer.distanceTo(target) < 5.0;
        }

        @Override
        public void start() {
            fleeTicks = 0;
            fleeFromTarget();
        }

        @Override
        public boolean canContinueToUse() {
            if (fleeTicks >= FLEE_DURATION_TICKS) {
                screamer.hasFled = true;
                return false;
            }
            return true;
        }

        @Override
        public void tick() {
            fleeTicks++;
            if (fleeTicks % 20 == 0) {
                fleeFromTarget();
            }
        }

        private void fleeFromTarget() {
            LivingEntity target = screamer.getTarget();
            if (target == null) return;

            double dx = screamer.getX() - target.getX();
            double dz = screamer.getZ() - target.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.01) { dx = 1; dz = 0; dist = 1; }

            double fleeX = screamer.getX() + (dx / dist) * 10;
            double fleeZ = screamer.getZ() + (dz / dist) * 10;
            double fleeY = screamer.getY();

            screamer.getNavigation().moveTo(fleeX, fleeY, fleeZ, 1.5);
        }
    }
}
