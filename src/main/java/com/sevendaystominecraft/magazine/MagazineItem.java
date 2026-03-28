package com.sevendaystominecraft.magazine;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.client.renderer.item.MagazineRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import java.util.List;
import java.util.function.Consumer;

public class MagazineItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String seriesId;
    private final int issue;

    public MagazineItem(Properties properties, String seriesId, int issue) {
        super(properties);
        this.seriesId = seriesId;
        this.issue = issue;
        software.bernie.geckolib.animatable.SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public String getSeriesId() {
        return seriesId;
    }

    public int getIssue() {
        return issue;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS,
                    1.0f, 0.8f + level.random.nextFloat() * 0.4f, false);
            return InteractionResult.SUCCESS;
        }

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

        boolean mastery = magData.hasCompletedSeries(seriesId);
        if (mastery) {
            player.displayClientMessage(
                    Component.literal("★ MASTERY COMPLETE: " + series.displayName() + " — " + series.masteryDescription())
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.sevendaystominecraft.advancement.RecipeUnlockManager.onMagazineRead(
                    serverPlayer, seriesId, issue, mastery);
            triggerAnim(player, software.bernie.geckolib.animatable.GeoItem.getOrAssignId(held, serverPlayer.serverLevel()), "magazine_controller", "read");
        }

        return InteractionResult.SUCCESS;
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

    // GeckoLib Implementation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "magazine_controller", state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle")))
                .triggerableAnim("read", RawAnimation.begin().thenPlay("read").thenLoop("idle")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private MagazineRenderer renderer;

            @Override
            public software.bernie.geckolib.renderer.GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new MagazineRenderer();

                return this.renderer;
            }
        });
    }
}
