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
  landing: { label: "Landing Page", color: "#ec4899", bg: "rgba(236,72,153,0.15)" },
  docs: { label: "Documentation", color: "#94a3b8", bg: "rgba(148,163,184,0.15)" },
  git: { label: "Git / Repo", color: "#78716c", bg: "rgba(120,113,108,0.15)" },
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
  { id: 6, title: "Zombie AI (behavior tree)", phase: 1, status: "not-started", complexity: 5 },
  { id: 7, title: "Heatmap system", phase: 1, status: "done", complexity: 3 },
  { id: 8, title: "Blood Moon / Horde Night", phase: 1, status: "done", complexity: 4 },
  { id: 9, title: "Custom HUD", phase: 1, status: "done", complexity: 3 },
  { id: 10, title: "4×4 crafting + quality tiers", phase: 1, status: "done", complexity: 4 },
  { id: 11, title: "Workstations", phase: 2, status: "not-started", complexity: 3 },
  { id: 12, title: "XP/Level + Perk system", phase: 2, status: "done", complexity: 4 },
  { id: 13, title: "Melee + ranged weapons", phase: 2, status: "not-started", complexity: 3 },
  { id: 14, title: "Armor system + clothing", phase: 2, status: "not-started", complexity: 3 },
  { id: 15, title: "World gen (biomes + city/POI)", phase: 2, status: "not-started", complexity: 5 },
  { id: 16, title: "All zombie variants", phase: 2, status: "done", complexity: 4 },
  { id: 17, title: "Building + block upgrades + traps", phase: 2, status: "not-started", complexity: 3 },
  { id: 18, title: "Loot tables + containers", phase: 2, status: "not-started", complexity: 3 },
  { id: 19, title: "Traders + quests", phase: 2, status: "not-started", complexity: 4 },
  { id: 20, title: "Vehicles + vehicle mods", phase: 2, status: "not-started", complexity: 5 },
  { id: 21, title: "Electricity system", phase: 2, status: "not-started", complexity: 4 },
  { id: 22, title: "Farming + cooking + Dew Collector", phase: 2, status: "not-started", complexity: 2 },
  { id: 23, title: "Skill book / magazine system", phase: 2, status: "not-started", complexity: 2 },
  { id: 24, title: "Stealth system", phase: 2, status: "not-started", complexity: 3 },
  { id: 25, title: "Inventory UI overhaul", phase: 2, status: "not-started", complexity: 4 },
  { id: 26, title: "Map system", phase: 2, status: "not-started", complexity: 3 },
  { id: 27, title: "Multiplayer sync + balancing", phase: 3, status: "not-started", complexity: 4 },
  { id: 28, title: "Performance optimization", phase: 3, status: "not-started", complexity: 5 },
  { id: 29, title: "Audio overhaul", phase: 3, status: "not-started", complexity: 2 },
  { id: 30, title: "Config GUI (Mod Menu compat)", phase: 3, status: "not-started", complexity: 2 },
  { id: 31, title: "Admin commands", phase: 3, status: "not-started", complexity: 1 },
  { id: 32, title: "QA, bug fixing, balancing pass", phase: 3, status: "not-started", complexity: 3 },
  { id: 33, title: "Sprint animation poses", phase: 3, status: "not-started", complexity: 3 },
  { id: 34, title: "Environmental sprint VFX", phase: 3, status: "not-started", complexity: 2 },
  { id: 35, title: "Third-person body language", phase: 3, status: "not-started", complexity: 2 },
  { id: 36, title: "Debuff vignette + temp overlays", phase: 3, status: "not-started", complexity: 3 },
  { id: 37, title: "Blood Moon atmosphere + camera", phase: 3, status: "not-started", complexity: 3 },
  { id: 38, title: "Zombie variant glow VFX", phase: 3, status: "not-started", complexity: 3 },
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
        <StatCard value={4} label="Dev Days" accent="#8b5cf6" />
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
            Brutal Zombie Horde Survival — March 12–15, 2026 — 63 tasks, 4 days of development
          </p>
        </div>

        <div style={{ display: "flex", flexWrap: "wrap", gap: 12, marginBottom: 24 }}>
          <StatCard value="61" label="Tasks Merged" accent="#22c55e" />
          <StatCard value="11/39" label="Milestones Touched" accent="#f97316" />
          <StatCard value="4" label="Dev Days" accent="#8b5cf6" />
          <StatCard value="11" label="Categories" accent="#06b6d4" />
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
            snapshot: March 15, 2026
          </span>
        </div>
      </div>
    </div>
  );
}
