# Brutal Zombie Horde Survival — Commands Guide

A complete reference for every in-game command in the mod, including syntax, parameters, valid values, and examples.

---

## Permissions

| Level | Who | Commands |
|-------|-----|----------|
| **None** | All players | `/bzhs loot_stage`, `/bzhs heat`, `/bzhs territory list`, `/bzhs level`, `/bzhs stats`, `/bzhs perk`, `/bzhs attribute`, `/bzhs perks` |
| **OP 2+** | Operators / admins | `/bzhs heat_clear`, `/bzhs territory listall`, `/bzhs cleardebuffs`, all `/bzhs admin` subcommands |

---

## Quick-Reference Summary

| Command | What It Does | Permission |
|---------|-------------|------------|
| `/bzhs loot_stage` | Shows your current loot stage | None |
| `/bzhs heat` | Shows your current chunk's heat level | None |
| `/bzhs heat_clear` | Resets all heat in every chunk to 0 | OP 2+ |
| `/bzhs territory list` | Lists nearby territories within 256 blocks | None |
| `/bzhs territory listall` | Lists all territories in the world | OP 2+ |
| `/bzhs level` / `/bzhs stats` | Shows your level, XP, perk/attribute points, and active perks | None |
| `/bzhs perk <perkId> [rank]` | Spend perk points to unlock or upgrade a perk | None |
| `/bzhs attribute <attr>` | Spend an attribute point to raise an attribute | None |
| `/bzhs perks` | Lists all registered perks grouped by attribute | None |
| `/bzhs cleardebuffs` | Clears all your active debuffs | OP 2+ |
| `/bzhs admin time day\|night\|bloodmoon` | Sets time or triggers a blood moon | OP 2+ |
| `/bzhs admin heatmap get\|set\|reset` | Inspect or manipulate chunk heat values | OP 2+ |
| `/bzhs admin debuff add\|remove\|clear` | Add, remove, or clear player debuffs | OP 2+ |
| `/bzhs admin stats set` | Set a player's survival stats | OP 2+ |
| `/bzhs admin territory spawn\|clear\|list` | Spawn, clear, or list territories | OP 2+ |
| `/bzhs admin zombie spawn` | Spawn specific zombie variants | OP 2+ |
| `/bzhs admin loot refill\|reset` | Refill or reset loot containers | OP 2+ |
| `/bzhs admin give` | Give mod items with optional quality | OP 2+ |

---

## Player Commands

These commands are available to all players and require no special permissions (unless noted).

### /bzhs loot_stage

Shows your current loot stage — the quality tier of loot you'll find in containers based on your game-stage progression. Displays your player level, days survived, and the formula used.

**Syntax:**

```
/bzhs loot_stage
```

**Example output:**

```
[BZHS] Loot Stage: 12
  Player Level: 15
  Days Survived: 8
  Formula: floor((level×0.5) + (days×0.3) + biomeBonus + perkBonus)
```

---

### /bzhs heat

Displays the heatmap value of the chunk you're standing in, the current spawn thresholds, and how many chunks in the world currently have active heat. See the [Heatmap Guide](heatmap_guide.md) for details on heat sources and spawn thresholds.

**Syntax:**

```
/bzhs heat
```

**Example output:**

```
[BZHS] Chunk (12, -5) Heat: 42.0/100 [Scouts:25 Screamer:50 Horde:75 Waves:100]
[BZHS] Active heated chunks: 7
```

---

### /bzhs heat_clear

Resets ALL heat in every chunk to 0 and clears all spawn cooldowns. Useful for resetting after testing.

**Permission:** OP 2+

**Syntax:**

```
/bzhs heat_clear
```

---

### /bzhs territory list

Shows nearby territories (POIs) within 256 blocks of your position. Output includes each territory's ID, label (type + tier stars), coordinates, distance, and cleared/active status.

**Syntax:**

```
/bzhs territory list
```

---

### /bzhs territory listall

