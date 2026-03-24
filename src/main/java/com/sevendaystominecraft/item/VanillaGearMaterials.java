package com.sevendaystominecraft.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;

import java.util.*;

public class VanillaGearMaterials {

    public enum MaterialTier {
        WOOD(QualityTier.POOR),
        LEATHER(QualityTier.POOR),
        STONE(QualityTier.GOOD),
        CHAINMAIL(QualityTier.GOOD),
        GOLD(QualityTier.GOOD),
        IRON(QualityTier.GREAT),
        DIAMOND(QualityTier.SUPERIOR),
        NETHERITE(QualityTier.EXCELLENT);

        private final QualityTier baselineQuality;

        MaterialTier(QualityTier baselineQuality) {
            this.baselineQuality = baselineQuality;
        }

        public QualityTier getBaselineQuality() {
            return baselineQuality;
        }
    }

    private static final Map<Item, MaterialTier> ITEM_MATERIAL_MAP = new HashMap<>();

    static {
        register(Items.WOODEN_PICKAXE, MaterialTier.WOOD);
        register(Items.WOODEN_AXE, MaterialTier.WOOD);
        register(Items.WOODEN_SHOVEL, MaterialTier.WOOD);
        register(Items.WOODEN_HOE, MaterialTier.WOOD);
        register(Items.WOODEN_SWORD, MaterialTier.WOOD);

        register(Items.STONE_PICKAXE, MaterialTier.STONE);
        register(Items.STONE_AXE, MaterialTier.STONE);
        register(Items.STONE_SHOVEL, MaterialTier.STONE);
        register(Items.STONE_HOE, MaterialTier.STONE);
        register(Items.STONE_SWORD, MaterialTier.STONE);

        register(Items.GOLDEN_PICKAXE, MaterialTier.GOLD);
        register(Items.GOLDEN_AXE, MaterialTier.GOLD);
        register(Items.GOLDEN_SHOVEL, MaterialTier.GOLD);
        register(Items.GOLDEN_HOE, MaterialTier.GOLD);
        register(Items.GOLDEN_SWORD, MaterialTier.GOLD);

        register(Items.IRON_PICKAXE, MaterialTier.IRON);
        register(Items.IRON_AXE, MaterialTier.IRON);
        register(Items.IRON_SHOVEL, MaterialTier.IRON);
        register(Items.IRON_HOE, MaterialTier.IRON);
        register(Items.IRON_SWORD, MaterialTier.IRON);

        register(Items.DIAMOND_PICKAXE, MaterialTier.DIAMOND);
        register(Items.DIAMOND_AXE, MaterialTier.DIAMOND);
        register(Items.DIAMOND_SHOVEL, MaterialTier.DIAMOND);
        register(Items.DIAMOND_HOE, MaterialTier.DIAMOND);
        register(Items.DIAMOND_SWORD, MaterialTier.DIAMOND);

        register(Items.NETHERITE_PICKAXE, MaterialTier.NETHERITE);
        register(Items.NETHERITE_AXE, MaterialTier.NETHERITE);
        register(Items.NETHERITE_SHOVEL, MaterialTier.NETHERITE);
        register(Items.NETHERITE_HOE, MaterialTier.NETHERITE);
        register(Items.NETHERITE_SWORD, MaterialTier.NETHERITE);

        register(Items.LEATHER_HELMET, MaterialTier.LEATHER);
        register(Items.LEATHER_CHESTPLATE, MaterialTier.LEATHER);
        register(Items.LEATHER_LEGGINGS, MaterialTier.LEATHER);
        register(Items.LEATHER_BOOTS, MaterialTier.LEATHER);

        register(Items.CHAINMAIL_HELMET, MaterialTier.CHAINMAIL);
        register(Items.CHAINMAIL_CHESTPLATE, MaterialTier.CHAINMAIL);
        register(Items.CHAINMAIL_LEGGINGS, MaterialTier.CHAINMAIL);
        register(Items.CHAINMAIL_BOOTS, MaterialTier.CHAINMAIL);

        register(Items.IRON_HELMET, MaterialTier.IRON);
        register(Items.IRON_CHESTPLATE, MaterialTier.IRON);
        register(Items.IRON_LEGGINGS, MaterialTier.IRON);
        register(Items.IRON_BOOTS, MaterialTier.IRON);

        register(Items.GOLDEN_HELMET, MaterialTier.GOLD);
        register(Items.GOLDEN_CHESTPLATE, MaterialTier.GOLD);
        register(Items.GOLDEN_LEGGINGS, MaterialTier.GOLD);
        register(Items.GOLDEN_BOOTS, MaterialTier.GOLD);

        register(Items.DIAMOND_HELMET, MaterialTier.DIAMOND);
        register(Items.DIAMOND_CHESTPLATE, MaterialTier.DIAMOND);
        register(Items.DIAMOND_LEGGINGS, MaterialTier.DIAMOND);
        register(Items.DIAMOND_BOOTS, MaterialTier.DIAMOND);

        register(Items.NETHERITE_HELMET, MaterialTier.NETHERITE);
        register(Items.NETHERITE_CHESTPLATE, MaterialTier.NETHERITE);
        register(Items.NETHERITE_LEGGINGS, MaterialTier.NETHERITE);
        register(Items.NETHERITE_BOOTS, MaterialTier.NETHERITE);
    }

