package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSprintMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void sevendaystominecraft$cancelClientSprint(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (!self.isSprinting()) return;

        if (self.hasData(ModAttachments.PLAYER_STATS.get())) {
            SevenDaysPlayerStats stats = self.getData(ModAttachments.PLAYER_STATS.get());
            if (stats.isStaminaExhausted()
                    || stats.getStamina() <= 0
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_FRACTURE)
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_SPRAIN)
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED)
                    || stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_STUNNED)) {
                self.setSprinting(false);
            }
        }
    }
}
