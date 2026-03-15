package com.sevendaystominecraft.heatmap;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class HeatmapCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("bzhs")
                        .then(Commands.literal("heat")
                                .executes(HeatmapCommand::showHeat))
                        .then(Commands.literal("heat_clear")
                                .requires(source -> source.hasPermission(2))
                                .executes(HeatmapCommand::clearHeat))
        );
    }

    private static int showHeat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        HeatmapData data = HeatmapData.getOrCreate(level);
        ChunkPos chunkPos = new ChunkPos(player.blockPosition());
        float heat = data.getHeat(chunkPos);

        String heatColor;
        if (heat >= 75) {
            heatColor = "§c";
        } else if (heat >= 50) {
            heatColor = "§6";
        } else if (heat >= 25) {
            heatColor = "§e";
        } else {
            heatColor = "§a";
        }

        float thresholdMult = com.sevendaystominecraft.config.HeatmapConfig.INSTANCE
                .spawnThresholdMultiplier.get().floatValue();
        String thresholds = String.format("§7 [Scouts:%.0f Screamer:%.0f Horde:%.0f Waves:%.0f]",
                25 * thresholdMult, 50 * thresholdMult, 75 * thresholdMult, 100 * thresholdMult);

        source.sendSuccess(() -> Component.literal(
                String.format("§7[BZHS] Chunk (%d, %d) Heat: %s%.1f§7/100%s",
                        chunkPos.x, chunkPos.z, heatColor, heat, thresholds)
        ), false);

        int activeChunks = data.getAllChunkSources().size();
        source.sendSuccess(() -> Component.literal(
                String.format("§7[BZHS] Active heated chunks: §f%d", activeChunks)
        ), false);

        return 1;
    }

    private static int clearHeat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        HeatmapData data = HeatmapData.getOrCreate(level);
        data.getAllChunkSources().clear();
        data.setDirty();
        HeatmapSpawner.clearCooldowns();

        source.sendSuccess(() -> Component.literal("§7[BZHS] §aHeatmap cleared."), false);
        return 1;
    }
}
