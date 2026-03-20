# Brutal Zombie Horde Survival — Zombie Bestiary

A complete guide to every zombie type in the mod, including base stats, special abilities, spawn conditions, modifier variants, and universal behaviors.

---

## Quick-Reference Table

| # | Zombie | HP | Damage | Speed | XP | Min Day | Special Ability |
|---|--------|-----|--------|-------|-----|---------|-----------------|
| 1 | Walker | 20 | 3.0 | 1.0 | 200 | 1 | None (basic melee) |
| 2 | Crawler | 12 | 3.8 | 0.8 | 150 | 1 | Low profile, hard to hit |
| 3 | Frostbitten Woodsman | 30 | 4.5 | 0.9 | 250 | 1 | Tanky early-game threat |
| 4 | Bloated Shambler | 40 | 3.8 | 0.7 | 300 | 3 | Death explosion (2-block radius) |
| 5 | Wall Creeper | 24 | 5.3 | 1.8 | 300 | 5 | Wall climbing, leap attack |
| 6 | Feral Wraith | 60 | 7.5 | 2.5 | 350 | 7 | Permanent sprint |
| 7 | Riot Husk | 70 | 5.6 | 1.2 | 400 | 14 | Acid spit (ranged), death explosion |
| 8 | Banshee | 16 | 1.9 | 1.5 | 250 | 7 | Scream summons 4-8 zombies |
| 9 | Zombie Dog | 16 | 6.8 | 3.5 | 200 | 3 | Pack spawning, high speed |
| 10 | Vulture | 12 | 4.5 | 4.0 | 200 | 7 | Flight, swoop attack |
| 11 | Wrecking Husk | 160 | 11.3 | 1.0 | 800 | 21 | Chest-hit explosion (8-block radius) |
| 12 | Mutated Brute | 50 | 6.8 | 1.3 | 350 | 14 | Ranged vomit attack (11 blocks) |
| 13 | Zombie Bear | 120 | 13.1 | 2.0 | 500 | 14 | High damage, charge attack |
| 14 | Nurse | 24 | 3.8 | 1.0 | 250 | 7 | Healing aura (5-block radius, 5 HP/sec to nearby zombies) |
| 15 | Soldier | 80 | 9.4 | 1.5 | 400 | 14 | Armored, high HP |
| 16 | Charged | * | * | * | 550 | 21 | Chain lightning on melee hit |
| 17 | Infernal | * | * | * | 550 | 21 | Fire trail, fire immunity |
| 18 | Behemoth | 400 | 18.8 | 0.8 | 2000 | 35 | Ground pound AoE, knockback immune |

\* Charged and Infernal are standalone entity types whose base stats are calculated from default values (20 HP, 3.0 damage, 1.0 speed) multiplied by their respective modifier multipliers.

---

## Zombie Details

### Walker

| Property | Value |
|----------|-------|
| HP | 20 |
| Damage | 3.0 |
| Speed | 1.0 blocks/sec |
| XP | 200 |
| First Appears | Day 1 |

The basic zombie. No special abilities — it walks toward you and attacks in melee. Walkers are the most common zombie in the game and make up the bulk of early-game encounters, heatmap scout spawns, and horde night waves. They serve as the baseline that every other zombie type is compared against.

---

### Crawler

| Property | Value |
|----------|-------|
| HP | 12 |
| Damage | 3.8 |
| Speed | 0.8 blocks/sec |
| XP | 150 |
| First Appears | Day 1 |

A low-profile zombie that moves along the ground. Crawlers are slower and have less health than Walkers, but their reduced height makes them harder to hit with ranged weapons and easier to miss in tall grass or uneven terrain. They deal slightly more damage per hit than Walkers.

---

### Frostbitten Woodsman

| Property | Value |
|----------|-------|
| HP | 30 |
| Damage | 4.5 |
| Speed | 0.9 blocks/sec |
| XP | 250 |
| First Appears | Day 1 |

A tanky early-game zombie with 50% more HP than a Walker. The Frostbitten Woodsman hits harder and takes more punishment, making it a noticeable step up from basic Walkers even on day 1. No special abilities, but its stat advantage makes it a genuine threat with early-game gear.

---

### Bloated Shambler

