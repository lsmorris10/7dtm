package com.sevendaystominecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.loot.LootStageCalculator;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LootStageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("7dtm")
                .then(Commands.literal("loot_stage")
                        .executes(LootStageCommand::showLootStage)
                )
        );
    }

    private static int showLootStage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getPlayer() == null) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        int lootStage = LootStageCalculator.calculate(player);
        long daysSurvived = player.level().getDayTime() / SevenDaysConstants.DAY_LENGTH;
        int playerLevel = player.experienceLevel;

        source.sendSuccess(() -> Component.literal(
                String.format("§6[7DTM] §fLoot Stage: §e%d\n" +
                        "§7  Player Level: %d\n" +
                        "§7  Days Survived: %d\n" +
                        "§7  Formula: floor((level×0.5) + (days×0.3) + biomeBonus + perkBonus)",
                        lootStage, playerLevel, daysSurvived)
        ), false);

        return 1;
    }
}
