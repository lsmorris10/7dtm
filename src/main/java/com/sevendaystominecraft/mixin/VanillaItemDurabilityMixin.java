package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.item.QualityTier;
import com.sevendaystominecraft.item.VanillaGearMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class VanillaItemDurabilityMixin {

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void sevendaystominecraft$scaleVanillaDurability(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Item self = (Item) (Object) this;
        if (!VanillaGearMaterials.isVanillaGear(self)) return;

        QualityTier quality = VanillaGearMaterials.getQualityFromStack(stack);
        if (quality == null || quality.getStatMultiplier() == 1.0f) return;

        int baseDurability = cir.getReturnValue();
        int scaled = Math.max(1, Math.round(baseDurability * quality.getStatMultiplier()));
        cir.setReturnValue(scaled);
    }
}
