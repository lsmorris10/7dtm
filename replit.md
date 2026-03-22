# Brutal Zombie Horde Survival (BZHS) — NeoForge Mod

## Project Overview
A total conversion mod for Minecraft 1.21.4 (NeoForge) inspired by 7 Days to Die survival gameplay. Aligned to the style of 7 Days to Die 2.6 Experimental (Feb 2026). Previously known as "7 Days to Minecraft" — rebranded to avoid trademark concerns. Internal code (mod ID `sevendaystominecraft`, package names) remains unchanged; commands use the `/bzhs` prefix.

**Mod ID**: `sevendaystominecraft`  
**Loader**: NeoForge 21.4.140  
**Java**: 21 (required)  
**Minecraft**: 1.21.4  

## Build System
- **Gradle 8.13** via wrapper (`./gradlew`)
- **NeoGradle 7.0+** plugin for NeoForge mod development
- Java toolchain set to Java 21 in `build.gradle`
- `JAVA_HOME` must point to Java 21 (jdk21 installed via Nix)

## Architecture

### Source Layout
```
src/main/java/com/sevendaystominecraft/
├── SevenDaysToMinecraft.java       — Main mod entry point (@Mod)
├── capability/
│   ├── ISevenDaysPlayerStats.java  — Player stats interface
│   ├── ModAttachments.java         — NeoForge data attachments registration
│   ├── PlayerStatsHandler.java     — Event handlers for player stats
│   ├── FallDamageHandler.java      — Fall damage event handler (sprain/fracture triggers)
│   ├── ExplosionHandler.java       — Explosion proximity handler (concussion trigger)
│   ├── WaterBottleConversionHandler.java — Converts vanilla water bottles to Murky Water Bottle on inventory tick
│   └── SevenDaysPlayerStats.java   — Player stats implementation (Food, Water, Stamina, etc.)
├── client/
│   ├── StatsHudOverlay.java        — HUD overlay for player stats + blood moon indicator
│   ├── CompassOverlay.java         — 360° compass strip at top-center with cardinal/intercardinal markers + heat indicator + territory markers
│   ├── MinimapOverlay.java         — Top-right minimap with terrain colors, player dot, nearby player dots
│   ├── NearbyPlayersClientState.java — Client-side state for synced nearby player positions
│   ├── ChunkHeatClientState.java   — Client-side state for current chunk heat value
│   ├── TerritoryClientState.java   — Client-side state for nearby territory data (synced from server)
│   ├── HudClientResetHandler.java  — Resets client HUD state on disconnect (including MusicManager)
│   ├── BloodMoonClientState.java   — Client-side blood moon state singleton
│   ├── BloodMoonSkyRenderer.java   — Red sky/fog tint during blood moon
│   ├── MusicManager.java           — Context-aware music system (Day/Night/Combat/BloodMoon priority)
│   ├── ModEntityRenderers.java     — Entity renderer registration for all 18 zombie types + territory label + trader
│   ├── ScaledZombieRenderer.java   — ZombieRenderer subclass with configurable scale factor
│   ├── TerritoryLabelRenderer.java — Entity renderer for territory floating label (uses EntityRenderState)
│   └── premade/
│       ├── PremadeWorldInfo.java        — Record: id, name, description, source, path
│       ├── PremadeWorldManager.java     — Scans bundled+external premade worlds, copies to saves
│       ├── PremadeWorldListWidget.java  — Scrollable list widget for premade world selection
│       └── CreateWorldScreenHandler.java — ScreenEvent listener injecting World Type toggle
├── block/
│   ├── ModBlocks.java              — DeferredRegister for all custom blocks (workstations, loot containers, vehicle wreckage, terrain, building/traps/land claim)
│   ├── ModBlockEntities.java       — Block entity type registration
│   ├── vehicle/
│   │   └── VehicleWreckageBlock.java — Decorative vehicle blocks (burnt car, broken truck, wrecked camper) that drop scrap materials
│   ├── workstation/
│   │   ├── WorkstationType.java    — Enum: Campfire, Grill, Workbench, Forge, Cement Mixer, Chemistry Station, Advanced Workbench
│   │   ├── WorkstationBlock.java   — BaseEntityBlock for all workstation types
│   │   ├── WorkstationBlockEntity.java — Block entity with fuel, crafting progress, input/output slots
│   │   ├── WorkstationMenu.java    — Container menu for workstation GUI
│   │   ├── WorkstationScreen.java  — Client-side GUI screen for workstations
│   │   ├── VanillaCampfireHandler.java — Event handler: intercepts right-click on vanilla campfire to open workstation GUI
│   │   ├── CampfireWorkstationSavedData.java — SavedData storing per-position campfire workstation inventory/fuel/progress
│   │   └── CampfireDataBlockEntity.java — Adapter bridging CampfireData to WorkstationBlockEntity for menu compatibility
│   ├── structural/
│   │   ├── StructuralIntegrityConfig.java — Config: enabled toggle, collapse delay ticks (structural_integrity.toml)
│   │   ├── StructuralIntegrityRegistry.java — Maps blocks/tiers to SI materials (Wood Frame, Cobblestone, Concrete, Reinforced Concrete, Steel) with max support, weight, horizontal span
│   │   ├── StructuralIntegrityManager.java — BFS ground-connection check, horizontal span enforcement, delayed collapse scheduling with cascade support
│   │   └── StructuralIntegrityEventHandler.java — @EventBusSubscriber: hooks BlockEvent.BreakEvent + LevelTickEvent.Post for SI checks
│   ├── building/
│   │   ├── UpgradeableBlock.java     — 6-tier upgradeable block (Wood Frame→Reinforced Wood→Cobblestone→Concrete→Reinforced Concrete→Steel) with right-click upgrade via repair hammer
│   │   ├── WoodSpikesBlock.java      — Contact damage trap (4 dmg), 10 durability, degrades on hit
│   │   ├── IronSpikesBlock.java      — Contact damage trap (8 dmg), 20 durability, degrades on hit
│   │   ├── BladeTrapBlock.java       — AoE damage trap (6 dmg), hits entities within 1 block every 20 ticks
│   │   ├── ElectricFencePostBlock.java — Contact damage (5 dmg) + stun (slowness V for 2s) on entity touch
│   │   └── LandClaimBlock.java       — 41-block protection radius preventing zombie spawns, one per player
│   ├── farming/
│   │   ├── CropBlock.java          — Base crop block with 4 growth stages (AGE 0-3), randomTick growth, harvest with Green Thumb perk integration
│   │   ├── FarmPlotBlock.java      — Tilled soil block that crops must be planted on (doesn't revert)
│   │   ├── DewCollectorBlock.java  — BaseEntityBlock for passive water generation
│   │   ├── DewCollectorBlockEntity.java — Block entity generating Murky Water every 6000 ticks with sky access
│   │   ├── DewCollectorMenu.java   — Container menu for Dew Collector (4 output slots)
│   │   ├── DewCollectorScreen.java — Client-side GUI screen for Dew Collector
│   │   └── FarmPlotInteractionHandler.java — Event handler: hoe + dirt/grass → Farm Plot conversion
│   └── loot/
│       ├── LootContainerType.java  — Enum: Trash Pile, Cardboard Box, Gun Safe, Munitions Box, etc.
│       ├── LootContainerBlock.java — BaseEntityBlock for loot containers
│       ├── LootContainerBlockEntity.java — Block entity with loot generation, respawn tracking
│       ├── LootContainerMenu.java  — Container menu for loot containers
│       └── LootContainerScreen.java — Client-side GUI for loot containers
├── command/
│   ├── LootStageCommand.java       — /bzhs loot_stage debug command
│   └── TerritoryCommand.java       — /bzhs territory list|listall debug commands
├── config/
│   ├── SurvivalConfig.java         — Server-side survival config (survival.toml)
│   ├── HordeConfig.java            — Server-side horde/blood moon config (horde.toml)
│   ├── ZombieConfig.java           — Zombie variant stats/modifiers config (zombies.toml)
│   ├── HeatmapConfig.java          — Heatmap config (heatmap.toml): enabled, decay/spawn multipliers
│   ├── StructuralIntegrityConfig.java — SI config (structural_integrity.toml): enabled, collapseDelay
│   └── LootConfig.java             — Loot config (loot.toml): respawnDays, abundanceMultiplier, qualityScaling
├── crafting/
│   └── ScrappingSystem.java        — Item scrapping into component materials (workbench vs inventory yield)
├── territory/
│   ├── TerritoryTier.java          — Tier 1-5 enum with star rating, size, zombie pool, loot counts, spawn weights
│   ├── TerritoryType.java          — Category enum (Residential/Commercial/Industrial/Military/Wilderness/Medical) with loot type mapping
│   ├── TerritoryRecord.java        — Per-territory instance: origin, tier, type, cleared status, zombie count
│   ├── TerritoryData.java          — SavedData: persists all territories by ID, chunk-to-territory index, spatial lookup
│   ├── TerritoryStructureBuilder.java — Legacy procedural structure generator (kept as fallback)
│   ├── TerritoryZombieSpawner.java — Legacy zombie spawner (kept for compatibility)
│   ├── TerritoryWorldGenerator.java — ChunkEvent.Load hook: village cluster generation with biome-based difficulty
│   ├── TerritoryBroadcaster.java   — @EventBusSubscriber: 60-tick broadcasts + sleeper zombie awakening
│   ├── TerritoryCompassRenderer.java — Client-side compass marker rendering for nearby territories (color by tier)
│   ├── TerritoryLabelEntity.java   — Entity with synced label text + tier, persisted, updates on clear
│   ├── VillageBuildingType.java    — 8 building type enum with materials, loot pools, zombie counts, weights
│   ├── VillageBuildingBuilder.java — Improved procedural builder: windows, doors, peaked roofs, dividers, porches
│   ├── VillageClusterGenerator.java — 4-12 building cluster generator with paths, props, vehicle wreckage
│   ├── NBTTemplateLoader.java      — NBT structure template system (loads .nbt from data/bzhs/structures/village/)
│   ├── SleeperZombieManager.java   — Dormant zombie spawn + awakening system
│   └── VillagerSuppressionHandler.java — Cancels vanilla villager spawns
├── stealth/
│   ├── NoiseManager.java           — Per-player noise event tracking with 5s linear decay
│   └── NoiseEventHandler.java      — @EventBusSubscriber for movement/block break noise + gunshot hook
├── entity/
│   ├── ModEntities.java            — DeferredRegister for all custom entity types + attribute events
│   └── zombie/
│       ├── BaseSevenDaysZombie.java — Base zombie entity with variant stats, modifiers, night speed bonus, radiated regen, detection state, behavior tree goals
│       ├── DetectionState.java      — Enum: UNAWARE(0), SUSPICIOUS(1), ALERT(2)
│       ├── ZombieVariant.java       — Enum of all 18 zombie variants with base stats
│       ├── ai/
│       │   ├── BlockHPRegistry.java       — Block HP lookup table for zombie block breaking (wood=10, stone=30, cobblestone=50, iron=200, obsidian=500)
│       │   ├── ZombieDetectionGoal.java   — Goal: stealth detection with noise/light/distance formula (priority 1)
│       │   ├── ZombieBreakBlockGoal.java  — Goal: zombies break obstructing blocks to reach targets (priority 3)
│       │   ├── ZombieHordePathGoal.java   — Goal: Blood Moon horde pathing toward nearest player (priority 4)
│       │   └── ZombieInvestigateGoal.java — Goal: investigate high-heat chunks when idle (priority 5)
│       ├── BehemothZombie.java      — Boss: knockback immune, ground pound AoE
│       ├── BloatedWalkerZombie.java — Explodes on death (2-block radius)
│       ├── ChargedZombie.java       — Chain lightning on hit
│       ├── CopZombie.java           — Acid spit projectile, explodes at 20% HP
│       ├── DemolisherZombie.java    — Chest-hit explosion, headshot mechanic
│       ├── FeralWightZombie.java    — Always sprints, glowing eyes
│       ├── FrozenLumberjackZombie.java — Cold-resistant Walker variant
│       ├── InfernalZombie.java      — Fire trail, burn debuff on melee
│       ├── MutatedChuckZombie.java  — Ranged vomit attack
│       ├── NurseZombie.java         — Heals nearby zombies
│       ├── ScreamerZombie.java      — Screams to spawn more zombies, flees
│       ├── SoldierZombie.java       — Armored Walker variant
│       ├── SpiderZombie.java        — Wall climbing, jump boost
│       ├── VultureEntity.java       — Flying dive attacks (Phantom base)
│       ├── ZombieBearEntity.java    — Charge + AoE swipe
│       └── ZombieDogEntity.java     — Pack spawns, fast (Wolf base)
├── item/
│   ├── ModItems.java               — DeferredRegister for all items (materials, melee weapons, ranged weapons, ammo, treatment items, armor, seeds, crops, cooked food, repair hammer)
│   ├── ModCreativeTabs.java        — Creative tabs: Materials, Workstations, Weapons, Armor, Loot Containers, Building, Magazines
│   ├── TreatmentItem.java          — Single-use right-click consumable that removes specific debuffs
│   ├── ConsumableStatItem.java     — Consumable item that modifies food/water stats, applies/cures debuffs, grants regen
│   ├── SeedItem.java               — Right-click-on-farm-plot plantable seed item
│   ├── QualityTier.java            — Quality tier enum (T1-T6: Poor → Legendary) with stat multipliers
│   ├── armor/
│   │   ├── ArmorTier.java          — Enum: LIGHT (Padded), MEDIUM (Scrap Iron), HEAVY (Military) with movement/stealth modifiers
│   │   ├── ModArmorMaterials.java  — ArmorMaterial definitions (PADDED, SCRAP_IRON, MILITARY) with protection/durability/toughness
│   │   ├── TieredArmorItem.java    — Custom ArmorItem subclass tracking ArmorTier with tooltips
│   │   └── ArmorSetBonusHandler.java — Tick handler: movement penalties, 2pc partial & 4pc full set bonuses (Light=50%/100% noise reduction, Medium=10%/20% stamina regen, Heavy=12%/25% DR), medium_armor perk integration, armor shattered notification, mixed-set warning
│   └── weapon/
│       └── RangedWeaponItem.java   — Right-click-to-fire ranged weapon (ammo consumption, cooldown, durability)
├── magazine/
│   ├── MagazineRegistry.java       — Static registry of 6 series × 5-7 issues = 36 magazine definitions
│   └── ModMagazines.java           — DeferredRegister auto-generating 36 magazine items from registry
├── worldgen/
│   ├── ModBiomes.java              — 7 ResourceKey<Biome> constants (Pine Forest, Forest, Plains, Desert, Snowy Tundra, Burned Forest, Wasteland)
│   └── BiomeProperties.java        — Per-biome temp range, zombie density multiplier, loot tier bonus; cosine day/night temp curve; vanilla biome fallback mapping
├── loot/
│   ├── LootStageCalculator.java    — Loot stage formula: floor((level×0.5) + (days×0.3) + biomeBonus + perkBonus)
│   └── LootStageHandler.java       — Periodic loot stage sync to client
├── menu/
│   └── ModMenuTypes.java           — Menu type registration for workstations, loot containers, and trader
├── heatmap/
│   ├── HeatSource.java             — Individual heat source with amount, decay rate, radius
│   ├── HeatmapData.java            — SavedData storing per-chunk heat sources, persisted via NBT
│   ├── HeatmapManager.java         — Server tick handler for heat decay + spawner integration
│   ├── HeatEventHandler.java       — Event hooks: block break, torch place, explosion, sprint
│   ├── HeatmapSpawner.java         — Threshold-based zombie spawning (scouts/screamer/mini-horde/waves)
│   └── HeatmapCommand.java         — /bzhs heat debug command + /bzhs heat_clear admin command
├── horde/
│   ├── BloodMoonTracker.java       — SavedData for day tracking & blood moon phase state
│   ├── BloodMoonEventHandler.java  — Server tick handler for blood moon timeline + sleep prevention
│   └── HordeSpawner.java           — Wave spawning with composition table, config day thresholds
├── perk/
│   ├── Attribute.java            — 5 attribute enum (STR/PER/FOR/AGI/INT)
│   ├── PerkDefinition.java       — Perk data class with rank requirements
│   ├── PerkRegistry.java         — Static registry of all 45 perks (40 + 5 masteries)
│   ├── LevelManager.java         — XP gain, level-up formula, zombie kill + block break hooks
│   ├── PerkCommand.java          — /bzhs level, /bzhs perk, /bzhs attribute, /bzhs perks commands
│   └── PerkEffectHandler.java    — Perk effect hooks (damage reduction, mining speed, unkillable, ghost)
├── mixin/
│   ├── FoodDataMixin.java          — Cancels vanilla food saturation
│   ├── LivingEntityHurtMixin.java  — Custom damage handling
│   ├── PlayerHealMixin.java        — Blocks vanilla passive regen
│   ├── SprintBlockMixin.java       — Sprint blocked when low stamina
│   └── CreateWorldScreenMixin.java — Intercepts Create World to handle premade world creation
└── network/
    ├── ModNetworking.java          — Packet channel registration (stats + blood moon + nearby players + chunk heat + territory + quests)
    ├── SyncPlayerStatsPayload.java — Client/server stats sync packet
    ├── BloodMoonSyncPayload.java   — Blood moon state sync packet
    ├── SyncNearbyPlayersPayload.java — Server→client nearby player positions (float coords, capped at 64)
    ├── SyncChunkHeatPayload.java   — Server→client current chunk heat value
    ├── SyncTerritoryPayload.java   — Server→client territory entries (id, pos, tier, label)
    ├── SyncTraderPayload.java      — Server→client trader entries (id, pos, tier, name)
    ├── TraderActionPayload.java    — Client→server trader buy/sell action packets
    └── NearbyPlayersBroadcaster.java — Server tick handler broadcasting nearby players + heat every 20 ticks
```

