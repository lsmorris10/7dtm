package com.sevendaystominecraft.horde;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HordeConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.ZombieVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.function.Supplier;

public class HordeSpawner {

    private record CompositionRow(int walker, int crawler, int feral, int cop,
                                   int demolisher, int radiated, int charged, int infernal) {
        int total() {
            return walker + crawler + feral + cop + demolisher + radiated + charged + infernal;
        }
    }

    private static final CompositionRow DAY_7  = new CompositionRow(70, 20, 10,  0,  0,  0,  0,  0);
    private static final CompositionRow DAY_14 = new CompositionRow(40, 15, 30, 15,  0,  0,  0,  0);
    private static final CompositionRow DAY_21 = new CompositionRow(15,  8, 25, 18, 12,  5, 10,  7);
    private static final CompositionRow DAY_28 = new CompositionRow( 8,  4, 20, 16, 12, 18, 12, 10);
    private static final CompositionRow DAY_49 = new CompositionRow( 3,  3, 15, 12, 17, 22, 15, 13);

    private static CompositionRow getComposition(int dayNumber) {
        if (dayNumber >= 49) return DAY_49;
        if (dayNumber >= 28) return DAY_28;
        if (dayNumber >= 21) return DAY_21;
        if (dayNumber >= 14) return DAY_14;
        return DAY_7;
    }

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
        CompositionRow comp = applyConfigThresholds(getComposition(dayNumber), dayNumber);

        SevenDaysToMinecraft.LOGGER.info(
                "[BZHS Horde] Wave {} | Day {} | Size: {} ({} per player, {} players)",
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

                if (com.sevendaystominecraft.block.building.LandClaimBlock.isWithinAnyClaimRadius(level, spawnPos)) continue;

                BaseSevenDaysZombie zombie = createVariantZombie(level, comp);
                if (zombie != null) {
                    zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                                  level.random.nextFloat() * 360f, 0f);
                    zombie.setHordeMob(true);
                    zombie.setTarget(player);
                    level.addFreshEntity(zombie);
                    spawned++;
                }
            }

            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Horde] Spawned {} zombies near {} (attempted {})",
                    spawned, player.getName().getString(), attempts
            );
        }
    }

    private static CompositionRow applyConfigThresholds(CompositionRow base, int dayNumber) {
        HordeConfig cfg = HordeConfig.INSTANCE;
        int feral = dayNumber >= cfg.feralDay.get() ? base.feral() : 0;
        int cop = dayNumber >= cfg.feralDay.get() ? base.cop() : 0;
        int demolisher = dayNumber >= cfg.demolisherDay.get() ? base.demolisher() : 0;
        int radiated = dayNumber >= 28 ? base.radiated() : 0;
        int charged = dayNumber >= cfg.chargedDay.get() ? base.charged() : 0;
        int infernal = dayNumber >= cfg.infernalDay.get() ? base.infernal() : 0;

        int removed = (base.feral() - feral) + (base.cop() - cop) + (base.demolisher() - demolisher)
                + (base.radiated() - radiated) + (base.charged() - charged) + (base.infernal() - infernal);

        int walker = base.walker() + removed;

        return new CompositionRow(walker, base.crawler(), feral, cop, demolisher, radiated, charged, infernal);
    }

    private static BaseSevenDaysZombie createVariantZombie(ServerLevel level, CompositionRow comp) {
        int roll = level.random.nextInt(comp.total());
        int cumulative = 0;
        boolean applyRadiated = false;

        EntityType<? extends BaseSevenDaysZombie> chosenType;

        cumulative += comp.walker();
        if (roll < cumulative) {
            chosenType = ModEntities.WALKER.get();
        } else {
            cumulative += comp.crawler();
            if (roll < cumulative) {
                chosenType = ModEntities.CRAWLER.get();
            } else {
                cumulative += comp.feral();
                if (roll < cumulative) {
                    chosenType = ModEntities.FERAL_WIGHT.get();
                } else {
                    cumulative += comp.cop();
                    if (roll < cumulative) {
                        chosenType = ModEntities.COP.get();
                    } else {
                        cumulative += comp.demolisher();
                        if (roll < cumulative) {
                            chosenType = ModEntities.DEMOLISHER.get();
                        } else {
                            cumulative += comp.radiated();
                            if (roll < cumulative) {
                                applyRadiated = true;
                                EntityType<? extends BaseSevenDaysZombie>[] bases = getRadiatedBases();
                                chosenType = bases[level.random.nextInt(bases.length)];
                            } else {
                                cumulative += comp.charged();
                                if (roll < cumulative) {
                                    chosenType = ModEntities.CHARGED.get();
                                } else {
                                    chosenType = ModEntities.INFERNAL.get();
                                }
                            }
                        }
                    }
                }
            }
        }

        BaseSevenDaysZombie zombie = chosenType.create(level, EntitySpawnReason.EVENT);
        if (zombie != null && applyRadiated) {
            zombie.setModifier(ZombieVariant.RADIATED);
        }
        return zombie;
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends BaseSevenDaysZombie>[] getRadiatedBases() {
        return new EntityType[] {
                ModEntities.WALKER.get(),
                ModEntities.CRAWLER.get(),
                ModEntities.FERAL_WIGHT.get(),
                ModEntities.COP.get(),
                ModEntities.SOLDIER.get()
        };
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
