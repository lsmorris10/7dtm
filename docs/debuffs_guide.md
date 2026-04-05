# Brutal Zombie Horde Survival — Debuffs Guide

A complete guide to every debuff in the game, including how they're triggered, what they do to your character, how long they last, and how to cure or manage them.

---

## How Debuffs Work

Debuffs are negative status effects applied to your character through combat, environmental hazards, falls, and other dangers. Each debuff has a duration measured in server ticks (20 server ticks = 1 second), and they count down automatically every server tick. Debuff timers run at normal server tick speed and are **not** slowed by the extended day cycle (TIME_SCALE). When the timer hits zero, the debuff is removed.

**Key mechanics:**
- Debuffs use a **longest-duration-wins** rule. If you're hit with the same debuff again while it's already active, only the longer duration is kept — the timer never downgrades.
- All debuffs are **cleared on death**. When you respawn, you start fresh with no active debuffs.
- Admins can instantly remove all debuffs with the `/bzhs cleardebuffs` command (requires permission level 2+).

---

## Quick-Reference Summary

| Debuff | Source | Duration | Key Effect |
|--------|--------|----------|------------|
| **Bleeding** | Zombie hit (30% chance) | 60 minutes | −1 HP/3 sec per stack (max 3 stacks) |
| **Infection (Stage 1)** | Zombie hit (10% chance) | 20 minutes | −25% stamina regen |
| **Infection (Stage 2)** | Severe infection | Until cured or death | −0.5 HP/sec |
| **Sprain** | Fall 4–7 blocks | 30 minutes | −30% movement speed |
| **Fracture** | Fall 8+ blocks | 60 minutes | −60% movement speed, no sprinting |
| **Concussion** | Explosion within 3 blocks | 45 seconds | Nausea (screen wobble) |
| **Stunned** | Riot Husk acid hit / zombie explosion | 2 seconds | Complete movement freeze |
| **Electrocuted** | Charged zombie melee | 1.5 seconds | Complete movement freeze |
| **Burn** | Fire, Infernal zombies | Varies | −2 HP/sec |
| **Dysentery** | Contaminated food/water | Varies | Water drain ×3, food drain ×2 |
| **Hypothermia** | Prolonged cold exposure | Varies | −20% speed, stamina drain ×2 |
| **Hyperthermia** | Prolonged heat exposure | Varies | Water drain ×3 |
| **Radiation** | Radiated zombies | Varies | −1 HP/5 sec |

---

## Debuff Details

### Bleeding

| Property | Value |
|----------|-------|
| Trigger | Zombie hit (30% chance per hit) |
| Duration | 72,000 ticks (60 minutes) |
| Effect | −1 HP every 3 seconds per stack |
| Stacking | Yes — up to 3 stacks. Each stack adds another −1 HP/3 sec. |
| Cure | Wait for the timer to expire, or die and respawn. |

Bleeding is the most common debuff you'll deal with. Every time a zombie hits you, there's a 30% chance (configurable via `bleedingChance` in `survival.toml`) to gain a bleed stack. Stacks accumulate up to 3, and each stack independently deals 1 HP of damage every 3 seconds. At max stacks, that's 3 HP every 3 seconds — enough to slowly kill you if you don't eat or heal.

Bleeding stacks reset to 0 when the bleeding debuff timer expires. A fresh zombie hit while bleeding can add another stack and refresh the 60-minute timer to the longer remaining value.

All zombie types can cause bleeding on melee contact — see the [Zombie Bestiary](zombie_guide.md) for the full list.

---

### Infection (Stage 1)

| Property | Value |
|----------|-------|
| Trigger | Zombie hit (10% chance per hit) |
| Duration | 24,000 ticks (20 minutes) |
| Effect | −25% stamina regeneration |
| Stacking | No — cannot be applied if Stage 1 or Stage 2 is already active. |
| Cure | Wait for the timer to expire (clears naturally), or die and respawn. |

Infection Stage 1 is a warning phase. You won't take direct damage, but your stamina recovery is significantly reduced — regen rates drop by 25% while active. This makes sustained sprinting, mining, and combat noticeably harder.

The infection chance is configurable via `infectionBaseChance` in `survival.toml` (default 10%). You cannot gain a new Infection if you already have Stage 1 or Stage 2 active.

---

### Infection (Stage 2)

| Property | Value |
|----------|-------|
| Trigger | Applied separately (e.g., advanced infection source) when Stage 1 is not active |
| Duration | Persistent until cured or death |
| Effect | −0.5 HP per second |
| Stacking | No |
| Cure | Die and respawn (all debuffs clear on death). |

Stage 2 is the lethal phase. You take constant health drain at 0.5 HP per second. Without healing, this will kill you. Stage 2 represents a severe infection that demands immediate attention — you're significantly weakened in combat and unlikely to outrun danger.

