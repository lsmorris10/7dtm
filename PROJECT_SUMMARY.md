# PROJECT_SUMMARY — Brutal Zombie Horde Survival (BZHS)

**Minecraft 1.21.4 | NeoForge 21.4.140 | Mod ID: `sevendaystominecraft`**

A total conversion mod inspired by 7 Days to Die 2.6. One full JAR, no dependencies.

---

## What's Implemented

- **Survival stats** — Food, Water, Stamina, Temperature replacing vanilla hunger/regen
- **Icon HUD** — Heart, food, water, armor icon rows; compass strip, minimap, day counter, XP bar, debuff display
- **12 debuffs** — Bleeding, Infection 1/2, Sprain, Fracture, Concussion, Electrocuted, Stunned, Burn, Dysentery, Hypothermia, Hyperthermia, Radiation
- **18 zombie variants** — Walker, Crawler, Soldier, Frozen Lumberjack, Bloated Walker, Spider, Feral Wight, Screamer, Cop, Demolisher, Nurse, Mutated Chuck, Behemoth, Charged, Infernal, Zombie Dog, Zombie Bear, Vulture
- **Zombie AI** — Layered priority behavior tree; block breaking, heatmap investigation, horde pathfinding, variant-specific special abilities
- **Blood moon / horde night** — Every 7th night, warning → red sky → siren → waves → dawn cleanup; 5 difficulty tiers
- **Heatmap system** — Player actions generate heat; Scout (25+), Screamer (50+), mini-horde (75+), wave mode (100)
- **Basic weapons** — 11 melee + 15 ranged weapons, quality-scaled damage and ammo consumption
- **7 workstations** — Campfire, Grill, Workbench, Forge, Cement Mixer, Chemistry Station, Advanced Workbench; all with functional GUIs and fuel-based recipe processing
- **8 loot containers** — level-scaled loot, 6 quality tiers (Poor → Legendary)
- **17 materials + Dukes tokens** — full material economy
- **XP, leveling, 45 perks** — 5 attribute trees, kill/mine XP, perk unlocks
- **Extended day cycle** — TIME_SCALE=2 slower-tick; one day = ~40 real minutes, vanilla 24k dayTime preserved
- **20 HP vanilla player base** — zombie stats proportionally balanced for vanilla 20 HP scale
- **Landing page** — published with GitHub Releases download button and Ko-fi/Patreon funding page

---

## In Progress

- **Sound system** — zombie, combat, and ambient sound events
- **Territory POIs** — location-specific world generation points of interest
- **Geckolib animations** — 3D animated weapon and zombie models

---

## What's Next

- Replace 349 placeholder textures (full audit in `docs/texture_audit.md`; HUD icons, weapons, workstations highest priority)
- Sprint bug fix (client-side Mixin on `LocalPlayer.aiStep()`)
- World generation — custom biomes, structures, and POIs
- Skill books
- Trader NPCs
- Vehicle system

---

## Build

```bash
./gradlew build --no-daemon
```

Output: `build/libs/BrutalZombieHordeSurvival-0.1.0-alpha.jar` → drop into `mods/` (NeoForge 21.4.140, Minecraft 1.21.4, Java 21)
