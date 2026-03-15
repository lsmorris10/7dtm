# If Bored — Edge Cases to Test & Debug

A go-to checklist of known edge cases, tricky interactions, and things worth testing across every major system in the mod. Pick a section, fire up a test world, and start poking.

---

## Zombie Combat & AI

- Spawn all 16 base zombie types one at a time and confirm none crash the game (Walker, Crawler, Frozen Lumberjack, Bloated Walker, Spider Zombie, Feral Wight, Cop, Screamer, Zombie Dog, Vulture, Demolisher, Mutated Chuck, Zombie Bear, Nurse, Soldier, Behemoth)
- Spawn each modifier variant (Radiated, Charged, Infernal) on every eligible base type (Walker, Crawler, Feral Wight, Cop, Soldier) — verify stat multipliers apply correctly:
  - Radiated: HP ×2.0, Damage ×1.5, Speed ×1.3
  - Charged: HP ×1.8, Damage ×1.3, Speed ×1.2
  - Infernal: HP ×1.8, Damage ×1.4, Speed ×1.1
- Radiated zombie regen: does 2 HP/sec regen actually work? Can you out-damage it?
- Charged zombie chain lightning: does it actually arc to 3 nearby entities (configurable)? Chain targets take 5.0 `chargedChainDamage` via lightningBolt damage and get `setTicksFrozen(30)` (vanilla freeze), which is separate from the player-specific `DEBUFF_ELECTROCUTED` applied via `LivingEntityHurtMixin`
- Infernal zombie fire trail: do fire blocks actually spawn behind it as it walks (every 20 ticks)? Is the zombie itself fire-immune?
- Nurse healing aura: does it heal other zombies within 5 blocks at 5 HP/sec? What happens with multiple Nurses stacking heals on one target?
- Mutated Chuck vomit attack: does the ranged vomit reach 11 blocks? (Uses SmallFireball projectile, no custom debuff application — only contact damage)
- Frozen Lumberjack: does it spawn correctly? Verify 150 HP / 12 damage / 0.9 speed base stats
- Bloated Walker death explosion: does the 2-block radius work? Can the explosion trigger a Demolisher's chest-hit explosion if one is nearby?
- Demolisher chest-hit explosion: does hitting it in the chest (vs. head/legs) actually trigger the 8-block explosion? What counts as a "chest hit"?
- Cop acid spit: does the ranged bile attack work at 8-block range? (Uses SmallFireball projectile — `copBileDamage` config exists but is not wired to the projectile; damage comes from the SmallFireball itself.) Does the Cop explode at <20% HP? Does it also explode on death?
- Screamer summoning: does the shriek spawn 4–8 zombies? Does the 3-summon cap work, or can it keep screaming forever?
- Spider Zombie wall climbing: can it scale vertical surfaces? Do overhangs stop it? Does it get stuck on ceilings?
- Feral Wight permanent sprint: does it always run regardless of day/night? Does `setSprinting(true)` every tick cause animation glitches?
- Zombie Dog pack spawning: do they actually spawn in groups? Is their 3.5 speed correct and not absurdly fast?
- Vulture flight: does the swoop attack work? Can it path through solid blocks? Does it get stuck in terrain?
- Behemoth ground pound: does the 6-block AoE radius work? Does it deal 75% of its base damage (37.5)? Does knockback apply?
- Darkness speed bonus: do zombies get +125% speed when both block light AND sky light ≤ 7? Does the same +125% bonus apply at night (tick 26000–46000)? When both conditions are true, verify the max of the two bonuses is used (not additive)
- Zombie block destruction: do zombies bash through walls? Do they prefer weaker materials (wood over concrete) via the A* cost formula?

---

## Player Survival Stats

- Base health: confirm player starts with 100 HP (50 hearts). There is NO per-level max HP scaling — health is a flat 100 HP
- Health regen: 0.5 HP/sec, only when BOTH food AND water are >50% — test with food at 60% but water at 40% (should NOT regen)
- Vanilla damage scaling: all vanilla environmental damage sources (fall, drown, fire, lava, cactus, etc.) are scaled ×5 to match the 100 HP pool. Player-dealt damage is also scaled ×5
- Stamina exhaustion cycle: drain stamina to 0 → confirm sprinting is blocked → confirm it only re-enables at 40% (not immediately when stamina ticks above 0)
- Stamina regen rates: 8/sec at rest, 4/sec while walking — verify these are accurate
- Stamina costs: sprint (-10/s), melee swing (-12), mining (-5), jump (-8) — test each individually
- Food drain: base -0.2/min, doubles (×2.0) while sprinting/mining — verify the multiplier kicks in and turns off
- Water drain: base -0.3/min, ×1.5 in hot biomes (ambient temp >85°F) — test in desert vs. forest
- Starvation cascade: at <30% food/water → stamina regen halved; at <10% → -0.5 HP/sec; at 0% → -2.0 HP/sec and -40% move speed
- Death/respawn reset: on death, do stats reset to 50% food/water and 100% stamina? Are ALL debuffs cleared? (Yes — `clearAllDebuffs` runs on death)

