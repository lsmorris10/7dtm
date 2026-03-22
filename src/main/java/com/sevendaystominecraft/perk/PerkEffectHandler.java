package com.sevendaystominecraft.perk;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.item.armor.ArmorSetBonusHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class PerkEffectHandler {

    private static final ResourceLocation FLURRY_ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "flurry_attack_speed");
    private static final ResourceLocation PARKOUR_JUMP_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "parkour_jump_bonus");
    private static final ResourceLocation SPEAR_REACH_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "spear_reach_bonus");
    private static final ResourceLocation PACK_MULE_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "pack_mule_speed");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerTakeDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        float dmgReduction = PlayerStatsHandler.getDamageReductionMultiplier(stats);
        if (dmgReduction < 1.0f) {
            event.setNewDamage(event.getNewDamage() * dmgReduction);
        }

        float armorSetReduction = ArmorSetBonusHandler.getDamageReductionMultiplier(player);
        if (armorSetReduction < 1.0f) {
            event.setNewDamage(event.getNewDamage() * armorSetReduction);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerFatalDamage(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        if (stats.getPerkRank("unkillable") > 0) {
            long currentTime = player.level().getGameTime();
            long cooldownEnd = stats.getUnkillableCooldownEnd();

            if (currentTime >= cooldownEnd) {
                event.setCanceled(true);
                player.setHealth(1.0f);
                stats.setUnkillableCooldownEnd(currentTime + 72000L);

                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§d[BZHS] Unkillable activated! 10s invulnerability."), true);
                player.invulnerableTime = 200;

                SevenDaysToMinecraft.LOGGER.info("[BZHS] Unkillable proc for {} — 60 min cooldown started",
                        player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        float mult = PlayerStatsHandler.getMiningSpeedMultiplier(stats);
        if (mult != 1.0f) {
            event.setNewSpeed(event.getNewSpeed() * mult);
        }

        int skullCrusherRank = stats.getPerkRank("skull_crusher");
        if (skullCrusherRank > 0) {
            ItemStack held = player.getMainHandItem();
            if (WeaponCategory.fromItemStack(held) == WeaponCategory.SLEDGEHAMMER) {
                event.setNewSpeed(event.getNewSpeed() + skullCrusherRank);
            }
        }
    }

    @SubscribeEvent
    public static void onZombieKilledForGhost(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof BaseSevenDaysZombie)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats.getPerkRank("ghost") <= 0) return;

        if (player.isShiftKeyDown()) {
            if (player.level() instanceof ServerLevel serverLevel) {
                ChunkPos chunkPos = new ChunkPos(event.getEntity().blockPosition());
                SevenDaysToMinecraft.LOGGER.debug("[BZHS] Ghost perk: stealth kill, zero heatmap noise at ({}, {})",
                        chunkPos.x, chunkPos.z);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDealDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (event.getEntity() instanceof Player) return;

        var source = event.getSource();
        Player player;
        if (source.getEntity() instanceof Player p) {
            player = p;
        } else {
            return;
        }
        if (player.level().isClientSide()) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        ItemStack heldItem = player.getMainHandItem();
        WeaponCategory category = WeaponCategory.fromItemStack(heldItem);

        float damageMultiplier = 1.0f;

        String perkId = category.getPerkId();
        if (perkId != null) {
            int perkRank = stats.getPerkRank(perkId);
            if (perkRank > 0) {
                damageMultiplier += category.getDamagePerRank() * perkRank;
            }
        }

        if (player.isCrouching()) {
            int shadowRank = stats.getPerkRank("shadow_strike");
            if (shadowRank > 0) {
                damageMultiplier += 0.20f * shadowRank;
            }

            if (stats.getPerkRank("ghost") > 0) {
                damageMultiplier *= 5.0f;
            }
        }

        if (damageMultiplier != 1.0f) {
            event.setNewDamage(event.getNewDamage() * damageMultiplier);
        }

        if (category.isMelee() && stats.getPerkRank("titan") > 0) {
            if (player.getRandom().nextFloat() < 0.15f) {
                double dx = player.getX() - target.getX();
                double dz = player.getZ() - target.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.01) {
                    target.knockback(1.5, dx / dist, dz / dist);
                }
                SevenDaysToMinecraft.LOGGER.debug("[BZHS] Titan stagger proc on {}", target.getName().getString());
            }
        }

        if (category == WeaponCategory.CLUB && stats.getPerkRank("iron_fists") >= 5) {
            if (player.getRandom().nextFloat() < 0.25f) {
                double dx = player.getX() - target.getX();
                double dz = player.getZ() - target.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.01) {
                    target.knockback(2.0, dx / dist, dz / dist);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPerkAttributeTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer)) return;
        if (player.tickCount % 20 != 0) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        applyFlurryOfBlows(player, stats);
        applyParkourJumpBonus(player, stats);
        applySpearReachBonus(player, stats);
        applyPackMuleEncumbrance(player, stats);
    }

    private static void applyFlurryOfBlows(Player player, SevenDaysPlayerStats stats) {
        int flurryRank = stats.getPerkRank("flurry_of_blows");
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) return;

        if (flurryRank > 0) {
            double bonus = 0.08 * flurryRank;
            updateTransientModifier(attackSpeed, FLURRY_ATTACK_SPEED_ID, bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            if (attackSpeed.hasModifier(FLURRY_ATTACK_SPEED_ID)) {
                attackSpeed.removeModifier(FLURRY_ATTACK_SPEED_ID);
            }
        }
    }

    private static void applyParkourJumpBonus(Player player, SevenDaysPlayerStats stats) {
        int parkourRank = stats.getPerkRank("parkour");
        AttributeInstance jumpStrength = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpStrength == null) return;

        if (parkourRank >= 4) {
            updateTransientModifier(jumpStrength, PARKOUR_JUMP_ID, 0.5,
                    AttributeModifier.Operation.ADD_VALUE);
        } else {
            if (jumpStrength.hasModifier(PARKOUR_JUMP_ID)) {
                jumpStrength.removeModifier(PARKOUR_JUMP_ID);
            }
        }
    }

    private static void applySpearReachBonus(Player player, SevenDaysPlayerStats stats) {
        int spearRank = stats.getPerkRank("spear_master");
        AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (reach == null) return;

        ItemStack held = player.getMainHandItem();
        WeaponCategory cat = WeaponCategory.fromItemStack(held);

        if (cat == WeaponCategory.SPEAR && spearRank >= 5) {
            updateTransientModifier(reach, SPEAR_REACH_ID, 1.0,
                    AttributeModifier.Operation.ADD_VALUE);
        } else {
            if (reach.hasModifier(SPEAR_REACH_ID)) {
                reach.removeModifier(SPEAR_REACH_ID);
            }
        }
    }

    private static void applyPackMuleEncumbrance(Player player, SevenDaysPlayerStats stats) {
        int packMuleRank = stats.getPerkRank("pack_mule");
        AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed == null) return;

        int encumbranceStart = 28 + (packMuleRank * 2);

        NonNullList<ItemStack> items = player.getInventory().items;
        int usedSlots = 0;
        for (ItemStack item : items) {
            if (!item.isEmpty()) usedSlots++;
        }

        int slotsOver = usedSlots - encumbranceStart;
        if (slotsOver > 0) {
            int penaltySteps = Math.min(8, slotsOver);
            double penalty = -0.015 * penaltySteps;
            updateTransientModifier(moveSpeed, PACK_MULE_SPEED_ID, penalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            if (moveSpeed.hasModifier(PACK_MULE_SPEED_ID)) {
                moveSpeed.removeModifier(PACK_MULE_SPEED_ID);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() == null) return;
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        int deepVeinsRank = stats.getPerkRank("deep_veins");
        if (deepVeinsRank <= 0) return;

        if (!event.getState().is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) return;

        float multiplier = 1.0f + (0.20f * deepVeinsRank);
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            int bonus = Math.round(stack.getCount() * (multiplier - 1.0f));
            if (bonus > 0) {
                stack.grow(bonus);
            }
        }
    }

    private static void updateTransientModifier(AttributeInstance attr, ResourceLocation id,
                                                 double value, AttributeModifier.Operation op) {
        if (attr.hasModifier(id)) {
            AttributeModifier existing = attr.getModifier(id);
            if (existing != null && Math.abs(existing.amount() - value) < 0.001) {
                return;
            }
            attr.removeModifier(id);
        }
        attr.addTransientModifier(new AttributeModifier(id, value, op));
    }

    public static int getInventoryBonusSlots(SevenDaysPlayerStats stats) {
        return stats.getPerkRank("pack_mule") * 10;
    }

    public static float getOreYieldMultiplier(SevenDaysPlayerStats stats) {
        int deepVeinsRank = stats.getPerkRank("deep_veins");
        if (deepVeinsRank > 0) {
            return 1.0f + (0.20f * deepVeinsRank);
        }
        return 1.0f;
    }

    public static int getCookingRecipeTier(SevenDaysPlayerStats stats) {
        return stats.getPerkRank("campfire_cook");
    }

    public static int getVehicleTier(SevenDaysPlayerStats stats) {
        return stats.getPerkRank("gearhead");
    }

    public static int getPhysicianTier(SevenDaysPlayerStats stats) {
        return stats.getPerkRank("physician");
    }

    public static float getTurretDamageMultiplier(SevenDaysPlayerStats stats) {
        int electroRank = stats.getPerkRank("electrocutioner");
        int roboticsRank = stats.getPerkRank("robotics_inventor");
        float mult = 1.0f;
        if (electroRank > 0) {
            mult += 0.20f * electroRank;
        }
        if (roboticsRank > 0) {
            mult += 0.10f * roboticsRank;
        }
        return mult;
    }

    public static int getRoboticsInventorTier(SevenDaysPlayerStats stats) {
        return stats.getPerkRank("robotics_inventor");
    }

    public static float getLockPickSpeedMultiplier(SevenDaysPlayerStats stats) {
        int lockRank = stats.getPerkRank("lock_picking");
        if (lockRank > 0) {
            return 1.0f + (0.33f * lockRank);
        }
        return 1.0f;
    }

    public static float getDualWieldAccuracyBonus(SevenDaysPlayerStats stats) {
        int rank = stats.getPerkRank("gunslinger_agility");
        if (rank > 0) {
            return 0.15f * rank;
        }
        return 0.0f;
    }
}
