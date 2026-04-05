package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Set;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class TerritoryEventHandler {

    private static final String TAG_PREFIX = "bzhs_territory_";

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        Set<String> tags = entity.getTags();
        for (String tag : tags) {
            if (tag.startsWith(TAG_PREFIX)) {
                String idStr = tag.substring(TAG_PREFIX.length());
                try {
                    int territoryId = Integer.parseInt(idStr);
                    handleZombieDeath(serverLevel, territoryId);
                } catch (NumberFormatException ignored) {
                }
                break;
            }
        }
    }

    private static void handleZombieDeath(ServerLevel level, int territoryId) {
        TerritoryData data = TerritoryData.getOrCreate(level);
        TerritoryRecord record = data.getTerritoryById(territoryId);
        if (record == null || record.isCleared()) return;

        record.decrementZombies();
        data.markDirtyRecord();

        if (record.isCleared()) {
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Territory] Territory #{} ({} {}) has been CLEARED!",
                    record.getId(), record.getType().getDisplayName(), record.getTier().getStars());
        }
    }
}