| Property | Value |
|----------|-------|
| HP | 40 |
| Damage | 3.8 |
| Speed | 0.7 blocks/sec |
| XP | 300 |
| First Appears | Day 3 |

A slow, bloated zombie that explodes on death. The death explosion has a 2-block radius and can damage nearby players, entities, and blocks. The Bloated Shambler is dangerous in groups — killing one near other zombies (or near a Wrecking Husk) can trigger chain reactions.

**Special Ability — Death Explosion:**
- Radius: 2 blocks
- Triggers on death from any cause
- Can damage players, other zombies, and blocks in the blast zone

---

### Wall Creeper

| Property | Value |
|----------|-------|
| HP | 24 |
| Damage | 5.3 |
| Speed | 1.8 blocks/sec |
| XP | 300 |
| First Appears | Day 5 |

A fast, agile zombie that can climb vertical surfaces and leap at targets. The Wall Creeper ignores traditional wall defenses by scaling the exterior and attacking from above or unexpected angles. Its leap attack closes distance quickly, and its high speed makes it hard to kite.

**Special Abilities:**
- **Wall Climbing:** Can scale vertical surfaces like spiders
- **Leap Attack:** Lunges at targets from a distance (0.6 leap velocity)

---

### Feral Wraith

| Property | Value |
|----------|-------|
| HP | 60 |
| Damage | 7.5 |
| Speed | 2.5 blocks/sec |
| XP | 350 |
| First Appears | Day 7 |

A permanently sprinting zombie with high HP, high damage, and high speed. The Feral Wraith always runs regardless of day/night cycle or light level — its `setSprinting(true)` is enforced every tick. At 2.5 blocks/sec base speed, it's one of the fastest ground zombies in the game.

**Special Ability — Permanent Sprint:**
- Always sprinting, cannot be slowed to walking pace
- Sprint is re-applied every tick, preventing any knockback-based slowdowns

---

### Riot Husk

| Property | Value |
|----------|-------|
| HP | 70 |
| Damage | 5.6 |
| Speed | 1.2 blocks/sec |
| XP | 400 |
| First Appears | Day 14 |

A ranged-capable zombie that spits acid projectiles and explodes on death. The Riot Husk's acid spit uses an AcidBall projectile with an 8-block effective range. On death, it detonates — making it dangerous even in its final moments.

**Special Abilities:**
- **Acid Spit:** Fires an AcidBall projectile at targets within range. The projectile deals contact damage on impact. Has a cooldown between shots.
- **Death Explosion:** Explodes when killed. The blast can apply the Stunned debuff to nearby players and trigger the Concussion debuff if within 3 blocks.

---

### Banshee

| Property | Value |
|----------|-------|
| HP | 16 |
| Damage | 1.9 |
| Speed | 1.5 blocks/sec |
| XP | 250 |
| First Appears | Day 7 |

A fragile support zombie that summons reinforcements by screaming. The Banshee has low HP and minimal melee damage — its threat comes entirely from its scream ability, which directly spawns 4-8 Walker zombies in the surrounding area. It has a 3-scream cap and a 600-tick (30-second) cooldown between screams. After screaming, the Banshee attempts to flee from the player.

**Special Ability — Scream:**
- Spawns 4-8 Walker zombies within 8-20 blocks
- Maximum 3 screams per Banshee
- 600-tick (30 second) cooldown between screams
- Spawns zombies directly (independent of the heatmap system)
- Banshee flees from the player after screaming

The Banshee also spawns as a heatmap response at heat level 50+. Its screaming does NOT add heatmap heat — it spawns zombies through a separate code path.

---

### Zombie Dog

| Property | Value |
|----------|-------|
| HP | 16 |
| Damage | 6.8 |
| Speed | 3.5 blocks/sec |
| XP | 200 |
| First Appears | Day 3 |

A fast, low-HP zombie that spawns in packs. Zombie Dogs are glass cannons — they hit hard and move fast, but go down quickly. Their 3.5 blocks/sec base speed makes them faster than sprinting players, so you cannot simply outrun them. Deal with them at range when possible.

---

### Vulture

| Property | Value |
|----------|-------|
| HP | 12 |
| Damage | 4.5 |
| Speed | 4.0 blocks/sec |
| XP | 200 |
| First Appears | Day 7 |

