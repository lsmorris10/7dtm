package com.sevendaystominecraft.item.weapon;

import com.sevendaystominecraft.entity.projectile.GrenadeEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GrenadeItem extends Item implements GeoItem {

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.grenade.idle");
    private static final RawAnimation ANIM_PIN_PULL_AND_THROW = RawAnimation.begin()
            .thenPlay("animation.grenade.pin_pull")
            .thenPlay("animation.grenade.throw");

    private static final float THROW_SPEED = 1.0f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GrenadeItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private software.bernie.geckolib.renderer.GeoItemRenderer<?> renderer;

            @Override
            public software.bernie.geckolib.renderer.GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new com.sevendaystominecraft.client.renderer.GrenadeItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "grenade_controller", 2, state -> {
            var currentRaw = state.getController().getCurrentRawAnimation();
            if (currentRaw != null && !currentRaw.getAnimationStages().isEmpty()) {
                String animName = currentRaw.getAnimationStages().get(0).animationName();
                if ((animName.equals("animation.grenade.pin_pull")
                        || animName.equals("animation.grenade.throw"))
                        && !state.getController().hasAnimationFinished()) {
                    return PlayState.CONTINUE;
                }
            }
            state.setAndContinue(ANIM_IDLE);
            return PlayState.CONTINUE;
        }).triggerableAnim("animation.grenade.pin_pull_and_throw", ANIM_PIN_PULL_AND_THROW));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(held)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 1.0f, 1.5f);

            triggerAnim(player, GeoItem.getId(held), "grenade_controller", "animation.grenade.pin_pull_and_throw");

            GrenadeEntity grenade = new GrenadeEntity(level, player);
            grenade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, THROW_SPEED, 0.5f);
            level.addFreshEntity(grenade);

            if (!player.isCreative()) {
                held.shrink(1);
            }
        }

        player.getCooldowns().addCooldown(held, 20);
        return InteractionResult.SUCCESS;
    }
}
