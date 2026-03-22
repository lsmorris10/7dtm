package com.sevendaystominecraft.menu;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.farming.DewCollectorMenu;
import com.sevendaystominecraft.block.loot.LootContainerMenu;
import com.sevendaystominecraft.block.workstation.WorkstationMenu;
import com.sevendaystominecraft.trader.TraderMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<MenuType<WorkstationMenu>> WORKSTATION_MENU =
            MENU_TYPES.register("workstation", () ->
                    IMenuTypeExtension.create(WorkstationMenu::fromNetwork));

    public static final Supplier<MenuType<LootContainerMenu>> LOOT_CONTAINER_MENU =
            MENU_TYPES.register("loot_container", () ->
                    IMenuTypeExtension.create(LootContainerMenu::fromNetwork));

    public static final Supplier<MenuType<TraderMenu>> TRADER_MENU =
            MENU_TYPES.register("trader", () ->
                    IMenuTypeExtension.create(TraderMenu::fromNetwork));

    public static final Supplier<MenuType<DewCollectorMenu>> DEW_COLLECTOR_MENU =
            MENU_TYPES.register("dew_collector", () ->
                    IMenuTypeExtension.create(DewCollectorMenu::fromNetwork));
}
