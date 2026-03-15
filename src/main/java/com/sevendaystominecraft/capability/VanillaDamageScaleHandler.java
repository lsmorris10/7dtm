package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class VanillaDamageScaleHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onVanillaDamage(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();

        if (event.getEntity() instanceof ServerPlayer) {
            if (isVanillaEnvironmentalDamage(source)) {
                float scale = SurvivalConfig.INSTANCE.vanillaDamageScale.get().floatValue();
                if (scale != 1.0f) {
                    event.setNewDamage(event.getNewDamage() * scale);
                }
                return;
            }

            if (isVanillaMobDamage(source)) {
                float scale = SurvivalConfig.INSTANCE.vanillaDamageScale.get().floatValue();
                if (scale != 1.0f) {
                    event.setNewDamage(event.getNewDamage() * scale);
                }
                return;
            }
        }

        if (isPlayerSourced(source)) {
            float scale = SurvivalConfig.INSTANCE.playerDamageScale.get().floatValue();
            if (scale != 1.0f) {
                event.setNewDamage(event.getNewDamage() * scale);
            }
        }
    }

    private static boolean isVanillaMobDamage(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) return false;
        if (attacker instanceof Player) return false;
        if (attacker instanceof BaseSevenDaysZombie) return false;

        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof Player) return false;
            if (owner instanceof BaseSevenDaysZombie) return false;
        }

        return attacker instanceof LivingEntity;
    }

    private static boolean isPlayerSourced(DamageSource source) {
        if (source.getEntity() instanceof Player) return true;

        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile) {
            return projectile.getOwner() instanceof Player;
        }

        return false;
    }

    private static boolean isVanillaEnvironmentalDamage(DamageSource source) {
        return source.is(DamageTypes.FALL)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.STARVE)
                || source.is(DamageTypes.FLY_INTO_WALL)
                || source.is(DamageTypes.CRAMMING)
                || source.is(DamageTypes.FALLING_STALACTITE)
                || source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.FALLING_BLOCK);
    }
}