---

## Debuffs & Status Effects

### Debuffs with active trigger paths

- Bleeding: 30% chance on zombie hit, -1 HP every 3 sec per stack, max 3 stacks (additional hits add stacks and refresh remaining duration up to 72000 ticks max), lasts 72000 ticks (1 hour)
- Infection Stage 1: 10% base chance on zombie hit, stamina regen -25%, lasts 48000 ticks (1 game day = 40 min real time). Applied only if neither infection_1 nor infection_2 is already active
- Fracture vs. Sprain: fall 4–7 blocks = Sprain (-30% speed, 30 min); fall 8+ blocks = Fracture (-60% speed, no sprint, 60 min) — does Fracture override Sprain? (In code, Sprain modifier is only applied when Fracture is NOT active)
- Concussion: explosion within 3 blocks applies 45s (900 ticks) of Nausea. Verify the screen wobble effect triggers
- Electrocuted (Charged zombie melee hit on player): no movement for 1.5s (30 ticks) — does the stun actually freeze the player? (Note: the 5 HP `chargedChainDamage` is applied to chain-lightning targets via a separate code path, not as part of the Electrocuted debuff itself)
- Stunned (Cop/Demolisher explosion): no movement for 2s (40 ticks) — Stunned and Electrocuted share the same movement-freeze slot (only the longer duration applies; the other is removed)

### Debuffs with effects coded but no automatic trigger path yet

These debuffs have their effects fully implemented in `PlayerStatsHandler.applyDebuffEffects()` and are listed in `KNOWN_DEBUFF_IDS`, but no code currently applies them automatically. They can be tested by applying them via network sync or future implementation:

- Infection Stage 2: -0.5 HP/sec effect is coded, but no automatic Stage 1 → Stage 2 progression exists. When applied, does the drain work correctly?
- Dysentery: water drain ×3 and food drain ×2 effects are coded, but no trigger (e.g., murky water item) applies this debuff yet
- Burn: damage-over-time effect is coded (1 HP per 10 ticks), but no in-game source currently applies this debuff
- Radiation: -1 HP every 100 ticks effect is coded, but no radiation zone or source applies this debuff
- Hypothermia: stamina drain + -20% movement speed effects are coded, but no temperature threshold triggers application of this debuff
- Hyperthermia: extra water drain effect is coded, but no temperature threshold triggers application of this debuff

### General debuff behavior

- Debuffs cleared on death: all 13 known debuffs (bleeding, infection_1, infection_2, dysentery, sprain, fracture, concussion, burn, hypothermia, hyperthermia, radiation, electrocuted, stunned) are removed via `clearAllDebuffs` on death
- Multiple debuffs simultaneously: apply several debuffs at once — does the game handle the combined drain without crashing or going negative on stats?

---

## Temperature System

- Biome temperature variation: does Pine Forest feel different from Desert and Snowy Tundra?
- Night temperature drop: -10°F at night (tick 26000–46000) — does it transition smoothly or jump?
- Altitude effect: -1°F per 10 blocks above Y=64 — go to Y=200 and check if you're freezing
- Temperature convergence rate: ±0.3°F/sec toward ambient — is this smooth or jerky?
- Clothing insulation: do armor pieces / clothing actually modify temperature? (Check if implemented)
- Fire proximity: does standing near a campfire/forge warm the player?

---

## Blood Moon & Horde

- Does the Blood Moon trigger exactly every 7 days (configurable)?
- Warning at tick 28000 the day before — does the chat message appear?
- Sky turns red at tick 24000 on horde day — visual check
- Siren plays at tick 25000 — audio check
- Horde spawning starts at tick 32000, 4 waves with 10-min (600 sec) intervals — verify timing
- Final wave forced at tick 44000 — verify it fires even if regular waves haven't finished (note: final-wave forcing may preempt regular interval wave progression depending on timing)
- Dawn burn at tick 47000: do all surviving horde zombies within 128 blocks ignite?
- Wave size escalation: +25% per wave index (wave 1 = base, wave 2 = ×1.25, wave 3 = ×1.5, wave 4 = ×1.75)
- Wave composition thresholds:
  - Day 7: Walkers (70%) + Crawlers (20%) + Ferals (10%, but only if day ≥ 14 via config — otherwise redistributed to Walkers)
  - Day 14: adds Cops (feralDay config = 14 enables Ferals and Cops)
  - Day 21: adds Demolishers (demolisherDay = 21) and Charged/Infernal modifier variants (chargedDay/infernalDay = 21)
  - Day 28+: Radiated modifier variants begin appearing
