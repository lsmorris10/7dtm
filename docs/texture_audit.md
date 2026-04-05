# Texture Audit Report

**Generated**: March 17, 2026
**Updated**: March 22, 2026
**Method**: Files under 200 bytes classified as placeholder (auto-generated colored squares). Files 200+ bytes classified as real pixel art.

**Note**: 9x9 pixel status icons (hearts, armor, food, water) contain real pixel art with outlines and shading but naturally compress under 200 bytes due to the small canvas size. These 16 icons are counted as placeholder by the byte-size method but are functional pixel art — not auto-generated colored squares.

## Summary

| Category | Total | Real | Placeholder | % Complete |
|----------|-------|------|-------------|------------|
| Item textures | 247 | 44 | 203 | 17.8% |
| Block textures | 95 | 48 | 47 | 50.5% |
| GUI textures | 89 | 34 | 55 | 38.2% |
| Effect/debuff textures | 13 | 13 | 0 | 100.0% |
| **Total** | **444** | **139** | **305** | **31.3%** |

### Changes Since Last Audit (March 22, 2026)
- **+41 new real item textures this update**: 12 melee weapons, 15 ranged weapons (ak47 was already real before this update), 14 ammo types
- **+4 new real block textures this update**: campfire_station, cement_mixer, chemistry_station, forge_station (other new block textures added by separate work)
- **+27 new real GUI textures this update**: 4 HUD panel backgrounds, 5 stat icons, 6 bar textures, 6 quality tier icons, 5 attribute icons, 1 heart icon (heart_half at exactly 200 bytes)
- **+16 hand-drawn 9x9 icons this update**: hearts (4), armor (3), food (5), water (5) — real pixel art but under 200-byte threshold due to small canvas size
- **+13 new real debuff/effect icons this update**: bleeding, burn, concussion, dysentery, electrocuted, fracture, hyperthermia, hypothermia, infection_1, infection_2, radiation, sprain, stunned
- **Minimap frame**: rendered procedurally in `MinimapOverlay.java` (no texture file needed)
- **Blood moon overlay**: rendered procedurally in `BloodMoonSkyRenderer.java` (no texture file needed)

---

## Real Textures (139 total — no action needed)

### Item (44)

#### Weapons — Melee (12)
- `baseball_bat.png`, `fists.png`, `hunting_knife.png`, `iron_spear.png`
- `machete.png`, `nailgun.png`, `sledgehammer.png`, `steel_knuckles.png`
- `stone_axe.png`, `stun_baton.png`, `wooden_club.png`, `wrench.png`

#### Weapons — Ranged (16)
- `ak47.png`, `blunderbuss.png`, `compound_bow.png`, `compound_crossbow.png`
- `hunting_rifle.png`, `m60.png`, `pipe_pistol.png`, `pipe_rifle.png`
- `pipe_shotgun.png`, `pistol.png`, `primitive_bow.png`, `rocket_launcher.png`
- `shotgun.png`, `smg.png`, `wooden_bow.png`, `repair_hammer.png`

#### Ammo (14)
- `44_magnum_round.png`, `762mm_round.png`, `9mm_round.png`
- `ap_ammo.png`, `arrow.png`, `blunderbuss_ammo.png`, `bolt.png`
- `burning_shaft.png`, `explosive_arrow.png`, `fire_arrow.png`
- `hp_ammo.png`, `junk_turret_dart.png`, `rocket_ammo.png`, `shotgun_shell.png`

#### Other (2)
- `sniper_rifle.png`, `large_storage_rack.png`

