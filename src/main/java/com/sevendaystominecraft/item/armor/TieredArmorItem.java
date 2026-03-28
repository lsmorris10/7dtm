package com.sevendaystominecraft.item.armor;

import com.sevendaystominecraft.item.QualityTier;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.List;

public class TieredArmorItem extends ArmorItem {

    private final ArmorTier armorTier;
    private final ArmorType armorType;

    public TieredArmorItem(ArmorMaterial material, ArmorType type, Properties properties, ArmorTier tier) {
        super(material, type, properties);
        this.armorTier = tier;
        this.armorType = type;
    }

    public ArmorTier getArmorTier() {
        return armorTier;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component originalName = super.getName(stack);
        QualityTier quality = getQuality(stack);
        if (quality != null) {
            return quality.applyToName(originalName);
        }
        return originalName;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        int baseDurability = super.getMaxDamage(stack);
        QualityTier quality = getQuality(stack);
        if (quality != null) {
            return Math.max(1, Math.round(baseDurability * quality.getStatMultiplier()));
        }
        return baseDurability;
    }

    private static QualityTier getQuality(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("QualityTier")) {
                return QualityTier.fromLevel(tag.getInt("QualityTier"));
            }
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        QualityTier quality = getQuality(stack);
        if (quality != null) {
            tooltipComponents.add(Component.literal("")
                    .append(Component.literal("Quality: " + quality.getDisplayName())
                            .withStyle(quality.getColor())));
        }

        tooltipComponents.add(Component.literal("§7Tier: §f" + armorTier.getDisplayName()));

        if (armorTier == ArmorTier.LIGHT) {
            tooltipComponents.add(Component.literal("§a-10% noise per piece"));
        } else if (armorTier == ArmorTier.MEDIUM) {
            tooltipComponents.add(Component.literal("§e-5% movement speed"));
        } else if (armorTier == ArmorTier.HEAVY) {
            tooltipComponents.add(Component.literal("§c-15% movement speed"));
        }

        tooltipComponents.add(Component.literal("§dSet Bonus (4pc): " + getFullSetBonusDescription()));
    }

    private String getFullSetBonusDescription() {
        return switch (armorTier) {
            case LIGHT -> "Silent Movement";
            case MEDIUM -> "+20% Stamina Regen";
            case HEAVY -> "+25% Damage Reduction";
        };
    }
}
