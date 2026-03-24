package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.block.workstation.recipe.WorkstationCraftingRecipe;
import com.sevendaystominecraft.menu.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class WorkstationMenu extends AbstractContainerMenu {

    private final WorkstationBlockEntity blockEntity;
    private final ContainerData data;
    private final int playerInvY;
    private java.util.function.Consumer<java.util.List<com.sevendaystominecraft.network.WorkstationRecipeListPayload.Entry>> recipeListConsumer;

    public void setRecipeListConsumer(java.util.function.Consumer<java.util.List<com.sevendaystominecraft.network.WorkstationRecipeListPayload.Entry>> consumer) {
        this.recipeListConsumer = consumer;
    }

    public void acceptRecipeList(java.util.List<com.sevendaystominecraft.network.WorkstationRecipeListPayload.Entry> entries) {
        if (recipeListConsumer != null) {
            recipeListConsumer.accept(entries);
        }
    }

    public WorkstationMenu(int containerId, Inventory playerInv, WorkstationBlockEntity blockEntity) {
        super(ModMenuTypes.WORKSTATION_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        blockEntity.setLastCrafter(playerInv.player);

        this.data = new ContainerData() {
            private final int[] clientCache = new int[4];

            @Override
            public int get(int index) {
                if (index < 0 || index >= 4) return 0;
                if (playerInv.player.level().isClientSide) {
                    return clientCache[index];
                }
                return switch (index) {
                    case 0 -> blockEntity.getBurnTime();
                    case 1 -> blockEntity.getBurnTimeTotal();
                    case 2 -> blockEntity.getCraftProgress();
                    case 3 -> blockEntity.getCraftTimeTotal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < 4) {
                    clientCache[index] = value;
                }
            }

            @Override
            public int getCount() { return 4; }
        };
        addDataSlots(this.data);

        WorkstationType type = blockEntity.getWorkstationType();
        WorkstationSlotContainer container = new WorkstationSlotContainer(blockEntity);

        int slotIndex = 0;
        for (int i = 0; i < type.getInputSlots(); i++) {
            int col = i % 3;
            int row = i / 3;
            addSlot(new WorkstationSlot(container, slotIndex++, 26 + col * 18, 17 + row * 18, true));
        }

        int outputStartX = 116;
        for (int i = 0; i < type.getOutputSlots(); i++) {
            int col = i % 3;
            int row = i / 3;
            addSlot(new WorkstationSlot(container, slotIndex++, outputStartX + col * 18, 17 + row * 18, false));
        }

        int inputRows = (int) Math.ceil((double) type.getInputSlots() / 3.0);
        int outputRows = (int) Math.ceil((double) type.getOutputSlots() / 3.0);
        int maxSlotRows = Math.max(inputRows, outputRows);
        int workstationBottom = 17 + maxSlotRows * 18;

        if (type.usesFuel()) {
            int fuelY = workstationBottom + 4;
            for (int i = 0; i < type.getFuelSlots(); i++) {
                addSlot(new FuelSlot(container, slotIndex++, 26 + i * 18, fuelY));
            }
            workstationBottom = fuelY + 18;
        }

        this.playerInvY = workstationBottom + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public int getPlayerInvY() { return playerInvY; }

    public static WorkstationMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        WorkstationType type = WorkstationType.WORKBENCH;
        if (buf.readableBytes() >= 4) {
            int ordinal = buf.readInt();
            WorkstationType[] values = WorkstationType.values();
            if (ordinal >= 0 && ordinal < values.length) {
                type = values[ordinal];
            }
        }
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof WorkstationBlockEntity wbe) {
            return new WorkstationMenu(containerId, playerInv, wbe);
        }
        return new WorkstationMenu(containerId, playerInv, new WorkstationBlockEntity(pos,
                playerInv.player.level().getBlockState(pos), type));
    }

    public WorkstationBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int stationSlots = blockEntity != null ? blockEntity.getWorkstationType().getTotalSlots() : 0;

        if (index < stationSlots) {
            if (!moveItemStackTo(stack, stationSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, stationSlots, false)) {
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

    public void handleRecipeSelect(ServerPlayer player, ResourceLocation recipeId) {
        if (blockEntity == null) return;

        WorkstationType type = blockEntity.getWorkstationType();
        if (player.getServer() == null) return;
        var optionalHolder = player.getServer().getRecipeManager()
                .byKey(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.RECIPE, recipeId));

        if (optionalHolder.isEmpty()) return;
        RecipeHolder<?> rawHolder = optionalHolder.get();
        if (!(rawHolder.value() instanceof WorkstationCraftingRecipe recipe)) return;
        if (!rawHolder.value().getType().equals(type.getRecipeType())) return;

        for (SizedIngredient sized : recipe.getIngredients()) {
            int available = 0;
            for (int invSlot = 0; invSlot < player.getInventory().getContainerSize(); invSlot++) {
                ItemStack invStack = player.getInventory().getItem(invSlot);
                if (!invStack.isEmpty() && sized.ingredient().test(invStack)) {
                    available += invStack.getCount();
                }
            }
            for (int i = 0; i < type.getInputSlots() && i < blockEntity.getContainerSize(); i++) {
                ItemStack existing = blockEntity.getItem(i);
                if (!existing.isEmpty() && sized.ingredient().test(existing)) {
                    available += existing.getCount();
                }
            }
            if (available < sized.count()) return;
        }

        for (int i = 0; i < type.getInputSlots() && i < blockEntity.getContainerSize(); i++) {
            ItemStack existing = blockEntity.getItem(i);
            if (!existing.isEmpty()) {
                if (!player.getInventory().add(existing.copy())) return;
                blockEntity.setItem(i, ItemStack.EMPTY);
            }
        }

        int slotIndex = 0;
        for (SizedIngredient sized : recipe.getIngredients()) {
            if (slotIndex >= type.getInputSlots()) break;

            int needed = sized.count();
            ItemStack gathered = ItemStack.EMPTY;

            for (int invSlot = 0; invSlot < player.getInventory().getContainerSize() && needed > 0; invSlot++) {
                ItemStack invStack = player.getInventory().getItem(invSlot);
                if (!invStack.isEmpty() && sized.ingredient().test(invStack)) {
                    int take = Math.min(needed, invStack.getCount());
                    if (gathered.isEmpty()) {
                        gathered = invStack.copyWithCount(take);
                    } else {
                        gathered.grow(take);
                    }
                    invStack.shrink(take);
                    needed -= take;
                }
            }

            if (!gathered.isEmpty()) {
                blockEntity.setItem(slotIndex, gathered);
            }
            slotIndex++;
        }

        broadcastChanges();
    }

    private static class WorkstationSlot extends Slot {
        private final boolean isInput;

        public WorkstationSlot(WorkstationSlotContainer container, int index, int x, int y, boolean isInput) {
            super(container, index, x, y);
            this.isInput = isInput;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isInput;
        }
    }

    private static class FuelSlot extends Slot {
        public FuelSlot(WorkstationSlotContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return WorkstationBlockEntity.isValidFuel(stack);
        }
    }

    static class WorkstationSlotContainer implements net.minecraft.world.Container {
        private final WorkstationBlockEntity be;

        WorkstationSlotContainer(WorkstationBlockEntity be) {
            this.be = be;
        }

        @Override public int getContainerSize() { return be.getContainerSize(); }
        @Override public boolean isEmpty() {
            for (int i = 0; i < be.getContainerSize(); i++) {
                if (!be.getItem(i).isEmpty()) return false;
            }
            return true;
        }
        @Override public ItemStack getItem(int slot) { return be.getItem(slot); }
        @Override public ItemStack removeItem(int slot, int count) { return be.removeItem(slot, count); }
        @Override public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = be.getItem(slot);
            be.setItem(slot, ItemStack.EMPTY);
            return stack;
        }
        @Override public void setItem(int slot, ItemStack stack) { be.setItem(slot, stack); }
        @Override public void setChanged() { be.setChanged(); }
        @Override public boolean stillValid(Player player) { return be.stillValid(player); }
        @Override public void clearContent() {
            for (int i = 0; i < be.getContainerSize(); i++) {
                be.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
