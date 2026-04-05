package com.sevendaystominecraft.magazine;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.function.Supplier;

public class ModMagazines {

    private static final Map<String, Supplier<Item>> MAGAZINE_ITEMS = new LinkedHashMap<>();

    static {
        for (MagazineSeries series : MagazineRegistry.getAllSeries()) {
            for (int issue = 1; issue <= series.issueCount(); issue++) {
                String name = "magazine_" + series.id() + "_" + issue;
                final int issueFinal = issue;
                Supplier<Item> supplier = ModItems.ITEMS.register(name,
                        () -> new MagazineItem(
                                new Item.Properties()
                                        .setId(ResourceKey.create(Registries.ITEM,
                                                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name)))
                                        .stacksTo(1),
                                series.id(), issueFinal));
                MAGAZINE_ITEMS.put(name, supplier);
            }
        }
    }

    public static void init() {
    }

    public static Collection<Supplier<Item>> getAllMagazineItems() {
        return Collections.unmodifiableCollection(MAGAZINE_ITEMS.values());
    }

    public static Supplier<Item> getMagazineItem(String seriesId, int issue) {
        return MAGAZINE_ITEMS.get("magazine_" + seriesId + "_" + issue);
    }
}
