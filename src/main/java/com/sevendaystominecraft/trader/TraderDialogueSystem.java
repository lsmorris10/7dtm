package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.quest.QuestInstance;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates contextual trader dialogue based on each player's individual
 * progression. Works in multiplayer — each player sees different dialogue
 * because it reads their personal stats, quests, and world state.
 */
public class TraderDialogueSystem {

    /**
     * Progression stages. The system evaluates the player's state and
     * picks the FIRST stage whose condition matches.
     */
    public enum Stage {
        BRAND_NEW,           // Just spawned, level 1, no quests ever done
        EARLY_GAME,          // Levels 1-5, maybe a quest or two
        GETTING_STARTED,     // Has some gear, levels 3-8
        MID_GAME,            // Levels 8-15, has explored territories
        LATE_GAME,           // Levels 15-25, cleared higher-tier areas
        END_GAME             // Level 25+, maxed perks
    }

    /**
     * Returns contextual dialogue lines for a trader to say to this specific player.
     * Each line is displayed one at a time — the player clicks "Next" to advance.
     */
    public static List<String> getDialogueForPlayer(ServerPlayer player, String traderName) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        Stage stage = determineStage(stats);

        List<String> lines = new ArrayList<>();

        // Greeting — always shown first
        lines.add(getGreeting(traderName, player.getName().getString(), stage));

        // Quest-aware hints (turn-in ready, etc.)
        lines.addAll(getQuestHints(stats));

        return lines;
    }

    /**
     * Determines which progression stage a player is in based on their stats.
     */
    static Stage determineStage(SevenDaysPlayerStats stats) {
        int level = stats.getLevel();
        int totalPerkRanks = getTotalPerkRanks(stats);
        int unlockedRecipeCount = stats.getUnlockedRecipes().size();

        if (level <= 1 && totalPerkRanks == 0) {
            return Stage.BRAND_NEW;
        } else if (level <= 5 && unlockedRecipeCount <= 5) {
            return Stage.EARLY_GAME;
        } else if (level <= 8) {
            return Stage.GETTING_STARTED;
        } else if (level <= 15) {
            return Stage.MID_GAME;
        } else if (level <= 25) {
            return Stage.LATE_GAME;
        } else {
            return Stage.END_GAME;
        }
    }

    private static String getGreeting(String traderName, String playerName, Stage stage) {
        return switch (stage) {
            case BRAND_NEW -> "§e" + traderName + ": §f\"Hey there, " + playerName
                    + "! You look like you just crawled out of a ditch. Don't worry, we all start somewhere.\"";
            case EARLY_GAME -> "§e" + traderName + ": §f\"Back again, " + playerName
                    + "? Good to see you're still breathing.\"";
            case GETTING_STARTED -> "§e" + traderName + ": §f\"" + playerName
                    + "! You're starting to look like you know what you're doing.\"";
            case MID_GAME -> "§e" + traderName + ": §f\"" + playerName
                    + ", welcome back. Word is you've been clearing out some nasty places.\"";
            case LATE_GAME -> "§e" + traderName + ": §f\"Well well, " + playerName
                    + ". You're making quite a name for yourself out there.\"";
            case END_GAME -> "§e" + traderName + ": §f\"" + playerName
                    + "! The legend returns. I've got some special things set aside for you.\"";
        };
    }

    private static List<String> getQuestHints(SevenDaysPlayerStats stats) {
        List<String> hints = new ArrayList<>();
        List<QuestInstance> activeQuests = stats.getActiveQuests();

        if (!activeQuests.isEmpty()) {
            int turnInCount = 0;
            for (QuestInstance quest : activeQuests) {
                if (quest.getState() == QuestInstance.State.READY_TO_TURN_IN) {
                    turnInCount++;
                }
            }

            if (turnInCount > 0) {
                hints.add("§a\"Hey, looks like you've got §e" + turnInCount
                        + " quest" + (turnInCount > 1 ? "s" : "") + "§a ready to turn in! Let's settle up.\"");
            }
        }

        return hints;
    }

    private static int getTotalPerkRanks(SevenDaysPlayerStats stats) {
        String[] knownPerks = {
                "campfire_cook", "advanced_engineering", "physician", "gearhead",
                "robotics_inventor", "pack_mule", "better_barter", "bold_explorer",
                "skull_crusher", "heavy_armor", "light_armor", "parkour"
        };
        int total = 0;
        for (String perkId : knownPerks) {
            total += stats.getPerkRank(perkId);
        }
        return total;
    }
}
