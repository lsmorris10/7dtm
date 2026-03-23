import { useState } from "react";

const CATEGORIES: Record<string, { label: string; color: string; bg: string }> = {
  zombies: { label: "Zombies", color: "#ef4444", bg: "rgba(239,68,68,0.15)" },
  survival: { label: "Core Survival", color: "#22c55e", bg: "rgba(34,197,94,0.15)" },
  hud: { label: "HUD / UI", color: "#3b82f6", bg: "rgba(59,130,246,0.15)" },
  heatmap: { label: "Heatmap", color: "#f97316", bg: "rgba(249,115,22,0.15)" },
  debuffs: { label: "Debuffs", color: "#a855f7", bg: "rgba(168,85,247,0.15)" },
  daycycle: { label: "Day Cycle / Balance", color: "#eab308", bg: "rgba(234,179,8,0.15)" },
  loot: { label: "Loot & Crafting", color: "#14b8a6", bg: "rgba(20,184,166,0.15)" },
  xp: { label: "XP & Perks", color: "#06b6d4", bg: "rgba(6,182,212,0.15)" },
  weapons: { label: "Weapons", color: "#f43f5e", bg: "rgba(244,63,94,0.15)" },
  audio: { label: "Audio / Music", color: "#d946ef", bg: "rgba(217,70,239,0.15)" },
  biomes: { label: "Biomes", color: "#84cc16", bg: "rgba(132,204,22,0.15)" },
  landing: { label: "Landing Page", color: "#ec4899", bg: "rgba(236,72,153,0.15)" },
  docs: { label: "Documentation", color: "#94a3b8", bg: "rgba(148,163,184,0.15)" },
  git: { label: "Git / Repo", color: "#78716c", bg: "rgba(120,113,108,0.15)" },
  building: { label: "Building", color: "#b45309", bg: "rgba(180,83,9,0.15)" },
  electricity: { label: "Electricity", color: "#fbbf24", bg: "rgba(251,191,36,0.15)" },
  farming: { label: "Farming", color: "#16a34a", bg: "rgba(22,163,74,0.15)" },
  traders: { label: "Traders & Quests", color: "#0ea5e9", bg: "rgba(14,165,233,0.15)" },
  stealth: { label: "Stealth", color: "#6366f1", bg: "rgba(99,102,241,0.15)" },
  map: { label: "Map", color: "#2dd4bf", bg: "rgba(45,212,191,0.15)" },
};

interface Task {
  id: number;
  title: string;
  category: string;
  status: "merged" | "cancelled";
}