### Key Systems Implemented

#### Phase 1 — Core Survival (Milestone 1-2)
- **Player Stats**: Food, Water, Stamina, Temperature, Debuffs via NeoForge DataAttachments
- **Custom HUD**: StatsHudOverlay renders custom bars (replacing vanilla hearts/food)
- **Mixins**: Vanilla food, regen, sprint systems overridden
- **Networking**: Stats synced from server to client via manual PacketDistributor
  - Client-side sprint cancel on exhaustion sync packet
- **Config**: `survival.toml` for per-server tuning of survival parameters
  - Temperature adjustment rate default lowered to 0.3°F/s for more realistic pacing

#### Horde Night & Blood Moon System (Spec §4) — DONE
- **BloodMoonTracker**: SavedData persisting game day, phase (NONE/PREP/ACTIVE/POST), wave state, and all event flags — survives server restarts
- **BloodMoonEventHandler**: Server-side tick handler implementing the full blood moon timeline:
  - Day before: "Horde Night Tomorrow" warning at 20:00
  - Blood moon day: Sky turns red at 18:00, siren at 18:30, horde starts at 22:00
  - Waves spawn every `waveIntervalSec` seconds (default 10 min)
  - Final wave at 04:00, dawn cleanup burns surviving zombies at 06:00
  - **Sleep prevention**: `CanPlayerSleepEvent` blocks sleeping during active blood moon
  - **Late-join sync**: Syncs blood moon state to players on login
