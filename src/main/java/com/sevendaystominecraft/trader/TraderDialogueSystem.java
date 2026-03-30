package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.quest.QuestInstance;
import com.sevendaystominecraft.magazine.MagazineRegistry;
import com.sevendaystominecraft.magazine.MagazineSeries;

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
     * The dialogue depends on the player's level, active quests, perk ranks,
     * recipe unlocks, and other progression data.
     */
    public static List<String> getDialogueForPlayer(ServerPlayer player, String traderName) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        Stage stage = determineStage(stats);

        List<String> lines = new ArrayList<>();

        // Greeting
        lines.add(getGreeting(traderName, player.getName().getString(), stage));

        // Main guidance based on stage
        lines.addAll(getStageAdvice(stats, stage));

        // Quest-aware hints
        lines.addAll(getQuestHints(stats));

        // Contextual tips based on what they're missing
        lines.addAll(getContextualTips(stats, stage));

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
                    + "? Good to see you're still breathing. Most don't make it past the first week.\"";
            case GETTING_STARTED -> "§e" + traderName + ": §f\"" + playerName
                    + "! You're starting to look like you know what you're doing. Almost.\"";
            case MID_GAME -> "§e" + traderName + ": §f\"" + playerName
                    + ", welcome back. Word is you've been clearing out some nasty places. Respect.\"";
            case LATE_GAME -> "§e" + traderName + ": §f\"Well well, " + playerName
                    + ". You're making quite a name for yourself out there. The zombies know your name.\"";
            case END_GAME -> "§e" + traderName + ": §f\"" + playerName
                    + "! The legend returns. I've got some special things set aside for you.\"";
        };
    }

    private static List<String> getStageAdvice(SevenDaysPlayerStats stats, Stage stage) {
        List<String> advice = new ArrayList<>();

        switch (stage) {
            case BRAND_NEW -> {
                advice.add("§7\"First things first — you need a weapon. Even a §eStone Club§7 will keep you alive.\"");
                advice.add("§7\"Find yourself some food and clean water. Cook at a §eCampfire§7 — raw meat'll make you sick.\"");
                advice.add("§7\"Scavenge houses for supplies. Trash piles and mailboxes sometimes have useful junk.\"");
                advice.add("§7\"Come back to me when you're ready — I'll have §eQuests§7 that'll earn you some dukes.\"");
            }
            case EARLY_GAME -> {
                advice.add("§7\"You should be building up your base. The §cBlood Moon§7 comes every 7 days and it's brutal.\"");
                if (stats.getPerkRank("campfire_cook") == 0) {
                    advice.add("§7\"Invest in §eCampfire Cooking§7 — better food means better everything.\"");
                }
                if (stats.getLevel() < 3) {
                    advice.add("§7\"Kill zombies and do quests to level up. Spend your §ePerk Points§7 wisely.\"");
                }
                advice.add("§7\"Look for §eMagazines§7 in bookshelves — they'll teach you new recipes you can't get any other way.\"");
            }
            case GETTING_STARTED -> {
                advice.add("§7\"Time to push out further. Higher-tier areas have better loot but meaner zombies.\"");
                if (stats.getPerkRank("advanced_engineering") == 0) {
                    advice.add("§7\"You should learn §eAdvanced Engineering§7 — it'll let you craft iron tools and real gear.\"");
                }
                advice.add("§7\"Keep an eye out for §eGun Safes§7 in military and commercial areas — that's where the good stuff hides.\"");
            }
            case MID_GAME -> {
                advice.add("§7\"You're ready for §e3-star§7 and §e4-star§7 territories now. The loot there is worth the risk.\"");
                if (stats.getPerkRank("physician") < 2) {
                    advice.add("§7\"Invest in §ePhysician§7 — you'll need antibiotics and first aid kits for the harder areas.\"");
                }
                if (stats.getPerkRank("gearhead") == 0) {
                    advice.add("§7\"If you haven't already, the §eGearhead§7 perk unlocks firearm crafting. You'll need guns out there.\"");
                }
                advice.add("§7\"Stock up on ammo and meds before heading into high-tier zones. Don't be a hero.\"");
            }
            case LATE_GAME -> {
                advice.add("§7\"The §c5-star zones§7 are the real test. Demolishers, Soldiers... they don't play around.\"");
                advice.add("§7\"Word is there are §eUnderground Bunkers§7 out in the 5-star areas. Massive loot, but you'll earn every piece.\"");
                if (stats.getPerkRank("robotics_inventor") == 0) {
                    advice.add("§7\"Look into §eRobotics§7 — turrets can watch your back when things get dicey.\"");
                }
            }
            case END_GAME -> {
                advice.add("§7\"Honestly? You've pretty much seen it all. But there's always harder challenges out there.\"");
                advice.add("§7\"Check my §eSecret Stash§7 — I save the best stuff for my most loyal customers.\"");
                advice.add("§7\"Help the newcomers if you see them. Everyone needs a hand at first.\"");
            }
        }

        return advice;
    }

    private static List<String> getQuestHints(SevenDaysPlayerStats stats) {
        List<String> hints = new ArrayList<>();
        List<QuestInstance> activeQuests = stats.getActiveQuests();

        if (activeQuests.isEmpty()) {
            hints.add("§7\"I've got §eQuests§7 if you need work. Good way to earn dukes and XP.\"");
        } else {
            int turnInCount = 0;
            int activeCount = 0;
            for (QuestInstance quest : activeQuests) {
                if (quest.getState() == QuestInstance.State.READY_TO_TURN_IN) {
                    turnInCount++;
                } else if (quest.getState() == QuestInstance.State.ACTIVE) {
                    activeCount++;
                }
            }

            if (turnInCount > 0) {
                hints.add("§a\"Hey, looks like you've got §e" + turnInCount
                        + " quest" + (turnInCount > 1 ? "s" : "") + "§a ready to turn in! Let's settle up.\"");
            }
            if (activeCount > 0) {
                QuestInstance firstActive = activeQuests.stream()
                        .filter(q -> q.getState() == QuestInstance.State.ACTIVE)
                        .findFirst().orElse(null);
                if (firstActive != null) {
                    hints.add("§7\"You're working on §e" + firstActive.getDefinition().getObjectiveDescription()
                            + "§7 — " + firstActive.getProgressText() + ". Keep at it!\"");
                }
            }
        }

        return hints;
    }

    private static List<String> getContextualTips(SevenDaysPlayerStats stats, Stage stage) {
        List<String> tips = new ArrayList<>();

        // Water/food warnings
        if (stats.getWater() < stats.getMaxWater() * 0.3f) {
            tips.add("§c\"You look parched. Drink something before you pass out.\"");
        }
        if (stats.getFood() < stats.getMaxFood() * 0.3f) {
            tips.add("§c\"You're skin and bones. Eat something, for crying out loud.\"");
        }

        // Level milestones
        int level = stats.getLevel();
        if (level == 5 || level == 10 || level == 15 || level == 20 || level == 25) {
            tips.add("§a\"Level " + level + "! That's a milestone. You should spend those perk points.\"");
        }

        // Magazine hint if early game
        if (stage == Stage.EARLY_GAME || stage == Stage.GETTING_STARTED) {
            int totalRead = getTotalMagazinesRead(stats);
            if (totalRead == 0) {
                tips.add("§7\"Pro tip: find §eMagazines§7 in bookshelves. They unlock exclusive recipes.\"");
            }
        }

        return tips;
    }

    private static int getTotalPerkRanks(SevenDaysPlayerStats stats) {
        // Check a representative set of perks to estimate total progression
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

    private static int getTotalMagazinesRead(SevenDaysPlayerStats stats) {
        int total = 0;
        for (MagazineSeries series : MagazineRegistry.getAllSeries()) {
            total += stats.getMagazineData().getReadCount(series.id());
        }
        return total;
    }
}
