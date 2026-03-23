# Brutal Zombie Horde Survival — Crafting Guide

A complete guide to the crafting system, covering workstations, recipes, scrapping, quality tiers, and the materials that make it all work.

---

## How Crafting Works

Crafting in BZHS is fundamentally different from vanilla Minecraft. The standard 3×3 crafting grid is replaced with a **4×4 grid** (both in inventory and at crafting tables), and most useful items require a dedicated **workstation** to craft. Raw materials are gathered through mining, scrapping, looting, and farming, then processed through a chain of workstations to produce weapons, armor, food, medicine, and building materials.

Items you craft (and find) come in **quality tiers** ranging from Poor to Legendary, which affect their stats, durability, and mod slot count. The quality you can craft depends on your skill level, and the quality you find in loot depends on your **Loot Stage** — a number calculated from your player level, days survived, biome, and perks.

---

## Quick-Reference: Workstations

| Workstation | Unlock Requirement | Fuel | Input Slots | Output Slots | Key Recipes |
|-------------|-------------------|------|-------------|--------------|-------------|
| **Campfire** (vanilla block) | Available from start | Wood, Coal | 3 | 1 | Boiled Water Bottle, Grilled Meat, Charred Meat, basic teas |
| **Grill** | Master Chef 2 | Wood, Coal, Gas | 3 | 1 | Stews, Corn Bread, Chili Dog, Beer, advanced meals |
| **Workbench** | Available (basic) / Adv Engineering 3 (advanced) | None | 4 | 4 | Tools, weapons, armor, mods, ammo |
| **Forge** | Advanced Engineering 1 | Wood, Coal | 3 | 3 | Smelt ores → ingots, forge items |
| **Cement Mixer** | Advanced Engineering 2 | Electricity or fuel | 2 | 2 | Cobblestone → Concrete Mix → Concrete Blocks |
| **Chemistry Station** | Physician 1 | None | 4 | 4 | Advanced meds, gunpowder, dyes, acid, gas, polymer |
| **Advanced Workbench** | Advanced Engineering 5 | Requires electricity (no fuel slot) | 6 | 6 | Tier 5 weapons, turrets, vehicle parts |

---

## Workstation Details

### Campfire (Vanilla Block)

| Property | Value |
|----------|-------|
| Unlock | Default — available immediately |
| Fuel | Wood, Coal (vanilla campfire fuel) |
| Slots | 3 input, 1 output, 1 fuel |

The Campfire uses the **vanilla Minecraft campfire block** — you do not craft a custom workstation. Place a vanilla campfire, light it, and right-click it to open the BZHS crafting UI. The campfire handles basic cooking and water purification — two things you need constantly in the early game.

Right-clicking a lit campfire with a Shovel, Flint and Steel, or Fire Charge preserves vanilla behavior (extinguishing, relighting, etc.) and does not open the crafting UI. Soul campfires are not used by BZHS.

**Key recipes:**

| Recipe | Ingredients | Effect |
|--------|-------------|--------|
| Boiled Water Bottle | Murky Water Bottle + Glass Bottle | Safe drinking water (+20 hydration) |
| Grilled Meat | Raw Meat (any) | +20 fullness, −3 hydration |
| Charred Meat | Raw Meat (quick cook) | +12 fullness, −5 hydration, 10% food poisoning risk |
| Goldenrod Tea | Goldenrod + Boiled Water Bottle | +25 hydration, cures Dysentery |
| Red Tea | Chrysanthemum + Boiled Water Bottle | +20 hydration, +1 HP/sec for 60 sec |

**Water purification workflow:** Fill a glass bottle in water (vanilla mechanic) → the vanilla water bottle auto-converts to **Murky Water Bottle** in your inventory → cook Murky Water Bottle + Glass Bottle at the campfire to produce **Boiled Water Bottle**. No Glass Jars are needed.

The Campfire generates heatmap activity (+1 heat/min, capped at +10, 2-chunk radius) while lit and running. Keep this in mind during long cooking sessions — extended use can attract zombie scouts.

---

### Grill

| Property | Value |
|----------|-------|
| Unlock | Master Chef 2 |
| Fuel | Wood, Coal, Gas |
| Slots | 3 input, 1 output, 1 fuel |

The Grill is the Campfire's upgrade. It unlocks advanced recipes that provide better fullness, hydration, and temporary stat buffs. The Grill also accepts Gas as fuel in addition to Wood and Coal.

