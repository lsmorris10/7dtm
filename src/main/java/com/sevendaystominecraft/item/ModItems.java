package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import com.sevendaystominecraft.item.weapon.GrenadeItem;
import com.sevendaystominecraft.sound.ModSounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.sevendaystominecraft.block.ModBlocks;

import com.sevendaystominecraft.item.armor.ArmorTier;
import com.sevendaystominecraft.item.armor.ModArmorMaterials;
import com.sevendaystominecraft.item.armor.TieredArmorItem;

import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SevenDaysToMinecraft.MOD_ID);

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }

    // Iron scrap is now a placeable block — registered in ModBlocks as IRON_SCRAP_BLOCK.
    // This lazy delegate preserves the ModItems.IRON_SCRAP API for all existing references.
    public static final Supplier<Item> IRON_SCRAP = () -> ModBlocks.IRON_SCRAP_BLOCK.get().asItem();

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

    public static final Supplier<Item> AMMO_SHOTGUN_SHELL = ITEMS.register("ammo_shotgun_shell",
            () -> new Item(new Item.Properties().setId(key("ammo_shotgun_shell")).stacksTo(64)));

    public static final Supplier<Item> AMMO_44_MAGNUM = ITEMS.register("ammo_44_magnum",
            () -> new Item(new Item.Properties().setId(key("ammo_44_magnum")).stacksTo(64)));

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
                    0.005, 200, true, true));

    public static final Supplier<Item> SHOTGUN = ITEMS.register("shotgun",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("shotgun")).durability(400).stacksTo(1),
                    48.0f, 16, 3.5f, 5.0f, () -> AMMO_SHOTGUN_SHELL.get(),
                    8, 60, GeoRangedWeaponItem.WeaponType.SHOTGUN,
                    () -> ModSounds.SHOTGUN_FIRE.get(),
                    0.02, 40, false, false, 8));

    public static final Supplier<Item> SMG = ITEMS.register("smg",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("smg")).durability(350).stacksTo(1),
                    6.0f, 3, 4.0f, 4.0f, () -> AMMO_9MM.get(),
                    30, 40, GeoRangedWeaponItem.WeaponType.SMG,
                    () -> ModSounds.GUN_FIRE_SMG.get(),
                    0.01, 100, true, false));

    public static final Supplier<Item> HUNTING_RIFLE = ITEMS.register("hunting_rifle",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("hunting_rifle")).durability(400).stacksTo(1),
                    18.0f, 20, 6.0f, 1.5f, () -> AMMO_762.get(),
                    5, 50, GeoRangedWeaponItem.WeaponType.HUNTING_RIFLE,
                    () -> ModSounds.GUN_FIRE_HUNTING_RIFLE.get(),
                    0.003, 300, false, false));

    public static final Supplier<Item> SNIPER_RIFLE = ITEMS.register("sniper_rifle",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("sniper_rifle")).durability(450).stacksTo(1),
                    30.0f, 30, 8.0f, 0.5f, () -> AMMO_762.get(),
                    5, 60, GeoRangedWeaponItem.WeaponType.SNIPER_RIFLE,
                    () -> ModSounds.GUN_FIRE_SNIPER_RIFLE.get(),
                    0.001, 400, false, false));

    public static final Supplier<Item> M60 = ITEMS.register("m60",
            () -> new GeoRangedWeaponItem(
                    new Item.Properties().setId(key("m60")).durability(600).stacksTo(1),
                    10.0f, 3, 5.0f, 4.5f, () -> AMMO_762.get(),
                    100, 80, GeoRangedWeaponItem.WeaponType.M60,
                    () -> ModSounds.GUN_FIRE_M60.get(),
                    0.005, 200, true, false, 1, 0.7f));

    public static final Supplier<Item> GRENADE = ITEMS.register("grenade",
            () -> new GrenadeItem(
                    new Item.Properties().setId(key("grenade")).stacksTo(16)));

    public static final Supplier<Item> BOILED_WATER = ITEMS.register("boiled_water",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("boiled_water")).stacksTo(64),
                    0f, 4f, new String[]{}, new String[]{}, 0));

    public static final Supplier<Item> GOLDENROD = ITEMS.register("goldenrod",
            () -> new Item(new Item.Properties().setId(key("goldenrod")).stacksTo(64)));

    public static final Supplier<Item> CHRYSANTHEMUM = ITEMS.register("chrysanthemum",
            () -> new Item(new Item.Properties().setId(key("chrysanthemum")).stacksTo(64)));

    public static final Supplier<Item> CHARRED_MEAT = ITEMS.register("charred_meat",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("charred_meat")).stacksTo(64),
                    2.4f, -1f, new String[]{}, new String[]{}, 0, 0.10f));

    public static final Supplier<Item> GOLDENROD_TEA = ITEMS.register("goldenrod_tea",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("goldenrod_tea")).stacksTo(64),
                    0.4f, 5f, new String[]{}, new String[]{SevenDaysPlayerStats.DEBUFF_DYSENTERY}, 0));

    public static final Supplier<Item> RED_TEA = ITEMS.register("red_tea",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("red_tea")).stacksTo(64),
                    0.4f, 4f, new String[]{}, new String[]{}, 1200));

    public static final Supplier<Item> PADDED_HELMET = ITEMS.register("padded_helmet",
            () -> new TieredArmorItem(ModArmorMaterials.PADDED, ArmorType.HELMET,
                    new Item.Properties().setId(key("padded_helmet")).stacksTo(1), ArmorTier.LIGHT));

    public static final Supplier<Item> PADDED_CHESTPLATE = ITEMS.register("padded_chestplate",
            () -> new TieredArmorItem(ModArmorMaterials.PADDED, ArmorType.CHESTPLATE,
                    new Item.Properties().setId(key("padded_chestplate")).stacksTo(1), ArmorTier.LIGHT));

    public static final Supplier<Item> PADDED_LEGGINGS = ITEMS.register("padded_leggings",
            () -> new TieredArmorItem(ModArmorMaterials.PADDED, ArmorType.LEGGINGS,
                    new Item.Properties().setId(key("padded_leggings")).stacksTo(1), ArmorTier.LIGHT));

    public static final Supplier<Item> PADDED_BOOTS = ITEMS.register("padded_boots",
            () -> new TieredArmorItem(ModArmorMaterials.PADDED, ArmorType.BOOTS,
                    new Item.Properties().setId(key("padded_boots")).stacksTo(1), ArmorTier.LIGHT));

    public static final Supplier<Item> SCRAP_IRON_HELMET = ITEMS.register("scrap_iron_helmet",
            () -> new TieredArmorItem(ModArmorMaterials.SCRAP_IRON, ArmorType.HELMET,
                    new Item.Properties().setId(key("scrap_iron_helmet")).stacksTo(1), ArmorTier.MEDIUM));

    public static final Supplier<Item> SCRAP_IRON_CHESTPLATE = ITEMS.register("scrap_iron_chestplate",
            () -> new TieredArmorItem(ModArmorMaterials.SCRAP_IRON, ArmorType.CHESTPLATE,
                    new Item.Properties().setId(key("scrap_iron_chestplate")).stacksTo(1), ArmorTier.MEDIUM));

    public static final Supplier<Item> SCRAP_IRON_LEGGINGS = ITEMS.register("scrap_iron_leggings",
            () -> new TieredArmorItem(ModArmorMaterials.SCRAP_IRON, ArmorType.LEGGINGS,
                    new Item.Properties().setId(key("scrap_iron_leggings")).stacksTo(1), ArmorTier.MEDIUM));

    public static final Supplier<Item> SCRAP_IRON_BOOTS = ITEMS.register("scrap_iron_boots",
            () -> new TieredArmorItem(ModArmorMaterials.SCRAP_IRON, ArmorType.BOOTS,
                    new Item.Properties().setId(key("scrap_iron_boots")).stacksTo(1), ArmorTier.MEDIUM));

    public static final Supplier<Item> MILITARY_HELMET = ITEMS.register("military_helmet",
            () -> new TieredArmorItem(ModArmorMaterials.MILITARY, ArmorType.HELMET,
                    new Item.Properties().setId(key("military_helmet")).stacksTo(1), ArmorTier.HEAVY));

    public static final Supplier<Item> MILITARY_CHESTPLATE = ITEMS.register("military_chestplate",
            () -> new TieredArmorItem(ModArmorMaterials.MILITARY, ArmorType.CHESTPLATE,
                    new Item.Properties().setId(key("military_chestplate")).stacksTo(1), ArmorTier.HEAVY));

    public static final Supplier<Item> MILITARY_LEGGINGS = ITEMS.register("military_leggings",
            () -> new TieredArmorItem(ModArmorMaterials.MILITARY, ArmorType.LEGGINGS,
                    new Item.Properties().setId(key("military_leggings")).stacksTo(1), ArmorTier.HEAVY));

    public static final Supplier<Item> MILITARY_BOOTS = ITEMS.register("military_boots",
            () -> new TieredArmorItem(ModArmorMaterials.MILITARY, ArmorType.BOOTS,
                    new Item.Properties().setId(key("military_boots")).stacksTo(1), ArmorTier.HEAVY));

    public static final ToolMaterial REPAIR_HAMMER_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 500, 2.0f, 0.0f, 10, ItemTags.IRON_ORES);

    public static final Supplier<Item> REPAIR_HAMMER = ITEMS.register("repair_hammer",
            () -> new SwordItem(REPAIR_HAMMER_MATERIAL, 2.0f, -2.4f,
                    new Item.Properties().setId(key("repair_hammer"))));

    public static final ToolMaterial WRENCH_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 500, 2.0f, 0.0f, 10, ItemTags.IRON_ORES);

    public static final Supplier<Item> WRENCH = ITEMS.register("wrench",
            () -> new SwordItem(WRENCH_MATERIAL, 3.0f, -2.4f,
                    new Item.Properties().setId(key("wrench"))));

    public static final Supplier<Item> MURKY_WATER = ITEMS.register("murky_water",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("murky_water")).stacksTo(64),
                    0f, 3f, new String[]{SevenDaysPlayerStats.DEBUFF_DYSENTERY}, new String[]{}, 0));

    public static final Supplier<Item> CORN = ITEMS.register("corn",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("corn")).stacksTo(64),
                    1f, 0f, new String[]{}, new String[]{}, 0, 0.05f));

    public static final Supplier<Item> POTATO_CROP_ITEM = ITEMS.register("potato_crop_item",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("potato_crop_item")).stacksTo(64),
                    0.8f, 0f, new String[]{}, new String[]{}, 0, 0.05f));

    public static final Supplier<Item> BLUEBERRY = ITEMS.register("blueberry",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("blueberry")).stacksTo(64),
                    0.6f, 0.4f, new String[]{}, new String[]{}, 0));

    public static final Supplier<Item> ALOE = ITEMS.register("aloe",
            () -> new Item(new Item.Properties().setId(key("aloe")).stacksTo(64)));

    public static final Supplier<Item> COFFEE_BEANS = ITEMS.register("coffee_beans",
            () -> new Item(new Item.Properties().setId(key("coffee_beans")).stacksTo(64)));

    public static final Supplier<Item> CORN_SEED = ITEMS.register("corn_seed",
            () -> new SeedItem(new Item.Properties().setId(key("corn_seed")).stacksTo(64),
                    () -> ModBlocks.CORN_CROP.get()));

    public static final Supplier<Item> POTATO_SEED = ITEMS.register("potato_seed",
            () -> new SeedItem(new Item.Properties().setId(key("potato_seed")).stacksTo(64),
                    () -> ModBlocks.POTATO_CROP.get()));

    public static final Supplier<Item> BLUEBERRY_SEED = ITEMS.register("blueberry_seed",
            () -> new SeedItem(new Item.Properties().setId(key("blueberry_seed")).stacksTo(64),
                    () -> ModBlocks.BLUEBERRY_CROP.get()));

    public static final Supplier<Item> GOLDENROD_SEED = ITEMS.register("goldenrod_seed",
            () -> new SeedItem(new Item.Properties().setId(key("goldenrod_seed")).stacksTo(64),
                    () -> ModBlocks.GOLDENROD_CROP.get()));

    public static final Supplier<Item> ALOE_SEED = ITEMS.register("aloe_seed",
            () -> new SeedItem(new Item.Properties().setId(key("aloe_seed")).stacksTo(64),
                    () -> ModBlocks.ALOE_CROP.get()));

    public static final Supplier<Item> COFFEE_SEED = ITEMS.register("coffee_seed",
            () -> new SeedItem(new Item.Properties().setId(key("coffee_seed")).stacksTo(64),
                    () -> ModBlocks.COFFEE_CROP.get()));

    public static final Supplier<Item> CORN_ON_THE_COB = ITEMS.register("corn_on_the_cob",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("corn_on_the_cob")).stacksTo(64),
                    3f, 0f, new String[]{}, new String[]{}, 0));

    public static final Supplier<Item> BLUEBERRY_PIE = ITEMS.register("blueberry_pie",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("blueberry_pie")).stacksTo(64),
                    4f, 1f, new String[]{}, new String[]{}, 600));

    public static final Supplier<Item> VEGETABLE_STEW = ITEMS.register("vegetable_stew",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("vegetable_stew")).stacksTo(64),
                    4.8f, 2f, new String[]{}, new String[]{}, 0));

    public static final Supplier<Item> MEAT_STEW = ITEMS.register("meat_stew",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("meat_stew")).stacksTo(64),
                    6f, 2f, new String[]{}, new String[]{}, 600));

    public static final Supplier<Item> COFFEE = ITEMS.register("coffee",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("coffee")).stacksTo(64),
                    0.4f, 3f, new String[]{}, new String[]{}, 0) {
                @Override
                public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
                    net.minecraft.world.InteractionResult result = super.use(level, player, hand);
                    if (!level.isClientSide && result == net.minecraft.world.InteractionResult.CONSUME) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 2400, 0));
                    }
                    return result;
                }
            });

    public static final Supplier<Item> HOBO_STEW = ITEMS.register("hobo_stew",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("hobo_stew")).stacksTo(64),
                    7f, 3f, new String[]{}, new String[]{}, 1200));

    public static final Supplier<Item> SHAM_CHOWDER = ITEMS.register("sham_chowder",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("sham_chowder")).stacksTo(64),
                    3.6f, 4f, new String[]{}, new String[]{}, 400));

    public static final Supplier<Item> BAKED_POTATO_MEAL = ITEMS.register("baked_potato_meal",
            () -> new ConsumableStatItem(new Item.Properties().setId(key("baked_potato_meal")).stacksTo(64),
                    3.2f, 0f, new String[]{}, new String[]{}, 0));

    public static final Supplier<Item> FERTILIZER = ITEMS.register("fertilizer",
            () -> new Item(new Item.Properties().setId(key("fertilizer")).stacksTo(64)));
}
