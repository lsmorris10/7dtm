package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TraderConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class TraderProtectionHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        BlockPos pos = event.getEntity().blockPosition();
        int protectionRadius = TraderConfig.INSTANCE.protectionRadius.get();
        TraderData data = TraderData.getOrCreate(serverLevel);

        if (data.isInProtectionZone(pos, protectionRadius)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        BlockPos pos = event.getPos();
        int protectionRadius = TraderConfig.INSTANCE.protectionRadius.get();
        TraderData data = TraderData.getOrCreate(serverLevel);

        if (data.isInProtectionZone(pos, protectionRadius)) {
            event.setCanceled(true);
        }
    }
}
