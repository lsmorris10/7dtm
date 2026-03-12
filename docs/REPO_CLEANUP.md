# Repository Cleanup & Environment Sync

**Date:** March 12, 2026
**From:** Replit Agent environment
**For:** Antigravity IDE / repo maintainer

---

## The Problem

This repo has been developed simultaneously from two environments:

1. **Replit Agent** — an AI-powered cloud IDE that auto-commits checkpoints, spawns temporary branches for parallel tasks, and manages its own git history
2. **Antigravity** — a local/desktop IDE pushing directly to the GitHub remote

These two systems don't coordinate their git operations. Replit creates checkpoint commits ("Transitioned from Plan to Build mode"), spawns `subrepl-*` branches for parallel task agents, and sometimes produces duplicate commits during code review iterations. When both environments push to the same repo, histories diverge and merge conflicts appear — even when the actual code is identical.

---

## Current Repo State (as of March 12, 2026)

### Branch: `master` (clean)
The main branch is currently clean and in sync between local Replit and GitHub origin. HEAD is at `478119f` ("chore: clean up project notes").

### Branches Safe to Delete

These are leftover branches from Replit's parallel task agent system. Each `subrepl-*` branch was a temporary workspace for a single task that has already been merged. The `replit-agent` branch is a stale checkpoint. None of these contain unmerged work.

| Branch | What It Was | Safe to Delete? |
|--------|-------------|-----------------|
| `replit-agent` | Stale Replit checkpoint branch | Yes |
| `subrepl-24kcl354` | Task #7: HUD health bar | Yes (merged) |
| `subrepl-503h5b70` | Task #6: Hide vanilla hunger bar | Yes (merged) |
| `subrepl-6rak8wde` | Task #9: PROJECT_NOTES update | Yes (merged) |
| `subrepl-bexjffm9` | Task #4: Edge case testing doc | Yes (merged) |
| `subrepl-m39wwtz9` | Task #3: HP display + bounding boxes | Yes (merged) |
| `subrepl-tn1mjr1j` | Task #2: Name tags above zombies | Yes (merged) |
| `subrepl-tnv6oy1s` | Task #8: PROJECT_NOTES update | Yes (merged) |
| `gitsafe-backup/main` | Replit internal backup ref | Yes |

**To delete all at once (from a terminal with push access):**
```bash
git branch -D replit-agent subrepl-24kcl354 subrepl-503h5b70 subrepl-6rak8wde subrepl-bexjffm9 subrepl-m39wwtz9 subrepl-tn1mjr1j subrepl-tnv6oy1s

# If any were pushed to GitHub as remote branches:
git push origin --delete replit-agent subrepl-24kcl354 subrepl-503h5b70 subrepl-6rak8wde subrepl-bexjffm9 subrepl-m39wwtz9 subrepl-tn1mjr1j subrepl-tnv6oy1s
```

---

## What Has Been Built (Spec Milestone Status)

The spec (`docs/7dtm_final_spec.md` §19) defines 39 milestones across 3 phases. Here is the current status of Phase 1 (milestones 1–10):

| Spec # | Milestone | Status | Notes |
|--------|-----------|--------|-------|
| 1 | Project scaffold + Mixin setup | DONE | Initial commit |
| 2 | Custom player stats (HP/Stamina/Food/Water) | DONE | Commit c6b7dba |
| 3 | Health conditions & debuffs (Bleeding/Infection/Dysentery) | SKIPPED | Not implemented yet |
| 4 | Temperature system | SKIPPED | Not implemented yet |
| 5 | Vanilla mob removal + base zombie entity | DONE | 18 variants, commit a89973c |
| 6 | Zombie AI (behavior tree, destructive pathfinding) | PARTIAL | Basic AI only, no destructive pathfinding or behavior trees |
| 7 | Heatmap system | DONE | Full implementation, commit fec67eb |
| 8 | Blood Moon / Horde Night | DONE | Full timeline + waves, commit 4b13080 |
| 9 | Custom HUD | PARTIAL | Health bar + hunger hidden; no compass/minimap yet |
| 10 | 4x4 crafting + quality tiers | NOT STARTED | — |

