package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.loot.LootContainerBlock;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.block.loot.LootContainerType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NBTTemplateLoader {

    private static final Map<VillageBuildingType, List<ResourceLocation>> TEMPLATE_CACHE = new HashMap<>();
    private static boolean initialized = false;

    public static void init(ServerLevel level) {
        if (initialized) return;
        initialized = true;
        TEMPLATE_CACHE.clear();

        StructureTemplateManager templateManager = level.getStructureManager();

        for (VillageBuildingType type : VillageBuildingType.values()) {
            List<ResourceLocation> templates = new ArrayList<>();
            String baseName = type.name().toLowerCase();

            for (int i = 1; i <= 10; i++) {
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                        "bzhs", "village/" + baseName + "_" + i);
                Optional<StructureTemplate> template = templateManager.get(loc);
                if (template.isPresent()) {
                    templates.add(loc);
                    SevenDaysToMinecraft.LOGGER.info("[BZHS Village] Found NBT template: {}", loc);
                }
            }

            if (!templates.isEmpty()) {
                TEMPLATE_CACHE.put(type, templates);
                SevenDaysToMinecraft.LOGGER.info("[BZHS Village] Loaded {} template(s) for building type {}",
                        templates.size(), type.name());
            }
        }

        SevenDaysToMinecraft.LOGGER.info("[BZHS Village] NBT template scan complete. {} building types have templates.",
                TEMPLATE_CACHE.size());
    }

    public static boolean hasTemplate(VillageBuildingType type) {
        return TEMPLATE_CACHE.containsKey(type) && !TEMPLATE_CACHE.get(type).isEmpty();
    }

    public static ResourceLocation chooseTemplate(VillageBuildingType buildingType, RandomSource random) {
        if (!hasTemplate(buildingType)) return null;
        List<ResourceLocation> templates = TEMPLATE_CACHE.get(buildingType);
        return templates.get(random.nextInt(templates.size()));
    }

    public static int[] getTemplateSize(ServerLevel level, ResourceLocation templateId) {
        if (templateId == null) return null;

        try {
            StructureTemplateManager templateManager = level.getStructureManager();
            Optional<StructureTemplate> templateOpt = templateManager.get(templateId);
            if (templateOpt.isEmpty()) return null;

            StructureTemplate template = templateOpt.get();
            var size = template.getSize();
            return new int[]{size.getX(), size.getZ()};
        } catch (Exception e) {
            return null;
        }
    }

    public static VillageBuildingBuilder.BuildingResult placeTemplate(ServerLevel level, BlockPos origin,
                                                                       VillageBuildingType buildingType,
                                                                       TerritoryTier tier, RandomSource random) {
        return placeTemplate(level, origin, buildingType, tier, random, null);
    }

    public static VillageBuildingBuilder.BuildingResult placeTemplate(ServerLevel level, BlockPos origin,
                                                                       VillageBuildingType buildingType,
                                                                       TerritoryTier tier, RandomSource random,
                                                                       ResourceLocation chosenTemplate) {
        if (!hasTemplate(buildingType)) return null;

        ResourceLocation chosen = chosenTemplate != null ? chosenTemplate :
                chooseTemplate(buildingType, random);

        try {
            StructureTemplateManager templateManager = level.getStructureManager();
            Optional<StructureTemplate> templateOpt = templateManager.get(chosen);
            if (templateOpt.isEmpty()) return null;

            StructureTemplate template = templateOpt.get();
            var size = template.getSize();

            int centerX = origin.getX() + size.getX() / 2;
            int centerZ = origin.getZ() + size.getZ() / 2;
            int halfX = size.getX() / 2;
            int halfZ = size.getZ() / 2;

            int slopeVariance = TerrainValidator.getSlopeVariance(level, centerX, centerZ, halfX, halfZ);
            if (slopeVariance > TerrainValidator.MAX_SLOPE_VARIANCE) return null;

            int surfaceY = TerrainValidator.findSolidGroundY(level, centerX, centerZ);
            if (TerrainValidator.isInRavine(level, centerX, centerZ, surfaceY)) return null;
            if (TerrainValidator.isInLocalDepression(level, centerX, centerZ, surfaceY)) return null;

            int minY = TerrainValidator.getMinSurfaceY(level, centerX, centerZ, halfX, halfZ);
            if (minY <= 0) minY = surfaceY;

            BlockPos placePos = new BlockPos(origin.getX(), minY, origin.getZ());
            BlockPos templateCenter = new BlockPos(centerX, minY, centerZ);

            TerrainValidator.clearVegetation(level, templateCenter, halfX, halfZ, size.getY() + 5);

            StructurePlaceSettings settings = new StructurePlaceSettings();
            template.placeInWorld(level, placePos, placePos, settings, random, Block.UPDATE_CLIENTS);

            TerrainValidator.fillFoundationColumns(level, templateCenter, halfX, halfZ, Blocks.STONE, false);
            List<BlockPos> zombieSpawns = new ArrayList<>();
            List<BlockPos> lootPositions = new ArrayList<>();
            List<LootContainerType> lootTypes = new ArrayList<>();

            com.sevendaystominecraft.util.BiomeWoodMapper.WoodSet woodSet =
                    com.sevendaystominecraft.util.BiomeWoodMapper.getWoodForBiome(level.getBiome(templateCenter));

            for (int dx = 0; dx < size.getX(); dx++) {
                for (int dy = 0; dy < size.getY(); dy++) {
                    for (int dz = 0; dz < size.getZ(); dz++) {
                        BlockPos checkPos = placePos.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(checkPos);

                        // Block Replacement Logic (Wood swapping)
                        if (buildingType == VillageBuildingType.MANSION || buildingType == VillageBuildingType.RESIDENTIAL || buildingType == VillageBuildingType.FARM) {
                            if (state.is(Blocks.DARK_OAK_PLANKS) || state.is(Blocks.OAK_PLANKS) || state.is(Blocks.SPRUCE_PLANKS)) {
                                level.setBlock(checkPos, woodSet.planks.defaultBlockState(), Block.UPDATE_CLIENTS);
                            } else if (state.is(Blocks.DARK_OAK_LOG) || state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG)) {
                                level.setBlock(checkPos, woodSet.log.defaultBlockState(), Block.UPDATE_CLIENTS);
                            } else if (state.is(Blocks.DARK_OAK_SLAB) || state.is(Blocks.OAK_SLAB) || state.is(Blocks.SPRUCE_SLAB)) {
                                copyBlockWithProperties(level, checkPos, state, woodSet.slab);
                            } else if (state.is(Blocks.DARK_OAK_STAIRS) || state.is(Blocks.OAK_STAIRS) || state.is(Blocks.SPRUCE_STAIRS)) {
                                copyBlockWithProperties(level, checkPos, state, woodSet.stairs);
                            } else if (state.is(Blocks.DARK_OAK_FENCE) || state.is(Blocks.OAK_FENCE) || state.is(Blocks.SPRUCE_FENCE)) {
                                copyBlockWithProperties(level, checkPos, state, woodSet.fence);
                            } else if (state.is(Blocks.DARK_OAK_FENCE_GATE) || state.is(Blocks.OAK_FENCE_GATE) || state.is(Blocks.SPRUCE_FENCE_GATE)) {
                                copyBlockWithProperties(level, checkPos, state, woodSet.fenceGate);
                            } else if (state.is(Blocks.DARK_OAK_DOOR) || state.is(Blocks.OAK_DOOR) || state.is(Blocks.SPRUCE_DOOR)) {
                                copyBlockWithProperties(level, checkPos, state, woodSet.door);
                            } else if (state.is(Blocks.DARK_OAK_TRAPDOOR) || state.is(Blocks.OAK_TRAPDOOR) || state.is(Blocks.SPRUCE_TRAPDOOR)) {
                                copyBlockWithProperties(level, checkPos, state, woodSet.trapdoor);
                            }
                            state = level.getBlockState(checkPos); // Update state after potential replacement
                        }

                        if (state.getBlock() instanceof LootContainerBlock lcb) {
                            lootPositions.add(checkPos);
                            lootTypes.add(lcb.getContainerType());
                            if (level.getBlockEntity(checkPos) instanceof LootContainerBlockEntity be) {
                                be.setTerritoryTier(tier.getTier());
                            }
                        }

                        if (dy >= 1 && dy <= 2 && state.isAir()) {
                            BlockState below = level.getBlockState(checkPos.below());
                            if (below.isSolid()) {
                                BlockState above = level.getBlockState(checkPos.above());
                                if (above.isAir()) {
                                    zombieSpawns.add(checkPos);
                                }
                            }
                        }
                    }
                }
            }

            SevenDaysToMinecraft.LOGGER.info("[BZHS Village] Placed NBT template '{}' at ({}, {}, {}) — {} loot, {} spawns",
                    chosen, placePos.getX(), placePos.getY(), placePos.getZ(),
                    lootPositions.size(), zombieSpawns.size());

            return new VillageBuildingBuilder.BuildingResult(templateCenter, zombieSpawns, lootPositions, lootTypes,
                    size.getX(), size.getZ());

        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.error("[BZHS Village] Failed to load NBT template '{}': {}",
                    chosen, e.getMessage());
            return null;
        }
    }

    /**
     * Replaces a block with a new type while preserving all compatible state properties
     * (e.g. direction, half, waterlogged, shape for stairs/slabs/doors).
     */
    private static void copyBlockWithProperties(ServerLevel level, BlockPos pos, BlockState oldState, Block newBlock) {
        BlockState newState = newBlock.defaultBlockState();
        for (net.minecraft.world.level.block.state.properties.Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) {
                newState = copyProperty(oldState, newState, property);
            }
        }
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to,
                                                                      net.minecraft.world.level.block.state.properties.Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
