# Project Notes

---

## Overnight Tasks — All Merged ✓

All three overnight task agents completed and merged successfully:

1. **Task #5 — Heatmap Spawning System** (Spec §1.3) — **MERGED** ✓
2. **Task #6 — Hide Vanilla Hunger Bar** — **MERGED** ✓
3. **Task #7 — HUD Health Bar Replacing Vanilla Hearts** — **MERGED** ✓

---

## Testing Today — March 12, 2026

### Build Verification
- [ ] Run `./gradlew build` — confirm it compiles cleanly with zero errors

### Heatmap Spawning (Task #5)
- [ ] Place a large cluster of torches in one chunk to raise heat (+2 per torch)
- [ ] Break a bunch of blocks in one area to add heat (+0.5 per block)
- [ ] Run `/7dtm heat` — verify it reports the correct heat value for your chunk
- [ ] Get heat to 25+ and watch for 1-2 scout Walkers to spawn
- [ ] Get heat to 50+ and confirm a Screamer spawns
- [ ] Get heat to 75+ and confirm a mini-horde of 8-12 mixed zombies spawns
- [ ] Run `/7dtm heat_clear` — verify all heat resets to 0 and spawning stops
- [ ] Confirm heat decays over time when you stop doing noisy things

### Vanilla Hunger Bar Hidden (Task #6)
- [ ] Verify the vanilla drumstick hunger icons are completely gone
- [ ] Confirm the custom HUD still shows Food, Water, and Stamina bars correctly

### HUD Health Bar (Task #7)
- [ ] Verify the vanilla heart icons are completely gone
- [ ] Confirm the custom HUD shows an HP bar in the stats panel
- [ ] Take damage and verify the HP bar updates in real-time

### Temperature System
- [ ] Check the 0.3°F/s adjustment rate — does it feel smooth and reasonable?
- [ ] Move between biomes and verify temperature shifts

### Debuffs
- [ ] Get hit by zombies and check for Infection/Bleeding effects
- [ ] Verify bleeding drains HP (-1 HP every 3 sec)
- [ ] Verify infection progression (Stage 1 → Stage 2)

### Horde Spawn Balance
- [ ] Trigger a blood moon or use commands to spawn a horde wave
- [ ] Verify spawn counts feel right for the current game day
- [ ] Check that zombie composition matches the tier table (day thresholds)

### Edge Cases (from `docs/if-bored.md`)
- [ ] Spawn all zombie types one by one — no crashes
- [ ] Test modifier variants (Radiated, Charged, Infernal) on eligible base types
- [ ] Try cross-system interactions: heatmap during blood moon, stacking debuffs, etc.

---

## Session — March 12, 2026

### What was done
**Milestone 4 — Custom Zombie System (Spec §3.1-3.2): COMPLETE**

Built the entire custom zombie entity system from scratch. Here's the breakdown:

1. **ZombieVariant enum** (`entity/zombie/ZombieVariant.java`)
   - All 18 variants defined with base HP, damage, speed, XP, and minimum spawn day.
   - 3 modifier types (Radiated, Charged, Infernal) with configurable stat multipliers.

2. **ZombieConfig** (`config/ZombieConfig.java`)
   - New `zombies.toml` config file with per-variant HP/damage/speed overrides.
   - All special mechanic tuning values exposed (explosion radius, bile damage, heal range, etc.).
   - Modifier multiplier configs (HP/damage/speed) for Radiated, Charged, Infernal.

3. **BaseSevenDaysZombie** (`entity/zombie/BaseSevenDaysZombie.java`)
   - Base class extending `Zombie`, applies variant stats on spawn via `finalizeSpawn()` + tick fallback.
   - Modifier system with proper lifecycle: modifier stats apply AFTER variant stats, persist in NBT, reapply on world reload.
   - Night speed bonus: all zombies get +50% movement speed at night (configurable via `nightSpeedBonus`).
   - Radiated regen: 2 HP/sec healing tick for Radiated modifier (configurable).

4. **16 special variant subclasses** — each with unique mechanics:
   - BloatedWalker (death explosion), SpiderZombie (wall climb + jump), FeralWight (permanent sprint + glow), CopZombie (acid spit + low-HP explosion), ScreamerZombie (summons reinforcements + flees), DemolisherZombie (chest-hit explosion + headshot mechanic), ChargedZombie (chain lightning), InfernalZombie (fire trail + burn on melee), MutatedChuck (ranged vomit + nausea), ZombieDog (Wolf base, pack spawns), Vulture (Phantom base, dive attacks), ZombieBear (charge + AoE swipe), BehemothZombie (boss, knockback immune, ground pound AoE), NurseZombie (heals nearby zombies), FrozenLumberjack, Soldier.