const ALL_TASKS: Record<string, Task[]> = {
  "March 12": [
    { id: 1, title: "Zombie renderer fix", category: "zombies", status: "merged" },
    { id: 2, title: "Zombie name tags", category: "zombies", status: "merged" },
    { id: 3, title: "Zombie HP display", category: "zombies", status: "merged" },
    { id: 4, title: "Zombie behavior docs", category: "docs", status: "merged" },
    { id: 5, title: "Heatmap system", category: "heatmap", status: "merged" },
    { id: 6, title: "Custom player stats", category: "survival", status: "merged" },
    { id: 7, title: "HUD stat bars", category: "survival", status: "merged" },
    { id: 8, title: "Heatmap guide docs", category: "docs", status: "merged" },
    { id: 9, title: "Edge case testing docs", category: "docs", status: "merged" },
    { id: 10, title: "Repo sync setup", category: "git", status: "merged" },
    { id: 11, title: "Git workflow config", category: "git", status: "merged" },
    { id: 12, title: "Git history cleanup", category: "git", status: "merged" },
    { id: 13, title: "Spec documentation", category: "docs", status: "merged" },
    { id: 14, title: "README updates", category: "docs", status: "merged" },
    { id: 15, title: "Heatmap docs update", category: "docs", status: "merged" },
  ],
  "March 13": [
    { id: 16, title: "Wall occlusion system", category: "zombies", status: "merged" },
    { id: 17, title: "Compass & minimap HUD", category: "hud", status: "merged" },
    { id: 18, title: "Sunburn fix for zombies", category: "zombies", status: "merged" },
    { id: 19, title: "Zombie guide update", category: "docs", status: "merged" },
    { id: 20, title: "Loot & crafting system", category: "loot", status: "merged" },
    { id: 21, title: "XP & perks system", category: "xp", status: "merged" },
    { id: 22, title: "Debuff system core", category: "debuffs", status: "merged" },
    { id: 23, title: "Stats HUD overlap fix", category: "hud", status: "merged" },
    { id: 24, title: "Night zombie speed", category: "zombies", status: "merged" },
    { id: 25, title: "Day cycle doubling", category: "daycycle", status: "merged" },
    { id: 26, title: "Debuff persistence fix", category: "debuffs", status: "merged" },
    { id: 27, title: "Debuffs guide & fix #2", category: "debuffs", status: "merged" },
    { id: 28, title: "Clear debuffs command", category: "debuffs", status: "merged" },
    { id: 29, title: "100 HP base health", category: "survival", status: "merged" },
    { id: 30, title: "Project notes update", category: "docs", status: "merged" },
    { id: 31, title: "Spec milestone docs", category: "docs", status: "merged" },
    { id: 32, title: "Cancelled task", category: "docs", status: "cancelled" },
    { id: 33, title: "Status docs update", category: "docs", status: "merged" },
    { id: 34, title: "Vanilla damage scaling", category: "survival", status: "merged" },
    { id: 35, title: "Day cycle sky fix", category: "daycycle", status: "merged" },
    { id: 36, title: "Darkness zombie speed", category: "docs", status: "merged" },
    { id: 37, title: "Coal vein nerf", category: "zombies", status: "merged" },
    { id: 38, title: "Coal balance docs", category: "docs", status: "merged" },
    { id: 39, title: "Gitignore update", category: "git", status: "merged" },
  ],
  "March 14": [
    { id: 40, title: "Landing page V1", category: "landing", status: "merged" },
    { id: 41, title: "Landing page publish", category: "landing", status: "merged" },
    { id: 42, title: "Cancelled task", category: "landing", status: "cancelled" },
    { id: 43, title: "BZHS rebrand", category: "landing", status: "merged" },
    { id: 44, title: "README restructure", category: "docs", status: "merged" },
    { id: 45, title: "Landing page V2", category: "landing", status: "merged" },
    { id: 46, title: "Gitignore public/", category: "git", status: "merged" },
    { id: 47, title: "README status section", category: "docs", status: "merged" },
    { id: 48, title: "Project notes update", category: "docs", status: "merged" },
    { id: 49, title: "Docs consolidation", category: "docs", status: "merged" },
    { id: 50, title: "Landing page polish", category: "landing", status: "merged" },
    { id: 51, title: "Spec updates", category: "docs", status: "merged" },
    { id: 52, title: "Download button", category: "landing", status: "merged" },
    { id: 53, title: "Formspree contact", category: "landing", status: "merged" },
    { id: 54, title: "Install tutorial", category: "landing", status: "merged" },
    { id: 55, title: "GitHub Releases API", category: "landing", status: "merged" },
    { id: 56, title: "Pre-release support", category: "landing", status: "merged" },
    { id: 57, title: "JAR rename branding", category: "landing", status: "merged" },
    { id: 58, title: "Download button fix", category: "landing", status: "merged" },
  ],
  "March 15": [
    { id: 59, title: "Mixin crash fix", category: "daycycle", status: "merged" },
    { id: 60, title: "Day cycle refactor", category: "daycycle", status: "merged" },
    { id: 61, title: "Mob damage scaling", category: "survival", status: "merged" },
    { id: 62, title: "POI zombie spec", category: "docs", status: "merged" },
    { id: 63, title: "Docs update", category: "docs", status: "merged" },
    { id: 64, title: "Tick refactor analysis", category: "daycycle", status: "merged" },
    { id: 65, title: "Blood moon tick constants", category: "daycycle", status: "merged" },
    { id: 66, title: "Dual zombie speed system", category: "zombies", status: "merged" },
    { id: 67, title: "Zombie speed config", category: "zombies", status: "merged" },
    { id: 68, title: "Zombie speed tests", category: "zombies", status: "merged" },
    { id: 69, title: "Landing page V3", category: "landing", status: "merged" },
    { id: 70, title: "Docs/guides/dashboard update", category: "docs", status: "merged" },
  ],
  "March 16": [
    { id: 71, title: "Zombie AI planning", category: "zombies", status: "merged" },
    { id: 72, title: "Zombie AI behavior tree", category: "zombies", status: "merged" },
    { id: 73, title: "Zombie variant config", category: "zombies", status: "merged" },
    { id: 74, title: "Zombie spawn tuning", category: "zombies", status: "merged" },
    { id: 75, title: "Zombie pathfinding fixes", category: "zombies", status: "merged" },
    { id: 76, title: "Gameplay bugfixes", category: "survival", status: "merged" },
    { id: 77, title: "Textures/models/blockstates", category: "hud", status: "merged" },
    { id: 78, title: "New world startup fixes", category: "survival", status: "merged" },
    { id: 79, title: "Zombie block breaking AI", category: "zombies", status: "merged" },
    { id: 80, title: "Minimap fix", category: "hud", status: "merged" },
    { id: 81, title: "HP display fix", category: "hud", status: "merged" },
    { id: 82, title: "Health rebalance", category: "daycycle", status: "merged" },
    { id: 83, title: "Combat rebalance", category: "daycycle", status: "merged" },
    { id: 84, title: "Item texture fixes", category: "hud", status: "merged" },
    { id: 85, title: "Container GUI fixes", category: "loot", status: "merged" },
  ],
  "March 17": [
    { id: 86, title: "Crafting recipe fixes", category: "loot", status: "merged" },
    { id: 87, title: "Workstation fuel logic", category: "loot", status: "merged" },
    { id: 88, title: "Block registry cleanup", category: "survival", status: "merged" },
    { id: 89, title: "Language file updates", category: "docs", status: "merged" },
    { id: 90, title: "Entity registration fixes", category: "zombies", status: "merged" },
    { id: 91, title: "Deprecated API fixes", category: "survival", status: "merged" },
    { id: 92, title: "Legacy config cleanup", category: "survival", status: "merged" },
    { id: 93, title: "Zombie AI special abilities", category: "zombies", status: "merged" },
    { id: 94, title: "Workstation recipe processing", category: "loot", status: "merged" },
    { id: 95, title: "Basic weapons system", category: "weapons", status: "merged" },
    { id: 96, title: "Weapon quality scaling", category: "weapons", status: "merged" },
    { id: 97, title: "Placeholder texture audit", category: "docs", status: "merged" },
    { id: 98, title: "Sound system foundation", category: "audio", status: "merged" },
    { id: 99, title: "HUD layout polish", category: "hud", status: "merged" },
    { id: 100, title: "Icon-based HUD", category: "hud", status: "merged" },
    { id: 101, title: "Texture processing tool", category: "docs", status: "merged" },
    { id: 102, title: "Territory POI system", category: "biomes", status: "merged" },
    { id: 103, title: "3D weapon animations (GeckoLib)", category: "weapons", status: "merged" },
    { id: 104, title: "Funding page", category: "landing", status: "merged" },
    { id: 105, title: "GeckoLib Jar-in-Jar bundling", category: "weapons", status: "merged" },
    { id: 106, title: "Sprint Mixin fix", category: "survival", status: "merged" },
    { id: 107, title: "Context-aware gameplay music", category: "audio", status: "merged" },
    { id: 108, title: "Magazine / Skill Book system", category: "xp", status: "merged" },
    { id: 109, title: "Skill Book item registration", category: "xp", status: "merged" },
    { id: 110, title: "Custom biome system", category: "biomes", status: "merged" },
    { id: 111, title: "Trademark name sweep", category: "zombies", status: "merged" },
    { id: 112, title: "Perk ID migration", category: "xp", status: "merged" },
    { id: 113, title: "Currency rename cleanup", category: "loot", status: "merged" },
    { id: 114, title: "Zombie name display update", category: "zombies", status: "merged" },
    { id: 115, title: "Docs/landing/dashboard update", category: "docs", status: "merged" },
  ],
  "March 18": [
    { id: 116, title: "Cancelled task", category: "docs", status: "cancelled" },
    { id: 117, title: "Perk icon renames", category: "xp", status: "merged" },
    { id: 118, title: "Registry crash fix", category: "survival", status: "merged" },
  ],
  "March 19": [
    { id: 119, title: "Stereo-to-mono sound fix", category: "audio", status: "merged" },
    { id: 120, title: "HUD hotbar spacing", category: "hud", status: "merged" },
    { id: 121, title: "Crawler zombie model", category: "zombies", status: "merged" },
    { id: 122, title: "Duplicate armor textures fix", category: "hud", status: "merged" },
    { id: 123, title: "Ocean territory spawn fix", category: "biomes", status: "merged" },
    { id: 124, title: "Texture tool fix", category: "docs", status: "merged" },
    { id: 125, title: "Sprint stamina bypass fix", category: "survival", status: "merged" },
    { id: 126, title: "Unified HUD bars", category: "hud", status: "merged" },
    { id: 127, title: "Exploitable structure blocks fix", category: "survival", status: "merged" },
    { id: 128, title: "Zombie stats rebalance", category: "zombies", status: "merged" },
    { id: 129, title: "Cancelled debuff overhaul", category: "debuffs", status: "cancelled" },
    { id: 130, title: "Debuff overhaul (vanilla scale)", category: "debuffs", status: "merged" },
    { id: 131, title: "World type selection", category: "survival", status: "merged" },
  ],
  "March 20": [
    { id: 132, title: "Spawn protection & difficulty", category: "survival", status: "merged" },
    { id: 133, title: "Docs/dashboard update", category: "docs", status: "merged" },
    { id: 134, title: "JAR rebuild", category: "git", status: "merged" },
    { id: 135, title: "Village overhaul", category: "biomes", status: "merged" },
    { id: 136, title: "Campfire merge", category: "loot", status: "merged" },
    { id: 137, title: "Water bottle consolidation", category: "loot", status: "merged" },
    { id: 138, title: "AK-47 model fix", category: "weapons", status: "merged" },
    { id: 139, title: "Docs/guides update", category: "docs", status: "merged" },
  ],
  "March 21": [
    { id: 140, title: "Vending machine", category: "loot", status: "merged" },
    { id: 141, title: "Wall placement", category: "building", status: "merged" },
    { id: 142, title: "Big map screen", category: "map", status: "merged" },
    { id: 143, title: "AK-47 texture/controls", category: "weapons", status: "merged" },
    { id: 144, title: "JAR rebuild", category: "git", status: "merged" },
    { id: 145, title: "A* pathfinding", category: "zombies", status: "merged" },
    { id: 146, title: "Stealth system", category: "stealth", status: "merged" },
    { id: 147, title: "Coordinated block bashing", category: "zombies", status: "merged" },
    { id: 148, title: "Smell tracking", category: "zombies", status: "merged" },
    { id: 149, title: "Blood Moon atmosphere", category: "daycycle", status: "merged" },
    { id: 150, title: "JAR rebuild", category: "git", status: "merged" },
    { id: 151, title: "Territory map labels", category: "map", status: "merged" },
    { id: 152, title: "HUD label cleanup", category: "hud", status: "merged" },
    { id: 153, title: "Territory entry animation", category: "hud", status: "merged" },
    { id: 154, title: "AK-47 bullet tuning", category: "weapons", status: "merged" },
    { id: 155, title: "AK-47 model fix", category: "weapons", status: "merged" },
    { id: 156, title: "Trader NPC system", category: "traders", status: "merged" },
    { id: 157, title: "Quest system", category: "traders", status: "merged" },
    { id: 158, title: "Vehicle wreckage", category: "biomes", status: "merged" },
  ],
  "March 22": [
    { id: 159, title: "Map icon fix", category: "map", status: "merged" },
    { id: 160, title: "Map territory rendering", category: "map", status: "merged" },
    { id: 161, title: "Map waypoint system", category: "map", status: "merged" },
    { id: 162, title: "Block texture atlas fix", category: "hud", status: "merged" },
    { id: 163, title: "Item texture batch update", category: "hud", status: "merged" },
    { id: 164, title: "Grenade weapon system", category: "weapons", status: "merged" },
    { id: 165, title: "Grenade physics & splash", category: "weapons", status: "merged" },
    { id: 166, title: "AK-47 full overhaul", category: "weapons", status: "merged" },
    { id: 167, title: "AK-47 scope attachment", category: "weapons", status: "merged" },
    { id: 168, title: "AK-47 sound effects", category: "weapons", status: "merged" },
    { id: 169, title: "Biome boundary smoothing", category: "biomes", status: "merged" },
    { id: 170, title: "Desert biome POI placement", category: "biomes", status: "merged" },
    { id: 171, title: "Trader inventory refresh", category: "traders", status: "merged" },
    { id: 172, title: "Trader price balancing", category: "traders", status: "merged" },
    { id: 173, title: "Trader POI world gen", category: "traders", status: "merged" },
    { id: 174, title: "Quest reward scaling", category: "traders", status: "merged" },
    { id: 175, title: "Block upgrade tiers", category: "building", status: "merged" },
    { id: 176, title: "Trap block system", category: "building", status: "merged" },
    { id: 177, title: "Reinforced block crafting", category: "building", status: "merged" },
    { id: 178, title: "Building system core", category: "building", status: "merged" },
    { id: 179, title: "Armor set system", category: "survival", status: "merged" },
    { id: 180, title: "Farming crop system", category: "farming", status: "merged" },
    { id: 181, title: "Cooking recipe overhaul", category: "farming", status: "merged" },
    { id: 182, title: "Dew Collector block", category: "farming", status: "merged" },
    { id: 183, title: "Wire connection system", category: "electricity", status: "merged" },
    { id: 184, title: "Generator block", category: "electricity", status: "merged" },
    { id: 185, title: "Electric fence trap", category: "electricity", status: "merged" },
    { id: 186, title: "Structural integrity calc", category: "building", status: "merged" },
    { id: 187, title: "Structure collapse physics", category: "building", status: "merged" },
    { id: 188, title: "Perk stat effects", category: "xp", status: "merged" },
    { id: 189, title: "Perk UI indicators", category: "xp", status: "merged" },
    { id: 190, title: "Debuff vignette overlays", category: "debuffs", status: "merged" },
    { id: 191, title: "Temperature overlay effects", category: "debuffs", status: "merged" },
    { id: 192, title: "Zombie smell detection polish", category: "zombies", status: "merged" },
    { id: 193, title: "Smell particle effects", category: "zombies", status: "merged" },
    { id: 194, title: "Admin teleport command", category: "survival", status: "merged" },
    { id: 195, title: "Admin spawn command", category: "survival", status: "merged" },
    { id: 196, title: "Ambient sound overhaul", category: "audio", status: "merged" },
    { id: 197, title: "Cooking & campfire audio", category: "farming", status: "merged" },
    { id: 198, title: "Electricity system core", category: "electricity", status: "merged" },
    { id: 199, title: "Inventory grid layout", category: "hud", status: "merged" },
    { id: 200, title: "Admin command system", category: "survival", status: "merged" },
    { id: 201, title: "Zombie glow rendering", category: "zombies", status: "merged" },
    { id: 202, title: "Inventory UI overhaul", category: "hud", status: "merged" },
    { id: 203, title: "Zombie variant glow VFX", category: "zombies", status: "merged" },
  ],
};

