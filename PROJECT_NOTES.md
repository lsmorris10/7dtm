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

**Completed Milestones:** 1 (Scaffold), 2 (Player Stats), 3 (Debuffs — all 12 types), 4 (Temperature — partial), 5 (Heatmap), 6 (Loot & Crafting), 7 (XP/Leveling/Perks), 8 (Blood Moon/Horde Night), 9 (HUD — compass, minimap, stats overlay). Plus: Village settlements, campfire merge, water bottle consolidation.

**March 16–17 Major Completed Work:**
- Zombie AI behavior tree refactored (#72) — fully layered, priority-ordered, conditions-checked AI system
- Gameplay bugfixes (#76) — assorted client/server crashes and balance corrections
- Textures, models, and blockstates added and organized (#77)
- New world startup fixes — mod now correctly initializes a fresh world without errors (#78)
- Zombie block breaking AI fix (#79) — path-to-target correctly triggers block break behavior
- Minimap fix (#80) — terrain rendering and player tracking corrected
- HP display fix (#81) — health readout now accurate at all HP values
- Health and combat rebalance (#82, #83) — zombie HP/damage tuned, player survivability improved
- Item texture fixes (#84) — missing/broken item textures resolved
- Container GUI fixes (#85) — workstation and loot container GUIs stable
- Language file updates (#89) — all new items, blocks, and UI strings localized
- Deprecated API fixes (#91) — NeoForge API call sites updated to current 1.21.4 signatures
- Legacy config cleanup (#92) — stale config keys removed, config files reorganized
- Zombie AI special abilities (#93) — block breaking, heatmap investigation trigger, horde pathfinding, variant-specific abilities (acid spit, charge, ground pound, etc.)
- Workstation recipe processing (#94) — all 7 workstations now process recipes with correct fuel logic and output
- Basic weapons system (#95) — melee and ranged weapons implemented with damage, range, and attack speed
- Placeholder texture audit (#97) — full audit run; 349 of 388 textures flagged as placeholder (report at `docs/texture_audit.md`)
- Sound system foundation (#98) — 8 custom sound events, gated playback, subtitles
- Icon-based HUD (#100) — stat bars replaced with icon rows (hearts, food, water, armor icons)
- Texture processing tool (#101) — batch tool for generating and validating texture assets
- Territory POI system (#102) — star-rated points of interest with procedural structures
- 3D weapon animations (#103) via GeckoLib — AK-47, 9mm Pistol, Grenade with full animations
- Funding page (#104) — Behind the Build support page added to landing site
- GeckoLib Jar-in-Jar (#105) — single-file mod distribution, no external dependency needed
- Sprint Mixin fix (#106) — client-side `LocalPlayer.aiStep()` Mixin prevents rubber-banding
- Context-aware gameplay music (#107) — day/night/combat/blood moon tracks with crossfading
- Magazine / Skill Book system (#108–#109) — 6 series (Steady Steve, Block Brawler, Sharpshot Sam, The Tinkerer, Overworld Chef, Dungeon Tactician), 36 items, per-issue bonuses, series mastery tracking, Minecraft-ified names
- Custom biome system (#110) — 7 biomes (Pine Forest, Forest, Plains, Desert, Snowy Tundra, Burned Forest, Wasteland) with per-biome temperature ranges, zombie density multipliers, loot tier bonuses; integrated into PlayerStatsHandler, LootStageCalculator, TerritoryZombieSpawner
- Trademark name sweep (#111) — Duke's Casino Token → Survivor's Coin; 8 zombie display names renamed (Feral Wight→Feral Wraith, Frozen Lumberjack→Frostbitten Woodsman, Cop→Riot Husk, Screamer→Banshee, Demolisher→Wrecking Husk, Mutated Chuck→Mutated Brute, Spider Zombie→Wall Creeper, Bloated Walker→Bloated Shambler); 12 perk IDs+names renamed

**March 18 Completed Work:**
- Perk icon renames (#117) — perk icon texture filenames updated to match renamed perk IDs from trademark sweep
- Registry crash fix (#118) — fixed startup crash caused by stale registry references after the trademark name sweep

**March 19–20 Completed Work:**
- Village overhaul (#135) — village settlement system with 8 building types (Abandoned House, Crack-a-Book, Working Stiffs, Pass-n-Gas, Pop-n-Pills, Farm, Utility, Trader Outpost), sleeper zombie spawning, per-building loot containers
- Campfire merge (#136) — campfire workstation merged into vanilla campfire block; right-clicking a lit vanilla campfire opens the BZHS crafting UI
- Water bottle consolidation (#137) — Glass Jars removed; vanilla water bottles auto-convert to Murky Water via `WaterBottleConversionHandler`; purify at campfire
- AK-47 fix (#138) — AK-47 weapon bug resolved

**Current Focus / In Progress:**
- Overworld biome placement — surface builder / noise router for custom biome definitions (definitions exist, placement pending)

**Next Up:**
- Custom textures — replace 349 placeholder textures with real pixel art (prioritize HUD icons, weapons, workstations)
- Full world generation pipeline (city grid, POI templates)
- Trader NPCs and quest system
- Vehicle system

---

## Known Bugs / Polish To Address

1. **SPRINT BUG — FIXED** (Task #106):
   - Sprint rubber-banding resolved via client-side `LocalPlayerSprintMixin` on `LocalPlayer.aiStep()`.
   - Registered under `"client"` key in `sevendaystominecraft.mixins.json`.

2. **F3 DEBUG SCREEN DAY COUNTER** (likely resolved):
   - After the slower-tick refactor (#60), `dayTime` stays on a vanilla 24k scale. F3 day counter should now match the HUD. Verify during next test session.

3. **PLACEHOLDER TEXTURES (349 of 388)** — Most item, GUI, and some block textures are auto-generated colored squares. Gameplay is functional but visually unpolished. Full list in `docs/texture_audit.md`.

---

## Next Session — Debug & Test Checklist (March 20+)

- **P1 — Build & Launch:** Does the mod build cleanly? Any Mixin or registry errors on startup? (Registry crash fix #118 should resolve previous startup issues.)
- **P2 — Basic Weapons (melee):** Stone Axe, Wooden Club, Baseball Bat, Sledgehammer — do they deal correct damage with quality scaling? Attack speed correct?
- **P3 — Basic Weapons (ranged):** Pipe Pistol, Primitive Bow — do they fire? Ammo consumption correct? Hit detection working?
- **P4 — Workstation Recipe Processing:** Campfire, Forge, Workbench — do recipes process with correct fuel consumption and output? Does Forge smelting take the right time?
- **P5 — Zombie AI Special Abilities:** Riot Husk acid spit, Wrecking Husk ground pound, Wall Creeper climbing, Charged chain lightning — do variant abilities trigger correctly? Does block breaking AI activate on targeted blocks?
- **P6 — Icon-Based HUD:** Hearts, food, water, armor shown as icon rows (not stat bars)? Icons update correctly as values change? No overlap with compass or minimap?
- **P7 — Heatmap + Zombie Investigation:** Does horde pathfinding respond to heat? Do zombies investigate high-heat areas?
- **P8 — Container GUIs:** Open all workstation and loot container GUIs — do they render correctly and accept/process items?
- **P9 — World Startup:** Does a fresh new world generate and load without errors or crashes?
- **P10 — Blood Moon:** Every 7th night still triggers correctly with the rebalanced zombie HP/damage?
- **P11 — Territory POIs:** Do POI structures spawn in expected locations and biomes?
- **P12 — Sound & Music:** Do zombie sounds, combat sounds, ambient sounds, and context-aware music tracks play correctly? Day/night/combat/blood moon crossfading working?
- **P13 — Geckolib Animations:** Do animated weapon models (AK-47, 9mm Pistol, Grenade) render without errors?
- **P14 — Sprint Fix:** Sprint rubber-banding resolved? (Fixed in #106 via LocalPlayerSprintMixin.)
- **P15 — Skill Books:** Do magazine/skill book items drop, read correctly, and grant per-issue bonuses? Series mastery tracking working?
- **P16 — Custom Biomes:** Do biome temperature ranges, zombie density multipliers, and loot tier bonuses apply correctly?
- **P17 — Perk Icons:** Do renamed perk icon textures load and display correctly in the perk UI? (Fixed in #117.)
- **P18 — Landing Page + Funding:** Funding page loads? Ko-fi/Patreon links work?

---

## Recent Completed Work

**March 19–20 Session (Tasks #135–#139)**
- Village overhaul (#135) — village settlement system with 8 building types and sleeper zombies
- Campfire merge (#136) — campfire workstation merged into vanilla campfire block
- Water bottle consolidation (#137) — Glass Jars replaced with vanilla water bottle → Murky Water auto-conversion
- AK-47 fix (#138) — AK-47 weapon bug resolved
- Docs update + zombie guide (#139) — all docs updated with renamed zombie names, zombie_guide.md created, campfire/water bottle changes reflected in crafting_guide.md, JAR rebuilt

**March 18 Session (Tasks #117–#118)**
- Perk icon renames (#117) — texture filenames updated to match renamed perk IDs
- Registry crash fix (#118) — fixed startup crash from stale registry references after trademark sweep

**March 16–17 Session (Tasks #72–#111)**
- Zombie AI behavior tree refactored (#72)
- Gameplay bugfixes (#76)
- Textures/models/blockstates (#77)
- New world startup fixes (#78)
- Zombie block breaking AI fix (#79)
- Minimap fix (#80)
- HP display fix (#81)
- Health and combat rebalance (#82, #83)
- Item texture fixes (#84)
- Container GUI fixes (#85)
- Language file updates (#89)
- Deprecated API fixes (#91)
- Legacy config cleanup (#92)
- Zombie AI special abilities (#93)
- Workstation recipe processing (#94)
- Basic weapons system (#95)
- Placeholder texture audit (#97) — `docs/texture_audit.md`
- Sound system foundation (#98)
- Icon-based HUD (#100)
- Texture processing tool (#101)
- Territory POI system (#102)
- 3D weapon animations via GeckoLib (#103)
- Funding page (#104)
- GeckoLib Jar-in-Jar (#105)
- Sprint Mixin fix (#106)
- Context-aware gameplay music (#107)
- Magazine / Skill Book system (#108–#109)
- Custom biome system (#110)
- Trademark name sweep (#111)

**March 14–15 Session**
- Download button upgraded to fetch latest JAR from GitHub Releases API (#55)
- Download button updated to support pre-releases (#56)
- Mod JAR and metadata renamed to match BZHS branding (#57)
- One-line install tutorial added above download button (#58)
- LevelTimeOfDayMixin crash fix — target corrected from `getTimeOfDay` to `getSunAngle` (#59)
- Day cycle refactored to slower-tick approach — 0.5 ticks/server tick via `setDayTime`, no custom sky rendering (#60)
- Vanilla mob damage scaling extended to all vanilla mobs (#61)

**March 14 Session**
- Project rebranded to "Brutal Zombie Horde Survival" (BZHS) — mod ID, display name, and all user-facing references updated (#43)
- Landing page created and published (#40, #41)
- Landing page upgraded to V2 with improved layout and design (#45)
- `.gitignore` updated to exclude `public/` folder build artifacts (#46)
- README.md status section restructured with milestone tracking table (#47)
- PROJECT_NOTES.md updated with March 14 status (#48)

**March 13 Session**
- Vanilla damage scaling (fall, drowning, fire, lava, cactus proportionally scaled)
- 48,000-tick day cycle sky fix — **superseded by slower-tick refactor (#60)**
- Darkness-based zombie speed — **later expanded to dual system: night dayTime check + darkness light-level check (#66)**
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
- Day cycle doubled to 48,000 ticks — **superseded by slower-tick refactor (#60)**
- Debuffs persistence bug fixed (twice — `/bzhs cleardebuffs` command + `copyOnDeath` removal)
- Debuffs guide created (`docs/debuffs_guide.md`)
- Player base health set to vanilla 20 HP
