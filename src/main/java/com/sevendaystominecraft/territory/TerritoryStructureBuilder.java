package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.block.loot.LootContainerType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TerritoryStructureBuilder {

    public static class BuildResult {
        public final BlockPos labelPos;
        public final List<BlockPos> zombieSpawnPositions;
        public final List<BlockPos> lootPositions;
        public final List<LootContainerType> lootTypes;

        public BuildResult(BlockPos labelPos,
                           List<BlockPos> zombieSpawnPositions,
                           List<BlockPos> lootPositions,
                           List<LootContainerType> lootTypes) {
            this.labelPos = labelPos;
            this.zombieSpawnPositions = zombieSpawnPositions;
            this.lootPositions = lootPositions;
            this.lootTypes = lootTypes;
        }
    }

    public static BuildResult build(ServerLevel level, BlockPos origin, TerritoryTier tier, TerritoryType type, RandomSource random) {
        int size = tier.getMinSize() + random.nextInt(tier.getMaxSize() - tier.getMinSize() + 1);
        int halfSize = size / 2;

        int minY = TerrainValidator.getMinSurfaceY(level, origin.getX(), origin.getZ(), halfSize, halfSize);
        if (minY <= 0) {
            minY = TerrainValidator.findSolidGroundY(level, origin.getX(), origin.getZ());
        }
        BlockPos base = new BlockPos(origin.getX(), minY, origin.getZ());

        Block wallBlock = getWallBlock(type, tier);
        Block floorBlock = getFloorBlock(type);
        Block roofBlock = getRoofBlock(type, tier);

        int wallHeight = 3 + tier.getTier();

        List<BlockPos> zombieSpawnPos = new ArrayList<>();
        List<BlockPos> lootPos = new ArrayList<>();
        List<LootContainerType> lootTypes = new ArrayList<>();

        TerrainValidator.clearVegetation(level, base, halfSize, halfSize, wallHeight + 5);
        TerrainValidator.fillFoundationColumns(level, base, halfSize, halfSize, floorBlock);
        buildWalls(level, base, halfSize, wallHeight, wallBlock);
        buildRoof(level, base, halfSize, wallHeight, roofBlock);

        collectInteriorPositions(base, halfSize, wallHeight, zombieSpawnPos);
        placeLoot(level, base, halfSize, wallHeight, tier, type, lootPos, lootTypes, random);

        BlockPos labelPos = base.above(wallHeight + tier.getLabelHeight());
        return new BuildResult(labelPos, zombieSpawnPos, lootPos, lootTypes);
    }


    private static void buildWalls(ServerLevel level, BlockPos base, int halfSize, int wallHeight, Block wallBlock) {
        for (int dy = 1; dy <= wallHeight; dy++) {
            for (int dx = -halfSize; dx <= halfSize; dx++) {
                setBlock(level, base.offset(dx, dy, -halfSize), wallBlock.defaultBlockState());
                setBlock(level, base.offset(dx, dy, halfSize), wallBlock.defaultBlockState());
            }
            for (int dz = -halfSize + 1; dz < halfSize; dz++) {
                setBlock(level, base.offset(-halfSize, dy, dz), wallBlock.defaultBlockState());
                setBlock(level, base.offset(halfSize, dy, dz), wallBlock.defaultBlockState());
            }
        }

        for (int dy = 1; dy <= Math.min(3, wallHeight); dy++) {
            setBlock(level, base.offset(0, dy, -halfSize), Blocks.AIR.defaultBlockState());
        }
    }

    private static void buildRoof(ServerLevel level, BlockPos base, int halfSize, int wallHeight, Block roofBlock) {
        for (int dx = -halfSize; dx <= halfSize; dx++) {
            for (int dz = -halfSize; dz <= halfSize; dz++) {
                setBlock(level, base.offset(dx, wallHeight + 1, dz), roofBlock.defaultBlockState());
            }
        }
    }

    private static void collectInteriorPositions(BlockPos base, int halfSize, int wallHeight,
                                                  List<BlockPos> spawnPositions) {
        int innerHalf = halfSize - 1;
        if (innerHalf <= 0) {
            spawnPositions.add(base.above(1));
            return;
        }
        for (int dx = -innerHalf; dx <= innerHalf; dx += 2) {
            for (int dz = -innerHalf; dz <= innerHalf; dz += 2) {
                spawnPositions.add(base.offset(dx, 1, dz));
            }
        }
    }

    private static void placeLoot(ServerLevel level, BlockPos base, int halfSize, int wallHeight,
                                   TerritoryTier tier, TerritoryType type,
                                   List<BlockPos> lootPos, List<LootContainerType> lootTypes,
                                   RandomSource random) {
        int innerHalf = halfSize - 1;
        if (innerHalf <= 0) return;

        int count = tier.getLootContainerCount();
        int wallCycle = 0;
        for (int i = 0; i < count; i++) {
            int attempts = 0;
            LootContainerType lootType = (random.nextFloat() < 0.6f)
                    ? type.getPrimaryLoot()
                    : type.getSecondaryLoot();

            while (attempts < 20) {
                int dx, dz;
                int wall = -1;
                if (lootType.isWallFurniture() && innerHalf > 0) {
                    wall = (wallCycle + random.nextInt(2)) % 4;
                    int freeRange = Math.max(1, innerHalf - 1);
                    switch (wall) {
                        case 0 -> { dx = innerHalf;  dz = random.nextInt(freeRange * 2 + 1) - freeRange; }
                        case 1 -> { dx = -innerHalf; dz = random.nextInt(freeRange * 2 + 1) - freeRange; }
                        case 2 -> { dz = innerHalf;  dx = random.nextInt(freeRange * 2 + 1) - freeRange; }
                        default -> { dz = -innerHalf; dx = random.nextInt(freeRange * 2 + 1) - freeRange; }
                    }
                    if (dx == 0 && dz == -innerHalf) {
                        attempts++;
                        continue;
                    }
                    wallCycle = (wall + 1) % 4;
                } else {
                    dx = (innerHalf > 0) ? random.nextInt(innerHalf * 2 + 1) - innerHalf : 0;
                    dz = (innerHalf > 0) ? random.nextInt(innerHalf * 2 + 1) - innerHalf : 0;
                }
                BlockPos pos = base.offset(dx, 1, dz);
                if (!lootPos.contains(pos)) {
                    net.minecraft.world.level.block.Block lootBlock = getLootBlock(lootType);
                    if (lootBlock != null) {
                        if (lootType == LootContainerType.VENDING_MACHINE) {
                            BlockPos abovePos = pos.above();
                            if (abovePos.getY() < level.getMaxY()
                                    && level.getBlockState(abovePos).canBeReplaced()
                                    && !lootPos.contains(abovePos)) {
                                net.minecraft.core.Direction vendingFacing = switch (wall) {
                                    case 0 -> net.minecraft.core.Direction.WEST;
                                    case 1 -> net.minecraft.core.Direction.EAST;
                                    case 2 -> net.minecraft.core.Direction.NORTH;
                                    default -> net.minecraft.core.Direction.SOUTH;
                                };
                                BlockState lowerState = lootBlock.defaultBlockState()
                                        .setValue(com.sevendaystominecraft.block.loot.VendingMachineBlock.HALF,
                                                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                                        .setValue(com.sevendaystominecraft.block.loot.VendingMachineBlock.FACING,
                                                vendingFacing);
                                BlockState upperState = lowerState
                                        .setValue(com.sevendaystominecraft.block.loot.VendingMachineBlock.HALF,
                                                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
                                setBlock(level, pos, lowerState);
                                setBlock(level, abovePos, upperState);
                                if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
                                    be.setTerritoryTier(tier.getTier());
                                }
                                lootPos.add(pos);
                                lootTypes.add(lootType);
                            } else {
                                attempts++;
                                continue;
                            }
                        } else {
                            setBlock(level, pos, lootBlock.defaultBlockState());
                            if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
                                be.setTerritoryTier(tier.getTier());
                            }
                            lootPos.add(pos);
                            lootTypes.add(lootType);
                        }
                        break;
                    }
                }
                attempts++;
            }
        }
    }

    static net.minecraft.world.level.block.Block getLootBlock(LootContainerType type) {
        return switch (type) {
            case KITCHEN_CABINET -> ModBlocks.KITCHEN_CABINET_BLOCK.get();
            case BOOKSHELF -> ModBlocks.BOOKSHELF_CONTAINER_BLOCK.get();
            case CARDBOARD_BOX -> ModBlocks.CARDBOARD_BOX_BLOCK.get();
            case SUPPLY_CRATE -> ModBlocks.SUPPLY_CRATE_BLOCK.get();
            case MUNITIONS_BOX -> ModBlocks.MUNITIONS_BOX_BLOCK.get();
            case GUN_SAFE -> ModBlocks.GUN_SAFE_BLOCK.get();
            case MEDICINE_CABINET -> ModBlocks.MEDICINE_CABINET_BLOCK.get();
            case TRASH_PILE -> ModBlocks.TRASH_PILE_BLOCK.get();
            case TOOL_CRATE -> ModBlocks.TOOL_CRATE_BLOCK.get();
            case FUEL_CACHE -> ModBlocks.FUEL_CACHE_BLOCK.get();
            case VENDING_MACHINE -> ModBlocks.VENDING_MACHINE_BLOCK.get();
            case MAILBOX -> ModBlocks.MAILBOX_BLOCK.get();
            case FARM_CRATE -> ModBlocks.FARM_CRATE_BLOCK.get();
        };
    }

    private static Block getWallBlock(TerritoryType type, TerritoryTier tier) {
        return switch (type) {
            case MILITARY -> (tier.getTier() >= 4) ? Blocks.DEEPSLATE_BRICKS : Blocks.STONE_BRICKS;
            case INDUSTRIAL -> Blocks.STONE_BRICKS;
            case COMMERCIAL -> Blocks.STONE;
            case MEDICAL -> Blocks.WHITE_CONCRETE;
            case WILDERNESS -> Blocks.OAK_LOG;
            default -> (tier.getTier() >= 3) ? Blocks.COBBLESTONE : Blocks.OAK_PLANKS;
        };
    }

    private static Block getFloorBlock(TerritoryType type) {
        return switch (type) {
            case MILITARY, INDUSTRIAL -> Blocks.STONE;
            case WILDERNESS -> Blocks.DIRT;
            default -> Blocks.OAK_PLANKS;
        };
    }

    private static Block getRoofBlock(TerritoryType type, TerritoryTier tier) {
        return switch (type) {
            case MILITARY -> Blocks.DEEPSLATE_BRICKS;
            case INDUSTRIAL -> Blocks.STONE_BRICKS;
            case WILDERNESS -> Blocks.OAK_SLAB;
            default -> Blocks.COBBLESTONE_SLAB;
        };
    }

    public static List<BlockPos> generateInteriorSpawnPositions(BlockPos origin, TerritoryTier tier) {
        int midSize = (tier.getMinSize() + tier.getMaxSize()) / 2;
        int halfSize = midSize / 2;
        int innerHalf = halfSize - 1;
        List<BlockPos> positions = new ArrayList<>();
        if (innerHalf <= 0) {
            positions.add(origin.above(1));
        } else {
            for (int dx = -innerHalf; dx <= innerHalf; dx += 2) {
                for (int dz = -innerHalf; dz <= innerHalf; dz += 2) {
                    positions.add(origin.offset(dx, 1, dz));
                }
            }
        }
        return positions;
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isLoaded(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
