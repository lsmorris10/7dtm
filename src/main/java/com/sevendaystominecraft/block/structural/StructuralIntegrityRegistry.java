package com.sevendaystominecraft.block.structural;

import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.building.UpgradeableBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class StructuralIntegrityRegistry {

    private static final Map<Block, SIMaterial> BLOCK_MATERIAL_MAP = new HashMap<>();
    private static final Set<Block> GROUND_BLOCKS = new HashSet<>();
    private static boolean modBlocksRegistered = false;

    public enum SIMaterial {
        WOOD_FRAME(10, 1, 3),
        COBBLESTONE(30, 3, 5),
        CONCRETE(60, 5, 8),
        REINFORCED_CONCRETE(80, 6, 10),
        STEEL(100, 8, 15);

        public final int maxSupport;
        public final int weight;
        public final int horizontalSpan;

        SIMaterial(int maxSupport, int weight, int horizontalSpan) {
            this.maxSupport = maxSupport;
            this.weight = weight;
            this.horizontalSpan = horizontalSpan;
        }
    }

    static {
        registerVanillaBlocks();
        registerGroundBlocks();
    }

    private StructuralIntegrityRegistry() {}

    private static void registerVanillaBlocks() {
        register(Blocks.OAK_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_PLANKS, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_PLANKS, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_OAK_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_SPRUCE_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_BIRCH_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_JUNGLE_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_ACACIA_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_DARK_OAK_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_MANGROVE_LOG, SIMaterial.WOOD_FRAME);
        register(Blocks.STRIPPED_CHERRY_LOG, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_WOOD, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_WOOD, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_SLAB, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_SLAB, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_STAIRS, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_STAIRS, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_DOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_DOOR, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_FENCE, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_FENCE, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_FENCE_GATE, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_FENCE_GATE, SIMaterial.WOOD_FRAME);

        register(Blocks.OAK_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.SPRUCE_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.BIRCH_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.JUNGLE_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.ACACIA_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.DARK_OAK_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.MANGROVE_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.CHERRY_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.BAMBOO_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.CRIMSON_TRAPDOOR, SIMaterial.WOOD_FRAME);
        register(Blocks.WARPED_TRAPDOOR, SIMaterial.WOOD_FRAME);

        register(Blocks.COBBLESTONE, SIMaterial.COBBLESTONE);
        register(Blocks.COBBLESTONE_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.COBBLESTONE_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.COBBLESTONE_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_COBBLESTONE, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_COBBLESTONE_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_COBBLESTONE_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_COBBLESTONE_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.STONE, SIMaterial.COBBLESTONE);
        register(Blocks.STONE_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.STONE_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.SMOOTH_STONE, SIMaterial.COBBLESTONE);
        register(Blocks.SMOOTH_STONE_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.STONE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.STONE_BRICK_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.STONE_BRICK_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.STONE_BRICK_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_STONE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_STONE_BRICK_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_STONE_BRICK_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.MOSSY_STONE_BRICK_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.CRACKED_STONE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.CHISELED_STONE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.BRICK_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.BRICK_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.BRICK_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.SANDSTONE, SIMaterial.COBBLESTONE);
        register(Blocks.SANDSTONE_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.SANDSTONE_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.SANDSTONE_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.RED_SANDSTONE, SIMaterial.COBBLESTONE);
        register(Blocks.RED_SANDSTONE_SLAB, SIMaterial.COBBLESTONE);
        register(Blocks.RED_SANDSTONE_STAIRS, SIMaterial.COBBLESTONE);
        register(Blocks.RED_SANDSTONE_WALL, SIMaterial.COBBLESTONE);
        register(Blocks.PRISMARINE, SIMaterial.COBBLESTONE);
        register(Blocks.PRISMARINE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.DARK_PRISMARINE, SIMaterial.COBBLESTONE);
        register(Blocks.ANDESITE, SIMaterial.COBBLESTONE);
        register(Blocks.POLISHED_ANDESITE, SIMaterial.COBBLESTONE);
        register(Blocks.DIORITE, SIMaterial.COBBLESTONE);
        register(Blocks.POLISHED_DIORITE, SIMaterial.COBBLESTONE);
        register(Blocks.GRANITE, SIMaterial.COBBLESTONE);
        register(Blocks.POLISHED_GRANITE, SIMaterial.COBBLESTONE);
        register(Blocks.TUFF, SIMaterial.COBBLESTONE);
        register(Blocks.TUFF_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.BLACKSTONE, SIMaterial.COBBLESTONE);
        register(Blocks.POLISHED_BLACKSTONE, SIMaterial.COBBLESTONE);
        register(Blocks.POLISHED_BLACKSTONE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.NETHER_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.RED_NETHER_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.END_STONE_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.PURPUR_BLOCK, SIMaterial.COBBLESTONE);
        register(Blocks.PURPUR_PILLAR, SIMaterial.COBBLESTONE);
        register(Blocks.QUARTZ_BLOCK, SIMaterial.COBBLESTONE);
        register(Blocks.QUARTZ_BRICKS, SIMaterial.COBBLESTONE);
        register(Blocks.QUARTZ_PILLAR, SIMaterial.COBBLESTONE);
        register(Blocks.MUD_BRICKS, SIMaterial.COBBLESTONE);

        register(Blocks.DEEPSLATE, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_BRICKS, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_BRICK_SLAB, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_BRICK_STAIRS, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_BRICK_WALL, SIMaterial.CONCRETE);
        register(Blocks.COBBLED_DEEPSLATE, SIMaterial.CONCRETE);
        register(Blocks.COBBLED_DEEPSLATE_SLAB, SIMaterial.CONCRETE);
        register(Blocks.COBBLED_DEEPSLATE_STAIRS, SIMaterial.CONCRETE);
        register(Blocks.COBBLED_DEEPSLATE_WALL, SIMaterial.CONCRETE);
        register(Blocks.POLISHED_DEEPSLATE, SIMaterial.CONCRETE);
        register(Blocks.POLISHED_DEEPSLATE_SLAB, SIMaterial.CONCRETE);
        register(Blocks.POLISHED_DEEPSLATE_STAIRS, SIMaterial.CONCRETE);
        register(Blocks.POLISHED_DEEPSLATE_WALL, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_TILES, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_TILE_SLAB, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_TILE_STAIRS, SIMaterial.CONCRETE);
        register(Blocks.DEEPSLATE_TILE_WALL, SIMaterial.CONCRETE);
        register(Blocks.CHISELED_DEEPSLATE, SIMaterial.CONCRETE);

        register(Blocks.IRON_BLOCK, SIMaterial.STEEL);
        register(Blocks.IRON_BARS, SIMaterial.STEEL);
        register(Blocks.IRON_DOOR, SIMaterial.STEEL);
        register(Blocks.IRON_TRAPDOOR, SIMaterial.STEEL);
        register(Blocks.CHAIN, SIMaterial.STEEL);
        register(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, SIMaterial.STEEL);

        register(Blocks.OBSIDIAN, SIMaterial.STEEL);
        register(Blocks.CRYING_OBSIDIAN, SIMaterial.STEEL);
        register(Blocks.REINFORCED_DEEPSLATE, SIMaterial.STEEL);
        register(Blocks.NETHERITE_BLOCK, SIMaterial.STEEL);

        register(Blocks.GLASS, SIMaterial.WOOD_FRAME);
        register(Blocks.GLASS_PANE, SIMaterial.WOOD_FRAME);
        register(Blocks.TINTED_GLASS, SIMaterial.WOOD_FRAME);
    }

    private static void registerGroundBlocks() {
        GROUND_BLOCKS.add(Blocks.BEDROCK);
        GROUND_BLOCKS.add(Blocks.BARRIER);

        GROUND_BLOCKS.add(Blocks.DIRT);
        GROUND_BLOCKS.add(Blocks.COARSE_DIRT);
        GROUND_BLOCKS.add(Blocks.ROOTED_DIRT);
        GROUND_BLOCKS.add(Blocks.GRASS_BLOCK);
        GROUND_BLOCKS.add(Blocks.PODZOL);
        GROUND_BLOCKS.add(Blocks.MYCELIUM);
        GROUND_BLOCKS.add(Blocks.MUD);
        GROUND_BLOCKS.add(Blocks.PACKED_MUD);
        GROUND_BLOCKS.add(Blocks.MUDDY_MANGROVE_ROOTS);
        GROUND_BLOCKS.add(Blocks.DIRT_PATH);

        GROUND_BLOCKS.add(Blocks.SAND);
        GROUND_BLOCKS.add(Blocks.RED_SAND);
        GROUND_BLOCKS.add(Blocks.GRAVEL);
        GROUND_BLOCKS.add(Blocks.CLAY);
        GROUND_BLOCKS.add(Blocks.SOUL_SAND);
        GROUND_BLOCKS.add(Blocks.SOUL_SOIL);

        GROUND_BLOCKS.add(Blocks.STONE);
        GROUND_BLOCKS.add(Blocks.GRANITE);
        GROUND_BLOCKS.add(Blocks.DIORITE);
        GROUND_BLOCKS.add(Blocks.ANDESITE);
        GROUND_BLOCKS.add(Blocks.TUFF);
        GROUND_BLOCKS.add(Blocks.CALCITE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE);
        GROUND_BLOCKS.add(Blocks.COBBLED_DEEPSLATE);

        GROUND_BLOCKS.add(Blocks.NETHERRACK);
        GROUND_BLOCKS.add(Blocks.BASALT);
        GROUND_BLOCKS.add(Blocks.SMOOTH_BASALT);
        GROUND_BLOCKS.add(Blocks.BLACKSTONE);
        GROUND_BLOCKS.add(Blocks.END_STONE);

        GROUND_BLOCKS.add(Blocks.TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.WHITE_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.ORANGE_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.MAGENTA_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.LIGHT_BLUE_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.YELLOW_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.LIME_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.PINK_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.GRAY_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.LIGHT_GRAY_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.CYAN_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.PURPLE_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.BLUE_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.BROWN_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.GREEN_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.RED_TERRACOTTA);
        GROUND_BLOCKS.add(Blocks.BLACK_TERRACOTTA);

        GROUND_BLOCKS.add(Blocks.SANDSTONE);
        GROUND_BLOCKS.add(Blocks.RED_SANDSTONE);

        GROUND_BLOCKS.add(Blocks.COAL_ORE);
        GROUND_BLOCKS.add(Blocks.IRON_ORE);
        GROUND_BLOCKS.add(Blocks.GOLD_ORE);
        GROUND_BLOCKS.add(Blocks.DIAMOND_ORE);
        GROUND_BLOCKS.add(Blocks.LAPIS_ORE);
        GROUND_BLOCKS.add(Blocks.REDSTONE_ORE);
        GROUND_BLOCKS.add(Blocks.EMERALD_ORE);
        GROUND_BLOCKS.add(Blocks.COPPER_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
        GROUND_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);

        GROUND_BLOCKS.add(Blocks.OBSIDIAN);
        GROUND_BLOCKS.add(Blocks.CRYING_OBSIDIAN);
    }

    public static void registerModBlocks() {
        if (modBlocksRegistered) return;
        modBlocksRegistered = true;

        register(ModBlocks.WOOD_SPIKES.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.IRON_SPIKES.get(), SIMaterial.STEEL);
        register(ModBlocks.BLADE_TRAP.get(), SIMaterial.STEEL);
        register(ModBlocks.ELECTRIC_FENCE_POST.get(), SIMaterial.STEEL);
        register(ModBlocks.LAND_CLAIM_BLOCK.get(), SIMaterial.STEEL);

        register(ModBlocks.ASPHALT_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.CRACKED_ASPHALT_BLOCK.get(), SIMaterial.COBBLESTONE);

        register(ModBlocks.GRILL_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.WORKBENCH_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.FORGE_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.CEMENT_MIXER_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.CHEMISTRY_STATION_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.ADVANCED_WORKBENCH_BLOCK.get(), SIMaterial.COBBLESTONE);

        register(ModBlocks.BURNT_CAR_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.BROKEN_TRUCK_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.WRECKED_CAMPER_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.VEHICLE_BODY_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.VEHICLE_BODY_CHARRED_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.VEHICLE_WINDOW_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.VEHICLE_WHEEL_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.VEHICLE_ROOF_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.CAMPER_BODY_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.TRUCK_BED_BLOCK.get(), SIMaterial.COBBLESTONE);

        register(ModBlocks.DEW_COLLECTOR_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.VENDING_MACHINE_BLOCK.get(), SIMaterial.COBBLESTONE);

        register(ModBlocks.TRASH_PILE_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.CARDBOARD_BOX_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.GUN_SAFE_BLOCK.get(), SIMaterial.STEEL);
        register(ModBlocks.MUNITIONS_BOX_BLOCK.get(), SIMaterial.STEEL);
        register(ModBlocks.SUPPLY_CRATE_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.KITCHEN_CABINET_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.MEDICINE_CABINET_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.BOOKSHELF_CONTAINER_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.TOOL_CRATE_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.FUEL_CACHE_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.MAILBOX_BLOCK.get(), SIMaterial.COBBLESTONE);
        register(ModBlocks.FARM_CRATE_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.ASH_BLOCK.get(), SIMaterial.WOOD_FRAME);
        register(ModBlocks.FARM_PLOT_BLOCK.get(), SIMaterial.WOOD_FRAME);
    }

    public static void register(Block block, SIMaterial material) {
        BLOCK_MATERIAL_MAP.put(block, material);
    }

    public static void registerGround(Block block) {
        GROUND_BLOCKS.add(block);
    }

    public static SIMaterial getMaterial(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof UpgradeableBlock) {
            int tier = state.getValue(UpgradeableBlock.TIER);
            return getMaterialForTier(tier);
        }

        SIMaterial material = BLOCK_MATERIAL_MAP.get(block);
        if (material != null) {
            return material;
        }

        if (state.is(BlockTags.PLANKS) || state.is(BlockTags.LOGS)
                || state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.DOORS) || state.is(BlockTags.FENCES)
                || state.is(BlockTags.FENCE_GATES) || state.is(BlockTags.TRAPDOORS)) {
            return SIMaterial.WOOD_FRAME;
        }

        if (state.is(BlockTags.WALLS) || state.is(BlockTags.STONE_BRICKS)) {
            return SIMaterial.COBBLESTONE;
        }

        return null;
    }

    public static SIMaterial getMaterialForTier(int tier) {
        return switch (tier) {
            case 0 -> SIMaterial.WOOD_FRAME;
            case 1 -> SIMaterial.WOOD_FRAME;
            case 2 -> SIMaterial.COBBLESTONE;
            case 3 -> SIMaterial.CONCRETE;
            case 4 -> SIMaterial.REINFORCED_CONCRETE;
            case 5 -> SIMaterial.STEEL;
            default -> SIMaterial.WOOD_FRAME;
        };
    }

    public static boolean isStructuralBlock(BlockState state) {
        return getMaterial(state) != null;
    }

    public static boolean isGroundBlock(BlockState state) {
        return GROUND_BLOCKS.contains(state.getBlock());
    }
}
