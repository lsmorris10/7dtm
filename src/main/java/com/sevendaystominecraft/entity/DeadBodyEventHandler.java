package com.sevendaystominecraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeadBodyEventHandler {

    private static final Map<UUID, ItemStack> PENDING_COIN_BAGS = new ConcurrentHashMap<>();

    public static void stashCoinBag(UUID playerUUID, ItemStack coinBag) {
        PENDING_COIN_BAGS.put(playerUUID, coinBag);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        Collection<ItemEntity> drops = event.getDrops();
        ItemStack pendingCoinBag = PENDING_COIN_BAGS.remove(player.getUUID());

        if (drops.isEmpty() && (pendingCoinBag == null || pendingCoinBag.isEmpty())) {
            return;
        }

        event.setCanceled(true);

        DeadBodyEntity body = new DeadBodyEntity(ModEntities.DEAD_BODY.get(), player.level());
        body.setPos(player.getX(), player.getY(), player.getZ());
        body.setOwnerName(player.getName().getString());
        body.setOwnerUUID(player.getUUID());

        int slot = 0;
        for (ItemEntity itemEntity : drops) {
            if (slot >= DeadBodyEntity.SLOT_COUNT) break;
            body.getInventory().setItem(slot, itemEntity.getItem().copy());
            slot++;
        }

        if (pendingCoinBag != null && !pendingCoinBag.isEmpty() && slot < DeadBodyEntity.SLOT_COUNT) {
            body.getInventory().setItem(DeadBodyEntity.SLOT_COUNT - 1, pendingCoinBag);
        }

        player.level().addFreshEntity(body);
    }
}
