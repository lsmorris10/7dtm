# If Bored — Edge Cases to Test & Debug

A go-to checklist of known edge cases, tricky interactions, and things worth testing across every major system in the mod. Pick a section, fire up a test world, and start poking.

---

## Zombie Combat & AI

- Spawn all 16 zombie types one at a time and confirm none crash the game
- Spawn each modifier variant (Radiated, Charged, Infernal) on every eligible base type (Walker, Crawler, Feral Wight, Cop, Soldier) — verify stat multipliers apply correctly (HP x2/x1.8, Damage x1.3–1.5, Speed boosts)
- Radiated zombie regen: does 2 HP/sec regen actually work? Can you out-damage it?
- Charged zombie chain lightning: does it actually arc to 3 nearby entities? Does the 1.5s stun (Electrocuted debuff) apply to all hit targets?
- Infernal zombie fire trail: do fire blocks actually spawn behind it as it walks? Is the zombie itself fire-immune?
- Nurse healing aura: does it heal other zombies within 5 blocks at 5 HP/sec? What happens with multiple Nurses stacking heals on one target?
- Bloated Walker death explosion: does the 2-block radius work? Can the explosion trigger a Demolisher's chest-hit explosion if one is nearby?
- Demolisher chest-hit explosion: does hitting it in the chest (vs. head/legs) actually trigger the 8-block explosion? What counts as a "chest hit"?
- Cop acid spit: does the ranged bile attack work? Does the Cop explode at <20% HP?
- Screamer summoning: does the shriek spawn 4–8 zombies? Does the 3-summon cap work, or can it keep screaming forever?
- Spider Zombie wall climbing: can it scale vertical surfaces? Do overhangs stop it? Does it get stuck on ceilings?
- Feral Wight permanent sprint: does it always run regardless of day/night? Does `setSprinting(true)` every tick cause animation glitches?
- Zombie Dog pack spawning: do they actually spawn in groups? Is their 3.5 speed correct and not absurdly fast?
- Vulture flight: does the swoop attack work? Can it path through solid blocks? Does it get stuck in terrain?
- Behemoth ground pound: does the 6-block AoE radius work? Does it deal 75% of its base damage (37.5)? Does knockback apply?
- Night speed bonus: do all non-horde zombies get +50% speed between tick 13000–23000? Does it turn off at dawn?
- Zombie block destruction: do zombies bash through walls? Do they prefer weaker materials (wood over concrete) via the A* cost formula?

---

## Player Survival Stats

- Stamina exhaustion cycle: drain stamina to 0 → confirm sprinting is blocked → confirm it only re-enables at 40% (not immediately when stamina ticks above 0)
- Stamina regen rates: 8/sec at rest, 4/sec while walking — verify these are accurate
- Stamina costs: sprint (-10/s), melee swing (-12), mining (-5), jump (-8) — test each individually
- Food drain: base -0.2/min, doubles while sprinting/mining — verify the multiplier kicks in and turns off
- Water drain: base -0.3/min, x1.5 in hot biomes — test in desert vs. forest
- Starvation cascade: at <30% food/water → stamina regen halved; at <10% → -0.5 HP/sec; at 0% → -2.0 HP/sec and -40% move speed
- Health regen: only triggers when both food AND water are >50% — test with food at 60% but water at 40% (should NOT regen)
- Health max scaling: does max HP increase by 10 per level, capping at 200?
- Death/respawn reset: on death, do stats reset to 50% food/water and 100% stamina? Are ALL debuffs cleared?

---

## Debuffs & Status Effects