**Key recipes:**

| Recipe | Ingredients | Effect |
|--------|-------------|--------|
| Corn Bread | Corn Meal + Egg + Water | +25 fullness, +5 hydration |
| Vegetable Stew | Potato + Corn + Mushroom + Boiled Water Bottle | +35 fullness, +15 hydration, +20% stamina regen (5 min) |
| Meat Stew | Raw Meat + Potato + Corn + Boiled Water Bottle | +45 fullness, +15 hydration, +1 HP/sec (5 min) |
| Blueberry Pie | Blueberry + Corn Meal + Egg + Animal Fat | +30 fullness, +5 hydration, +10% XP (10 min) |
| Chili Dog | Grilled Meat + Corn Bread + Canned Chili | +50 fullness, +5 hydration, +30% stamina regen |
| Sham Sandwich | Sham (canned) + Corn Bread + Mushroom | +35 fullness, +5 hydration |
| Coffee | Coffee Beans + Boiled Water Bottle | +2 fullness, +10 hydration, +50% stamina regen (5 min) |
| Beer | Hops + Corn Meal + Boiled Water Bottle (ferment 30 min) | +5 fullness, +15 hydration, +10% melee damage / −5% accuracy (5 min) |

Meat Stew and Chili Dog are the best food items in the game for sustained survival. Coffee is essential for any extended building, mining, or combat session due to the massive stamina regen boost.

---

### Workbench

| Property | Value |
|----------|-------|
| Unlock | Default (basic) / Advanced Engineering 3 (advanced recipes) |
| Fuel | None — hand-crafted |
| Slots | 4 input, 4 output |

The Workbench is the central crafting station for tools, weapons, armor, weapon mods, and ammunition. It requires no fuel. Basic recipes are available immediately, but most useful items require the Advanced Engineering perk tree to unlock.

**Key recipe categories:**

**Ammo:**

| Ammo Type | Materials | Yield |
|-----------|-----------|-------|
| 9mm Round | Bullet Casing + Bullet Tip (lead) + Gunpowder | ×8 |
| 7.62mm Round | Casing + Tip + Gunpowder (×2) | ×6 |
| Shotgun Shell | Casing + Buckshot (lead) + Gunpowder | ×4 |
| .44 Magnum Round | Casing + Large Tip + Gunpowder (×2) | ×4 |
| AP (Armor Piercing) variant | Standard ammo + Steel Tip | ×4 |
| HP (Hollow Point) variant | Standard ammo + Soft Lead Tip | ×4 |

**Note:** Arrows (×5) and Bolts (×3) can be crafted directly from inventory without a Workbench. Blunderbuss Ammo (Gunpowder + Small Stones + Paper, ×1) is also an inventory recipe.

**Tools and Weapons:** The Workbench produces everything from Stone Axes and Wooden Clubs to Sledgehammers, Machetes, Hunting Knives, and all firearms (Pistol, SMG, Shotgun, Hunting Rifle, AK-47, etc.). Higher-tier weapons require higher perk levels. See the spec's weapon tables (§12.1, §12.2) for the full list.

**Armor:** All armor sets from Cloth through Steel are crafted at the Workbench. Each set has 5 pieces (Head, Chest, Legs, Boots, Gloves).

The Workbench is also where **scrapping** is most efficient — scrapping at a Workbench gives full yield, while scrapping from inventory gives only 50%.

---

### Forge

| Property | Value |
|----------|-------|
| Unlock | Advanced Engineering 1 |
| Fuel | Wood, Coal |
| Slots | 3 input, 3 output, 1 fuel |

The Forge smelts raw ores into usable ingots and produces forged components. It has an internal buffer system — raw materials are smelted into a molten metal reserve, then recipes draw from that reserve to produce finished items.

**Forge UI layout:**
- **Input slots**: Raw materials (iron, lead, clay, stone, brass, sand)
- **Fuel slot**: Wood, Coal
- **Tool slot**: Anvil (for iron items), Crucible (for steel — requires clay + forged iron to craft)
- **Output**: Ingots and forged items

**Forge recipes:**

