package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;


/**
 * Generates underground bunker dungeons for 5-star territory areas.
 * Layout: small surface entrance → staircase → linear chain of rooms going deeper.
 */
public class BunkerGenerator {

    private static final int ROOM_WIDTH = 9;
    private static final int ROOM_HEIGHT = 5;
    private static final int ROOM_DEPTH = 9;
    private static final int CORRIDOR_LENGTH = 6;
    private static final int MIN_ROOMS = 5;
    private static final int MAX_ROOMS = 8;
    private static final int STAIRCASE_DEPTH = 12;

    public static class BunkerResult {
        public final BlockPos entrancePos;
        public final List<BlockPos> zombieSpawnPositions;
        public final List<BlockPos> lootPositions;
        public final int roomCount;

        public BunkerResult(BlockPos entrancePos, List<BlockPos> zombieSpawns,
                            List<BlockPos> lootPositions, int roomCount) {
            this.entrancePos = entrancePos;
            this.zombieSpawnPositions = zombieSpawns;
            this.lootPositions = lootPositions;
            this.roomCount = roomCount;
        }
    }

    public static BunkerResult generate(ServerLevel level, BlockPos surfacePos, RandomSource random) {
        List<BlockPos> zombieSpawns = new ArrayList<>();
        List<BlockPos> lootPositions = new ArrayList<>();
        int roomCount = MIN_ROOMS + random.nextInt(MAX_ROOMS - MIN_ROOMS + 1);

        // 1. Build surface entrance (small 5x5 concrete shed)
        buildSurfaceEntrance(level, surfacePos);

        // 2. Build staircase going down
        BlockPos stairBottom = buildStaircase(level, surfacePos, STAIRCASE_DEPTH);

        // 3. Generate rooms in a linear chain, alternating X and Z directions
        BlockPos currentPos = stairBottom;
        int direction = 0; // 0=+X, 1=+Z, 2=-X, 3=-Z

        for (int i = 0; i < roomCount; i++) {
            boolean isFinalRoom = (i == roomCount - 1);

            // Carve the room
            carveRoom(level, currentPos, random, isFinalRoom);

            // Place zombies (more in later rooms)
            int zombieCount = 2 + (i / 2) + random.nextInt(2);
            placeZombiesInRoom(level, currentPos, zombieCount, random, zombieSpawns);

            // Place loot (more in final room)
            if (isFinalRoom) {
                placeFinalRoomLoot(level, currentPos, random, lootPositions);
            } else if (random.nextFloat() < 0.6f) {
                placeRoomLoot(level, currentPos, random, lootPositions);
            }

            if (i < roomCount - 1) {
                // Build corridor to next room
                direction = (direction + 1 + random.nextInt(2)) % 4;
                BlockPos corridorEnd = buildCorridor(level, currentPos, direction, random);
                currentPos = corridorEnd;
                // Go slightly deeper with each room
                currentPos = currentPos.below(1 + random.nextInt(2));
            }
        }

        SevenDaysToMinecraft.LOGGER.info(
                "[BZHS Bunker] Generated underground bunker at ({}, {}, {}) with {} rooms, {} zombie spawns, {} loot containers",
                surfacePos.getX(), surfacePos.getY(), surfacePos.getZ(),
                roomCount, zombieSpawns.size(), lootPositions.size());

        return new BunkerResult(surfacePos, zombieSpawns, lootPositions, roomCount);
    }

