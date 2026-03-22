package com.sevendaystominecraft.client.particle;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<SimpleParticleType> RADIOACTIVE_GLOW =
            PARTICLE_TYPES.register("radioactive_glow", () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> BLOOD_DRIP =
            PARTICLE_TYPES.register("blood_drip", () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> SONIC_PULSE =
            PARTICLE_TYPES.register("sonic_pulse", () -> new SimpleParticleType(false));
}
