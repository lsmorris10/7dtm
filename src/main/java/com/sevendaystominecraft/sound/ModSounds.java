package com.sevendaystominecraft.sound;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<SoundEvent> ZOMBIE_GROAN = register("zombie_groan");
    public static final Supplier<SoundEvent> ZOMBIE_SCREAM = register("zombie_scream");
    public static final Supplier<SoundEvent> ZOMBIE_DEATH = register("zombie_death");
    public static final Supplier<SoundEvent> ZOMBIE_ALERT = register("zombie_alert");
    public static final Supplier<SoundEvent> ZOMBIE_ATTACK = register("zombie_attack");
    public static final Supplier<SoundEvent> ZOMBIE_HURT = register("zombie_hurt");
    public static final Supplier<SoundEvent> ZOMBIE_HORDE_ROAR = register("zombie_horde_roar");
    public static final Supplier<SoundEvent> GUN_FIRE_9MM = register("gun_fire_9mm");
    public static final Supplier<SoundEvent> GUN_FIRE_AK47 = register("gun_fire_ak47");
    public static final Supplier<SoundEvent> SHOTGUN_FIRE = register("shotgun_fire");
    public static final Supplier<SoundEvent> GUN_FIRE_SMG = register("gun_fire_smg");
    public static final Supplier<SoundEvent> GUN_FIRE_HUNTING_RIFLE = register("gun_fire_hunting_rifle");
    public static final Supplier<SoundEvent> GUN_FIRE_SNIPER_RIFLE = register("gun_fire_sniper_rifle");
    public static final Supplier<SoundEvent> GUN_FIRE_M60 = register("gun_fire_m60");
    public static final Supplier<SoundEvent> WEAPON_RELOAD = register("weapon_reload");
    public static final Supplier<SoundEvent> MELEE_SWING = register("melee_swing");
    public static final Supplier<SoundEvent> MELEE_HIT = register("melee_hit");
    public static final Supplier<SoundEvent> BLOOD_MOON_SIREN = register("blood_moon_siren");
    public static final Supplier<SoundEvent> WORKSTATION_AMBIENT = register("workstation_ambient");
    public static final Supplier<SoundEvent> BLOCK_BREAK_ZOMBIE = register("block_break_zombie");
    public static final Supplier<SoundEvent> ZOMBIE_SNIFF = register("zombie_sniff");
    public static final Supplier<SoundEvent> PLAYER_DRINK = register("player_drink");
    public static final Supplier<SoundEvent> PLAYER_EAT_COOKED = register("player_eat_cooked");
    public static final Supplier<SoundEvent> PLAYER_BANDAGE = register("player_bandage");
    public static final Supplier<SoundEvent> PLAYER_LEVELUP = register("player_levelup");
    public static final Supplier<SoundEvent> PLAYER_DEBUFF_APPLY = register("player_debuff_apply");
    public static final Supplier<SoundEvent> TERRITORY_ENTER = register("territory_enter");
    public static final Supplier<SoundEvent> LOOT_OPEN = register("loot_open");
    public static final Supplier<SoundEvent> CRAFT_COMPLETE = register("craft_complete");
    public static final Supplier<SoundEvent> QUEST_COMPLETE = register("quest_complete");
    public static final Supplier<SoundEvent> TRADER_BUY = register("trader_buy");

    public static final Supplier<SoundEvent> MUSIC_EXPLORATION_DAY = register("music_exploration_day");
    public static final Supplier<SoundEvent> MUSIC_EXPLORATION_NIGHT = register("music_exploration_night");
    public static final Supplier<SoundEvent> MUSIC_BLOOD_MOON = register("music_blood_moon");
    public static final Supplier<SoundEvent> MUSIC_COMBAT = register("music_combat");
    public static final Supplier<SoundEvent> MUSIC_TRADER_OUTPOST = register("music_trader_outpost");

    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static boolean isAvailable(Supplier<SoundEvent> sound) {
        try {
            return sound.get() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static void playAtEntity(Supplier<SoundEvent> sound, Entity entity,
                                    SoundSource source, float volume, float pitch) {
        if (!isAvailable(sound)) return;
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                sound.get(), source, volume, pitch);
    }

    public static void playAtBlock(Supplier<SoundEvent> sound, Level level, BlockPos pos,
                                   SoundSource source, float volume, float pitch) {
        if (!isAvailable(sound)) return;
        level.playSound(null, pos, sound.get(), source, volume, pitch);
    }
}