### Block (48)
- `advanced_workbench.png`, `animal_trough.png`, `ash_block.png`, `asphalt.png`
- `broken_truck.png`, `burnt_car.png`, `camper_body.png`, `campfire_station.png`
- `cement_mixer.png`, `chemistry_station.png`, `chicken_coop.png`, `cobblestone_custom.png`
- `commercial_metal.png`, `concrete_block.png`, `concrete_sidewalk.png`, `corrugated_iron.png`
- `dirt_road.png`, `drywall.png`, `farm_crate.png`, `farm_plot.png`
- `forge.png`, `forge_station.png`, `gravel_custom.png`, `grill.png`
- `lead_ore.png`, `nitrate_ore.png`, `oil_shale_ore.png`
- `rebar_concrete.png`, `reinforced_concrete.png`, `reinforced_wood.png`
- `sand_custom.png`, `snow_layer_custom.png`, `solar_panel.png`
- `steel_block.png`, `steel_plate.png`, `tool_crate.png`
- `truck_bed.png`, `vehicle_body.png`, `vehicle_body_charred.png`
- `vehicle_roof.png`, `vehicle_wheel.png`, `vehicle_window.png`
- `vending_machine_front_lower.png`, `vending_machine_front_upper.png`
- `wood_block.png`, `wood_frame.png`, `workbench.png`, `wrecked_camper.png`

### GUI (34)
- `ammo_counter_bg.png`, `compass_strip.png`, `day_counter_bg.png`
- `food_icon.png`, `frost_overlay.png`, `fuel_icon.png`
- `health_bar_bg.png`, `health_bar_fill.png`, `heart_half.png`, `heat_indicator.png`
- `horde_timer_bg.png`
- `quality_excellent.png`, `quality_good.png`, `quality_great.png`
- `quality_legendary.png`, `quality_poor.png`, `quality_superior.png`
- `stamina_bar_bg.png`, `stamina_bar_fill.png`
- `temperature_icon.png`, `vehicle_hud_bg.png`
- `vignette_bleeding.png`, `vignette_burn.png`, `vignette_electrocuted.png`
- `vignette_infection.png`, `vignette_radiation.png`
- `water_icon.png`, `xp_bar_bg.png`, `xp_bar_fill.png`
- `attribute_agility.png`, `attribute_fortitude.png`, `attribute_intellect.png`
- `attribute_perception.png`, `attribute_strength.png`

### Effect/Debuff (13)
- `bleeding.png`, `burn.png`, `concussion.png`, `dysentery.png`
- `electrocuted.png`, `fracture.png`, `hyperthermia.png`, `hypothermia.png`
- `infection_1.png`, `infection_2.png`, `radiation.png`, `sprain.png`, `stunned.png`

### GUI — Real Art Under 200 Bytes (16, not counted in "Real" column)
- `heart_full.png`, `heart_empty.png`, `heart_low.png`
- `armor_full.png`, `armor_half.png`, `armor_empty.png`
- `food_full.png`, `food_half.png`, `food_empty.png`, `food_low.png`, `food_half_low.png`
- `water_full.png`, `water_half.png`, `water_empty.png`, `water_low.png`, `water_half_low.png`

---

## Placeholder Textures Needing Replacement (305 total)

### Item Textures — Weapon Mods (15)
- `barrel_extender.png`, `bullet_casing.png`, `bullet_tip.png`
- `drum_magazine.png`, `ergonomic_grip.png`, `extended_magazine.png`
- `flashlight_mod.png`, `laser_sight.png`, `magazine.png`
- `paint_job.png`, `reflex_sight.png`, `scope_4x.png`, `scope_8x.png`
- `silencer.png`, `weighted_head.png`

