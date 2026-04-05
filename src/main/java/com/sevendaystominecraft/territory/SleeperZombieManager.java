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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SleeperZombieManager {

    private static final String SLEEPER_TAG = "bzhs_sleeper";
    private static final String BUILDING_TAG_PREFIX = "bzhs_building_";

    public static void spawnSleepers(ServerLevel level, TerritoryRecord record,
                                      List<List<BlockPos>> perBuildingSpawns) {
        if (record.isCleared()) return;

        TerritoryTier tier = record.getTier();
        @SuppressWarnings("unchecked")
        Supplier<EntityType<? extends BaseSevenDaysZombie>>[] pool = tier.getZombiePool();
        if (pool.length == 0) return;

        int currentDay = (int) (level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        boolean checkMinDay = TerritoryConfig.INSTANCE.enforceMinSpawnDay.get();

        @SuppressWarnings("unchecked")
        Supplier<EntityType<? extends BaseSevenDaysZombie>>[] fallbackPool = new Supplier[] {
                ModEntities.WALKER, ModEntities.CRAWLER
        };

        int totalSpawned = 0;

        for (int buildingIdx = 0; buildingIdx < perBuildingSpawns.size(); buildingIdx++) {
            List<BlockPos> spawnPositions = perBuildingSpawns.get(buildingIdx);

            BlockPos center = spawnPositions.isEmpty() ? record.getOrigin() : spawnPositions.get(0);
            Holder<Biome> biomeHolder = level.getBiome(center);
            float densityMult = BiomeProperties.getStats(biomeHolder).zombieDensityMultiplier();
            int baseCount = Math.max(1, spawnPositions.size());
            int count = Math.max(1, Math.round(baseCount * densityMult));
            int spawned = 0;

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
                zombie.addTag(SLEEPER_TAG);
                zombie.addTag(BUILDING_TAG_PREFIX + buildingIdx);

                zombie.setNoAi(true);

                level.addFreshEntity(zombie);
                spawned++;
            }
            totalSpawned += spawned;
        }

        record.setZombiesRemaining(totalSpawned);
        if (totalSpawned > 0) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Territory] Placed {} sleeper zombies across {} buildings in territory #{} ({} {})",
                    totalSpawned, perBuildingSpawns.size(), record.getId(),
                    record.getType().getDisplayName(), record.getTier().getStars());
        }
    }

    public static void awakenSleepersForBuilding(ServerLevel level, TerritoryRecord record, int buildingIndex) {
        String territoryTag = "bzhs_territory_" + record.getId();
        String buildingTag = BUILDING_TAG_PREFIX + buildingIndex;

        List<BaseSevenDaysZombie> sleepers = new ArrayList<>();
        for (var entity : level.getAllEntities()) {
            if (entity instanceof BaseSevenDaysZombie zombie) {
                if (zombie.getTags().contains(territoryTag)
                        && zombie.getTags().contains(SLEEPER_TAG)
                        && zombie.getTags().contains(buildingTag)) {
                    sleepers.add(zombie);
                }
            }
        }

        for (BaseSevenDaysZombie zombie : sleepers) {
            zombie.setNoAi(false);
            zombie.removeTag(SLEEPER_TAG);
        }

        if (!sleepers.isEmpty()) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Territory] Awakened {} sleeper zombies in building #{} of territory #{}",
                    sleepers.size(), buildingIndex, record.getId());
        }
    }

    public static void awakenSleepers(ServerLevel level, TerritoryRecord record) {
        String territoryTag = "bzhs_territory_" + record.getId();

        List<BaseSevenDaysZombie> sleepers = new ArrayList<>();
        for (var entity : level.getAllEntities()) {
            if (entity instanceof BaseSevenDaysZombie zombie) {
                if (zombie.getTags().contains(territoryTag) && zombie.getTags().contains(SLEEPER_TAG)) {
                    sleepers.add(zombie);
                }
            }
        }

        for (BaseSevenDaysZombie zombie : sleepers) {
            zombie.setNoAi(false);
            zombie.removeTag(SLEEPER_TAG);
        }

        if (!sleepers.isEmpty()) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Territory] Awakened {} sleeper zombies in territory #{}",
                    sleepers.size(), record.getId());
        }
    }

    public static boolean isSleeper(BaseSevenDaysZombie zombie) {
        return zombie.getTags().contains(SLEEPER_TAG);
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
                "[BZHS Territory] Skipped sleeper spawn position at ({}, {}, {}): no valid 2-block air gap found",
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
