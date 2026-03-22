package com.sevendaystominecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.ZombieVariant;
import com.sevendaystominecraft.heatmap.HeatmapData;
import com.sevendaystominecraft.heatmap.HeatmapSpawner;
import com.sevendaystominecraft.horde.BloodMoonTracker;
import com.sevendaystominecraft.item.QualityTier;
import com.sevendaystominecraft.territory.TerritoryData;
import com.sevendaystominecraft.territory.TerritoryRecord;
import com.sevendaystominecraft.territory.TerritoryTier;
import com.sevendaystominecraft.territory.TerritoryType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class AdminCommand {

    private static final String PREFIX = "§6[BZHS] §r";
    private static final String SUCCESS_COLOR = "§a";
    private static final String ERROR_COLOR = "§c";
    private static final String INFO_COLOR = "§7";

    private static final Map<String, java.util.function.Supplier<EntityType<? extends Mob>>> ZOMBIE_TYPES = new LinkedHashMap<>();
    static {
        ZOMBIE_TYPES.put("walker", ModEntities.WALKER::get);
        ZOMBIE_TYPES.put("crawler", ModEntities.CRAWLER::get);
        ZOMBIE_TYPES.put("frozen_lumberjack", ModEntities.FROZEN_LUMBERJACK::get);
        ZOMBIE_TYPES.put("bloated_walker", ModEntities.BLOATED_WALKER::get);
        ZOMBIE_TYPES.put("spider_zombie", ModEntities.SPIDER_ZOMBIE::get);
        ZOMBIE_TYPES.put("feral_wight", ModEntities.FERAL_WIGHT::get);
        ZOMBIE_TYPES.put("cop", ModEntities.COP::get);
        ZOMBIE_TYPES.put("screamer", ModEntities.SCREAMER::get);
        ZOMBIE_TYPES.put("zombie_dog", ModEntities.ZOMBIE_DOG::get);
        ZOMBIE_TYPES.put("vulture", ModEntities.VULTURE::get);
        ZOMBIE_TYPES.put("demolisher", ModEntities.DEMOLISHER::get);
        ZOMBIE_TYPES.put("mutated_chuck", ModEntities.MUTATED_CHUCK::get);
        ZOMBIE_TYPES.put("zombie_bear", ModEntities.ZOMBIE_BEAR::get);
        ZOMBIE_TYPES.put("nurse", ModEntities.NURSE::get);
        ZOMBIE_TYPES.put("soldier", ModEntities.SOLDIER::get);
        ZOMBIE_TYPES.put("charged", ModEntities.CHARGED::get);
        ZOMBIE_TYPES.put("infernal", ModEntities.INFERNAL::get);
        ZOMBIE_TYPES.put("behemoth", ModEntities.BEHEMOTH::get);
    }

    private static final String[] STAT_NAMES = {"health", "stamina", "food", "water", "temperature"};

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_DEBUFFS = (ctx, builder) -> {
        for (String id : SevenDaysPlayerStats.KNOWN_DEBUFF_IDS) {
            if (id.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(id);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ZOMBIE_TYPES = (ctx, builder) -> {
        for (String name : ZOMBIE_TYPES.keySet()) {
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_STATS = (ctx, builder) -> {
        for (String name : STAT_NAMES) {
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MOD_ITEMS = (ctx, builder) -> {
        String modId = SevenDaysToMinecraft.MOD_ID;
        for (ResourceLocation key : BuiltInRegistries.ITEM.keySet()) {
            if (key.getNamespace().equals(modId)) {
                String name = key.getPath();
                if (name.startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(name);
                }
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUALITY = (ctx, builder) -> {
        for (QualityTier tier : QualityTier.values()) {
            String name = tier.name().toLowerCase();
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bzhs")
                .then(Commands.literal("admin")
                        .requires(src -> src.hasPermission(2))
                        .then(buildTimeCommand())
                        .then(buildHeatmapCommand())
                        .then(buildDebuffCommand())
                        .then(buildStatsCommand())
                        .then(buildTerritoryCommand())
                        .then(buildZombieCommand())
                        .then(buildLootCommand())
                        .then(buildGiveCommand())
                )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildTimeCommand() {
        return Commands.literal("time")
                .then(Commands.literal("day").executes(ctx -> setTime(ctx, "day")))
                .then(Commands.literal("night").executes(ctx -> setTime(ctx, "night")))
                .then(Commands.literal("bloodmoon").executes(AdminCommand::triggerBloodMoon));
    }

    private static int setTime(CommandContext<CommandSourceStack> ctx, String timeOfDay) {
        ServerLevel level = ctx.getSource().getLevel();
        long currentDay = level.getDayTime() / SevenDaysConstants.DAY_LENGTH;
        long targetTick;
        if (timeOfDay.equals("day")) {
            targetTick = (currentDay + 1) * SevenDaysConstants.DAY_LENGTH;
        } else {
            targetTick = currentDay * SevenDaysConstants.DAY_LENGTH + 13000;
            if (level.getDayTime() >= targetTick) {
                targetTick += SevenDaysConstants.DAY_LENGTH;
            }
        }
        level.setDayTime(targetTick);
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Time set to " + timeOfDay + "."), true);
        return 1;
    }

    private static int triggerBloodMoon(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(level);

        long currentDay = level.getDayTime() / SevenDaysConstants.DAY_LENGTH;
        long targetTick = currentDay * SevenDaysConstants.DAY_LENGTH + 16000;
        if (level.getDayTime() >= targetTick) {
            targetTick += SevenDaysConstants.DAY_LENGTH;
            currentDay++;
        }

        level.setDayTime(targetTick);
        tracker.setGameDay((int) currentDay + 1);
        tracker.resetDayFlags();
        tracker.startBloodMoon();

        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Blood Moon triggered! Horde spawning now."), true);
        return 1;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildHeatmapCommand() {
        return Commands.literal("heatmap")
                .then(Commands.literal("get")
                        .executes(AdminCommand::heatmapGet)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(AdminCommand::heatmapGetPlayer)))
                .then(Commands.literal("set")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
                                .executes(AdminCommand::heatmapSet)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(AdminCommand::heatmapSetPlayer))))
                .then(Commands.literal("reset")
                        .executes(AdminCommand::heatmapReset));
    }

    private static int heatmapGet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);
        return showHeatForPlayer(ctx, player);
    }

    private static int heatmapGetPlayer(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        return showHeatForPlayer(ctx, player);
    }

    private static int showHeatForPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        HeatmapData data = HeatmapData.getOrCreate(level);
        ChunkPos chunkPos = new ChunkPos(player.blockPosition());
        float heat = data.getHeat(chunkPos);
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + INFO_COLOR + "Heat at " + player.getName().getString() + "'s chunk (" +
                        chunkPos.x + "," + chunkPos.z + "): §e" + String.format("%.1f", heat)), false);
        return 1;
    }

    private static int heatmapSet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);
        float value = FloatArgumentType.getFloat(ctx, "value");
        return setHeatForPlayer(ctx, player, value);
    }

    private static int heatmapSetPlayer(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        float value = FloatArgumentType.getFloat(ctx, "value");
        return setHeatForPlayer(ctx, player, value);
    }

    private static int setHeatForPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player, float value) {
        ServerLevel level = player.serverLevel();
        HeatmapData data = HeatmapData.getOrCreate(level);
        ChunkPos chunkPos = new ChunkPos(player.blockPosition());
        data.getAllChunkSources().remove(ChunkPos.asLong(chunkPos.x, chunkPos.z));
        if (value > 0) {
            data.addHeatSource(chunkPos, value, 0, 0);
        }
        data.setDirty();
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Set heat at " + player.getName().getString() +
                        "'s chunk to " + String.format("%.1f", value)), true);
        return 1;
    }

    private static int heatmapReset(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        HeatmapData data = HeatmapData.getOrCreate(level);
        int count = data.getAllChunkSources().size();
        data.getAllChunkSources().clear();
        data.setDirty();
        HeatmapSpawner.clearCooldowns();
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Heatmap reset. Cleared " + count + " heated chunks."), true);
        return 1;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildDebuffCommand() {
        return Commands.literal("debuff")
                .then(Commands.literal("add")
                        .then(Commands.argument("debuff_id", StringArgumentType.word())
                                .suggests(SUGGEST_DEBUFFS)
                                .executes(ctx -> debuffAdd(ctx, null))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> debuffAdd(ctx, EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("debuff_id", StringArgumentType.word())
                                .suggests(SUGGEST_DEBUFFS)
                                .executes(ctx -> debuffRemove(ctx, null))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> debuffRemove(ctx, EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("clear")
                        .executes(ctx -> debuffClear(ctx, null))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> debuffClear(ctx, EntityArgument.getPlayer(ctx, "player")))));
    }

    private static int debuffAdd(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (target == null) {
            target = ctx.getSource().getPlayer();
            if (target == null) return sendPlayerRequired(ctx);
        }
        String debuffId = StringArgumentType.getString(ctx, "debuff_id");
        if (!isValidDebuff(debuffId)) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR_COLOR + "Unknown debuff: " + debuffId));
            return 0;
        }
        SevenDaysPlayerStats stats = target.getData(ModAttachments.PLAYER_STATS.get());
        stats.addDebuff(debuffId, 6000);
        final String name = target.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Added debuff '" + debuffId + "' to " + name + " (5 min)."), true);
        return 1;
    }

    private static int debuffRemove(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (target == null) {
            target = ctx.getSource().getPlayer();
            if (target == null) return sendPlayerRequired(ctx);
        }
        String debuffId = StringArgumentType.getString(ctx, "debuff_id");
        SevenDaysPlayerStats stats = target.getData(ModAttachments.PLAYER_STATS.get());
        if (!stats.hasDebuff(debuffId)) {
            final String n = target.getName().getString();
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR_COLOR + n + " doesn't have debuff '" + debuffId + "'."));
            return 0;
        }
        stats.removeDebuff(debuffId);
        final String name = target.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Removed debuff '" + debuffId + "' from " + name + "."), true);
        return 1;
    }

    private static int debuffClear(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (target == null) {
            target = ctx.getSource().getPlayer();
            if (target == null) return sendPlayerRequired(ctx);
        }
        SevenDaysPlayerStats stats = target.getData(ModAttachments.PLAYER_STATS.get());
        PlayerStatsHandler.clearAllDebuffs(target, stats);
        final String name = target.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Cleared all debuffs from " + name + "."), true);
        return 1;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildStatsCommand() {
        return Commands.literal("stats")
                .then(Commands.literal("set")
                        .then(Commands.argument("stat", StringArgumentType.word())
                                .suggests(SUGGEST_STATS)
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes(ctx -> statsSet(ctx, null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> statsSet(ctx, EntityArgument.getPlayer(ctx, "player")))))));
    }

    private static int statsSet(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (target == null) {
            target = ctx.getSource().getPlayer();
            if (target == null) return sendPlayerRequired(ctx);
        }
        String stat = StringArgumentType.getString(ctx, "stat").toLowerCase();
        float value = FloatArgumentType.getFloat(ctx, "value");
        SevenDaysPlayerStats stats = target.getData(ModAttachments.PLAYER_STATS.get());

        switch (stat) {
            case "health" -> target.setHealth(value);
            case "stamina" -> stats.setStamina(value);
            case "food" -> stats.setFood(value);
            case "water" -> stats.setWater(value);
            case "temperature" -> stats.setCoreTemperature(value);
            default -> {
                ctx.getSource().sendFailure(Component.literal(
                        PREFIX + ERROR_COLOR + "Unknown stat: " + stat + ". Use: health, stamina, food, water, temperature"));
                return 0;
            }
        }
        final String name = target.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Set " + stat + " to " + String.format("%.1f", value) +
                        " for " + name + "."), true);
        return 1;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildTerritoryCommand() {
        return Commands.literal("territory")
                .then(Commands.literal("spawn")
                        .executes(ctx -> territorySpawn(ctx, 0))
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> territorySpawn(ctx, IntegerArgumentType.getInteger(ctx, "tier")))))
                .then(Commands.literal("clear")
                        .executes(AdminCommand::territoryClear))
                .then(Commands.literal("list")
                        .executes(AdminCommand::territoryList));
    }

    private static int territorySpawn(CommandContext<CommandSourceStack> ctx, int tierNum) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);

        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        TerritoryTier tier = tierNum > 0 ? TerritoryTier.fromNumber(tierNum) : TerritoryTier.roll(level.random);
        TerritoryType type = TerritoryType.randomNonTrader(level.random);

        TerritoryData data = TerritoryData.getOrCreate(level);
        TerritoryRecord record = data.addTerritory(pos, tier, type);

        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Spawned territory #" + record.getId() + " (" +
                        type.getDisplayName() + " " + tier.getStars() + ") at " +
                        pos.getX() + "," + pos.getY() + "," + pos.getZ()), true);
        return 1;
    }

    private static int territoryClear(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);

        ServerLevel level = player.serverLevel();
        TerritoryData data = TerritoryData.getOrCreate(level);
        List<TerritoryRecord> nearby = data.getNearby(player.blockPosition(), 64.0);

        if (nearby.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR_COLOR + "No territories within 64 blocks."));
            return 0;
        }

        TerritoryRecord closest = nearby.get(0);
        double minDist = Double.MAX_VALUE;
        for (TerritoryRecord r : nearby) {
            double dist = player.blockPosition().distSqr(r.getOrigin());
            if (dist < minDist) {
                minDist = dist;
                closest = r;
            }
        }

        closest.setCleared(true);
        data.setDirty();
        final TerritoryRecord c = closest;
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Cleared territory #" + c.getId() + " (" + c.getLabel() + ")."), true);
        return 1;
    }

    private static int territoryList(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        TerritoryData data = TerritoryData.getOrCreate(level);
        Collection<TerritoryRecord> all = data.getAllTerritories();

        if (all.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(PREFIX + INFO_COLOR + "No territories generated yet."), false);
            return 1;
        }

        StringBuilder sb = new StringBuilder(PREFIX + "§fAll territories (" + all.size() + "):\n");
        for (TerritoryRecord r : all) {
            BlockPos origin = r.getOrigin();
            String clearedStr = r.isCleared() ? "§a[CLEARED]" : "§c[ACTIVE]";
            sb.append(String.format("  §e#%d §f%s §7(%s) §7at (%d,%d,%d) %s\n",
                    r.getId(), r.getLabel(), r.getType().getDisplayName(),
                    origin.getX(), origin.getY(), origin.getZ(), clearedStr));
        }

        String text = sb.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    @SuppressWarnings("unchecked")
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildZombieCommand() {
        return Commands.literal("zombie")
                .then(Commands.literal("spawn")
                        .then(Commands.argument("variant", StringArgumentType.word())
                                .suggests(SUGGEST_ZOMBIE_TYPES)
                                .executes(ctx -> zombieSpawn(ctx, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50))
                                        .executes(ctx -> zombieSpawn(ctx, IntegerArgumentType.getInteger(ctx, "count"))))));
    }

    private static int zombieSpawn(CommandContext<CommandSourceStack> ctx, int count) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);

        String variant = StringArgumentType.getString(ctx, "variant").toLowerCase();
        var supplier = ZOMBIE_TYPES.get(variant);
        if (supplier == null) {
            ctx.getSource().sendFailure(Component.literal(
                    PREFIX + ERROR_COLOR + "Unknown variant: " + variant + ". Use tab completion for valid variants."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        EntityType<? extends Mob> entityType = supplier.get();
        int spawned = 0;

        for (int i = 0; i < count; i++) {
            Mob entity = entityType.create(level, EntitySpawnReason.COMMAND);
            if (entity != null) {
                double x = player.getX() + (level.random.nextDouble() - 0.5) * 8;
                double z = player.getZ() + (level.random.nextDouble() - 0.5) * 8;
                double y = player.getY();
                entity.moveTo(x, y, z, level.random.nextFloat() * 360.0f, 0.0f);
                entity.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()),
                        EntitySpawnReason.COMMAND, null);
                level.addFreshEntity(entity);
                spawned++;
            }
        }

        final int s = spawned;
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Spawned " + s + " " + variant + "(s)."), true);
        return 1;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildLootCommand() {
        return Commands.literal("loot")
                .then(Commands.literal("refill").executes(AdminCommand::lootRefill))
                .then(Commands.literal("reset").executes(AdminCommand::lootReset));
    }

    private static int lootRefill(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);

        ServerLevel level = player.serverLevel();
        int refilled = resetLootContainers(level, player.blockPosition(), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Refilled " + refilled + " loot containers in loaded chunks."), true);
        return 1;
    }

    private static int lootReset(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);

        ServerLevel level = player.serverLevel();
        int reset = resetLootContainers(level, player.blockPosition(), true);
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Reset " + reset + " loot containers (timers cleared)."), true);
        return 1;
    }

    private static int resetLootContainers(ServerLevel level, BlockPos center, boolean resetTimers) {
        int count = 0;
        int chunkRadius = 8;
        ChunkPos centerChunk = new ChunkPos(center);

        for (int cx = centerChunk.x - chunkRadius; cx <= centerChunk.x + chunkRadius; cx++) {
            for (int cz = centerChunk.z - chunkRadius; cz <= centerChunk.z + chunkRadius; cz++) {
                if (!level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof LootContainerBlockEntity lootBE) {
                        lootBE.resetForAdmin(resetTimers);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildGiveCommand() {
        return Commands.literal("give")
                .then(Commands.argument("item_id", StringArgumentType.word())
                        .suggests(SUGGEST_MOD_ITEMS)
                        .executes(ctx -> giveItem(ctx, 1, null))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> giveItem(ctx, IntegerArgumentType.getInteger(ctx, "count"), null))
                                .then(Commands.argument("quality", StringArgumentType.word())
                                        .suggests(SUGGEST_QUALITY)
                                        .executes(ctx -> giveItem(ctx, IntegerArgumentType.getInteger(ctx, "count"),
                                                StringArgumentType.getString(ctx, "quality"))))));
    }

    private static int giveItem(CommandContext<CommandSourceStack> ctx, int count, String qualityName) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return sendPlayerRequired(ctx);

        String itemId = StringArgumentType.getString(ctx, "item_id");
        ResourceLocation itemRL = ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, itemId);

        if (!BuiltInRegistries.ITEM.containsKey(itemRL)) {
            ctx.getSource().sendFailure(Component.literal(
                    PREFIX + ERROR_COLOR + "Unknown item: " + itemId));
            return 0;
        }

        Item item = BuiltInRegistries.ITEM.getValue(itemRL);
        ItemStack stack = new ItemStack(item, count);

        if (qualityName != null) {
            try {
                QualityTier tier = QualityTier.valueOf(qualityName.toUpperCase());
                CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                tag.putInt("QualityTier", tier.getLevel());
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.of(tag));
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.literal(
                        PREFIX + ERROR_COLOR + "Unknown quality: " + qualityName +
                                ". Use: poor, good, great, superior, excellent, legendary"));
                return 0;
            }
        }

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                PREFIX + SUCCESS_COLOR + "Gave " + count + "x " + itemId +
                        (qualityName != null ? " (" + qualityName + " quality)" : "") +
                        " to " + player.getName().getString() + "."), true);
        return 1;
    }

    private static boolean isValidDebuff(String id) {
        for (String known : SevenDaysPlayerStats.KNOWN_DEBUFF_IDS) {
            if (known.equals(id)) return true;
        }
        return false;
    }

    private static int sendPlayerRequired(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR_COLOR + "This command must be run by a player."));
        return 0;
    }
}
