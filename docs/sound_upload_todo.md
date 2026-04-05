# Sound Upload To-Do

This checklist tracks placeholder `.ogg` files that need to be replaced with real audio assets.

## Format & Path Requirements

- All files must be in **`.ogg` format**
- Place files at: `src/main/resources/assets/sevendaystominecraft/sounds/`
- File names must match exactly (no spaces, lowercase)

---

## Priority Sounds (Replace First)

These 5 sounds are heard earliest and most frequently in gameplay. Replace them in order.

- [ ] **1. `zombie_groan.ogg`**
  - Sound event: `sevendaystominecraft:zombie_groan`
  - Why high priority: Plays constantly while zombies are nearby — the most frequently heard sound in the game. A placeholder here immediately degrades immersion.

- [ ] **2. `gun_fire_9mm.ogg`**
  - Sound event: `sevendaystominecraft:gun_fire_9mm`
  - Why high priority: The 9mm pistol is the starting weapon. Every new player will hear this sound within their first few minutes of play.

- [ ] **3. `zombie_death.ogg`**
  - Sound event: `sevendaystominecraft:zombie_death`
  - Why high priority: Plays on every zombie kill, which is the core feedback loop of the game. A bad or missing sound here makes combat feel broken.

- [ ] **4. `block_break_zombie.ogg`**
  - Sound event: `sevendaystominecraft:block_break_zombie`
  - Why high priority: Heard whenever zombies destroy blocks during raids. This is a tension-critical moment and the sound significantly affects the threat feel.

- [ ] **5. `blood_moon_siren.ogg`**
  - Sound event: `sevendaystominecraft:blood_moon_siren`
  - Why high priority: Announces the Blood Moon event — the game's primary set piece. It plays once per cycle but is highly memorable and sets the tone for the entire night.

---

## Lower Priority Follow-Ups (Replace After)

These 3 sounds are less frequent or context-specific. Address them after the priority list above is complete.

- [ ] `gun_fire_ak47.ogg` — `sevendaystominecraft:gun_fire_ak47` — AK-47 is a mid-to-late game weapon, heard less often than the 9mm.
- [ ] `zombie_scream.ogg` — `sevendaystominecraft:zombie_scream` — Situational trigger (aggro or special zombie behavior), not constant background noise.
- [ ] `workstation_ambient.ogg` — `sevendaystominecraft:workstation_ambient` — Ambient loop at crafting stations; important for atmosphere but not combat-critical.
