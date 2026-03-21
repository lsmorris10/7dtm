package com.sevendaystominecraft.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

public class TerrainColorHelper {

    public static int getTopBlockColor(Level level, int x, int z) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (topY <= -64) return 0xFF222222;

        BlockPos pos = new BlockPos(x, topY - 1, z);
        BlockState state = level.getBlockState(pos);
        MapColor mapColor = state.getMapColor(level, pos);

        if (mapColor == MapColor.NONE) return 0xFF222222;

        int col = mapColor.col;
        if (col == 0) return 0xFF222222;

        return 0xFF000000 | col;
    }

    public static int[] sampleTerrain(Level level, int centerX, int centerZ, int radius, int step) {
        int samplesPerSide = radius / step;
        int totalSamples = samplesPerSide * 2;
        int[] colors = new int[totalSamples * totalSamples];

        for (int sx = -samplesPerSide; sx < samplesPerSide; sx++) {
            for (int sz = -samplesPerSide; sz < samplesPerSide; sz++) {
                int worldX = centerX + sx * step;
                int worldZ = centerZ + sz * step;
                int idx = (sx + samplesPerSide) * totalSamples + (sz + samplesPerSide);
                colors[idx] = getTopBlockColor(level, worldX, worldZ);
            }
        }

        return colors;
    }
}