A flying zombie that attacks from the air with swoop attacks. Vultures are the fastest zombie type in the game at 4.0 blocks/sec. They bypass ground-level defenses entirely and can be difficult to hit with melee weapons. Ranged weapons are strongly recommended.

**Special Ability — Flight:**
- Flies above ground level
- Performs diving swoop attacks
- Ignores ground-based walls and obstacles

---

### Wrecking Husk

| Property | Value |
|----------|-------|
| HP | 160 |
| Damage | 11.3 |
| Speed | 1.0 blocks/sec |
| XP | 800 |
| First Appears | Day 21 |

A massive, heavily armored zombie that detonates when hit in the chest. The Wrecking Husk's chest-hit explosion has an 8-block radius — large enough to destroy significant portions of a base. The key is to aim for the head or legs; chest hits trigger the detonation.

**Special Ability — Chest-Hit Explosion:**
- Hitting the Wrecking Husk's chest area triggers an 8-block radius explosion
- The explosion destroys blocks (based on game rules) and deals heavy damage
- Head shots and leg shots do NOT trigger the explosion
- The explosion can trigger structural integrity collapses

At 160 HP, the Wrecking Husk is extremely durable. Combined with the explosion risk, it's one of the most dangerous zombies in the game.

---

### Mutated Brute

| Property | Value |
|----------|-------|
| HP | 50 |
| Damage | 6.8 |
| Speed | 1.3 blocks/sec |
| XP | 350 |
| First Appears | Day 14 |

A mid-tier zombie with a ranged vomit attack. The Mutated Brute uses a SmallFireball-based projectile that reaches up to 11 blocks, giving it a dangerous ranged option on top of its solid melee stats.

**Special Ability — Vomit Attack:**
- Ranged projectile attack using SmallFireball
- Effective range: 11 blocks
- Deals contact damage on hit (damage comes from the projectile itself)

---

### Zombie Bear

| Property | Value |
|----------|-------|
| HP | 120 |
| Damage | 13.1 |
| Speed | 2.0 blocks/sec |
| XP | 500 |
| First Appears | Day 14 |

A high-HP, high-damage, high-speed threat. The Zombie Bear combines 120 HP of durability with 13.1 damage per hit and 2.0 blocks/sec movement speed. It has a charge attack that closes distance rapidly. Fighting a Zombie Bear in melee without good armor is extremely dangerous.

**Special Ability — Charge Attack:**
- Charges at targets with increased speed
- High knockback on impact

---

### Nurse

| Property | Value |
|----------|-------|
| HP | 24 |
| Damage | 3.8 |
| Speed | 1.0 blocks/sec |
| XP | 250 |
| First Appears | Day 7 |

A support zombie with a healing aura that keeps nearby zombies alive. The Nurse heals all zombies within a 5-block radius at 5 HP per second (applied every 20 ticks). The Nurse itself has low stats — its danger is in making every other zombie around it much harder to kill.

**Special Ability — Healing Aura:**
- Heals all zombie entities within a 5-block radius
- Heal rate: 5 HP per second (configurable)
- Triggers every 20 ticks (1 second)
- Does NOT heal itself through the aura (only other zombies)
- Heart particles appear above the Nurse when healing

**Priority target.** Kill Nurses before other zombies to prevent them from out-healing your damage output. A Radiated Nurse is particularly dangerous — it regenerates 2 HP/sec on its own while healing everything around it.

---

### Soldier

| Property | Value |
|----------|-------|
| HP | 80 |
| Damage | 9.4 |
| Speed | 1.5 blocks/sec |
| XP | 400 |
| First Appears | Day 14 |

An armored, high-stat zombie with no special abilities beyond raw power. The Soldier has 80 HP, 9.4 damage, and 1.5 speed — making it a well-rounded mid-tier threat. Think of it as an upgraded Walker with substantially better stats across the board.

---

### Charged

| Property | Value |
|----------|-------|
| HP | 36 (20 base x1.8) |
| Damage | 3.9 (3.0 base x1.3) |
| Speed | 1.2 (1.0 base x1.2) |
| XP | 550 |
| First Appears | Day 21 |

A standalone electric zombie that triggers chain lightning on every melee hit. The Charged zombie applies the Electrocuted debuff to the primary target (1.5-second movement freeze) and sends chain lightning arcing to up to 3 nearby entities, dealing 5.0 damage via lightningBolt damage and applying a brief freeze effect.

