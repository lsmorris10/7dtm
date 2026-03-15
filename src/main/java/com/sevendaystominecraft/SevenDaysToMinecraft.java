package com.sevendaystominecraft;

import com.mojang.logging.LogUtils;
import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.command.LootStageCommand;
import com.sevendaystominecraft.config.HeatmapConfig;
import com.sevendaystominecraft.config.HordeConfig;
import com.sevendaystominecraft.config.LootConfig;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.item.ModCreativeTabs;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.menu.ModMenuTypes;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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

        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

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
    }

    private void onRegisterCommands(final RegisterCommandsEvent event) {
        LootStageCommand.register(event.getDispatcher());
        LOGGER.info("BZHS: Registered /bzhs loot_stage command");
    }
}