- Bleeding: 30% chance on zombie hit, -1 HP every 3 sec, lasts 1 hour — does it stack up to 3x (adding duration)?
- Infection Stage 1 → Stage 2 transition: does untreated Stage 1 (stamina regen -25%) auto-progress to Stage 2 (-0.5 HP/sec) after 1 MC day?
- Infection Stage 2 → Death: does Stage 2 kill the player after its duration expires?
- Dysentery from murky water: does water drain x3 and food drain x2 apply? Duration = 60 min?
- Burn debuff: -2 HP/sec for 10s active + 20s linger — is the linger period at reduced damage or same?
- Radiation: -1 HP every 5 sec + reduces MAX health by 1 every 30s — does max health actually decrease? Does it recover after leaving the zone?
- Fracture vs. Sprain: fall 4–7 blocks = Sprain (-30% speed, 30 min); fall 8+ blocks = Fracture (-60% speed, no sprint, 60 min) — does Fracture override Sprain?
- Hypothermia: triggers below 32°F, stamina drain x2, movement -20% — clears above 50°F (not 32°F, there's a hysteresis gap)
- Hyperthermia: triggers above 110°F, water drain x3, stamina drain x1.5 — clears below 100°F
- Electrocuted (Charged zombie): no movement for 1.5s + 5 HP burst — does the stun actually freeze the player?
- Stunned (Cop/Demolisher): no movement for 2s — does this stack with Electrocuted if both hit at once?
- Multiple debuffs simultaneously: apply Bleeding + Infection + Dysentery + Fracture all at once — does the game handle the combined drain without crashing or going negative on stats?

---

## Temperature System

- Biome temperature variation: does Pine Forest feel different from Desert and Snowy Tundra?
- Night temperature drop: -10°F at night — does it transition smoothly or jump?
- Altitude effect: -1°F per 10 blocks above Y=64 — go to Y=200 and check if you're freezing
- Temperature convergence rate: ±0.3°F/sec toward ambient — is this smooth or jerky?
- Clothing insulation: do armor pieces / clothing actually modify temperature? (Check if implemented)
- Fire proximity: does standing near a campfire/forge warm the player?

---

## Blood Moon & Horde

- Does the Blood Moon trigger exactly every 7 days (configurable)?
- Warning at 20:00 the day before — does the chat message appear?
- Sky turns red at 18:00 on horde day — visual check
- Horde spawning starts at 22:00, waves every 10 min until 04:00 — verify timing
- Dawn burn at 23500: do all surviving horde zombies within 128 blocks ignite?
- Sleep blocking: can the player use a bed during Blood Moon? Should get a custom error message
- Horde scaling formula: `baseCount * (1 + cycle * difficultyMultiplier)^1.2` — does wave size actually grow on day 14, 21, 28?
- Late-game composition: do Ferals, Cops, Demolishers, and modifier variants start appearing in later Blood Moons?
- Spawn distance: do horde zombies spawn 24–40 blocks from the player? Not on top of them or 200 blocks away?
- Persistence: do horde zombies have `setPersistenceRequired`? They shouldn't despawn mid-fight

---

## Heatmap System

- Gunshots add +15 heat — fire a gun and check if scouts start showing up
- Forge running adds +3/min — leave a forge on for a while and see what happens
- Mining adds +0.5 per block — mine a big area and watch for Screamers
- Thresholds: 25 = scouts, 50 = guaranteed Screamer, 75 = mini-horde (8–12), 100 = continuous waves every 90s
- Does heat decay over time when the player stops doing noisy things?
- Screamer scream adds +40 heat — can a Screamer scream escalate heat to 100 and cause a cascade?

---

## Structural Integrity & Block Physics

- Build a long horizontal bridge out of wood — does it collapse after spanning more than 3 blocks unsupported?
- Steel vs. wood span limits — steel should support much longer spans
- Cascade collapse: break one support pillar under a large structure — does the whole thing come down?
- Zombie-caused collapse: can zombies bashing a support column cause a building to collapse on them (and on the player)?
- Block HP by material: Wood (100), Cobblestone (?), Concrete (?), Steel (5000) — verify values
- Collapse delay: is there a 0.5s delay before unsupported blocks fall?

---

## Loot & Progression

- Loot Stage formula: `(Level + DaysSurvived) * BiomeMultiplier` — does loot quality actually improve over time?
- Biome multipliers: Pine Forest (1.0), Desert (1.2), Snowy (1.5), Wasteland (2.5) — loot in Wasteland should be noticeably better
- Container loot: do POI containers (safes, cabinets) roll quality tiers 1–6 based on loot stage?

---

## Cross-System Interactions (The Fun Ones)

- Radiated Nurse: does it heal itself AND nearby zombies while regenerating 2 HP/sec on its own?
- Infernal zombie in a wooden base: does the fire trail set the base on fire and cause structural collapse?
- Charged zombie hitting a player with Fracture: movement is already -60%, then Electrocuted stuns for 1.5s — do the effects layer correctly?
- Bloated Walker exploding during Blood Moon near other zombies — does it damage the horde or just players?
- Screamer during Blood Moon: can a Screamer spawn EXTRA zombies on top of the normal horde waves?
- Demolisher explosion destroying support blocks: does it trigger structural integrity collapse?
- Heatmap + Blood Moon overlap: if heat is at 100 during Blood Moon, do you get double spawns?
- Starvation + Bleeding + Infection: all three drain HP — does the player die in seconds? Are the drain rates additive?
- Temperature extremes during Blood Moon: player is already stressed by the horde, then Hypothermia kicks in — does the stamina drain x2 make combat impossible?
- Death loop: if the player dies and respawns in a dangerous area (Wasteland, active Blood Moon), can they stabilize or do they instantly die again?
