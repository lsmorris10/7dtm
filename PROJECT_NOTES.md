# Project Notes

---

## Development Workflow (Permanent)

**Primary coding environment:** Replit Agent
**Testing environment:** Antigravity (local PC)

| Environment | Role | Push Frequency |
|-------------|------|---------------|
| **Replit** | Main coding, building, pushing to GitHub | Most pushes |
| **Antigravity** | Pull, test mod in-game on local PC, occasional code + push | Less frequent |

**Sync Rules:**
1. **Before coding in Antigravity:** Always `git pull` first to grab latest from Replit.
2. **Before starting work in Replit after an Antigravity session:** Run `git fetch origin && git reset --hard origin/master` to pick up anything pushed from Antigravity.
3. **Never squash or rebase from Replit** — Replit cannot force-push to GitHub. Do history cleanup from Antigravity only.
4. **Replit auto-creates checkpoint commits** ("Transitioned from Plan to Build mode", task completion commits). This is normal. Squash from Antigravity if cleaner history is desired, then sync Replit with step 2.
5. **Replit creates `subrepl-*` branches** for parallel task agents. These are temporary and can be deleted from Antigravity after tasks are merged.

---

## Current Status & Next Up

**Next Tasks:**
- **Sprint bug fix** — Client-side Mixin on `LocalPlayer.aiStep()` to properly cancel sprint when stamina is depleted.
- **HUD Polish** — Compass and Minimap.
- **Loot & crafting system** (Spec §5-6) — Custom loot tables, crafting recipes, and item progression.
- **Custom textures & models** — Replace scaled zombie renderers with proper custom models and textures for each variant.
- **World generation** — Custom biomes, structures, and POI generation per the spec.

---

## Pending Testing (March 12, 2026)

### Build Verification
- [ ] Run `./gradlew build` — confirm it compiles cleanly with zero errors

### Systems to Verify In-Game
- **Temperature:** [ ] Check 0.3°F/s adjustment rate and biomes shifts.
- **Debuffs:** [ ] Check Infection/Bleeding effects (Bleeding drains -1 HP/3s, Infection stages).
- **Horde Spawn Balance:** [ ] Trigger blood moon/horde wave, verify counts and zombie composition.
- **Heatmap Spawning:** 
  - [ ] Break blocks (+0.5 heat) and place torches (+2 heat).
  - [ ] Verify `/7dtm heat` reports correct values.
  - [ ] Heat 25+ (scout Walkers), 50+ (Screamer), 75+ (Mini-horde).
  - [ ] Verify `/7dtm heat_clear` resets heat and heat decays over time.
- **HUD Changes:** 
  - [ ] Verify vanilla hunger (drumsticks) and vanilla health (hearts) are hidden.
  - [ ] Confirm custom HUD shows Food, Water, Stamina, and HP correctly.
- **Edge Cases:** [ ] Spawn all zombie types (no crashes), test modifier variants, cross-system interactions (heatmap during blood moon).

---

## Known Bugs / Polish To Address

1. **SPRINT BUG — FIX ANOTHER DAY** (unresolved since Milestone 2):
   - Sprint can get stuck — holding W alone gives infinite sprint. Stamina drains but sprint doesn't cancel. Needs a client-side Mixin on `LocalPlayer.aiStep()`.
   - **Not on today's test list.** This is a known issue that requires a proper client-side fix.

2. **TODO — HUD polish**: Compass/minimap not yet started.

---

## Recent Completed Work (Milestones 3, 4, 5)

**Milestone 5 — Heatmap System (§1.3) [MERGED]**
- Full per-chunk heatmap system with decay over time.
- Triggers: Block break, torches, explosions, sprinting.
- Spawning: Scout Walkers (Heat 25+), Screamer (50+), Mini-horde (75+), continuous Wave Mode (100+).
- Added `/7dtm heat` and `/7dtm heat_clear` commands.

**Milestone 4 — Custom Zombie System (§3.1-3.2) [MERGED]**
- 18 custom zombie variants with configurable stats (HP, damage, speed, special abilities).
- Modifiers implemented: Radiated (heals), Charged (lightning), Infernal (fire).
- Bestiary guide added (`docs/zombie_guide.md`).
- Entities use `ScaledZombieRenderer` with custom bounding boxes and double-line name tags (Name + HP).

**Milestone 3 — Horde Night & Blood Moon System (§4.2) [MERGED]**
- Full timeline: warning day before, red sky 18:00, siren 18:30, horde 22:00, dawn 06:00.
- Sleep prevention integration.

**Misc Polish & UI [MERGED]**
- Vanilla hunger and health bars fully hidden.
- Custom HUD now displays Health, Food, Water, and Stamina.
- Post-merge Gradle verification script added.
