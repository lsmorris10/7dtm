package com.sevendaystominecraft.perk;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class PerkCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("bzhs")
                        .then(Commands.literal("level")
                                .executes(PerkCommand::showLevel))
                        .then(Commands.literal("stats")
                                .executes(PerkCommand::showLevel))
                        .then(Commands.literal("perk")
                                .then(Commands.argument("perkId", StringArgumentType.string())
                                        .executes(ctx -> spendPerk(ctx, 0))
                                        .then(Commands.argument("rank", IntegerArgumentType.integer(1))
                                                .executes(ctx -> spendPerk(ctx, IntegerArgumentType.getInteger(ctx, "rank"))))))
                        .then(Commands.literal("attribute")
                                .then(Commands.argument("attr", StringArgumentType.string())
                                        .executes(PerkCommand::spendAttribute)))
                        .then(Commands.literal("perks")
                                .executes(PerkCommand::listPerks))
        );
    }

    private static int showLevel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        int xpNeeded = LevelManager.xpToNextLevel(stats.getLevel());
        float xpPct = (xpNeeded > 0) ? (float) stats.getXp() / xpNeeded * 100f : 0f;

        source.sendSuccess(() -> Component.literal(
                String.format("§6[BZHS] §fLevel: §e%d §7| XP: §e%d§7/§e%d §7(%.1f%%)",
                        stats.getLevel(), stats.getXp(), xpNeeded, xpPct)
        ), false);

        source.sendSuccess(() -> Component.literal(
                String.format("§6[BZHS] §fPerk Points: §a%d §7| Attribute Points: §a%d",
                        stats.getPerkPoints(), stats.getAttributePoints())
        ), false);

        StringBuilder attrLine = new StringBuilder("§6[BZHS] §fAttributes: ");
        for (Attribute attr : Attribute.values()) {
            attrLine.append(String.format("§e%s§7:§f%d ", attr.getShortName(), stats.getAttributeLevel(attr)));
        }
        String attrStr = attrLine.toString();
        source.sendSuccess(() -> Component.literal(attrStr), false);

        Map<String, Integer> perks = stats.getActivePerks();
        if (!perks.isEmpty()) {
            StringBuilder perkLine = new StringBuilder("§6[BZHS] §fActive Perks: ");
            for (Map.Entry<String, Integer> entry : perks.entrySet()) {
                PerkDefinition def = PerkRegistry.get(entry.getKey());
                String name = def != null ? def.getDisplayName() : entry.getKey();
                perkLine.append(String.format("§a%s§7[%d] ", name, entry.getValue()));
            }
            String perkStr = perkLine.toString();
            source.sendSuccess(() -> Component.literal(perkStr), false);
        }

        return 1;
    }

    private static int spendPerk(CommandContext<CommandSourceStack> context, int targetRank) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        String perkId = StringArgumentType.getString(context, "perkId");
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        PerkDefinition perk = PerkRegistry.get(perkId);
        if (perk == null) {
            source.sendFailure(Component.literal("§c[BZHS] Unknown perk: " + perkId + ". Use /bzhs perks to list all."));
            return 0;
        }

        int currentRank = stats.getPerkRank(perkId);
        int nextRank = (targetRank > 0) ? targetRank : currentRank + 1;

        if (nextRank <= currentRank) {
            source.sendFailure(Component.literal(
                    String.format("§c[BZHS] You already have %s at rank %d.", perk.getDisplayName(), currentRank)));
            return 0;
        }

        if (nextRank > perk.getMaxRank()) {
            source.sendFailure(Component.literal(
                    String.format("§c[BZHS] %s max rank is %d.", perk.getDisplayName(), perk.getMaxRank())));
            return 0;
        }

        int pointsNeeded = nextRank - currentRank;
        if (stats.getPerkPoints() < pointsNeeded) {
            source.sendFailure(Component.literal(
                    String.format("§c[BZHS] Need %d perk point(s), you have %d.", pointsNeeded, stats.getPerkPoints())));
            return 0;
        }

        int requiredAttrLevel = perk.getAttributeRequirement(nextRank);
        int currentAttrLevel = stats.getAttributeLevel(perk.getAttribute());
        if (currentAttrLevel < requiredAttrLevel) {
            source.sendFailure(Component.literal(
                    String.format("§c[BZHS] %s rank %d requires %s level %d (you have %d).",
                            perk.getDisplayName(), nextRank, perk.getAttribute().getShortName(),
                            requiredAttrLevel, currentAttrLevel)));
            return 0;
        }

        stats.setPerkRank(perkId, nextRank);
        stats.addPerkPoints(-pointsNeeded);

        source.sendSuccess(() -> Component.literal(
                String.format("§a[BZHS] §fUnlocked §e%s §frank %d! §7(%s)",
                        perk.getDisplayName(), nextRank, perk.getDescription())
        ), false);

        com.sevendaystominecraft.advancement.RecipeUnlockManager.onPerkRankGained(player, perkId, nextRank);

        PlayerStatsHandler.sendStatsToClient(player, stats);
        return 1;
    }

    private static int spendAttribute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        String attrName = StringArgumentType.getString(context, "attr");
        Attribute attribute = Attribute.fromShortName(attrName);
        if (attribute == null) {
            source.sendFailure(Component.literal("§c[BZHS] Unknown attribute: " + attrName + ". Use STR/PER/FOR/AGI/INT."));
            return 0;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        if (stats.getAttributePoints() <= 0) {
            source.sendFailure(Component.literal("§c[BZHS] No attribute points available."));
            return 0;
        }

        int current = stats.getAttributeLevel(attribute);
        if (current >= 10) {
            source.sendFailure(Component.literal(
                    String.format("§c[BZHS] %s is already at max level 10.", attribute.getDisplayName())));
            return 0;
        }

        stats.setAttributeLevel(attribute, current + 1);
        stats.addAttributePoints(-1);

        int newLevel = current + 1;
        source.sendSuccess(() -> Component.literal(
                String.format("§a[BZHS] §f%s §eincreased to level %d!",
                        attribute.getDisplayName(), newLevel)
        ), false);

        PlayerStatsHandler.sendStatsToClient(player, stats);
        return 1;
    }

    private static int listPerks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        for (Attribute attr : Attribute.values()) {
            List<PerkDefinition> perks = PerkRegistry.getByAttribute(attr);
            StringBuilder line = new StringBuilder("§6[" + attr.getDisplayName() + "] ");
            for (PerkDefinition perk : perks) {
                if (perk.isMastery()) {
                    line.append(String.format("§d%s§7(T10) ", perk.getDisplayName()));
                } else {
                    line.append(String.format("§a%s§7(%d) ", perk.getId(), perk.getMaxRank()));
                }
            }
            String lineStr = line.toString();
            source.sendSuccess(() -> Component.literal(lineStr), false);
        }

        source.sendSuccess(() -> Component.literal(
                String.format("§7Total perks registered: %d. Use /bzhs perk <id> to unlock.", PerkRegistry.count())
        ), false);

        return 1;
    }
}