| Recipe | Input | Output | Smelt Time |
|--------|-------|--------|------------|
| Iron Ingot | Scrap Iron ×3 | Iron Ingot ×1 | 20 sec |
| Steel Ingot | Iron Ingot ×1 + Clay ×1 (requires Crucible) | Forged Steel ×1 | 40 sec |
| Lead Ingot | Raw Lead ×3 | Lead Ingot ×1 | 15 sec |
| Bullet Casing | Brass ×1 | Casing ×6 | 10 sec |
| Iron Arrow Head | Iron Ingot ×1 | Arrow Head ×10 | 15 sec |
| Forged Iron Blade | Iron Ingot ×3 | Blade ×1 | 30 sec |
| Nails | Iron Ingot ×1 | Nails ×12 | 10 sec |
| Anvil | Forged Iron ×10 | Anvil ×1 (forge tool) | 120 sec |

The Forge generates significant heatmap activity (+3 heat/min, capped at +30, 4-chunk radius) while running. Extended forging sessions will attract unwanted attention.

---

### Cement Mixer

| Property | Value |
|----------|-------|
| Unlock | Advanced Engineering 2 |
| Fuel | Electricity or combustible fuel |
| Slots | 2 input, 2 output, 1 fuel |

The Cement Mixer converts raw stone materials into concrete building blocks. It's essential for mid-to-late game base building when you need to upgrade past Cobblestone.

**Process chain:**
1. Cobblestone → Concrete Mix (at Cement Mixer)
2. Concrete Mix → Concrete Blocks (at Cement Mixer)
3. Concrete blocks can then be upgraded in-world to Reinforced Concrete and Steel using the block upgrade system

**Building upgrade path for reference:**
```
Wood Frame → Wood Block → Reinforced Wood
  ↓
Cobblestone → Concrete → Reinforced Concrete → Steel
```

---

### Chemistry Station

| Property | Value |
|----------|-------|
| Unlock | Physician 1 |
| Fuel | None |
| Slots | 4 input, 4 output |

The Chemistry Station produces advanced medicine, explosives components, and refined materials. It's gated behind the Physician perk, making it a mid-game unlock.

**Key recipe categories:**

- **Medicine**: Advanced antibiotics, first aid kits, painkillers, Rad-Away pills
- **Gunpowder**: Required component for all ammunition — crafted from Nitrate + Coal
- **Gas**: Refined from Oil Shale (5 Oil Shale → 1 Gas Can = 100 fuel units)
- **Ethanol**: Alternative fuel from Corn (×5 → 1 Ethanol Can = 80 units)
- **Acid**: Used in trap crafting and advanced chemistry recipes
- **Polymer**: Refined from Oil Shale — used in advanced weapons and vehicle parts
- **Dyes**: Cosmetic block coloring
- **Rockets**: Pipe + Gunpowder (×5) + Duct Tape + Chemistry Materials → ×1 Rocket

---

### Advanced Workbench

| Property | Value |
|----------|-------|
| Unlock | Advanced Engineering 5 |
| Fuel | None (requires electricity connection to operate) |
| Slots | 6 input, 6 output |

The Advanced Workbench is the end-game crafting station. Unlike fuel-burning workstations, it has no fuel slot — instead, it must be connected to a power source (Generator or Solar Bank) via the electricity system. You won't access this until you've invested heavily in the Advanced Engineering perk tree.

**What it unlocks:**
- Tier 5 weapons (top craftable quality before Legendary loot)
- Auto Turrets and SMG Turrets
- Vehicle parts and modifications
- Advanced weapon mods

---

## Scrapping System

Any item can be scrapped to recover component materials. You can scrap items in two ways:

1. **At a Workbench** — full yield (100%)
2. **From inventory** — reduced yield (50%)

Always scrap at a Workbench when possible. The 50% inventory penalty means you lose half your potential materials.

### Scrap Yields by Category

| Item Category | Scrap Output (Workbench) | Notes |
|---------------|-------------------------|-------|
| **Tools** | Iron Scrap (×2–6) + Mechanical Parts (×0–1, 30% chance) | Pickaxes, axes, shovels, hoes, swords of all material tiers |
| **Armor** | Leather (×1–2) + String (×3–5) | Leather, iron, chainmail, and diamond armor pieces |
| **Electronics** | Electrical Parts (×1–3) + Polymer (×0–1, 50% chance) | Redstone, redstone torches, repeaters, comparators |
| **Canned food** | Iron Scrap (×0–1, 50% chance) | Cooked beef, porkchop, chicken, mutton |
| **Everything else** | Iron Scrap (×1–2) | Default fallback for any uncategorized item |