5. **ModEntities** (`entity/ModEntities.java`)
   - `DeferredRegister<EntityType<?>>` registration for all 18 entity types.
   - `EntityAttributeCreationEvent` handler wiring up attributes for every type.
   - Uses `ResourceKey.create(Registries.ENTITY_TYPE, ...)` as required by NeoForge 1.21.4's `build()`.

6. **HordeSpawner integration** (`horde/HordeSpawner.java`)
   - Composition table with 5 tiers: day 7 / 14 / 21 / 28 / 49+.
   - Config day thresholds (`feralDay`, `demolisherDay`, `chargedDay`, `infernalDay`) now properly gate which variants can appear — if the day hasn't been reached, those slots roll back to Walkers.
   - Radiated modifier randomly applied to one of 5 base types (Walker, Crawler, Feral, Cop, Soldier) for day 28+.

7. **Zombie Bestiary doc** (`docs/zombie_guide.md`)
   - Full player-facing guide with stats, abilities, and strategies for all 18 zombie types + 3 modifiers.

### Code review fixes applied
- **Modifier timing bug**: `setModifier()` was applying modifier stats immediately, but `finalizeSpawn()/tick()` would then overwrite them with base variant stats. Fixed by deferring modifier application until after variant stats are set.
- **Modifier persistence bug**: On world reload, `readAdditionalSaveData()` restored the modifier enum but never reapplied the stat multipliers. Fixed by resetting `statsApplied` flag in `readAdditionalSaveData()` so the next tick re-runs the full stat pipeline.
- **Missing Radiated regen**: Spec §3.2 says Radiated zombies heal 2 HP/sec — wasn't implemented. Added a tick-based heal in `BaseSevenDaysZombie.tick()`.
- **Night speed bonus unused**: Config key `nightSpeedBonus` was defined but never read. Now applied in `tick()` based on time-of-day check.
- **HordeConfig day thresholds ignored**: `feralDay`/`demolisherDay`/`chargedDay`/`infernalDay` were in config but `HordeSpawner` used hardcoded tables. Added `applyConfigThresholds()` to gate variant slots.

### Entity renderer crash fix
- **Problem**: All 18 zombie types crashed the game on spawn with `NullPointerException: entityrenderer is null` — no renderers were registered.
- **Fix**: Created `client/ModEntityRenderers.java` — registers renderers for all 18 entity types via `EntityRenderersEvent.RegisterRenderers` on the MOD bus (client-only).
- Created `client/ScaledZombieRenderer.java` — extends `ZombieRenderer` with configurable scale factor, overrides `scale()` to apply PoseStack scaling.
- **Renderer assignments**:
  - Standard humanoid zombies (Walker, Crawler, FrozenLumberjack, BloatedWalker, SpiderZombie, FeralWight, Cop, Screamer, MutatedChuck, Nurse, Soldier, Charged, Infernal): `ZombieRenderer` (1.0x)
  - Demolisher: `ScaledZombieRenderer` at 1.2x (larger armored zombie)
  - Behemoth: `ScaledZombieRenderer` at 1.8x (boss-sized)
  - ZombieDog: `ScaledZombieRenderer` at 0.5x (dog-sized)
  - Vulture: `ScaledZombieRenderer` at 0.4x (small flying)
  - ZombieBear: `ScaledZombieRenderer` at 1.5x (bear-sized)
- **Note**: All entities extend `Zombie` via `BaseSevenDaysZombie`, so vanilla animal renderers (`WolfRenderer`, `PhantomRenderer`, `PolarBearRenderer`) cannot be used directly (type mismatch — those renderers expect their specific entity classes). Scaled zombie models with correct bounding boxes provide visually distinct sizes. Full custom models/textures are future work.

