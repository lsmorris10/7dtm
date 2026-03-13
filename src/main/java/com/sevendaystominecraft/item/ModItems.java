package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
}
