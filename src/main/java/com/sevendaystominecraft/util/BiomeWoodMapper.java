package com.sevendaystominecraft.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BiomeWoodMapper {

    public static class WoodSet {
        public final Block planks;
        public final Block log;
        public final Block slab;
        public final Block stairs;
        public final Block fence;
        public final Block fenceGate;
        public final Block door;
        public final Block trapdoor;
        public final Block pressurePlate;
        public final Block button;

        public WoodSet(Block planks, Block log, Block slab, Block stairs,
                       Block fence, Block fenceGate, Block door, Block trapdoor,
                       Block pressurePlate, Block button) {
            this.planks = planks;
            this.log = log;
            this.slab = slab;
            this.stairs = stairs;
            this.fence = fence;
            this.fenceGate = fenceGate;
            this.door = door;
            this.trapdoor = trapdoor;
            this.pressurePlate = pressurePlate;
            this.button = button;
        }
    }

    public static WoodSet getWoodForBiome(Holder<Biome> biome) {
        if (biome.is(Biomes.JUNGLE) || biome.is(Biomes.SPARSE_JUNGLE) || biome.is(Biomes.BAMBOO_JUNGLE)) {
            return new WoodSet(Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, Blocks.JUNGLE_SLAB,
                    Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE,
                    Blocks.JUNGLE_DOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.JUNGLE_BUTTON);
        }
        if (biome.is(Biomes.TAIGA) || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA) || biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA) || biome.is(Biomes.SNOWY_TAIGA)) {
            return new WoodSet(Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.SPRUCE_SLAB,
                    Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE,
                    Blocks.SPRUCE_DOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.SPRUCE_BUTTON);
        }
        if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
            return new WoodSet(Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, Blocks.BIRCH_SLAB,
                    Blocks.BIRCH_STAIRS, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE,
                    Blocks.BIRCH_DOOR, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_PRESSURE_PLATE, Blocks.BIRCH_BUTTON);
        }
        if (biome.is(Biomes.SAVANNA) || biome.is(Biomes.SAVANNA_PLATEAU) || biome.is(Biomes.WINDSWEPT_SAVANNA)) {
            return new WoodSet(Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, Blocks.ACACIA_SLAB,
                    Blocks.ACACIA_STAIRS, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE,
                    Blocks.ACACIA_DOOR, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_PRESSURE_PLATE, Blocks.ACACIA_BUTTON);
        }
        if (biome.is(Biomes.MANGROVE_SWAMP)) {
            return new WoodSet(Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_LOG, Blocks.MANGROVE_SLAB,
                    Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE,
                    Blocks.MANGROVE_DOOR, Blocks.MANGROVE_TRAPDOOR, Blocks.MANGROVE_PRESSURE_PLATE, Blocks.MANGROVE_BUTTON);
        }
        if (biome.is(Biomes.CHERRY_GROVE)) {
            return new WoodSet(Blocks.CHERRY_PLANKS, Blocks.CHERRY_LOG, Blocks.CHERRY_SLAB,
                    Blocks.CHERRY_STAIRS, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE,
                    Blocks.CHERRY_DOOR, Blocks.CHERRY_TRAPDOOR, Blocks.CHERRY_PRESSURE_PLATE, Blocks.CHERRY_BUTTON);
        }
        if (biome.is(Biomes.PALE_GARDEN)) {
            return new WoodSet(Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_SLAB,
                    Blocks.PALE_OAK_STAIRS, Blocks.PALE_OAK_FENCE, Blocks.PALE_OAK_FENCE_GATE,
                    Blocks.PALE_OAK_DOOR, Blocks.PALE_OAK_TRAPDOOR, Blocks.PALE_OAK_PRESSURE_PLATE, Blocks.PALE_OAK_BUTTON);
        }
        if (biome.is(Biomes.DARK_FOREST)) {
            return new WoodSet(Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SLAB,
                    Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE,
                    Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.DARK_OAK_BUTTON);
        }

        // Default to Oak
        return new WoodSet(Blocks.OAK_PLANKS, Blocks.OAK_LOG, Blocks.OAK_SLAB,
                Blocks.OAK_STAIRS, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE,
                Blocks.OAK_DOOR, Blocks.OAK_TRAPDOOR, Blocks.OAK_PRESSURE_PLATE, Blocks.OAK_BUTTON);
    }
}
