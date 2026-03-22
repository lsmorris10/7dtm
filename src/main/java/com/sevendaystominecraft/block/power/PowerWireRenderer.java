package com.sevendaystominecraft.block.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public class PowerWireRenderer {

    private static final int PARTICLES_PER_WIRE = 4;

    public static void spawnWireParticles(ServerLevel level, BlockPos sourcePos, Set<BlockPos> devices, boolean powered) {
        for (BlockPos devicePos : devices) {
            for (int i = 1; i <= PARTICLES_PER_WIRE; i++) {
                float t = (float) i / (PARTICLES_PER_WIRE + 1);
                double x = sourcePos.getX() + 0.5 + (devicePos.getX() - sourcePos.getX()) * t;
                double y = sourcePos.getY() + 0.5 + (devicePos.getY() - sourcePos.getY()) * t;
                double z = sourcePos.getZ() + 0.5 + (devicePos.getZ() - sourcePos.getZ()) * t;

                if (powered) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0);
                } else {
                    level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
