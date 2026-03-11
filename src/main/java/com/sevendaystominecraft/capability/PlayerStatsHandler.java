package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.network.SyncPlayerStatsPayload;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side tick handler for the custom player stats system.
 *
 * Runs every tick on the server for each player and manages:
 * <ul>
 *   <li>Food/Water passive drain (§1.1)</li>
 *   <li>Stamina drain (sprint/jump/mining) and regen (rest/walking)</li>
 *   <li>Health regen gated on food/water thresholds (§1.1)</li>
 *   <li>Starvation/dehydration cascade effects (§1.1)</li>
 *   <li>Core temperature adjustment toward ambient (§1.1)</li>
 *   <li>Debuff tick-down (§1.2)</li>
 *   <li>Debuff effects (bleeding damage, infection drain, etc.)</li>
 *   <li>Movement speed penalty application via attribute modifiers</li>
 *   <li>Periodic client sync via SyncPlayerStatsPayload (manual PacketDistributor)</li>
 * </ul>
 *
 * Listens on the NeoForge game event bus (not mod bus).
 */
@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class PlayerStatsHandler {

    // ResourceLocation for the movement speed modifier we manage
    private static final ResourceLocation STARVATION_SLOWDOWN_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "starvation_slowdown");

    // Tick counter key — we store it transiently (doesn't need persistence)
    // We use the player's tick count (tickCount) modulo for throttling

    /**
     * Fires every server tick for each player.
     * Uses {@code PlayerTickEvent.Post} to run after vanilla tick logic.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Server-side only
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        // ── DEBUG: Log stats every second (20 ticks) for testing ─────────
        if (player.tickCount % 20 == 0) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[7DTM DEBUG] {} | Food: {}/{} | Water: {}/{} | Stamina: {}/{} | Sprinting: {} | Temp: {}°F",
                    player.getName().getString(),
                    String.format("%.1f", stats.getFood()), String.format("%.1f", stats.getMaxFood()),
                    String.format("%.1f", stats.getWater()), String.format("%.1f", stats.getMaxWater()),
                    String.format("%.1f", stats.getStamina()), String.format("%.1f", stats.getMaxStamina()),
                    player.isSprinting(),
                    String.format("%.1f", stats.getCoreTemperature())
            );
        }

        // ── 1. Passive Food/Water Drain ─────────────────────────────────
        // Rates are per-minute in config; convert to per-tick (÷ 1200)
        float foodDrainPerTick = (float) (cfg.foodDrainPerMinute.get() / 1200.0);
        float waterDrainPerTick = (float) (cfg.waterDrainPerMinute.get() / 1200.0);

        // Activity multiplier: sprinting or mining increases drain
        if (player.isSprinting()) {
            foodDrainPerTick *= cfg.foodDrainActivityMultiplier.get().floatValue();
        }

        // Desert/hot biome multiplier for water
        // Simple check: if ambient temp > 85°F (desert range from spec §2.1)
        float ambientTemp = estimateAmbientTemperature(player);
        if (ambientTemp > 85.0f) {
            waterDrainPerTick *= cfg.waterDrainDesertMultiplier.get().floatValue();
        }

        stats.setFood(stats.getFood() - foodDrainPerTick);
        stats.setWater(stats.getWater() - waterDrainPerTick);

        // ── 2. Stamina Drain / Regen ────────────────────────────────────
        // Per-second values in config; convert to per-tick (÷ 20)

        // Check exhaustion recovery: clear exhausted flag when stamina > 40%
        // (Higher threshold prevents rapid stutter cycling)
        float staminaPct = (stats.getMaxStamina() > 0)
                ? (stats.getStamina() / stats.getMaxStamina()) * 100f : 0f;
        boolean wasExhausted = stats.isStaminaExhausted();
        if (wasExhausted && staminaPct >= 40f) {
            stats.setStaminaExhausted(false);
            SevenDaysToMinecraft.LOGGER.info("[7DTM] Stamina recovered to 40% — sprint available again");
            // Immediate sync so client SprintBlockMixin gets the update
            sendStatsToClient(serverPlayer, stats);
        }

        // Force-cancel sprint while exhausted (every tick)
        if (stats.isStaminaExhausted() && player.isSprinting()) {
            player.setSprinting(false);
        }

        if (player.isSprinting() && !stats.isStaminaExhausted()) {
            float drain = (float) (cfg.staminaDrainSprint.get() / 20.0);
            stats.setStamina(stats.getStamina() - drain);

            // Enter exhaustion mode when stamina depleted
            if (stats.getStamina() <= 0) {
                stats.setStaminaExhausted(true);
                player.setSprinting(false);
                // Immediate sync so client blocks sprint input right away
                sendStatsToClient(serverPlayer, stats);
                SevenDaysToMinecraft.LOGGER.info("[7DTM] Sprint cancelled — stamina exhausted! Must recover to 40%");
            }
        } else if (!stats.isStaminaExhausted() && isPlayerMoving(player)) {
            // Walking — regen at walking rate (only if NOT exhausted)
            float regenRate = (float) (cfg.staminaRegenWalking.get() / 20.0);
            applyStaminaRegen(stats, cfg, regenRate);
        } else if (!player.isSprinting()) {
            // At rest — regen at rest rate
            float regenRate = (float) (cfg.staminaRegenRest.get() / 20.0);
            applyStaminaRegen(stats, cfg, regenRate);
        }

        // Jump stamina drain — detect jump by checking vertical velocity spike
        // (fallDistance starts > 0 on the tick after jumping, and onGround was true last tick)
        // This is a simplified heuristic
        if (!player.onGround() && player.getDeltaMovement().y > 0.1 && player.fallDistance < 0.1f) {
            stats.setStamina(stats.getStamina() - cfg.staminaDrainJump.get().floatValue());
        }

        // ── 3. Starvation / Dehydration Cascade (§1.1) ─────────────────
        float foodPct = (stats.getMaxFood() > 0) ? (stats.getFood() / stats.getMaxFood()) * 100f : 0f;
        float waterPct = (stats.getMaxWater() > 0) ? (stats.getWater() / stats.getMaxWater()) * 100f : 0f;
        float worstPct = Math.min(foodPct, waterPct);

        // Cascade: food/water = 0 → heavy health drain + movement penalty
        if (worstPct <= 0) {
            float drain = (float) (cfg.cascadeHealthDrainFast.get() / 20.0);
            player.hurt(player.damageSources().starve(), drain);
            applySpeedPenalty(player, cfg.cascadeSpeedPenalty.get().floatValue());
        }
        // Cascade: food/water < 10% → slow health drain
        else if (worstPct < cfg.cascadeThreshold2.get().floatValue()) {
            float drain = (float) (cfg.cascadeHealthDrainSlow.get() / 20.0);
            player.hurt(player.damageSources().starve(), drain);
            removeSpeedPenalty(player);
        }
        // Above threshold 2 — remove speed penalty if present
        else {
            removeSpeedPenalty(player);
        }

        // ── 4. Health Regen ─────────────────────────────────────────────
        // Only when food > threshold AND water > threshold (spec §1.1)
        if (foodPct > cfg.healthRegenFoodThreshold.get().floatValue()
                && waterPct > cfg.healthRegenWaterThreshold.get().floatValue()) {
            float healPerTick = (float) (cfg.healthRegenRate.get() / 20.0);
            player.heal(healPerTick);
        }

        // ── 5. Core Temperature ─────────────────────────────────────────
        // Adjust core temp toward ambient at configured rate
        float currentTemp = stats.getCoreTemperature();
        float adjustRate = (float) (cfg.tempAdjustRate.get() / 20.0); // per tick
        if (currentTemp < ambientTemp) {
            stats.setCoreTemperature(Math.min(ambientTemp, currentTemp + adjustRate));
        } else if (currentTemp > ambientTemp) {
            stats.setCoreTemperature(Math.max(ambientTemp, currentTemp - adjustRate));
        }

        // ── 6. Tick Debuffs ─────────────────────────────────────────────
        stats.tickDebuffs();
        applyDebuffEffects(player, stats);

        // ── 7. Heatmap Noise (§1.3) ─────────────────────────────────────
        // Sprinting adds +0.2/sec heatmap noise (stub — actual heatmap not yet implemented)
        // This is a placeholder for future heatmap chunk capability
        if (player.isSprinting()) {
            // TODO: When HeatmapCapability is implemented:
            // ChunkAccess chunk = player.level().getChunk(player.blockPosition());
            // HeatmapCapability heatmap = chunk.getData(ModAttachments.HEATMAP.get());
            // heatmap.addHeat(0.2f / 20f, (ServerLevel) player.level());
        }

        // ── 8. Sync to Client (throttled) ───────────────────────────────
        // NeoForge 21.4.140 does not have built-in AttachmentSyncHandler.
        // We sync manually via SyncPlayerStatsPayload + PacketDistributor.
        int syncInterval = cfg.syncIntervalTicks.get();
        if (player.tickCount % syncInterval == 0) {
            sendStatsToClient(serverPlayer, stats);
        }
    }

    /**
     * Handle player clone (death/respawn, end dimension return).
     * Backup for copyOnDeath — ensures stats are properly copied even
     * if the attachment system's copyOnDeath doesn't fire correctly.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            if (event.getOriginal().hasData(ModAttachments.PLAYER_STATS.get())) {
                SevenDaysPlayerStats oldStats = event.getOriginal().getData(ModAttachments.PLAYER_STATS.get());
                SevenDaysPlayerStats newStats = event.getEntity().getData(ModAttachments.PLAYER_STATS.get());
                newStats.copyFrom(oldStats);

                // On respawn, reset certain stats to reasonable values
                // (player shouldn't respawn at 0 food/water)
                newStats.setFood(newStats.getMaxFood() * 0.5f);
                newStats.setWater(newStats.getMaxWater() * 0.5f);
                newStats.setStamina(newStats.getMaxStamina());
                // Clear all debuffs on respawn
                for (String id : SevenDaysPlayerStats.KNOWN_DEBUFF_IDS) {
                    newStats.removeDebuff(id);
                }
            }
        }
    }

    /**
     * Handle player login — force initial sync of stats to client.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SevenDaysPlayerStats stats = serverPlayer.getData(ModAttachments.PLAYER_STATS.get());
            sendStatsToClient(serverPlayer, stats);
            SevenDaysToMinecraft.LOGGER.debug("7DTM: Synced player stats to {} on login", serverPlayer.getName().getString());
        }
    }

    /**
     * Send stats to a specific player's client via CustomPacketPayload.
     * Uses PacketDistributor.sendToPlayer for targeted delivery.
     */
    private static void sendStatsToClient(ServerPlayer player, SevenDaysPlayerStats stats) {
        PacketDistributor.sendToPlayer(player, new SyncPlayerStatsPayload(
                stats.getFood(), stats.getMaxFood(),
                stats.getWater(), stats.getMaxWater(),
                stats.getStamina(), stats.getMaxStamina(),
                stats.isStaminaExhausted(),
                stats.getCoreTemperature(),
                stats.getDebuffs()
        ));
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    /**
     * Apply stamina regen, accounting for starvation cascade.
     * If Food < 30% or Water < 30%, stamina regen is halved (spec §1.1).
     */
    private static void applyStaminaRegen(SevenDaysPlayerStats stats, SurvivalConfig cfg, float regenPerTick) {
        float foodPct = (stats.getMaxFood() > 0) ? (stats.getFood() / stats.getMaxFood()) * 100f : 0f;
        float waterPct = (stats.getMaxWater() > 0) ? (stats.getWater() / stats.getMaxWater()) * 100f : 0f;

        // Halve regen if food OR water below cascade threshold 1
        if (foodPct < cfg.cascadeThreshold1.get().floatValue()
                || waterPct < cfg.cascadeThreshold1.get().floatValue()) {
            regenPerTick *= 0.5f;
        }

        stats.setStamina(stats.getStamina() + regenPerTick);
    }

    /**
     * Apply debuff damage/effects each tick.
     * Spec §1.2 — each debuff has specific effects.
     */
    private static void applyDebuffEffects(Player player, SevenDaysPlayerStats stats) {
        // Bleeding: −1 HP/3sec = −1/60 ticks ≈ −0.0167/tick
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_BLEEDING)) {
            if (player.tickCount % 60 == 0) { // every 3 seconds
                player.hurt(player.damageSources().magic(), 1.0f);
            }
        }

        // Infection Stage 2: −0.5 HP/sec = −0.025/tick
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2)) {
            if (player.tickCount % 20 == 0) { // every second
                player.hurt(player.damageSources().magic(), 0.5f);
            }
        }

        // Burn: −2 HP/sec while active
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_BURN)) {
            if (player.tickCount % 10 == 0) { // every 0.5 sec
                player.hurt(player.damageSources().onFire(), 1.0f);
            }
        }

        // Radiation: −1 HP/5sec
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_RADIATION)) {
            if (player.tickCount % 100 == 0) { // every 5 seconds
                player.hurt(player.damageSources().magic(), 1.0f);
            }
        }

        // Dysentery: water drains ×3, food drains ×2
        // (Applied as additional drain in tick — multiplicative effect)
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_DYSENTERY)) {
            SurvivalConfig cfg = SurvivalConfig.INSTANCE;
            // Extra ×2 water drain (on top of normal ×1 already applied = total ×3)
            float extraWater = (float) (cfg.waterDrainPerMinute.get() * 2.0 / 1200.0);
            stats.setWater(stats.getWater() - extraWater);
            // Extra ×1 food drain (on top of normal ×1 = total ×2)
            float extraFood = (float) (cfg.foodDrainPerMinute.get() / 1200.0);
            stats.setFood(stats.getFood() - extraFood);
        }

        // Hypothermia: stamina drain ×2 (extra drain)
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA)) {
            SurvivalConfig cfg = SurvivalConfig.INSTANCE;
            // Extra stamina drain equivalent to sprint drain
            float extraStamina = (float) (cfg.staminaDrainSprint.get() / 20.0);
            stats.setStamina(stats.getStamina() - extraStamina * 0.5f);
        }

        // Hyperthermia: water drain ×3 (extra ×2)
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA)) {
            SurvivalConfig cfg = SurvivalConfig.INSTANCE;
            float extraWater = (float) (cfg.waterDrainPerMinute.get() * 2.0 / 1200.0);
            stats.setWater(stats.getWater() - extraWater);
        }
    }

    /**
     * Apply movement speed penalty via attribute modifier.
     */
    private static void applySpeedPenalty(Player player, float penalty) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && !attribute.hasModifier(STARVATION_SLOWDOWN_ID)) {
            attribute.addTransientModifier(new AttributeModifier(
                    STARVATION_SLOWDOWN_ID,
                    -penalty, // negative = slower
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    /**
     * Remove starvation speed penalty.
     */
    private static void removeSpeedPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.hasModifier(STARVATION_SLOWDOWN_ID)) {
            attribute.removeModifier(STARVATION_SLOWDOWN_ID);
        }
    }

    /**
     * Check if the player is moving horizontally.
     */
    private static boolean isPlayerMoving(Player player) {
        return player.getDeltaMovement().horizontalDistanceSqr() > 0.0001;
    }

    /**
     * Estimate ambient temperature based on biome.
     * Placeholder — returns a simplified value from spec §2.1 biome table.
     * Full temperature system will be implemented in a later milestone.
     *
     * @return estimated ambient temperature in °F
     */
    private static float estimateAmbientTemperature(Player player) {
        // Use Minecraft's biome temperature as a rough proxy
        // MC biome temp is 0.0–2.0; map to °F range
        float biomeTemp = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();

        // Map MC biome temp to 7DTD range:
        // 0.0 (snowy) → 10°F, 0.5 (temperate) → 60°F, 1.0 (warm) → 85°F, 2.0 (desert) → 120°F
        float fahrenheit = 10.0f + (biomeTemp * 55.0f);

        // Time-of-day modifier: nighttime is cooler (−10°F)
        long dayTime = player.level().getDayTime() % 24000;
        if (dayTime > 13000 && dayTime < 23000) { // night
            fahrenheit -= 10.0f;
        }

        // Altitude modifier: higher = cooler (−1°F per 10 blocks above y=64)
        double altitude = player.getY();
        if (altitude > 64) {
            fahrenheit -= (float) ((altitude - 64) / 10.0);
        }

        return fahrenheit;
    }
}
