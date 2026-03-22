package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class UpgradeableBlock extends Block {

    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 5);
    public static final BooleanProperty UPGRADING = BooleanProperty.create("upgrading");

    private static final int UPGRADE_DELAY_TICKS = 30;

    public UpgradeableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TIER, 0).setValue(UPGRADING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, UPGRADING);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        int tier = state.getValue(TIER);
        float hardness = getDestroySpeedForTier(tier);
        if (hardness <= 0) return 0.0f;
        return player.getDestroySpeed(state) / hardness / 30.0f;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        int tier = state.getValue(TIER);
        return getBlastResistanceForTier(tier);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(UPGRADING)) {
            int currentTier = state.getValue(TIER);
            level.setBlock(pos, state.setValue(TIER, currentTier + 1).setValue(UPGRADING, false), 3);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 1.2f);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.PASS;

        Item heldItem = stack.getItem();
        boolean isUpgradeTool = heldItem == ModItems.REPAIR_HAMMER.get()
                || heldItem == ModItems.WRENCH.get();
        if (!isUpgradeTool) return InteractionResult.PASS;

        int currentTier = state.getValue(TIER);
        if (currentTier >= 5) {
            player.displayClientMessage(Component.literal("This block is already at maximum tier."), true);
            return InteractionResult.CONSUME;
        }

        if (state.getValue(UPGRADING)) {
            player.displayClientMessage(Component.literal("Upgrade in progress..."), true);
            return InteractionResult.CONSUME;
        }

        UpgradeCost cost = getUpgradeCost(currentTier);
        if (cost == null) return InteractionResult.PASS;

        if (!hasRequiredMaterials(player, cost)) {
            player.displayClientMessage(Component.literal("Missing materials: " + cost.amount + "x " + cost.materialName), true);
            return InteractionResult.CONSUME;
        }

        consumeMaterials(player, cost);

        level.setBlock(pos, state.setValue(UPGRADING, true), 3);
        level.scheduleTick(pos, this, UPGRADE_DELAY_TICKS);

        level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6f, 0.8f);

        String tierName = getTierName(currentTier + 1);
        player.displayClientMessage(Component.literal("Upgrading to " + tierName + "..."), true);

        stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                : net.minecraft.world.entity.EquipmentSlot.OFFHAND);

        return InteractionResult.SUCCESS;
    }

    private boolean hasRequiredMaterials(Player player, UpgradeCost cost) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() == cost.material.get()) {
                count += invStack.getCount();
            }
        }
        return count >= cost.amount;
    }

    private void consumeMaterials(Player player, UpgradeCost cost) {
        int remaining = cost.amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() == cost.material.get()) {
                int take = Math.min(remaining, invStack.getCount());
                invStack.shrink(take);
                remaining -= take;
            }
        }
    }

    private UpgradeCost getUpgradeCost(int currentTier) {
        return switch (currentTier) {
            case 0 -> new UpgradeCost(ModItems.NAIL, 4, "Nails");
            case 1 -> new UpgradeCost(() -> Items.COBBLESTONE, 10, "Cobblestone");
            case 2 -> new UpgradeCost(ModItems.CONCRETE_MIX, 8, "Concrete Mix");
            case 3 -> new UpgradeCost(ModItems.FORGED_IRON, 4, "Forged Iron");
            case 4 -> new UpgradeCost(ModItems.FORGED_STEEL, 4, "Forged Steel");
            default -> null;
        };
    }

    public static String getTierName(int tier) {
        return switch (tier) {
            case 0 -> "Wood Frame";
            case 1 -> "Reinforced Wood";
            case 2 -> "Cobblestone";
            case 3 -> "Concrete";
            case 4 -> "Reinforced Concrete";
            case 5 -> "Steel";
            default -> "Unknown";
        };
    }

    public static float getHPForTier(int tier) {
        return switch (tier) {
            case 0 -> 100;
            case 1 -> 250;
            case 2 -> 500;
            case 3 -> 1500;
            case 4 -> 3000;
            case 5 -> 5000;
            default -> 100;
        };
    }

    public static float getBlastResistanceForTier(int tier) {
        return switch (tier) {
            case 0 -> 2.0f;
            case 1 -> 4.0f;
            case 2 -> 6.0f;
            case 3 -> 12.0f;
            case 4 -> 20.0f;
            case 5 -> 30.0f;
            default -> 2.0f;
        };
    }

    public static float getDestroySpeedForTier(int tier) {
        return switch (tier) {
            case 0 -> 2.0f;
            case 1 -> 3.0f;
            case 2 -> 4.0f;
            case 3 -> 8.0f;
            case 4 -> 15.0f;
            case 5 -> 25.0f;
            default -> 2.0f;
        };
    }

    private record UpgradeCost(Supplier<Item> material, int amount, String materialName) {}
}
