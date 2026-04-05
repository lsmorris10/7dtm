package com.sevendaystominecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sevendaystominecraft.territory.TerritoryData;
import com.sevendaystominecraft.territory.TerritoryRecord;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class TerritoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bzhs")
                .then(Commands.literal("territory")
                        .then(Commands.literal("list")
                                .executes(TerritoryCommand::listNearby))
                        .then(Commands.literal("listall")
                                .requires(src -> src.hasPermission(2))
                                .executes(TerritoryCommand::listAll))
                )
        );
    }

    private static int listNearby(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        ServerLevel level = (ServerLevel) player.level();
        TerritoryData data = TerritoryData.getOrCreate(level);
        BlockPos playerPos = player.blockPosition();
        List<TerritoryRecord> nearby = data.getNearby(playerPos, 256.0);

        if (nearby.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§6[BZHS] §fNo territories within 256 blocks."), false);
            return 1;
        }

        StringBuilder sb = new StringBuilder("§6[BZHS] §fNearby territories:\n");
        for (TerritoryRecord record : nearby) {
            BlockPos origin = record.getOrigin();
            double dist = Math.sqrt(
                    Math.pow(origin.getX() - playerPos.getX(), 2) +
                    Math.pow(origin.getZ() - playerPos.getZ(), 2));
            String clearedStr = record.isCleared() ? "§a[CLEARED]" : "§c[ACTIVE]";
            sb.append(String.format("  §e#%d §f%s §7(%s) §7at (%d,%d,%d) dist=%.0f %s\n",
                    record.getId(),
                    record.getLabel(),
                    record.getType().getDisplayName(),
                    origin.getX(), origin.getY(), origin.getZ(),
                    dist,
                    clearedStr));
        }

        String text = sb.toString();
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int listAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getLevel() instanceof ServerLevel level) {
            TerritoryData data = TerritoryData.getOrCreate(level);
            var all = data.getAllTerritories();

            if (all.isEmpty()) {
                source.sendSuccess(() -> Component.literal("§6[BZHS] §fNo territories generated yet."), false);
                return 1;
            }

            StringBuilder sb = new StringBuilder("§6[BZHS] §fAll territories (" + all.size() + "):\n");
            for (TerritoryRecord record : all) {
                BlockPos origin = record.getOrigin();
                String clearedStr = record.isCleared() ? "§a[CLEARED]" : "§c[ACTIVE]";
                sb.append(String.format("  §e#%d §f%s §7at (%d,%d,%d) %s\n",
                        record.getId(),
                        record.getLabel(),
                        origin.getX(), origin.getY(), origin.getZ(),
                        clearedStr));
            }

            String text = sb.toString();
            source.sendSuccess(() -> Component.literal(text), false);
        }
        return 1;
    }
}
