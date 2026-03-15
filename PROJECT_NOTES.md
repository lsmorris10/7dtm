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

**March 13–15 Completed Work:**
- Vanilla damage scaling to 100 HP — fall damage, drowning, fire, lava, cactus all proportionally scaled so 100 HP feels equivalent to vanilla 20 HP.
- Day cycle doubled via slower-tick refactor (TIME_SCALE=2) — game time advances 1 tick per 2 server ticks, keeping vanilla 24k dayTime scale intact. Sky rendering, F3 day counter, and all vanilla time logic work natively.
- Darkness-based zombie speed — zombies speed up based on light level instead of tick-based day/night; replaces old tick-based night speed system.
- Coal vein nerf — reduced coal ore vein sizes to balance resource acquisition.
- Project rebranded to "Brutal Zombie Horde Survival" (BZHS) — mod ID, display name, and all user-facing references updated (#43).
- Landing page created and published, then upgraded to V2 with improved design (#40, #41, #45).
- `.gitignore` updated to exclude `public/` folder artifacts (#46).
- README.md status section restructured for clarity (#47).
- Download button upgraded to fetch latest JAR directly from GitHub Releases API (#55, #56, #58).
- Mod JAR and metadata renamed to match BZHS branding (#57).
- LevelTimeOfDayMixin crash fix — target corrected from `getTimeOfDay` to `getSunAngle` (#59).
- Day cycle refactored to slower-tick approach — vanilla `setDayTime` incremented at 0.5 ticks/server tick, eliminating need for custom sky rendering (#60).
- Vanilla mob damage scaling extended to all vanilla mobs — all mob attack damage proportionally scaled for 100 HP balance (#61).

**Next Tasks:**
- **Sprint bug fix** — Client-side Mixin on `LocalPlayer.aiStep()` to properly cancel sprint when stamina is depleted.
- **Custom textures & models** — Replace scaled zombie renderers with proper custom models and textures for each variant.
- **World generation** — Custom biomes, structures, and POI generation per the spec.

---

## Known Bugs / Polish To Address

1. **SPRINT BUG — FIX ANOTHER DAY** (unresolved since Milestone 2):
   - Sprint can get stuck — holding W alone gives infinite sprint. Stamina drains but sprint doesn't cancel. Needs a client-side Mixin on `LocalPlayer.aiStep()`.
   - **Not on today's test list.** This is a known issue that requires a proper client-side fix.

2. **F3 DEBUG SCREEN DAY COUNTER** (likely resolved):
   - Previously showed ~2x the actual day when the day cycle used a raw 48k-tick `dayTime`. After the slower-tick refactor (#60), `dayTime` stays on a vanilla 24k scale (time advances 1 tick every 2 server ticks via TIME_SCALE=2). F3 day counter should now match the HUD. Verify during next test session.

---

## Next Session — Debug & Test Checklist (March 16+)

- **P1 (game-breaking):** Does the mod build and launch without crashes? Verify LevelTimeOfDayMixin crash fix (#59) — no more startup crash on `getSunAngle`.
- **P2 (core — day cycle):** Slower-tick day cycle (TIME_SCALE=2) — game time advances 1 tick every 2 server ticks, so one full day takes ~40 real minutes instead of ~20. Verify: sun/moon visual cycle uses vanilla 24k rendering naturally (no custom sky code). F3 day counter should now be correct (dayTime is still 24k-based). HUD day counter should also match.
- **P3 (core — mob damage):** Vanilla mob damage scaling — do all vanilla mob attacks (zombies, skeletons, spiders, creeper explosions, etc.) feel proportional to 100 HP? Environmental damage (fall, drowning, fire, lava, cactus) still correct?
- **P4 (core — darkness speed):** Darkness-based zombie speed — do zombies speed up in dark caves during daytime? Do torches slow them down?
- **P5 (HUD):** Compass at top-center showing cardinal directions? Minimap in top-right showing terrain + player dots? Stats bars (HP/Food/Water/Stamina) rendering without overlap?
- **P6 (heatmap):** Mining/torches/sprinting raise chunk heat? Scouts at 25, Screamer at 50, mini-horde at 75, waves at 100?
- **P7 (combat):** Zombie name tags + HP bars hidden behind walls? Sunlight doesn't burn BZHS zombies?
- **P8 (survival stats):** Sprint bug still present (known, deferred)? Stamina drain/regen rates feel correct? Food/water drain working?
- **P9 (blood moon):** Every 7 days, warning → red sky → siren → horde → dawn burn sequence works? Blood moon timing correct with slower-tick cycle?
- **P10 (debuffs):** Bleeding/Infection/Fracture/etc. apply and clear correctly? `/bzhs cleardebuffs` works?
- **P11 (landing page):** Verify published landing page loads correctly, download button fetches latest release JAR, and links work.

---

## Recent Completed Work

**March 14–15 Session**
- Download button upgraded to fetch latest JAR from GitHub Releases API (#55)
- Download button updated to support pre-releases (#56)
- Mod JAR and metadata renamed to match BZHS branding (#57)
- One-line install tutorial added above download button (#58)
- LevelTimeOfDayMixin crash fix — target corrected from `getTimeOfDay` to `getSunAngle` (#59)
- Day cycle refactored to slower-tick approach — 0.5 ticks/server tick via `setDayTime`, no custom sky rendering (#60)
- Vanilla mob damage scaling extended to all vanilla mobs for 100 HP balance (#61)

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
- Debuffs persistence bug fixed (twice — `/bzhs cleardebuffs` command + `copyOnDeath` removal)
- Debuffs guide created (`docs/debuffs_guide.md`)
- Player base health set to 100 HP
