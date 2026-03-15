# Brutal Zombie Horde Survival — Heatmap System Guide

The heatmap is a per-chunk tracking system that measures how much noise and activity a player generates. The more you do in an area, the more heat builds up — and the more zombies come looking for you.

---

## How Heat Works

Every 16x16 block chunk in the Overworld has its own heat value, starting at 0 and capping at **100**. Player activities add heat to the chunk you're in, and that heat spreads to nearby chunks at reduced intensity. Heat naturally decays over time, so if you stop being active in an area, it cools down.

The system only runs in the Overworld and is paused during Blood Moon horde nights.

---

## Heat Sources

| Activity | Heat Added | Spread Radius | Decay Rate | Notes |
|----------|-----------|---------------|------------|-------|
| **Breaking a block** | +0.5 | 3 chunks | 2.0/min | Any block, including mining ore |
| **Placing a torch** | +2.0 | 1 chunk | 1.0/min | Includes wall torches |
| **Explosion** | +25.0 | 6 chunks | 2.0/min | TNT, creeper, any explosion |
| **Sprinting** | +0.2/sec | 2 chunks | 3.0/min | Accumulates every second while sprinting |

**Spread radius** means the heat also affects neighboring chunks, but at reduced intensity. For example, an explosion adds +25 heat to the center chunk and a fraction of that to chunks within 6 chunks away. The further from the center, the less heat reaches that chunk.

**Decay rate** is how fast each heat source fades on its own, measured in heat points lost per minute. A torch placement (+2.0 heat, 1.0/min decay) would fully fade in about 2 minutes if nothing else is added.

---

## What Spawns at Each Heat Level

Heat thresholds determine what shows up. Once a chunk's total heat crosses a threshold, a spawn is triggered (with a cooldown so you're not overwhelmed instantly).

| Heat Level | What Happens | Spawn Count | Cooldown |
|------------|-------------|-------------|----------|
| **25+** | **Scout patrol** — Walker zombies investigate the area | 1-2 Walkers | 30 seconds |
| **50+** | **Screamer arrives** — a Screamer zombie spawns independently | 1 Screamer | 60 seconds |
| **75+** | **Mini-horde** — a mixed group attacks | 8-12 zombies (Walkers, Crawlers, Feral Wights, Spider Zombies, Bloated Walkers) | 90 seconds |
| **100** | **Wave mode** — continuous assault begins | 8-12 per wave (adds Cops to the mix) | 90 seconds between waves |

**Important details:**
- The Screamer spawns on its own check — it can appear alongside scouts or a mini-horde.
- Wave mode zombies include Cop zombies in addition to the mini-horde lineup.
- Mini-hordes and wave spawns prefer dark locations (light level 7 or below, or nighttime). If no dark spot is found, they'll spawn anywhere valid.
- Scouts spawn at any valid surface position 20-48 blocks from the player.

---

## Wave Mode

When a chunk hits **100 heat**, it enters **wave mode** — a continuous cycle of zombie waves every 90 seconds. This keeps going until the heat drops below **75** (75% of the wave threshold).

Wave mode is the heatmap's way of punishing sustained heavy activity. If you keep mining, placing torches, and running around in one area without letting heat decay, you'll face relentless zombie waves.

**To escape wave mode:**
- Stop all activity in that chunk and let heat decay naturally below 75.
- Move to a different area — heat is tracked per chunk, so a fresh chunk starts at 0.
- Use `/bzhs heat_clear` to reset all heat (debug/admin command).

---

## Heat Decay

Heat doesn't last forever. Each heat source decays individually based on its own decay rate:
- Decay is calculated every second (technically every 20 game ticks).
- The formula: each tick, a source loses `(decayPerMinute / 60)` heat points.
- When a source hits 0, it's removed entirely.
- If all sources in a chunk are gone, the chunk returns to 0 heat.

You can adjust how fast heat decays globally using the config (see below).

---

## Commands

| Command | What It Does |
|---------|-------------|
| `/bzhs heat` | Shows the heat level of the chunk you're standing in, plus the spawn thresholds and total active heated chunks in the world |
| `/bzhs heat_clear` | Resets ALL heat in every chunk to 0 and clears all spawn cooldowns. Debug/admin use. |

---

## Configuration (heatmap.toml)

The heatmap system is configurable via `heatmap.toml` in your config folder:

| Setting | Default | Range | What It Does |
|---------|---------|-------|-------------|
| `enabled` | true | true/false | Turns the entire heatmap system on or off |
| `decayMultiplier` | 1.0 | 0.1 - 5.0 | Scales all decay rates. 2.0 = heat fades twice as fast. 0.5 = heat lingers twice as long. |
| `spawnThresholdMultiplier` | 1.0 | 0.5 - 3.0 | Scales the spawn thresholds. 0.5 = scouts at 12.5, screamer at 25, etc. 2.0 = scouts at 50, screamer at 100, etc. |

**Examples:**
- Want a harder experience? Set `spawnThresholdMultiplier` to 0.5 — zombies come at half the normal heat levels.
- Want a quieter game? Set `decayMultiplier` to 3.0 and `spawnThresholdMultiplier` to 2.0 — heat fades fast and zombies need much more activity to trigger.

---

## Tips

- **Mining sessions** generate steady heat from block breaks. Long mining runs in one chunk will eventually attract scouts, then screamers, then mini-hordes.
- **Building a base** with lots of torches adds up fast — each torch is +2 heat. Placing 13 torches in one chunk hits the scout threshold.
- **TNT/explosions** are the fastest way to spike heat — a single explosion adds +25 and spreads wide.
- **Moving around** helps. If you spread your activity across multiple chunks, no single chunk reaches dangerous levels.
- **Night is riskier** — mini-horde and wave spawns prefer dark areas, which are everywhere at night.