**Special Ability — Chain Lightning:**
- Triggers on every successful melee hit
- Arcs to up to 3 nearby entities within 3 blocks of the initial target
- Chain targets take 5.0 `chargedChainDamage` via lightningBolt damage source
- Chain targets receive `setTicksFrozen(30)` (vanilla freeze effect)
- Primary melee target receives the Electrocuted debuff (1.5 second movement freeze)
- Chain lightning checks line-of-sight between targets (blocked by solid blocks)

Charged zombies demand ranged combat. Melee fighting one results in repeated stuns that leave you vulnerable to other zombies.

---

### Infernal

| Property | Value |
|----------|-------|
| HP | 36 (20 base x1.8) |
| Damage | 4.2 (3.0 base x1.4) |
| Speed | 1.1 (1.0 base x1.1) |
| XP | 550 |
| First Appears | Day 21 |

A standalone fire zombie that leaves a trail of fire blocks as it walks and is completely immune to fire damage. The Infernal zombie's fire trail can ignite wooden structures, trigger structural integrity collapses, and create persistent area denial during horde nights.

**Special Ability — Fire Trail:**
- Spawns fire blocks behind the zombie as it moves (every 20 ticks)
- The zombie is completely fire-immune
- Fire trail can ignite flammable blocks (wood, planks, etc.)
- Contact with the Infernal zombie can apply the Burn debuff

Infernal zombies are particularly dangerous during base defense — their fire trails can compromise wooden fortifications and cause cascading structural failures.

---

### Behemoth

| Property | Value |
|----------|-------|
| HP | 400 |
| Damage | 18.8 |
| Speed | 0.8 blocks/sec |
| XP | 2000 |
| First Appears | Day 35 |

The ultimate zombie. The Behemoth has 400 HP, deals 18.8 damage per hit, and performs a devastating ground pound AoE attack. It is completely immune to knockback. At day 35+, Behemoths represent the endgame challenge — fighting one requires significant preparation, good weapons, and ideally a group.

**Special Ability — Ground Pound:**
- AoE attack with a 6-block radius
- Deals 75% of base damage (14.1) to all entities in range
- Applies knockback to affected targets
- Has a cooldown between uses
- Visual: particle effects on impact

**Knockback Immunity:**
- The Behemoth's `knockback()` method is overridden to do nothing
- No weapon, explosion, or ability can push a Behemoth back

---

## Modifier Variants

Modifier variants are enhanced versions of base zombie types. Three modifiers exist, and each one multiplies the base zombie's stats by fixed multipliers. Modifiers can be applied to any base zombie type (Walker, Crawler, Feral Wraith, Riot Husk, Soldier, etc.) via the modifier system.

### Modifier Stat Multipliers

| Modifier | HP Multiplier | Damage Multiplier | Speed Multiplier | Bonus XP | First Appears |
|----------|--------------|-------------------|-----------------|----------|---------------|
| **Radiated** | x2.0 | x1.5 | x1.3 | +500 | Day 28 |
| **Charged** | x1.8 | x1.3 | x1.2 | +550 | Day 21 |
| **Infernal** | x1.8 | x1.4 | x1.1 | +550 | Day 21 |

### Radiated (Day 28+)

Radiated zombies have doubled HP, increased damage and speed, and regenerate 2 HP per second passively. The regeneration makes them extremely durable in prolonged fights — you must out-damage the regen to kill them.

- **HP Regeneration:** 2 HP/sec (applied every 20 ticks)
- **Appearance:** Green-tinted glow
- **Example:** A Radiated Soldier has 160 HP (80 x2.0), 14.1 damage (9.4 x1.5), 1.95 speed (1.5 x1.3), plus 2 HP/sec regen

### Charged (Day 21+)

When applied as a modifier (not the standalone entity), the Charged modifier boosts stats and adds chain lightning to melee attacks.

- **Chain Lightning:** Same behavior as the standalone Charged zombie — arcs to 3 nearby targets on melee hit
- **Example:** A Charged Feral Wraith has 108 HP (60 x1.8), 9.75 damage (7.5 x1.3), 3.0 speed (2.5 x1.2), plus chain lightning

### Infernal (Day 21+)

When applied as a modifier (not the standalone entity), the Infernal modifier boosts stats and adds a fire trail.