    private static void buildSurfaceEntrance(ServerLevel level, BlockPos pos) {
        // Clear and build a small 5x5 concrete bunker entrance on the surface
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 3; y++) {
                    BlockPos bp = pos.offset(x, y, z);
                    boolean isWall = Math.abs(x) == 2 || Math.abs(z) == 2;
                    boolean isRoof = y == 3;
                    boolean isFloor = y == 0;
                    boolean isDoor = x == 0 && z == 2 && (y == 1 || y == 2);

                    if (isDoor) {
                        setBlock(level, bp, Blocks.AIR.defaultBlockState());
                    } else if (isRoof || (isWall && y > 0)) {
                        setBlock(level, bp, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                    } else if (isFloor) {
                        setBlock(level, bp, Blocks.STONE_BRICKS.defaultBlockState());
                    } else if (!isWall) {
                        setBlock(level, bp, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // Iron door frame
        setBlock(level, pos.offset(0, 3, 2), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());

        // Place a torch inside
        try {
            setBlock(level, pos.offset(0, 2, -1), Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.SOUTH));
        } catch (Exception ignored) {
            // Fallback if torch placement fails
        }
    }

    private static BlockPos buildStaircase(ServerLevel level, BlockPos startPos, int depth) {
        BlockPos current = startPos.offset(0, 0, 0);

        for (int d = 0; d < depth; d++) {
            // Each step: go down 1 and forward (north) 1
            current = current.below(1).north(1);

            // Carve 3-wide staircase
            for (int x = -1; x <= 1; x++) {
                // Floor
                setBlock(level, current.offset(x, 0, 0), Blocks.STONE_BRICK_STAIRS.defaultBlockState());
                // Air above
                setBlock(level, current.offset(x, 1, 0), Blocks.AIR.defaultBlockState());
                setBlock(level, current.offset(x, 2, 0), Blocks.AIR.defaultBlockState());
                // Walls
                if (Math.abs(x) == 1) {
                    setBlock(level, current.offset(x > 0 ? 2 : -2, 1, 0), Blocks.STONE_BRICKS.defaultBlockState());
                    setBlock(level, current.offset(x > 0 ? 2 : -2, 2, 0), Blocks.STONE_BRICKS.defaultBlockState());
                }
                // Ceiling
                setBlock(level, current.offset(x, 3, 0), Blocks.STONE_BRICKS.defaultBlockState());
            }

            // Torch every 4 steps
            if (d % 4 == 0) {
                try {
                    setBlock(level, current.offset(1, 2, 0), Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.WEST));
                } catch (Exception ignored) {}
            }
        }

        return current;
    }

    private static void carveRoom(ServerLevel level, BlockPos center, RandomSource random, boolean isFinalRoom) {
        int halfW = ROOM_WIDTH / 2;
        int halfD = ROOM_DEPTH / 2;

        BlockState wallBlock = isFinalRoom
                ? Blocks.DEEPSLATE_BRICKS.defaultBlockState()
                : Blocks.STONE_BRICKS.defaultBlockState();
        BlockState crackedWall = isFinalRoom
                ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
                : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState floorBlock = isFinalRoom
                ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                : Blocks.STONE_BRICKS.defaultBlockState();

        for (int x = -halfW - 1; x <= halfW + 1; x++) {
            for (int z = -halfD - 1; z <= halfD + 1; z++) {
                for (int y = -1; y <= ROOM_HEIGHT; y++) {
                    BlockPos bp = center.offset(x, y, z);
                    boolean isOuterWall = Math.abs(x) == halfW + 1 || Math.abs(z) == halfD + 1;
                    boolean isFloor = y == -1;
                    boolean isCeiling = y == ROOM_HEIGHT;

                    if (isFloor) {
                        setBlock(level, bp, floorBlock);
                    } else if (isCeiling) {
                        setBlock(level, bp, wallBlock);
                    } else if (isOuterWall) {
                        BlockState wall = random.nextFloat() < 0.15f ? crackedWall : wallBlock;
                        setBlock(level, bp, wall);
                    } else {
                        // Interior: air
                        setBlock(level, bp, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // Pillars in larger rooms
        if (ROOM_WIDTH >= 9) {
            for (int y = 0; y < ROOM_HEIGHT; y++) {
                setBlock(level, center.offset(-3, y, -3), wallBlock);
                setBlock(level, center.offset(3, y, -3), wallBlock);
                setBlock(level, center.offset(-3, y, 3), wallBlock);
                setBlock(level, center.offset(3, y, 3), wallBlock);
            }
        }

        // Dim lighting: redstone lamps or lanterns
        try {
            setBlock(level, center.offset(0, ROOM_HEIGHT - 1, 0), Blocks.LANTERN.defaultBlockState());
            if (isFinalRoom) {
                setBlock(level, center.offset(-3, ROOM_HEIGHT - 1, 0), Blocks.LANTERN.defaultBlockState());
                setBlock(level, center.offset(3, ROOM_HEIGHT - 1, 0), Blocks.LANTERN.defaultBlockState());
            }
        } catch (Exception ignored) {}
    }

    private static BlockPos buildCorridor(ServerLevel level, BlockPos fromRoom, int direction, RandomSource random) {
        int dx = 0, dz = 0;
        switch (direction) {
            case 0 -> dx = 1;
            case 1 -> dz = 1;
            case 2 -> dx = -1;
            case 3 -> dz = -1;
        }

        int totalLength = ROOM_WIDTH / 2 + CORRIDOR_LENGTH + ROOM_WIDTH / 2 + 2;
        BlockPos current = fromRoom;

        for (int step = 0; step < totalLength; step++) {
            current = current.offset(dx, 0, dz);

            // Carve 3-wide, 3-tall corridor
            for (int perpX = -1; perpX <= 1; perpX++) {
                for (int y = 0; y < 3; y++) {
                    BlockPos bp;
                    if (dx != 0) {
                        bp = current.offset(0, y, perpX);
                    } else {
                        bp = current.offset(perpX, y, 0);
                    }
                    setBlock(level, bp, Blocks.AIR.defaultBlockState());
                }
                // Floor
                BlockPos floorPos;
                if (dx != 0) {
                    floorPos = current.offset(0, -1, perpX);
                } else {
                    floorPos = current.offset(perpX, -1, 0);
                }
                setBlock(level, floorPos, Blocks.STONE_BRICKS.defaultBlockState());
                // Ceiling
                BlockPos ceilPos;
                if (dx != 0) {
                    ceilPos = current.offset(0, 3, perpX);
                } else {
                    ceilPos = current.offset(perpX, 3, 0);
                }
                setBlock(level, ceilPos, Blocks.STONE_BRICKS.defaultBlockState());
            }

            // Walls on sides
            for (int y = 0; y < 3; y++) {
                if (dx != 0) {
                    setBlock(level, current.offset(0, y, -2), Blocks.STONE_BRICKS.defaultBlockState());
                    setBlock(level, current.offset(0, y, 2), Blocks.STONE_BRICKS.defaultBlockState());
                } else {
                    setBlock(level, current.offset(-2, y, 0), Blocks.STONE_BRICKS.defaultBlockState());
                    setBlock(level, current.offset(2, y, 0), Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
        }

        return current;
    }

    private static void placeZombiesInRoom(ServerLevel level, BlockPos roomCenter, int count,
                                            RandomSource random, List<BlockPos> allSpawns) {
        int halfW = ROOM_WIDTH / 2 - 1;
        int halfD = ROOM_DEPTH / 2 - 1;

        for (int i = 0; i < count; i++) {
            int offsetX = random.nextInt(halfW * 2 + 1) - halfW;
            int offsetZ = random.nextInt(halfD * 2 + 1) - halfD;
            BlockPos spawnPos = roomCenter.offset(offsetX, 0, offsetZ);
            allSpawns.add(spawnPos);
        }
    }

    private static void placeRoomLoot(ServerLevel level, BlockPos roomCenter,
                                       RandomSource random, List<BlockPos> lootPositions) {
        // Place 1-2 loot containers along the wall
        int halfW = ROOM_WIDTH / 2;
        BlockPos lootPos = roomCenter.offset(halfW, 0, random.nextInt(3) - 1);

        Block lootBlock = random.nextBoolean()
                ? ModBlocks.SUPPLY_CRATE_BLOCK.get()
                : ModBlocks.MUNITIONS_BOX_BLOCK.get();

        setBlock(level, lootPos, lootBlock.defaultBlockState());
        if (level.getBlockEntity(lootPos) instanceof LootContainerBlockEntity be) {
            be.setTerritoryTier(5);
        }
        lootPositions.add(lootPos);
    }

    private static void placeFinalRoomLoot(ServerLevel level, BlockPos roomCenter,
                                            RandomSource random, List<BlockPos> lootPositions) {
        // 3-4 high-tier loot containers in the final room
        int[][] positions = {{-2, -3}, {2, -3}, {-2, 3}, {2, 3}};

        Block[] lootBlocks = {
                ModBlocks.GUN_SAFE_BLOCK.get(),
                ModBlocks.SUPPLY_CRATE_BLOCK.get(),
                ModBlocks.SUPPLY_CRATE_BLOCK.get(),
                ModBlocks.MUNITIONS_BOX_BLOCK.get()
        };

        for (int i = 0; i < positions.length; i++) {
            BlockPos lootPos = roomCenter.offset(positions[i][0], 0, positions[i][1]);
            setBlock(level, lootPos, lootBlocks[i].defaultBlockState());
            if (level.getBlockEntity(lootPos) instanceof LootContainerBlockEntity be) {
                be.setTerritoryTier(5);
            }
            lootPositions.add(lootPos);
        }
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isLoaded(pos) && pos.getY() > level.getMinY() && pos.getY() < level.getMaxY()) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
