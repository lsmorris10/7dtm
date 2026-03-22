package com.sevendaystominecraft.block.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricFencePostBlock extends Block {

    private static final float DAMAGE = 5.0f;
    private static final int STUN_DURATION_TICKS = 40;

    public ElectricFencePostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !(living instanceof Player)) {
            living.hurt(level.damageSources().lightningBolt(), DAMAGE);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_DURATION_TICKS, 5, false, true));
        }
    }
}
