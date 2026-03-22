package com.sevendaystominecraft.item;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TreatmentItem extends Item {

    private final String[] curedDebuffs;

    public TreatmentItem(Properties properties, String... curedDebuffs) {
        super(properties);
        this.curedDebuffs = curedDebuffs;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) {
            return InteractionResult.PASS;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        boolean curedAny = false;

        for (String debuff : curedDebuffs) {
            if (stats.hasDebuff(debuff)) {
                stats.removeDebuff(debuff);
                curedAny = true;
            }
        }

        if (!curedAny) {
            return InteractionResult.PASS;
        }

        int fieldMedicRank = stats.getPerkRank("field_medic");
        if (fieldMedicRank > 0) {
            float healAmount = 2.0f * fieldMedicRank;
            player.heal(healAmount);
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PlayerStatsHandler.sendStatsToClient(serverPlayer, stats);
        }

        return InteractionResult.CONSUME;
    }
}
