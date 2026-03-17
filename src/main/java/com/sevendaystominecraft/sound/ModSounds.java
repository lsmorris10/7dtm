package com.sevendaystominecraft.sound;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<SoundEvent> ZOMBIE_GROAN = register("zombie_groan");
    public static final Supplier<SoundEvent> ZOMBIE_SCREAM = register("zombie_scream");
    public static final Supplier<SoundEvent> ZOMBIE_DEATH = register("zombie_death");
    public static final Supplier<SoundEvent> GUN_FIRE_9MM = register("gun_fire_9mm");
    public static final Supplier<SoundEvent> GUN_FIRE_AK47 = register("gun_fire_ak47");
    public static final Supplier<SoundEvent> BLOOD_MOON_SIREN = register("blood_moon_siren");
    public static final Supplier<SoundEvent> WORKSTATION_AMBIENT = register("workstation_ambient");
    public static final Supplier<SoundEvent> BLOCK_BREAK_ZOMBIE = register("block_break_zombie");

    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
