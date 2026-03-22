package com.sevendaystominecraft.block;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.loot.LootContainerBlock;
import com.sevendaystominecraft.block.loot.LootContainerType;
import com.sevendaystominecraft.block.loot.VendingMachineBlock;
import com.sevendaystominecraft.block.vehicle.VehicleWreckageBlock;
import com.sevendaystominecraft.block.workstation.WorkstationBlock;
import com.sevendaystominecraft.block.workstation.WorkstationType;

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

    private static Supplier<Block> registerWithItem(String name, Supplier<Block> blockSupplier) {
        Supplier<Block> block = BLOCKS.register(name, blockSupplier);
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().setId(itemKey(name))));
        return block;
    }
}
