package com.sevendaystominecraft.magazine;

import java.util.*;

public class MagazineRegistry {

    private static final Map<String, MagazineSeries> SERIES = new LinkedHashMap<>();

    static {
        register(new MagazineSeries("steady_steve", "Steady Steve", 7,
                List.of(
                        "+3% pistol damage",
                        "+5% pistol reload speed",
                        "+3% pistol damage",
                        "+10% pistol range",
                        "+5% pistol reload speed",
                        "+5% pistol critical chance",
                        "+3% pistol damage"
                ),
                "Mastery: 20% chance to not consume ammo with pistols"
        ));

        register(new MagazineSeries("block_brawler", "Block Brawler", 5,
                List.of(
                        "+5% fist/club damage",
                        "+10% club knockback",
                        "+5% fist/club damage",
                        "+15% power attack damage",
                        "+5% fist/club damage"
                ),
                "Mastery: Power attacks have 25% chance to stun"
        ));

        register(new MagazineSeries("sharpshot_sam", "Sharpshot Sam", 7,
                List.of(
                        "+3% rifle damage",
                        "+5% rifle reload speed",
                        "+5% scope stability",
                        "+3% rifle damage",
                        "+10% rifle range",
                        "+5% rifle critical chance",
                        "+3% rifle damage"
                ),
                "Mastery: 15% chance for headshots to deal double damage"
        ));

        register(new MagazineSeries("the_tinkerer", "The Tinkerer", 5,
                List.of(
                        "+5% repair speed",
                        "-5% repair material cost",
                        "+5% repair speed",
                        "-5% repair material cost",
                        "+10% repaired item durability bonus"
                ),
                "Mastery: Repaired items gain +1 quality tier (max 5)"
        ));

        register(new MagazineSeries("overworld_chef", "Overworld Chef", 5,
                List.of(
                        "+5% food restore from cooked items",
                        "+5% water restore from drinks",
                        "+5% food restore from cooked items",
                        "Cooking uses 10% less ingredients",
                        "+5% food restore from cooked items"
                ),
                "Mastery: Crafted food items restore 20% stamina on consumption"
        ));

        register(new MagazineSeries("dungeon_tactician", "Dungeon Tactician", 7,
                List.of(
                        "+3% shotgun damage",
                        "+10% shotgun spread tightness",
                        "+5% shotgun reload speed",
                        "+3% shotgun damage",
                        "+5% shotgun pellet count",
                        "+5% shotgun critical chance",
                        "+3% shotgun damage"
                ),
                "Mastery: Shotgun kills within 5 blocks have 30% chance to dismember"
        ));
    }

    private static void register(MagazineSeries series) {
        SERIES.put(series.id(), series);
    }

    public static MagazineSeries getSeries(String id) {
        return SERIES.get(id);
    }

    public static Collection<MagazineSeries> getAllSeries() {
        return Collections.unmodifiableCollection(SERIES.values());
    }

    public static boolean isValidSeries(String id) {
        return SERIES.containsKey(id);
    }
}
