package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class LandClaimProtectionHandler {

    private static final float PVP_BREAK_SPEED_MULTIPLIER = 0.25f;

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        java.util.Optional<BlockPos> optPos = event.getPosition();
        if (optPos.isEmpty()) return;

        BlockPos breakPos = optPos.get();
        LandClaimData data = LandClaimData.getOrCreate(serverLevel);
        Map<UUID, BlockPos> claims = data.getAllClaims();

        for (Map.Entry<UUID, BlockPos> entry : claims.entrySet()) {
            if (entry.getKey().equals(player.getUUID())) continue;

            if (LandClaimBlock.isWithinClaimRadius(entry.getValue(), breakPos)) {
                event.setNewSpeed(event.getNewSpeed() * PVP_BREAK_SPEED_MULTIPLIER);
                return;
            }
        }
    }
}
