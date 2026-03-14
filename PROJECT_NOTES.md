# Project Notes — Brutal Zombie Horde Survival (BZHS)

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

**Completed Milestones:** 1 (Scaffold), 2 (Player Stats), 3 (Debuffs — all 12 types), 4 (Temperature — partial), 5 (Heatmap), 6 (Loot & Crafting), 7 (XP/Leveling/Perks), 8 (Blood Moon/Horde Night), 9 (HUD — compass, minimap, stats overlay).

**March 13–14 Completed Work:**
- Vanilla damage scaling to 100 HP — fall damage, drowning, fire, lava, cactus all proportionally scaled so 100 HP feels equivalent to vanilla 20 HP.
- 48,000-tick day cycle sky fix — sun/moon visual cycle now correctly completes one full rotation in 48,000 ticks.
- Darkness-based zombie speed — zombies speed up based on light level instead of tick-based day/night; replaces old tick-based night speed system.
- Coal vein nerf — reduced coal ore vein sizes to balance resource acquisition.
- Project rebranded to "Brutal Zombie Horde Survival" (BZHS) — mod ID, display name, and all user-facing references updated (#43).
- Landing page created and published, then upgraded to V2 with improved design (#40, #41, #45).
- `.gitignore` updated to exclude `public/` folder artifacts (#46).
- README.md status section restructured for clarity (#47).

**Next Tasks:**
- **Sprint bug fix** — Client-side Mixin on `LocalPlayer.aiStep()` to properly cancel sprint when stamina is depleted.
- **Custom textures & models** — Replace scaled zombie renderers with proper custom models and textures for each variant.
- **World generation** — Custom biomes, structures, and POI generation per the spec.

---

## Known Bugs / Polish To Address

1. **SPRINT BUG — FIX ANOTHER DAY** (unresolved since Milestone 2):
   - Sprint can get stuck — holding W alone gives infinite sprint. Stamina drains but sprint doesn't cancel. Needs a client-side Mixin on `LocalPlayer.aiStep()`.
   - **Not on today's test list.** This is a known issue that requires a proper client-side fix.

2. **F3 DEBUG SCREEN DAY COUNTER** (known cosmetic discrepancy):
   - The F3 debug screen's "Day" counter uses vanilla's hardcoded `24000L` division inside `DebugScreenOverlay`, which is a deeply embedded formatted string render. Overriding it cleanly via Mixin would be fragile across MC updates.
   - The mod HUD (`StatsHudOverlay`) already displays the correct day number based on the 48,000-tick cycle. Use the mod HUD as the authoritative day counter.
   - The F3 day counter will show approximately double the actual day number (since vanilla thinks each 24k ticks = 1 day, but our cycle is 48k ticks).

---

## Next Session — Debug & Test Checklist (March 15)

- **P1 (game-breaking):** Does the mod build and launch without crashes? Do all 18 zombie types spawn without errors?
- **P2 (core — darkness speed):** Darkness-based zombie speed — do zombies speed up in dark caves during daytime? Do torches slow them down? Coal vein sizes reduced?
- **P3 (core — day cycle):** 48,000-tick day cycle — does the sun/moon complete one full visual cycle in 48,000 ticks? Note: F3 day counter uses vanilla 24k logic and will differ from HUD — HUD is authoritative.
- **P4 (core — damage scaling):** Vanilla damage scaling — does fall damage, drowning, fire, lava, cactus feel proportional to 100 HP (same danger as vanilla 20 HP)?
- **P5 (HUD):** Compass at top-center showing cardinal directions? Minimap in top-right showing terrain + player dots? Stats bars (HP/Food/Water/Stamina) rendering without overlap?
- **P6 (heatmap):** Mining/torches/sprinting raise chunk heat? Scouts at 25, Screamer at 50, mini-horde at 75, waves at 100?
- **P7 (combat):** Zombie name tags + HP bars hidden behind walls? Sunlight doesn't burn BZHS zombies?
- **P8 (survival stats):** Sprint bug still present (known, deferred)? Stamina drain/regen rates feel correct? Food/water drain working?
- **P9 (blood moon):** Every 7 days, warning → red sky → siren → horde → dawn burn sequence works?
- **P10 (debuffs):** Bleeding/Infection/Fracture/etc. apply and clear correctly? `/7dtm cleardebuffs` works?
- **P11 (landing page):** Verify published landing page loads correctly and links work.

---

## Recent Completed Work

**March 14 Session**
- Project rebranded to "Brutal Zombie Horde Survival" (BZHS) — mod ID, display name, and all user-facing references updated (#43)
- Landing page created and published (#40, #41)
- Landing page upgraded to V2 with improved layout and design (#45)
- `.gitignore` updated to exclude `public/` folder build artifacts (#46)
- README.md status section restructured with milestone tracking table (#47)
- PROJECT_NOTES.md updated with March 14 status (#48)

**March 13 Session**
- Vanilla damage scaling to 100 HP (fall, drowning, fire, lava, cactus proportionally scaled)
- 48,000-tick day cycle sky fix (sun/moon visual rotation corrected)
- Darkness-based zombie speed (light-level-based, replaces tick-based night speed)
- Coal vein nerf (reduced ore vein sizes)

**March 12 Late-Session Work [MERGED]**
- Zombie name tag occlusion fix (name tags and HP bars not visible through walls)
- HUD compass and minimap with player tracking
- Sunlight burning disabled for all BZHS zombies
- Zombie behavior summary added to `docs/zombie_guide.md`
- Loot and crafting system (Milestone 6) — loot tables, crafting recipes, item progression tiers
- XP, leveling & perk system — kill XP, level-up notifications, perk unlocks
- Debuffs system — Bleeding, Infection, Dysentery, Sprain, Fracture with triggers and cures
- Stats HUD overlap fix (moved down below compass, removed background)
- Night zombie speed increased to 2.25x
- Day cycle doubled to 48,000 ticks
- Debuffs persistence bug fixed (twice — `/7dtm cleardebuffs` command + `copyOnDeath` removal)
- Debuffs guide created (`docs/debuffs_guide.md`)
- Player base health set to 100 HP