interface Milestone {
  id: number;
  title: string;
  phase: 1 | 2 | 3;
  status: "done" | "partial" | "not-started";
  complexity: number;
}

const MILESTONES: Milestone[] = [
  { id: 1, title: "Project scaffold + Mixin setup", phase: 1, status: "done", complexity: 2 },
  { id: 2, title: "Custom player stats (HP/Stamina/Food/Water)", phase: 1, status: "done", complexity: 3 },
  { id: 3, title: "Health conditions & debuff system", phase: 1, status: "done", complexity: 3 },
  { id: 4, title: "Temperature system", phase: 1, status: "partial", complexity: 3 },
  { id: 5, title: "Vanilla mob removal + base zombie entity", phase: 1, status: "done", complexity: 3 },
  { id: 6, title: "Zombie AI (behavior tree)", phase: 1, status: "done", complexity: 5 },
  { id: 7, title: "Heatmap system", phase: 1, status: "done", complexity: 3 },
  { id: 8, title: "Blood Moon / Horde Night", phase: 1, status: "done", complexity: 4 },
  { id: 9, title: "Custom HUD", phase: 1, status: "done", complexity: 3 },
  { id: 10, title: "4×4 crafting + quality tiers", phase: 1, status: "done", complexity: 4 },
  { id: 11, title: "Workstations", phase: 2, status: "done", complexity: 3 },
  { id: 12, title: "XP/Level + Perk system", phase: 2, status: "done", complexity: 4 },
  { id: 13, title: "Melee + ranged weapons", phase: 2, status: "done", complexity: 3 },
  { id: 14, title: "Armor system + clothing", phase: 2, status: "done", complexity: 3 },
  { id: 15, title: "World gen (biomes + city/POI)", phase: 2, status: "done", complexity: 5 },
  { id: 16, title: "All zombie variants", phase: 2, status: "done", complexity: 4 },
  { id: 17, title: "Building + block upgrades + traps", phase: 2, status: "done", complexity: 3 },
  { id: 18, title: "Loot tables + containers", phase: 2, status: "done", complexity: 3 },
  { id: 19, title: "Traders + quests", phase: 2, status: "done", complexity: 4 },
  { id: 20, title: "Vehicles + vehicle mods", phase: 2, status: "not-started", complexity: 5 },
  { id: 21, title: "Electricity system", phase: 2, status: "done", complexity: 4 },
  { id: 22, title: "Farming + cooking + Dew Collector", phase: 2, status: "done", complexity: 2 },
  { id: 23, title: "Skill book / magazine system", phase: 2, status: "done", complexity: 2 },
  { id: 24, title: "Stealth system", phase: 2, status: "done", complexity: 3 },
  { id: 25, title: "Inventory UI overhaul", phase: 2, status: "done", complexity: 4 },
  { id: 26, title: "Map system", phase: 2, status: "done", complexity: 3 },
  { id: 27, title: "Multiplayer sync + balancing", phase: 3, status: "not-started", complexity: 4 },
  { id: 28, title: "Performance optimization", phase: 3, status: "not-started", complexity: 5 },
  { id: 29, title: "Audio overhaul", phase: 3, status: "done", complexity: 2 },
  { id: 30, title: "Config GUI (Mod Menu compat)", phase: 3, status: "not-started", complexity: 2 },
  { id: 31, title: "Admin commands", phase: 3, status: "done", complexity: 1 },
  { id: 32, title: "QA, bug fixing, balancing pass", phase: 3, status: "not-started", complexity: 3 },
  { id: 33, title: "Sprint animation poses", phase: 3, status: "not-started", complexity: 3 },
  { id: 34, title: "Environmental sprint VFX", phase: 3, status: "not-started", complexity: 2 },
  { id: 35, title: "Third-person body language", phase: 3, status: "not-started", complexity: 2 },
  { id: 36, title: "Debuff vignette + temp overlays", phase: 3, status: "done", complexity: 3 },
  { id: 37, title: "Blood Moon atmosphere + camera", phase: 3, status: "done", complexity: 3 },
  { id: 38, title: "Zombie variant glow VFX", phase: 3, status: "done", complexity: 3 },
  { id: 39, title: "Vehicle camera bob/sway", phase: 3, status: "not-started", complexity: 2 },
];