---

### Sprain

| Property | Value |
|----------|-------|
| Trigger | Falling 4–7 blocks |
| Duration | 36,000 ticks (30 minutes) |
| Effect | −30% movement speed |
| Stacking | No — a Fracture (from a harder fall) replaces and overrides Sprain. |
| Cure | Wait for the timer to expire, or die and respawn. |

Sprains are the lighter of the two fall injuries. A 4-block fall is roughly 2 blocks higher than the safe fall distance in vanilla Minecraft. The 30% speed penalty makes you noticeably slower but still mobile — you can walk and sprint, just not as fast.

If you already have a Fracture, landing another 4–7 block fall will **not** downgrade it to a Sprain. The more severe debuff always takes priority.

---

### Fracture

| Property | Value |
|----------|-------|
| Trigger | Falling 8+ blocks |
| Duration | 72,000 ticks (60 minutes) |
| Effect | −60% movement speed, sprinting disabled |
| Stacking | No — replaces any active Sprain. |
| Cure | Wait for the timer to expire, or die and respawn. |

Fractures are brutal. A 60% speed reduction combined with the inability to sprint makes you nearly immobile. If you fracture a leg during a horde night, you're in serious trouble — you can't outrun anything, and your only option is to fight or find shelter immediately.

A Fracture automatically clears any existing Sprain when applied, since it's the more severe version of the same injury.

---

### Concussion

