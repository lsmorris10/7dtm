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

public class GeneratorMenu extends AbstractContainerMenu {

    private final GeneratorBankBlockEntity blockEntity;
    private final ContainerData data;
    private static final int CONTAINER_SLOTS = 1;
    private static final int DATA_COUNT = 3;

    public GeneratorMenu(int containerId, Inventory playerInv, GeneratorBankBlockEntity blockEntity) {
        super(ModMenuTypes.GENERATOR_MENU.get(), containerId);
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
                    case 0 -> blockEntity.getBurnTime();
                    case 1 -> blockEntity.getBurnTimeTotal();
                    case 2 -> blockEntity.getPowerOutput();
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

        PowerSlotContainer container = new PowerSlotContainer(blockEntity);
        addSlot(new Slot(container, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GeneratorBankBlockEntity.isValidFuel(stack);
            }
        });

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

    public static GeneratorMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof GeneratorBankBlockEntity genBE) {
            return new GeneratorMenu(containerId, playerInv, genBE);
        }
        return new GeneratorMenu(containerId, playerInv, new GeneratorBankBlockEntity(pos,
                playerInv.player.level().getBlockState(pos)));
    }

    public int getBurnTime() {
        return data.get(0);
    }

    public int getBurnTimeTotal() {
        return data.get(1);
    }

    public int getPowerOutput() {
        return data.get(2);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < CONTAINER_SLOTS) {
            if (!moveItemStackTo(stack, CONTAINER_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (GeneratorBankBlockEntity.isValidFuel(stack)) {
                if (!moveItemStackTo(stack, 0, CONTAINER_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }
}