- **HordeSpawner**: Wave spawning with spec §4.2 scaling formula:
  `floor(baseCount × (1 + (dayNumber / cycleLength) × diffMult) ^ 1.2)`
  - Day-based composition table with 5 tiers (day 7/14/21/28/49+)
  - Config day thresholds gate advanced variants (feral, demolisher, charged, infernal)
  - Radiated modifier randomly applied to base variants on day 28+
  - Spawns zombies 24-40 blocks from each player at surface level
  - Wave multiplier: `1 + 0.25 * waveIndex` for escalating difficulty
- **HordeConfig**: `horde.toml` with all spec §4.2 config keys
- **BloodMoonSyncPayload**: Network packet syncing blood moon state to clients
- **BloodMoonClientState**: Client singleton storing active state, wave info, day number
- **BloodMoonSkyRenderer**: Fog color tint that gradually ramps to red during active blood moon

#### Custom Zombie System (Spec §3.1-3.2) — DONE
- **ZombieVariant enum**: All 18 variants with base HP, damage, speed, XP, spawn day
  - 3 modifier types (Radiated, Charged, Infernal) with configurable stat multipliers
- **BaseSevenDaysZombie**: Core entity extending Zombie
  - Applies variant stats on spawn via `finalizeSpawn()` with tick fallback
  - Modifier system: stats applied after variant stats, persisted via NBT, reapplied on load
  - Night speed bonus: +50% movement speed during nighttime (configurable)
  - Radiated regen: 2 HP/sec healing tick (configurable)
  - XP reward includes modifier bonus
  - **Detection state**: Synced entity data (UNAWARE/SUSPICIOUS/ALERT) with NBT persistence, particle indicators (WITCH for ?, ANGRY_VILLAGER for !)
  - **Behavior tree goals** (registered at priorities 1-5, inherited by all 16 subclasses):
    - `ZombieDetectionGoal` (P1): Stealth detection system — scans nearby players every second using formula `detectionChance = (noise × lightFactor) / distance²`; crouching halves noise; Feral Wights get 1.6x multiplier; SUSPICIOUS state lasts 3 seconds before escalating or reverting
    - `ZombieBreakBlockGoal` (P3): Breaks obstructing blocks to reach targets, with block HP system, vanilla break animation, mobGriefing gamerule respect
    - `ZombieHordePathGoal` (P4): During Blood Moon, horde zombies path toward nearest player (64 blocks, 128 on day 21+)
    - `ZombieInvestigateGoal` (P5): When idle, investigates high-heat chunks from heatmap system, wanders locally then re-queries
  - **BlockHPRegistry**: Block HP lookup table (glass=3, wood=10, log=15, stone=30, cobblestone=50, iron=200, obsidian=500, bedrock=unbreakable)
  - **ZombieConfig additions**: `blockBreakEnabled`, `blockBreakSpeedMultiplier`, `investigateRange`, `hordePathRange`, `hordePathRangeDay21`, `blockHPMultiplier`
