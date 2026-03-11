package com.sevendaystominecraft;

import com.mojang.logging.LogUtils;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.config.HordeConfig;
import com.sevendaystominecraft.config.SurvivalConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

/**
 * 7 Days to Minecraft — Main mod entry point.
 * 
 * Total conversion mod bringing 7 Days to Die survival into Minecraft 1.21+.
 * Aligned to 7DTD 2.6 Experimental (Feb 25, 2026).
 * 
 * @see <a href="docs/7dtm_final_spec.md">Full Specification</a>
 */
@Mod(SevenDaysToMinecraft.MOD_ID)
public class SevenDaysToMinecraft {

    public static final String MOD_ID = "sevendaystominecraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SevenDaysToMinecraft(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("7 Days to Minecraft — Initializing...");

        // ── Milestone #2: Register Data Attachments ─────────────────────
        // Player stats (Food, Water, Stamina, Debuffs, Temperature)
        // This DeferredRegister handles attachment type registration on the mod bus.
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // ── Milestone #2: Register Server Config (survival.toml) ────────
        modContainer.registerConfig(ModConfig.Type.SERVER, SurvivalConfig.SPEC, "survival.toml");

        // ── Milestone #3: Register Horde Config (horde.toml) ─────────
        modContainer.registerConfig(ModConfig.Type.SERVER, HordeConfig.SPEC, "horde.toml");

        // Register mod lifecycle events
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        // Register game events on the NeoForge event bus
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        // Note: PlayerStatsHandler is registered via @EventBusSubscriber (auto-discovered)
        // Note: ModNetworking is registered via @EventBusSubscriber(bus = Bus.MOD) (auto-discovered)

        // TODO: Phase 1 — Register systems in order:
        // - ModBlocks.register(modEventBus);      // Custom blocks
        // - ModItems.register(modEventBus);       // Custom items
        // - ModEntities.register(modEventBus);    // Zombie/animal entities
        // - ModParticles.register(modEventBus);   // Custom particles (§14.7.2)
        // - ModSounds.register(modEventBus);      // Custom sounds

        LOGGER.info("7 Days to Minecraft — Mod registered successfully.");
    }

    /**
     * Common setup — runs on both client and server after registry events.
     * Initialize cross-side systems (heatmap, temperature, AI, etc.)
     */
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("7DTM Common Setup — Initializing shared systems...");
        // TODO: Initialize systems:
        // - Heatmap chunk capability
        // - Temperature calculator
        // - Zombie behavior tree AI
        // - Perk registry loader
        // - Electricity grid manager
    }

    /**
     * Client setup — register renderers, screens, keybinds, particles.
     * All visual/animation systems from §14 go here.
     */
    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("7DTM Client Setup — Registering renderers and visual systems...");
        // TODO: Register client-side systems:
        // - Custom HUD overlay renderer (§14.1)
        // - Sprint pose model extension (§14.7.1)
        // - Sprint particle handler (§14.7.2)
        // - Body language handler (§14.7.3)
        // - Debuff vignette renderer (§14.5)
        // - Temperature overlay renderer (§14.5)
        // - Blood Moon atmosphere effects (§14.5)
        // - Zombie glow layer renderer (§14.5)
        // - Vehicle camera handler (§14.5)
        // - Custom inventory/crafting screens (§14.2, §14.3)
        // - Map screen (§14.4)
    }

    /**
     * Server starting — load configs, initialize heatmap data, register commands.
     */
    private void onServerStarting(final ServerStartingEvent event) {
        LOGGER.info("7DTM Server Starting — Loading world data...");
        // TODO: Initialize server-side:
        // - Load/initialize heatmap data
        // - Register /7dtm command tree (§15.4)
        // - Initialize trader restock timers
        // - Blood Moon scheduler
    }
}