### Skipped milestones that need to be circled back to:
- **#3 (Debuffs)** — Bleeding, Infection stages, Dysentery. The HUD (#9) depends on this for debuff indicators.
- **#4 (Temperature)** — Core temp, biome effects, clothing insulation. Referenced by many later systems.
- **#6 (Full Zombie AI)** — Behavior trees, destructive pathfinding, block breaking. Currently zombies have basic vanilla-style AI only.

### Known bugs:
- **Sprint bug** — Holding W gives infinite sprint even when stamina depletes. Needs a client-side Mixin on `LocalPlayer.aiStep()`. Known since Milestone 2, deferred intentionally.

---

## How the Dual-Environment Issues Happened

### Timeline of the git mess:
1. Replit Agent built Milestone 5 (Heatmap). Code review iterations created 6 commits all titled "Milestone 5: Implement Heatmap System" — same code, separate commits from each review pass.
2. Replit also created "Transitioned from Plan to Build mode" checkpoint commits automatically.
3. A squash was attempted from Replit to clean up, but this rewrote commit SHAs.
4. The repo owner force-pushed the squashed history from GitHub.
5. Replit's local history (old SHAs) diverged from the new remote history (new SHAs), causing merge conflicts even though the code was byte-for-byte identical.
6. Resolved by `git fetch origin && git reset --hard origin/master` on the Replit side.

### Why it happens:
- Replit auto-commits checkpoints on every mode transition and task completion.
- Replit spawns `subrepl-*` branches for parallel task agents, which leave orphan branches after merging.
- Replit cannot `git push --force` to GitHub (no GitHub auth credentials in this environment).
- History rewrites (squash/rebase) done on one side cause the other side to diverge.

---

## Recommendations: Choosing a Primary Environment

### Option A: Replit Agent as Primary
- **Pros:** AI handles implementation, auto-testing, code review. Good for rapid feature development.
- **Cons:** Creates noisy git history (checkpoint commits, duplicate review commits). Cannot force-push to GitHub. Orphan branches accumulate.
- **If chosen:** Don't edit code in Antigravity. Use Antigravity only for reading/reviewing. Periodically clean up branches from Antigravity since Replit can't delete remote branches.

### Option B: Antigravity as Primary
- **Pros:** Clean git history, full GitHub access (push, force-push, branch management). Traditional development workflow.
- **Cons:** No AI assistance for implementation. Manual coding and testing.
- **If chosen:** Don't use Replit Agent for code tasks. Can still use Replit for planning/discussion, but all code changes go through Antigravity.

### Option C: Both, but with a Protocol
- **Pros:** Best of both worlds when coordinated.
- **Cons:** Requires discipline.
- **Protocol if chosen:**
  1. Only one environment makes code changes at a time.
  2. Before switching environments, always sync: `git pull` in the destination environment.
  3. Never squash/rebase from Replit — only do history cleanup from Antigravity (which has GitHub push access).
  4. Periodically delete `subrepl-*` and `replit-agent` branches from Antigravity.
  5. Accept that Replit will create noisy checkpoint commits — squash them from Antigravity if desired, then `git fetch && git reset --hard origin/master` on the Replit side after.

---

## Quick Cleanup Checklist

- [ ] Delete all `subrepl-*` branches (local and remote)
- [ ] Delete `replit-agent` branch
- [ ] Delete `gitsafe-backup/main` remote ref if it exists on GitHub
- [ ] Decide on primary environment (A, B, or C above)
- [ ] If keeping Replit as primary or shared: accept checkpoint commit noise or plan periodic squash sessions
- [ ] Verify build passes: `./gradlew build --no-daemon` (requires Java 21)
- [ ] Review skipped milestones (#3, #4, #6) and decide priority order for next work

---

## File Reference

| File | Purpose |
|------|---------|
| `docs/7dtm_final_spec.md` | Full mod specification (39 milestones, all systems) |
| `docs/zombie_guide.md` | Guide to all 18 zombie variants |
| `docs/if-bored.md` | Edge case testing scenarios |
| `PROJECT_NOTES.md` | Current status, testing checklist, known bugs |
| `docs/REPO_CLEANUP.md` | This file |
