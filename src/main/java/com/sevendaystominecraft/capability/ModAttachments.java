package com.sevendaystominecraft.capability;

import java.util.function.Supplier;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Registers NeoForge Data Attachments for the mod.
 *
 * The primary attachment is {@link #PLAYER_STATS}, which stores all
 * custom survival stats (Food, Water, Stamina, Debuffs, Temperature)
 * on every Player entity.
 *
 * <h3>Key behaviors:</h3>
 * <ul>
 *   <li>Persisted to disk via {@link SevenDaysPlayerStats} (INBTSerializable)</li>
 *   <li>Death handling is done manually in PlayerStatsHandler's Clone handler</li>
 *   <li>Synced to client manually via {@link com.sevendaystominecraft.network.SyncPlayerStatsPayload}</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>
 * // Get stats for a player (auto-creates with defaults if absent)
 * SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
 *
 * // Modify stats (mutable object — changes persist automatically on save)
 * stats.setFood(stats.getFood() - 0.5f);
 *
 * // For client sync, use the manual payload in PlayerStatsHandler
 * </pre>
 *
 * <h3>Note on sync:</h3>
 * NeoForge 21.4.140 does not have the built-in AttachmentSyncHandler API.
 * Client sync is handled manually in {@link PlayerStatsHandler} using
 * {@link com.sevendaystominecraft.network.SyncPlayerStatsPayload} sent via
 * {@code PacketDistributor.sendToPlayer()}.
 */
public class ModAttachments {

    /**
     * DeferredRegister for all attachment types in this mod.
     * Must be registered on the mod event bus in the mod constructor.
     */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SevenDaysToMinecraft.MOD_ID);

    /**
     * Player stats attachment — Food, Water, Stamina, Temperature, Debuffs.
     *
     * Registered with:
     * - serializable() → uses INBTSerializable for CompoundTag persistence
     *
     * Death/respawn copying is handled manually in PlayerStatsHandler's Clone
     * handler via copyFrom(), which allows debuffs to be cleared on respawn.
     * Client sync is handled manually via SyncPlayerStatsPayload.
     */
    public static final Supplier<AttachmentType<SevenDaysPlayerStats>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats",
                    () -> AttachmentType.serializable(SevenDaysPlayerStats::new)
                            .build()
            );
}
