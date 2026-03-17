package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, value = Dist.CLIENT)
public class MusicManager {

    public enum MusicContext {
        NONE,
        DAY,
        NIGHT,
        COMBAT,
        BLOOD_MOON
    }

    private static final long NIGHT_START_TIME = 12541L;
    private static final long DAY_START_TIME = 23459L;

    private static final int COMBAT_DETECTION_RADIUS = 24;
    private static final int COMBAT_GRACE_PERIOD_TICKS = 200;
    private static final int CONTEXT_SWITCH_COOLDOWN_TICKS = 40;

    private static MusicContext currentContext = MusicContext.NONE;
    private static MusicContext pendingContext = null;
    private static FadeableMusicSound currentTrack = null;
    private static int lastCombatTick = Integer.MIN_VALUE / 2;
    private static int cooldownTicker = 0;
    private static int tickCount = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (event.getEntity() != mc.player) return;

        tickCount++;

        SoundManager soundManager = mc.getSoundManager();

        if (currentTrack != null && currentTrack.isFadingOut()) {
            if (currentTrack.isDoneFading() || !soundManager.isActive(currentTrack)) {
                soundManager.stop(currentTrack);
                currentTrack = null;
                if (pendingContext != null) {
                    applyContext(mc, pendingContext);
                    pendingContext = null;
                }
            }
            return;
        }

        if (cooldownTicker > 0) {
            cooldownTicker--;
            return;
        }

        MusicContext desired = determineContext(mc);
        if (desired != currentContext) {
            if (currentTrack != null && soundManager.isActive(currentTrack)) {
                pendingContext = desired;
                currentTrack.startFadeOut();
            } else {
                if (currentTrack != null) {
                    soundManager.stop(currentTrack);
                    currentTrack = null;
                }
                applyContext(mc, desired);
            }
        }
    }

    private static void applyContext(Minecraft mc, MusicContext newContext) {
        currentContext = newContext;
        cooldownTicker = CONTEXT_SWITCH_COOLDOWN_TICKS;

        Supplier<SoundEvent> soundSupplier = getSoundForContext(newContext);
        if (soundSupplier == null) {
            SevenDaysToMinecraft.LOGGER.debug("[MusicManager] Context: {}, deferring to vanilla music", newContext);
            return;
        }

        SoundEvent soundEvent = soundSupplier.get();
        if (soundEvent == null) return;

        currentTrack = new FadeableMusicSound(soundEvent);
        mc.getSoundManager().play(currentTrack);
        SevenDaysToMinecraft.LOGGER.debug("[MusicManager] Started music context: {}", newContext);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Entity attacker = event.getSource().getEntity();
        Entity target = event.getEntity();

        boolean playerAttacked = target == mc.player && attacker instanceof Monster;
        boolean playerAttacking = attacker == mc.player && target instanceof Monster;

        if (playerAttacked || playerAttacking) {
            lastCombatTick = tickCount;
        }
    }

    private static MusicContext determineContext(Minecraft mc) {
        ResourceKey<Level> dimension = mc.level.dimension();
        if (dimension != Level.OVERWORLD) {
            return MusicContext.NONE;
        }

        if (BloodMoonClientState.isActive()) {
            return MusicContext.BLOOD_MOON;
        }

        if (isInCombat(mc)) {
            return MusicContext.COMBAT;
        }

        long timeOfDay = mc.level.getDayTime() % 24000L;
        if (timeOfDay >= NIGHT_START_TIME && timeOfDay < DAY_START_TIME) {
            return MusicContext.NIGHT;
        }

        return MusicContext.DAY;
    }

    private static boolean isInCombat(Minecraft mc) {
        if (tickCount - lastCombatTick < COMBAT_GRACE_PERIOD_TICKS) {
            return true;
        }

        Player player = mc.player;
        Level level = mc.level;
        AABB searchBox = new AABB(
                player.getX() - COMBAT_DETECTION_RADIUS, player.getY() - COMBAT_DETECTION_RADIUS, player.getZ() - COMBAT_DETECTION_RADIUS,
                player.getX() + COMBAT_DETECTION_RADIUS, player.getY() + COMBAT_DETECTION_RADIUS, player.getZ() + COMBAT_DETECTION_RADIUS
        );
        List<Monster> nearby = level.getEntitiesOfClass(Monster.class, searchBox,
                mob -> mob.getTarget() == player && mob.isAlive());
        return !nearby.isEmpty();
    }

    private static Supplier<SoundEvent> getSoundForContext(MusicContext context) {
        return switch (context) {
            case DAY -> ModSounds.MUSIC_EXPLORATION_DAY;
            case NIGHT -> ModSounds.MUSIC_EXPLORATION_NIGHT;
            case BLOOD_MOON -> ModSounds.MUSIC_BLOOD_MOON;
            case COMBAT -> ModSounds.MUSIC_COMBAT;
            case NONE -> null;
        };
    }

    public static void reset() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getSoundManager() != null && currentTrack != null) {
            mc.getSoundManager().stop(currentTrack);
        }
        currentTrack = null;
        currentContext = MusicContext.NONE;
        pendingContext = null;
        lastCombatTick = Integer.MIN_VALUE / 2;
        cooldownTicker = 0;
        tickCount = 0;
    }
}
