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
│   └── SevenDaysPlayerStats.java   — Player stats implementation (Food, Water, Stamina, etc.)
├── client/
│   ├── StatsHudOverlay.java        — HUD overlay for player stats + blood moon indicator
│   ├── CompassOverlay.java         — 360° compass strip at top-center with cardinal/intercardinal markers + heat indicator
│   ├── MinimapOverlay.java         — Top-right minimap with terrain colors, player dot, nearby player dots
│   ├── NearbyPlayersClientState.java — Client-side state for synced nearby player positions
│   ├── ChunkHeatClientState.java   — Client-side state for current chunk heat value
│   ├── HudClientResetHandler.java  — Resets client HUD state on disconnect
│   ├── BloodMoonClientState.java   — Client-side blood moon state singleton
│   ├── BloodMoonSkyRenderer.java   — Red sky/fog tint during blood moon
│   ├── ModEntityRenderers.java     — Entity renderer registration for all 18 zombie types
│   └── ScaledZombieRenderer.java   — ZombieRenderer subclass with configurable scale factor
├── block/
│   ├── ModBlocks.java              — DeferredRegister for all custom blocks (workstations + loot containers)
│   ├── ModBlockEntities.java       — Block entity type registration
│   ├── workstation/
│   │   ├── WorkstationType.java    — Enum: Campfire, Grill, Workbench, Forge, Cement Mixer, Chemistry Station, Advanced Workbench
│   │   ├── WorkstationBlock.java   — BaseEntityBlock for all workstation types
│   │   ├── WorkstationBlockEntity.java — Block entity with fuel, crafting progress, input/output slots
│   │   ├── WorkstationMenu.java    — Container menu for workstation GUI
│   │   └── WorkstationScreen.java  — Client-side GUI screen for workstations
│   └── loot/
│       ├── LootContainerType.java  — Enum: Trash Pile, Cardboard Box, Gun Safe, Munitions Box, etc.
│       ├── LootContainerBlock.java — BaseEntityBlock for loot containers
│       ├── LootContainerBlockEntity.java — Block entity with loot generation, respawn tracking
│       ├── LootContainerMenu.java  — Container menu for loot containers
│       └── LootContainerScreen.java — Client-side GUI for loot containers
├── command/
│   └── LootStageCommand.java       — /bzhs loot_stage debug command
├── config/
│   ├── SurvivalConfig.java         — Server-side survival config (survival.toml)
│   ├── HordeConfig.java            — Server-side horde/blood moon config (horde.toml)
│   ├── ZombieConfig.java           — Zombie variant stats/modifiers config (zombies.toml)
│   ├── HeatmapConfig.java          — Heatmap config (heatmap.toml): enabled, decay/spawn multipliers
│   └── LootConfig.java             — Loot config (loot.toml): respawnDays, abundanceMultiplier, qualityScaling
├── crafting/
│   └── ScrappingSystem.java        — Item scrapping into component materials (workbench vs inventory yield)
├── entity/
│   ├── ModEntities.java            — DeferredRegister for all custom entity types + attribute events
│   └── zombie/
│       ├── BaseSevenDaysZombie.java — Base zombie entity with variant stats, modifiers, night speed bonus, radiated regen, behavior tree goals
│       ├── ZombieVariant.java       — Enum of all 18 zombie variants with base stats
│       ├── ai/
│       │   ├── BlockHPRegistry.java       — Block HP lookup table for zombie block breaking (wood=10, stone=30, cobblestone=50, iron=200, obsidian=500)
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
│   ├── ModItems.java               — DeferredRegister for all items (materials, melee weapons, ranged weapons, ammo)
│   ├── ModCreativeTabs.java        — Creative tabs: Materials, Workstations, Weapons, Loot Containers
│   ├── QualityTier.java            — Quality tier enum (T1-T6: Poor → Legendary) with stat multipliers
│   └── weapon/
│       └── RangedWeaponItem.java   — Right-click-to-fire ranged weapon (ammo consumption, cooldown, durability)
├── loot/
│   ├── LootStageCalculator.java    — Loot stage formula: floor((level×0.5) + (days×0.3) + biomeBonus + perkBonus)
│   └── LootStageHandler.java       — Periodic loot stage sync to client
├── menu/
│   └── ModMenuTypes.java           — Menu type registration for workstations and loot containers
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
│   └── SprintBlockMixin.java       — Sprint blocked when low stamina
└── network/
    ├── ModNetworking.java          — Packet channel registration (stats + blood moon + nearby players + chunk heat)
    ├── SyncPlayerStatsPayload.java — Client/server stats sync packet
    ├── BloodMoonSyncPayload.java   — Blood moon state sync packet
    ├── SyncNearbyPlayersPayload.java — Server→client nearby player positions (float coords, capped at 64)
    ├── SyncChunkHeatPayload.java   — Server→client current chunk heat value
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
  - **Behavior tree goals** (registered at priorities 3-5, inherited by all 16 subclasses):
    - `ZombieBreakBlockGoal` (P3): Breaks obstructing blocks to reach targets, with block HP system, vanilla break animation, mobGriefing gamerule respect
    - `ZombieHordePathGoal` (P4): During Blood Moon, horde zombies path toward nearest player (64 blocks, 128 on day 21+)
    - `ZombieInvestigateGoal` (P5): When idle, investigates high-heat chunks from heatmap system, wanders locally then re-queries
  - **BlockHPRegistry**: Block HP lookup table (glass=3, wood=10, log=15, stone=30, cobblestone=50, iron=200, obsidian=500, bedrock=unbreakable)
  - **ZombieConfig additions**: `blockBreakEnabled`, `blockBreakSpeedMultiplier`, `investigateRange`, `hordePathRange`, `hordePathRangeDay21`, `blockHPMultiplier`
