package com.sevendaystominecraft.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class VanillaGearHandler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack stack = event.getItemEntity().getItem();
        tryAssignBaselineQuality(stack);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        tryAssignBaselineQuality(stack);
    }

    private static void tryAssignBaselineQuality(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!VanillaGearMaterials.isVanillaGear(stack.getItem())) return;

        QualityTier existing = VanillaGearMaterials.getQualityFromStack(stack);
        if (existing != null) return;

        QualityTier baseline = VanillaGearMaterials.getBaselineQuality(stack.getItem());
        if (baseline != null) {
            VanillaGearMaterials.setQualityOnStack(stack, baseline);
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!VanillaGearMaterials.isVanillaGear(stack.getItem())) return;

        QualityTier quality = VanillaGearMaterials.getQualityFromStack(stack);
        if (quality == null || quality.getStatMultiplier() == 1.0f) return;

        float multiplier = quality.getStatMultiplier();

        if (VanillaGearMaterials.isVanillaTool(stack.getItem())) {
            applyToolAttributeScaling(event, multiplier);
        }

        if (VanillaGearMaterials.isVanillaArmor(stack.getItem())) {
            applyArmorAttributeScaling(event, stack, multiplier);
        }
    }

    private static void applyToolAttributeScaling(ItemAttributeModifierEvent event, float multiplier) {
        var modifiers = event.getModifiers();

        double baseDamage = 0;
        for (var entry : modifiers) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                baseDamage += entry.modifier().amount();
            }
        }

        if (baseDamage > 0) {
            double bonusDamage = baseDamage * (multiplier - 1.0f);
            event.addModifier(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "quality_damage_bonus"),
                            bonusDamage,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        }
    }

    private static void applyArmorAttributeScaling(ItemAttributeModifierEvent event, ItemStack stack, float multiplier) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        var modifiers = event.getModifiers();

        double baseArmor = 0;
        double baseToughness = 0;
        for (var entry : modifiers) {
            if (entry.attribute().equals(Attributes.ARMOR)
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                baseArmor += entry.modifier().amount();
            }
            if (entry.attribute().equals(Attributes.ARMOR_TOUGHNESS)
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                baseToughness += entry.modifier().amount();
            }
        }

        if (baseArmor > 0) {
            double bonusArmor = baseArmor * (multiplier - 1.0f);
            event.addModifier(
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "quality_armor_" + itemId),
                            bonusArmor,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.ARMOR
            );
        }

        if (baseToughness > 0) {
            double bonusToughness = baseToughness * (multiplier - 1.0f);
            event.addModifier(
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "quality_toughness_" + itemId),
                            bonusToughness,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.ARMOR
            );
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        if (tool.isEmpty()) return;
        if (!VanillaGearMaterials.isVanillaTool(tool.getItem())) return;

        QualityTier quality = VanillaGearMaterials.getQualityFromStack(tool);
        if (quality == null || quality.getStatMultiplier() == 1.0f) return;

        event.setNewSpeed(event.getNewSpeed() * quality.getStatMultiplier());
    }

    @EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) return;

            // Quality tooltip for vanilla gear
            if (VanillaGearMaterials.isVanillaGear(stack.getItem())) {
                QualityTier quality = VanillaGearMaterials.getQualityFromStack(stack);
                if (quality != null) {
                    if (!event.getToolTip().isEmpty()) {
                        Component originalName = event.getToolTip().getFirst();
                        event.getToolTip().set(0, quality.applyToName(originalName));
                    }
                    int insertIdx = Math.min(1, event.getToolTip().size());
                    event.getToolTip().add(insertIdx, Component.literal("")
                            .append(Component.literal("Quality: " + quality.getDisplayName())
                                    .withStyle(quality.getColor())));
                }
            }

            // Mod-specific vanilla item descriptions
            if (stack.is(Items.GOLDEN_APPLE)) {
                event.getToolTip().add(Component.literal("§aCures all debuffs"));
                event.getToolTip().add(Component.literal("§bGrants temporary sprint buff"));
            } else if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                event.getToolTip().add(Component.literal("§aCures all debuffs"));
                event.getToolTip().add(Component.literal("§bGrants extended sprint buff"));
            } else if (stack.is(Items.HONEY_BOTTLE)) {
                event.getToolTip().add(Component.literal("§aCures Infection I"));
            } else if (stack.is(Items.ROTTEN_FLESH)) {
                event.getToolTip().add(Component.literal("§cCauses Dysentery"));
            } else if (stack.is(Items.MILK_BUCKET)) {
                event.getToolTip().add(Component.literal("§aClears all potion effects"));
                event.getToolTip().add(Component.literal("§aCures Dysentery, Burn"));
            }
        }
    }
}