The spec also defines scrapping for additional categories not yet in the current code:

| Item Category (Spec §6.4) | Scrap Output |
|---------------------------|-------------|
| **Guns** | Gun Parts (×1–3) + Spring (×1) + Scrap Iron (×2) |
| **Vehicles** | Mechanical Parts (×5–15) + Engine Parts (×1–3) + Iron (×10+) |

**Inventory scrapping** applies a ×0.5 multiplier to all yields. For example, scrapping an iron tool at a Workbench gives 2–6 Iron Scrap, but scrapping the same tool from inventory gives only 1–3.

---

## Quality Tiers

All craftable and lootable gear comes in one of 6 quality tiers. Quality affects damage output, durability, armor rating, tool harvest amounts, and the number of modification slots available.

| Tier | Name | Color | Stat Multiplier | Mod Slots | How to Obtain |
|------|------|-------|-----------------|-----------|---------------|
| 1 | Poor | Gray | ×0.70 | 1 | Craft with any skill level |
| 2 | Good | Orange | ×0.85 | 1 | Craft with Skill 2+ |
| 3 | Great | Yellow | ×1.00 | 2 | Craft with Skill 4+ |
| 4 | Superior | Green | ×1.15 | 2 | Craft with Skill 6+ at a workstation |
| 5 | Excellent | Blue | ×1.30 | 3 | Craft with Skill 8+ at an advanced workstation |
| 6 | Legendary | Purple | ×1.50 | 4 | Loot and quest rewards only — **not craftable** |

**What the stat multiplier affects:**
- **Weapons**: Base damage is multiplied. A Poor iron tool at ×0.70 deals 30% less damage than its base stat, while a Legendary version at ×1.50 deals 50% more.
- **Armor**: Armor rating is multiplied by the same factor.
- **Tools**: Harvest yield per swing scales with the multiplier.
- **Durability**: Items last proportionally longer at higher tiers.

**Mod slots** determine how many modifications (scopes, grips, magazine upgrades, etc.) you can attach to the item. A Poor weapon has 1 mod slot; a Legendary has 4.

Legendary (Tier 6) items cannot be crafted at any workstation. They only come from high Loot Stage loot containers and quest rewards. If you find one, it's a significant upgrade over anything you can make yourself.

---

## Loot Stage and Crafting Quality

**Loot Stage** is a per-player number that determines the quality tier of items you find in loot containers. It's calculated using this formula:

```
lootStage = floor((playerLevel × 0.5) + (daysSurvived × 0.3) + (biomeBonus) + (looterPerkBonus))
```

### Loot Stage Thresholds

| Loot Stage | Max Quality Tier | Typical Items Found |
|------------|-----------------|---------------------|
| 1–4 | Poor (T1) | Stone tools, primitive bows, cloth armor, basic food |
| 5–10 | Good (T2) | Improved stone/iron tools, basic firearms parts |
| 11–25 | Great (T3) | Iron tools, pistols, leather armor, antibiotics |
| 26–50 | Superior (T4) | Steel tools, rifles, military armor, vehicle parts |
| 51–100 | Excellent (T5) | Automatic weapons, power armor, rare schematics |
| 100+ | Legendary (T6) | Legendary items, full schematic sets |

### How Loot Stage Factors Work

- **Player Level** (×0.5 weight): Each level adds 0.5 to your Loot Stage. Level 20 contributes 10 points.
- **Days Survived** (×0.3 weight): Each day adds 0.3. Surviving 30 days contributes 9 points.
- **Biome Bonus**: Higher-tier biomes give a flat bonus — Pine Forest +0, Forest +5, Desert +10, Snow +10, Burned Forest +15, Wasteland +25. Looting in dangerous biomes rewards better gear.
- **Looter Perk Bonus**: The Lucky Looter perk tree adds flat bonuses to your Loot Stage. *(Note: the perk bonus term exists in the formula but is currently set to 0 in code — this will be activated when the perk system is fully implemented.)*

### Quality Roll for Loot

When loot is generated, the game determines your maximum quality tier from your Loot Stage, then rolls a random tier within a range of up to 2 tiers below that maximum. For example, at Loot Stage 30 (max tier: Superior), you might find Great, Good, or Superior items — but not Poor or Excellent.

This means higher Loot Stage doesn't guarantee top-tier items every time, but it raises both the floor and ceiling of what you can find.