| Property | Value |
|----------|-------|
| Trigger | Being within 3 blocks of any explosion |
| Duration | 900 ticks (45 seconds) |
| Effect | Nausea (Minecraft's Confusion effect — screen wobble and distortion) |
| Stacking | No — re-application refreshes the timer if the new duration is longer. |
| Cure | Wait for the timer to expire (45 seconds), or die and respawn. |

Concussions disorient you with a swirling screen effect, making it difficult to aim and navigate. They're triggered by any explosion — TNT, Creepers, [Riot Husk](zombie_guide.md#riot-husk) death explosions, [Wrecking Husk](zombie_guide.md#wrecking-husk) detonations, and [Bloated Shambler](zombie_guide.md#bloated-shambler) death blasts all count.

If the explosion was caused by a zombie (Riot Husk or Wrecking Husk), you also receive the Stunned debuff on top of the Concussion.

---

### Stunned

| Property | Value |
|----------|-------|
| Trigger | Riot Husk acid projectile hit, or zombie-caused explosion within 3 blocks |
| Duration | 40 ticks (2 seconds) |
| Effect | Complete movement freeze — you cannot move or sprint |
| Stacking | No — shares a slot with Electrocuted. Only the longer-duration freeze is kept. |
| Cure | Wait 2 seconds for the timer to expire. |

Being stunned locks you in place completely. Your movement speed is reduced to zero and sprinting is forcibly cancelled. It lasts only 2 seconds, but that's more than enough time for nearby zombies to close in.

Stunned and Electrocuted are **mutually exclusive** — if you get hit by both sources in quick succession, only the one with the longer remaining duration stays active. They never stack.

The [Riot Husk's](zombie_guide.md#riot-husk) acid projectile triggers Stunned on hit. Zombie-caused explosions (from Riot Husks or [Wrecking Husks](zombie_guide.md#wrecking-husk)) also apply Stunned to players within the blast radius.

---

### Electrocuted

| Property | Value |
|----------|-------|
| Trigger | Melee hit from a [Charged zombie](zombie_guide.md#charged) |
| Duration | 30 ticks (1.5 seconds) |
| Effect | Complete movement freeze — you cannot move or sprint |
| Stacking | No — shares a slot with Stunned. Only the longer-duration freeze is kept. |
| Cure | Wait 1.5 seconds for the timer to expire. |

Electrocuted works identically to Stunned — total movement lockdown for a brief window. The difference is the source: [Charged zombies](zombie_guide.md#charged) apply Electrocuted on every melee hit, giving them a crowd-control ability that can pin you down in the middle of a fight.

Since Electrocuted and Stunned share a slot, getting hit by a Charged zombie while already Stunned from an explosion will only keep the longer freeze. They don't chain into extended lockdowns.

---

### Burn

| Property | Value |
|----------|-------|
| Trigger | Fire damage, [Infernal zombie](zombie_guide.md#infernal) contact |
| Duration | Varies by source |
| Effect | −2 HP per second (1 HP every 10 ticks) |
| Stacking | No |
| Cure | Wait for the timer to expire, extinguish fire, or die and respawn. |

Burn deals consistent fire damage for its duration. [Infernal zombies](zombie_guide.md#infernal) leave fire trails and ignite players on melee contact, making them a persistent burn source during late-game horde nights. Standing in fire or lava also triggers the Burn debuff.

At 2 HP per second, Burn kills quickly if you can't heal through it.

---

### Dysentery

| Property | Value |
|----------|-------|
| Trigger | Consuming contaminated food or water |
| Duration | Varies by source |
| Effect | Water drain ×3, food drain ×2 |
| Stacking | No |
| Cure | Wait for the timer to expire, or die and respawn. |

Dysentery massively accelerates your resource depletion. Your water drains at triple the normal rate and food at double, meaning your supplies will run out far faster than expected. In a desert biome where water already drains faster, dysentery can be a death sentence if you don't have reserves.

---

### Hypothermia

| Property | Value |
|----------|-------|
| Trigger | Prolonged exposure to cold environments (core temperature too low) |
| Duration | Persists while cold conditions continue |
| Effect | −20% movement speed, stamina drain ×2 |
| Stacking | No |
| Cure | Warm up (move to a warmer biome, stand near fire, go indoors). Dying and respawning also clears it. |

Hypothermia punishes you for staying in freezing environments without preparation. The 20% speed penalty stacks with other movement debuffs (like Sprain or Fracture), and doubled stamina drain means sprinting burns through your stamina reserve twice as fast. In snowy biomes at night, hypothermia can set in quickly.

The trigger and duration mechanics for Hypothermia are tied to the temperature system — it activates and persists based on your core temperature rather than a fixed timer.

---

### Hyperthermia

| Property | Value |
|----------|-------|
| Trigger | Prolonged exposure to hot environments (core temperature too high) |
| Duration | Persists while hot conditions continue |
| Effect | Water drain ×3 |
| Stacking | No |
| Cure | Cool down (move to a cooler biome, find shade, go underground). Dying and respawning also clears it. |

Hyperthermia triples your water consumption rate. In desert biomes where water already drains 1.5× faster, hyperthermia pushes you to dangerous dehydration levels very quickly. Keep water reserves stocked when exploring hot areas.

---

### Radiation

| Property | Value |
|----------|-------|
| Trigger | Exposure to [Radiated zombies](zombie_guide.md#radiated-day-28) |
| Duration | Varies by exposure |
| Effect | −1 HP every 5 seconds |
| Stacking | No |
| Cure | Wait for the timer to expire, or die and respawn. |

Radiation is a slow-burn damage debuff tied to [Radiated zombies](zombie_guide.md#radiated-day-28), which appear in horde nights starting day 28. The damage rate is low (1 HP per 5 seconds), but it adds up during prolonged fights against radiated hordes. Combined with Radiated zombies' 2× HP and health regeneration, these encounters are grueling endurance tests.

---

## General Tips

- **Bleeding is inevitable** — with a 30% chance per zombie hit, you'll get it constantly. Plan your healing around it rather than trying to avoid it entirely.
- **Kill Nurses first** — their healing aura keeps zombies alive longer, which means more hits on you and more chances for Bleeding and Infection. See the [Zombie Bestiary](zombie_guide.md#nurse) for details.
- **Watch your fall distance** — Sprain and Fracture have no RNG; they're guaranteed on the right fall height. A Fracture at the wrong moment (horde night, low supplies) can be a death sentence.
- **Avoid explosion clusters** — Concussion from one explosion is manageable. Concussion + Stunned from a Riot Husk or Wrecking Husk explosion while surrounded by zombies is not.
- **Charged zombies demand range** — Their guaranteed Electrocuted-on-hit means melee fighting a Charged zombie will get you frozen repeatedly. Use ranged weapons when possible. See [Charged zombie](zombie_guide.md#charged).
- **Temperature debuffs are environmental** — Hypothermia and Hyperthermia clear themselves when you fix the temperature problem. Carry water in deserts and stay warm in snow biomes.
- **Death is a valid cure** — All debuffs clear on respawn. If you're stacked with Bleeding, Infection Stage 2, and a Fracture, sometimes dying intentionally is the most practical option.

---

## Admin Command

| Command | What It Does |
|---------|-------------|
| `/bzhs cleardebuffs` | Instantly removes all active debuffs from the player who runs it. Requires permission level 2 (operator). Clears bleeding stacks, all debuff timers, movement speed penalties, and the nausea effect from Concussion. |

---

## Configuration

Debuff trigger chances are configurable in `survival.toml` under the `[survival.debuffs]` section:

| Setting | Default | Range | What It Does |
|---------|---------|-------|-------------|
| `bleedingChance` | 0.3 | 0.0 – 1.0 | Chance of Bleeding on each zombie melee hit (0.3 = 30%) |
| `infectionBaseChance` | 0.1 | 0.0 – 1.0 | Chance of Infection Stage 1 on each zombie hit (0.1 = 10%) |

Fall distances for Sprain (4 blocks) and Fracture (8 blocks) and explosion radius for Concussion (3 blocks) are not currently configurable — they're fixed values in the code.
