package com.sevendaystominecraft.stealth;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class NoiseEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer)) return;

        if (player.tickCount % 10 != 0) return;

        Vec3 pos = player.position();

        if (player.isSprinting()) {
            NoiseManager.addNoise(player.getUUID(), pos, NoiseManager.NOISE_SPRINTING);
        } else if (player.isCrouching()) {
            NoiseManager.addNoise(player.getUUID(), pos, NoiseManager.NOISE_CROUCHING);
        } else if (player.getDeltaMovement().horizontalDistanceSqr() > 0.003) {
            NoiseManager.addNoise(player.getUUID(), pos, NoiseManager.NOISE_WALKING);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (player.level().isClientSide()) return;

        Vec3 pos = new Vec3(
                event.getPos().getX() + 0.5,
                event.getPos().getY() + 0.5,
                event.getPos().getZ() + 0.5
        );
        NoiseManager.addNoise(player.getUUID(), pos, NoiseManager.NOISE_BLOCK_BREAK);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 explosionPos = new Vec3(
                event.getExplosion().center().x,
                event.getExplosion().center().y,
                event.getExplosion().center().z
        );

        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(explosionPos) <= 50.0 * 50.0) {
                NoiseManager.addNoise(player.getUUID(), explosionPos, NoiseManager.NOISE_EXPLOSION);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NoiseManager.clearPlayer(event.getEntity().getUUID());
    }

    public static void onGunshot(Player player) {
        if (player == null || player.level().isClientSide()) return;
        NoiseManager.addNoise(player.getUUID(), player.position(), NoiseManager.NOISE_GUNSHOT);
    }
}
