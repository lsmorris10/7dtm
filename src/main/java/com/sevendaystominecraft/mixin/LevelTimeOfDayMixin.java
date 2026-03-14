package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.SevenDaysConstants;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelTimeOfDayMixin {

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void sevendaystominecraft$stretchDayCycle(float partialTick, CallbackInfoReturnable<Float> cir) {
        Level self = (Level) (Object) this;
        if (self.dimension() == Level.OVERWORLD) {
            double smoothTime = (double) self.getDayTime() + (double) partialTick;
            double d = Mth.frac(smoothTime / (double) SevenDaysConstants.DAY_LENGTH - 0.25);
            double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
            float timeOfDay = (float) (d * 2.0 + e) / 3.0F;
            cir.setReturnValue(timeOfDay);
        }
    }
}
