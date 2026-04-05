package com.sevendaystominecraft.loot;

import com.mojang.serialization.MapCodec;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Deferred register for all Global Loot Modifier codecs in BZHS.
 */
public class ModLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    SevenDaysToMinecraft.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddItemsLootModifier>>
            ADD_ITEMS = LOOT_MODIFIERS.register("add_items", () -> AddItemsLootModifier.CODEC);
}
