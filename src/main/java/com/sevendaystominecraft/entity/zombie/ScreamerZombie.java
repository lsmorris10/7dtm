package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.ModEntities;
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

    private int screamCooldown = 0;
    private int totalScreams = 0;
    private static final int MAX_SCREAMS = 3;
    private static final int SCREAM_COOLDOWN_TICKS = 600;

    public ScreamerZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.SCREAMER);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new ScreamGoal(this));
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
    }

    private void performScream() {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) return;

        playSound(SoundEvents.GHAST_SCREAM, 3.0f, 1.5f);

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

        SevenDaysToMinecraft.LOGGER.info("[7DTM] Screamer screamed! Spawned {} zombies (scream {}/{})",
                spawnCount, totalScreams, MAX_SCREAMS);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15);
    }

    private static class ScreamGoal extends Goal {
        private final ScreamerZombie screamer;
        private boolean hasScreamed = false;

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
            hasScreamed = true;
        }

        @Override
        public boolean canContinueToUse() {
            if (hasScreamed) {
                hasScreamed = false;
                return false;
            }
            return false;
        }
    }
}
