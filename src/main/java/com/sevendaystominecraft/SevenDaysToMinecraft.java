package com.sevendaystominecraft;

import com.mojang.logging.LogUtils;
import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.command.LootStageCommand;
import com.sevendaystominecraft.command.TerritoryCommand;
import com.sevendaystominecraft.config.HeatmapConfig;
import com.sevendaystominecraft.config.HordeConfig;
import com.sevendaystominecraft.config.LootConfig;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.config.TerritoryConfig;
import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.client.CompassOverlay;
import com.sevendaystominecraft.client.MinimapOverlay;
import com.sevendaystominecraft.client.ModEntityRenderers;
import com.sevendaystominecraft.client.ModScreens;
import com.sevendaystominecraft.client.StatsHudOverlay;
import com.sevendaystominecraft.horde.DayCycleHandler;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.territory.TerritoryWorldGenerator;
import com.sevendaystominecraft.item.ModCreativeTabs;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.menu.ModMenuTypes;
import com.sevendaystominecraft.magazine.ModMagazines;
import com.sevendaystominecraft.network.ModNetworking;
import com.sevendaystominecraft.sound.ModSounds;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(SevenDaysToMinecraft.MOD_ID)
public class SevenDaysToMinecraft {

    public static final String MOD_ID = "sevendaystominecraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SevenDaysToMinecraft(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("BZHS — Initializing...");

        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, SurvivalConfig.SPEC, "survival.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, HordeConfig.SPEC, "horde.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ZombieConfig.SPEC, "zombies.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, HeatmapConfig.SPEC, "heatmap.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, LootConfig.SPEC, "loot.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, TerritoryConfig.SPEC, "territory.toml");

        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModMagazines.init();
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        modEventBus.addListener(ModNetworking::onRegisterPayloads);
        modEventBus.addListener(ModEntities.AttributeRegistration::onEntityAttributeCreation);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ModEntityRenderers::onRegisterRenderers);
            modEventBus.addListener(ModScreens::onRegisterMenuScreens);
            modEventBus.addListener(CompassOverlay::onRegisterGuiLayers);
            modEventBus.addListener(StatsHudOverlay::onRegisterGuiLayers);
            modEventBus.addListener(MinimapOverlay::onRegisterGuiLayers);
        }

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(TerritoryWorldGenerator::onChunkLoad);

        LOGGER.info("BZHS — Mod registered successfully.");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("BZHS Common Setup — Initializing shared systems...");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("BZHS Client Setup — Registering renderers and visual systems...");
    }

    private void onServerStarting(final ServerStartingEvent event) {
        LOGGER.info("BZHS Server Starting — Loading world data...");
        DayCycleHandler.reset();
        migrateLegacyDamageMultiplier();
    }

    private void migrateLegacyDamageMultiplier() {
        double current = SurvivalConfig.INSTANCE.playerDamageMultiplier.get();
        if (current == 2.0) {
            SurvivalConfig.INSTANCE.playerDamageMultiplier.set(1.0);
            LOGGER.warn("BZHS: Migrated legacy playerDamageMultiplier from 2.0 to 1.0 " +
                    "(old default from pre-Task#83 worlds). Set a different value in survival.toml to override.");
        }
    }

    private void onRegisterCommands(final RegisterCommandsEvent event) {
        LootStageCommand.register(event.getDispatcher());
        TerritoryCommand.register(event.getDispatcher());
        LOGGER.info("BZHS: Registered /bzhs commands");
    }
}
