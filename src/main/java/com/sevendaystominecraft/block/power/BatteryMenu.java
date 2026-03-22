package com.sevendaystominecraft.block.power;

import com.sevendaystominecraft.menu.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BatteryMenu extends AbstractContainerMenu {

    private final BatteryBankBlockEntity blockEntity;
    private final ContainerData data;
    private static final int DATA_COUNT = 2;

    public BatteryMenu(int containerId, Inventory playerInv, BatteryBankBlockEntity blockEntity) {
        super(ModMenuTypes.BATTERY_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            private final int[] clientCache = new int[DATA_COUNT];

            @Override
            public int get(int index) {
                if (index < 0 || index >= DATA_COUNT) return 0;
                if (playerInv.player.level().isClientSide) {
                    return clientCache[index];
                }
                return switch (index) {
                    case 0 -> blockEntity.getStoredEnergy();
                    case 1 -> blockEntity.getMaxEnergy();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < DATA_COUNT) {
                    clientCache[index] = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
        addDataSlots(data);

        int playerInvY = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public static BatteryMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof BatteryBankBlockEntity batBE) {
            return new BatteryMenu(containerId, playerInv, batBE);
        }
        return new BatteryMenu(containerId, playerInv, new BatteryBankBlockEntity(pos,
                playerInv.player.level().getBlockState(pos)));
    }

    public int getStoredEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }
}
