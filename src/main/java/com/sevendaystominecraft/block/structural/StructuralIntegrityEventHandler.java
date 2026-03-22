package com.sevendaystominecraft.block.structural;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.StructuralIntegrityConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class StructuralIntegrityEventHandler {

    private static final List<PendingSICheck> PENDING_CHECKS = new ArrayList<>();
    private static final int MAX_PENDING_WAIT_TICKS = 5;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!StructuralIntegrityConfig.INSTANCE.enabled.get()) return;
        if (event.isCanceled()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos().immutable();
        net.minecraft.world.level.block.state.BlockState originalState = level.getBlockState(pos);
        PENDING_CHECKS.add(new PendingSICheck(level, pos, originalState, level.getGameTime()));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (!PENDING_CHECKS.isEmpty()) {
            long currentTick = level.getGameTime();
            List<PendingSICheck> remaining = new ArrayList<>();
            for (PendingSICheck check : PENDING_CHECKS) {
                if (check.level == level) {
                    boolean stateChanged = check.level.getBlockState(check.pos).isAir()
                            || !check.level.getBlockState(check.pos).equals(check.originalState);
                    if (stateChanged) {
                        StructuralIntegrityManager.onBlockBroken(check.level, check.pos);
                    } else if (currentTick - check.queuedTick < MAX_PENDING_WAIT_TICKS) {
                        remaining.add(check);
                    }
                } else {
                    remaining.add(check);
                }
            }
            PENDING_CHECKS.clear();
            PENDING_CHECKS.addAll(remaining);
        }

        if (StructuralIntegrityConfig.INSTANCE.enabled.get()) {
            StructuralIntegrityManager.onServerTick(level);
        }
    }

    private record PendingSICheck(ServerLevel level, BlockPos pos,
                                      net.minecraft.world.level.block.state.BlockState originalState,
                                      long queuedTick) {}
}
