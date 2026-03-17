package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.weapon.RangedWeaponItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SevenDaysToMinecraft.MOD_ID);

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }

    public static final Supplier<Item> IRON_SCRAP = ITEMS.register("iron_scrap",
            () -> new Item(new Item.Properties().setId(key("iron_scrap")).stacksTo(64)));

    public static final Supplier<Item> IRON_INGOT = ITEMS.register("iron_ingot",
            () -> new Item(new Item.Properties().setId(key("iron_ingot")).stacksTo(64)));

    public static final Supplier<Item> LEAD = ITEMS.register("lead",
            () -> new Item(new Item.Properties().setId(key("lead")).stacksTo(64)));

    public static final Supplier<Item> NITRATE = ITEMS.register("nitrate",
            () -> new Item(new Item.Properties().setId(key("nitrate")).stacksTo(64)));

    public static final Supplier<Item> COAL = ITEMS.register("coal",
            () -> new Item(new Item.Properties().setId(key("coal")).stacksTo(64)));

    public static final Supplier<Item> OIL_SHALE = ITEMS.register("oil_shale",
            () -> new Item(new Item.Properties().setId(key("oil_shale")).stacksTo(64)));

    public static final Supplier<Item> CLAY = ITEMS.register("clay",
            () -> new Item(new Item.Properties().setId(key("clay")).stacksTo(64)));

    public static final Supplier<Item> SAND = ITEMS.register("sand",
            () -> new Item(new Item.Properties().setId(key("sand")).stacksTo(64)));

    public static final Supplier<Item> GLASS_JAR = ITEMS.register("glass_jar",
            () -> new Item(new Item.Properties().setId(key("glass_jar")).stacksTo(64)));

    public static final Supplier<Item> MECHANICAL_PARTS = ITEMS.register("mechanical_parts",
            () -> new Item(new Item.Properties().setId(key("mechanical_parts")).stacksTo(64)));

    public static final Supplier<Item> ELECTRICAL_PARTS = ITEMS.register("electrical_parts",
            () -> new Item(new Item.Properties().setId(key("electrical_parts")).stacksTo(64)));

    public static final Supplier<Item> DUCT_TAPE = ITEMS.register("duct_tape",
            () -> new Item(new Item.Properties().setId(key("duct_tape")).stacksTo(64)));

    public static final Supplier<Item> FORGED_IRON = ITEMS.register("forged_iron",
            () -> new Item(new Item.Properties().setId(key("forged_iron")).stacksTo(64)));

    public static final Supplier<Item> FORGED_STEEL = ITEMS.register("forged_steel",
            () -> new Item(new Item.Properties().setId(key("forged_steel")).stacksTo(64)));

    public static final Supplier<Item> ACID = ITEMS.register("acid",
            () -> new Item(new Item.Properties().setId(key("acid")).stacksTo(64)));

    public static final Supplier<Item> POLYMER = ITEMS.register("polymer",
            () -> new Item(new Item.Properties().setId(key("polymer")).stacksTo(64)));

    public static final Supplier<Item> DUKES_CASINO_TOKEN = ITEMS.register("dukes_casino_token",
            () -> new Item(new Item.Properties().setId(key("dukes_casino_token")).stacksTo(50000)));

    public static final Supplier<Item> CONCRETE_MIX = ITEMS.register("concrete_mix",
            () -> new Item(new Item.Properties().setId(key("concrete_mix")).stacksTo(64)));

    public static final Supplier<Item> ANTIBIOTICS = ITEMS.register("antibiotics",
            () -> new Item(new Item.Properties().setId(key("antibiotics")).stacksTo(64)));

    public static final Supplier<Item> GUNPOWDER = ITEMS.register("gunpowder",
            () -> new Item(new Item.Properties().setId(key("gunpowder")).stacksTo(64)));

    public static final Supplier<Item> GAS_CAN = ITEMS.register("gas_can",
            () -> new Item(new Item.Properties().setId(key("gas_can")).stacksTo(16)));

    public static final Supplier<Item> FORGED_LEAD = ITEMS.register("forged_lead",
            () -> new Item(new Item.Properties().setId(key("forged_lead")).stacksTo(64)));

    public static final Supplier<Item> NAIL = ITEMS.register("nail",
            () -> new Item(new Item.Properties().setId(key("nail")).stacksTo(64)));

    public static final Supplier<Item> SPRING = ITEMS.register("spring",
            () -> new Item(new Item.Properties().setId(key("spring")).stacksTo(64)));

    public static final Supplier<Item> CEMENT = ITEMS.register("cement",
            () -> new Item(new Item.Properties().setId(key("cement")).stacksTo(64)));

    public static final ToolMaterial WOOD_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 131, 2.0f, 0.0f, 10, ItemTags.PLANKS);

    public static final ToolMaterial IRON_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 500, 6.0f, 2.0f, 14, ItemTags.IRON_ORES);

    public static final Supplier<Item> STONE_CLUB = ITEMS.register("stone_club",
            () -> new SwordItem(WOOD_MATERIAL, 4.0f, -2.8f,
                    new Item.Properties().setId(key("stone_club"))));

    public static final Supplier<Item> BASEBALL_BAT = ITEMS.register("baseball_bat",
            () -> new SwordItem(WOOD_MATERIAL, 5.0f, -2.6f,
                    new Item.Properties().setId(key("baseball_bat"))));

    public static final Supplier<Item> IRON_SLEDGEHAMMER = ITEMS.register("iron_sledgehammer",
            () -> new SwordItem(IRON_MATERIAL, 9.0f, -3.4f,
                    new Item.Properties().setId(key("iron_sledgehammer"))));

    public static final Supplier<Item> AMMO_9MM = ITEMS.register("ammo_9mm",
            () -> new Item(new Item.Properties().setId(key("ammo_9mm")).stacksTo(64)));

    public static final Supplier<Item> AMMO_762 = ITEMS.register("ammo_762",
            () -> new Item(new Item.Properties().setId(key("ammo_762")).stacksTo(64)));

    public static final Supplier<Item> PISTOL_9MM = ITEMS.register("pistol_9mm",
            () -> new RangedWeaponItem(
                    new Item.Properties().setId(key("pistol_9mm")).durability(250).stacksTo(1),
                    8.0f, 8, 3.0f, 2.0f, () -> AMMO_9MM.get()));

    public static final Supplier<Item> AK47 = ITEMS.register("ak47",
            () -> new RangedWeaponItem(
                    new Item.Properties().setId(key("ak47")).durability(500).stacksTo(1),
                    12.0f, 4, 3.5f, 3.0f, () -> AMMO_762.get()));
}