### Name tags above zombies (Task #2 — merged from task agent)
- **What**: Added floating name tags above all custom zombie entities so players can identify types before custom textures exist.
- **How**: `BaseSevenDaysZombie.applyNameTag()` sets `setCustomName()` + `setCustomNameVisible(true)` using a formatted display name built from variant + modifier.
- Converts enum names to Title Case (e.g. `FROZEN_LUMBERJACK` → "Frozen Lumberjack", `RADIATED` + `COP` → "Radiated Cop").
- Called from `applyAllStats()`, `setModifier()`, and `readAdditionalSaveData()` so names stay correct through spawn, modifier changes, and save/load.
- All animal-type zombies inherit this automatically — no subclass changes needed.

### Post-merge setup
- Created `scripts/post-merge.sh` — runs `./gradlew build --no-daemon -q` after task agent merges to verify compilation.
- Configured in `.replit` with 180s timeout.

### Build status
BUILD SUCCESSFUL — 0 errors, only deprecation warnings on `@EventBusSubscriber(bus = Bus.MOD)` (still functional in NeoForge 21.4.140).

---

## Session — March 11, 2026

### What was done
**Milestone 3 — Horde Night & Blood Moon System: COMPLETE**

- BloodMoonTracker (SavedData), BloodMoonEventHandler (tick handler), HordeSpawner (wave spawning).
- Full blood moon timeline: warning day before, sky red at 18:00, siren at 18:30, horde at 22:00, waves every 10 min, dawn cleanup at 06:00.
- Sleep prevention during blood moon via `CanPlayerSleepEvent`.
- Late-join sync for blood moon state.
- BloodMoonSkyRenderer for red fog tint.
- HordeConfig (`horde.toml`) with all §4.2 config keys.

---

## Known Bugs / Polish To Address

1. **SPRINT BUG — FIX ANOTHER DAY** (unresolved since Milestone 2):
   - Sprint can get stuck — holding W alone gives infinite sprint. Stamina drains but sprint doesn't cancel. Needs a client-side Mixin on `LocalPlayer.aiStep()`.
   - **Not on today's test list.** This is a known issue that requires a proper client-side fix.

