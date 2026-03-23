# PROJECT_SUMMARY — Brutal Zombie Horde Survival (BZHS)

**Minecraft 1.21.4 | NeoForge 21.4.140 | Mod ID: `sevendaystominecraft`**

A total conversion mod inspired by 7 Days to Die 2.6. One full JAR, no dependencies.

---

## What's Implemented

- **Survival stats** — Food, Water, Stamina, Temperature replacing vanilla hunger/regen
- **Icon HUD** — Heart, food, water, armor icon rows; compass strip, minimap, day counter, XP bar, debuff display
- **Custom inventory screen** — BzhsInventoryScreen replacing vanilla (stats/perks/debuffs/armor set bonus panels)
- **Territory HUD** — Shows territory & building names when inside a POI
- **Full attribute names** — Inventory shows Strength/Perception/etc. instead of STR/PER
- **Big map screen** — M key opens full map with territory labels, trader markers, quest markers
- **12 debuffs** — Bleeding, Infection 1/2, Sprain, Fracture, Concussion, Electrocuted, Stunned, Burn, Dysentery, Hypothermia, Hyperthermia, Radiation
- **20 zombie variants** — Walker, Crawler, Soldier, Frostbitten Woodsman, Bloated Shambler, Wall Creeper, Feral Wraith, Banshee, Riot Husk, Wrecking Husk, Nurse, Mutated Brute, Behemoth, Charged, Infernal, Zombie Dog, Zombie Bear, Vulture, Zombie Bird, Zombie Parrot
- **Zombie AI** — Layered priority behavior tree; block breaking, heatmap investigation, horde pathfinding, variant-specific special abilities, smart A* pathfinding, coordinated block bashing, stealth detection system
- **Blood moon / horde night** — Every 7th night, warning → red sky → siren → waves → dawn cleanup; 5 difficulty tiers; red fog and camera shake atmosphere effects
- **Heatmap system** — Player actions generate heat; Scout (25+), Banshee (50+), mini-horde (75+), wave mode (100)
- **Basic weapons** — 11 melee + 15 ranged weapons, quality-scaled damage and ammo consumption
- **7 workstations** — Campfire, Grill, Workbench, Forge, Cement Mixer, Chemistry Station, Advanced Workbench; all with functional GUIs and fuel-based recipe processing
- **8 loot containers** — level-scaled loot, 6 quality tiers (Poor → Legendary)
- **17 materials + Survivor's Coins** — full material economy
- **XP, leveling, 45 perks** — 5 attribute trees, kill/mine XP, perk unlocks
- **Extended day cycle** — TIME_SCALE=2 slower-tick; one day = ~40 real minutes, vanilla 24k dayTime preserved
- **20 HP vanilla player base** — zombie stats proportionally balanced for vanilla 20 HP scale
- **Sound system** — 8 custom sound events with gated playback and subtitles
- **Context-aware gameplay music** — day/night/combat/blood moon tracks with crossfading
- **Territory POIs** — star-rated points of interest with procedural structures
- **3D weapon animations** — GeckoLib-powered AK-47, 9mm Pistol, Grenade with full animations; bundled via Jar-in-Jar
- **Sprint fix** — client-side `LocalPlayer.aiStep()` Mixin prevents rubber-banding
- **Skill books / magazines** — 6 series (36 items), per-issue bonuses, series mastery tracking
- **Custom biomes** — 7 biome definitions with per-biome temperature ranges, zombie density multipliers, loot tier bonuses
- **Overworld biome placement** — BZHS Apocalypse world preset with multi-noise source, custom surface rules per biome
- **Premade world system** — bundled world templates, Create World screen integration
- **Trademark name sweep** — zombie display names, perk IDs, and currency renamed to avoid trademark conflicts
- **Landing page** — published with GitHub Releases download button and Ko-fi/Patreon funding page
- **Trader NPC system** — TraderEntity NPCs (Joel, Rekt, Jen, Hugh, Bob) with specialty stock, buy/sell GUI, Better Barter perk pricing, Secret Stash, night closure, trader compounds as safe zones (no zombie spawns)
- **Quest system** — 4 quest types (Kill Count, Clear Territory, Fetch & Deliver, Buried Treasure), HUD overlay, map markers, NBT persistence, trader-integrated UI
- **Power/electricity system** — GeneratorBank (fuel-powered, 100W), BatteryBank (1000 EU storage), SolarPanel (30W daytime), PowerGridManager, wire connections via Electrical Parts
- **Building & defense blocks** — UpgradeableBlock (6-tier: Wood Frame→Steel), WoodSpikes (4 dmg), IronSpikes (8 dmg), BladeTrap (6 dmg AoE, powered), ElectricFencePost (5 dmg + stun, powered), LandClaimBlock (41-block protection radius)
- **Farming system** — CropBlock (4 growth stages), FarmPlotBlock (tilled soil), DewCollector (passive water gen), SeedItem, FarmPlotInteractionHandler
- **Armor system** — 3 armor tiers (Light/Padded, Medium/Scrap Iron, Heavy/Military), TieredArmorItem with movement/stealth modifiers, ArmorSetBonusHandler (2pc partial + 4pc full set bonuses)
- **Stealth/noise system** — NoiseManager (per-player noise tracking with 5s decay), NoiseEventHandler (movement, block break, gunshots)
- **Village settlements** — 8 building types with sleeper zombies, loot containers, difficulty scaling
- **Vehicle wreckage blocks** — decorative blocks (burnt car, broken truck, wrecked camper) that drop scrap

---

## In Progress

- **Building overlap fix** (Task #214) — preventing overlapping structures during world generation

---

## What's Next

- Replace 349 placeholder textures (full audit in `docs/texture_audit.md`; HUD icons, weapons, workstations highest priority)
- Full world generation pipeline (city grid, POI templates)
- Vehicle system
- Multiplayer sync & balancing
- Performance optimization

---

## Build

```bash
./gradlew build --no-daemon
```

Output: `build/libs/BrutalZombieHordeSurvival-0.1.0-alpha.jar` → drop into `mods/` (NeoForge 21.4.140, Minecraft 1.21.4, Java 21)