- **18 variant entity types** registered via `DeferredRegister<EntityType<?>>`
  - Special mechanics per spec §3.2: explosions, projectiles, chain lightning, fire trails, wall climbing, healing aura, screamer spawning, flying dive attacks, ground pound AoE
- **ZombieConfig** (`zombies.toml`): Per-variant HP/damage/speed overrides, all special mechanic tuning values, modifier multipliers
- **ModEntities**: Registration with `EntityAttributeCreationEvent` for all 18 types

#### Stealth & Detection System — DONE
- **DetectionState**: Enum with UNAWARE(0), SUSPICIOUS(1), ALERT(2) and synced entity data in BaseSevenDaysZombie
- **NoiseManager**: Per-player noise event tracking with 5-second linear decay; noise values: Gunshot=80, Sprint=15, Walk=5, Crouch=1, Block Break=10
- **NoiseEventHandler**: `@EventBusSubscriber` hooks for player movement (every 10 ticks), block break events, and gunshot (called from GeoRangedWeaponItem)
- **ZombieDetectionGoal**: Priority 1 goal scanning every 20 ticks; detection formula: `(noise × lightFactor) / distance²`
  - Light factor: 1.5 (above 10), 1.0 (5-10), 0.5 (below 5)
  - Crouch reduces noise by 50%
  - Feral Wights: 25-block detection range (vs 15 normal), 1.6x detection multiplier
  - Suspicious threshold: detectionChance ≥ 0.1; Alert threshold: ≥ 1.0
  - Suspicious state: zombie looks toward noise source for 60 ticks (3 seconds), then re-evaluates and either alerts (≥1.0) or returns to unaware
- **Visual indicators**: Server-side particles every 10 ticks — WITCH particles for SUSPICIOUS, ANGRY_VILLAGER for ALERT
- **Files**: `stealth/NoiseManager.java`, `stealth/NoiseEventHandler.java`, `entity/zombie/DetectionState.java`, `entity/zombie/ai/ZombieDetectionGoal.java`

#### Heatmap System (Spec §1.3) — DONE
- **HeatmapData**: Per-chunk SavedData storing heat sources with individual decay rates, persisted to NBT
- **HeatmapManager**: Server-side tick handler (1-second intervals) processing heat decay with configurable multiplier
- **HeatEventHandler**: Hooks into block break (+0.5, 3-chunk radius), torch placement (+2, 1-chunk), sprint (+0.2/sec, 2-chunk), explosions (+25, 6-chunk)
- **HeatmapSpawner**: Threshold-based spawning with cooldowns:
  - Heat 25+: 1-2 scout Walkers (30s cooldown)
  - Heat 50+: Screamer guaranteed (60s cooldown)
  - Heat 75+: Mini-horde of 8-12 mixed zombies from nearest dark area (90s cooldown)
  - Heat 100: Enters "wave mode" — continuous waves every 90s until heat drops below 75
  - Mini-horde and wave spawns prefer dark areas (light level ≤ 7); falls back to any valid position
  - Skips spawning during active blood moon
- **Heat radiation**: Sources radiate to neighboring chunks with distance-based falloff (50% at center, less at edges)
- **Heat cap**: 100 per chunk (spec-accurate); threshold multiplier scales spawn thresholds only
- **HeatmapConfig**: `heatmap.toml` with enabled toggle, decayMultiplier (0.1-5.0), spawnThresholdMultiplier (0.5-3.0)
- **Debug commands**: `/bzhs heat` shows current chunk heat + effective thresholds, `/bzhs heat_clear` (op-only) resets all heat data