Lists every territory generated in the world. Output includes each territory's ID, label (type + tier stars), coordinates, and cleared/active status.

**Permission:** OP 2+

**Syntax:**

```
/bzhs territory listall
```

---

### /bzhs level

Shows your current level, XP progress, available perk and attribute points, attribute levels, and active perks. `/bzhs stats` is an alias for this command.

**Syntax:**

```
/bzhs level
/bzhs stats
```

**Example output:**

```
[BZHS] Level: 5 | XP: 120/300 (40.0%)
[BZHS] Perk Points: 2 | Attribute Points: 1
[BZHS] Attributes: STR:3 PER:2 FOR:2 AGI:1 INT:1
[BZHS] Active Perks: Iron Fists[2] Pack Mule[1]
```

---

### /bzhs perk

Spend perk points to unlock or upgrade a perk. If no rank is specified, it upgrades to the next rank.

**Syntax:**

```
/bzhs perk <perkId> [rank]
```

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `perkId` | String | The perk's ID (use `/bzhs perks` to see all IDs) |
| `rank` | Integer | Target rank (optional — defaults to current rank + 1) |

**Examples:**

```
/bzhs perk iron_fists
/bzhs perk pack_mule 3
```

---

### /bzhs attribute

Spend an attribute point to raise an attribute by one level (max 10).

**Syntax:**

```
/bzhs attribute <attr>
```

**Parameters:**

| Parameter | Type | Valid Values |
|-----------|------|-------------|
| `attr` | String | `STR`, `PER`, `FOR`, `AGI`, `INT` |

**Examples:**

```
/bzhs attribute STR
/bzhs attribute INT
```

---

### /bzhs perks

Lists all registered perks grouped by attribute. Shows perk IDs, max ranks, and mastery perks.

**Syntax:**

```
/bzhs perks
```

---

### /bzhs cleardebuffs

Instantly removes all active debuffs from the player who runs it. Clears bleeding stacks, all debuff timers, movement speed penalties, and the nausea effect from Concussion.

**Permission:** OP 2+

**Syntax:**

```
/bzhs cleardebuffs
```

---

## Admin Commands

All admin commands live under `/bzhs admin` and require **permission level 2+** (operator). They are organized into 8 subcommand groups.

---

### Admin: Time

Control the day/night cycle and trigger blood moon events.

| Command | What It Does |
|---------|-------------|
| `/bzhs admin time day` | Advances time to the start of the next day |
| `/bzhs admin time night` | Sets the time to night (13000 ticks into the current day) |
| `/bzhs admin time bloodmoon` | Immediately triggers a blood moon event — sets time to dusk, advances the day counter, and starts horde spawning |

**Examples:**

```
/bzhs admin time day
/bzhs admin time night
/bzhs admin time bloodmoon
```

---

### Admin: Heatmap

Inspect and manipulate the per-chunk heatmap system that drives zombie spawning.

| Command | What It Does |
|---------|-------------|
| `/bzhs admin heatmap get` | Shows the heat value of your current chunk |
| `/bzhs admin heatmap get <player>` | Shows the heat value of another player's current chunk |
| `/bzhs admin heatmap set <value>` | Sets your current chunk's heat to a specific value (0–100) |
| `/bzhs admin heatmap set <player> <value>` | Sets another player's current chunk's heat to a value (0–100) |
| `/bzhs admin heatmap reset` | Resets your current chunk's heat to 0 |
| `/bzhs admin heatmap reset <player>` | Resets another player's current chunk's heat to 0 |

**Parameters:**

| Parameter | Type | Range | Description |
|-----------|------|-------|-------------|
| `player` | Player name | — | Target player (optional — defaults to yourself) |
| `value` | Float | 0–100 | Heat value to set |

**Examples:**

```
/bzhs admin heatmap get
/bzhs admin heatmap get Steve
/bzhs admin heatmap set 75
/bzhs admin heatmap set Steve 50
/bzhs admin heatmap reset
/bzhs admin heatmap reset Steve
```

