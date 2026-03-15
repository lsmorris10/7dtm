package com.sevendaystominecraft.capability;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class DebuffCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("bzhs")
                        .then(Commands.literal("cleardebuffs")
                                .requires(source -> source.hasPermission(2))
                                .executes(DebuffCommand::clearDebuffs))
        );
    }

    private static int clearDebuffs(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        PlayerStatsHandler.clearAllDebuffs(player, stats);

        source.sendSuccess(() -> Component.literal("§7[BZHS] §aAll debuffs cleared."), false);
        SevenDaysToMinecraft.LOGGER.info("BZHS: {} cleared all debuffs via command", player.getName().getString());
        return 1;
    }
}