- **18 variant entity types** registered via `DeferredRegister<EntityType<?>>`
  - Special mechanics per spec §3.2: explosions, projectiles, chain lightning, fire trails, wall climbing, healing aura, screamer spawning, flying dive attacks, ground pound AoE
- **ZombieConfig** (`zombies.toml`): Per-variant HP/damage/speed overrides, all special mechanic tuning values, modifier multipliers
- **ModEntities**: Registration with `EntityAttributeCreationEvent` for all 18 types

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

## Known Bugs / Issues
1. **Sprint bug (known, unresolved)**: Sprint can get stuck — holding W alone gives infinite sprint (stamina drains but sprint doesn't cancel). Simplified from speed-heuristic approach to direct `isSprinting()` checks. Likely needs a client-side Mixin on `LocalPlayer.aiStep()` for proper fix.
2. **Temperature**: Adjustment rate changed to 0.3°F/s — needs long-term gameplay verification
3. **Debuffs**: All 12 types have triggers and effects implemented; gameplay balance verification pending
4. **Horde spawn balance**: Needs verification that spawn counts match intended difficulty

## Workflow
- **Build Mod**: `export JAVA_HOME=$(dirname $(dirname $(which java))) && ./gradlew build --no-daemon`
  - Compiles Java sources, processes resources, assembles the mod JAR
  - Output JAR: `build/libs/sevendaystominecraft-0.1.0-alpha.jar`
  - First run downloads Minecraft + NeoForge dependencies (~several minutes)
  - This is a build workflow (console output type), not a web server

## Environment
- Java 21 installed via Nix (`jdk21` package)
- `gradlew` script created for Linux (gradlew.bat only existed for Windows)
- `gradle/wrapper/gradle-wrapper.jar` downloaded separately (excluded from git)

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
- `convertsInWater()` → `isSensitiveToWater()`; `isSunSensitive()` removed entirely
- `isGlowing()` → `isCurrentlyGlowing()`
- `getExperienceReward()` → `getBaseExperienceReward(ServerLevel level)`
- `EntityType.create()` requires `EntitySpawnReason` parameter in 1.21.4
- `@EventBusSubscriber(bus = Bus.MOD)` is deprecated but still functional
- `SavedData` uses `Factory<>` with constructor + load function for `computeIfAbsent`
- `CanPlayerSleepEvent` is the correct hook for blocking sleep (not `PlayerSleepInBedEvent`)
- Sprint detection: avoid speed-based heuristics; use `player.isSprinting()` directly and handle client-side via Mixin or sync packets
- Config pattern: Static `SPEC` + `INSTANCE` via `new ModConfigSpec.Builder().configure(Klass::new)`

#### Weapons System — DONE
- **Melee weapons**: 3 weapons via `SwordItem` + `ToolMaterial`:
  - Stone Club (4 dmg, -2.8 speed, wood durability), Baseball Bat (5 dmg, -2.6 speed), Iron Sledgehammer (9 dmg, -3.4 speed, iron durability)
- **Ranged weapons**: 2 guns via `RangedWeaponItem` (right-click fire, ammo consumption, cooldown, durability):
  - 9mm Pistol (8 dmg, 8-tick cooldown, 250 dur), AK-47 (12 dmg, 4-tick cooldown, 500 dur)
- **Ammo**: 9mm Ammo, 7.62mm Ammo (stackable to 64)
- **BulletEntity**: `ThrowableItemProjectile` with near-zero gravity (0.01), configurable damage, crit particles
- **Crafting**: Melee at Workbench, guns at Advanced Workbench, ammo at Chemistry Station
- **Creative tab**: BZHS Weapons tab with all melee + ranged + ammo items

## Spec / Roadmap
The full implementation is tracked in `docs/bzhs_final_spec.md` with 19 phases.
Milestones 1-9 complete (except #4 Temperature which is partial). Milestone 3 (Debuffs): DONE — all 12 debuff types. Milestone 5 (Heatmap): DONE. Milestone 6 (Loot & Crafting): DONE — workstations, loot containers, scrapping, quality tiers. Milestone 7 (XP/Leveling/Perks): DONE — full perk registry, level-up system, commands, HUD XP bar. Milestone 8 (Blood Moon/Horde Night): DONE. Milestone 9 (HUD): DONE — compass, minimap, player tracking, stats overlay. Milestone 10 (Weapons): DONE — melee + ranged weapons, ammo, crafting recipes. Next priorities: sprint bug fix, custom textures/models, world generation.

## Loot & Crafting System (Spec §6) — DONE
- **Items**: 17 core materials + Dukes Casino Token registered via ModItems with creative tabs
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
  - Strength: Brawler, Pummel Pete, Skull Crusher, Sexual Tyrannosaurus, Master Chef, Pack Mule, Miner 69er, Mother Lode, Titan
  - Perception: Archery, Gunslinger, Rifle Guy, Demolitions Expert, Lock Picking, Lucky Looter, Treasure Hunter, Spear Master, Eagle Eye
  - Fortitude: Healing Factor, Iron Gut, Rule 1 Cardio, Living Off the Land, Pain Tolerance, Heavy Armor, Well Insulated, Self-Medicated, Unkillable
  - Agility: Light Armor, Parkour, Hidden Strike, From the Shadows, Deep Cuts, Run and Gun, Flurry of Blows, Gunslinger (Agility), Ghost
  - Intellect: Advanced Engineering, Grease Monkey, Better Barter, Daring Adventurer, Physician, Electrocutioner, Robotics Inventor, Charismatic Nature, Mastermind
- **Active perk effects**:
  - Healing Factor: +20% health regen per rank
  - Rule 1 Cardio: +10% stamina regen + 5% sprint speed per rank
  - Sexual Tyrannosaurus: -15% stamina cost on all actions per rank
  - Pain Tolerance: -10% damage taken per rank
  - Miner 69er: +15% mining speed per rank
  - Well Insulated: ±10°F comfort zone per rank
  - Unkillable (Fortitude T10): Fatal damage → survive at 1 HP + 10s invulnerability (60 min cooldown)
  - Ghost (AGI T10): Stealth kills produce zero heatmap noise
- **Commands**: `/bzhs level|stats`, `/bzhs perk <id> [rank]`, `/bzhs attribute <STR|PER|FOR|AGI|INT>`, `/bzhs perks`
- **HUD**: XP bar + level counter added to stats overlay
- **Persistence**: All XP/level/perk data serialized to NBT, synced via network payload, preserved through death/respawn
