package com.sevendaystominecraft.entity;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.entity.projectile.AcidBallEntity;
import com.sevendaystominecraft.entity.projectile.BulletEntity;
import com.sevendaystominecraft.entity.projectile.GrenadeEntity;
import com.sevendaystominecraft.entity.zombie.*;
import com.sevendaystominecraft.entity.npc.VanillaVillagerTraderEntity;
import com.sevendaystominecraft.territory.TerritoryLabelEntity;
import com.sevendaystominecraft.trader.TraderEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SevenDaysToMinecraft.MOD_ID);

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }

    public static final Supplier<EntityType<BaseSevenDaysZombie>> WALKER =
            ENTITY_TYPES.register("walker", () ->
                    EntityType.Builder.<BaseSevenDaysZombie>of(
                            (type, level) -> new BaseSevenDaysZombie(type, level, ZombieVariant.WALKER),
                            MobCategory.MONSTER
                    ).sized(0.6f, 1.95f).clientTrackingRange(8).build(key("walker")));

    public static final Supplier<EntityType<BaseSevenDaysZombie>> CRAWLER =
            ENTITY_TYPES.register("crawler", () ->
                    EntityType.Builder.<BaseSevenDaysZombie>of(
                            (type, level) -> new BaseSevenDaysZombie(type, level, ZombieVariant.CRAWLER),
                            MobCategory.MONSTER
                    ).sized(1.5f, 0.6f).clientTrackingRange(8).build(key("crawler")));

    public static final Supplier<EntityType<FrozenLumberjackZombie>> FROZEN_LUMBERJACK =
            ENTITY_TYPES.register("frozen_lumberjack", () ->
                    EntityType.Builder.<FrozenLumberjackZombie>of(FrozenLumberjackZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("frozen_lumberjack")));

    public static final Supplier<EntityType<BloatedWalkerZombie>> BLOATED_WALKER =
            ENTITY_TYPES.register("bloated_walker", () ->
                    EntityType.Builder.<BloatedWalkerZombie>of(BloatedWalkerZombie::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.1f).clientTrackingRange(8).build(key("bloated_walker")));

    public static final Supplier<EntityType<SpiderZombie>> SPIDER_ZOMBIE =
            ENTITY_TYPES.register("spider_zombie", () ->
                    EntityType.Builder.<SpiderZombie>of(SpiderZombie::new, MobCategory.MONSTER)
                            .sized(0.9f, 0.8f).clientTrackingRange(8).build(key("spider_zombie")));

    public static final Supplier<EntityType<FeralWightZombie>> FERAL_WIGHT =
            ENTITY_TYPES.register("feral_wight", () ->
                    EntityType.Builder.<FeralWightZombie>of(FeralWightZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("feral_wight")));

    public static final Supplier<EntityType<CopZombie>> COP =
            ENTITY_TYPES.register("cop", () ->
                    EntityType.Builder.<CopZombie>of(CopZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("cop")));

    public static final Supplier<EntityType<ScreamerZombie>> SCREAMER =
            ENTITY_TYPES.register("screamer", () ->
                    EntityType.Builder.<ScreamerZombie>of(ScreamerZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("screamer")));

    public static final Supplier<EntityType<ZombieDogEntity>> ZOMBIE_DOG =
            ENTITY_TYPES.register("zombie_dog", () ->
                    EntityType.Builder.<ZombieDogEntity>of(ZombieDogEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.85f).clientTrackingRange(8).build(key("zombie_dog")));

    public static final Supplier<EntityType<VultureEntity>> VULTURE =
            ENTITY_TYPES.register("vulture", () ->
                    EntityType.Builder.<VultureEntity>of(VultureEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 0.5f).clientTrackingRange(10).build(key("vulture")));

    public static final Supplier<EntityType<ZombieBirdEntity>> ZOMBIE_BIRD =
            ENTITY_TYPES.register("zombie_bird", () ->
                    EntityType.Builder.<ZombieBirdEntity>of(ZombieBirdEntity::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.4f).clientTrackingRange(10).build(key("zombie_bird")));

    public static final Supplier<EntityType<ZombieParrotEntity>> ZOMBIE_PARROT =
            ENTITY_TYPES.register("zombie_parrot", () ->
                    EntityType.Builder.<ZombieParrotEntity>of(ZombieParrotEntity::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.9f).clientTrackingRange(10).build(key("zombie_parrot")));

    public static final Supplier<EntityType<DemolisherZombie>> DEMOLISHER =
            ENTITY_TYPES.register("demolisher", () ->
                    EntityType.Builder.<DemolisherZombie>of(DemolisherZombie::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.4f).clientTrackingRange(8).build(key("demolisher")));

    public static final Supplier<EntityType<MutatedChuckZombie>> MUTATED_CHUCK =
            ENTITY_TYPES.register("mutated_chuck", () ->
                    EntityType.Builder.<MutatedChuckZombie>of(MutatedChuckZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("mutated_chuck")));

    public static final Supplier<EntityType<ZombieBearEntity>> ZOMBIE_BEAR =
            ENTITY_TYPES.register("zombie_bear", () ->
                    EntityType.Builder.<ZombieBearEntity>of(ZombieBearEntity::new, MobCategory.MONSTER)
                            .sized(1.4f, 1.4f).clientTrackingRange(10).build(key("zombie_bear")));

    public static final Supplier<EntityType<NurseZombie>> NURSE =
            ENTITY_TYPES.register("nurse", () ->
                    EntityType.Builder.<NurseZombie>of(NurseZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("nurse")));

    public static final Supplier<EntityType<SoldierZombie>> SOLDIER =
            ENTITY_TYPES.register("soldier", () ->
                    EntityType.Builder.<SoldierZombie>of(SoldierZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("soldier")));

    public static final Supplier<EntityType<ChargedZombie>> CHARGED =
            ENTITY_TYPES.register("charged", () ->
                    EntityType.Builder.<ChargedZombie>of(ChargedZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("charged")));

    public static final Supplier<EntityType<InfernalZombie>> INFERNAL =
            ENTITY_TYPES.register("infernal", () ->
                    EntityType.Builder.<InfernalZombie>of(InfernalZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f).clientTrackingRange(8).build(key("infernal")));

    public static final Supplier<EntityType<BehemothZombie>> BEHEMOTH =
            ENTITY_TYPES.register("behemoth", () ->
                    EntityType.Builder.<BehemothZombie>of(BehemothZombie::new, MobCategory.MONSTER)
                            .sized(1.6f, 3.0f).clientTrackingRange(10).build(key("behemoth")));

    public static final Supplier<EntityType<AcidBallEntity>> ACID_BALL =
            ENTITY_TYPES.register("acid_ball", () ->
                    EntityType.Builder.<AcidBallEntity>of(AcidBallEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
                            .build(key("acid_ball")));

    public static final Supplier<EntityType<BulletEntity>> BULLET =
            ENTITY_TYPES.register("bullet", () ->
                    EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f).clientTrackingRange(4).updateInterval(2)
                            .build(key("bullet")));

    public static final Supplier<EntityType<GrenadeEntity>> GRENADE =
            ENTITY_TYPES.register("grenade", () ->
                    EntityType.Builder.<GrenadeEntity>of(GrenadeEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f).clientTrackingRange(6).updateInterval(4)
                            .build(key("grenade")));

    public static final Supplier<EntityType<TerritoryLabelEntity>> TERRITORY_LABEL =
            ENTITY_TYPES.register("territory_label", () ->
                    EntityType.Builder.<TerritoryLabelEntity>of(TerritoryLabelEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(16).updateInterval(20)
                            .build(key("territory_label")));

    public static final Supplier<EntityType<TraderEntity>> TRADER =
            ENTITY_TYPES.register("trader", () ->
                    EntityType.Builder.<TraderEntity>of(TraderEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f).clientTrackingRange(10)
                            .build(key("trader")));

    public static final Supplier<EntityType<VanillaVillagerTraderEntity>> VANILLA_VILLAGER_TRADER =
            ENTITY_TYPES.register("vanilla_villager_trader", () ->
                    EntityType.Builder.<VanillaVillagerTraderEntity>of(VanillaVillagerTraderEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f).clientTrackingRange(10)
                            .build(key("vanilla_villager_trader")));

    public static final Supplier<EntityType<AirdropPlaneEntity>> AIRDROP_PLANE =
            ENTITY_TYPES.register("airdrop_plane", () ->
                    EntityType.Builder.<AirdropPlaneEntity>of(AirdropPlaneEntity::new, MobCategory.MISC)
                            .sized(6.0f, 2.0f).clientTrackingRange(20).updateInterval(2)
                            .build(key("airdrop_plane")));

    public static final Supplier<EntityType<FallingAirdropEntity>> FALLING_AIRDROP =
            ENTITY_TYPES.register("falling_airdrop", () ->
                    EntityType.Builder.<FallingAirdropEntity>of(FallingAirdropEntity::new, MobCategory.MISC)
                            .sized(2.0f, 4.0f).clientTrackingRange(20).updateInterval(2)
                            .build(key("falling_airdrop")));

    public static final Supplier<EntityType<DeadBodyEntity>> DEAD_BODY =
            ENTITY_TYPES.register("dead_body", () ->
                    EntityType.Builder.<DeadBodyEntity>of(DeadBodyEntity::new, MobCategory.MISC)
                            .sized(0.8f, 0.3f).clientTrackingRange(10).updateInterval(20)
                            .build(key("dead_body")));

    public static class AttributeRegistration {
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(WALKER.get(), BaseSevenDaysZombie.createBaseZombieAttributes().build());
            event.put(CRAWLER.get(), BaseSevenDaysZombie.createBaseZombieAttributes().build());
            event.put(FROZEN_LUMBERJACK.get(), FrozenLumberjackZombie.createAttributes().build());
            event.put(BLOATED_WALKER.get(), BloatedWalkerZombie.createAttributes().build());
            event.put(SPIDER_ZOMBIE.get(), SpiderZombie.createAttributes().build());
            event.put(FERAL_WIGHT.get(), FeralWightZombie.createAttributes().build());
            event.put(COP.get(), CopZombie.createAttributes().build());
            event.put(SCREAMER.get(), ScreamerZombie.createAttributes().build());
            event.put(ZOMBIE_DOG.get(), ZombieDogEntity.createAttributes().build());
            event.put(VULTURE.get(), VultureEntity.createAttributes().build());
            event.put(ZOMBIE_BIRD.get(), ZombieBirdEntity.createAttributes().build());
            event.put(ZOMBIE_PARROT.get(), ZombieParrotEntity.createAttributes().build());
            event.put(DEMOLISHER.get(), DemolisherZombie.createAttributes().build());
            event.put(MUTATED_CHUCK.get(), MutatedChuckZombie.createAttributes().build());
            event.put(ZOMBIE_BEAR.get(), ZombieBearEntity.createAttributes().build());
            event.put(NURSE.get(), NurseZombie.createAttributes().build());
            event.put(SOLDIER.get(), SoldierZombie.createAttributes().build());
            event.put(CHARGED.get(), ChargedZombie.createAttributes().build());
            event.put(INFERNAL.get(), InfernalZombie.createAttributes().build());
            event.put(BEHEMOTH.get(), BehemothZombie.createAttributes().build());
            event.put(TRADER.get(), TraderEntity.createAttributes().build());
            event.put(VANILLA_VILLAGER_TRADER.get(), VanillaVillagerTraderEntity.createAttributes().build());
            event.put(AIRDROP_PLANE.get(), AirdropPlaneEntity.createAttributes().build());
            event.put(FALLING_AIRDROP.get(), FallingAirdropEntity.createAttributes().build());
        }
    }
}
