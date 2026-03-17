package com.sevendaystominecraft.item.weapon;

import com.sevendaystominecraft.entity.projectile.BulletEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class RangedWeaponItem extends Item {

    private final float bulletDamage;
    private final int cooldownTicks;
    private final float projectileSpeed;
    private final float inaccuracy;
    private final Supplier<Item> ammoSupplier;

    public RangedWeaponItem(Properties properties, float bulletDamage, int cooldownTicks,
                            float projectileSpeed, float inaccuracy, Supplier<Item> ammoSupplier) {
        super(properties);
        this.bulletDamage = bulletDamage;
        this.cooldownTicks = cooldownTicks;
        this.projectileSpeed = projectileSpeed;
        this.inaccuracy = inaccuracy;
        this.ammoSupplier = ammoSupplier;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(held)) {
            return InteractionResult.FAIL;
        }

        if (!player.isCreative()) {
            Item ammoItem = ammoSupplier.get();
            ItemStack ammoStack = findAmmo(player, ammoItem);
            if (ammoStack.isEmpty()) {
                return InteractionResult.FAIL;
            }
            ammoStack.shrink(1);
        }

        if (level instanceof ServerLevel sl) {
            BulletEntity bullet = new BulletEntity(level, player, bulletDamage);
            Vec3 look = player.getLookAngle();
            bullet.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            bullet.shoot(look.x, look.y, look.z, projectileSpeed, inaccuracy);
            sl.addFreshEntity(bullet);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.5f, 1.2f);

        player.getCooldowns().addCooldown(held, cooldownTicks);

        if (held.isDamageableItem()) {
            held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }

        return InteractionResult.SUCCESS;
    }

    private ItemStack findAmmo(Player player, Item ammoItem) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ammoItem)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
