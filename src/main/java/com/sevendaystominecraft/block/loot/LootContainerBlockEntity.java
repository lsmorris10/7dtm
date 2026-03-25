package com.sevendaystominecraft.block.loot;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.config.LootConfig;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.item.QualityTier;
import com.sevendaystominecraft.item.VanillaGearMaterials;
import com.sevendaystominecraft.loot.LootStageCalculator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.TridentItem;

import com.sevendaystominecraft.item.armor.TieredArmorItem;
import com.sevendaystominecraft.item.weapon.RangedWeaponItem;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
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

    public void resetForAdmin(boolean clearTimer) {
        this.hasBeenLooted = false;
        if (clearTimer) {
            this.lastLootedGameTime = -1;
        }
        setChanged();
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
                if (qualityEnabled && supportsQuality(stack.getItem())) {
                    QualityTier tier = QualityTier.randomForLootStage(lootStage, random);
                    QualityTier baseline = VanillaGearMaterials.getBaselineQuality(stack.getItem());
                    if (baseline != null && baseline.getLevel() > tier.getLevel()) {
                        tier = baseline;
                    }
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

    private static boolean supportsQuality(Item item) {
        if (VanillaGearMaterials.isVanillaGear(item)) {
            return true;
        }
        return item instanceof TieredArmorItem
                || item instanceof SwordItem
                || item instanceof DiggerItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof ArmorItem
                || item instanceof RangedWeaponItem
                || item instanceof GeoRangedWeaponItem;
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
            case TOOL_CRATE -> 4;
            case FUEL_CACHE -> 3;
            case VENDING_MACHINE -> 3;
            case MAILBOX -> 2;
            case FARM_CRATE -> 3;
        };
    }

    private static ItemStack generateItemForType(LootContainerType type, int lootStage, Random random) {
        return switch (type) {
            case TRASH_PILE -> pickRandom(random, lootStage,
                    ModItems.IRON_SCRAP, ModItems.DUCT_TAPE, () -> Items.CLAY_BALL);
            case CARDBOARD_BOX -> pickRandomWithVanillaGear(random, lootStage, 0.15f,
                    ModItems.IRON_SCRAP, ModItems.MECHANICAL_PARTS, ModItems.DUCT_TAPE, ModItems.POLYMER);
            case GUN_SAFE -> pickRandom(random, lootStage,
                    ModItems.MECHANICAL_PARTS, ModItems.FORGED_IRON, ModItems.FORGED_STEEL, ModItems.SURVIVORS_COIN);
            case MUNITIONS_BOX -> pickRandom(random, lootStage,
                    ModItems.IRON_SCRAP, ModItems.LEAD, ModItems.NITRATE, ModItems.MECHANICAL_PARTS, ModItems.FORGED_STEEL);
            case SUPPLY_CRATE -> pickRandomWithArmorAndVanillaGear(random, lootStage,
                    ModItems.FORGED_IRON, ModItems.FORGED_STEEL, ModItems.MECHANICAL_PARTS,
                    ModItems.ELECTRICAL_PARTS, ModItems.POLYMER, ModItems.SURVIVORS_COIN);
            case KITCHEN_CABINET -> pickRandomVanilla(random,
                    Items.BREAD, Items.APPLE, Items.COOKED_BEEF, Items.CARROT);
            case MEDICINE_CABINET -> pickRandomVanilla(random,
                    Items.GOLDEN_APPLE, Items.GLISTERING_MELON_SLICE);
            case BOOKSHELF -> pickRandomVanilla(random,
                    Items.BOOK, Items.PAPER, Items.WRITABLE_BOOK);
            case TOOL_CRATE -> pickRandomWithVanillaGear(random, lootStage, 0.30f,
                    ModItems.IRON_SCRAP, ModItems.MECHANICAL_PARTS, ModItems.FORGED_IRON,
                    ModItems.NAIL, ModItems.SPRING, ModItems.DUCT_TAPE);
            case FUEL_CACHE -> pickRandom(random, lootStage,
                    ModItems.GAS_CAN, ModItems.OIL_SHALE, () -> Items.GLASS_BOTTLE);
            case VENDING_MACHINE -> pickRandomVanilla(random,
                    Items.BREAD, Items.APPLE, Items.PAPER, Items.GLASS_BOTTLE);
            case MAILBOX -> pickRandomVanilla(random,
                    Items.PAPER, Items.MAP, Items.BOOK);
            case FARM_CRATE -> pickFarmCrate(random);
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

    @SafeVarargs
    private static ItemStack pickRandomWithVanillaGear(Random random, int lootStage, float chance, Supplier<Item>... baseItems) {
        if (random.nextFloat() < chance) {
            return pickVanillaGearForLootStage(random, lootStage);
        }
        return pickRandom(random, lootStage, baseItems);
    }

    @SafeVarargs
    private static ItemStack pickRandomWithArmorAndVanillaGear(Random random, int lootStage, Supplier<Item>... baseItems) {
        float roll = random.nextFloat();
        if (roll < 0.25f) {
            return pickArmorForLootStage(random, lootStage);
        }
        if (roll < 0.45f) {
            return pickVanillaGearForLootStage(random, lootStage);
        }
        return pickRandom(random, lootStage, baseItems);
    }

    @SafeVarargs
    private static ItemStack pickRandomWithArmor(Random random, int lootStage, Supplier<Item>... baseItems) {
        if (random.nextFloat() < 0.25f) {
            return pickArmorForLootStage(random, lootStage);
        }
        return pickRandom(random, lootStage, baseItems);
    }

    private static ItemStack pickVanillaGearForLootStage(Random random, int lootStage) {
        Item[] lowTierTools = {
                Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SWORD,
                Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SWORD
        };
        Item[] lowTierArmor = {
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
        };
        Item[] midTierTools = {
                Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SWORD, Items.IRON_SHOVEL,
                Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SWORD
        };
        Item[] midTierArmor = {
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
                Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS
        };
        Item[] highTierTools = {
                Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL
        };
        Item[] highTierArmor = {
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS
        };
        Item[] topTierTools = {
                Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SWORD, Items.NETHERITE_SHOVEL
        };
        Item[] topTierArmor = {
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS
        };

        Item[] pool;
        if (lootStage >= 80) {
            pool = random.nextBoolean() ? topTierTools : topTierArmor;
        } else if (lootStage >= 50) {
            pool = random.nextBoolean() ? highTierTools : highTierArmor;
        } else if (lootStage >= 20) {
            pool = random.nextBoolean() ? midTierTools : midTierArmor;
        } else {
            pool = random.nextBoolean() ? lowTierTools : lowTierArmor;
        }

        Item chosen = pool[random.nextInt(pool.length)];
        return new ItemStack(chosen, 1);
    }

    private static ItemStack pickArmorForLootStage(Random random, int lootStage) {
        Supplier<Item>[] lightArmor = new Supplier[]{
                ModItems.PADDED_HELMET, ModItems.PADDED_CHESTPLATE,
                ModItems.PADDED_LEGGINGS, ModItems.PADDED_BOOTS
        };
        Supplier<Item>[] mediumArmor = new Supplier[]{
                ModItems.SCRAP_IRON_HELMET, ModItems.SCRAP_IRON_CHESTPLATE,
                ModItems.SCRAP_IRON_LEGGINGS, ModItems.SCRAP_IRON_BOOTS
        };
        Supplier<Item>[] heavyArmor = new Supplier[]{
                ModItems.MILITARY_HELMET, ModItems.MILITARY_CHESTPLATE,
                ModItems.MILITARY_LEGGINGS, ModItems.MILITARY_BOOTS
        };

        Supplier<Item>[] pool;
        if (lootStage >= 50) {
            pool = random.nextBoolean() ? heavyArmor : mediumArmor;
        } else if (lootStage >= 20) {
            pool = random.nextBoolean() ? mediumArmor : lightArmor;
        } else {
            pool = lightArmor;
        }

        return new ItemStack(pool[random.nextInt(pool.length)].get(), 1);
    }

    private static ItemStack pickFarmCrate(Random random) {
        Item[] pool = new Item[]{
                Items.WHEAT_SEEDS, Items.WHEAT, Items.CARROT, Items.POTATO,
                Items.BEETROOT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS,
                ModItems.CORN_SEED.get(), ModItems.POTATO_SEED.get(),
                ModItems.BLUEBERRY_SEED.get(), ModItems.GOLDENROD_SEED.get(),
                ModItems.ALOE_SEED.get(), ModItems.COFFEE_SEED.get(),
                ModItems.CORN.get(), ModItems.POTATO_CROP_ITEM.get(),
                ModItems.BLUEBERRY.get(), ModItems.GOLDENROD.get(),
                ModItems.ALOE.get(), ModItems.COFFEE_BEANS.get()
        };
        Item chosen = pool[random.nextInt(pool.length)];
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
