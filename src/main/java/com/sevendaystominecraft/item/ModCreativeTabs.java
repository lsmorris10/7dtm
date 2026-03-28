package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.magazine.ModMagazines;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                        output.accept(Items.IRON_INGOT);
                        output.accept(ModItems.LEAD.get());
                        output.accept(ModItems.NITRATE.get());
                        output.accept(Items.COAL);
                        output.accept(ModItems.OIL_SHALE.get());
                        output.accept(Items.CLAY_BALL);
                        output.accept(Items.SAND);
                        output.accept(ModItems.MECHANICAL_PARTS.get());
                        output.accept(ModItems.ELECTRICAL_PARTS.get());
                        output.accept(ModItems.DUCT_TAPE.get());
                        output.accept(ModItems.FORGED_IRON.get());
                        output.accept(ModItems.FORGED_STEEL.get());
                        output.accept(ModItems.ACID.get());
                        output.accept(ModItems.POLYMER.get());
                        output.accept(ModItems.SURVIVORS_COIN.get());
                        output.accept(ModItems.CONCRETE_MIX.get());
                        output.accept(ModItems.ANTIBIOTICS.get());
                        output.accept(ModItems.BANDAGE.get());
                        output.accept(ModItems.SPLINT.get());
                        output.accept(ModItems.PAINKILLER.get());
                        output.accept(ModItems.ALOE_CREAM.get());
                        output.accept(ModItems.FIRST_AID_KIT.get());
                        output.accept(Items.GUNPOWDER);
                        output.accept(ModItems.GAS_CAN.get());
                        output.accept(ModItems.FORGED_LEAD.get());
                        output.accept(ModItems.NAIL.get());
                        output.accept(ModItems.SPRING.get());
                        output.accept(ModItems.CEMENT.get());
                        output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
                        output.accept(ModItems.BOILED_WATER.get());
                        output.accept(ModItems.GOLDENROD.get());
                        output.accept(ModItems.CHRYSANTHEMUM.get());
                        output.accept(ModItems.CHARRED_MEAT.get());
                        output.accept(ModItems.GOLDENROD_TEA.get());
                        output.accept(ModItems.RED_TEA.get());
                        output.accept(ModItems.MURKY_WATER.get());
                        output.accept(ModItems.CORN.get());
                        output.accept(ModItems.POTATO_CROP_ITEM.get());
                        output.accept(ModItems.BLUEBERRY.get());
                        output.accept(ModItems.ALOE.get());
                        output.accept(ModItems.COFFEE_BEANS.get());
                        output.accept(ModItems.CORN_SEED.get());
                        output.accept(ModItems.POTATO_SEED.get());
                        output.accept(ModItems.BLUEBERRY_SEED.get());
                        output.accept(ModItems.GOLDENROD_SEED.get());
                        output.accept(ModItems.ALOE_SEED.get());
                        output.accept(ModItems.COFFEE_SEED.get());
                        output.accept(ModItems.CORN_ON_THE_COB.get());
                        output.accept(ModItems.BLUEBERRY_PIE.get());
                        output.accept(ModItems.VEGETABLE_STEW.get());
                        output.accept(ModItems.MEAT_STEW.get());
                        output.accept(ModItems.COFFEE.get());
                        output.accept(ModItems.HOBO_STEW.get());
                        output.accept(ModItems.SHAM_CHOWDER.get());
                        output.accept(ModItems.BAKED_POTATO_MEAL.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> WORKSTATIONS_TAB = CREATIVE_TABS.register("workstations",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.workstations"))
                    .icon(() -> new ItemStack(ModBlocks.WORKBENCH_BLOCK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.GRILL_BLOCK.get());
                        output.accept(ModBlocks.WORKBENCH_BLOCK.get());
                        output.accept(ModBlocks.FORGE_BLOCK.get());
                        output.accept(ModBlocks.CEMENT_MIXER_BLOCK.get());
                        output.accept(ModBlocks.CHEMISTRY_STATION_BLOCK.get());
                        output.accept(ModBlocks.ADVANCED_WORKBENCH_BLOCK.get());

                        output.accept(ModBlocks.DEW_COLLECTOR_BLOCK.get());
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
                        output.accept(ModItems.SHOTGUN.get());
                        output.accept(ModItems.SMG.get());
                        output.accept(ModItems.HUNTING_RIFLE.get());
                        output.accept(ModItems.SNIPER_RIFLE.get());
                        output.accept(ModItems.M60.get());
                        output.accept(ModItems.AMMO_9MM.get());
                        output.accept(ModItems.AMMO_762.get());
                        output.accept(ModItems.AMMO_SHOTGUN_SHELL.get());
                        output.accept(ModItems.AMMO_44_MAGNUM.get());
                        output.accept(ModItems.GRENADE.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> ARMOR_TAB = CREATIVE_TABS.register("armor",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.armor"))
                    .icon(() -> new ItemStack(ModItems.MILITARY_CHESTPLATE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.PADDED_HELMET.get());
                        output.accept(ModItems.PADDED_CHESTPLATE.get());
                        output.accept(ModItems.PADDED_LEGGINGS.get());
                        output.accept(ModItems.PADDED_BOOTS.get());
                        output.accept(ModItems.SCRAP_IRON_HELMET.get());
                        output.accept(ModItems.SCRAP_IRON_CHESTPLATE.get());
                        output.accept(ModItems.SCRAP_IRON_LEGGINGS.get());
                        output.accept(ModItems.SCRAP_IRON_BOOTS.get());
                        output.accept(ModItems.MILITARY_HELMET.get());
                        output.accept(ModItems.MILITARY_CHESTPLATE.get());
                        output.accept(ModItems.MILITARY_LEGGINGS.get());
                        output.accept(ModItems.MILITARY_BOOTS.get());
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

    public static final Supplier<CreativeModeTab> BUILDING_TAB = CREATIVE_TABS.register("building",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.building"))
                    .icon(() -> new ItemStack(ModBlocks.UPGRADEABLE_FRAME.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.UPGRADEABLE_FRAME.get());
                        output.accept(ModBlocks.WOOD_SPIKES.get());
                        output.accept(ModBlocks.IRON_SPIKES.get());
                        output.accept(ModBlocks.BLADE_TRAP.get());
                        output.accept(ModBlocks.ELECTRIC_FENCE_POST.get());
                        output.accept(ModBlocks.GENERATOR_BANK_BLOCK.get());
                        output.accept(ModBlocks.BATTERY_BANK_BLOCK.get());
                        output.accept(ModBlocks.SOLAR_PANEL_BLOCK.get());
                        output.accept(ModBlocks.LAND_CLAIM_BLOCK.get());
                        output.accept(ModItems.REPAIR_HAMMER.get());
                        output.accept(ModItems.WRENCH.get());
                        output.accept(ModBlocks.GAS_CAN_BLOCK.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> MAGAZINES_TAB = CREATIVE_TABS.register("magazines",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.sevendaystominecraft.magazines"))
                    .icon(() -> {
                        Supplier<Item> first = ModMagazines.getMagazineItem("steady_steve", 1);
                        return first != null ? new ItemStack(first.get()) : new ItemStack(Items.BOOK);
                    })
                    .displayItems((params, output) -> {
                        for (Supplier<Item> mag : ModMagazines.getAllMagazineItems()) {
                            output.accept(mag.get());
                        }
                    })
                    .build());
}
