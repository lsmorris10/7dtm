package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.config.SurvivalConfig;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into {@link LivingEntity#heal(float)} to gate natural healing
 * on food/water thresholds from spec §1.1.
 *
 * <h3>IMPORTANT: Why LivingEntity, not Player?</h3>
 * The {@code heal(float)} method is declared in {@link LivingEntity}, not
 * {@link Player}. Mixin only sees methods declared directly on the target
 * class, not inherited methods. So we must target LivingEntity and filter
 * to Player instances inside the method body.
 *
 * <h3>Spec §1.1 Rule:</h3>
 * Health regen (0.5/sec) only when Food > 50% AND Water > 50%.
 * Below these thresholds, the heal call is cancelled.
 */
@Mixin(LivingEntity.class)
public abstract class PlayerHealMixin {

    /**
     * Cancel heal if this is a player and their food or water is below threshold.
     *
     * @param amount the amount to heal
     * @param ci callback info — cancellable
     */
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void sevendaystominecraft$gateHealOnStats(float amount, CallbackInfo ci) {
        // Only apply to Player entities
        if (!((Object) this instanceof Player player)) return;

        // Client-side: don't interfere
        if (player.level().isClientSide()) return;

        // Check if our stats data is attached
        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        float foodPct = (stats.getMaxFood() > 0)
                ? (stats.getFood() / stats.getMaxFood()) * 100f
                : 0f;
        float waterPct = (stats.getMaxWater() > 0)
                ? (stats.getWater() / stats.getMaxWater()) * 100f
                : 0f;

        // Block heal if either food or water below threshold
        if (foodPct < cfg.healthRegenFoodThreshold.get().floatValue()
                || waterPct < cfg.healthRegenWaterThreshold.get().floatValue()) {
            ci.cancel();
        }
    }
}
