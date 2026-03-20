package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.entity.zombie.CopZombie;
import com.sevendaystominecraft.entity.zombie.DemolisherZombie;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class ExplosionHandler {

    private static final double CONCUSSION_RADIUS = 3.0;
    private static final int STUNNED_DURATION = 40;

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Vec3 center = explosion.center();
        Entity sourceEntity = explosion.getDirectSourceEntity();
        boolean isZombieBlast = sourceEntity instanceof CopZombie || sourceEntity instanceof DemolisherZombie;

        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        AABB searchArea = new AABB(
                center.x - CONCUSSION_RADIUS, center.y - CONCUSSION_RADIUS, center.z - CONCUSSION_RADIUS,
                center.x + CONCUSSION_RADIUS, center.y + CONCUSSION_RADIUS, center.z + CONCUSSION_RADIUS
        );
        List<Player> nearbyPlayers = event.getLevel().getEntitiesOfClass(Player.class, searchArea);

        for (Player player : nearbyPlayers) {
            if (player.level().isClientSide()) continue;
            if (!player.hasData(ModAttachments.PLAYER_STATS.get())) continue;

            double dist = player.position().distanceTo(center);
            if (dist > CONCUSSION_RADIUS) continue;

            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            stats.addDebuff(SevenDaysPlayerStats.DEBUFF_CONCUSSION, cfg.concussionDuration.get());

            if (isZombieBlast) {
                applyFreeze(stats, STUNNED_DURATION);
            }
        }
    }

    private static void applyFreeze(SevenDaysPlayerStats stats, int duration) {
        int electrocutedRemaining = stats.getDebuffs().getOrDefault(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED, 0);
        int stunnedRemaining = stats.getDebuffs().getOrDefault(SevenDaysPlayerStats.DEBUFF_STUNNED, 0);
        int currentMax = Math.max(electrocutedRemaining, stunnedRemaining);

        if (duration > currentMax) {
            if (electrocutedRemaining > 0) stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED);
            if (stunnedRemaining > 0) stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_STUNNED);
            stats.addDebuff(SevenDaysPlayerStats.DEBUFF_STUNNED, duration);
        }
    }
}
