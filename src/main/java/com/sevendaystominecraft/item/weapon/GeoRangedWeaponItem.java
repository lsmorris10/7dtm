package com.sevendaystominecraft.item.weapon;

import com.sevendaystominecraft.entity.projectile.BulletEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import java.util.function.Supplier;

public class GeoRangedWeaponItem extends Item implements GeoItem {

    public static final String TAG_AMMO = "CurrentAmmo";
    public static final String TAG_RELOADING = "IsReloading";
    public static final String TAG_RELOAD_TICKS = "ReloadTicksLeft";
    public static final String TAG_RACKING = "IsRacking";

    private final float bulletDamage;
    private final int cooldownTicks;
    private final float projectileSpeed;
    private final float inaccuracy;
    private final Supplier<Item> ammoSupplier;
    private final int magazineCapacity;
    private final int reloadTicks;
    private final WeaponType weaponType;
    private final Supplier<SoundEvent> fireSoundSupplier;
    private final double bulletGravity;
    private final int bulletMaxLife;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final float ADS_FOV_MULTIPLIER = 0.5f;
    public static final float ADS_INACCURACY_MULTIPLIER = 0.3f;

    public enum WeaponType {
        AK47("animation.ak47"),
        PISTOL_9MM("animation.pistol_9mm");

        public final String animPrefix;

        WeaponType(String animPrefix) {
            this.animPrefix = animPrefix;
        }
    }

    public GeoRangedWeaponItem(Properties properties, float bulletDamage, int cooldownTicks,
                               float projectileSpeed, float inaccuracy, Supplier<Item> ammoSupplier,
                               int magazineCapacity, int reloadTicks, WeaponType weaponType,
                               Supplier<SoundEvent> fireSoundSupplier) {
        this(properties, bulletDamage, cooldownTicks, projectileSpeed, inaccuracy, ammoSupplier,
                magazineCapacity, reloadTicks, weaponType, fireSoundSupplier, 0.01, 60);
    }

