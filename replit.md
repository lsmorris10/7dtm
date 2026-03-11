# 7 Days to Minecraft — NeoForge Mod

## Project Overview
A total conversion mod for Minecraft 1.21.4 (NeoForge) that brings 7 Days to Die survival gameplay into Minecraft. Aligned to 7 Days to Die 2.6 Experimental (Feb 2026).

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
│   └── SevenDaysPlayerStats.java   — Player stats implementation (Food, Water, Stamina, etc.)
├── client/
│   ├── StatsHudOverlay.java        — HUD overlay for player stats + blood moon indicator
│   ├── BloodMoonClientState.java   — Client-side blood moon state singleton
│   └── BloodMoonSkyRenderer.java   — Red sky/fog tint during blood moon
├── config/
│   ├── SurvivalConfig.java         — Server-side survival config (survival.toml)
│   └── HordeConfig.java            — Server-side horde/blood moon config (horde.toml)
├── horde/
│   ├── BloodMoonTracker.java       — SavedData for day tracking & blood moon phase state
│   ├── BloodMoonEventHandler.java  — Server tick handler for blood moon timeline + sleep prevention
│   └── HordeSpawner.java           — Wave spawning logic with scaling formula
├── mixin/
│   ├── FoodDataMixin.java          — Cancels vanilla food saturation
│   ├── LivingEntityHurtMixin.java  — Custom damage handling
│   ├── PlayerHealMixin.java        — Blocks vanilla passive regen
│   └── SprintBlockMixin.java       — Sprint blocked when low stamina
└── network/
    ├── ModNetworking.java          — Packet channel registration (stats + blood moon)
    ├── SyncPlayerStatsPayload.java — Client/server stats sync packet
    └── BloodMoonSyncPayload.java   — Blood moon state sync packet
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

#### Horde Night System — Milestone 3 (Spec §4)
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
  - Spawns zombies 24-40 blocks from each player at surface level
  - Wave multiplier: `1 + 0.25 * waveIndex` for escalating difficulty
  - Currently uses vanilla zombies (placeholder for future custom variants)
- **HordeConfig**: `horde.toml` with all spec §4.2 config keys
- **BloodMoonSyncPayload**: Network packet syncing blood moon state to clients
- **BloodMoonClientState**: Client singleton storing active state, wave info, day number
- **BloodMoonSkyRenderer**: Fog color tint that gradually ramps to red during active blood moon

## Known Bugs / Issues
1. **Sprint bug (known, unresolved)**: Sprint can get stuck — holding W alone gives infinite sprint (stamina drains but sprint doesn't cancel). Simplified from speed-heuristic approach to direct `isSprinting()` checks. Likely needs a client-side Mixin on `LocalPlayer.aiStep()` for proper fix.
2. **Temperature**: Adjustment rate changed to 0.3°F/s — needs long-term gameplay verification
3. **Debuffs**: Infection/bleeding effects unverified in gameplay testing
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
- Full spec in `docs/7dtm_final_spec.md` (2273 lines, 20 sections)
- See `archive/` for resolved spec drafts
- See `PROJECT_NOTES.md` for session-by-session status and known issues

## NeoForge API Notes
- `EntityType.create()` requires `EntitySpawnReason` parameter in 1.21.4
- `SoundEvents` fields are `Reference<SoundEvent>`, use `.value()` for direct access
- `@EventBusSubscriber(bus = Bus.MOD)` is deprecated but still functional
- `SavedData` uses `Factory<>` with constructor + load function for `computeIfAbsent`
- `CanPlayerSleepEvent` is the correct hook for blocking sleep (not `PlayerSleepInBedEvent`)
- Sprint detection: avoid speed-based heuristics; use `player.isSprinting()` directly and handle client-side via Mixin or sync packets

## Spec / Roadmap
The full implementation is tracked in `docs/7dtm_final_spec.md` with 19 phases.
Milestones 1-3 complete. Next priorities: sprint bug fix, custom zombie entities (§3), heatmap system (§1.3).
