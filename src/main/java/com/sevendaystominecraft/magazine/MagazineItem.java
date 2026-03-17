package com.sevendaystominecraft.magazine;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class MagazineItem extends Item {

    private final String seriesId;
    private final int issue;

    public MagazineItem(Properties properties, String seriesId, int issue) {
        super(properties);
        this.seriesId = seriesId;
        this.issue = issue;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public int getIssue() {
        return issue;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) {
            return InteractionResult.PASS;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        MagazinePlayerData magData = stats.getMagazineData();
        MagazineSeries series = MagazineRegistry.getSeries(seriesId);

        if (series == null) return InteractionResult.FAIL;

        if (magData.hasRead(seriesId, issue)) {
            player.displayClientMessage(
                    Component.literal("You've already read " + series.displayName() + " #" + issue + "!")
                            .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }

        magData.markRead(seriesId, issue);

        ItemStack held = player.getItemInHand(hand);
        if (!player.isCreative()) {
            held.shrink(1);
        }

        String bonusText = series.getIssueDescription(issue);
        player.displayClientMessage(
                Component.literal("✦ " + series.displayName() + " #" + issue + " — " + bonusText)
                        .withStyle(ChatFormatting.GREEN), false);

        if (magData.hasCompletedSeries(seriesId)) {
            player.displayClientMessage(
                    Component.literal("★ MASTERY COMPLETE: " + series.displayName() + " — " + series.masteryDescription())
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        MagazineSeries series = MagazineRegistry.getSeries(seriesId);
        if (series == null) return;

        tooltip.add(Component.literal(series.displayName() + " #" + issue)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(series.getIssueDescription(issue))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Series Mastery: " + series.masteryDescription())
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