#### Territory POI System (Spec §2.2 — First Version) — DONE
- **TerritoryTier** (1-5): Star ratings (★ to ★★★★★), size ranges (5×5 to 15×15), zombie pools, loot counts, spawn weights (Tier 1-2 common, Tier 4-5 rare)
- **TerritoryType**: 13 categories (Residential, Commercial, Industrial, Military, Wilderness, Medical, Crack-a-Book, Working Stiffs, Pass-n-Gas, Pop-n-Pills, Farm, Utility, Trader Outpost) each mapped to appropriate loot container types
- **TerritoryRecord**: Per-instance data (origin, tier, type, cleared status, awakened status, zombie count) serialized to NBT
- **TerritoryData** (SavedData): Persists all territories across server restarts; spatial hasNearby check for minimum spacing (16-chunk min separation); getNearby for client sync
- **TerritoryStructureBuilder**: Legacy procedural structure generation (floor/walls/roof); kept as fallback and utility
- **TerritoryZombieSpawner**: Legacy spawner kept for compatibility
- **TerritoryWorldGenerator**: `ChunkEvent.Load` hook; 1-in-40 chance per chunk in Overworld; deterministic per-chunk seed; now generates village clusters instead of single buildings; biome-based tier cap
- **TerritoryLabelEntity**: Custom entity with synced `LABEL_TEXT`/`TIER` data; floating custom name renders via EntityRenderer name tag system; updates to cleared state every 100 ticks; persisted between sessions; implements `hurtServer` → false (immune)
- **TerritoryLabelRenderer**: Extends `EntityRenderer<TerritoryLabelEntity, EntityRenderState>` using NeoForge 1.21.4's two-type-param pattern; relies on base class `renderNameTag()` for label display
- **TerritoryBroadcaster**: `@EventBusSubscriber` server tick handler sends `SyncTerritoryPayload` to each player every 60 ticks with all territories within 512 blocks; now handles sleeper zombie awakening on player proximity
- **TerritoryClientState**: Thread-safe `CopyOnWriteArrayList` storing nearby territories for compass rendering
- **TerritoryCompassRenderer**: Draws tier-colored markers on the compass strip (green Tier 1-2, yellow Tier 3, red Tier 4-5) pointing toward territory direction; renders star rating above each marker
- **TerritoryCommand**: `/bzhs territory list` shows nearby territories (256 block radius) with coords/status; `/bzhs territory listall` (op-only) shows all territories on the level
- **SyncTerritoryPayload**: Network packet with list of `TerritoryEntry` records (id, pos, tier, label string) using manual ByteBuf codec

#### Village Overhaul (7DTD-style Settlements) — DONE
- **VillageBuildingType**: 8 building types (Residential, Crack-a-Book, Working Stiffs, Pass-n-Gas, Pop-n-Pills, Farm, Utility, Trader Outpost) with weighted random selection, per-type wall/floor/roof/frame materials, loot pools, zombie counts. Trader Outpost is fixed 16x16 with custom compound generation (fenced perimeter, central building, lanterns)
- **VillageBuildingBuilder**: Improved procedural building generation with windows (glass panes), doors, peaked roofs, interior room dividers, porches, material variety per building type
- **VillageClusterGenerator**: Generates 4-12 building clusters connected by gravel paths; scatters exterior props (trash piles, mailboxes, vending machines, vehicle wreckage) between buildings
- **NBTTemplateLoader**: Checks `data/bzhs/structures/village/` for `.nbt` files matching building types (e.g., `residential_1.nbt`); falls back to procedural if no template exists; supports up to 10 variants per type
- **SleeperZombieManager**: Spawns dormant zombies (noAI=true) inside buildings at generation time; awakens them when player enters trigger radius via TerritoryBroadcaster
- **VillagerSuppressionHandler**: `EntityJoinLevelEvent` handler that cancels all vanilla Villager spawns
- **Vehicle wreckage blocks**: 3 decorative blocks (Burnt Car, Broken Truck, Wrecked Camper) that drop scrap iron, mechanical parts, and fuel when broken
- **New loot container types**: TOOL_CRATE (tools/iron/mechanical parts), FUEL_CACHE (fuel/water), VENDING_MACHINE (misc), MAILBOX (junk), FARM_CRATE (seeds/food)
- **Biome-based difficulty**: Village tier capped by both distance from spawn AND biome lootTierMax from BiomeProperties (Forest=3, Desert/Snow=5, Wasteland=6)