    public GeoRangedWeaponItem(Properties properties, float bulletDamage, int cooldownTicks,
                               float projectileSpeed, float inaccuracy, Supplier<Item> ammoSupplier,
                               int magazineCapacity, int reloadTicks, WeaponType weaponType,
                               Supplier<SoundEvent> fireSoundSupplier,
                               double bulletGravity, int bulletMaxLife) {
        super(properties);
        this.bulletDamage = bulletDamage;
        this.cooldownTicks = cooldownTicks;
        this.projectileSpeed = projectileSpeed;
        this.inaccuracy = inaccuracy;
        this.ammoSupplier = ammoSupplier;
        this.magazineCapacity = magazineCapacity;
        this.reloadTicks = reloadTicks;
        this.weaponType = weaponType;
        this.fireSoundSupplier = fireSoundSupplier;
        this.bulletGravity = bulletGravity;
        this.bulletMaxLife = bulletMaxLife;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private software.bernie.geckolib.renderer.GeoItemRenderer<?> renderer;

            @Override
            public software.bernie.geckolib.renderer.GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = createRenderer();
                }
                return this.renderer;
            }
        });
    }

    protected software.bernie.geckolib.renderer.GeoItemRenderer<?> createRenderer() {
        return new com.sevendaystominecraft.client.renderer.GeoRangedWeaponRenderer(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String prefix = weaponType.animPrefix;
        RawAnimation idleAnim = RawAnimation.begin().thenLoop(prefix + ".idle");
        RawAnimation fireAnim = RawAnimation.begin().thenPlay(prefix + ".fire");
        RawAnimation reloadAnim = RawAnimation.begin().thenPlay(prefix + ".reload");
        RawAnimation rackAnim = RawAnimation.begin().thenPlay(prefix + ".rack");

        controllers.add(new AnimationController<>(this, "main_controller", 2, state -> {
            var currentRaw = state.getController().getCurrentRawAnimation();
            if (currentRaw != null && !currentRaw.getAnimationStages().isEmpty()) {
                String animName = currentRaw.getAnimationStages().get(0).animationName();
                if ((animName.equals(prefix + ".fire")
                        || animName.equals(prefix + ".reload")
                        || animName.equals(prefix + ".rack"))
                        && !state.getController().hasAnimationFinished()) {
                    return PlayState.CONTINUE;
                }
            }
            state.setAndContinue(idleAnim);
            return PlayState.CONTINUE;
        }).triggerableAnim(prefix + ".fire", fireAnim)
          .triggerableAnim(prefix + ".reload", reloadAnim)
          .triggerableAnim(prefix + ".rack", rackAnim));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public void fireWeapon(Level level, Player player, boolean isADS) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack held = player.getItemInHand(hand);

        if (!(held.getItem() instanceof GeoRangedWeaponItem)) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(held)) {
            return;
        }

        if (isReloading(held)) {
            return;
        }

        int currentAmmo = getCurrentAmmo(held);

        if (currentAmmo < 0) {
            currentAmmo = magazineCapacity;
            setCurrentAmmo(held, currentAmmo);
        }

        if (currentAmmo == 0) {
            if (!level.isClientSide()) {
                startReload(level, player, hand, held);
            }
            return;
        }

        if (!player.isCreative()) {
            setCurrentAmmo(held, currentAmmo - 1);
        }

        float currentInaccuracy = isADS ? inaccuracy * ADS_INACCURACY_MULTIPLIER : inaccuracy;

        if (level instanceof ServerLevel sl) {
            BulletEntity bullet = new BulletEntity(level, player, bulletDamage, bulletGravity, bulletMaxLife);
            Vec3 look = player.getLookAngle();
            bullet.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            bullet.shoot(look.x, look.y, look.z, projectileSpeed, currentInaccuracy);
            sl.addFreshEntity(bullet);
        }

        SoundEvent fireSound = fireSoundSupplier != null ? fireSoundSupplier.get() : SoundEvents.CROSSBOW_SHOOT;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                fireSound, SoundSource.PLAYERS, 1.5f, 1.2f);

        player.getCooldowns().addCooldown(held, cooldownTicks);

        if (!level.isClientSide()) {
            triggerAnim(player, GeoItem.getId(held), "main_controller", weaponType.animPrefix + ".fire");

            if (getCurrentAmmo(held) == 0 && weaponType == WeaponType.AK47) {
                triggerAnim(player, GeoItem.getId(held), "main_controller", weaponType.animPrefix + ".rack");
            }
        }

        if (held.isDamageableItem()) {
            held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide() && entity instanceof Player player && isReloading(stack)) {
            int ticksLeft = getReloadTicksLeft(stack);
            if (ticksLeft <= 0) {
                finishReload(stack, player, selected);
            } else {
                setReloadTicksLeft(stack, ticksLeft - 1);
                if (ticksLeft == reloadTicks && selected && weaponType == WeaponType.AK47) {
                    triggerAnim(player, GeoItem.getId(stack), "main_controller", weaponType.animPrefix + ".rack");
                }
            }
        }
    }

    private void startReload(Level level, Player player, InteractionHand hand, ItemStack held) {
        Item ammoItem = ammoSupplier.get();
        if (!hasAmmoInInventory(player, ammoItem)) {
            return;
        }
        setReloading(held, true);
        setReloadTicksLeft(held, reloadTicks);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 1.0f, 0.8f);
        triggerAnim(player, GeoItem.getId(held), "main_controller", weaponType.animPrefix + ".reload");
    }

    private void finishReload(ItemStack stack, Player player, boolean selected) {
        if (player == null) return;
        Item ammoItem = ammoSupplier.get();
        int toLoad = magazineCapacity;
        if (!player.isCreative()) {
            toLoad = consumeAmmo(player, ammoItem, magazineCapacity);
        }
        setCurrentAmmo(stack, toLoad);
        setReloading(stack, false);
        setReloadTicksLeft(stack, 0);
        if (selected && weaponType == WeaponType.AK47) {
            triggerAnim(player, GeoItem.getId(stack), "main_controller", weaponType.animPrefix + ".rack");
        }
    }

    private boolean hasAmmoInInventory(Player player, Item ammoItem) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ammoItem)) return true;
        }
        return false;
    }

    private int consumeAmmo(Player player, Item ammoItem, int needed) {
        int consumed = 0;
        for (int i = 0; i < player.getInventory().getContainerSize() && consumed < needed; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ammoItem)) {
                int take = Math.min(stack.getCount(), needed - consumed);
                stack.shrink(take);
                consumed += take;
            }
        }
        return consumed;
    }

    public static int getCurrentAmmo(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomTag(stack);
        return tag.contains(TAG_AMMO) ? tag.getInt(TAG_AMMO) : -1;
    }

    public static void setCurrentAmmo(ItemStack stack, int ammo) {
        CompoundTag tag = getOrCreateCustomTag(stack);
        tag.putInt(TAG_AMMO, ammo);
        applyCustomTag(stack, tag);
    }

    public static boolean isReloading(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomTag(stack);
        return tag.getBoolean(TAG_RELOADING);
    }

    public static void setReloading(ItemStack stack, boolean reloading) {
        CompoundTag tag = getOrCreateCustomTag(stack);
        tag.putBoolean(TAG_RELOADING, reloading);
        applyCustomTag(stack, tag);
    }

    public static int getReloadTicksLeft(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomTag(stack);
        return tag.contains(TAG_RELOAD_TICKS) ? tag.getInt(TAG_RELOAD_TICKS) : 0;
    }

    public static void setReloadTicksLeft(ItemStack stack, int ticks) {
        CompoundTag tag = getOrCreateCustomTag(stack);
        tag.putInt(TAG_RELOAD_TICKS, ticks);
        applyCustomTag(stack, tag);
    }

    private static CompoundTag getOrCreateCustomTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return new CompoundTag();
        return customData.copyTag();
    }

    private static void applyCustomTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public int getMagazineCapacity() {
        return magazineCapacity;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public float getInaccuracy() {
        return inaccuracy;
    }
}
