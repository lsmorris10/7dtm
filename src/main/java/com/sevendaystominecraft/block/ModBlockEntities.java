package com.sevendaystominecraft.block;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.farming.DewCollectorBlockEntity;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.block.workstation.WorkstationBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<BlockEntityType<WorkstationBlockEntity>> WORKSTATION_BE =
            BLOCK_ENTITY_TYPES.register("workstation", () ->
                    new BlockEntityType<>(WorkstationBlockEntity::new,
                            ModBlocks.GRILL_BLOCK.get(),
                            ModBlocks.WORKBENCH_BLOCK.get(),
                            ModBlocks.FORGE_BLOCK.get(),
                            ModBlocks.CEMENT_MIXER_BLOCK.get(),
                            ModBlocks.CHEMISTRY_STATION_BLOCK.get(),
                            ModBlocks.ADVANCED_WORKBENCH_BLOCK.get()
                    ));

    public static final Supplier<BlockEntityType<LootContainerBlockEntity>> LOOT_CONTAINER_BE =
            BLOCK_ENTITY_TYPES.register("loot_container", () ->
                    new BlockEntityType<>(LootContainerBlockEntity::new,
                            ModBlocks.TRASH_PILE_BLOCK.get(),
                            ModBlocks.CARDBOARD_BOX_BLOCK.get(),
                            ModBlocks.GUN_SAFE_BLOCK.get(),
                            ModBlocks.MUNITIONS_BOX_BLOCK.get(),
                            ModBlocks.SUPPLY_CRATE_BLOCK.get(),
                            ModBlocks.KITCHEN_CABINET_BLOCK.get(),
                            ModBlocks.MEDICINE_CABINET_BLOCK.get(),
                            ModBlocks.BOOKSHELF_CONTAINER_BLOCK.get(),
                            ModBlocks.TOOL_CRATE_BLOCK.get(),
                            ModBlocks.FUEL_CACHE_BLOCK.get(),
                            ModBlocks.VENDING_MACHINE_BLOCK.get(),
                            ModBlocks.MAILBOX_BLOCK.get(),
                            ModBlocks.FARM_CRATE_BLOCK.get()
                    ));

    public static final Supplier<BlockEntityType<DewCollectorBlockEntity>> DEW_COLLECTOR_BE =
            BLOCK_ENTITY_TYPES.register("dew_collector", () ->
                    new BlockEntityType<>(DewCollectorBlockEntity::new,
                            ModBlocks.DEW_COLLECTOR_BLOCK.get()
                    ));
}
