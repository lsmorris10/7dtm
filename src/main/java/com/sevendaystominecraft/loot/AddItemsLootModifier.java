package com.sevendaystominecraft.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;

/**
 * A Global Loot Modifier that adds a list of weighted item entries to any
 * matching vanilla (or modded) loot table.  Each entry specifies the item,
 * a roll chance (0‒1), and a min/max stack count.
 */
public class AddItemsLootModifier extends LootModifier {

    /** An individual loot entry that can be added to a chest / mob drop. */
    public record LootEntry(Item item, float chance, int minCount, int maxCount) {

        public static final Codec<LootEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(LootEntry::item),
                Codec.FLOAT.fieldOf("chance").forGetter(LootEntry::chance),
                Codec.INT.optionalFieldOf("min_count", 1).forGetter(LootEntry::minCount),
                Codec.INT.optionalFieldOf("max_count", 1).forGetter(LootEntry::maxCount)
        ).apply(inst, LootEntry::new));
    }

    public static final MapCodec<AddItemsLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    LootEntry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.entries)
            ).apply(inst, AddItemsLootModifier::new)
    );

    private final List<LootEntry> entries;

    public AddItemsLootModifier(LootItemCondition[] conditions, List<LootEntry> entries) {
        super(conditions);
        this.entries = entries;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (LootEntry entry : entries) {
            if (context.getRandom().nextFloat() < entry.chance()) {
                int count = entry.minCount();
                if (entry.maxCount() > entry.minCount()) {
                    count += context.getRandom().nextInt(entry.maxCount() - entry.minCount() + 1);
                }
                generatedLoot.add(new ItemStack(entry.item(), count));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
