# Brutal Zombie Horde Survival

> **Disclaimer:** This mod is unofficial fan-made content inspired by 7 Days to Die and is not affiliated with, endorsed by, or connected to The Fun Pimps in any way.

A total conversion mod for **Minecraft 1.21.4** (NeoForge) inspired by **7 Days to Die** survival gameplay. Aligned to the style of 7 Days to Die 2.6 Experimental (Feb 2026).

| | |
|---|---|
| **Mod ID** | `sevendaystominecraft` |
| **Loader** | NeoForge 21.4.140 |
| **Minecraft** | 1.21.4 |
| **Java** | 21 |
| **Dependencies** | None (single JAR) |

---

## Features

### Survival Stats & HUD
- Custom **Food**, **Water**, **Stamina**, and **Temperature** systems replacing vanilla hunger/regen
- **Icon-based HUD** — hearts, food, water, and armor rendered as icon rows (replaces old stat bars)
- 360° **compass strip** at top-center with cardinal/intercardinal markers and heat level indicator
- **Minimap** in top-right corner with terrain colors, player dot, and nearby player tracking
- Day counter, temperature readout, debuff display, and XP bar
- Player base health stays at vanilla **20 HP** (10 hearts) for balanced combat against custom zombies
- Vanilla hunger bar, health hearts, and passive regen disabled via Mixins

### Debuffs (12 Types)
- **Bleeding** — Zombie hits (30% chance), stacks up to 3x, damage scales with stacks
- **Infection Stage 1/2** — Zombie hits (10% chance), stamina penalty → lethal drain if untreated
- **Sprain** — Fall 4-7 blocks, -30% movement speed for 30 minutes
- **Fracture** — Fall 8+ blocks, -60% movement speed + sprint blocked for 60 minutes
- **Concussion** — Explosion within 3 blocks, nausea/screen wobble for 45 seconds
- **Electrocuted** — Charged zombie hit, 1.5 second movement freeze
- **Stunned** — Riot Husk bile or Riot Husk/Wrecking Husk explosion, 2 second movement freeze
- **Burn, Dysentery, Hypothermia, Hyperthermia, Radiation** — Various environmental triggers
- All debuffs clear on death

### Zombie System (20 Variants + 3 Modifiers)
- **Humanoid (15)**: Walker, Crawler, Soldier, Frostbitten Woodsman, Bloated Shambler, Wall Creeper, Feral Wraith, Banshee, Riot Husk, Wrecking Husk, Nurse, Mutated Brute, Behemoth, Charged, Infernal
- **Animals (5)**: Zombie Dog, Zombie Bear, Vulture, Zombie Bird, Zombie Parrot
- **Special Mechanics**: Explosions, acid spit projectiles, chain lightning, fire trails, wall climbing, healing aura, banshee spawning, flying dive/swoop attacks, heat shrieks, ground pound AoE
- **Modifiers**: Radiated (HP regen), Charged (chain lightning), Infernal (fire trail + burn)
- **Dual Speed System**: Night speed bonus when dayTime is in the night range (13000–23000), plus a separate darkness speed bonus when both block light and sky light are ≤ 7. When both conditions apply, the higher bonus is used (not additive)
- Zombies do NOT burn in sunlight
- Name tags and HP bars only visible with line-of-sight (not through walls)

### Zombie AI Special Abilities
- **Block breaking** — zombies path to and break blocks separating them from targets
- **Heatmap investigation** — zombies in horde pathfinding mode route toward high-heat chunks
- **Variant abilities**: Riot Husk acid spit, Wrecking Husk explosion, Wall Creeper wall climbing, Charged chain lightning, Banshee horde spawning, Nurse healing aura, Behemoth ground pound, Vulture dive attacks, Zombie Bird swarm dive, Zombie Parrot heat shrieks
- Fully layered priority behavior tree — target acquisition, ability selection, and movement all conditions-checked

