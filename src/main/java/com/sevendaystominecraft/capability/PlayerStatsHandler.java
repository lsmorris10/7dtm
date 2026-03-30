package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.item.armor.ArmorSetBonusHandler;
import com.sevendaystominecraft.network.SyncPlayerStatsPayload;
import com.sevendaystominecraft.perk.Attribute;
import com.sevendaystominecraft.worldgen.BiomeProperties;

import net.minecraft.core.Holder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class PlayerStatsHandler {

    private static final ResourceLocation STARVATION_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "starvation_slowdown");
    private static final ResourceLocation SPRAIN_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sprain_slowdown");
    private static final ResourceLocation FRACTURE_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "fracture_slowdown");
    private static final ResourceLocation HYPOTHERMIA_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "hypothermia_slowdown");
    private static final ResourceLocation FREEZE_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "freeze_slowdown");
    private static final ResourceLocation CARDIO_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "cardio_speed_bonus");
    private static final ResourceLocation GOLDEN_APPLE_SPRINT_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "golden_apple_sprint_buff");
    private static final ResourceLocation ARMOR_SPEED_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "armor_speed_modifier");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        cleanupHeavyWeaponModifier(player);

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        if (player.tickCount % 20 == 0) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS DEBUG] {} | Food: {}/{} | Water: {}/{} | Stamina: {}/{} | Sprinting: {} | Temp: {}°F | Lvl: {} XP: {}",
                    player.getName().getString(),
                    String.format("%.1f", stats.getFood()), String.format("%.1f", stats.getMaxFood()),
                    String.format("%.1f", stats.getWater()), String.format("%.1f", stats.getMaxWater()),
                    String.format("%.1f", stats.getStamina()), String.format("%.1f", stats.getMaxStamina()),
                    player.isSprinting(),
                    String.format("%.1f", stats.getCoreTemperature()),
                    stats.getLevel(), stats.getXp()
            );
        }

        // ── 1. Passive Food/Water Drain ─────────────────────────────────
        float foodDrainPerTick = (float) (cfg.foodDrainPerMinute.get() / 1200.0);
        float waterDrainPerTick = (float) (cfg.waterDrainPerMinute.get() / 1200.0);

        if (player.isSprinting()) {
            foodDrainPerTick *= cfg.foodDrainActivityMultiplier.get().floatValue();
        }

        boolean nearHeatSource = false;
        if (player.tickCount % 20 == 0) {
            nearHeatSource = hasNearbyHeatSource(player);
            player.getPersistentData().putBoolean("sevendtm_near_heat", nearHeatSource);
        } else {
            nearHeatSource = player.getPersistentData().getBoolean("sevendtm_near_heat");
        }
        float ambientTemp = estimateAmbientTemperature(player, nearHeatSource);
        if (ambientTemp > 85.0f) {
            waterDrainPerTick *= cfg.waterDrainDesertMultiplier.get().floatValue();
        }

        stats.setFood(stats.getFood() - foodDrainPerTick);
        stats.setWater(stats.getWater() - waterDrainPerTick);

        // Sync mod hunger to vanilla every tick so vanilla food items can be eaten
        // Also zero saturation to prevent vanilla's own health regen (we handle regen ourselves)
        int vanillaFoodLevel = (int) Math.floor(stats.getFood());
        player.getFoodData().setFoodLevel(vanillaFoodLevel);
        player.getFoodData().setSaturation(0.0f);

        // ── 2. Stamina system removed — vanilla sprinting restored ─────

        // ── 3. Starvation / Dehydration Cascade (§1.1) ─────────────────
        float foodPct = (stats.getMaxFood() > 0) ? (stats.getFood() / stats.getMaxFood()) * 100f : 0f;
        float waterPct = (stats.getMaxWater() > 0) ? (stats.getWater() / stats.getMaxWater()) * 100f : 0f;
        float worstPct = Math.min(foodPct, waterPct);

        if (worstPct <= 0) {
            float drain = (float) (cfg.cascadeHealthDrainFast.get() / 20.0);
            serverPlayer.hurtServer((ServerLevel) player.level(), player.damageSources().starve(), drain);
            applySpeedPenalty(player, cfg.cascadeSpeedPenalty.get().floatValue());
        } else if (worstPct < cfg.cascadeThreshold2.get().floatValue()) {
            float drain = (float) (cfg.cascadeHealthDrainSlow.get() / 20.0);
            serverPlayer.hurtServer((ServerLevel) player.level(), player.damageSources().starve(), drain);
            removeSpeedPenalty(player);
        } else {
            removeSpeedPenalty(player);
        }

        // ── 4. Health Regen ─────────────────────────────────────────────
        if (foodPct > cfg.healthRegenFoodThreshold.get().floatValue()
                && waterPct > cfg.healthRegenWaterThreshold.get().floatValue()) {
            float healPerTick = (float) (cfg.healthRegenRate.get() / 20.0);

            int healingFactorRank = stats.getPerkRank("healing_factor");
            if (healingFactorRank > 0) {
                healPerTick *= (1.0f + 0.20f * healingFactorRank);
            }

            player.heal(healPerTick);
        }

        // ── 5. Core Temperature ─────────────────────────────────────────
        float currentTemp = stats.getCoreTemperature();
        float adjustRate = (float) (cfg.tempAdjustRate.get() / 20.0);

        float comfortExpansion = stats.getPerkRank("well_insulated") * 10.0f;
        float effectiveAmbient = ambientTemp;
        if (comfortExpansion > 0) {
            float baselineTemp = 70.0f;
            if (effectiveAmbient < baselineTemp) {
                effectiveAmbient = Math.min(baselineTemp, effectiveAmbient + comfortExpansion);
            } else if (effectiveAmbient > baselineTemp) {
                effectiveAmbient = Math.max(baselineTemp, effectiveAmbient - comfortExpansion);
            }
        }

        if (currentTemp < effectiveAmbient) {
            stats.setCoreTemperature(Math.min(effectiveAmbient, currentTemp + adjustRate));
        } else if (currentTemp > effectiveAmbient) {
            stats.setCoreTemperature(Math.max(effectiveAmbient, currentTemp - adjustRate));
        }

        float updatedTemp = stats.getCoreTemperature();

        if (updatedTemp < cfg.hypothermiaThreshold.get().floatValue()) {
            stats.setColdExposureTicks(stats.getColdExposureTicks() + 1);
            if (stats.getColdExposureTicks() >= cfg.temperatureExposureTicks.get()
                    && !stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA)) {
                stats.addDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA, Integer.MAX_VALUE);
            }
        } else {
            stats.setColdExposureTicks(0);
        }

        if (updatedTemp > cfg.hyperthermiaThreshold.get().floatValue()) {
            stats.setHeatExposureTicks(stats.getHeatExposureTicks() + 1);
            if (stats.getHeatExposureTicks() >= cfg.temperatureExposureTicks.get()
                    && !stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA)) {
                stats.addDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA, Integer.MAX_VALUE);
            }
        } else {
            stats.setHeatExposureTicks(0);
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA)
                && updatedTemp > cfg.hypothermiaClearThreshold.get().floatValue()) {
            stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA);
            stats.setColdExposureTicks(0);
        }
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA)
                && updatedTemp < cfg.hyperthermiaClearThreshold.get().floatValue()) {
            stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA);
            stats.setHeatExposureTicks(0);
        }

        // ── 6. Tick Debuffs ─────────────────────────────────────────────
        stats.tickDebuffs();
        applyDebuffEffects(player, stats);

        // ── 6b. Smell Tracking (3.0 Preview) ────────────────────────────
        if (com.sevendaystominecraft.config.ZombieConfig.INSTANCE.smellTrackingEnabled.get()) {
            com.sevendaystominecraft.smell.SmellTracker.onPlayerTick(serverPlayer);
        }

        // ── 7. Heatmap Noise (§1.3) ─────────────────────────────────────
        if (player.isSprinting()) {
            com.sevendaystominecraft.heatmap.HeatEventHandler.onPlayerSprint(
                    serverPlayer.serverLevel(), player);
        }

        // ── 8. Perk: Rule 1 Cardio sprint speed bonus ───────────────────
        applyCardioSpeedBonus(player, stats);

        // ── 8b. Golden Apple Sprint Buff ─────────────────────────────────
        tickGoldenAppleSprintBuff(serverPlayer, stats);

        // ── 9. Sync to Client (throttled) ───────────────────────────────
        int syncInterval = cfg.syncIntervalTicks.get();
        if (player.tickCount % syncInterval == 0) {
            sendStatsToClient(serverPlayer, stats);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            if (event.getOriginal().hasData(ModAttachments.PLAYER_STATS.get())) {
                SevenDaysPlayerStats oldStats = event.getOriginal().getData(ModAttachments.PLAYER_STATS.get());
                SevenDaysPlayerStats newStats = event.getEntity().getData(ModAttachments.PLAYER_STATS.get());
                newStats.copyFrom(oldStats);

                newStats.setFood(newStats.getMaxFood());
                newStats.setWater(newStats.getMaxWater());
                newStats.setStamina(newStats.getMaxStamina());

                boolean keepInventory = event.getEntity().level() instanceof ServerLevel sl
                        && sl.getServer().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY);
                if (!keepInventory) {
                    newStats.setEquippedCoinBag(ItemStack.EMPTY);
                }

                clearAllDebuffs(event.getEntity(), newStats);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            com.sevendaystominecraft.network.ModNetworking.sendCoinBagToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeathDropCoinBag(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!serverPlayer.hasData(ModAttachments.PLAYER_STATS.get())) return;

        boolean keepInventory = serverPlayer.server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY);

        SevenDaysPlayerStats stats = serverPlayer.getData(ModAttachments.PLAYER_STATS.get());
        ItemStack coinBag = stats.getEquippedCoinBag();
        if (!coinBag.isEmpty() && !keepInventory) {
            com.sevendaystominecraft.entity.DeadBodyEventHandler.stashCoinBag(serverPlayer.getUUID(), coinBag.copy());
        }
    }

    public static void clearAllDebuffs(Player player, SevenDaysPlayerStats stats) {
        for (String id : SevenDaysPlayerStats.KNOWN_DEBUFF_IDS) {
            stats.removeDebuff(id);
        }
        stats.setColdExposureTicks(0);
        stats.setHeatExposureTicks(0);
        stats.setGoldenAppleSprintBuffTicks(0);

        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPRAIN_SLOWDOWN_ID);
            speedAttr.removeModifier(FRACTURE_SLOWDOWN_ID);
            speedAttr.removeModifier(HYPOTHERMIA_SLOWDOWN_ID);
            speedAttr.removeModifier(FREEZE_SLOWDOWN_ID);
            speedAttr.removeModifier(GOLDEN_APPLE_SPRINT_ID);
        }

        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ItemStack stack = event.getItem();
        SevenDaysPlayerStats stats = serverPlayer.getData(ModAttachments.PLAYER_STATS.get());
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;
        boolean changed = false;

        FoodProperties food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food != null) {
            float restoration = food.nutrition() * cfg.foodRestorationMultiplier.get().floatValue();
            stats.setFood(Math.min(stats.getMaxFood(), stats.getFood() + restoration));
            changed = true;
        }

        boolean isDrink = (stack.is(Items.POTION) && !WaterBottleConversionHandler.isVanillaWaterBottle(stack))
                || stack.is(Items.MILK_BUCKET)
                || stack.is(Items.MUSHROOM_STEW)
                || stack.is(Items.BEETROOT_SOUP)
                || stack.is(Items.RABBIT_STEW)
                || stack.is(Items.SUSPICIOUS_STEW);
        if (isDrink) {
            float waterAmount = cfg.waterPerDrink.get().floatValue();
            stats.setWater(Math.min(stats.getMaxWater(), stats.getWater() + waterAmount));
            changed = true;
        }

        if (stack.is(Items.ROTTEN_FLESH)) {
            stats.addDebuff(SevenDaysPlayerStats.DEBUFF_DYSENTERY, cfg.dysenteryDuration.get());
            changed = true;
        }


        if (stack.is(Items.MILK_BUCKET)) {
            stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_DYSENTERY);
            stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_BURN);
            changed = true;
        }

        if (stack.is(Items.HONEY_BOTTLE)) {
            if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1)) {
                stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1);
                changed = true;
            }
        }

        if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            clearAllDebuffs(serverPlayer, stats);
            changed = true;

            int duration = stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                    ? SurvivalConfig.INSTANCE.enchantedGoldenAppleSprintDuration.get()
                    : SurvivalConfig.INSTANCE.goldenAppleSprintDuration.get();
            stats.setGoldenAppleSprintBuffTicks(duration);
            applyGoldenAppleSprintBuff(serverPlayer);
            int durationSec = duration / 20;
            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§a[GOLDEN APPLE] Light on your feet! (" + durationSec + "s)"));
        }

        if (changed) {
            sendStatsToClient(serverPlayer, stats);
        }
    }

    @SubscribeEvent
    public static void onPlayerDamageZombie(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof BaseSevenDaysZombie)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        float multiplier = SurvivalConfig.INSTANCE.playerDamageMultiplier.get().floatValue();
        if (multiplier != 1.0f) {
            event.setNewDamage(event.getNewDamage() * multiplier);
        }

        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof net.minecraft.world.item.SwordItem) {
                com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                        com.sevendaystominecraft.sound.ModSounds.MELEE_SWING, player,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8f,
                        0.9f + player.getRandom().nextFloat() * 0.2f);
                com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                        com.sevendaystominecraft.sound.ModSounds.MELEE_HIT, event.getEntity(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f,
                        0.9f + player.getRandom().nextFloat() * 0.2f);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SevenDaysPlayerStats stats = serverPlayer.getData(ModAttachments.PLAYER_STATS.get());
            sendStatsToClient(serverPlayer, stats);
            com.sevendaystominecraft.quest.QuestSyncHelper.syncQuests(serverPlayer);
            com.sevendaystominecraft.network.ModNetworking.sendWaypointsToPlayer(serverPlayer);
            com.sevendaystominecraft.network.ModNetworking.sendCoinBagToClient(serverPlayer);
            SevenDaysToMinecraft.LOGGER.debug("BZHS: Synced player stats to {} on login", serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            com.sevendaystominecraft.smell.SmellTracker.removePlayer(event.getEntity().getUUID());
        }
    }


    public static void sendStatsToClient(ServerPlayer player, SevenDaysPlayerStats stats) {
        int[] attrLevels = new int[Attribute.values().length];
        for (Attribute attr : Attribute.values()) {
            attrLevels[attr.ordinal()] = stats.getAttributeLevel(attr);
        }
        PacketDistributor.sendToPlayer(player, new SyncPlayerStatsPayload(
                stats.getFood(), stats.getMaxFood(),
                stats.getWater(), stats.getMaxWater(),
                stats.getStamina(), stats.getMaxStamina(),
                stats.isStaminaExhausted(),
                stats.getCoreTemperature(),
                stats.getDebuffs(),
                stats.getXp(), stats.getLevel(),
                stats.getPerkPoints(), stats.getAttributePoints(),
                stats.getActivePerks(),
                attrLevels,
                stats.getUnkillableCooldownEnd()
        ));
    }

    public static float getDamageReductionMultiplier(SevenDaysPlayerStats stats) {
        int painRank = stats.getPerkRank("pain_tolerance");
        if (painRank > 0) {
            return 1.0f - (0.10f * painRank);
        }
        return 1.0f;
    }

    public static float getMiningSpeedMultiplier(SevenDaysPlayerStats stats) {
        int minerRank = stats.getPerkRank("deep_striker");
        if (minerRank > 0) {
            return 1.0f + (0.15f * minerRank);
        }
        return 1.0f;
    }

    private static float getStaminaCostMultiplier(SevenDaysPlayerStats stats) {
        int unstoppableRank = stats.getPerkRank("unstoppable_force");
        if (unstoppableRank > 0) {
            return 1.0f - (0.15f * unstoppableRank);
        }
        return 1.0f;
    }

    private static void applyCardioSpeedBonus(Player player, SevenDaysPlayerStats stats) {
        int cardioRank = stats.getPerkRank("rule1_cardio");
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        if (cardioRank > 0) {
            float bonus = 0.05f * cardioRank;
            if (attribute.hasModifier(CARDIO_SPEED_ID)) {
                var existing = attribute.getModifier(CARDIO_SPEED_ID);
                if (existing != null && Math.abs(existing.amount() - bonus) > 0.001) {
                    attribute.removeModifier(CARDIO_SPEED_ID);
                    attribute.addTransientModifier(new AttributeModifier(
                            CARDIO_SPEED_ID,
                            bonus,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ));
                }
            } else {
                attribute.addTransientModifier(new AttributeModifier(
                        CARDIO_SPEED_ID,
                        bonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        } else {
            if (attribute.hasModifier(CARDIO_SPEED_ID)) {
                attribute.removeModifier(CARDIO_SPEED_ID);
            }
        }
    }

    private static void tickGoldenAppleSprintBuff(ServerPlayer player, SevenDaysPlayerStats stats) {
        if (!stats.hasGoldenAppleSprintBuff()) return;

        int remaining = stats.getGoldenAppleSprintBuffTicks() - 1;
        stats.setGoldenAppleSprintBuffTicks(remaining);

        if (remaining > 0) {
            applyGoldenAppleSprintBuff(player);
            if (remaining % 20 == 0) {
                int secondsLeft = remaining / 20;
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§a⚡ Sprint Buff: " + secondsLeft + "s"),
                        true);
            }
        } else {
            removeGoldenAppleSprintBuff(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§c[GOLDEN APPLE] Sprint buff expired!"));
        }
    }

    private static void applyGoldenAppleSprintBuff(Player player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        double negativeProduct = 1.0;

        var armorMod = speedAttr.getModifier(ARMOR_SPEED_MOD_ID);
        if (armorMod != null && armorMod.amount() < 0) {
            negativeProduct *= (1.0 + armorMod.amount());
        }

        var starvMod = speedAttr.getModifier(STARVATION_SLOWDOWN_ID);
        if (starvMod != null && starvMod.amount() < 0) {
            negativeProduct *= (1.0 + starvMod.amount());
        }

        var heavyWeaponMod = speedAttr.getModifier(HEAVY_WEAPON_SLOWDOWN_ID);
        if (heavyWeaponMod != null && heavyWeaponMod.amount() < 0) {
            negativeProduct *= (1.0 + heavyWeaponMod.amount());
        }

        if (negativeProduct >= 0.999) {
            if (speedAttr.hasModifier(GOLDEN_APPLE_SPRINT_ID)) {
                speedAttr.removeModifier(GOLDEN_APPLE_SPRINT_ID);
            }
            return;
        }

        double buffAmount = (1.0 / negativeProduct) - 1.0;

        if (speedAttr.hasModifier(GOLDEN_APPLE_SPRINT_ID)) {
            var existing = speedAttr.getModifier(GOLDEN_APPLE_SPRINT_ID);
            if (existing != null && Math.abs(existing.amount() - buffAmount) < 0.001) {
                return;
            }
            speedAttr.removeModifier(GOLDEN_APPLE_SPRINT_ID);
        }

        speedAttr.addTransientModifier(new AttributeModifier(
                GOLDEN_APPLE_SPRINT_ID,
                buffAmount,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private static void removeGoldenAppleSprintBuff(Player player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.hasModifier(GOLDEN_APPLE_SPRINT_ID)) {
            speedAttr.removeModifier(GOLDEN_APPLE_SPRINT_ID);
        }
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    private static void applyStaminaRegen(SevenDaysPlayerStats stats, SurvivalConfig cfg, float regenPerTick) {
        float foodPct = (stats.getMaxFood() > 0) ? (stats.getFood() / stats.getMaxFood()) * 100f : 0f;
        float waterPct = (stats.getMaxWater() > 0) ? (stats.getWater() / stats.getMaxWater()) * 100f : 0f;

        if (foodPct < cfg.cascadeThreshold1.get().floatValue()
                || waterPct < cfg.cascadeThreshold1.get().floatValue()) {
            regenPerTick *= 0.5f;
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1)) {
            regenPerTick *= cfg.infection1StaminaRegenMult.get().floatValue();
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2)) {
            regenPerTick *= cfg.infection2StaminaRegenMult.get().floatValue();
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA)) {
            regenPerTick *= cfg.hyperthermiaStaminaRegenMult.get().floatValue();
        }

        int cardioRank = stats.getPerkRank("rule1_cardio");
        if (cardioRank > 0) {
            regenPerTick *= (1.0f + 0.10f * cardioRank);
        }

        stats.setStamina(stats.getStamina() + regenPerTick);
    }

    private static void applyDebuffEffects(Player player, SevenDaysPlayerStats stats) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        if (stats.isInfection1Expired()) {
            stats.clearInfection1Expired();
            if (!stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2)) {
                stats.addDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2, Integer.MAX_VALUE);
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_BLEEDING)) {
            if (player.tickCount % 20 == 0) {
                player.hurtServer(serverLevel, player.damageSources().magic(),
                        cfg.bleedingDamagePerSec.get().floatValue());
                // Stamina drain removed — vanilla sprinting
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1)) {
            int interval = cfg.infection1HpDrainInterval.get();
            if (player.tickCount % interval == 0) {
                player.hurtServer(serverLevel, player.damageSources().magic(),
                        cfg.infection1HpDrain.get().floatValue());
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2)) {
            if (player.tickCount % 20 == 0) {
                player.hurtServer(serverLevel, player.damageSources().magic(),
                        cfg.infection2DamagePerSec.get().floatValue());
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_BURN)) {
            if (player.isInWater()) {
                stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_BURN);
            } else if (player.tickCount % 20 == 0) {
                player.hurtServer(serverLevel, player.damageSources().onFire(),
                        cfg.burnDamagePerSec.get().floatValue());
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_DYSENTERY)) {
            float extraWater = (float) (cfg.waterDrainPerMinute.get() * 2.0 / 1200.0);
            stats.setWater(stats.getWater() - extraWater);
            float extraFood = (float) (cfg.foodDrainPerMinute.get() / 1200.0);
            stats.setFood(stats.getFood() - extraFood);
            if (player.tickCount % 100 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false, true));
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA)) {
            int interval = cfg.hypothermiaHpDrainInterval.get();
            if (player.tickCount % interval == 0) {
                player.hurtServer(serverLevel, player.damageSources().magic(),
                        cfg.hypothermiaHpDrain.get().floatValue());
            }
        }

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA)) {
            int interval = cfg.hyperthermiaHpDrainInterval.get();
            if (player.tickCount % interval == 0) {
                player.hurtServer(serverLevel, player.damageSources().magic(),
                        cfg.hyperthermiaHpDrain.get().floatValue());
            }
        }

        boolean frozen = stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED)
                || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_STUNNED);
        applyDebuffModifier(player, FREEZE_SLOWDOWN_ID, frozen, -1.0f);

        if (frozen && player.isSprinting()) {
            player.setSprinting(false);
        }

        boolean sprained = stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_SPRAIN)
                && !stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_FRACTURE);
        applyDebuffModifier(player, SPRAIN_SLOWDOWN_ID, sprained, -0.3f);
        if (sprained && player.isSprinting()) {
            player.setSprinting(false);
        }

        boolean fractured = stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_FRACTURE);
        applyDebuffModifier(player, FRACTURE_SLOWDOWN_ID, fractured, -0.6f);
        if (fractured && player.isSprinting()) {
            player.setSprinting(false);
        }

        boolean hypothermic = stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA);
        applyDebuffModifier(player, HYPOTHERMIA_SLOWDOWN_ID, hypothermic, -0.2f);

        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_CONCUSSION)) {
            if (!player.hasEffect(MobEffects.CONFUSION)) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false, true));
            }
            if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false, true));
            }
        }
    }

    private static void applyDebuffModifier(Player player, ResourceLocation id, boolean active, float amount) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        if (active && !attribute.hasModifier(id)) {
            attribute.addTransientModifier(new AttributeModifier(
                    id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!active && attribute.hasModifier(id)) {
            attribute.removeModifier(id);
        }
    }

    private static void applySpeedPenalty(Player player, float penalty) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && !attribute.hasModifier(STARVATION_SLOWDOWN_ID)) {
            attribute.addTransientModifier(new AttributeModifier(
                    STARVATION_SLOWDOWN_ID,
                    -penalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private static void removeSpeedPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.hasModifier(STARVATION_SLOWDOWN_ID)) {
            attribute.removeModifier(STARVATION_SLOWDOWN_ID);
        }
    }

    private static boolean isPlayerMoving(Player player) {
        return player.getDeltaMovement().horizontalDistanceSqr() > 0.0001;
    }

    private static float estimateAmbientTemperature(Player player, boolean nearHeatSource) {
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;
        Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
        BiomeProperties.BiomeStats biomeStats = BiomeProperties.getStats(biomeHolder);

        long dayTimeFull = player.level().getDayTime() % SevenDaysConstants.DAY_LENGTH;
        float dayFraction = (float) dayTimeFull / SevenDaysConstants.DAY_LENGTH;
        float fahrenheit = biomeStats.ambientTemperature(dayFraction);

        double altitude = player.getY();
        if (altitude > 64) {
            fahrenheit -= (float) ((altitude - 64) / 10.0);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = player.blockPosition();
            if (serverLevel.isRaining() && serverLevel.canSeeSky(pos)) {
                Biome biome = serverLevel.getBiome(pos).value();
                Biome.Precipitation precip = biome.getPrecipitationAt(pos, serverLevel.getSeaLevel());
                if (precip == Biome.Precipitation.SNOW) {
                    fahrenheit += cfg.snowTempModifier.get().floatValue();
                } else if (precip == Biome.Precipitation.RAIN) {
                    fahrenheit += cfg.rainTempModifier.get().floatValue();
                }
            }
        }

        if (player.isInWater()) {
            fahrenheit += cfg.waterTempModifier.get().floatValue();
        }

        float undergroundTarget = cfg.undergroundNormTemp.get().floatValue();
        if (player.getY() < 40) {
            fahrenheit = fahrenheit + (undergroundTarget - fahrenheit) * 0.5f;
        }

        if (nearHeatSource) {
            fahrenheit += cfg.fireTempBonus.get().floatValue();
        }

        return fahrenheit;
    }

    private static boolean hasNearbyHeatSource(Player player) {
        BlockPos center = player.blockPosition();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = player.level().getBlockState(checkPos);
                    if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                        return true;
                    }
                    if (state.getBlock() instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
                        return true;
                    }
                    if (state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)) {
                        if (state.hasProperty(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT)
                                && state.getValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT)) {
                            return true;
                        }
                    }
                    if (state.is(Blocks.LAVA)) {
                        return true;
                    }
                    if (state.is(ModBlocks.FORGE_BLOCK.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static final ResourceLocation HEAVY_WEAPON_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "heavy_weapon_slowdown");

    private static void cleanupHeavyWeaponModifier(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        boolean holdingHeavyWeapon = mainHand.getItem() instanceof com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem gun
                && gun.getMovementPenaltyMultiplier() < 1.0f;
        if (!holdingHeavyWeapon) {
            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.getModifier(HEAVY_WEAPON_SLOWDOWN_ID) != null) {
                speedAttr.removeModifier(HEAVY_WEAPON_SLOWDOWN_ID);
            }
        }
    }
}
