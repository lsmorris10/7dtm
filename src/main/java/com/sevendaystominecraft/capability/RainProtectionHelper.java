package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.block.building.TarpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Utility to determine if a player (or block position) is sheltered from rain
 * by either a vanilla solid roof or a modded tarp canopy.
 */
public class RainProtectionHelper {

    /**
     * Returns true if the position is protected from rain.
     * Checks for:
     * 1. Vanilla solid-block roof (canSeeSky = false)
     * 2. A TarpBlock anywhere above the position (within 16 blocks up)
     */
    public static boolean isProtectedFromRain(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;

        BlockPos pos = player.blockPosition();

        // If vanilla says we can't see sky, we're under a roof
        if (!serverLevel.canSeeSky(pos)) return true;

        // Scan upward for tarp blocks (max 16 blocks)
        return hasTarpAbove(serverLevel, pos);
    }

    /**
     * Checks whether a tarp block exists above the given position.
     *
     * @param level the server level
     * @param pos   the position to check above
     * @return true if a TarpBlock is found within 16 blocks above
     */
    public static boolean hasTarpAbove(ServerLevel level, BlockPos pos) {
        for (int y = 1; y <= 16; y++) {
            BlockPos above = pos.above(y);
            if (level.getBlockState(above).getBlock() instanceof TarpBlock) {
                return true;
            }
            // If we hit a solid opaque block, stop scanning
            if (level.getBlockState(above).isSolidRender()) {
                return true;
            }
        }
        return false;
    }
}