### Item Textures — Armor & Armor Mods (42)
- `cloth_boots.png`, `cloth_chestplate.png`, `cloth_gloves.png`, `cloth_helmet.png`, `cloth_leggings.png`
- `iron_boots.png`, `iron_chestplate.png`, `iron_gloves.png`, `iron_helmet.png`, `iron_leggings.png`
- `leather_boots.png`, `leather_chestplate.png`, `leather_gloves.png`, `leather_helmet.png`, `leather_leggings.png`
- `military_boots.png`, `military_chestplate.png`, `military_gloves.png`, `military_helmet.png`, `military_leggings.png`
- `power_armor_boots.png`, `power_armor_chestplate.png`, `power_armor_gloves.png`, `power_armor_helmet.png`, `power_armor_leggings.png`
- `scrap_boots.png`, `scrap_chestplate.png`, `scrap_gloves.png`, `scrap_helmet.png`, `scrap_leggings.png`
- `steel_boots.png`, `steel_chestplate.png`, `steel_gloves.png`, `steel_helmet.png`, `steel_leggings.png`
- `armor_plating.png`, `heavy_armor_plating.png`
- `insulation_mod.png`, `mobility_mod.png`, `padded_mod.png`, `plating_mod.png`, `pocket_mod.png`, `spiked_mod.png`, `cooling_mesh.png`

### Item Textures — Food & Drink (30)
- `aloe.png`, `blueberry.png`, `chrysanthemum.png`, `coffee_beans.png`, `corn.png`
- `goldenrod.png`, `hops.png`, `mushroom.png`, `potato.png`, `pumpkin.png`, `super_corn.png`
- `charred_meat.png`, `corn_bread.png`, `grilled_meat.png`, `meat_stew.png`, `vegetable_stew.png`
- `blueberry_pie.png`, `chili_dog.png`, `sham_sandwich.png`
- `canned_cat_food.png`, `canned_chili.png`, `canned_dog_food.png`, `canned_pasta.png`, `canned_sham.png`
- `beer.png`, `coffee_cup.png`, `goldenrod_tea.png`, `red_tea.png`
- `egg.png`, `honey.png`

### Item Textures — Medical (11)
- `aloe_cream.png`, `antibiotics.png`, `bandage.png`, `first_aid_bandage.png`, `first_aid_kit.png`
- `herbal_antibiotics.png`, `painkillers.png`, `plaster_cast.png`, `splint.png`, `vitamins.png`
- `rad_away.png`

### Item Textures — Materials & Resources (42)
- `acid.png`, `animal_fat.png`, `animal_hide.png`, `bear_hide.png`, `bone.png`
- `clay.png`, `clay_lump.png`, `cloth.png`, `coal.png`, `concrete_mix.png`
- `duct_tape.png`, `duke_coin.png`, `dukes_casino_token.png`, `electrical_parts.png`
- `empty_can.png`, `ethanol_can.png`, `feather.png`, `fertilizer.png`
- `forged_iron.png`, `forged_steel.png`, `gas_can.png`, `glass_jar.png`
- `glass_jar_boiled_water.png`, `gunpowder.png`
- `iron_ingot.png`, `iron_scrap.png`, `lead.png`, `lead_ingot.png`, `leather.png`
- `mechanical_parts.png`, `nails.png`, `nitrate.png`, `oil_shale.png`
- `plant_fiber.png`, `polymer.png`, `rebar_frame.png`, `sand.png`
- `scrap_iron.png`, `scrap_plastic.png`, `spider_gland.png`, `venom_sac.png`

### Item Textures — Seeds (10)
- `aloe_seed.png`, `blueberry_seed.png`, `chrysanthemum_seed.png`, `coffee_beans_seed.png`
- `corn_seed.png`, `goldenrod_seed.png`, `hops_seed.png`, `mushroom_seed.png`
- `potato_seed.png`, `pumpkin_seed.png`

### Item Textures — Raw Meat (5)
- `raw_chicken.png`, `raw_meat.png`, `raw_pork.png`, `raw_rabbit.png`, `raw_venison.png`

### Item Textures — Vehicles (17)
- `gyrocopter_chassis.png`, `handlebars.png`, `headlight.png`, `headlight_upgrade.png`
- `medium_engine.png`, `nos_tank.png`, `offroad_tires.png`, `reinforced_wheels.png`
- `rotor.png`, `small_engine.png`, `structural_brace.png`, `truck_chassis.png`
- `v8_engine.png`, `vehicle_battery.png`, `vehicle_chassis.png`, `vehicle_seat.png`, `wheel.png`