---

## Core Materials Reference

The mod introduces 17 key materials used across all crafting systems. Here's where to find them and what they're used for.

| Material | Source | Primary Uses |
|----------|--------|-------------|
| **Iron Scrap** | Mining iron ore, scrapping tools/items | Smelting into Iron Ingots at the Forge |
| **Iron Ingot** | Forge (smelt Iron Scrap ×3) | Tools, weapons, forged components, building upgrades |
| **Lead** | Mining lead ore | Bullet tips, ammo crafting |
| **Nitrate** | Mining nitrate ore, desert surface deposits | Gunpowder (at Chemistry Station), farm fertilizer |
| **Coal** | Mining coal ore, harvesting trees | Forge/Campfire fuel, gunpowder component |
| **Oil Shale** | Mining in desert biome | Gas and Polymer (refined at Chemistry Station) |
| **Clay** | Digging soil/dirt | Forge recipes, Cement Mixer |
| **Sand** | Desert biome, riverbeds | Cement |
| **Murky Water Bottle** | Fill glass bottle in water (auto-converts from vanilla water bottle) | Boiled Water Bottle (cook at Campfire with Glass Bottle) |
| **Mechanical Parts** | Scrapping tools (30% chance), loot | Weapons, vehicles, workstation crafting |
| **Electrical Parts** | Scrapping electronics (×1–3), loot | Electricity system components, turrets, Stun Baton |
| **Duct Tape** | Craft (cloth + glue) or loot | Repairs, Plaster Casts (fracture cure), various recipes |
| **Forged Iron** | Forge (from Iron Ingots) | Building upgrades (Reinforced Concrete → Steel path), Anvil |
| **Forged Steel** | Forge (Iron Ingot ×1 + Clay ×1, requires Crucible) | Steel building tier, advanced tools and weapons |
| **Acid** | Chemistry Station, Riot Husk drops | Chemistry recipes, trap crafting |
| **Polymer** | Chemistry Station (from Oil Shale), scrapping electronics (50% chance) | Advanced weapons, vehicle parts |
| **Survivor's Coins** | Safes, quest rewards, selling to traders | Currency — used for all trader purchases (stacks to 50,000) |

### Material Acquisition Tips

- **Iron** is everywhere — mine ore veins, scrap found tools and weapons, loot Working Stiff crates in hardware stores.
- **Lead and Nitrate** are the ammo bottleneck. Lead comes from mining; Nitrate is found both underground and on desert surfaces. Prioritize these once you start using firearms.
- **Oil Shale** is desert-exclusive. You need it for Gas (vehicle fuel) and Polymer (advanced crafting). Plan desert expeditions or set up a desert outpost.
- **Mechanical Parts** are rare and valuable. They drop from scrapping tools (30% chance) and from Feral Wraith kills. Don't scrap them — you need them for workstations and vehicles.
- **Water bottles replace Glass Jars** — fill a glass bottle in water (vanilla mechanic) and it auto-converts to Murky Water Bottle. Cook Murky Water Bottle + Glass Bottle at a campfire to get Boiled Water Bottle. No Glass Jars needed.
- **Forged Steel** requires the Crucible tool in the Forge, which itself costs Forged Iron ×10 + Clay. Plan this upgrade early in mid-game.

---

## Tips

- **Place a Campfire immediately** — purifying water and cooking meat are your top survival priorities on day 1. Place a vanilla campfire, light it, and right-click it to open the BZHS crafting UI. You need purified water to avoid Dysentery and cooked food to keep your fullness up.
- **Rush Advanced Engineering 1** for the Forge. Smelting iron opens up most of the crafting tree.
- **Always scrap at a Workbench** — the 50% inventory penalty adds up fast. Carry junk items back to base rather than scrapping them in the field.
- **Quality tier 3 (Great) is the baseline** — at ×1.00 stat multiplier, it's the "normal" version of any item. Tiers 1–2 are below standard; tiers 4–5 are above. Don't invest heavily in crafting early-game Poor items when you'll replace them soon.
- **Legendary items can't be crafted** — if you find a Tier 6 item, treasure it. They only come from loot at Loot Stage 100+ or quest rewards.
- **The Forge generates heat** — running the Forge adds +3 heat/min to your chunk (capped at +30), which can attract Banshees and mini-hordes. Forge in batches and let the heat decay between sessions, or accept the combat risk.
- **Gas requires planning** — you need Oil Shale (desert mining) refined at a Chemistry Station (Physician 1 perk). Vehicles are useless without fuel, so unlock the Chemistry Station before building your first vehicle.
- **Cement Mixer unlocks concrete** — the jump from Cobblestone (500 HP) to Concrete (1500 HP) is massive for base defense. Prioritize this before your first horde night if possible.

