package com.sevendaystominecraft.client;

import com.sevendaystominecraft.entity.ModEntities;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModEntityRenderers {

    private static final float NAME_TAG_EXTRA_HEIGHT = 1.0f;

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WALKER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.CRAWLER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.FROZEN_LUMBERJACK.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.BLOATED_WALKER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.1f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.SPIDER_ZOMBIE.get(), ctx -> new ScaledZombieRenderer(ctx, 0.5f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.FERAL_WIGHT.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.COP.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.SCREAMER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.DEMOLISHER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.3f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.MUTATED_CHUCK.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.NURSE.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.SOLDIER.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.CHARGED.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.INFERNAL.get(), ctx -> new ScaledZombieRenderer(ctx, 1.0f, NAME_TAG_EXTRA_HEIGHT));

        event.registerEntityRenderer(ModEntities.BEHEMOTH.get(), ctx -> new ScaledZombieRenderer(ctx, 2.0f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.ZOMBIE_DOG.get(), ctx -> new ScaledZombieRenderer(ctx, 0.5f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.VULTURE.get(), ctx -> new ScaledZombieRenderer(ctx, 0.4f, NAME_TAG_EXTRA_HEIGHT));
        event.registerEntityRenderer(ModEntities.ZOMBIE_BEAR.get(), ctx -> new ScaledZombieRenderer(ctx, 1.5f, NAME_TAG_EXTRA_HEIGHT));

        event.registerEntityRenderer(ModEntities.ACID_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BULLET.get(), ThrownItemRenderer::new);
    }
}