**Heat thresholds** (configurable via `spawnThresholdMultiplier` in `heatmap.toml`):

| Heat Level | Spawn |
|------------|-------|
| 25 | Scout patrol (1–2 Walkers) |
| 50 | Screamer arrives |
| 75 | Mini-horde (8–12 mixed zombies) |
| 100 | Continuous wave mode |

---

### Admin: Debuffs

Add, remove, or clear survival debuffs on players. See the [Debuffs Guide](debuffs_guide.md) for full debuff details.

| Command | What It Does |
|---------|-------------|
| `/bzhs admin debuff add <debuff_id>` | Applies a debuff to yourself for 5 minutes |
| `/bzhs admin debuff add <player> <debuff_id>` | Applies a debuff to another player for 5 minutes |
| `/bzhs admin debuff remove <debuff_id>` | Removes a specific debuff from yourself |
| `/bzhs admin debuff remove <player> <debuff_id>` | Removes a specific debuff from another player |
| `/bzhs admin debuff clear` | Clears all debuffs from yourself |
| `/bzhs admin debuff clear <player>` | Clears all debuffs from another player |

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | Player name | Target player (optional — defaults to yourself) |
| `debuff_id` | String | One of the valid debuff IDs listed below |

**Valid Debuff IDs:**

| Debuff ID | Effect |
|-----------|--------|
| `bleeding` | −1 HP/3 sec per stack (max 3 stacks) |
| `infection_1` | −25% stamina regen |
| `infection_2` | −0.5 HP/sec (persistent) |
| `dysentery` | Water drain ×3, food drain ×2 |
| `sprain` | −30% movement speed |
| `fracture` | −60% movement speed, no sprinting |
| `concussion` | Nausea (screen wobble) |
| `burn` | −2 HP/sec |
| `hypothermia` | −20% speed, stamina drain ×2 |
| `hyperthermia` | Water drain ×3 |
| `electrocuted` | Complete movement freeze |
| `stunned` | Complete movement freeze |

**Examples:**

```
/bzhs admin debuff add bleeding
/bzhs admin debuff add Steve infection_1
/bzhs admin debuff remove dysentery
/bzhs admin debuff remove Steve fracture
/bzhs admin debuff clear
/bzhs admin debuff clear Steve
```

---

### Admin: Stats

Directly set a player's survival stats to specific values.

| Command | What It Does |
|---------|-------------|
| `/bzhs admin stats set <stat> <value>` | Sets one of your stats to a specific value |
| `/bzhs admin stats set <player> <stat> <value>` | Sets one of another player's stats to a specific value |

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | Player name | Target player (optional — defaults to yourself) |
| `stat` | String | One of the valid stat names listed below |
| `value` | Float | The value to set |

**Valid Stat Names:**

| Stat | Description | Typical Range |
|------|-------------|---------------|
| `health` | Player health (hearts × 2) | 0–20 |
| `stamina` | Stamina for sprinting and melee | 0–100 |
| `food` | Hunger level | 0–100 |
| `water` | Hydration level | 0–100 |
| `temperature` | Core body temperature | 0–100 |

**Examples:**

```
/bzhs admin stats set health 20
/bzhs admin stats set Steve stamina 100
/bzhs admin stats set water 50
```

---

### Admin: Territory

Manage POI territories — spawn test territories, mark them cleared, or list all.

| Command | What It Does |
|---------|-------------|
| `/bzhs admin territory spawn` | Spawns a random-tier territory at your position |
| `/bzhs admin territory spawn <tier>` | Spawns a territory of a specific tier (1–5) at your position |
| `/bzhs admin territory clear` | Marks the nearest territory within 64 blocks as cleared |
| `/bzhs admin territory list` | Lists all territories with coordinates and tier |

**Parameters:**

| Parameter | Type | Range | Description |
|-----------|------|-------|-------------|
| `tier` | Integer | 1–5 | Territory difficulty tier (optional — random if omitted) |

**Examples:**