### Basic Weapons System
- **Melee**: Stone Axe, Wooden Club, Baseball Bat, Sledgehammer, Hunting Knife, Machete, Iron Spear, Stun Baton, Steel Knuckles, Wrench, Nailgun
- **Ranged**: Blunderbuss, Pipe Pistol, Pipe Rifle, Pipe Shotgun, Wooden Bow, Primitive Bow, Compound Bow, Compound Crossbow, Pistol, SMG, Shotgun, Hunting Rifle, AK-47, M60, Rocket Launcher
- All weapons use quality tier stat multipliers (×0.70 Poor → ×1.50 Legendary)
- Ranged weapons consume ammo (crafted at Workbench or directly from inventory)

### Blood Moon / Horde Night
- Every 7th night triggers a blood moon with escalating zombie waves
- Full timeline: warning at Day 6 evening → red sky → siren → horde waves → dawn cleanup
- Wave composition scales with game day (5 difficulty tiers)
- Day-based thresholds gate advanced variants (feral, demolisher, charged, infernal)
- Sleep prevented during active blood moon
- Red sky/fog visual effects

### Heatmap Activity System
- Player actions generate "heat" in nearby chunks (block breaking, torch placement, sprinting, explosions)
- Heat threshold spawning:
  - 25+: Scout Walkers
  - 50+: Banshee zombie
  - 75+: Mini-horde (8-12 mixed zombies)
  - 100: Continuous wave mode until heat drops
- Heat decays over time, radiates to neighboring chunks
- Spawning paused during blood moon
- Zombies in horde mode investigate and route toward high-heat chunks

### Loot & Crafting
- **17 Core Materials** + Survivor's Coin currency
- **7 Workstations**: Campfire, Grill, Workbench, Forge, Cement Mixer, Chemistry Station, Advanced Workbench — all with functional GUIs and fuel-based processing
- **8 Loot Containers**: Trash Pile, Cardboard Box, Gun Safe, Munitions Box, Supply Crate, Kitchen Cabinet, Medicine Cabinet, Bookshelf — loot scales with player level
- **Quality Tiers**: T1 Poor (x0.7) through T6 Legendary (x1.5) with color codes and mod slot scaling
- **Loot Stage**: `floor((level x 0.5) + (days x 0.3) + biomeBonus + perkBonus)`
- **Scrapping**: Break down items into materials (full yield at workbench, 50% in inventory)

### XP, Leveling & Perks
- XP from zombie kills and block mining
- Level-up formula: `XP_to_next = floor(1000 x level ^ 1.05)`
- +1 perk point per level, +1 bonus attribute point every 10 levels
- **5 Attribute Trees**: Strength, Perception, Fortitude, Agility, Intellect
- **45 Perks** (8 per tree + 5 Tier-10 masteries)
- Active perk effects: damage reduction, mining speed, health regen, stamina efficiency, temperature resistance, death-save (Unkillable), stealth kills (Ghost), and more

### Extended Day Cycle
- Day/night cycle doubled via **slower-tick approach** (TIME_SCALE=2) — game time advances 1 tick every 2 server ticks
- `dayTime` stays on vanilla 24,000-tick scale, so sun/moon rendering, F3 day counter, and all vanilla time logic work natively
- One full day takes ~40 real minutes instead of vanilla's ~20 minutes
- All blood moon, zombie behavior, and temperature thresholds use the natural `dayTime` values
- **Affected by TIME_SCALE**: day/night progression, sun/moon position, sleep cycle — anything driven by `dayTime`
- **NOT affected by TIME_SCALE**: server tick rate, debuff timers, heatmap decay, redstone, mob AI ticks — these all run on server ticks at normal speed

---

## Commands

All commands use the `/bzhs` prefix:

| Command | Permission | Description |
|---------|-----------|-------------|
| `/bzhs heat` | All | Show current chunk heat level and thresholds |
| `/bzhs heat_clear` | OP | Reset all heatmap data |
| `/bzhs cleardebuffs` | OP | Clear all active debuffs and effects |
| `/bzhs loot_stage` | All | Show your current loot stage breakdown |
| `/bzhs level` | All | Show your XP, level, and available points |
| `/bzhs stats` | All | Show your attribute levels and perk points |
| `/bzhs perk <id> [rank]` | All | Unlock or upgrade a perk |
| `/bzhs attribute <STR\|PER\|FOR\|AGI\|INT>` | All | Increase an attribute level |
| `/bzhs perks` | All | List all available perks and requirements |

