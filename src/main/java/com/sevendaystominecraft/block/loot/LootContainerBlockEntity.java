package com.sevendaystominecraft.block.loot;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.config.LootConfig;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.item.QualityTier;
import com.sevendaystominecraft.loot.LootStageCalculator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.function.Supplier;

public class LootContainerBlockEntity extends BlockEntity {

    private LootContainerType containerType;
    private NonNullList<ItemStack> items;
    private long lastLootedGameTime = -1;
    private boolean hasBeenLooted = false;
    private int territoryTier = 0;

    public LootContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOOT_CONTAINER_BE.get(), pos, state);
        this.containerType = resolveTypeFromBlock(state);
        this.items = NonNullList.withSize(containerType.getSlotCount(), ItemStack.EMPTY);
    }

    public LootContainerBlockEntity(BlockPos pos, BlockState state, LootContainerType type) {
        super(ModBlockEntities.LOOT_CONTAINER_BE.get(), pos, state);
        this.containerType = type;
        this.items = NonNullList.withSize(type.getSlotCount(), ItemStack.EMPTY);
    }

    private static LootContainerType resolveTypeFromBlock(BlockState state) {
        if (state.getBlock() instanceof LootContainerBlock lcb) {
            return lcb.getContainerType();
        }
        return LootContainerType.CARDBOARD_BOX;
    }

    public LootContainerType getContainerType() {
        return containerType;
    }

    public void setTerritoryTier(int tier) {
        this.territoryTier = Math.max(0, Math.min(5, tier));
        setChanged();
    }

    public int getTerritoryTier() {
        return territoryTier;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getContainerSize() {
        return items.size();
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) return;
        items.set(slot, stack);
        setChanged();
    }

    public ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        setChanged();
        return result;
    }

    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    public void tryGenerateLoot(ServerPlayer player) {
        if (level == null) return;

        int respawnDays = LootConfig.INSTANCE.respawnDays.get();
        int containerRespawn = containerType.getDefaultRespawnDays();
        if (containerRespawn == 0) {
            if (hasBeenLooted) return;
        } else {
            if (respawnDays == 0 && hasBeenLooted) return;
            int effectiveRespawn = respawnDays > 0 ? respawnDays : containerRespawn;

            if (hasBeenLooted) {
                long currentDay = level.getDayTime() / SevenDaysConstants.DAY_LENGTH;
                long lootedDay = lastLootedGameTime / SevenDaysConstants.DAY_LENGTH;
                if (currentDay - lootedDay < effectiveRespawn) return;
            }
        }

        int lootStage = LootStageCalculator.calculate(player);
        if (territoryTier > 0) {
            lootStage = Math.min(100, lootStage + (territoryTier - 1) * 15);
        }
        double abundance = LootConfig.INSTANCE.abundanceMultiplier.get();
        boolean qualityEnabled = LootConfig.INSTANCE.qualityScaling.get();

        generateLootForType(lootStage, abundance, qualityEnabled);

        hasBeenLooted = true;
        lastLootedGameTime = level.getDayTime();
        setChanged();
    }

    private void generateLootForType(int lootStage, double abundance, boolean qualityEnabled) {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        Random random = new Random();
        int itemCount = Math.max(1, (int) (getBaseItemCount() * abundance));
        itemCount = Math.min(itemCount, items.size());

        for (int i = 0; i < itemCount; i++) {
            ItemStack stack = generateItemForType(containerType, lootStage, random);
            if (!stack.isEmpty()) {
                if (qualityEnabled) {
                    QualityTier tier = QualityTier.randomForLootStage(lootStage, random);
                    CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                    tag.putInt("QualityTier", tier.getLevel());
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(tag));
                }
                items.set(i, stack);
            }
        }
        setChanged();
    }

    private int getBaseItemCount() {
        return switch (containerType) {
            case TRASH_PILE -> 2;
            case CARDBOARD_BOX -> 3;
            case GUN_SAFE -> 4;
            case MUNITIONS_BOX -> 5;
            case SUPPLY_CRATE -> 8;
            case KITCHEN_CABINET -> 3;
            case MEDICINE_CABINET -> 2;
            case BOOKSHELF -> 2;
        };
    }

    private static ItemStack generateItemForType(LootContainerType type, int lootStage, Random random) {
        return switch (type) {
            case TRASH_PILE -> pickRandom(random, lootStage,
                    ModItems.IRON_SCRAP, ModItems.DUCT_TAPE, () -> Items.CLAY_BALL);
            case CARDBOARD_BOX -> pickRandom(random, lootStage,
                    ModItems.IRON_SCRAP, ModItems.MECHANICAL_PARTS, ModItems.DUCT_TAPE, ModItems.POLYMER);
            case GUN_SAFE -> pickRandom(random, lootStage,
                    ModItems.MECHANICAL_PARTS, ModItems.FORGED_IRON, ModItems.FORGED_STEEL, ModItems.DUKES_CASINO_TOKEN);
            case MUNITIONS_BOX -> pickRandom(random, lootStage,
                    ModItems.IRON_SCRAP, ModItems.LEAD, ModItems.NITRATE, ModItems.MECHANICAL_PARTS, ModItems.FORGED_STEEL);
            case SUPPLY_CRATE -> pickRandom(random, lootStage,
                    ModItems.FORGED_IRON, ModItems.FORGED_STEEL, ModItems.MECHANICAL_PARTS,
                    ModItems.ELECTRICAL_PARTS, ModItems.POLYMER, ModItems.DUKES_CASINO_TOKEN);
            case KITCHEN_CABINET -> pickRandomVanilla(random,
                    Items.BREAD, Items.APPLE, Items.COOKED_BEEF, Items.CARROT);
            case MEDICINE_CABINET -> pickRandomVanilla(random,
                    Items.GOLDEN_APPLE, Items.GLISTERING_MELON_SLICE);
            case BOOKSHELF -> pickRandomVanilla(random,
                    Items.BOOK, Items.PAPER, Items.WRITABLE_BOOK);
        };
    }

    @SafeVarargs
    private static ItemStack pickRandom(Random random, int lootStage, Supplier<Item>... items) {
        Supplier<Item> chosen = items[random.nextInt(items.length)];
        int count = 1 + random.nextInt(Math.max(1, lootStage / 20 + 1));
        return new ItemStack(chosen.get(), count);
    }

    private static ItemStack pickRandomVanilla(Random random, Item... items) {
        Item chosen = items[random.nextInt(items.length)];
        return new ItemStack(chosen, 1 + random.nextInt(3));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putLong("LastLootedGameTime", lastLootedGameTime);
        tag.putBoolean("HasBeenLooted", hasBeenLooted);
        tag.putString("ContainerType", containerType.name());
        if (territoryTier > 0) {
            tag.putInt("TerritoryTier", territoryTier);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ContainerType")) {
            try {
                containerType = LootContainerType.valueOf(tag.getString("ContainerType"));
            } catch (IllegalArgumentException e) {
                containerType = resolveTypeFromBlock(getBlockState());
            }
        } else {
            containerType = resolveTypeFromBlock(getBlockState());
        }
        items = NonNullList.withSize(containerType.getSlotCount(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        lastLootedGameTime = tag.getLong("LastLootedGameTime");
        hasBeenLooted = tag.getBoolean("HasBeenLooted");
        territoryTier = tag.contains("TerritoryTier") ? tag.getInt("TerritoryTier") : 0;
    }
}
