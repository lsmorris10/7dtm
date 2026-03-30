package com.sevendaystominecraft.chat;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.*;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class ChatModerationHandler {

    // Track offenses per player UUID
    private static final Map<UUID, Integer> offenseCount = new HashMap<>();
    private static final Map<UUID, Long> mutedUntil = new HashMap<>();

    // Compiled patterns for efficient matching
    private static final List<Pattern> BANNED_PATTERNS = new ArrayList<>();

    static {
        // Racial slurs and severe hate speech
        String[] slurs = {
                "nigger", "nigga", "nigg3r", "n1gger", "n1gga",
                "faggot", "f4ggot", "fagg0t",
                "retard", "r3tard",
                "chink", "ch1nk",
                "spic", "sp1c",
                "kike", "k1ke",
                "wetback", "w3tback",
                "beaner", "b3aner",
                "gook", "g00k",
                "coon", "c00n",
                "tranny", "tr4nny",
                "dyke", "dyk3"
        };

        for (String slur : slurs) {
            // Word boundary matching — won't catch innocent words like "snigger" or "bigger"
            // Uses (?i) for case-insensitive and word boundary \b
            String escaped = Pattern.quote(slur);
            // Also match leet-speak variants with common substitutions
            BANNED_PATTERNS.add(Pattern.compile("(?i)\\b" + escaped + "s?\\b"));
        }

        // Also match attempts to bypass with spaces/dots between letters
        // e.g. "n.i.g.g.e.r" or "n i g g e r"
        addSpacedPattern("nigger");
        addSpacedPattern("nigga");
        addSpacedPattern("faggot");
    }

    private static void addSpacedPattern(String word) {
        StringBuilder sb = new StringBuilder("(?i)");
        for (int i = 0; i < word.length(); i++) {
            if (i > 0) sb.append("[\\s.\\-_*]+");
            sb.append(Pattern.quote(String.valueOf(word.charAt(i))));
        }
        BANNED_PATTERNS.add(Pattern.compile(sb.toString()));
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        UUID uuid = player.getUUID();

        // Check if player is muted
        Long muteExpiry = mutedUntil.get(uuid);
        if (muteExpiry != null && System.currentTimeMillis() < muteExpiry) {
            long secondsLeft = (muteExpiry - System.currentTimeMillis()) / 1000;
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal(
                    "§c§l[BZHS] §cYou are muted for " + secondsLeft + " more seconds."));
            return;
        }

        String message = event.getMessage().getString();

        if (containsBannedContent(message)) {
            event.setCanceled(true);

            int offenses = offenseCount.getOrDefault(uuid, 0) + 1;
            offenseCount.put(uuid, offenses);

            SevenDaysToMinecraft.LOGGER.warn(
                    "[BZHS Moderation] Player {} (UUID: {}) triggered chat filter (offense #{}): \"{}\"",
                    player.getName().getString(), uuid, offenses, message);

            if (offenses == 1) {
                // First offense: warning
                player.sendSystemMessage(Component.literal(
                        "§c§l[BZHS] §cYour message was blocked for containing prohibited language. " +
                                "§7This is a warning. Repeat offenses will result in muting/kicking."));
            } else if (offenses == 2) {
                // Second offense: 60-second mute
                mutedUntil.put(uuid, System.currentTimeMillis() + 60_000);
                player.sendSystemMessage(Component.literal(
                        "§c§l[BZHS] §cYou are now muted for 60 seconds. §7Continued use of prohibited " +
                                "language will result in being kicked from the server."));
                SevenDaysToMinecraft.LOGGER.warn(
                        "[BZHS Moderation] Player {} muted for 60 seconds (offense #2)", player.getName().getString());
            } else {
                // Third+ offense: kick
                player.connection.disconnect(Component.literal(
                        "§c§lKicked by BZHS Moderation\n§fRepeated use of prohibited language.\n" +
                                "§7Please follow community guidelines."));
                SevenDaysToMinecraft.LOGGER.warn(
                        "[BZHS Moderation] Player {} KICKED for repeated violations (offense #{})",
                        player.getName().getString(), offenses);
            }
        }
    }

    private static boolean containsBannedContent(String message) {
        for (Pattern pattern : BANNED_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }
        return false;
    }

    // Called on server stop to clean up
    public static void reset() {
        offenseCount.clear();
        mutedUntil.clear();
    }
}