type ActiveSection = "timeline" | "milestones" | "stats";

function StatCard({ value, label, accent }: { value: string | number; label: string; accent: string }) {
  return (
    <div style={{
      background: "rgba(255,255,255,0.03)",
      border: "1px solid rgba(255,255,255,0.08)",
      borderRadius: 12,
      padding: "20px 24px",
      textAlign: "center",
      flex: "1 1 160px",
      minWidth: 140,
    }}>
      <div style={{ fontSize: 32, fontWeight: 700, color: accent, fontFamily: "'JetBrains Mono', monospace" }}>
        {value}
      </div>
      <div style={{ fontSize: 13, color: "#94a3b8", marginTop: 4, fontWeight: 500 }}>
        {label}
      </div>
    </div>
  );
}

function CategoryLegend({ filter, onFilter }: { filter: string | null; onFilter: (cat: string | null) => void }) {
  return (
    <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginBottom: 20 }}>
      <button
        onClick={() => onFilter(null)}
        style={{
          padding: "4px 12px",
          borderRadius: 6,
          border: filter === null ? "1px solid rgba(255,255,255,0.3)" : "1px solid rgba(255,255,255,0.08)",
          background: filter === null ? "rgba(255,255,255,0.1)" : "rgba(255,255,255,0.03)",
          color: "#e2e8f0",
          fontSize: 12,
          cursor: "pointer",
          fontWeight: filter === null ? 600 : 400,
        }}
      >
        All
      </button>
      {Object.entries(CATEGORIES).map(([key, cat]) => (
        <button
          key={key}
          onClick={() => onFilter(filter === key ? null : key)}
          style={{
            padding: "4px 12px",
            borderRadius: 6,
            border: `1px solid ${filter === key ? cat.color : "rgba(255,255,255,0.08)"}`,
            background: filter === key ? cat.bg : "rgba(255,255,255,0.03)",
            color: filter === key ? cat.color : "#94a3b8",
            fontSize: 12,
            cursor: "pointer",
            fontWeight: filter === key ? 600 : 400,
            transition: "all 0.15s ease",
          }}
        >
          <span style={{
            display: "inline-block",
            width: 8,
            height: 8,
            borderRadius: "50%",
            backgroundColor: cat.color,
            marginRight: 6,
          }} />
          {cat.label}
        </button>
      ))}
    </div>
  );
}

