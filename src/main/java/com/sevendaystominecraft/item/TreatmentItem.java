package com.sevendaystominecraft.item;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

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

        com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                com.sevendaystominecraft.sound.ModSounds.PLAYER_BANDAGE, player,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Use when afflicted to cure:"));
        for (String debuff : curedDebuffs) {
            tooltip.add(Component.literal("  §a✔ " + formatDebuffName(debuff)));
        }
    }

    private static String formatDebuffName(String debuffId) {
        return switch (debuffId) {
            case "bleeding" -> "Bleeding";
            case "infection_1" -> "Infection I";
            case "infection_2" -> "Infection II";
            case "dysentery" -> "Dysentery";
            case "sprain" -> "Sprain";
            case "fracture" -> "Fracture";
            case "concussion" -> "Concussion";
            case "burn" -> "Burn";
            case "hypothermia" -> "Hypothermia";
            case "hyperthermia" -> "Hyperthermia";
            default -> debuffId.substring(0, 1).toUpperCase() + debuffId.substring(1).replace('_', ' ');
        };
    }
}
