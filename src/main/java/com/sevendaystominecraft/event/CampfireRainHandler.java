package com.sevendaystominecraft.event;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.RainProtectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Handles campfire extinguishing during rain.
 * <p>
 * Every 100 ticks (5 seconds), scans loaded chunks for lit campfires
 * that are exposed to rain (can see sky AND not under a tarp).
 * Exposed campfires are extinguished by setting their LIT state to false.
 * <p>
 * Campfires under tarps or solid roofs remain lit.
 */
@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class CampfireRainHandler {

    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 100; // Every 5 seconds

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.isRaining()) continue;

            // Iterate through all loaded chunk sections and find campfires
            // We use a player-proximity approach for efficiency
            level.players().forEach(player -> {
                BlockPos playerPos = player.blockPosition();
                int radius = 64; // Check campfires within 64 blocks of each player

                for (int x = -radius; x <= radius; x += 4) {
                    for (int z = -radius; z <= radius; z += 4) {
                        for (int y = -8; y <= 8; y++) {
                            BlockPos checkPos = playerPos.offset(x, y, z);
                            BlockState state = level.getBlockState(checkPos);

                            if (state.getBlock() instanceof CampfireBlock
                                    && state.hasProperty(CampfireBlock.LIT)
                                    && state.getValue(CampfireBlock.LIT)) {

                                // Check if exposed to rain
                                if (level.canSeeSky(checkPos)
                                        && !RainProtectionHelper.hasTarpAbove(level, checkPos)) {
                                    // Extinguish the campfire
                                    level.setBlock(checkPos,
                                            state.setValue(CampfireBlock.LIT, false), 3);
                                    SevenDaysToMinecraft.LOGGER.debug(
                                            "BZHS: Rain extinguished campfire at {}",
                                            checkPos.toShortString());
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
