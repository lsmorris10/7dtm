# Project Notes

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

1. **SPRINT BUG** (unresolved since Milestone 2):
   - Sprint can get stuck — holding W alone gives infinite sprint. Stamina drains but sprint doesn't cancel. Needs a client-side Mixin on `LocalPlayer.aiStep()`.

2. **TEST NEEDED — Temperature**: Adjustment rate at 0.3°F/s, needs gameplay verification.

3. **TEST NEEDED — Debuffs**: Infection/bleeding effects unverified in gameplay.

4. **TEST NEEDED — Horde spawn balance**: Verify spawn counts match intended difficulty.

5. **TODO — HUD polish**: Compass/minimap not yet started.

6. **TODO — Heatmap**: Stub exists but needs actual chunk data for noise events.

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

## Next Up
- Sprint bug fix (Mixin approach).
- Loot/crafting system (Spec §5-6) or heatmap system (§1.3) depending on priority.
