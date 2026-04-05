package com.sevendaystominecraft.item.armor;

import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

public final class ModArmorMaterials {

    private static ResourceKey<EquipmentAsset> modAsset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID,
                ResourceLocation.fromNamespaceAndPath("sevendaystominecraft", name));
    }

    public static final ArmorMaterial PADDED = new ArmorMaterial(
            5,
            Util.make(new EnumMap<>(ArmorType.class), m -> {
                m.put(ArmorType.HELMET, 1);
                m.put(ArmorType.CHESTPLATE, 2);
                m.put(ArmorType.LEGGINGS, 2);
                m.put(ArmorType.BOOTS, 1);
            }),
            12,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0f,
            0.0f,
            ItemTags.REPAIRS_LEATHER_ARMOR,
            modAsset("padded")
    );

    public static final ArmorMaterial SCRAP_IRON = new ArmorMaterial(
            15,
            Util.make(new EnumMap<>(ArmorType.class), m -> {
                m.put(ArmorType.HELMET, 2);
                m.put(ArmorType.CHESTPLATE, 5);
                m.put(ArmorType.LEGGINGS, 4);
                m.put(ArmorType.BOOTS, 2);
            }),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0f,
            0.0f,
            ItemTags.REPAIRS_IRON_ARMOR,
            modAsset("scrap_iron")
    );

    public static final ArmorMaterial MILITARY = new ArmorMaterial(
            30,
            Util.make(new EnumMap<>(ArmorType.class), m -> {
                m.put(ArmorType.HELMET, 3);
                m.put(ArmorType.CHESTPLATE, 7);
                m.put(ArmorType.LEGGINGS, 6);
                m.put(ArmorType.BOOTS, 3);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            2.5f,
            0.05f,
            ItemTags.REPAIRS_IRON_ARMOR,
            modAsset("military")
    );

    private ModArmorMaterials() {}
}