    private static void register(Item item, MaterialTier tier) {
        ITEM_MATERIAL_MAP.put(item, tier);
    }

    public static boolean isVanillaGear(Item item) {
        return ITEM_MATERIAL_MAP.containsKey(item);
    }

    public static boolean isVanillaTool(Item item) {
        return isVanillaGear(item) && (item instanceof SwordItem || item instanceof DiggerItem);
    }

    public static boolean isVanillaArmor(Item item) {
        return isVanillaGear(item) && item instanceof ArmorItem;
    }

    public static MaterialTier getMaterialTier(Item item) {
        return ITEM_MATERIAL_MAP.get(item);
    }

    public static QualityTier getBaselineQuality(Item item) {
        MaterialTier tier = ITEM_MATERIAL_MAP.get(item);
        return tier != null ? tier.getBaselineQuality() : null;
    }

    public static QualityTier getQualityFromStack(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("QualityTier")) {
                return QualityTier.fromLevel(tag.getInt("QualityTier"));
            }
        }
        return null;
    }

    public static void setQualityOnStack(ItemStack stack, QualityTier quality) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("QualityTier", quality.getLevel());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public record ScrapOutput(Item item, int minCount, int maxCount) {}

    public static List<ScrapOutput> getScrapYields(MaterialTier tier) {
        return switch (tier) {
            case WOOD -> List.of(
                    new ScrapOutput(Items.STICK, 2, 4),
                    new ScrapOutput(ModItems.IRON_SCRAP.get(), 0, 1)
            );
            case LEATHER -> List.of(
                    new ScrapOutput(Items.LEATHER, 1, 3),
                    new ScrapOutput(Items.STRING, 1, 2)
            );
            case STONE -> List.of(
                    new ScrapOutput(Items.COBBLESTONE, 1, 3),
                    new ScrapOutput(Items.STICK, 1, 2)
            );
            case CHAINMAIL -> List.of(
                    new ScrapOutput(ModItems.IRON_SCRAP.get(), 2, 4),
                    new ScrapOutput(Items.STRING, 1, 2)
            );
            case GOLD -> List.of(
                    new ScrapOutput(Items.GOLD_NUGGET, 3, 6),
                    new ScrapOutput(ModItems.IRON_SCRAP.get(), 1, 2)
            );
            case IRON -> List.of(
                    new ScrapOutput(ModItems.IRON_SCRAP.get(), 3, 6),
                    new ScrapOutput(ModItems.MECHANICAL_PARTS.get(), 0, 1)
            );
            case DIAMOND -> List.of(
                    new ScrapOutput(ModItems.FORGED_IRON.get(), 2, 4),
                    new ScrapOutput(ModItems.MECHANICAL_PARTS.get(), 1, 2)
            );
            case NETHERITE -> List.of(
                    new ScrapOutput(ModItems.FORGED_STEEL.get(), 2, 4),
                    new ScrapOutput(ModItems.MECHANICAL_PARTS.get(), 1, 3)
            );
        };
    }
}
