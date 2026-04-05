package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.building.UpgradeableBlock;
import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class BlockHPRegistry {

    private static final Map<Block, Float> BLOCK_HP_MAP = new HashMap<>();

    static {
        register(Blocks.OAK_DOOR, 10);
        register(Blocks.SPRUCE_DOOR, 10);
        register(Blocks.BIRCH_DOOR, 10);
        register(Blocks.JUNGLE_DOOR, 10);
        register(Blocks.ACACIA_DOOR, 10);
        register(Blocks.DARK_OAK_DOOR, 10);
        register(Blocks.MANGROVE_DOOR, 10);
        register(Blocks.CHERRY_DOOR, 10);
        register(Blocks.BAMBOO_DOOR, 10);
        register(Blocks.CRIMSON_DOOR, 15);
        register(Blocks.WARPED_DOOR, 15);

        register(Blocks.OAK_TRAPDOOR, 10);
        register(Blocks.SPRUCE_TRAPDOOR, 10);
        register(Blocks.BIRCH_TRAPDOOR, 10);
        register(Blocks.JUNGLE_TRAPDOOR, 10);
        register(Blocks.ACACIA_TRAPDOOR, 10);
        register(Blocks.DARK_OAK_TRAPDOOR, 10);
        register(Blocks.MANGROVE_TRAPDOOR, 10);
        register(Blocks.CHERRY_TRAPDOOR, 10);
        register(Blocks.BAMBOO_TRAPDOOR, 10);
        register(Blocks.CRIMSON_TRAPDOOR, 15);
        register(Blocks.WARPED_TRAPDOOR, 15);
        register(Blocks.IRON_TRAPDOOR, 200);

        register(Blocks.IRON_DOOR, 200);

        register(Blocks.GLASS, 3);
        register(Blocks.GLASS_PANE, 3);
        register(Blocks.TINTED_GLASS, 5);

        register(Blocks.OAK_FENCE, 10);
        register(Blocks.SPRUCE_FENCE, 10);
        register(Blocks.BIRCH_FENCE, 10);
        register(Blocks.JUNGLE_FENCE, 10);
        register(Blocks.ACACIA_FENCE, 10);
        register(Blocks.DARK_OAK_FENCE, 10);
        register(Blocks.MANGROVE_FENCE, 10);
        register(Blocks.CHERRY_FENCE, 10);
        register(Blocks.BAMBOO_FENCE, 10);
        register(Blocks.CRIMSON_FENCE, 15);
        register(Blocks.WARPED_FENCE, 15);
        register(Blocks.OAK_FENCE_GATE, 10);
        register(Blocks.SPRUCE_FENCE_GATE, 10);
        register(Blocks.BIRCH_FENCE_GATE, 10);
        register(Blocks.JUNGLE_FENCE_GATE, 10);
        register(Blocks.ACACIA_FENCE_GATE, 10);
        register(Blocks.DARK_OAK_FENCE_GATE, 10);
        register(Blocks.MANGROVE_FENCE_GATE, 10);
        register(Blocks.CHERRY_FENCE_GATE, 10);
        register(Blocks.BAMBOO_FENCE_GATE, 10);
        register(Blocks.CRIMSON_FENCE_GATE, 15);
        register(Blocks.WARPED_FENCE_GATE, 15);

        register(Blocks.OAK_PLANKS, 10);
        register(Blocks.SPRUCE_PLANKS, 10);
        register(Blocks.BIRCH_PLANKS, 10);
        register(Blocks.JUNGLE_PLANKS, 10);
        register(Blocks.ACACIA_PLANKS, 10);
        register(Blocks.DARK_OAK_PLANKS, 10);
        register(Blocks.MANGROVE_PLANKS, 10);
        register(Blocks.CHERRY_PLANKS, 10);
        register(Blocks.BAMBOO_PLANKS, 10);
        register(Blocks.CRIMSON_PLANKS, 15);
        register(Blocks.WARPED_PLANKS, 15);

        register(Blocks.OAK_LOG, 15);
        register(Blocks.SPRUCE_LOG, 15);
        register(Blocks.BIRCH_LOG, 15);
        register(Blocks.JUNGLE_LOG, 15);
        register(Blocks.ACACIA_LOG, 15);
        register(Blocks.DARK_OAK_LOG, 15);
        register(Blocks.MANGROVE_LOG, 15);
        register(Blocks.CHERRY_LOG, 15);

        register(Blocks.COBBLESTONE, 50);
        register(Blocks.COBBLESTONE_WALL, 50);
        register(Blocks.COBBLESTONE_SLAB, 50);
        register(Blocks.COBBLESTONE_STAIRS, 50);
        register(Blocks.MOSSY_COBBLESTONE, 50);
        register(Blocks.STONE, 30);
        register(Blocks.STONE_BRICKS, 40);
        register(Blocks.MOSSY_STONE_BRICKS, 40);
        register(Blocks.CRACKED_STONE_BRICKS, 35);
        register(Blocks.DEEPSLATE, 60);
        register(Blocks.DEEPSLATE_BRICKS, 70);
        register(Blocks.COBBLED_DEEPSLATE, 60);

        register(Blocks.BRICKS, 40);
        register(Blocks.SANDSTONE, 25);
        register(Blocks.RED_SANDSTONE, 25);

        register(Blocks.IRON_BLOCK, 200);
        register(Blocks.IRON_BARS, 100);

        register(Blocks.OBSIDIAN, 500);
        register(Blocks.CRYING_OBSIDIAN, 500);
        register(Blocks.REINFORCED_DEEPSLATE, 1000);

        register(Blocks.BEDROCK, -1);
    }

    private BlockHPRegistry() {}

    private static void register(Block block, float hp) {
        BLOCK_HP_MAP.put(block, hp);
    }

    public static float getBlockHP(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof UpgradeableBlock) {
            int tier = state.getValue(UpgradeableBlock.TIER);
            float mult = ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
            return UpgradeableBlock.getHPForTier(tier) * mult;
        }

        Float hp = BLOCK_HP_MAP.get(block);
        if (hp != null) {
            if (hp < 0) return -1;
            float mult = ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
            return hp * mult;
        }

        if (state.is(BlockTags.DOORS) || block instanceof DoorBlock) return 10 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.TRAPDOORS) || block instanceof TrapDoorBlock) return 10 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.FENCE_GATES)) return 10 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.FENCES)) return 10 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.WOODEN_STAIRS)) return 10 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.PLANKS)) return 10 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.LOGS)) return 15 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.WALLS)) return 50 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();
        if (state.is(BlockTags.IMPERMEABLE)) return 3 * ZombieConfig.INSTANCE.blockHPMultiplier.get().floatValue();

        return 0;
    }

    public static boolean isBreakable(BlockState state) {
        if (state.isAir()) return false;
        float hp = getBlockHP(state);
        return hp > 0;
    }
}
