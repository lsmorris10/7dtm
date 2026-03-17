package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SevenDaysToMinecraft.MOD_ID);

    private static ResourceKey<CreativeModeTab> key(String name) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }

    public static final Supplier<CreativeModeTab> MATERIALS_TAB = CREATIVE_TABS.register("materials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.materials"))
                    .icon(() -> new ItemStack(ModItems.IRON_SCRAP.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.IRON_SCRAP.get());
                        output.accept(ModItems.IRON_INGOT.get());
                        output.accept(ModItems.LEAD.get());
                        output.accept(ModItems.NITRATE.get());
                        output.accept(ModItems.COAL.get());
                        output.accept(ModItems.OIL_SHALE.get());
                        output.accept(ModItems.CLAY.get());
                        output.accept(ModItems.SAND.get());
                        output.accept(ModItems.GLASS_JAR.get());
                        output.accept(ModItems.MECHANICAL_PARTS.get());
                        output.accept(ModItems.ELECTRICAL_PARTS.get());
                        output.accept(ModItems.DUCT_TAPE.get());
                        output.accept(ModItems.FORGED_IRON.get());
                        output.accept(ModItems.FORGED_STEEL.get());
                        output.accept(ModItems.ACID.get());
                        output.accept(ModItems.POLYMER.get());
                        output.accept(ModItems.DUKES_CASINO_TOKEN.get());
                        output.accept(ModItems.CONCRETE_MIX.get());
                        output.accept(ModItems.ANTIBIOTICS.get());
                        output.accept(ModItems.GUNPOWDER.get());
                        output.accept(ModItems.GAS_CAN.get());
                        output.accept(ModItems.FORGED_LEAD.get());
                        output.accept(ModItems.NAIL.get());
                        output.accept(ModItems.SPRING.get());
                        output.accept(ModItems.CEMENT.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> WORKSTATIONS_TAB = CREATIVE_TABS.register("workstations",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.workstations"))
                    .icon(() -> new ItemStack(ModBlocks.WORKBENCH_BLOCK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.CAMPFIRE_BLOCK.get());
                        output.accept(ModBlocks.GRILL_BLOCK.get());
                        output.accept(ModBlocks.WORKBENCH_BLOCK.get());
                        output.accept(ModBlocks.FORGE_BLOCK.get());
                        output.accept(ModBlocks.CEMENT_MIXER_BLOCK.get());
                        output.accept(ModBlocks.CHEMISTRY_STATION_BLOCK.get());
                        output.accept(ModBlocks.ADVANCED_WORKBENCH_BLOCK.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> WEAPONS_TAB = CREATIVE_TABS.register("weapons",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.weapons"))
                    .icon(() -> new ItemStack(ModItems.AK47.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.STONE_CLUB.get());
                        output.accept(ModItems.BASEBALL_BAT.get());
                        output.accept(ModItems.IRON_SLEDGEHAMMER.get());
                        output.accept(ModItems.PISTOL_9MM.get());
                        output.accept(ModItems.AK47.get());
                        output.accept(ModItems.AMMO_9MM.get());
                        output.accept(ModItems.AMMO_762.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> LOOT_CONTAINERS_TAB = CREATIVE_TABS.register("loot_containers",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.loot_containers"))
                    .icon(() -> new ItemStack(ModBlocks.SUPPLY_CRATE_BLOCK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.TRASH_PILE_BLOCK.get());
                        output.accept(ModBlocks.CARDBOARD_BOX_BLOCK.get());
                        output.accept(ModBlocks.GUN_SAFE_BLOCK.get());
                        output.accept(ModBlocks.MUNITIONS_BOX_BLOCK.get());
                        output.accept(ModBlocks.SUPPLY_CRATE_BLOCK.get());
                        output.accept(ModBlocks.KITCHEN_CABINET_BLOCK.get());
                        output.accept(ModBlocks.MEDICINE_CABINET_BLOCK.get());
                        output.accept(ModBlocks.BOOKSHELF_CONTAINER_BLOCK.get());
                    })
                    .build());
}
