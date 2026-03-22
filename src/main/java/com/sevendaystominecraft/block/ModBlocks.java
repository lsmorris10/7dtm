package com.sevendaystominecraft.block;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.building.BladeTrapBlock;
import com.sevendaystominecraft.block.building.ElectricFencePostBlock;
import com.sevendaystominecraft.block.building.IronSpikesBlock;
import com.sevendaystominecraft.block.building.LandClaimBlock;
import com.sevendaystominecraft.block.building.UpgradeableBlock;
import com.sevendaystominecraft.block.building.WoodSpikesBlock;
import com.sevendaystominecraft.block.farming.CropBlock;
import com.sevendaystominecraft.block.farming.DewCollectorBlock;
import com.sevendaystominecraft.block.farming.FarmPlotBlock;
import com.sevendaystominecraft.block.loot.LootContainerBlock;
import com.sevendaystominecraft.block.loot.LootContainerType;
import com.sevendaystominecraft.block.loot.VendingMachineBlock;
import com.sevendaystominecraft.block.power.BatteryBankBlock;
import com.sevendaystominecraft.block.power.GeneratorBankBlock;
import com.sevendaystominecraft.block.power.SolarPanelBlock;
import com.sevendaystominecraft.block.vehicle.VehicleWreckageBlock;
import com.sevendaystominecraft.block.workstation.WorkstationBlock;
import com.sevendaystominecraft.block.workstation.WorkstationType;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, SevenDaysToMinecraft.MOD_ID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(Registries.ITEM, SevenDaysToMinecraft.MOD_ID);

    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }

    private static BlockBehaviour.Properties workstationProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .strength(3.5f)
                .sound(SoundType.WOOD)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties metalWorkstationProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .strength(5.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties containerProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .strength(2.0f)
                .sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties metalContainerProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .strength(4.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties vehicleWreckageProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .strength(2.5f)
                .sound(SoundType.METAL);
    }

    public static final Supplier<Block> GRILL_BLOCK = registerWithItem("grill",
            () -> new WorkstationBlock(metalWorkstationProps("grill"), WorkstationType.GRILL));

    public static final Supplier<Block> WORKBENCH_BLOCK = registerWithItem("workbench",
            () -> new WorkstationBlock(workstationProps("workbench"), WorkstationType.WORKBENCH));

    public static final Supplier<Block> FORGE_BLOCK = registerWithItem("forge_station",
            () -> new WorkstationBlock(metalWorkstationProps("forge_station"), WorkstationType.FORGE));

    public static final Supplier<Block> CEMENT_MIXER_BLOCK = registerWithItem("cement_mixer",
            () -> new WorkstationBlock(metalWorkstationProps("cement_mixer"), WorkstationType.CEMENT_MIXER));

    public static final Supplier<Block> CHEMISTRY_STATION_BLOCK = registerWithItem("chemistry_station",
            () -> new WorkstationBlock(metalWorkstationProps("chemistry_station"), WorkstationType.CHEMISTRY_STATION));

    public static final Supplier<Block> ADVANCED_WORKBENCH_BLOCK = registerWithItem("advanced_workbench",
            () -> new WorkstationBlock(metalWorkstationProps("advanced_workbench"), WorkstationType.ADVANCED_WORKBENCH));

    public static final Supplier<Block> TRASH_PILE_BLOCK = registerWithItem("trash_pile",
            () -> new LootContainerBlock(containerProps("trash_pile"), LootContainerType.TRASH_PILE));

    public static final Supplier<Block> CARDBOARD_BOX_BLOCK = registerWithItem("cardboard_box",
            () -> new LootContainerBlock(containerProps("cardboard_box"), LootContainerType.CARDBOARD_BOX));

    public static final Supplier<Block> GUN_SAFE_BLOCK = registerWithItem("gun_safe",
            () -> new LootContainerBlock(metalContainerProps("gun_safe"), LootContainerType.GUN_SAFE));

    public static final Supplier<Block> MUNITIONS_BOX_BLOCK = registerWithItem("munitions_box",
            () -> new LootContainerBlock(metalContainerProps("munitions_box"), LootContainerType.MUNITIONS_BOX));

    public static final Supplier<Block> SUPPLY_CRATE_BLOCK = registerWithItem("supply_crate",
            () -> new LootContainerBlock(containerProps("supply_crate"), LootContainerType.SUPPLY_CRATE));

    public static final Supplier<Block> KITCHEN_CABINET_BLOCK = registerWithItem("kitchen_cabinet",
            () -> new LootContainerBlock(containerProps("kitchen_cabinet"), LootContainerType.KITCHEN_CABINET));

    public static final Supplier<Block> MEDICINE_CABINET_BLOCK = registerWithItem("medicine_cabinet",
            () -> new LootContainerBlock(containerProps("medicine_cabinet"), LootContainerType.MEDICINE_CABINET));

    public static final Supplier<Block> BOOKSHELF_CONTAINER_BLOCK = registerWithItem("bookshelf_container",
            () -> new LootContainerBlock(containerProps("bookshelf_container"), LootContainerType.BOOKSHELF));

    public static final Supplier<Block> TOOL_CRATE_BLOCK = registerWithItem("tool_crate",
            () -> new LootContainerBlock(metalContainerProps("tool_crate"), LootContainerType.TOOL_CRATE));

    public static final Supplier<Block> FUEL_CACHE_BLOCK = registerWithItem("fuel_cache",
            () -> new LootContainerBlock(metalContainerProps("fuel_cache"), LootContainerType.FUEL_CACHE));

    public static final Supplier<Block> VENDING_MACHINE_BLOCK = registerWithItem("vending_machine",
            () -> new VendingMachineBlock(metalContainerProps("vending_machine")));

    public static final Supplier<Block> MAILBOX_BLOCK = registerWithItem("mailbox",
            () -> new LootContainerBlock(containerProps("mailbox"), LootContainerType.MAILBOX));

    public static final Supplier<Block> FARM_CRATE_BLOCK = registerWithItem("farm_crate",
            () -> new LootContainerBlock(containerProps("farm_crate"), LootContainerType.FARM_CRATE));

    public static final Supplier<Block> BURNT_CAR_BLOCK = registerWithItem("burnt_car",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("burnt_car")));

    public static final Supplier<Block> BROKEN_TRUCK_BLOCK = registerWithItem("broken_truck",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("broken_truck")));

    public static final Supplier<Block> WRECKED_CAMPER_BLOCK = registerWithItem("wrecked_camper",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("wrecked_camper")));

    public static final Supplier<Block> VEHICLE_BODY_BLOCK = registerWithItem("vehicle_body",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("vehicle_body")));

    public static final Supplier<Block> VEHICLE_BODY_CHARRED_BLOCK = registerWithItem("vehicle_body_charred",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("vehicle_body_charred")));

    public static final Supplier<Block> VEHICLE_WINDOW_BLOCK = registerWithItem("vehicle_window",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("vehicle_window")));

    public static final Supplier<Block> VEHICLE_WHEEL_BLOCK = registerWithItem("vehicle_wheel",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("vehicle_wheel")));

    public static final Supplier<Block> VEHICLE_ROOF_BLOCK = registerWithItem("vehicle_roof",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("vehicle_roof")));

    public static final Supplier<Block> CAMPER_BODY_BLOCK = registerWithItem("camper_body",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("camper_body")));

    public static final Supplier<Block> TRUCK_BED_BLOCK = registerWithItem("truck_bed",
            () -> new VehicleWreckageBlock(vehicleWreckageProps("truck_bed")));

    public static final Supplier<Block> ASH_BLOCK = registerWithItem("ash_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .setId(blockKey("ash_block"))
                    .strength(0.5f)
                    .sound(SoundType.SAND)
                    .mapColor(net.minecraft.world.level.material.MapColor.COLOR_GRAY)));

    public static final Supplier<Block> ASPHALT_BLOCK = registerWithItem("asphalt_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .setId(blockKey("asphalt_block"))
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> CRACKED_ASPHALT_BLOCK = registerWithItem("cracked_asphalt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .setId(blockKey("cracked_asphalt"))
                    .strength(1.2f, 4.0f)
                    .sound(SoundType.STONE)
                    .mapColor(net.minecraft.world.level.material.MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> UPGRADEABLE_FRAME = registerWithItem("upgradeable_frame",
            () -> new UpgradeableBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("upgradeable_frame"))
                    .strength(2.0f, 2.0f)
                    .sound(SoundType.WOOD)));

    public static final Supplier<Block> WOOD_SPIKES = registerWithItem("wood_spikes",
            () -> new WoodSpikesBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("wood_spikes"))
                    .strength(1.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final Supplier<Block> IRON_SPIKES = registerWithItem("iron_spikes",
            () -> new IronSpikesBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("iron_spikes"))
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> BLADE_TRAP = registerWithItem("blade_trap",
            () -> new BladeTrapBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("blade_trap"))
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> ELECTRIC_FENCE_POST = registerWithItem("electric_fence_post",
            () -> new ElectricFencePostBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("electric_fence_post"))
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> LAND_CLAIM_BLOCK = registerWithItem("land_claim_block",
            () -> new LandClaimBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("land_claim_block"))
                    .strength(50.0f, 1200.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7)));

    public static final Supplier<Block> FARM_PLOT_BLOCK = registerWithItem("farm_plot",
            () -> new FarmPlotBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("farm_plot"))
                    .strength(0.6f)
                    .sound(SoundType.GRAVEL)
                    .mapColor(net.minecraft.world.level.material.MapColor.DIRT)));

    private static BlockBehaviour.Properties cropProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .noCollission()
                .randomTicks()
                .strength(0f)
                .sound(SoundType.CROP)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }

    public static final Supplier<Block> CORN_CROP = registerBlock("corn_crop",
            () -> new CropBlock(cropProps("corn_crop"),
                    () -> ModItems.CORN_SEED.get(), () -> ModItems.CORN.get(), 1, 3));

    public static final Supplier<Block> POTATO_CROP = registerBlock("potato_crop",
            () -> new CropBlock(cropProps("potato_crop"),
                    () -> ModItems.POTATO_SEED.get(), () -> ModItems.POTATO_CROP_ITEM.get(), 1, 3));

    public static final Supplier<Block> BLUEBERRY_CROP = registerBlock("blueberry_crop",
            () -> new CropBlock(cropProps("blueberry_crop"),
                    () -> ModItems.BLUEBERRY_SEED.get(), () -> ModItems.BLUEBERRY.get(), 2, 4));

    public static final Supplier<Block> GOLDENROD_CROP = registerBlock("goldenrod_crop",
            () -> new CropBlock(cropProps("goldenrod_crop"),
                    () -> ModItems.GOLDENROD_SEED.get(), () -> ModItems.GOLDENROD.get(), 1, 2));

    public static final Supplier<Block> ALOE_CROP = registerBlock("aloe_crop",
            () -> new CropBlock(cropProps("aloe_crop"),
                    () -> ModItems.ALOE_SEED.get(), () -> ModItems.ALOE.get(), 1, 2));

    public static final Supplier<Block> COFFEE_CROP = registerBlock("coffee_crop",
            () -> new CropBlock(cropProps("coffee_crop"),
                    () -> ModItems.COFFEE_SEED.get(), () -> ModItems.COFFEE_BEANS.get(), 1, 3));

    public static final Supplier<Block> DEW_COLLECTOR_BLOCK = registerWithItem("dew_collector",
            () -> new DewCollectorBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("dew_collector"))
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> GENERATOR_BANK_BLOCK = registerWithItem("generator_bank",
            () -> new GeneratorBankBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("generator_bank"))
                    .strength(5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> BATTERY_BANK_BLOCK = registerWithItem("battery_bank",
            () -> new BatteryBankBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("battery_bank"))
                    .strength(5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> SOLAR_PANEL_BLOCK = registerWithItem("solar_panel",
            () -> new SolarPanelBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("solar_panel"))
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    private static Supplier<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        return BLOCKS.register(name, blockSupplier);
    }

    private static Supplier<Block> registerWithItem(String name, Supplier<Block> blockSupplier) {
        Supplier<Block> block = BLOCKS.register(name, blockSupplier);
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().setId(itemKey(name))));
        return block;
    }
}