#### Trader NPC System — DONE
- **TraderEntity**: Invulnerable, non-despawning PathfinderMob NPC with random name from pool (Joel, Rekt, Jen, Hugh, Bob), LookAtPlayerGoal, right-click opens shop GUI; night closure (22:00-06:00 in-game) — refuses interaction with "Closed" message
- **DukeToken**: Currency item (Duke's Casino Tokens) for buying/selling at traders, stacksTo(50000)
- **TraderConfig** (`trader.toml`): guaranteeRadius (150), minChunkSpacing (25), spawnChanceDenominator (30), protectionRadius (30), restockIntervalDays (3), syncRangeBlocks (512), tier distance thresholds
- **TraderSpawnHandler**: Dedicated chunk-load handler (completely independent from TerritoryWorldGenerator) using TraderConfig spacing/chance values; guaranteed near-spawn trader within guaranteeRadius; creates full TRADER_OUTPOST territory + trader entity; `randomNonTrader()` in TerritoryType prevents TRADER_OUTPOST from appearing via regular territory RNG
- **TraderData** (SavedData): Persists trader locations, names, tiers across restarts; spatial lookup; protection zone check
- **TraderRecord**: Per-trader data (id, origin, name, tier, lastRestockDay, quest generation/caching)
- **TraderInventory**: Per-trader specialty stock — Joel (general goods), Rekt (weapons/military), Jen (medicine/books), Bob (tools/building materials), Hugh (armor/survival gear). Common stock shared across all. Secret Stash per-trader (unlocked at Better Barter rank 5). Price formula: `finalPrice = basePrice × (1 + (6 - betterBarterRank) × 0.1) × difficultyMult`. Sell values scale with Better Barter perk (+5% per rank). Backward-compat `getOffersForTier()` maps tiers to trader names.
- **TraderMenu**: AbstractContainerMenu with buy/sell actions; 4 sell input slots for place-and-sell workflow; server-side stock tracking via ContainerData (live sync to client); proximity enforcement (10-block radius entity check); Better Barter perk integration for pricing; Secret Stash section (rank 5); open-hours enforcement (server rejects buy/sell during night 22:00-06:00)
- **TraderScreen**: Buy tab shows items with adjusted prices per Better Barter rank, live stock count (or "SOLD OUT"), scrollable; Sell tab has 4 input slots + sell button + value preview; Quests tab shows available quests from trader with accept/turn-in buttons; Secret Stash tab (only visible at Better Barter rank 5) with purple-themed UI
- **SyncTraderPayload**: Server→client trader position sync (id, pos, tier, name) following SyncTerritoryPayload pattern
- **TraderClientState**: Thread-safe client-side trader position storage
- **TraderBroadcaster**: @EventBusSubscriber 60-tick broadcast of nearby traders to each player; 1200-tick restock check comparing game day against lastRestockDay per trader
- **TraderProtectionHandler**: Suppresses monster spawns (EntityJoinLevelEvent) and player block breaking (BlockEvent.BreakEvent) within configurable radius
- **ZombieBreakBlockGoal integration**: Zombie AI block destruction also checks `TraderData.isInProtectionZone()` before destroying blocks, preventing zombie block-breaking in trader zones
- **Map markers**: Cyan "T" markers on compass, cyan diamond icon primitives on minimap/big map (filled diamond shape rendered via `graphics.fill()`) with trader name labels
- **Quest markers**: Yellow "Q" markers on compass, minimap, and big map for active quest target locations
- **TraderRenderer**: HumanoidModel-based renderer using Steve texture
- **TraderActionPayload**: Client→server buy/sell packets with traderId validation

#### Quest System (Task #157) — DONE
- **Package**: `com.sevendaystominecraft.quest`
- **QuestType**: 4 quest types — KILL_COUNT, CLEAR_TERRITORY, FETCH_DELIVER, BURIED_TREASURE
- **QuestDefinition**: Quest data record (type, name, objective, targets, counts, location, rewards, traderId) with NBT serialization
- **QuestInstance**: Active quest with state machine (ACTIVE → READY_TO_TURN_IN → COMPLETED), progress tracking, unique ID
- **QuestGenerator**: Generates tier-appropriate quests per trader — kill targets from ZombieVariant pool, fetch items by tier, nearby uncleared territories, buried treasure within configurable range
- **QuestConfig** (`quest.toml`): maxActiveQuests (3), xpMultiplier, tokenMultiplier, questRefreshIntervalDays (3), maxTreasureDistance (200)
- **Quest storage**: Active quests stored in SevenDaysPlayerStats with full NBT persistence; TraderRecord caches generated quests with refresh timer
- **Network**: SyncQuestPayload (server→client active quest sync), QuestActionPayload (client→server accept/abandon/turn-in), SyncTraderQuestsPayload (server→client available trader quests)
- **QuestClientState**: Client-side quest state for HUD/map rendering
- **QuestHudOverlay**: Renders active quests on left side below minimap — quest name, objective, progress bar
- **QuestProgressHandler**: LivingDeathEvent for kill quests (supports projectile/ranged kills via owner resolution), 40-tick server tick for fetch/clear-territory progress checks, BlockEvent.BreakEvent for buried treasure (supply crate dig-up detection)
- **QuestActionHandler**: Server-side accept/abandon/turn-in/track with proximity validation (10-block radius to trader), deterministic quest ID matching, inventory item removal for fetch quests, supply crate placement for buried treasure on accept
- **Tracked quest**: Players select one active quest to track; only the tracked quest shows markers on compass/minimap/big map. First accepted quest auto-tracked. Track button in trader UI quests tab.
- **Quest refresh**: Quests refresh when the trader restocks (tied to trader restock cycle, not separate timer)
- **Quest markers**: Yellow "Q" diamond markers on compass, yellow squares on minimap/big map for tracked quest only
- **Quest sync**: Quests + tracked quest ID synced to client on login, on progress change, and on trader interaction

## Known Bugs / Issues
1. **Sprint bug (FIXED)**: Added `LocalPlayerSprintMixin` targeting `LocalPlayer.aiStep()` (client-side mixin in `sevendaystominecraft.mixins.json` "client" array). Cancels sprinting client-side when stamina exhausted, fracture, electrocuted, or stunned — prevents rubber-banding.
2. **Temperature**: Adjustment rate changed to 0.3°F/s — needs long-term gameplay verification
3. **Debuffs**: 12 debuffs (radiation removed) with 7DTD-faithful values (20 HP scale), treatment items (Bandage, Splint, Painkiller, Aloe Cream, First Aid Kit), infection stage 1→2 progression, dysentery triggers (rotten flesh/water bottles)
4. **Horde spawn balance**: Needs verification that spawn counts match intended difficulty

## Workflow
- **Build Mod**: `export JAVA_HOME=$(dirname $(dirname $(which java))) && ./gradlew build --no-daemon`
  - Compiles Java sources, processes resources, assembles the mod JAR
  - Output JAR: `build/libs/sevendaystominecraft-0.1.0-alpha.jar`
  - First run downloads Minecraft + NeoForge dependencies (~several minutes)
  - This is a build workflow (console output type), not a web server
- **Preview Site**: `python3 scripts/preview_server.py` (webview, port 5000)
  - Serves `public/` directory with no-cache headers for fresh content during development
  - Appears as a "Website" in the Library panel

## Environment
- Java 21 installed via Nix (`jdk21` package)
- `gradlew` script created for Linux (gradlew.bat only existed for Windows)
- `gradle/wrapper/gradle-wrapper.jar` downloaded separately (excluded from git)

## GitHub Sync
- **Remote**: `origin` → `https://github.com/lsmorris10/bzhs`
- **Branch**: `master`
- **Push from Replit**: No GitHub auth is configured in this Replit environment. Pushes must be done manually:
  1. Open the **Git** tab in the Replit sidebar (Version Control panel)
  2. Use the "Push" button to sync commits to GitHub
  3. Alternatively, on your local PC: `git remote add replit <replit-git-url>` then `git pull replit master`

## Development Notes
- No frontend web server — this is a pure Java Minecraft mod
- Use `./gradlew build` to compile and package the mod JAR
- Use `./gradlew runClient` to launch Minecraft with the mod (requires display)
- Use `./gradlew runServer` to launch a Minecraft server with the mod
- Full spec in `docs/bzhs_final_spec.md` (2273 lines, 20 sections)
- See `archive/` for resolved spec drafts
- See `PROJECT_NOTES.md` for session-by-session status and known issues

## NeoForge API Notes
- `EntityType.Builder.build()` requires `ResourceKey<EntityType<?>>` in 1.21.4 — use `ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, name))`
- `Entity.hurt()` is `final` → override `actuallyHurt(ServerLevel, DamageSource, float)` for damage interception
- `doHurtTarget()` signature: `doHurtTarget(ServerLevel serverLevel, Entity target)`
- `SoundEvents.LLAMA_SPIT`, `GHAST_SCREAM`, `RAVAGER_ROAR`, `LIGHTNING_BOLT_THUNDER` — direct `SoundEvent`, no `.value()` needed

### Custom Sound System
- **`ModSounds.java`** (`com.sevendaystominecraft.sound`) — DeferredRegister for all custom SoundEvents
- **sounds.json** at `assets/sevendaystominecraft/sounds.json` — maps event IDs to OGG paths
- **OGG files** in `assets/sevendaystominecraft/sounds/` — placeholder silent OGGs (replace with real audio)
- 8 sound events: `zombie_groan`, `zombie_scream`, `zombie_death`, `gun_fire_9mm`, `gun_fire_ak47`, `blood_moon_siren`, `workstation_ambient`, `block_break_zombie`
- `RangedWeaponItem` constructor takes `Supplier<SoundEvent> fireSound` parameter
- `BaseSevenDaysZombie` overrides `getAmbientSound()` and `getDeathSound()`
- `BloodMoonEventHandler` siren uses `ModSounds.BLOOD_MOON_SIREN`
- `ZombieBreakBlockGoal` plays `BLOCK_BREAK_ZOMBIE` on block destruction
- Subtitle translations in `en_us.json` under `subtitles.sevendaystominecraft.*`
- `convertsInWater()` → `isSensitiveToWater()`; `isSunSensitive()` removed entirely
- `isGlowing()` → `isCurrentlyGlowing()`
- `getExperienceReward()` → `getBaseExperienceReward(ServerLevel level)`
- `EntityType.create()` requires `EntitySpawnReason` parameter in 1.21.4
- `@EventBusSubscriber(bus = Bus.MOD)` is deprecated but still functional
- `SavedData` uses `Factory<>` with constructor + load function for `computeIfAbsent`
- `CanPlayerSleepEvent` is the correct hook for blocking sleep (not `PlayerSleepInBedEvent`)
- Sprint detection: avoid speed-based heuristics; use `player.isSprinting()` directly and handle client-side via Mixin or sync packets
- Currency item: `survivors_coin` (renamed from `dukes_casino_token` to avoid trademark)
- Zombie display names renamed: Feral Wight→Feral Wraith, Frozen Lumberjack→Frostbitten Woodsman, Cop→Riot Husk, Screamer→Banshee, Demolisher→Wrecking Husk, Mutated Chuck→Mutated Brute, Spider Zombie→Wall Creeper, Bloated Walker→Bloated Shambler
- 12 perk IDs renamed (e.g. `miner_69er`→`deep_striker`, `sexual_tyrannosaurus`→`unstoppable_force`) — see PerkRegistry.java for full list
- Config pattern: Static `SPEC` + `INSTANCE` via `new ModConfigSpec.Builder().configure(Klass::new)`

#### Weapons System — DONE
- **Melee weapons**: 3 weapons via `SwordItem` + `ToolMaterial`:
  - Stone Club (4 dmg, -2.8 speed, wood durability), Baseball Bat (5 dmg, -2.6 speed), Iron Sledgehammer (9 dmg, -3.4 speed, iron durability)
- **Ranged weapons**: 2 guns via `GeoRangedWeaponItem` (Geckolib `GeoItem`, magazine system, reload state machine):
  - 9mm Pistol (8 dmg, 8-tick cooldown, 15-round mag, 36-tick reload, 250 dur)
  - AK-47 (12 dmg, 4-tick cooldown, 30-round mag, 50-tick reload, 500 dur)
- **Grenade**: `GrenadeItem` (Geckolib `GeoItem`, right-click throws a `GrenadeEntity`):
  - Throwable, 3-second fuse (60 ticks), 5-block explosion radius, stackable to 16
- **Ammo**: 9mm Ammo, 7.62mm Ammo (stackable to 64)
- **BulletEntity**: `ThrowableItemProjectile` with near-zero gravity (0.01), configurable damage, crit particles
- **GrenadeEntity**: `ThrowableItemProjectile` with 0.05 gravity, 60-tick fuse, `Level.ExplosionInteraction.NONE` explosion
- **Geckolib 4.8.5**: Added as dependency (`software.bernie.geckolib:geckolib-neoforge-1.21.4:4.8.5`)
  - Maven: `https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/`
  - Item JSON files for weapons use `{"model":{"type":"geckolib:geo_item"}}`
  - **Bundled via Jar-in-Jar**: `jarJar` dependency in `build.gradle` embeds GeckoLib inside the BZHS JAR at `META-INF/jarjar/`; players need only the single BZHS JAR
  - Deploy script (`scripts/deploy-jar.sh`) prefers the `-all.jar` (JiJ bundle) and renames to clean filename
  - 3D voxel-style `.geo.json` models at `assets/sevendaystominecraft/geo/item/`
  - Keyframe animations at `assets/sevendaystominecraft/animations/item/`
  - Placeholder textures at `assets/sevendaystominecraft/textures/item/`
  - Animations: AK-47 (idle/fire/rack/reload), Pistol (idle/fire/reload), Grenade (idle/pin_pull/throw)
  - Magazine system tracked via `CustomData` ItemStack component (NBT)
- **Crafting**: Melee at Workbench, guns at Advanced Workbench, ammo at Chemistry Station
- **Creative tab**: BZHS Weapons tab with all melee + ranged + ammo + grenade items

## Spec / Roadmap
The full implementation is tracked in `docs/bzhs_final_spec.md` with 19 phases.
Milestones 1-9 complete (except #4 Temperature which is partial). Milestone 3 (Debuffs): DONE — all 12 debuff types. Milestone 5 (Heatmap): DONE. Milestone 6 (Loot & Crafting): DONE — workstations, loot containers, scrapping, quality tiers. Milestone 7 (XP/Leveling/Perks): DONE — full perk registry, level-up system, commands, HUD XP bar. Milestone 8 (Blood Moon/Horde Night): DONE. Milestone 9 (HUD): DONE — compass, minimap, player tracking, stats overlay. Milestone 10 (Weapons): DONE — melee + ranged weapons, ammo, crafting recipes. Milestone 11 (Skill Books/Magazines): DONE — 6 series, 36 items, mastery tracking. Milestone 15 (World Gen — Biomes): PARTIAL — 7 biome definitions with gameplay properties, integrated into temperature/loot/spawning; full world gen pipeline (city grid, POI templates, overworld biome placement) still needed. Quest System (Task #157): DONE — 4 quest types, trader GUI, HUD overlay, map markers, NBT persistence. Next priorities: full world gen pipeline, custom textures/models.

### Biome System (Spec §2)
- **Package**: `com.sevendaystominecraft.worldgen`
- **ModBiomes**: 7 ResourceKey<Biome> definitions (pine_forest, forest, plains, desert, snowy_tundra, burned_forest, wasteland)
- **BiomeProperties**: Gameplay stats per biome — temperature range (°F), zombie density multiplier, loot tier range; fallback mapping for vanilla biomes via `getBaseTemperature()`
- **BiomeStats.ambientTemperature(dayFraction)**: Calculates ambient temp from biome range + time-of-day modulation
- **Integration**: `PlayerStatsHandler.estimateAmbientTemperature()` now uses BiomeProperties; `LootStageCalculator.getBiomeBonus()` uses BiomeProperties; `TerritoryZombieSpawner.populate()` scales spawn count by biome density multiplier
- **Data files**: 7 biome JSONs in `data/sevendaystominecraft/worldgen/biome/` with appropriate temperature, precipitation, sky/fog/water/grass colors, carvers, and vegetation features
- **Overworld biome placement**: BZHS Apocalypse world preset places all 7 biomes via multi-noise source; custom noise settings with biome-specific surface rules; world preset tagged for world creation screen
- **Surface rules**: Snow block (Tundra), sand/sandstone (Desert), coarse dirt (Burned Forest), terracotta+gravel (Wasteland), podzol (Pine Forest), grass (Forest/Plains)
- **Not yet implemented**: City/road grid; POI template system; wasteland weather events

### Magazine / Skill Book System
- **Package**: `com.sevendaystominecraft.magazine`
- **MagazineSeries**: Record defining a series (id, displayName, issueCount, issueDescriptions, masteryDescription)
- **MagazineRegistry**: Static registry of all 6 series (steady_steve x7, block_brawler x5, sharpshot_sam x7, the_tinkerer x5, overworld_chef x5, dungeon_tactician x7)
- **MagazinePlayerData**: Per-player read tracking stored in `SevenDaysPlayerStats` → serialized to NBT under "Magazines" key
- **MagazineItem**: Right-click to read; grants permanent passive bonus; consumes item; tracks completion; awards mastery on series completion
- **ModMagazines**: DeferredRegister auto-generates all 36 item registrations from MagazineRegistry
- **Creative tab**: "BZHS Magazines" tab shows all magazine items
- Items: `magazine_<seriesId>_<issue>` (e.g., `magazine_steady_steve_1` through `magazine_steady_steve_7`)
- Tooltips show series name, issue bonus, and mastery reward
- Each magazine is stacksTo(1), consumed on use, cannot be re-read

## Loot & Crafting System (Spec §6) — DONE
- **Items**: 17 core materials + Survivor's Coin registered via ModItems with creative tabs
- **Quality Tiers**: T1 (Poor, ×0.7) → T6 (Legendary, ×1.5) with color codes and mod slot scaling
- **Workstations**: 7 workstation blocks (Campfire, Grill, Workbench, Forge, Cement Mixer, Chemistry Station, Advanced Workbench) with block entities, container menus, and GUI screens; fuel-based workstations tick to process items
- **Loot Containers**: 8 loot container blocks (Trash Pile, Cardboard Box, Gun Safe, Munitions Box, Supply Crate, Kitchen Cabinet, Medicine Cabinet, Bookshelf) with loot generation scaled by player loot stage and configurable respawn timers
- **Loot Stage**: Calculated per player: `floor((level×0.5) + (days×0.3) + biomeBonus + perkBonus)`, synced to client every 10 seconds
- **Scrapping**: Tools/weapons/armor/electronics/food can be scrapped into materials, with workbench giving full yield and inventory giving 50%
- **Config**: `loot.toml` with respawnDays, abundanceMultiplier, qualityScaling options
- **Command**: `/bzhs loot_stage` shows player's current loot stage with breakdown
- **4×4 Crafting Grid**: Deferred — Mixin complexity on NeoForge 1.21.4's CraftingMenu/InventoryMenu is too high; workstation-based crafting is implemented first as the task spec allows
- `BlockEntityType` in NeoForge 1.21.4: No `Builder` class — use constructor directly: `new BlockEntityType<>(Supplier, Block...)`

#### XP, Leveling & Perk System (Spec §1.4, §5) — DONE
- **LevelManager**: XP gain from zombie kills (uses ZombieVariant.xpReward + modifier bonus) and block mining (1-5 XP by hardness)
  - Formula: `XP_to_next = floor(1000 × level ^ 1.05)` — handles multi-level gains
  - Each level-up: +1 perk point; every 10 levels: +1 bonus attribute point
- **PerkRegistry**: 45 perks total (8 per attribute tree + 5 Tier-10 masteries)
  - Strength: Brawler, Iron Fists, Skull Crusher, Unstoppable Force, Campfire Cook, Pack Mule, Deep Striker, Deep Veins, Titan
  - Perception: Archery, Gunslinger, Rifle Guy, Demolitions Expert, Lock Picking, Keen Scavenger, Treasure Hunter, Spear Master, Eagle Eye
  - Fortitude: Healing Factor, Iron Gut, Rule 1 Cardio, Green Thumb, Pain Tolerance, Heavy Armor, Well Insulated, Field Medic, Unkillable
  - Agility: Light Armor, Parkour, Shadow Strike, Nightstalker, Deep Cuts, Run and Gun, Flurry of Blows, Gunslinger (Agility), Ghost
  - Intellect: Advanced Engineering, Gearhead, Better Barter, Bold Explorer, Physician, Electrocutioner, Robotics Inventor, Charismatic Nature, Mastermind
- **Active perk effects**:
  - Healing Factor: +20% health regen per rank
  - Rule 1 Cardio: +10% stamina regen + 5% sprint speed per rank
  - Unstoppable Force: -15% stamina cost on all actions per rank
  - Pain Tolerance: -10% damage taken per rank
  - Deep Striker: +15% mining speed per rank
  - Well Insulated: ±10°F comfort zone per rank
  - Unkillable (Fortitude T10): Fatal damage → survive at 1 HP + 10s invulnerability (60 min cooldown)
  - Ghost (AGI T10): Stealth kills produce zero heatmap noise
- **Commands**: `/bzhs level|stats`, `/bzhs perk <id> [rank]`, `/bzhs attribute <STR|PER|FOR|AGI|INT>`, `/bzhs perks`
- **HUD**: XP bar + level counter added to stats overlay
- **Persistence**: All XP/level/perk data serialized to NBT, synced via network payload, preserved through death/respawn

