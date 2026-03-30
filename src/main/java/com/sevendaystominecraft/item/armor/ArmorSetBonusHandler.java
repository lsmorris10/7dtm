package com.sevendaystominecraft.item.armor;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.QualityTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class ArmorSetBonusHandler {

    private static final ResourceLocation ARMOR_SPEED_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "armor_speed_modifier");
    private static final ResourceLocation LIGHT_ARMOR_PERK_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "light_armor_perk_bonus");
    private static final ResourceLocation QUALITY_ARMOR_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "quality_armor_bonus");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.tickCount % 10 != 0) return;

        ArmorCounts counts = computeArmorCounts(player);
        float speedMod = calculateMovementModifier(counts, serverPlayer);
        applyMovementModifier(player, speedMod);
        applyLightArmorPerkProtection(serverPlayer, counts);
        applyQualityArmorBonus(serverPlayer);
    }

    public static ArmorCounts computeArmorCounts(Player player) {
        int lightCount = 0;
        int mediumCount = 0;
        int heavyCount = 0;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof TieredArmorItem tieredArmor)) continue;
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) continue;

            switch (tieredArmor.getArmorTier()) {
                case LIGHT -> lightCount++;
                case MEDIUM -> mediumCount++;
                case HEAVY -> heavyCount++;
            }
        }

        return new ArmorCounts(lightCount, mediumCount, heavyCount);
    }

    private static float calculateMovementModifier(ArmorCounts counts, ServerPlayer player) {
        float totalMod = 0.0f;

        totalMod += counts.medium * ArmorTier.MEDIUM.getMovementModifier();
        totalMod += counts.heavy * ArmorTier.HEAVY.getMovementModifier();

        if (counts.heavy > 0) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            int heavyArmorRank = stats.getPerkRank("heavy_armor");
            if (heavyArmorRank > 0) {
                float penaltyReduction = 0.15f * heavyArmorRank;
                float heavyPenalty = counts.heavy * Math.abs(ArmorTier.HEAVY.getMovementModifier());
                float reduction = Math.min(heavyPenalty, heavyPenalty * penaltyReduction);
                totalMod += reduction;
            }
        }

        return totalMod;
    }

    private static void applyMovementModifier(Player player, float modifier) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        if (Math.abs(modifier) < 0.001f) {
            if (speedAttr.hasModifier(ARMOR_SPEED_MOD_ID)) {
                speedAttr.removeModifier(ARMOR_SPEED_MOD_ID);
            }
            return;
        }

        if (speedAttr.hasModifier(ARMOR_SPEED_MOD_ID)) {
            AttributeModifier existing = speedAttr.getModifier(ARMOR_SPEED_MOD_ID);
            if (existing != null && Math.abs(existing.amount() - modifier) < 0.001) {
                return;
            }
            speedAttr.removeModifier(ARMOR_SPEED_MOD_ID);
        }

        speedAttr.addTransientModifier(new AttributeModifier(
                ARMOR_SPEED_MOD_ID,
                modifier,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private static void applyLightArmorPerkProtection(ServerPlayer player, ArmorCounts counts) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        if (counts.light <= 0) {
            if (armorAttr.hasModifier(LIGHT_ARMOR_PERK_BONUS_ID)) {
                armorAttr.removeModifier(LIGHT_ARMOR_PERK_BONUS_ID);
            }
            return;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        int lightArmorRank = stats.getPerkRank("light_armor");
        if (lightArmorRank <= 0) {
            if (armorAttr.hasModifier(LIGHT_ARMOR_PERK_BONUS_ID)) {
                armorAttr.removeModifier(LIGHT_ARMOR_PERK_BONUS_ID);
            }
            return;
        }

        float bonusPerPiece = 0.10f * lightArmorRank;
        float totalLightArmor = 0.0f;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof TieredArmorItem tiered)) continue;
            if (tiered.getArmorTier() != ArmorTier.LIGHT) continue;
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) continue;
            int defenseForSlot = ModArmorMaterials.SCRAP_IRON.defense().getOrDefault(tiered.getArmorType(), 0);
            totalLightArmor += defenseForSlot;
        }

        float perkBonus = totalLightArmor * bonusPerPiece;

        if (armorAttr.hasModifier(LIGHT_ARMOR_PERK_BONUS_ID)) {
            AttributeModifier existing = armorAttr.getModifier(LIGHT_ARMOR_PERK_BONUS_ID);
            if (existing != null && Math.abs(existing.amount() - perkBonus) < 0.01) {
                return;
            }
            armorAttr.removeModifier(LIGHT_ARMOR_PERK_BONUS_ID);
        }

        if (perkBonus > 0.0f) {
            armorAttr.addTransientModifier(new AttributeModifier(
                    LIGHT_ARMOR_PERK_BONUS_ID,
                    perkBonus,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    private static void applyQualityArmorBonus(ServerPlayer player) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        float qualityBonus = 0.0f;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof TieredArmorItem tiered)) continue;
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) continue;

            QualityTier quality = getQualityTier(stack);
            if (quality != null && quality.getStatMultiplier() != 1.0f) {
                int baseDefense = getBaseDefense(tiered);
                float bonus = baseDefense * (quality.getStatMultiplier() - 1.0f);
                qualityBonus += bonus;
            }
        }

        if (Math.abs(qualityBonus) < 0.01f) {
            if (armorAttr.hasModifier(QUALITY_ARMOR_BONUS_ID)) {
                armorAttr.removeModifier(QUALITY_ARMOR_BONUS_ID);
            }
            return;
        }

        if (armorAttr.hasModifier(QUALITY_ARMOR_BONUS_ID)) {
            AttributeModifier existing = armorAttr.getModifier(QUALITY_ARMOR_BONUS_ID);
            if (existing != null && Math.abs(existing.amount() - qualityBonus) < 0.01) {
                return;
            }
            armorAttr.removeModifier(QUALITY_ARMOR_BONUS_ID);
        }

        armorAttr.addTransientModifier(new AttributeModifier(
                QUALITY_ARMOR_BONUS_ID,
                qualityBonus,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        EquipmentSlot slot = event.getSlot();
        if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST
                && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        if (from.isEmpty()) return;
        if (!(from.getItem() instanceof TieredArmorItem)) return;
        if (!to.isEmpty()) return;

        if (from.getMaxDamage() > 0 && from.getDamageValue() >= from.getMaxDamage()) {
            String itemName = from.getHoverName().getString();
            player.sendSystemMessage(Component.literal(
                    "§c§l[ARMOR SHATTERED] §r§c" + itemName + " has broken! It provides no protection."));
        }
    }

    private static int getBaseDefense(TieredArmorItem tiered) {
        return switch (tiered.getArmorTier()) {
            case LIGHT -> ModArmorMaterials.SCRAP_IRON.defense().getOrDefault(tiered.getArmorType(), 0);
            case MEDIUM -> ModArmorMaterials.SCRAP_IRON.defense().getOrDefault(tiered.getArmorType(), 0);
            case HEAVY -> ModArmorMaterials.MILITARY.defense().getOrDefault(tiered.getArmorType(), 0);
        };
    }

    public static QualityTier getQualityTier(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("QualityTier")) {
                return QualityTier.fromLevel(tag.getInt("QualityTier"));
            }
        }
        return null;
    }

    public static float getNoiseMultiplier(Player player) {
        ArmorCounts counts = computeArmorCounts(player);
        if (counts.light <= 0) return 1.0f;

        if (counts.light >= 4) {
            return 0.0f;
        }

        float reductionPerPiece = ArmorTier.LIGHT.getStealthReductionPerPiece();

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        int lightArmorRank = stats.getPerkRank("light_armor");
        float perkBonus = lightArmorRank > 0 ? (0.10f * lightArmorRank) : 0.0f;

        float totalReduction = counts.light * (reductionPerPiece + (reductionPerPiece * perkBonus));

        return Math.max(0.0f, 1.0f - totalReduction);
    }

    public static float getStaminaRegenMultiplier(Player player) {
        ArmorCounts counts = computeArmorCounts(player);

        float bonus = 0.0f;
        if (counts.medium >= 4) {
            bonus = 0.20f;
        }

        if (bonus > 0.0f) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            int mediumArmorRank = stats.getPerkRank("medium_armor");
            if (mediumArmorRank > 0) {
                bonus += 0.05f * mediumArmorRank;
            }
        }

        return 1.0f + bonus;
    }

    public static float getDamageReductionMultiplier(Player player) {
        ArmorCounts counts = computeArmorCounts(player);
        if (counts.heavy >= 4) {
            return 0.75f;
        }
        return 1.0f;
    }

    public record ArmorCounts(int light, int medium, int heavy) {}
}