2. **TEST NEEDED — Temperature**: Adjustment rate at 0.3°F/s, needs gameplay verification. (On today's test list.)

3. **TEST NEEDED — Debuffs**: Infection/bleeding effects unverified in gameplay. (On today's test list.)

4. **TEST NEEDED — Horde spawn balance**: Verify spawn counts match intended difficulty. (On today's test list.)

5. **TODO — HUD polish**: Compass/minimap not yet started.

6. **DONE — Heatmap Spawning** (Task #5): Full heatmap system with per-chunk heat tracking, threshold-based spawning, and debug commands. Merged. ✓

7. **DONE — Hide Vanilla Hunger Bar** (Task #6): Vanilla drumstick icons removed; custom HUD handles Food/Water/Stamina. Merged. ✓

8. **DONE — HUD Health Bar** (Task #7): HP bar added to custom stats panel, vanilla heart icons hidden. Merged. ✓

### HP Display and Zombie Size Fixes (Task #3)

1. **HP display under name tags**
   - `BaseSevenDaysZombie.applyNameTag()` now includes HP: displays "Zombie Name" on one line and "currentHP / maxHP" below it in red.
   - Tick-based HP refresh every 5 ticks — only updates the custom name when HP actually changes (tracks `lastDisplayedHP`).
   - `ScaledZombieRenderer` overrides `renderNameTag()` to split the `\n`-delimited custom name into two lines, rendering the name higher and the HP counter below.
   - `ScaledZombieRenderer` overrides `extractRenderState()` to push `nameTagAttachment` up by 1.0 block, making both lines clearly float above the entity.

2. **All zombie types now use ScaledZombieRenderer**
   - Previously only Demolisher, Behemoth, ZombieDog, Vulture, and ZombieBear used `ScaledZombieRenderer`. Now all 18 types use it (standard zombies at scale 1.0) to get the raised name tag and two-line HP rendering.

3. **Bounding box size corrections in ModEntities**
   - Bloated Walker: 0.8×1.95 → 0.9×2.1 (wider, taller)
   - Spider Zombie: 0.6×1.95 → 0.9×0.8 (low profile)
   - Demolisher: 0.8×2.2 → 0.9×2.4 (bulkier)
   - Behemoth: 1.2×2.5 → 1.6×3.0 (massive)
   - Vulture: 0.8×0.6 → 0.9×0.5 (small flying)
   - Zombie Bear: 1.4×1.4 (confirmed, no change)
   - Zombie Dog: 0.6×0.85 (confirmed, no change)

4. **Renderer scale factor updates in ModEntityRenderers**
   - Bloated Walker: new, 1.1x (fatter visual)
   - Spider Zombie: new, 0.5x (low profile visual)
   - Demolisher: 1.2x → 1.3x
   - Behemoth: 1.8x → 2.0x
   - Zombie Bear, Zombie Dog, Vulture: unchanged

5. **Zombie guide updated** — Added Size (W × H) row to every zombie's stat table in `docs/zombie_guide.md`.

---

## Session — March 12, 2026 (continued)

### What was done
**Milestone 5 — Heatmap System (Spec §1.3): COMPLETE**

Built the full per-chunk heatmap system that drives ambient zombie spawns outside of blood moon nights.

1. **HeatSource** (`heatmap/HeatSource.java`)
   - Individual heat source object with amount (float), decay rate (per minute), and chunk radius.
   - NBT serialization for persistence.

2. **HeatmapData** (`heatmap/HeatmapData.java`)
   - SavedData storing all active heat sources keyed by chunk position (long).
   - Heat capped at 100 per chunk (spec-accurate).
   - Additive heat with overflow protection — new sources are clamped to remaining capacity.
   - Full NBT save/load for persistence across server restarts.

3. **HeatmapManager** (`heatmap/HeatmapManager.java`)
   - Server-side tick handler running every 20 ticks (1 second).
   - Decays all heat sources using per-second math: `decayPerMinute * decayMultiplier / 60`.
   - Removes depleted sources and empty chunks automatically.
   - Marks data dirty every decay pass for reliable persistence.
   - Invokes HeatmapSpawner.tick() after decay processing.

4. **HeatEventHandler** (`heatmap/HeatEventHandler.java`)
   - Block break: +0.5 heat, 3-chunk radius, -2/min decay
   - Torch placement: +2 heat, 1-chunk radius, -1/min decay (detects TorchBlock and WallTorchBlock)
   - Explosion: +25 heat, 6-chunk radius, -2/min decay
   - Sprint: +0.2/sec, 2-chunk radius, -3/min decay (wired up from PlayerStatsHandler stub)
   - Heat radiates to neighboring chunks within radius with distance-based falloff (50% × linear falloff).

5. **HeatmapSpawner** (`heatmap/HeatmapSpawner.java`)
   - Threshold-based spawning with per-chunk cooldowns:
     - Heat 25+: 1-2 scout Walkers (30s cooldown)
     - Heat 50+: Screamer guaranteed (60s cooldown)
     - Heat 75+: Mini-horde of 8-12 mixed zombies from nearest dark area (90s cooldown)
     - Heat 100: Enters "wave mode" — continuous waves every 90s until heat drops below 75
   - Wave mode: tracked via `waveActiveChunks` set; chunk enters wave mode at heat ≥ 100, exits when heat < 75 (75% of wave threshold × threshold multiplier).
   - Dark area preference: mini-horde and wave spawns try 20 attempts for blocks with light level ≤ 7; falls back to any valid position.
   - Spawning disabled during active blood moon.
   - All cooldowns and wave state cleared by `/7dtm heat_clear`.

6. **HeatmapCommand** (`heatmap/HeatmapCommand.java`)
   - `/7dtm heat`: Shows current chunk heat value with color coding + effective spawn thresholds.
   - `/7dtm heat_clear`: Op-only (permission level 2) — resets all heat data and cooldowns.

7. **HeatmapConfig** (`config/HeatmapConfig.java`)
   - `heatmap.toml` with: `enabled` (bool), `decayMultiplier` (0.1-5.0), `spawnThresholdMultiplier` (0.5-3.0).
   - Registered in main mod class constructor.

### Build status
BUILD SUCCESSFUL — 0 errors.

### Out of scope (stubs only)
- Gunshot heat (no custom weapons yet)
- Forge/campfire/vehicle heat (no custom blocks/vehicles yet)
- Screamer scream heat (already partially handled in ScreamerZombie)

## Next Up
- **Sprint bug fix** — Client-side Mixin on `LocalPlayer.aiStep()` to properly cancel sprint when stamina is depleted.
- **Loot & crafting system** (Spec §5-6) — Custom loot tables, crafting recipes, and item progression.
- **Custom textures & models** — Replace scaled zombie renderers with proper custom models and textures for each variant.
- **World generation** — Custom biomes, structures, and POI generation per the spec.
