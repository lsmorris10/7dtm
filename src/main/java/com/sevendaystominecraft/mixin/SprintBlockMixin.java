package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class SprintBlockMixin {

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void sevendaystominecraft$blockSprintWhenExhausted(boolean sprinting, CallbackInfo ci) {
        if (!sprinting) return;
        if (!((Object) this instanceof Player player)) return;

        if (player.hasData(ModAttachments.PLAYER_STATS.get())) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            if (stats.isStaminaExhausted()
                    || stats.getStamina() <= 0
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_FRACTURE)
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_SPRAIN)
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED)
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_STUNNED)) {
                ci.cancel();
            }
        }
    }
}
