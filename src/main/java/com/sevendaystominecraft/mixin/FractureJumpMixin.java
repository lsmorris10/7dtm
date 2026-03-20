package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class FractureJumpMixin {

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void sevendaystominecraft$dampenJumpWhenFractured(CallbackInfo ci) {
        if (!((Object) this instanceof Player player)) return;

        if (player.hasData(ModAttachments.PLAYER_STATS.get())) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_FRACTURE)) {
                net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, motion.y * 0.4, motion.z);
            }
        }
    }
}
