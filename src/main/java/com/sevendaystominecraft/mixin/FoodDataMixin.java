package com.sevendaystominecraft.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into {@link FoodData} to disable vanilla hunger and allow eating at all times.
 *
 * Spec §1.1: "Override FoodData entirely" — all food/saturation/exhaustion
 * logic is replaced by our custom system in {@link com.sevendaystominecraft.capability.PlayerStatsHandler}.
 *
 * <h3>What this does:</h3>
 * <ul>
 *   <li>Cancels the vanilla FoodData.tick() method at HEAD</li>
 *   <li>Prevents vanilla hunger bar depletion, saturation calculations,
 *       and natural regen tied to foodLevel</li>
 *   <li>Forces canEat() to always return true so vanilla food items can be
 *       consumed regardless of the frozen food level</li>
 *   <li>Our custom drain/regen runs in PlayerTickEvent instead</li>
 * </ul>
 *
 * <h3>MC 1.21.4 note:</h3>
 * In MC 1.21.4, FoodData.tick() takes a {@link ServerPlayer} parameter
 * (not {@link net.minecraft.world.entity.player.Player}). The mixin
 * callback must match this exact signature.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    /**
     * Inject at the HEAD of tick() and cancel — all vanilla food logic
     * is replaced by our custom system.
     *
     * @param player the ServerPlayer (MC 1.21.4 signature)
     * @param ci callback info — cancellable
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sevendaystominecraft$cancelVanillaTick(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void sevendaystominecraft$alwaysCanEat(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