### Item Textures — Other (6)
- `junk_turret.png`, `repair_kit.png`, `schematic.png`, `storage_rack.png`
- `turret_mount.png`, `wire_tool.png`

### Block Textures (47)

#### Workstations & Containers
- `bookshelf_container.png`, `cardboard_box.png`
- `desk.png`, `filing_cabinet.png`
- `gun_safe.png`, `kitchen_cabinet.png`, `medicine_cabinet.png`, `munitions_box.png`
- `refrigerator.png`, `supply_crate.png`, `trash_pile.png`

#### Traps & Defense
- `barbed_wire.png`, `blade_trap.png`, `bulletproof_glass.png`, `dart_trap.png`
- `electric_fence_post.png`, `iron_spikes.png`, `pressure_plate_custom.png`
- `shotgun_trap.png`, `trip_wire.png`, `wood_spikes.png`

#### Electrical
- `auto_turret.png`, `battery_bank.png`, `generator_bank.png`, `grow_light.png`
- `land_claim_block.png`, `logic_gate_and.png`, `logic_gate_or.png`, `motion_sensor.png`
- `powered_door.png`, `smg_turret.png`, `speaker.png`, `spotlight.png`
- `switch.png`, `timer.png`, `wind_turbine.png`, `wire_relay.png`
- `dew_collector.png`

#### Decorative
- `brick.png`, `campfire.png`, `car_wreck.png`

### GUI Textures (55)

#### Perk Icons (39)
- `advanced_engineering.png`, `archery.png`, `better_barter.png`, `bold_explorer.png`
- `brawler.png`, `campfire_cook.png`, `charismatic_nature.png`, `deep_cuts.png`
- `deep_striker.png`, `deep_veins.png`, `demolitions_expert.png`, `electrocutioner.png`
- `field_medic.png`, `flurry_of_blows.png`, `gearhead.png`, `green_thumb.png`
- `gunslinger.png`, `healing_factor.png`, `heavy_armor.png`, `iron_fists.png`
- `iron_gut.png`, `keen_scavenger.png`, `light_armor.png`, `lock_picking.png`
- `nightstalker.png`, `pack_mule.png`, `pain_tolerance.png`, `parkour.png`
- `physician.png`, `rifle_guy.png`, `robotics_inventor.png`, `rule1_cardio.png`
- `run_and_gun.png`, `shadow_strike.png`, `skull_crusher.png`, `spear_master.png`
- `treasure_hunter.png`, `unstoppable_force.png`, `well_insulated.png`

#### Status Icons Under 200 Bytes (16)
- `heart_full.png`, `heart_empty.png`, `heart_low.png`
- `armor_full.png`, `armor_half.png`, `armor_empty.png`
- `food_full.png`, `food_half.png`, `food_empty.png`, `food_low.png`, `food_half_low.png`
- `water_full.png`, `water_half.png`, `water_empty.png`, `water_low.png`, `water_half_low.png`

---

## Priority Recommendations

### High Priority (visible in normal gameplay)
1. ~~**Heart/armor/food/water icons**~~ ✅ Done (16 icons with pixel art)
2. **Core material items** (iron_ingot, forged_iron, duct_tape, etc.) — seen in inventory
3. ~~**Weapon items**~~ ✅ Done (28 weapons with pixel art)
4. ~~**Workstation blocks**~~ ✅ Done (4 workstation blocks replaced)

### Medium Priority
5. **Food items** (canned goods, cooked meat, drinks) — common inventory items
6. **Medical items** (bandage, first_aid_kit, antibiotics) — survival essentials
7. ~~**Ammo types**~~ ✅ Done (14 ammo textures with pixel art)
8. **Armor pieces** — equipped items

### Low Priority (future content / rarely seen)
9. **Vehicle parts** — vehicles not yet implemented
10. **Weapon mods** — mod system not yet implemented
11. **Perk icons** — only seen in perk menu
12. **Seeds** — farming not yet implemented