- Sleep blocking: can the player use a bed during Blood Moon? Should get "You cannot sleep during a Blood Moon!" message
- Horde scaling formula: `baseCount * (1 + cycle * difficultyMultiplier)^1.2` — does wave size actually grow on day 14, 21, 28?
- Spawn distance: do horde zombies spawn 24–40 blocks from the player? Not on top of them or 200 blocks away?
- Persistence: do horde zombies have `setHordeMob(true)`? They shouldn't despawn mid-fight

---

## Heatmap System

- Block breaking adds +0.5 heat (radius 3) — mine a big area and watch for Screamers
- Torch placement adds +2.0 heat (radius 1) — place several torches and check if scouts show up
- Explosions add +25.0 heat (radius 6) — set off TNT and see the heat spike
- Sprinting adds +0.2/sec (radius 2) — sprint around for a while and monitor heat
- Thresholds: 25 = scouts, 50 = guaranteed Screamer, 75 = mini-horde (8–12), 100 = continuous waves every 90s
- Does heat decay over time when the player stops doing noisy things?
- Screamer scream behavior: scream spawns 4–8 walkers directly (not via heatmap), with a 3-scream cap and 600-tick cooldown. Note: screaming does NOT add heat — it spawns zombies independently of the heatmap system
- Test `/bzhs heat` command to view current chunk heat
- Test `/bzhs heat_clear` command to reset heat in the area
- Heatmap spawning is disabled during active Blood Moons — verify no overlap

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

- Loot Stage formula: `(PlayerLevel * 0.5) + (DaysSurvived * 0.3) + BiomeBonus + PerkBonus` — does loot quality actually improve over time?
- Biome bonuses: Wasteland/Badlands +25, Burnt Forest/Charred +15, Desert +10, Snow/Frozen/Ice +10, Forest +5
- Loot container types: trash piles, gun safes, munitions boxes, supply crates — verify each uses loot stage for quality rolls
- Container loot: do POI containers roll quality tiers 1–6 based on loot stage?
- Loot container respawn: 5-day default timer — verify containers re-roll loot after 5 in-game days

---

## XP, Leveling & Perks

- XP from zombie kills: verify each zombie type awards its XP value (e.g., Walker 200, Feral Wight 350, Behemoth 2000). Modifier zombies add their modifier's XP reward on top
- XP from mining: verify block hardness determines XP (≤0.5 = 1 XP, ≤2.0 = 2 XP, ≤5.0 = 3 XP, ≤10.0 = 4 XP, >10.0 = 5 XP)
- XP-to-level formula: `1000 * (level ^ 1.05)` — verify XP requirements increase per level
- Perk point per level: every level-up grants 1 perk point
- Bonus attribute point every 10 levels: at level 10, 20, 30, etc., earn 1 extra attribute point
- Test key perks:
  - Healing Factor (Fortitude): +20% health regen per rank (5 ranks)
  - Pain Tolerance (Fortitude): -10% damage taken per rank (5 ranks)
  - Unkillable (Fortitude mastery, requires attribute level 10): survive fatal hit with 1 HP + 10 sec invulnerability, 60-min cooldown
  - Parkour (Agility): -25% fall damage, +0.5 jump height at rank 4 (4 ranks)
  - Pack Mule (Strength): +10 slots carry capacity per rank (4 ranks)
  - Miner 69er (Strength): +15% mining speed per rank (5 ranks)
  - Sexual Tyrannosaurus (Strength): -15% stamina cost on power attacks per rank (4 ranks)
  - Rule 1: Cardio (Fortitude): +10% stamina regen + 5% sprint speed per rank (3 ranks)
- Test all 5 attributes: Strength, Perception, Fortitude, Agility, Intellect (levels 1–10)
- Mastery perks (require attribute level 10): Titan (STR), Eagle Eye (PER), Unkillable (FOR), Ghost (AGI), Mastermind (INT)

---

## Crafting & Workstations

