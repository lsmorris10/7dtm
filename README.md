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
- Full HUD overlay with stat bars, day counter, temperature readout, debuff display, and XP bar
- 360° **compass strip** at top-center with cardinal/intercardinal markers and heat level indicator
- **Minimap** in top-right corner with terrain colors, player dot, and nearby player tracking
- Player base health set to **100 HP** (50 hearts) for balanced combat against custom zombies
- Vanilla hunger bar, health hearts, and passive regen disabled via Mixins

### Debuffs (12 Types)
- **Bleeding** — Zombie hits (30% chance), stacks up to 3x, damage scales with stacks
- **Infection Stage 1/2** — Zombie hits (10% chance), stamina penalty → lethal drain if untreated
- **Sprain** — Fall 4-7 blocks, -30% movement speed for 30 minutes
- **Fracture** — Fall 8+ blocks, -60% movement speed + sprint blocked for 60 minutes
- **Concussion** — Explosion within 3 blocks, nausea/screen wobble for 45 seconds
- **Electrocuted** — Charged zombie hit, 1.5 second movement freeze
- **Stunned** — Cop bile or Cop/Demolisher explosion, 2 second movement freeze
- **Burn, Dysentery, Hypothermia, Hyperthermia, Radiation** — Various environmental triggers
- All debuffs clear on death

### Zombie System (18 Variants + 3 Modifiers)
- **Humanoid (15)**: Walker, Crawler, Soldier, Frozen Lumberjack, Bloated Walker, Spider Zombie, Feral Wight, Screamer, Cop, Demolisher, Nurse, Mutated Chuck, Behemoth, Charged, Infernal
- **Animals (3)**: Zombie Dog, Zombie Bear, Vulture
- **Special Mechanics**: Explosions, acid spit projectiles, chain lightning, fire trails, wall climbing, healing aura, screamer spawning, flying dive attacks, ground pound AoE
- **Modifiers**: Radiated (HP regen), Charged (chain lightning), Infernal (fire trail + burn)
- **Night Speed**: 2.25x movement speed at night (configurable)
- Zombies do NOT burn in sunlight
- Name tags and HP bars only visible with line-of-sight (not through walls)

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
  - 50+: Screamer zombie
  - 75+: Mini-horde (8-12 mixed zombies)
  - 100: Continuous wave mode until heat drops
- Heat decays over time, radiates to neighboring chunks
- Spawning paused during blood moon

### Loot & Crafting
- **17 Core Materials** + Dukes Casino Token currency
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

### Implemented
- Core survival stats & HUD
- 12 debuff types with triggers and effects
- 18 zombie variants with special mechanics
- Blood moon / horde night system
- Heatmap activity-based spawning
- Loot containers, workstations, and crafting
- XP, leveling, and 45-perk system
- Compass, minimap, and player tracking
- Extended day cycle via slower-tick refactor (TIME_SCALE=2, vanilla 24k dayTime preserved)
- 100 HP player base health
- Vanilla mob damage scaling for all mobs (fall, drowning, fire, lava, cactus, plus all vanilla mob attacks proportionally scaled to 100 HP)
- LevelTimeOfDayMixin crash fix (target corrected to `getSunAngle`)
- Mod JAR and metadata renamed to match BZHS branding
- Landing page download button with GitHub Releases API integration

### In Progress / Next
- Sprint bug fix (client-side Mixin)
- Custom textures and models for zombie variants
- World generation (custom biomes, structures, POIs)
- Skill books
- Trader NPCs