```
/bzhs admin territory spawn
/bzhs admin territory spawn 3
/bzhs admin territory clear
/bzhs admin territory list
```

---

### Admin: Zombie Spawn

Spawn specific zombie variants near your position for testing.

**Syntax:**

```
/bzhs admin zombie spawn <variant> [count]
```

**Parameters:**

| Parameter | Type | Range | Description |
|-----------|------|-------|-------------|
| `variant` | String | — | Zombie type name or alias (see table below) |
| `count` | Integer | 1–50 | Number to spawn (optional — defaults to 1) |

**Zombie Variant Names and Aliases:**

| Variant | Aliases | Description |
|---------|---------|-------------|
| `walker` | `normal` | Standard zombie |
| `crawler` | — | Low, fast ground zombie |
| `frozen_lumberjack` | — | Tough cold-biome zombie (Frostbitten Woodsman) |
| `bloated_walker` | — | Tanky, slow walker (Bloated Shambler) |
| `spider_zombie` | `spider` | Wall-climbing, jump-boosted (Wall Creeper) |
| `feral_wight` | `feral`, `wight` | Fast, aggressive late-game zombie (Feral Wraith) |
| `cop` | `spitter` | Acid vomit ranged attack (Riot Husk) |
| `screamer` | — | Screams to attract more zombies (Banshee) |
| `zombie_dog` | `dog` | Fast pack hunter |
| `vulture` | — | Flying dive-bomber |
| `zombie_bird` | `bird` | Fast swarming flyer |
| `zombie_parrot` | `parrot` | Erratic flyer, generates heat |
| `demolisher` | — | Explodes on chest hit (Wrecking Husk) |
| `mutated_chuck` | — | Acid vomit ranged (Mutated Brute) |
| `zombie_bear` | `bear` | Charge + AoE swipe |
| `nurse` | — | Heals nearby zombies |
| `soldier` | — | Armored walker variant |
| `charged` | `irradiated` | Electric modifier variant |
| `infernal` | — | Fire modifier variant |
| `behemoth` | — | Boss-tier mega zombie |

**Examples:**

```
/bzhs admin zombie spawn walker
/bzhs admin zombie spawn feral 5
/bzhs admin zombie spawn zombie_bird 10
/bzhs admin zombie spawn behemoth
```

---

### Admin: Loot

Manage loot containers across all loaded chunks.

| Command | What It Does |
|---------|-------------|
| `/bzhs admin loot refill` | Refills all loot containers in loaded chunks with fresh loot (respects loot stage) |
| `/bzhs admin loot reset` | Fully resets all loot containers — clears contents and marks them as unopened so they regenerate on next player interaction |

**Examples:**

```
/bzhs admin loot refill
/bzhs admin loot reset
```

---

### Admin: Give Items

Give mod items to yourself with optional count and quality tier.

**Syntax:**

```
/bzhs admin give <item_id> [count] [quality]
```

`count` must be provided if you want to specify `quality` (positional arguments).

**Parameters:**

| Parameter | Type | Range | Description |
|-----------|------|-------|-------------|
| `item_id` | String | — | The mod item's registry name (tab-completable) |
| `count` | Integer | 1–64 | Number of items (optional — defaults to 1; required if specifying quality) |
| `quality` | String | — | Quality tier name (optional — see table below; requires count) |

**Quality Tiers:**

| Tier | Stat Multiplier | Mod Slots |
|------|----------------|-----------|
| `poor` | ×0.70 | 1 |
| `good` | ×0.85 | 1 |
| `great` | ×1.00 | 2 |
| `superior` | ×1.15 | 2 |
| `excellent` | ×1.30 | 3 |
| `legendary` | ×1.50 | 4 |

Quality is stored as NBT data and affects item stats. Items that don't support quality (non-weapons, non-armor) will ignore the quality parameter.

**Examples:**

```
/bzhs admin give iron_pipe
/bzhs admin give ak47 1 legendary
/bzhs admin give iron_armor_chest 1 superior
```
