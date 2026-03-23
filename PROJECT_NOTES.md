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

**Completed Milestones:** 1 (Scaffold), 2 (Player Stats), 3 (Debuffs — all 12 types), 4 (Temperature — partial), 5 (Heatmap), 6 (Loot & Crafting), 7 (XP/Leveling/Perks), 8 (Blood Moon/Horde Night), 9 (HUD — compass, minimap, stats overlay), 10 (Weapons), 11 (Skill Books/Magazines), 14 (Armor), 15 (World Gen — Biomes + Overworld), 17 (Building + Traps), 18 (Loot Tables), 19 (Traders + Quests), 21 (Electricity), 22 (Farming), 24 (Stealth), 25 (Inventory UI), 26 (Map). Plus: Village settlements, campfire merge, water bottle consolidation, premade worlds.

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
- Village overhaul (#129) — village settlement system with 8 building types (Abandoned House, Crack-a-Book, Working Stiffs, Pass-n-Gas, Pop-n-Pills, Farm, Utility, Trader Outpost), sleeper zombie spawning, per-building loot containers
- Campfire merge (#130) — campfire workstation merged into vanilla campfire block; right-clicking a lit vanilla campfire opens the BZHS crafting UI
- Water bottle consolidation (#131) — Glass Jars removed; vanilla water bottles auto-convert to Murky Water via `WaterBottleConversionHandler`; purify at campfire
- AK-47 fix (#132) — AK-47 weapon bug resolved
- World type selection (#125) — Generated vs Premade world types on Create World screen
- Spawn protection (#126) — distance-based difficulty scaling
- Debuff overhaul (#124) — debuffs updated to match 7 Days to Die values

**March 21 Completed Work (Tasks #134–#143):**
- Vending machine blocks (#134–#135) — two-block-tall textured vending machines with wall-only placement
- Big map screen (#136) — M key opens full map with territory labels and markers
- AK-47 texture fix (#137) — gun texture and controls improved
- Rebuild and deploy JAR (#138)
- Territory labels on map and minimap (#139) — territories visible on all map views
- HUD cleanup (#140) — removed 'Difficulty:' label text
- Territory entry announcement animation (#141) — HUD animation when entering a territory
- AK-47 bullet speed and range boost (#142)
- Fix AK-47 and Pistol 9mm 3D model rendering (#143)

**March 22 Completed Work:**
- Smart A* pathfinding (#144) — block-break cost weighting for zombie pathfinding
- Stealth and detection system (#145) — Unaware → Suspicious → Alert states with visual indicators
- Coordinated block bashing (#146) — 3+ zombies stack damage on same block
- Blood Moon atmosphere effects (#147) — red fog, camera shake during horde night
- Smell tracking system (#148) — config-gated 3.0 preview feature
- Complete Trader NPC system (#150) — Joel, Rekt, Jen, Hugh, Bob with specialty stock, buy/sell GUI, Better Barter pricing, Secret Stash, night closure
- Quest system (#151) — 4 quest types (Kill Count, Clear Territory, Fetch & Deliver, Buried Treasure), HUD overlay, map markers
- Overworld biome placement (#165) — BZHS Apocalypse world preset with custom surface blocks per biome
- Trader economy (#166) — specialty stock, Better Barter perk integration
- Priority texture replacement (#167) — weapons, ammo, HUD textures updated
- Building system (#170) — upgradeable blocks (6-tier), wood/iron spikes, blade trap, electric fence, land claim block
- Armor system (#171) — 3 tiers (Light/Padded, Medium/Scrap Iron, Heavy/Military) with set bonuses
- Farming system (#172) — crops, farm plots, dew collector, seed items
- Power/electricity system (part of building #170) — generator, battery, solar panel, wire connections
- Vehicle wreckage blocks (#163) — decorative blocks that drop scrap
- Multiple bug fixes (#152–#164) — map arrow, textures, structure placement, grenade, water bottles

**March 23 (Tasks #209, #210, #213):**
- Territory HUD (#209) — shows territory & building names when inside a POI
- Trader compounds as safe zones (#210) — no zombie spawns within trader compound
- Full attribute names in inventory (#213) — shows Strength/Perception/etc. instead of STR/PER

**Current Focus / In Progress:**
- Building overlap fix (#214) — preventing overlapping structures during world generation

**Next Up:**
- Custom textures — replace 349 placeholder textures with real pixel art (prioritize HUD icons, weapons, workstations)
- Full world generation pipeline (city grid, POI templates)
- Vehicle system
- Multiplayer sync & balancing
- Performance optimization

---

## Known Bugs / Polish To Address

1. **SPRINT BUG — FIXED** (Task #106):
   - Sprint rubber-banding resolved via client-side `LocalPlayerSprintMixin` on `LocalPlayer.aiStep()`.
   - Registered under `"client"` key in `sevendaystominecraft.mixins.json`.

2. **F3 DEBUG SCREEN DAY COUNTER** (likely resolved):
   - After the slower-tick refactor (#60), `dayTime` stays on a vanilla 24k scale. F3 day counter should now match the HUD. Verify during next test session.

3. **PLACEHOLDER TEXTURES (349 of 388)** — Most item, GUI, and some block textures are auto-generated colored squares. Gameplay is functional but visually unpolished. Full list in `docs/texture_audit.md`.

4. **BUILDING OVERLAP (#214 — In Progress)** — Structures can sometimes overlap during world generation. Fix in progress.

---

## Recent Completed Work

**March 23 (Tasks #209, #210, #213, #215)**
- Territory HUD (#209) — shows territory & building names when entering POI
- Trader compounds as safe zones (#210) — suppresses zombie spawns in trader compounds
- Full attribute names (#213) — inventory shows Strength/Perception/etc. instead of abbreviations
- Full documentation & website update (#215) — all docs, guides, README, website pages updated

**March 22 Session (Tasks #144–#174)**
- Smart A* pathfinding (#144) — block-break cost for zombie pathing
- Stealth and detection system (#145) — Unaware → Suspicious → Alert
- Coordinated block bashing (#146) — 3+ zombies stack damage
- Blood Moon atmosphere (#147) — red fog, camera shake
- Smell tracking (#148) — config-gated preview
- Complete Trader NPC system (#150) — 5 traders with shop GUI
- Quest system (#151) — 4 quest types with HUD/map integration
- Overworld biome placement (#165) — BZHS Apocalypse world preset
- Building system (#170) — upgradeable blocks, spikes, traps, land claim
- Armor system (#171) — 3 tiers with set bonuses
- Farming system (#172) — crops, farm plots, dew collector
- Multiple bug fixes and texture updates (#152–#168)

**March 21 Session (Tasks #134–#143)**
- Vending machines (#134–#135) — two-block-tall with wall placement
- Big map screen (#136) — M key full map
- Territory labels on map (#139) — visible on all views
- Territory entry announcement (#141) — HUD animation
- AK-47 improvements (#137, #142, #143)

**March 19–20 Session (Tasks #115–#133)**
- Debuff overhaul (#124) — updated to 7DTD values
- World type selection (#125) — premade world support
- Spawn protection (#126) — distance-based difficulty
- Village overhaul (#129) — 8 building types with sleeper zombies
- Campfire merge (#130) — merged into vanilla campfire block
- Water bottle consolidation (#131) — Glass Jars replaced with vanilla water bottle conversion
- AK-47 fix (#132) — weapon bug resolved
- Multiple HUD and texture fixes (#115–#123)

**March 18 Session (Tasks #112–#114)**
- Perk icon renames (#112) — texture filenames updated to match renamed perk IDs
- Registry crash fix (#113) — fixed startup crash from stale registry references after trademark sweep

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
