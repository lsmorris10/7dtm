package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class WaterBottleConversionHandler {

    /**
     * Intercept right-clicking with a glass bottle.
     * If the player is looking at water, cancel the vanilla fill behavior
     * and give them a Murky Water Bottle instead.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = player.level();
        InteractionHand hand = event.getHand();
        ItemStack heldStack = player.getItemInHand(hand);

        // Only care about glass bottles
        if (!heldStack.is(Items.GLASS_BOTTLE)) return;

        // Perform a ray trace to see if the player is looking at a water block
        BlockHitResult hitResult = Item_getPlayerPOVHitResult(level, player);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos hitPos = hitResult.getBlockPos();
        if (!level.getFluidState(hitPos).is(FluidTags.WATER)) return;

        // Cancel the vanilla bottle-fill behavior
        event.setCanceled(true);

        // Server-side only: give the player a Murky Water Bottle
        if (level.isClientSide()) return;

        // Play the vanilla bottle-fill sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);

        // Consume one glass bottle, give one murky water bottle
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        ItemStack murkyWater = new ItemStack(ModItems.MURKY_WATER.get(), 1);
        if (!player.getInventory().add(murkyWater)) {
            player.drop(murkyWater, false);
        }

        // Swing the arm for visual feedback
        player.swing(hand, true);
    }

    /**
     * Replicates the vanilla Item.getPlayerPOVHitResult logic for liquid-aware ray tracing.
     */
    private static BlockHitResult Item_getPlayerPOVHitResult(Level level, Player player) {
        float pitch = player.getXRot();
        float yaw = player.getYRot();
        var eyePos = player.getEyePosition(1.0F);
        float cosYaw = (float) Math.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float sinYaw = (float) Math.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float cosPitch = (float) -Math.cos(-pitch * ((float) Math.PI / 180F));
        float sinPitch = (float) Math.sin(-pitch * ((float) Math.PI / 180F));
        float dx = sinYaw * cosPitch;
        float dz = cosYaw * cosPitch;
        double reach = player.blockInteractionRange();
        var endPos = eyePos.add((double) dx * reach, (double) sinPitch * reach, (double) dz * reach);
        return level.clip(new ClipContext(eyePos, endPos,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
    }

    /**
     * Utility to check if a stack is a vanilla water bottle (used by recipes/other systems).
     */
    public static boolean isVanillaWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
        return potionContents != null
                && potionContents.potion().isPresent()
                && potionContents.potion().get().is(Potions.WATER);
    }
}