---

## Farming System

The farming system provides a renewable source of food and water, essential for long-term survival.

### Farm Plot Block

Created by using a hoe on dirt or grass blocks. Farm Plots are permanent tilled soil that does not revert — unlike vanilla farmland. All crops must be planted on Farm Plot blocks.

### Crop Block

Crops grow through 4 stages (AGE 0-3) via randomTick growth. The Green Thumb perk can speed up growth. Harvest mature crops (AGE 3) to collect produce and seeds for replanting.

### Dew Collector

A passive water generation device. When placed with sky access, the Dew Collector generates a Murky Water Bottle every 6000 ticks (~5 real minutes). It has 4 output slots and its own GUI. Essential for desert bases or locations far from water sources.

### Seed Items

Seeds are right-click-to-plant items that can only be placed on Farm Plot blocks. They grow into the corresponding crop over time.

---

## Power / Electricity System

The electricity system powers advanced defense blocks and the Advanced Workbench.

### Generator Bank

| Property | Value |
|----------|-------|
| Fuel | Gas Can |
| Burn Time | 6000 ticks per fuel unit |
| Output | 100W |

A fuel-powered generator. Place a Gas Can in the fuel slot to generate power. Wire connections link the generator to powered devices.

### Battery Bank

| Property | Value |
|----------|-------|
| Storage | 1000 EU max |
| Role | Energy storage + redistribution |

Charges from connected power sources (Generator or Solar Panel) and discharges to connected powered devices. Acts as a buffer to keep devices running when generators cycle.

### Solar Panel

| Property | Value |
|----------|-------|
| Output | 30W (daytime only) |
| Requirement | Sky visibility |

Generates power during daytime with direct sky access. No fuel needed — place and connect to devices or battery banks.

### Wire Connections

Right-click a power source with Electrical Parts, then right-click a powered device to create a wire connection. Wire connections are tracked by the PowerGridManager SavedData and persist across server restarts.

### Powered Devices

| Device | Effect | Power Required |
|--------|--------|----------------|
| Blade Trap | 6 dmg AoE to nearby entities | Yes |
| Electric Fence Post | 5 dmg + stun on contact | Yes |
| Advanced Workbench | End-game crafting station | Yes |

---

## Building & Defense System

### Upgradeable Block

A 6-tier block progression upgraded via right-click with a repair hammer:

| Tier | Block | Materials to Upgrade |
|------|-------|---------------------|
| 1 | Wood Frame | (starting block) |
| 2 | Reinforced Wood | Wood Planks |
| 3 | Cobblestone | Cobblestone |
| 4 | Concrete | Concrete Mix (from Cement Mixer) |
| 5 | Reinforced Concrete | Forged Iron |
| 6 | Steel | Forged Steel |

### Defensive Blocks

| Block | Damage | Durability | Notes |
|-------|--------|-----------|-------|
| Wood Spikes | 4 | 10 hits | Contact damage, degrades on hit |
| Iron Spikes | 8 | 20 hits | Contact damage, degrades on hit |
| Blade Trap | 6 (AoE) | — | Requires power, damages nearby entities |
| Electric Fence Post | 5 + stun | — | Requires power, contact damage + stun effect |

### Land Claim Block

Places a 41-block protection radius that prevents zombie spawns within the area. Limited to one per player.

---

## Armor System

Three armor tiers with movement and stealth trade-offs:

| Tier | Name | Protection | Movement Modifier | Set Bonus (2pc) | Set Bonus (4pc) |
|------|------|-----------|-------------------|----------------|----------------|
| Light | Padded | Low | None | 50% noise reduction | 100% noise reduction |
| Medium | Scrap Iron | Medium | Slight speed reduction | 10% stamina regen | 20% stamina regen |
| Heavy | Military | High | Moderate speed reduction | 12% damage reduction | 25% damage reduction |

Each set has 4 pieces (Helmet, Chestplate, Leggings, Boots). Wearing pieces from different tiers triggers a mixed-set warning.
