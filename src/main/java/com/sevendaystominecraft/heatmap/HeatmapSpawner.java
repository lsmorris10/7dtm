package com.sevendaystominecraft.heatmap;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HeatmapConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.horde.BloodMoonTracker;
import com.sevendaystominecraft.territory.TerritoryWorldGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class HeatmapSpawner {

    private static final int SCOUT_COOLDOWN_TICKS = 30 * 20;
    private static final int SCREAMER_COOLDOWN_TICKS = 60 * 20;
    private static final int MINI_HORDE_COOLDOWN_TICKS = 90 * 20;
    private static final int WAVE_COOLDOWN_TICKS = 90 * 20;
    private static final float WAVE_EXIT_THRESHOLD_RATIO = 0.75f;

    private static final Map<Long, Integer> scoutCooldowns = new HashMap<>();
    private static final Map<Long, Integer> screamerCooldowns = new HashMap<>();
    private static final Map<Long, Integer> miniHordeCooldowns = new HashMap<>();
    private static final Map<Long, Integer> waveCooldowns = new HashMap<>();
    private static final Set<Long> waveActiveChunks = new HashSet<>();

    public static void tick(ServerLevel level, HeatmapData data) {
        BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(level);
        if (tracker.isBloodMoonActive()) return;

        float thresholdMult = HeatmapConfig.INSTANCE.spawnThresholdMultiplier.get().floatValue();
        float scoutThreshold = 25.0f * thresholdMult;
        float screamerThreshold = 50.0f * thresholdMult;
        float miniHordeThreshold = 75.0f * thresholdMult;
        float waveThreshold = 100.0f * thresholdMult;
        float waveExitThreshold = waveThreshold * WAVE_EXIT_THRESHOLD_RATIO;

        decrementCooldowns(scoutCooldowns);
        decrementCooldowns(screamerCooldowns);
        decrementCooldowns(miniHordeCooldowns);
        decrementCooldowns(waveCooldowns);

        for (ServerPlayer player : level.players()) {
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            long chunkKey = playerChunk.toLong();
            float heat = data.getHeat(playerChunk);

            if (heat < scoutThreshold) continue;

            if (heat >= waveThreshold) {
                waveActiveChunks.add(chunkKey);
            }

            boolean inWaveMode = waveActiveChunks.contains(chunkKey);

            if (inWaveMode) {
                if (heat < waveExitThreshold) {
                    waveActiveChunks.remove(chunkKey);
                } else if (!hasCooldown(waveCooldowns, chunkKey)) {
                    spawnWave(level, player, 8 + level.random.nextInt(5));
                    waveCooldowns.put(chunkKey, WAVE_COOLDOWN_TICKS);
                    SevenDaysToMinecraft.LOGGER.info(
                            "[BZHS Heatmap] Continuous wave spawned near {} (heat: {})",
                            player.getName().getString(), String.format("%.1f", heat));
                }
            } else if (heat >= miniHordeThreshold && !hasCooldown(miniHordeCooldowns, chunkKey)) {
                spawnHorde(level, player, 8 + level.random.nextInt(5));
                miniHordeCooldowns.put(chunkKey, MINI_HORDE_COOLDOWN_TICKS);
                SevenDaysToMinecraft.LOGGER.info(
                        "[BZHS Heatmap] Mini-horde spawned near {} (heat: {})",
                        player.getName().getString(), String.format("%.1f", heat));
            } else if (heat >= scoutThreshold && !hasCooldown(scoutCooldowns, chunkKey)) {
                int count = 1 + level.random.nextInt(2);
                spawnScouts(level, player, count);
                scoutCooldowns.put(chunkKey, SCOUT_COOLDOWN_TICKS);
                SevenDaysToMinecraft.LOGGER.info(
                        "[BZHS Heatmap] {} scout(s) spawned near {} (heat: {})",
                        count, player.getName().getString(), String.format("%.1f", heat));
            }

            if (heat >= screamerThreshold && !hasCooldown(screamerCooldowns, chunkKey)) {
                spawnScreamer(level, player);
                screamerCooldowns.put(chunkKey, SCREAMER_COOLDOWN_TICKS);
                SevenDaysToMinecraft.LOGGER.info(
                        "[BZHS Heatmap] Screamer spawned near {} (heat: {})",
                        player.getName().getString(), String.format("%.1f", heat));
            }
        }
    }

    private static void spawnScouts(ServerLevel level, ServerPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            spawnZombieNear(level, player, ModEntities.WALKER, false);
        }
    }

    private static void spawnScreamer(ServerLevel level, ServerPlayer player) {
        spawnZombieNear(level, player, ModEntities.SCREAMER, false);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<EntityType<? extends BaseSevenDaysZombie>>[] getSafeZonePool() {
        return new Supplier[] {
                ModEntities.WALKER, ModEntities.WALKER, ModEntities.WALKER,
                ModEntities.CRAWLER,
                ModEntities.BLOATED_WALKER
        };
    }

    private static void spawnHorde(ServerLevel level, ServerPlayer player, int count) {
        boolean inSafeZone = TerritoryWorldGenerator.isInSafeZone(
                player.blockPosition().getX(), player.blockPosition().getZ());

        @SuppressWarnings("unchecked")
        Supplier<EntityType<? extends BaseSevenDaysZombie>>[] waveTypes = inSafeZone
                ? getSafeZonePool()
                : new Supplier[] {
                        ModEntities.WALKER, ModEntities.WALKER, ModEntities.WALKER,
                        ModEntities.CRAWLER,
                        ModEntities.FERAL_WIGHT,
                        ModEntities.SPIDER_ZOMBIE,
                        ModEntities.BLOATED_WALKER,
                        ModEntities.ZOMBIE_BIRD
                };

        for (int i = 0; i < count; i++) {
            Supplier<EntityType<? extends BaseSevenDaysZombie>> type =
                    waveTypes[level.random.nextInt(waveTypes.length)];
            spawnZombieNear(level, player, type, true);
        }
    }

    private static void spawnWave(ServerLevel level, ServerPlayer player, int count) {
        boolean inSafeZone = TerritoryWorldGenerator.isInSafeZone(
                player.blockPosition().getX(), player.blockPosition().getZ());

        @SuppressWarnings("unchecked")
        Supplier<EntityType<? extends BaseSevenDaysZombie>>[] waveTypes = inSafeZone
                ? getSafeZonePool()
                : new Supplier[] {
                        ModEntities.WALKER, ModEntities.WALKER,
                        ModEntities.CRAWLER,
                        ModEntities.FERAL_WIGHT,
                        ModEntities.SPIDER_ZOMBIE,
                        ModEntities.BLOATED_WALKER,
                        ModEntities.COP,
                        ModEntities.ZOMBIE_BIRD,
                        ModEntities.ZOMBIE_PARROT
                };

        for (int i = 0; i < count; i++) {
            Supplier<EntityType<? extends BaseSevenDaysZombie>> type =
                    waveTypes[level.random.nextInt(waveTypes.length)];
            spawnZombieNear(level, player, type, true);
        }
    }

    private static void spawnZombieNear(ServerLevel level, ServerPlayer player,
                                         Supplier<? extends EntityType<? extends BaseSevenDaysZombie>> typeSupplier,
                                         boolean preferDark) {
        BlockPos spawnPos = preferDark
                ? findDarkSpawnPosition(level, player.blockPosition())
                : findSpawnPosition(level, player.blockPosition());
        if (spawnPos == null) return;

        if (com.sevendaystominecraft.block.building.LandClaimBlock.isWithinAnyClaimRadius(level, spawnPos)) return;

        BaseSevenDaysZombie zombie = typeSupplier.get().create(level, EntitySpawnReason.EVENT);
        if (zombie == null) return;

        zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                level.random.nextFloat() * 360f, 0f);
        zombie.setTarget(player);
        level.addFreshEntity(zombie);
    }

    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos playerPos) {
        int minDist = 20;
        int maxDist = 48;

        for (int attempt = 0; attempt < 10; attempt++) {
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

    private static BlockPos findDarkSpawnPosition(ServerLevel level, BlockPos playerPos) {
        int minDist = 20;
        int maxDist = 48;

        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            int dist = minDist + level.random.nextInt(maxDist - minDist + 1);
            int x = playerPos.getX() + (int) (Math.cos(angle) * dist);
            int z = playerPos.getZ() + (int) (Math.sin(angle) * dist);

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
                continue;
            }

            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            int skyLight = level.getBrightness(LightLayer.SKY, pos);
            boolean isDark = blockLight <= 7 && (skyLight <= 7 || !level.isDay());

            if (isDark) {
                return pos;
            }
        }

        return findSpawnPosition(level, playerPos);
    }

    private static void decrementCooldowns(Map<Long, Integer> cooldowns) {
        cooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 20);
            return entry.getValue() <= 0;
        });
    }

    private static boolean hasCooldown(Map<Long, Integer> cooldowns, long chunkKey) {
        return cooldowns.containsKey(chunkKey) && cooldowns.get(chunkKey) > 0;
    }

    public static void clearCooldowns() {
        scoutCooldowns.clear();
        screamerCooldowns.clear();
        miniHordeCooldowns.clear();
        waveCooldowns.clear();
        waveActiveChunks.clear();
    }
}