function TimelineSection() {
  const [filter, setFilter] = useState<string | null>(null);
  const [expandedDay, setExpandedDay] = useState<string | null>(null);

  const days = Object.entries(ALL_TASKS);

  return (
    <div>
      <CategoryLegend filter={filter} onFilter={setFilter} />
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        {days.map(([day, tasks]) => {
          const filtered = filter ? tasks.filter(t => t.category === filter) : tasks;
          const isExpanded = expandedDay === day;
          const merged = tasks.filter(t => t.status === "merged").length;
          const cancelled = tasks.filter(t => t.status === "cancelled").length;

          return (
            <div key={day} style={{
              background: "rgba(255,255,255,0.02)",
              border: "1px solid rgba(255,255,255,0.06)",
              borderRadius: 12,
              overflow: "hidden",
            }}>
              <button
                onClick={() => setExpandedDay(isExpanded ? null : day)}
                style={{
                  width: "100%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  padding: "16px 20px",
                  background: "none",
                  border: "none",
                  cursor: "pointer",
                  color: "#e2e8f0",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                  <div style={{
                    width: 40,
                    height: 40,
                    borderRadius: 10,
                    background: "linear-gradient(135deg, #3b82f6, #8b5cf6)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: 700,
                    fontSize: 14,
                    fontFamily: "'JetBrains Mono', monospace",
                  }}>
                    {day.split(" ")[1]}
                  </div>
                  <div style={{ textAlign: "left" }}>
                    <div style={{ fontWeight: 600, fontSize: 15 }}>{day}, 2026</div>
                    <div style={{ fontSize: 12, color: "#64748b" }}>
                      {merged} merged{cancelled > 0 ? `, ${cancelled} cancelled` : ""} — Tasks #{tasks[0].id}–#{tasks[tasks.length - 1].id}
                    </div>
                  </div>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <div style={{ display: "flex", gap: 3 }}>
                    {Object.entries(CATEGORIES).map(([key, cat]) => {
                      const count = tasks.filter(t => t.category === key).length;
                      if (count === 0) return null;
                      return (
                        <div key={key} style={{
                          width: Math.max(count * 6, 6),
                          height: 6,
                          borderRadius: 3,
                          backgroundColor: cat.color,
                          opacity: 0.7,
                        }} />
                      );
                    })}
                  </div>
                  <span style={{ fontSize: 18, color: "#64748b", transition: "transform 0.2s", transform: isExpanded ? "rotate(180deg)" : "rotate(0)" }}>
                    ▾
                  </span>
                </div>
              </button>
              {isExpanded && (
                <div style={{ padding: "0 20px 16px" }}>
                  <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 8 }}>
                    {filtered.map(task => {
                      const cat = CATEGORIES[task.category];
                      return (
                        <div key={task.id} style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 10,
                          padding: "10px 14px",
                          borderRadius: 8,
                          background: cat.bg,
                          border: `1px solid ${cat.color}22`,
                          opacity: task.status === "cancelled" ? 0.5 : 1,
                        }}>
                          <span style={{
                            fontFamily: "'JetBrains Mono', monospace",
                            fontSize: 11,
                            color: "#64748b",
                            minWidth: 28,
                          }}>
                            #{task.id}
                          </span>
                          <span style={{
                            width: 8,
                            height: 8,
                            borderRadius: "50%",
                            backgroundColor: cat.color,
                            flexShrink: 0,
                          }} />
                          <span style={{
                            fontSize: 13,
                            color: task.status === "cancelled" ? "#64748b" : "#e2e8f0",
                            textDecoration: task.status === "cancelled" ? "line-through" : "none",
                            fontWeight: 500,
                          }}>
                            {task.title}
                          </span>
                          {task.status === "cancelled" && (
                            <span style={{
                              fontSize: 10,
                              color: "#ef4444",
                              background: "rgba(239,68,68,0.15)",
                              padding: "2px 6px",
                              borderRadius: 4,
                              marginLeft: "auto",
                              fontWeight: 600,
                            }}>
                              CANCELLED
                            </span>
                          )}
                        </div>
                      );
                    })}
                    {filtered.length === 0 && (
                      <div style={{ color: "#475569", fontSize: 13, padding: 12, fontStyle: "italic" }}>
                        No tasks in this category for {day}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

function MilestoneBar({ phase, milestones }: { phase: number; milestones: Milestone[] }) {
  const done = milestones.filter(m => m.status === "done").length;
  const total = milestones.length;
  const pct = Math.round((done / total) * 100);

  const phaseColors: Record<number, string> = {
    1: "#22c55e",
    2: "#3b82f6",
    3: "#a855f7",
  };
  const phaseLabels: Record<number, string> = {
    1: "Core Foundation",
    2: "Content & Systems",
    3: "Polish & Optimization",
  };

  return (
    <div style={{
      background: "rgba(255,255,255,0.02)",
      border: "1px solid rgba(255,255,255,0.06)",
      borderRadius: 12,
      padding: 20,
      marginBottom: 16,
    }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 12 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <span style={{
            fontFamily: "'JetBrains Mono', monospace",
            fontSize: 11,
            fontWeight: 700,
            color: phaseColors[phase],
            background: `${phaseColors[phase]}22`,
            padding: "3px 8px",
            borderRadius: 4,
          }}>
            PHASE {phase}
          </span>
          <span style={{ fontSize: 14, fontWeight: 600, color: "#e2e8f0" }}>
            {phaseLabels[phase]}
          </span>
        </div>
        <span style={{ fontSize: 13, color: "#94a3b8", fontFamily: "'JetBrains Mono', monospace" }}>
          {done}/{total} ({pct}%)
        </span>
      </div>
      <div style={{
        width: "100%",
        height: 6,
        borderRadius: 3,
        background: "rgba(255,255,255,0.06)",
        marginBottom: 16,
      }}>
        <div style={{
          width: `${pct}%`,
          height: "100%",
          borderRadius: 3,
          background: `linear-gradient(90deg, ${phaseColors[phase]}, ${phaseColors[phase]}aa)`,
          transition: "width 0.5s ease",
        }} />
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 8 }}>
        {milestones.map(m => {
          const statusColor = m.status === "done" ? "#22c55e" : m.status === "partial" ? "#eab308" : "#334155";
          const statusIcon = m.status === "done" ? "✓" : m.status === "partial" ? "◐" : "○";

          return (
            <div key={m.id} style={{
              display: "flex",
              alignItems: "center",
              gap: 10,
              padding: "8px 12px",
              borderRadius: 8,
              background: m.status === "done" ? "rgba(34,197,94,0.06)" : "rgba(255,255,255,0.01)",
              border: `1px solid ${m.status === "done" ? "rgba(34,197,94,0.15)" : "rgba(255,255,255,0.04)"}`,
            }}>
              <span style={{
                fontSize: 14,
                color: statusColor,
                width: 20,
                textAlign: "center",
                fontWeight: 700,
              }}>
                {statusIcon}
              </span>
              <span style={{
                fontFamily: "'JetBrains Mono', monospace",
                fontSize: 11,
                color: "#64748b",
                minWidth: 24,
              }}>
                #{m.id}
              </span>
              <span style={{
                fontSize: 13,
                color: m.status === "done" ? "#e2e8f0" : "#64748b",
                fontWeight: m.status === "done" ? 500 : 400,
              }}>
                {m.title}
              </span>
              <div style={{ marginLeft: "auto", display: "flex", gap: 2 }}>
                {Array.from({ length: 5 }).map((_, i) => (
                  <span key={i} style={{
                    fontSize: 8,
                    color: i < m.complexity ? phaseColors[m.phase] : "#1e293b",
                  }}>
                    ★
                  </span>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function MilestonesSection() {
  const phases = [1, 2, 3] as const;
  return (
    <div>
      {phases.map(phase => (
        <MilestoneBar
          key={phase}
          phase={phase}
          milestones={MILESTONES.filter(m => m.phase === phase)}
        />
      ))}
    </div>
  );
}

function StatsSection() {
  const allTasks = Object.values(ALL_TASKS).flat();
  const merged = allTasks.filter(t => t.status === "merged").length;
  const cancelled = allTasks.filter(t => t.status === "cancelled").length;
  const total = allTasks.length;
  const milestonesTouched = MILESTONES.filter(m => m.status === "done" || m.status === "partial").length;
  const categoriesUsed = new Set(allTasks.map(t => t.category)).size;

  const catCounts = Object.entries(CATEGORIES).map(([key, cat]) => ({
    key,
    label: cat.label,
    color: cat.color,
    bg: cat.bg,
    count: allTasks.filter(t => t.category === key).length,
  })).sort((a, b) => b.count - a.count);

  const maxCount = Math.max(...catCounts.map(c => c.count));

  return (
    <div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 12, marginBottom: 24 }}>
        <StatCard value={total} label="Total Tasks" accent="#3b82f6" />
        <StatCard value={merged} label="Merged" accent="#22c55e" />
        <StatCard value={cancelled} label="Cancelled" accent="#ef4444" />
        <StatCard value={12} label="Dev Days" accent="#8b5cf6" />
        <StatCard value={`${milestonesTouched}/39`} label="Milestones Done" accent="#f97316" />
        <StatCard value={categoriesUsed} label="Categories" accent="#06b6d4" />
      </div>

      <div style={{
        background: "rgba(255,255,255,0.02)",
        border: "1px solid rgba(255,255,255,0.06)",
        borderRadius: 12,
        padding: 24,
        marginBottom: 24,
      }}>
        <h3 style={{ fontSize: 15, fontWeight: 600, color: "#e2e8f0", marginBottom: 20 }}>
          Tasks by Category
        </h3>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {catCounts.map(cat => (
            <div key={cat.key} style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <span style={{
                fontSize: 12,
                color: "#94a3b8",
                minWidth: 100,
                textAlign: "right",
                fontWeight: 500,
              }}>
                {cat.label}
              </span>
              <div style={{ flex: 1, height: 20, borderRadius: 4, background: "rgba(255,255,255,0.04)" }}>
                <div style={{
                  width: `${(cat.count / maxCount) * 100}%`,
                  height: "100%",
                  borderRadius: 4,
                  background: `linear-gradient(90deg, ${cat.color}, ${cat.color}88)`,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "flex-end",
                  paddingRight: 8,
                  transition: "width 0.5s ease",
                  minWidth: 30,
                }}>
                  <span style={{
                    fontSize: 11,
                    fontWeight: 700,
                    color: "#fff",
                    fontFamily: "'JetBrains Mono', monospace",
                  }}>
                    {cat.count}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={{
        background: "rgba(255,255,255,0.02)",
        border: "1px solid rgba(255,255,255,0.06)",
        borderRadius: 12,
        padding: 24,
      }}>
        <h3 style={{ fontSize: 15, fontWeight: 600, color: "#e2e8f0", marginBottom: 20 }}>
          Daily Output
        </h3>
        <div style={{ display: "flex", alignItems: "flex-end", gap: 16, height: 160 }}>
          {Object.entries(ALL_TASKS).map(([day, tasks]) => {
            const m = tasks.filter(t => t.status === "merged").length;
            const maxDaily = Math.max(...Object.values(ALL_TASKS).map(ts => ts.filter(t => t.status === "merged").length));
            const h = (m / maxDaily) * 140;
            return (
              <div key={day} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
                <span style={{
                  fontFamily: "'JetBrains Mono', monospace",
                  fontSize: 13,
                  fontWeight: 700,
                  color: "#8b5cf6",
                }}>
                  {m}
                </span>
                <div style={{
                  width: "100%",
                  maxWidth: 80,
                  height: h,
                  borderRadius: "8px 8px 4px 4px",
                  background: "linear-gradient(180deg, #8b5cf6, #3b82f6)",
                  opacity: 0.8,
                }} />
                <span style={{ fontSize: 12, color: "#94a3b8", fontWeight: 500 }}>
                  {day.split(" ")[1]}
                </span>
              </div>
            );
          })}
        </div>
        <div style={{ textAlign: "center", fontSize: 12, color: "#475569", marginTop: 8 }}>
          March 2026
        </div>
      </div>
    </div>
  );
}

export default function DevHistoryDashboard() {
  const [activeSection, setActiveSection] = useState<ActiveSection>("timeline");

  const sections: { key: ActiveSection; label: string; icon: string }[] = [
    { key: "timeline", label: "Timeline", icon: "📅" },
    { key: "milestones", label: "Milestones", icon: "🎯" },
    { key: "stats", label: "Stats", icon: "📊" },
  ];

  return (
    <div className="dark" style={{
      minHeight: "100vh",
      background: "#0a0a0f",
      color: "#e2e8f0",
      fontFamily: "'Inter', -apple-system, sans-serif",
    }}>
      <div style={{ maxWidth: 1100, margin: "0 auto", padding: "32px 24px" }}>
        <div style={{ marginBottom: 32 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 8 }}>
            <div style={{
              width: 36,
              height: 36,
              borderRadius: 8,
              background: "linear-gradient(135deg, #ef4444, #dc2626)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 18,
            }}>
              ⚔
            </div>
            <h1 style={{
              fontSize: 26,
              fontWeight: 800,
              margin: 0,
              background: "linear-gradient(135deg, #ef4444, #f97316, #eab308)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
              letterSpacing: "-0.5px",
            }}>
              BZHS Development History
            </h1>
          </div>
          <p style={{ fontSize: 14, color: "#64748b", margin: 0 }}>
            Brutal Zombie Horde Survival — March 12–22, 2026 — 203 tasks, 12 dev days
          </p>
        </div>

        <div style={{ display: "flex", flexWrap: "wrap", gap: 12, marginBottom: 24 }}>
          <StatCard value="199" label="Tasks Merged" accent="#22c55e" />
          <StatCard value="30/39" label="Milestones Touched" accent="#f97316" />
          <StatCard value="12" label="Dev Days" accent="#8b5cf6" />
          <StatCard value="20" label="Categories" accent="#06b6d4" />
        </div>

        <div style={{
          display: "flex",
          gap: 4,
          marginBottom: 24,
          background: "rgba(255,255,255,0.03)",
          borderRadius: 10,
          padding: 4,
          border: "1px solid rgba(255,255,255,0.06)",
        }}>
          {sections.map(s => (
            <button
              key={s.key}
              onClick={() => setActiveSection(s.key)}
              style={{
                flex: 1,
                padding: "10px 16px",
                borderRadius: 8,
                border: "none",
                cursor: "pointer",
                fontSize: 14,
                fontWeight: activeSection === s.key ? 600 : 400,
                background: activeSection === s.key ? "rgba(255,255,255,0.08)" : "transparent",
                color: activeSection === s.key ? "#e2e8f0" : "#64748b",
                transition: "all 0.15s ease",
                fontFamily: "inherit",
              }}
            >
              {s.icon} {s.label}
            </button>
          ))}
        </div>

        {activeSection === "timeline" && <TimelineSection />}
        {activeSection === "milestones" && <MilestonesSection />}
        {activeSection === "stats" && <StatsSection />}

        <div style={{
          marginTop: 32,
          padding: "16px 20px",
          borderRadius: 10,
          background: "rgba(255,255,255,0.02)",
          border: "1px solid rgba(255,255,255,0.05)",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: 12,
        }}>
          <span style={{ fontSize: 12, color: "#475569" }}>
            Brutal Zombie Horde Survival — Minecraft 1.21.4 NeoForge Mod — Built with Replit Agent
          </span>
          <span style={{ fontSize: 12, color: "#334155", fontFamily: "'JetBrains Mono', monospace" }}>
            snapshot: March 23, 2026
          </span>
        </div>
      </div>
    </div>
  );
}
