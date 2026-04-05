package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TerritoryConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.worldgen.BiomeProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class TerritoryZombieSpawner {

    public static void populate(ServerLevel level, TerritoryRecord record,
                                List<BlockPos> spawnPositions) {
        if (record.isCleared()) return;

        TerritoryTier tier = record.getTier();
        @SuppressWarnings("unchecked")
        Supplier<EntityType<? extends BaseSevenDaysZombie>>[] pool = tier.getZombiePool();
        if (pool.length == 0) return;

        BlockPos center = spawnPositions.isEmpty() ? record.getOrigin() : spawnPositions.get(0);
        Holder<Biome> biomeHolder = level.getBiome(center);
        float densityMult = BiomeProperties.getStats(biomeHolder).zombieDensityMultiplier();
        int baseCount = Math.min(tier.getMaxZombies(), Math.max(1, spawnPositions.size()));
        int count = Math.max(1, Math.round(baseCount * densityMult));
        int spawned = 0;

        int currentDay = (int) (level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        boolean checkMinDay = TerritoryConfig.INSTANCE.enforceMinSpawnDay.get();

        @SuppressWarnings("unchecked")
        Supplier<EntityType<? extends BaseSevenDaysZombie>>[] fallbackPool = new Supplier[] {
                ModEntities.WALKER, ModEntities.CRAWLER
        };

        for (int i = 0; i < spawnPositions.size() && spawned < count; i++) {
            BlockPos rawPos = spawnPositions.get(i);
            BlockPos spawnPos = findSafeInteriorY(level, rawPos);
            if (spawnPos == null) continue;

            Supplier<EntityType<? extends BaseSevenDaysZombie>> typeSupplier =
                    pool[level.random.nextInt(pool.length)];

            BaseSevenDaysZombie zombie = typeSupplier.get().create(level, EntitySpawnReason.STRUCTURE);
            if (zombie == null) continue;

            if (checkMinDay && zombie.getVariant().getMinSpawnDay() > currentDay) {
                zombie.discard();
                typeSupplier = fallbackPool[level.random.nextInt(fallbackPool.length)];
                zombie = typeSupplier.get().create(level, EntitySpawnReason.STRUCTURE);
                if (zombie == null) continue;
            }

            zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    level.random.nextFloat() * 360f, 0f);
            zombie.setPersistenceRequired();
            zombie.addTag("bzhs_territory_" + record.getId());
            level.addFreshEntity(zombie);
            spawned++;
        }

        record.setZombiesRemaining(spawned);
        if (spawned > 0) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Territory] Populated territory #{} ({} {}) with {} zombies",
                    record.getId(), record.getType().getDisplayName(), record.getTier().getStars(), spawned);
        }
    }

    private static BlockPos findSafeInteriorY(ServerLevel level, BlockPos rawPos) {
        BlockPos result = findSafeInteriorYAt(level, rawPos);
        if (result != null) return result;

        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] off : offsets) {
            result = findSafeInteriorYAt(level, rawPos.offset(off[0], 0, off[1]));
            if (result != null) return result;
        }

        SevenDaysToMinecraft.LOGGER.debug(
                "[BZHS Territory] Skipped spawn position at ({}, {}, {}): no valid 2-block air gap found",
                rawPos.getX(), rawPos.getY(), rawPos.getZ());
        return null;
    }

    private static BlockPos findSafeInteriorYAt(ServerLevel level, BlockPos rawPos) {
        int startY = rawPos.getY();
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos test = new BlockPos(rawPos.getX(), startY + dy, rawPos.getZ());
            BlockState below = level.getBlockState(test.below());
            BlockState here = level.getBlockState(test);
            BlockState above = level.getBlockState(test.above());
            if ((below.isSolid() || !below.isAir()) && here.isAir() && above.isAir()) {
                return test;
            }
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos test = new BlockPos(rawPos.getX(), startY - dy, rawPos.getZ());
            BlockState below = level.getBlockState(test.below());
            BlockState here = level.getBlockState(test);
            BlockState above = level.getBlockState(test.above());
            if ((below.isSolid() || !below.isAir()) && here.isAir() && above.isAir()) {
                return test;
            }
        }
        return null;
    }
}