---

## Configuration

Five server-side config files are generated in the `serverconfig/` folder:

| File | Contents |
|------|----------|
| `survival.toml` | Food/water drain rates, stamina costs, health regen thresholds, temperature settings, base max health |
| `horde.toml` | Blood moon cycle length, wave intervals, difficulty multiplier, spawn counts |
| `zombies.toml` | Per-variant HP/damage/speed overrides, modifier multipliers, special mechanic tuning |
| `heatmap.toml` | Enabled toggle, decay multiplier, spawn threshold multiplier |
| `loot.toml` | Container respawn days, loot abundance multiplier, quality scaling |

---

## Documentation

| Guide | Description |
|-------|-------------|
| [`docs/heatmap_guide.md`](docs/heatmap_guide.md) | Heatmap system mechanics with exact values |
| [`docs/debuffs_guide.md`](docs/debuffs_guide.md) | All 12 debuffs with triggers, effects, and durations |
| [`docs/zombie_guide.md`](docs/zombie_guide.md) | Zombie bestiary — all 20 variants with stats, abilities, and combat tips |
| [`docs/crafting_guide.md`](docs/crafting_guide.md) | Full crafting system: workstations, recipes, quality tiers, materials |
| [`docs/commands_guide.md`](docs/commands_guide.md) | All commands with syntax, parameters, and examples |
| [`docs/texture_audit.md`](docs/texture_audit.md) | Placeholder texture audit report (349 of 388 textures need replacement) |
| [`docs/edge-case-testing.md`](docs/edge-case-testing.md) | Edge case testing scenarios |
| [`docs/bzhs_final_spec.md`](docs/bzhs_final_spec.md) | Full implementation spec (2273 lines, 20 sections) |

---

## Building

Requires **Java 21**.

```bash
./gradlew build --no-daemon
```

Output JAR: `build/libs/BrutalZombieHordeSurvival-0.1.0-alpha.jar`

Copy the JAR into your Minecraft `mods/` folder (requires NeoForge 21.4.140 for Minecraft 1.21.4).

---

## Status

### Trader NPC System
- **5 named traders** (Joel, Rekt, Jen, Hugh, Bob) with specialty stock per trader
- Buy/sell GUI with Better Barter perk pricing integration
- Secret Stash tab unlocked at Better Barter rank 5
- Night closure (22:00-06:00 in-game) — traders refuse interaction
- Trader compounds as safe zones — no zombie spawns within compound
- Automatic restock cycle (configurable interval)
- Cyan "T" markers on compass and map

### Quest System
- **4 quest types**: Kill Count, Clear Territory, Fetch & Deliver, Buried Treasure
- Quest HUD overlay with progress tracking
- Quest markers on compass, minimap, and big map
- Accept/abandon/turn-in at trader NPCs
- Tier-appropriate quest generation per trader
- Quest refresh tied to trader restock cycle

### Power / Electricity System
- **Generator Bank** — fuel-powered (Gas Can), 100W output, 6000-tick burn time
- **Battery Bank** — 1000 EU storage, charges from sources, discharges to devices
- **Solar Panel** — 30W output during daytime with sky visibility
- **Power Grid Manager** — SavedData tracking wire connections between sources and devices
- Wire connections created via Electrical Parts right-click linking
- Powered devices: Blade Trap, Electric Fence Post, Advanced Workbench

### Building & Defense Blocks
- **Upgradeable Block** — 6-tier progression: Wood Frame → Reinforced Wood → Cobblestone → Concrete → Reinforced Concrete → Steel (right-click upgrade via repair hammer)
- **Wood Spikes** — Contact damage trap (4 dmg), 10 durability, degrades on hit
- **Iron Spikes** — Contact damage trap (8 dmg), 20 durability, degrades on hit
- **Blade Trap** — AoE damage (6 dmg), requires power
- **Electric Fence Post** — Contact damage (5 dmg) + stun, requires power
- **Land Claim Block** — 41-block protection radius preventing zombie spawns, one per player

