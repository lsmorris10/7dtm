package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import com.sevendaystominecraft.item.weapon.GrenadeItem;
import com.sevendaystominecraft.sound.ModSounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

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

    public static final Supplier<Item> LEAD = ITEMS.register("lead",
            () -> new Item(new Item.Properties().setId(key("lead")).stacksTo(64)));

    public static final Supplier<Item> NITRATE = ITEMS.register("nitrate",
            () -> new Item(new Item.Properties().setId(key("nitrate")).stacksTo(64)));

    public static final Supplier<Item> OIL_SHALE = ITEMS.register("oil_shale",
            () -> new Item(new Item.Properties().setId(key("oil_shale")).stacksTo(64)));

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

    public static final Supplier<Item> SURVIVORS_COIN = ITEMS.register("survivors_coin",
            () -> new Item(new Item.Properties().setId(key("survivors_coin")).stacksTo(50000)));

    public static final Supplier<Item> CONCRETE_MIX = ITEMS.register("concrete_mix",
            () -> new Item(new Item.Properties().setId(key("concrete_mix")).stacksTo(64)));

    public static final Supplier<Item> ANTIBIOTICS = ITEMS.register("antibiotics",
            () -> new TreatmentItem(new Item.Properties().setId(key("antibiotics")).stacksTo(64),
                    SevenDaysPlayerStats.DEBUFF_INFECTION_1,
                    SevenDaysPlayerStats.DEBUFF_INFECTION_2,
                    SevenDaysPlayerStats.DEBUFF_DYSENTERY));

    public static final Supplier<Item> BANDAGE = ITEMS.register("bandage",
            () -> new TreatmentItem(new Item.Properties().setId(key("bandage")).stacksTo(64),
                    SevenDaysPlayerStats.DEBUFF_BLEEDING));

    public static final Supplier<Item> SPLINT = ITEMS.register("splint",
            () -> new TreatmentItem(new Item.Properties().setId(key("splint")).stacksTo(64),
                    SevenDaysPlayerStats.DEBUFF_SPRAIN,
                    SevenDaysPlayerStats.DEBUFF_FRACTURE));

    public static final Supplier<Item> PAINKILLER = ITEMS.register("painkiller",
            () -> new TreatmentItem(new Item.Properties().setId(key("painkiller")).stacksTo(64),
                    SevenDaysPlayerStats.DEBUFF_CONCUSSION));

    public static final Supplier<Item> ALOE_CREAM = ITEMS.register("aloe_cream",
            () -> new TreatmentItem(new Item.Properties().setId(key("aloe_cream")).stacksTo(64),
                    SevenDaysPlayerStats.DEBUFF_BURN));

    public static final Supplier<Item> FIRST_AID_KIT = ITEMS.register("first_aid_kit",
            () -> new TreatmentItem(new Item.Properties().setId(key("first_aid_kit")).stacksTo(16),
                    SevenDaysPlayerStats.DEBUFF_BLEEDING,
                    SevenDaysPlayerStats.DEBUFF_SPRAIN,
                    SevenDaysPlayerStats.DEBUFF_FRACTURE));

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

    public static final ToolMaterial STONE_CLUB_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 100, 2.0f, 0.0f, 10, ItemTags.STONE_CRAFTING_MATERIALS);

    public static final ToolMaterial WOOD_BAT_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 200, 2.0f, 0.0f, 10, ItemTags.PLANKS);

    public static final ToolMaterial IRON_SLEDGE_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 500, 6.0f, 2.0f, 14, ItemTags.IRON_ORES);

    public static final Supplier<Item> STONE_CLUB = ITEMS.register("stone_club",
            () -> new SwordItem(STONE_CLUB_MATERIAL, 4.0f, -2.8f,
                    new Item.Properties().setId(key("stone_club"))));

    public static final Supplier<Item> BASEBALL_BAT = ITEMS.register("baseball_bat",
            () -> new SwordItem(WOOD_BAT_MATERIAL, 5.0f, -2.6f,
                    new Item.Properties().setId(key("baseball_bat"))));

    public static final Supplier<Item> IRON_SLEDGEHAMMER = ITEMS.register("iron_sledgehammer",
            () -> new SwordItem(IRON_SLEDGE_MATERIAL, 9.0f, -3.4f,
                    new Item.Properties().setId(key("iron_sledgehammer"))));

    public static final Supplier<Item> AMMO_9MM = ITEMS.register("ammo_9mm",
            () -> new Item(new Item.Properties().setId(key("ammo_9mm")).stacksTo(64)));

    public static final Supplier<Item> AMMO_762 = ITEMS.register("ammo_762",
            () -> new Item(new Item.Properties().setId(key("ammo_762")).stacksTo(64)));

    public static final Supplier<Item> PISTOL_9MM = ITEMS.register("pistol_9mm",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("pistol_9mm")).durability(250).stacksTo(1),
                    8.0f, 8, 3.0f, 2.0f, () -> AMMO_9MM.get(),
                    15, 36, GeoRangedWeaponItem.WeaponType.PISTOL_9MM,
                    () -> ModSounds.GUN_FIRE_9MM.get()));

    public static final Supplier<Item> AK47 = ITEMS.register("ak47",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("ak47")).durability(500).stacksTo(1),
                    12.0f, 4, 5.25f, 3.0f, () -> AMMO_762.get(),
                    30, 50, GeoRangedWeaponItem.WeaponType.AK47,
                    () -> ModSounds.GUN_FIRE_AK47.get(),
                    0.005, 200, true));

    public static final Supplier<Item> GRENADE = ITEMS.register("grenade",
            () -> new GrenadeItem(
                    new Item.Properties().setId(key("grenade")).stacksTo(16)));

    public static final Supplier<Item> BOILED_WATER = ITEMS.register("boiled_water",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("boiled_water")).stacksTo(64),
                    0f, 20f, new String[]{}, new String[]{}, 0));

    public static final Supplier<Item> GOLDENROD = ITEMS.register("goldenrod",
            () -> new Item(new Item.Properties().setId(key("goldenrod")).stacksTo(64)));

    public static final Supplier<Item> CHRYSANTHEMUM = ITEMS.register("chrysanthemum",
            () -> new Item(new Item.Properties().setId(key("chrysanthemum")).stacksTo(64)));

    public static final Supplier<Item> CHARRED_MEAT = ITEMS.register("charred_meat",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("charred_meat")).stacksTo(64),
                    12f, -5f, new String[]{}, new String[]{}, 0, 0.10f));

    public static final Supplier<Item> GOLDENROD_TEA = ITEMS.register("goldenrod_tea",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("goldenrod_tea")).stacksTo(64),
                    2f, 25f, new String[]{}, new String[]{SevenDaysPlayerStats.DEBUFF_DYSENTERY}, 0));

    public static final Supplier<Item> RED_TEA = ITEMS.register("red_tea",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("red_tea")).stacksTo(64),
                    2f, 20f, new String[]{}, new String[]{}, 1200));
}