- **Fire Trail:** Same behavior as the standalone Infernal zombie — spawns fire blocks every 20 ticks
- **Fire Immunity:** The zombie is immune to fire damage
- **Example:** An Infernal Wrecking Husk has 288 HP (160 x1.8), 15.82 damage (11.3 x1.4), 1.1 speed (1.0 x1.1), plus fire trail

---

## Universal Behaviors

All BZHS zombies share these behaviors regardless of type or modifier:

### No Sunlight Burning
BZHS zombies do NOT burn in sunlight. Unlike vanilla Minecraft zombies, they are active 24/7. The `isSunSensitive()` method returns `false` for all types.

### Night Speed Bonus
Between dayTime 13000-23000 (night), all zombies receive a configurable speed bonus. The bonus is multiplicative — e.g., a 1.25x night bonus makes a Walker move at 1.25 blocks/sec instead of 1.0.

### Darkness Speed Bonus
When both block light AND sky light are at or below the darkness threshold (default 7), zombies receive a darkness speed bonus. This applies even during daytime — caves are extra dangerous because the darkness bonus is always active underground.

When both night and darkness conditions are true, only the higher bonus is applied (they do not stack additively). The formula uses `Math.max(nightBonus, darknessBonus)`.

### Block Breaking
All zombies can break blocks to reach players. They use a pathfinding-integrated block breaking AI (`ZombieBreakBlockGoal`) that factors in block material hardness. Zombies prefer weaker materials (wood over concrete) when pathing through structures.

### Heatmap Investigation
Zombies spawned by the heatmap system will investigate the source of heat using `ZombieInvestigateGoal`. They path toward the highest-heat chunk in their area.

### Horde Pathfinding
During Blood Moon horde nights, zombies use `ZombieHordePathGoal` to aggressively path toward players. Horde zombies have `setPersistenceRequired()` set — they will not despawn.

### Name Tags
All zombies display a floating name tag showing their type name (with modifier prefix if applicable) and current HP / max HP. The name tag updates every 5 ticks to reflect health changes in real time.

---

## Spawn Conditions

### Natural Spawning
Zombies spawn naturally based on biome zombie density multipliers and the current game day. Each zombie type has a minimum spawn day requirement — types with higher minimum days will not appear until that day is reached.

### Heatmap Spawning
The heatmap system spawns zombies based on player activity. See the [Heatmap Guide](heatmap_guide.md) for details on heat thresholds and spawn composition.

### Blood Moon Spawning
During horde nights, zombies spawn in escalating waves. Wave composition changes based on the current day:
- **Day 7:** Walkers (70%), Crawlers (20%), Feral Wraiths (10% — only if day >= 14)
- **Day 14:** Adds Riot Husks (feralDay config = 14 enables Feral Wraiths and Riot Husks)
- **Day 21:** Adds Wrecking Husks, Charged, and Infernal modifier variants
- **Day 28+:** Adds Radiated modifier variants

See the [edge case testing doc](../docs/edge-case-testing.md) for detailed horde composition testing notes.

### Village Sleeper Spawning
Village settlements contain sleeper zombies that spawn inside buildings. The number and type of sleepers depends on the building type and the territory's difficulty tier. Sleeper zombies are placed at valid spawn positions within each building and activate when a player enters the area.

---

## Combat Tips

- **Kill Nurses first** — their healing aura can out-heal your damage, making every other zombie in the group effectively unkillable.
- **Use ranged weapons against Charged zombies** — melee attacks trigger chain lightning that stuns you, leaving you open to other zombies.
- **Aim for the head on Wrecking Husks** — chest hits trigger the 8-block explosion that can destroy your base.
- **Avoid fighting Infernal zombies in wooden structures** — their fire trail will ignite wood and can cause structural collapses.
- **Prioritize Banshees quickly** — each scream spawns 4-8 more zombies. Three screams means up to 24 extra Walkers.
- **Respect Zombie Bears** — 120 HP, 13.1 damage, and 2.0 speed makes them one of the most dangerous mid-game threats.
- **Behemoths require preparation** — at 400 HP with knockback immunity and a ground pound AoE, you need end-game gear and ideally a group.
- **Darkness means danger** — underground caves trigger the darkness speed bonus on all zombies, even during daytime. Bring light sources and good weapons.