- Test all workstation types: Campfire (fuel, 3 in / 1 out), Grill (fuel, 3 in / 1 out), Workbench (no fuel, 4 in / 4 out), Forge (fuel, 3 in / 3 out), Cement Mixer (fuel, 2 in / 2 out), Chemistry Station (no fuel, 4 in / 4 out), Advanced Workbench (no fuel, 6 in / 6 out)
- Forge smelting: does it consume fuel and process recipes correctly? (Iron Scrap → Iron Ingot, Iron Ingot → Forged Iron, Sand + Clay → Glass Jar ×3)
- Campfire/Grill cooking: do food recipes produce correct output with fuel consumption? (Beef → Cooked Beef, Porkchop → Cooked Porkchop, Chicken → Cooked Chicken, Mutton → Cooked Mutton, Cod → Cooked Cod, Salmon → Cooked Salmon, Potato → Baked Potato)
- Fuel-based stations only: `serverTick` only processes crafts for stations with `usesFuel() = true` (Campfire, Grill, Forge, Cement Mixer). Workbench, Chemistry Station, and Advanced Workbench do NOT auto-process — verify if manual/non-fuel crafting is implemented or if these stations are UI-only
- Scrapping at workbench: verify 100% yield multiplier (full scrap return)
- Scrapping manually (without workbench): verify 50% yield multiplier (half scrap return)
- Quality tiers: `QualityTier.java` defines quality tiers for loot drops based on loot stage. Note: there is currently no crafted-item quality pipeline tied to loot stage or perks — quality tiers apply only to looted items

---

## HUD & UI

- Compass direction accuracy: verify N/NE/E/SE/S/SW/W/NW labels align with actual facing direction. Compass uses `(mcYaw + 180) % 360` for bearing — verify this is correct
- Heat indicator: does the pulsing flame icon appear next to the compass when chunk heat exceeds 25? Does it show the numeric heat value?
- Minimap terrain rendering: does the minimap sample terrain colors from block MapColor? Does it update within 1 second / 8 blocks of movement?
- Minimap rotation: does the minimap rotate with player yaw?
- Minimap player dot: white dot centered, with directional indicator pointing forward. Other players shown as colored dots with names
- Stats bars: Food (orange), Water (blue), Stamina (green), HP (red), XP (purple) — each turns to a darker/warning color when below 30%
- Debuff list display: active debuffs shown with ID and remaining duration countdown in seconds
- Day counter display: "Day: X | Lvl: Y" shown above the stat bars in gold text
- Temperature display: shows current core temp in °F with color coding (blue = cold <50°F, green = normal, red = hot >90°F)

---

## Cross-System Interactions (The Fun Ones)

- Radiated Nurse: does it heal itself AND nearby zombies while regenerating 2 HP/sec on its own?
- Infernal zombie in a wooden base: does the fire trail set the base on fire and cause structural collapse?
- Charged zombie hitting a player with Fracture: movement is already -60%, then Electrocuted stuns for 1.5s — do the effects layer correctly? (Stunned/Electrocuted share a freeze slot, so both shouldn't stack)
- Bloated Walker exploding during Blood Moon near other zombies — does it damage the horde or just players?
- Screamer during Blood Moon: Screamer scream spawning is independent of heatmap (direct spawn via `performScream()`). A Screamer CAN spawn extra zombies during Blood Moon since scream behavior is not gated by heatmap state — verify if this creates unintended double-spawning
- Demolisher explosion destroying support blocks: does it trigger structural integrity collapse?
- Heatmap + Blood Moon overlap: heatmap spawner skips ticking when Blood Moon is active — verify no double spawns
- Starvation + Bleeding + Infection Stage 1: starvation drains HP while infection reduces stamina regen — does the player degrade quickly?
- Temperature extremes during Blood Moon: hypothermia/hyperthermia effects are coded but not yet auto-triggered by temperature — once trigger paths are added, test if stamina drain during horde makes combat impossible
- Death loop: if the player dies and respawns in a dangerous area (Wasteland, active Blood Moon), can they stabilize or do they instantly die again?
- Darkness speed in caves: zombies underground (block light ≤ 7, sky light ≤ 7) should get +125% speed even during daytime — verify caves are extra dangerous
- 48000-tick day cycle: verify all time-dependent systems use the custom DAY_LENGTH (48000) instead of vanilla's 24000
- 100 HP pool interactions: verify damage from all sources (zombies, fall, fire, starvation, debuffs) is balanced for the 100 HP pool
- Miner 69er + heatmap from mining: does the +15% mining speed per rank cause faster heat buildup (more blocks broken per minute)?
- Healing Factor during combat: does the +20% health regen per rank meaningfully offset damage taken in fights?
- Rule 1: Cardio sprint speed + heatmap: does the sprint speed bonus from Cardio perks generate heat faster due to more distance covered?
