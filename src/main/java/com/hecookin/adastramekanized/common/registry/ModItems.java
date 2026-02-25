package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.items.AstronomerJournalItem;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.items.armor.*;
import com.hecookin.adastramekanized.common.items.armor.base.CustomDyeableArmorItem;
import com.hecookin.adastramekanized.common.items.armor.materials.*;
import com.hecookin.adastramekanized.common.items.vehicles.RocketItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Central registry for all mod items.
 *
 * Organized by category for easy maintenance and debugging.
 * All Ad Astra material items are registered here.
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, AdAstraMekanized.MOD_ID);

    // ========== METAL INGOTS ==========
    // Note: Steel ingot provided by Mekanism (mekanism:steel_ingot)

    public static final Supplier<Item> ETRIUM_INGOT = ITEMS.register("etrium_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> DESH_INGOT = ITEMS.register("desh_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> OSTRUM_INGOT = ITEMS.register("ostrum_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CALORITE_INGOT = ITEMS.register("calorite_ingot",
            () -> new Item(new Item.Properties().fireResistant()));

    // ========== METAL NUGGETS ==========
    // Note: Steel nugget provided by Mekanism (mekanism:steel_nugget)

    public static final Supplier<Item> ETRIUM_NUGGET = ITEMS.register("etrium_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> DESH_NUGGET = ITEMS.register("desh_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> OSTRUM_NUGGET = ITEMS.register("ostrum_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CALORITE_NUGGET = ITEMS.register("calorite_nugget",
            () -> new Item(new Item.Properties()));

    // ========== RAW MATERIALS ==========

    public static final Supplier<Item> RAW_DESH = ITEMS.register("raw_desh",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> RAW_OSTRUM = ITEMS.register("raw_ostrum",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> RAW_CALORITE = ITEMS.register("raw_calorite",
            () -> new Item(new Item.Properties()));

    // ========== PROCESSED MATERIALS ==========

    public static final Supplier<Item> IRON_SHEET = ITEMS.register("iron_sheet",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> IRON_ROD = ITEMS.register("iron_rod",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_SHEET = ITEMS.register("steel_sheet",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_ROD = ITEMS.register("steel_rod",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ETRIUM_SHEET = ITEMS.register("etrium_sheet",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ETRIUM_ROD = ITEMS.register("etrium_rod",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> DESH_SHEET = ITEMS.register("desh_sheet",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> OSTRUM_SHEET = ITEMS.register("ostrum_sheet",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CALORITE_SHEET = ITEMS.register("calorite_sheet",
            () -> new Item(new Item.Properties()));

    // ========== TOOLS & MOLDS ==========

    public static final Supplier<Item> SHEET_DIE = ITEMS.register("sheet_die",
            () -> new Item(new Item.Properties().stacksTo(1)));

    // ========== SPECIAL MATERIALS ==========

    public static final Supplier<Item> ETRIONIC_CORE = ITEMS.register("etrionic_core",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CHEESE = ITEMS.register("cheese",
            () -> new Item(new Item.Properties().food(FoodProperties.CHEESE)));

    public static final Supplier<Item> ETRIONIC_CAPACITOR = ITEMS.register("etrionic_capacitor",
            () -> new Item(new Item.Properties()));

    // ========== TECHNICAL ITEMS ==========

    public static final Supplier<Item> OXYGEN_NETWORK_CONTROLLER = ITEMS.register("oxygen_network_controller",
            () -> new OxygenNetworkController());

    public static final Supplier<Item> ASTRONOMER_JOURNAL = ITEMS.register("astronomer_journal",
            () -> new AstronomerJournalItem(new Item.Properties().stacksTo(1)));

    // ========== ROCKET COMPONENTS ==========

    public static final Supplier<Item> ROCKET_NOSE_CONE = ITEMS.register("rocket_nose_cone",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ROCKET_FIN = ITEMS.register("rocket_fin",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_ENGINE = ITEMS.register("steel_engine",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> DESH_ENGINE = ITEMS.register("desh_engine",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> OSTRUM_ENGINE = ITEMS.register("ostrum_engine",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CALORITE_ENGINE = ITEMS.register("calorite_engine",
            () -> new Item(new Item.Properties().fireResistant()));

    public static final Supplier<Item> STEEL_TANK = ITEMS.register("steel_tank",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> DESH_TANK = ITEMS.register("desh_tank",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> OSTRUM_TANK = ITEMS.register("ostrum_tank",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CALORITE_TANK = ITEMS.register("calorite_tank",
            () -> new Item(new Item.Properties().fireResistant()));

    public static final Supplier<Item> OXYGEN_GEAR = ITEMS.register("oxygen_gear",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> FAN = ITEMS.register("fan",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ENGINE_FRAME = ITEMS.register("engine_frame",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> GAS_TANK = ITEMS.register("gas_tank",
            () -> new com.hecookin.adastramekanized.common.items.GasTankItem(10000L, new Item.Properties())); // 10,000 mB capacity

    // ========== ROCKETS ==========

    public static final Supplier<Item> TIER_1_ROCKET = ITEMS.register("tier_1_rocket",
            () -> new RocketItem(ModEntityTypes.TIER_1_ROCKET, new Item.Properties().stacksTo(1).fireResistant()));

    public static final Supplier<Item> TIER_2_ROCKET = ITEMS.register("tier_2_rocket",
            () -> new RocketItem(ModEntityTypes.TIER_2_ROCKET, new Item.Properties().stacksTo(1).fireResistant()));

    public static final Supplier<Item> TIER_3_ROCKET = ITEMS.register("tier_3_rocket",
            () -> new RocketItem(ModEntityTypes.TIER_3_ROCKET, new Item.Properties().stacksTo(1).fireResistant()));

    public static final Supplier<Item> TIER_4_ROCKET = ITEMS.register("tier_4_rocket",
            () -> new RocketItem(ModEntityTypes.TIER_4_ROCKET, new Item.Properties().stacksTo(1).fireResistant()));

    public static final Supplier<Item> LARGE_GAS_TANK = ITEMS.register("large_gas_tank",
            () -> new com.hecookin.adastramekanized.common.items.GasTankItem(50000L, new Item.Properties())); // 50,000 mB capacity

    // ========== SPACE SUITS - STANDARD ==========

    public static final Supplier<Item> SPACE_HELMET = ITEMS.register("space_helmet",
            () -> new CustomDyeableArmorItem(Holder.direct(SpaceSuitMaterial.MATERIAL), ArmorItem.Type.HELMET,
                    new Item.Properties().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> SPACE_SUIT = ITEMS.register("space_suit",
            () -> new SpaceSuitItem(Holder.direct(SpaceSuitMaterial.MATERIAL), ArmorItem.Type.CHESTPLATE, 2000,
                    new Item.Properties().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> SPACE_PANTS = ITEMS.register("space_pants",
            () -> new CustomDyeableArmorItem(Holder.direct(SpaceSuitMaterial.MATERIAL), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> SPACE_BOOTS = ITEMS.register("space_boots",
            () -> new CustomDyeableArmorItem(Holder.direct(SpaceSuitMaterial.MATERIAL), ArmorItem.Type.BOOTS,
                    new Item.Properties().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    // ========== SPACE SUITS - NETHERITE ==========

    public static final Supplier<Item> NETHERITE_SPACE_HELMET = ITEMS.register("netherite_space_helmet",
            () -> new CustomDyeableArmorItem(Holder.direct(NetheriteSpaceSuitMaterial.MATERIAL), ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> NETHERITE_SPACE_SUIT = ITEMS.register("netherite_space_suit",
            () -> new NetheriteSpaceSuitItem(Holder.direct(NetheriteSpaceSuitMaterial.MATERIAL), ArmorItem.Type.CHESTPLATE, 4000,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> NETHERITE_SPACE_PANTS = ITEMS.register("netherite_space_pants",
            () -> new CustomDyeableArmorItem(Holder.direct(NetheriteSpaceSuitMaterial.MATERIAL), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> NETHERITE_SPACE_BOOTS = ITEMS.register("netherite_space_boots",
            () -> new CustomDyeableArmorItem(Holder.direct(NetheriteSpaceSuitMaterial.MATERIAL), ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    // ========== JET SUITS ==========

    public static final Supplier<Item> JET_SUIT_HELMET = ITEMS.register("jet_suit_helmet",
            () -> new CustomDyeableArmorItem(Holder.direct(JetSuitMaterial.MATERIAL), ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> JET_SUIT = ITEMS.register("jet_suit",
            () -> new JetSuitItem(Holder.direct(JetSuitMaterial.MATERIAL), ArmorItem.Type.CHESTPLATE, 8000, 8000,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> JET_SUIT_PANTS = ITEMS.register("jet_suit_pants",
            () -> new CustomDyeableArmorItem(Holder.direct(JetSuitMaterial.MATERIAL), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    public static final Supplier<Item> JET_SUIT_BOOTS = ITEMS.register("jet_suit_boots",
            () -> new CustomDyeableArmorItem(Holder.direct(JetSuitMaterial.MATERIAL), ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant().component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF, false))));

    /**
     * Food properties for items
     */
    public static class FoodProperties {
        public static final net.minecraft.world.food.FoodProperties CHEESE =
                new net.minecraft.world.food.FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.3f)
                        .build();
    }

    /**
     * Register all mod items
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered {} mod items", ITEMS.getEntries().size());
    }
}