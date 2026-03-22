package com.sevendaystominecraft.client;

import com.sevendaystominecraft.client.particle.BloodDripParticle;
import com.sevendaystominecraft.client.particle.ModParticles;
import com.sevendaystominecraft.client.particle.RadioactiveGlowParticle;
import com.sevendaystominecraft.client.particle.SonicPulseParticle;
import com.sevendaystominecraft.entity.ModEntities;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ModEntityRenderers {

    private static final float NAME_TAG_EXTRA_HEIGHT = 1.0f;

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WALKER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.CRAWLER.get(), ctx -> new CrawlerZombieRenderer(ctx, 1.0f, -0.5f));
        event.registerEntityRenderer(ModEntities.FROZEN_LUMBERJACK.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.BLOATED_WALKER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.1f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.SPIDER_ZOMBIE.get(), ctx -> new SpiderZombieRenderer(ctx, 0.5f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.FERAL_WIGHT.get(), ctx -> new FeralWightRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.COP.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.SCREAMER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.DEMOLISHER.get(), ctx -> new DemolisherRenderer(ctx, 1.3f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.MUTATED_CHUCK.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.NURSE.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.SOLDIER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.CHARGED.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.INFERNAL.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));

        event.registerEntityRenderer(ModEntities.BEHEMOTH.get(), ctx -> new ScaledZombieRenderer(ctx, 2.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.ZOMBIE_DOG.get(), ctx -> new ScaledZombieRenderer(ctx, 0.5f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.VULTURE.get(), ctx -> new ScaledZombieRenderer(ctx, 0.4f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.ZOMBIE_BIRD.get(), ctx -> new ScaledZombieRenderer(ctx, 0.3f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.ZOMBIE_PARROT.get(), ctx -> new ScaledZombieRenderer(ctx, 0.35f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.ZOMBIE_BEAR.get(), ctx -> new ScaledZombieRenderer(ctx, 1.5f, NAME_TAG_EXTRA_HEIGHT));

        event.registerEntityRenderer(ModEntities.ACID_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GRENADE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.TERRITORY_LABEL.get(), ctx -> new TerritoryLabelRenderer(ctx));
        event.registerEntityRenderer(ModEntities.TRADER.get(), ctx -> new TraderRenderer(ctx));
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.RADIOACTIVE_GLOW.get(), RadioactiveGlowParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BLOOD_DRIP.get(), BloodDripParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SONIC_PULSE.get(), SonicPulseParticle.Provider::new);
    }
}
