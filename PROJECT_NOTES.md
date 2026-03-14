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

**Completed Milestones:** 1 (Scaffold), 2 (Player Stats), 3 (Debuffs — all 12 types), 4 (Temperature — partial), 5 (Heatmap), 6 (Loot & Crafting), 7 (XP/Leveling/Perks), 8 (Blood Moon/Horde Night), 9 (HUD — compass, minimap, stats overlay).

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

## Recent Completed Work

**March 12 Late-Session Work [MERGED]**
- Zombie name tag occlusion fix (name tags and HP bars not visible through walls)
- HUD compass and minimap with player tracking
- Sunlight burning disabled for all 7DTM zombies
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
