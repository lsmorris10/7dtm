package com.sevendaystominecraft.heatmap;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class SkyBaseDetector {

    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final int MIN_ELEVATION_ABOVE_GROUND = 10;
    private static final int MAX_SCAN_DEPTH = 64;
    private static final float HEAT_PER_CHECK = 3.0f;
    private static final float HEAT_DECAY_PER_MINUTE = 1.0f;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL_TICKS) return;
        tickCounter = 0;

        HeatmapData data = HeatmapData.getOrCreate(level);

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) continue;

            BlockPos playerPos = player.blockPosition();
            int elevationAboveGround = getElevationAboveNaturalGround(level, playerPos);

            if (elevationAboveGround >= MIN_ELEVATION_ABOVE_GROUND) {
                ChunkPos chunk = new ChunkPos(playerPos);
                float bonus = Math.min(HEAT_PER_CHECK * (elevationAboveGround / (float) MIN_ELEVATION_ABOVE_GROUND), 12.0f);
                data.addHeatSource(chunk, bonus, HEAT_DECAY_PER_MINUTE, 0);

                if (elevationAboveGround >= MIN_ELEVATION_ABOVE_GROUND * 2) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§c§oThe horde senses your elevated position... they're getting agitated."),
                            true);
                }
            }
        }
    }

    private static int getElevationAboveNaturalGround(ServerLevel level, BlockPos playerPos) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(
                playerPos.getX(), playerPos.getY() - 1, playerPos.getZ());

        for (int depth = 0; depth < MAX_SCAN_DEPTH; depth++) {
            if (checkPos.getY() <= level.getMinY()) {
                return depth;
            }

            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir() && isNaturalBlock(state)) {
                return depth;
            }

            checkPos.move(0, -1, 0);
        }

        return MAX_SCAN_DEPTH;
    }

    private static boolean isNaturalBlock(BlockState state) {
        String blockName = state.getBlock().getDescriptionId();
        return blockName.contains("stone") ||
                blockName.contains("dirt") ||
                blockName.contains("grass") ||
                blockName.contains("sand") ||
                blockName.contains("gravel") ||
                blockName.contains("clay") ||
                blockName.contains("terracotta") ||
                blockName.contains("bedrock") ||
                blockName.contains("deepslate") ||
                blockName.contains("tuff") ||
                blockName.contains("diorite") ||
                blockName.contains("granite") ||
                blockName.contains("andesite") ||
                blockName.contains("ore") ||
                blockName.contains("basalt") ||
                blockName.contains("calcite") ||
                blockName.contains("dripstone") ||
                blockName.contains("water") ||
                blockName.contains("lava");
    }
}
