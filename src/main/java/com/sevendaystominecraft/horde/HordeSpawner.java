package com.sevendaystominecraft.horde;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HordeConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public class HordeSpawner {

    public static int calculateWaveSize(int dayNumber, int waveIndex) {
        HordeConfig cfg = HordeConfig.INSTANCE;
        int cycleLength = cfg.hordeCycleLength.get();
        int baseCount = cfg.baseCount.get();
        float diffMult = cfg.difficultyMultiplier.get().floatValue();
        int maxPerWave = cfg.maxPerWave.get();

        float cycle = (float) dayNumber / cycleLength;
        int baseSize = (int) Math.floor(baseCount * Math.pow(1 + cycle * diffMult, 1.2));

        float waveMultiplier = 1.0f + 0.25f * waveIndex;
        int waveSize = Math.round(baseSize * waveMultiplier);

        return Math.min(waveSize, maxPerWave);
    }

    public static void spawnWave(ServerLevel level, int waveIndex, int dayNumber) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        int totalSize = calculateWaveSize(dayNumber, waveIndex);
        int perPlayer = Math.max(1, totalSize / players.size());

        SevenDaysToMinecraft.LOGGER.info(
                "[7DTM Horde] Wave {} | Day {} | Size: {} ({} per player, {} players)",
                waveIndex + 1, dayNumber, totalSize, perPlayer, players.size()
        );

        for (ServerPlayer player : players) {
            int spawned = 0;
            int attempts = 0;
            int maxAttempts = perPlayer * 4;

            while (spawned < perPlayer && attempts < maxAttempts) {
                attempts++;
                BlockPos spawnPos = findSpawnPosition(level, player.blockPosition());
                if (spawnPos == null) continue;

                // TODO: Replace with custom zombie variants based on spec §4.2 composition table.
                // Currently spawns vanilla zombies as placeholder.
                // Future: Use dayNumber to determine composition percentages for
                // Walker, Crawler, Feral, Cop, Demolisher, Radiated, Charged, Infernal
                Zombie zombie = (Zombie) EntityType.ZOMBIE.create(level, EntitySpawnReason.EVENT);
                if (zombie != null) {
                    zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 
                                  level.random.nextFloat() * 360f, 0f);
                    zombie.setPersistenceRequired();
                    zombie.setTarget(player);
                    level.addFreshEntity(zombie);
                    spawned++;
                }
            }

            SevenDaysToMinecraft.LOGGER.info(
                    "[7DTM Horde] Spawned {} zombies near {} (attempted {})",
                    spawned, player.getName().getString(), attempts
            );
        }
    }

    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos playerPos) {
        int minDist = 24;
        int maxDist = 40;

        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            int dist = minDist + level.random.nextInt(maxDist - minDist + 1);
            int x = playerPos.getX() + (int) (Math.cos(angle) * dist);
            int z = playerPos.getZ() + (int) (Math.sin(angle) * dist);

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            BlockPos pos = new BlockPos(x, y, z);

            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }

        return null;
    }
}
