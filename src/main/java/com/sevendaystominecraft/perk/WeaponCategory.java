package com.sevendaystominecraft.perk;

import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import net.minecraft.world.item.*;

public enum WeaponCategory {
    FIST,
    CLUB,
    SLEDGEHAMMER,
    KNIFE,
    SPEAR,
    BOW,
    PISTOL,
    RIFLE,
    SHOTGUN,
    MACHINE_GUN,
    STUN_BATON,
    NONE;

    public static WeaponCategory fromItemStack(ItemStack stack) {
        if (stack.isEmpty()) return FIST;

        Item item = stack.getItem();

        if (item instanceof GeoRangedWeaponItem ranged) {
            return switch (ranged.getWeaponType()) {
                case PISTOL_9MM, SMG -> PISTOL;
                case AK47, HUNTING_RIFLE, SNIPER_RIFLE -> RIFLE;
                case SHOTGUN -> SHOTGUN;
                case M60 -> MACHINE_GUN;
            };
        }

        if (item == ModItems.STONE_CLUB.get() || item == ModItems.BASEBALL_BAT.get()) return CLUB;
        if (item == ModItems.IRON_SLEDGEHAMMER.get()) return SLEDGEHAMMER;
        if (item instanceof BowItem || item instanceof CrossbowItem) return BOW;
        if (item instanceof TridentItem) return SPEAR;
        if (item instanceof SwordItem) return KNIFE;

        return NONE;
    }

    public String getPerkId() {
        return switch (this) {
            case FIST -> "brawler";
            case CLUB -> "iron_fists";
            case SLEDGEHAMMER -> "skull_crusher";
            case KNIFE -> "deep_cuts";
            case SPEAR -> "spear_master";
            case BOW -> "archery";
            case PISTOL -> "gunslinger";
            case RIFLE -> "rifle_guy";
            case SHOTGUN -> "boomstick";
            case MACHINE_GUN -> "rifle_guy";
            case STUN_BATON -> "electrocutioner";
            case NONE -> null;
        };
    }

    public float getDamagePerRank() {
        return switch (this) {
            case FIST, CLUB, SLEDGEHAMMER, KNIFE, SPEAR, BOW, PISTOL, RIFLE, SHOTGUN, MACHINE_GUN -> 0.10f;
            case STUN_BATON -> 0.15f;
            case NONE -> 0.0f;
        };
    }

    public boolean isMelee() {
        return switch (this) {
            case FIST, CLUB, SLEDGEHAMMER, KNIFE, SPEAR, STUN_BATON -> true;
            default -> false;
        };
    }

    public boolean isRanged() {
        return switch (this) {
            case BOW, PISTOL, RIFLE, SHOTGUN, MACHINE_GUN -> true;
            default -> false;
        };
    }
}
