# Project Notes – End of Session (March 11, 2026)

## Current Status
- **Milestone #2 (Custom Player Stats)**: Complete (Food/Water/Stamina/Temp/Debuffs/Sync/HUD).
- **Milestone #3 (Horde Night & Blood Moon)**: Implemented and tested!
  - Sky turns red and atmospheric fog applies on Day 7 evening (18:00).
  - Siren plays and warning text appears to players.
  - Zombie waves spawn throughout the night.
  - **New addition**: Players cannot sleep in beds during the Blood Moon event.

## Known Bugs / Polish To Address Next
1. ❌ **SPRINT BUG PERSISTS**:
   - Sometimes sprint gets STUCK — holding W alone gives infinite sprint (stamina drains but sprint doesn't cancel). Needs a different approach (maybe client-side Mixin on `LocalPlayer.aiStep()`).
2. ⚠️ **TEST NEEDED - Temperature**:
   - Changed adjustment rate to a much slower 0.3°F/s. Needs long-term gameplay verification.
3. ⚠️ **TEST NEEDED - Debuffs**: 
   - Debuffs are unverified. Needs a dedicated test session to confirm infection/bleeding.
4. ⚠️ **TEST NEEDED - Horde Spawns**:
   - Verify that the amount of zombies spawning matches intended difficulty vs vanilla spawn rates.
5. **Other Tasks**:
   - HUD polish (compass/minimap).
   - Heatmap stub is implemented but needs actual chunk data tied to it to add noise when sprinting/mining.

## Next Session Goals
1. Review and address the persistent sprint bug.
2. Verify temperature and debuff systems in normal gameplay.
3. Move on to Milestone #4 (Custom Zombies) or Item/Block Overhaul based on priority.