### Farming System
- **Crop Block** — 4 growth stages (AGE 0-3), randomTick growth, Green Thumb perk integration
- **Farm Plot Block** — Tilled soil (hoe + dirt/grass conversion), crops must be planted on Farm Plots
- **Dew Collector** — Passive water generation (Murky Water every 6000 ticks with sky access)
- **Seed Items** — Right-click-on-farm-plot plantable seeds

### Armor System
- **3 armor tiers**: Light (Padded), Medium (Scrap Iron), Heavy (Military)
- Movement and stealth modifiers per tier
- **Set bonuses**: 2-piece partial + 4-piece full (Light: noise reduction, Medium: stamina regen, Heavy: damage reduction)
- Mixed-set warnings and armor shattered notifications

### Stealth / Noise System
- **NoiseManager** — Per-player noise tracking with 5-second linear decay
- Noise sources: Gunshot (80), Sprint (15), Walk (5), Crouch (1), Block Break (10)
- Zombies detect players via formula: `(noise × lightFactor) / distance²`
- Detection states: Unaware → Suspicious → Alert with visual particle indicators

### Custom Inventory Screen
- Replaces vanilla inventory with BzhsInventoryScreen
- Stats panel, perks panel, debuffs panel, armor set bonus display
- Full attribute names (Strength/Perception/Fortitude/Agility/Intellect)

### Premade World System
- Bundled world templates for quick start
- Create World screen integration with World Type toggle

### Implemented
- Core survival stats & HUD
- 12 debuff types with triggers and effects
- 20 zombie variants with special mechanics (including Zombie Bird and Zombie Parrot)
- Zombie AI special abilities (block breaking, heatmap investigation, horde pathfinding, variant abilities, smart A* pathfinding, coordinated block bashing, stealth detection)
- Blood moon / horde night system with atmosphere effects (red fog, camera shake)
- Heatmap activity-based spawning
- Loot containers, workstations, and crafting (all 7 workstations processing recipes)
- XP, leveling, and 45-perk system
- Basic weapons system (melee + ranged, quality-scaled)
- Icon-based HUD (hearts, food, water, armor icon rows)
- Compass, minimap, big map screen, and player tracking
- Extended day cycle via slower-tick refactor (TIME_SCALE=2, vanilla 24k dayTime preserved)
- 20 HP vanilla player base health
- Zombie stats balanced for vanilla 20 HP scale
- Placeholder texture audit (`docs/texture_audit.md`)
- Sound system foundation (8 custom events, gated playback, subtitles)
- Context-aware gameplay music system (day/night/combat/blood moon)
- Territory POIs (star-rated points of interest with procedural structures)
- Territory HUD (shows territory & building names when inside a POI)
- 3D weapon animations via Geckolib (AK-47, 9mm Pistol, Grenade)
- GeckoLib bundled via Jar-in-Jar (single-file mod distribution)
- Sprint fix: client-side `LocalPlayer.aiStep()` mixin prevents rubber-banding
- Skill books / magazines: 6 series (36 items), per-issue bonuses, series mastery tracking
- Custom biomes: 7 biome definitions with per-biome temperature ranges, zombie density multipliers, and loot tier bonuses
- Overworld biome placement: BZHS Apocalypse world preset with custom surface rules
- Trademark name sweep: zombie display names, perk IDs, and currency renamed to avoid trademark conflicts
- Village settlements: 8 building types with sleeper zombies and difficulty scaling
- Campfire merged into vanilla block
- Water bottle consolidation (Glass Jars removed)
- Vehicle wreckage blocks (decorative, drop scrap materials)
- Trader NPC system with specialty stock and economy
- Quest system (4 types, HUD overlay, map markers)
- Power/electricity system (Generator, Battery, Solar Panel, wire connections)
- Building & defense blocks (6-tier upgradeable, spikes, traps, land claim)
- Farming system (crops, farm plots, dew collector)
- Armor system (3 tiers with set bonuses)
- Stealth/noise system (detection states, noise decay)
- Custom inventory screen (stats, perks, debuffs, armor bonuses)
- Premade world system (bundled templates)

### Planned / Next
- Replace placeholder textures with real pixel art (prioritize HUD icons, weapons, workstations)
- Full world generation pipeline (city grid, POI templates)
- Vehicle system
- Multiplayer sync & balancing
- Performance optimization
