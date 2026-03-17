package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ZombieBreakBlockGoal extends Goal {

    private final BaseSevenDaysZombie zombie;
    private BlockPos targetBlockPos;
    private float blockDamageAccumulated;
    private float blockMaxHP;
    private int breakProgressId;
    private int ticksSinceLastCheck;
    private static final int RECHECK_INTERVAL = 20;

    private static final int TARGET_MEMORY_TICKS = 100;
    private LivingEntity lastKnownTarget;
    private int ticksSinceTargetSeen;

    private static final double STUCK_MOVE_THRESHOLD_SQ = 0.25 * 0.25;
    private Vec3 lastStuckCheckPos;
    private int stuckTicks;
    private static final int STUCK_THRESHOLD_TICKS = 30;

    private static final double ABANDON_DISTANCE_SQ = 16.0 * 16.0;

    public ZombieBreakBlockGoal(BaseSevenDaysZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!ZombieConfig.INSTANCE.blockBreakEnabled.get()) return false;
        if (zombie.isNoGravity()) return false;
        if (zombie.level() instanceof ServerLevel serverLevel
                && !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
        LivingEntity target = zombie.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (zombie.getNavigation().isDone() || isPathBlocked() || isStuck()) {
            BlockPos obstruction = findObstructingBlock();
            if (obstruction != null) {
                targetBlockPos = obstruction;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        blockDamageAccumulated = 0;
        BlockState state = zombie.level().getBlockState(targetBlockPos);
        blockMaxHP = BlockHPRegistry.getBlockHP(state);
        breakProgressId = zombie.getId();
        ticksSinceLastCheck = 0;
        lastKnownTarget = zombie.getTarget();
        ticksSinceTargetSeen = 0;
        lastStuckCheckPos = zombie.position();
        stuckTicks = 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (!ZombieConfig.INSTANCE.blockBreakEnabled.get()) return false;
        if (targetBlockPos == null) return false;

        BlockState state = zombie.level().getBlockState(targetBlockPos);
        if (state.isAir() || !BlockHPRegistry.isBreakable(state)) return false;

        LivingEntity currentTarget = zombie.getTarget();
        if (currentTarget != null && currentTarget.isAlive()) {
            lastKnownTarget = currentTarget;
            ticksSinceTargetSeen = 0;
            return true;
        }

        if (lastKnownTarget != null && lastKnownTarget.isAlive()) {
            if (blockDamageAccumulated > 0) {
                return true;
            }
            return ticksSinceTargetSeen < TARGET_MEMORY_TICKS;
        }

        return false;
    }

    @Override
    public void tick() {
        if (targetBlockPos == null || !(zombie.level() instanceof ServerLevel serverLevel)) return;

        ticksSinceTargetSeen++;

        LivingEntity currentTarget = zombie.getTarget();
        if (currentTarget != null && currentTarget.isAlive()) {
            lastKnownTarget = currentTarget;
            ticksSinceTargetSeen = 0;
        }

        if (lastKnownTarget != null && lastKnownTarget.isAlive()) {
            double distToTargetSq = zombie.distanceToSqr(lastKnownTarget);
            if (distToTargetSq > ABANDON_DISTANCE_SQ) {
                serverLevel.destroyBlockProgress(breakProgressId, targetBlockPos, -1);
                targetBlockPos = null;
                return;
            }
        }

        zombie.getLookControl().setLookAt(
                targetBlockPos.getX() + 0.5, targetBlockPos.getY() + 0.5, targetBlockPos.getZ() + 0.5);

        double distSq = zombie.distanceToSqr(
                targetBlockPos.getX() + 0.5, targetBlockPos.getY() + 0.5, targetBlockPos.getZ() + 0.5);
        if (distSq > 4.0) {
            zombie.getNavigation().moveTo(
                    targetBlockPos.getX() + 0.5, targetBlockPos.getY(), targetBlockPos.getZ() + 0.5, 1.0);
            return;
        }

        float attackDamage = (float) zombie.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float speedMult = ZombieConfig.INSTANCE.blockBreakSpeedMultiplier.get().floatValue();
        float damagePerTick = (attackDamage * speedMult) / 20.0f;

        blockDamageAccumulated += damagePerTick;

        if (blockMaxHP > 0) {
            int progress = (int) ((blockDamageAccumulated / blockMaxHP) * 10.0f);
            progress = Math.min(progress, 9);
            serverLevel.destroyBlockProgress(breakProgressId, targetBlockPos, progress);
        }

        if (blockDamageAccumulated >= blockMaxHP) {
            serverLevel.destroyBlockProgress(breakProgressId, targetBlockPos, -1);
            ModSounds.playAtBlock(ModSounds.BLOCK_BREAK_ZOMBIE, serverLevel, targetBlockPos,
                    SoundSource.HOSTILE, 1.0f, 1.0f);
            serverLevel.destroyBlock(targetBlockPos, true, zombie);
            targetBlockPos = null;
        }

        ticksSinceLastCheck++;
        if (ticksSinceLastCheck >= RECHECK_INTERVAL && targetBlockPos != null) {
            ticksSinceLastCheck = 0;
            BlockState current = zombie.level().getBlockState(targetBlockPos);
            if (current.isAir()) {
                targetBlockPos = null;
            }
        }
    }

    @Override
    public void stop() {
        if (targetBlockPos != null && zombie.level() instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlockProgress(breakProgressId, targetBlockPos, -1);
        }
        targetBlockPos = null;
        blockDamageAccumulated = 0;
        lastKnownTarget = null;
        ticksSinceTargetSeen = 0;
        lastStuckCheckPos = null;
        stuckTicks = 0;
    }

    private boolean isPathBlocked() {
        return zombie.getNavigation().isDone() && zombie.getTarget() != null
                && zombie.distanceToSqr(zombie.getTarget()) > 4.0;
    }

    private boolean isStuck() {
        LivingEntity target = zombie.getTarget();
        if (target == null || zombie.getNavigation().isDone()) return false;

        if (lastStuckCheckPos == null) {
            lastStuckCheckPos = zombie.position();
            stuckTicks = 0;
            return false;
        }

        Vec3 currentPos = zombie.position();
        double movedSq = lastStuckCheckPos.distanceToSqr(currentPos);

        if (movedSq < STUCK_MOVE_THRESHOLD_SQ) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
            lastStuckCheckPos = currentPos;
        }

        if (stuckTicks >= STUCK_THRESHOLD_TICKS) {
            stuckTicks = 0;
            lastStuckCheckPos = currentPos;
            return true;
        }

        return false;
    }

    private BlockPos findObstructingBlock() {
        LivingEntity target = zombie.getTarget();
        if (target == null) return null;

        BlockPos zombiePos = zombie.blockPosition();
        BlockPos targetPos = target.blockPosition();

        double dx = targetPos.getX() - zombiePos.getX();
        double dz = targetPos.getZ() - zombiePos.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.01) return null;
        dx /= len;
        dz /= len;

        BlockPos bestPos = null;
        float bestHP = Float.MAX_VALUE;

        for (int i = 1; i <= 3; i++) {
            int checkX = zombiePos.getX() + (int) Math.round(dx * i);
            int checkZ = zombiePos.getZ() + (int) Math.round(dz * i);

            for (int yOff = 0; yOff <= 1; yOff++) {
                BlockPos check = new BlockPos(checkX, zombiePos.getY() + yOff, checkZ);
                BlockState state = zombie.level().getBlockState(check);
                if (!state.isAir() && BlockHPRegistry.isBreakable(state)) {
                    float hp = BlockHPRegistry.getBlockHP(state);
                    if (hp > 0 && hp < bestHP) {
                        bestHP = hp;
                        bestPos = check;
                    }
                }
            }
        }

        return bestPos;
    }
}
