package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Creative mode tabs for Ad Astra Mekanized.
 *
 * Organized by category for easy item discovery:
 * - Materials: All ingots, nuggets, plates, rods, raw materials
 * - Building Blocks: All planet stones, metal blocks, processed blocks
 * - Industrial: All factory blocks, plating, machinery
 * - Decorative: Flags, special decorative items
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdAstraMekanized.MOD_ID);

    // ========== MATERIALS TAB ==========
    public static final Supplier<CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TABS.register("materials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.materials"))
                    .icon(() -> new ItemStack(ModItems.DESH_INGOT.get()))
                    .displayItems((parameters, output) -> {
                        // Metal Ingots (Steel provided by Mekanism)
                        output.accept(ModItems.ETRIUM_INGOT.get());
                        output.accept(ModItems.DESH_INGOT.get());
                        output.accept(ModItems.OSTRUM_INGOT.get());
                        output.accept(ModItems.CALORITE_INGOT.get());

                        // Metal Nuggets (Steel provided by Mekanism)
                        output.accept(ModItems.ETRIUM_NUGGET.get());
                        output.accept(ModItems.DESH_NUGGET.get());
                        output.accept(ModItems.OSTRUM_NUGGET.get());
                        output.accept(ModItems.CALORITE_NUGGET.get());

                        // Raw Materials
                        output.accept(ModItems.RAW_DESH.get());
                        output.accept(ModItems.RAW_OSTRUM.get());
                        output.accept(ModItems.RAW_CALORITE.get());

                        // Processed Materials - Sheets
                        // Iron sheet provided by Create
                        output.accept(ModItems.STEEL_SHEET.get());
                        output.accept(ModItems.ETRIUM_SHEET.get());
                        output.accept(ModItems.DESH_SHEET.get());
                        output.accept(ModItems.OSTRUM_SHEET.get());
                        output.accept(ModItems.CALORITE_SHEET.get());

                        // Processed Materials - Rods
                        output.accept(ModItems.IRON_ROD.get());
                        output.accept(ModItems.STEEL_ROD.get());
                        output.accept(ModItems.ETRIUM_ROD.get());

                        // Special Materials
                        output.accept(ModItems.ETRIONIC_CORE.get());
                        output.accept(ModItems.CHEESE.get());
                    })
                    .build());

    // ========== BUILDING BLOCKS TAB ==========
    public static final Supplier<CreativeModeTab> BUILDING_BLOCKS_TAB = CREATIVE_MODE_TABS.register("building_blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.building_blocks"))
                    .icon(() -> new ItemStack(ModBlocks.DESH_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        // Metal Blocks
                        output.accept(ModBlocks.STEEL_BLOCK.get());
                        output.accept(ModBlocks.ETRIUM_BLOCK.get());
                        output.accept(ModBlocks.DESH_BLOCK.get());
                        output.accept(ModBlocks.OSTRUM_BLOCK.get());
                        output.accept(ModBlocks.CALORITE_BLOCK.get());

                        // Raw Material Blocks
                        output.accept(ModBlocks.RAW_DESH_BLOCK.get());
                        output.accept(ModBlocks.RAW_OSTRUM_BLOCK.get());
                        output.accept(ModBlocks.RAW_CALORITE_BLOCK.get());

                        // Ore Blocks
                        output.accept(ModBlocks.DESH_ORE.get());
                        output.accept(ModBlocks.OSTRUM_ORE.get());
                        output.accept(ModBlocks.CALORITE_ORE.get());
                        output.accept(ModBlocks.ETRIUM_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_DESH_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_OSTRUM_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_CALORITE_ORE.get());

                        // Planet-Specific Ores
                        output.accept(ModBlocks.MOON_DESH_ORE.get());
                        output.accept(ModBlocks.MOON_IRON_ORE.get());
                        output.accept(ModBlocks.MOON_ETRIUM_ORE.get());
                        output.accept(ModBlocks.MOON_CHEESE_ORE.get());
                        output.accept(ModBlocks.MARS_IRON_ORE.get());
                        output.accept(ModBlocks.MARS_DIAMOND_ORE.get());
                        output.accept(ModBlocks.MARS_OSTRUM_ORE.get());
                        output.accept(ModBlocks.MARS_ETRIUM_ORE.get());
                        output.accept(ModBlocks.VENUS_COAL_ORE.get());
                        output.accept(ModBlocks.VENUS_GOLD_ORE.get());
                        output.accept(ModBlocks.VENUS_DIAMOND_ORE.get());
                        output.accept(ModBlocks.VENUS_CALORITE_ORE.get());
                        output.accept(ModBlocks.MERCURY_IRON_ORE.get());
                        output.accept(ModBlocks.GLACIO_COAL_ORE.get());
                        output.accept(ModBlocks.GLACIO_COPPER_ORE.get());
                        output.accept(ModBlocks.GLACIO_IRON_ORE.get());
                        output.accept(ModBlocks.GLACIO_LAPIS_ORE.get());
                        output.accept(ModBlocks.GLACIO_ETRIUM_ORE.get());

                        // Special Blocks
                        output.accept(ModBlocks.CHEESE_BLOCK.get());

                        // Moon Stones
                        output.accept(ModBlocks.MOON_STONE.get());
                        output.accept(ModBlocks.MOON_STONE_STAIRS.get());
                        output.accept(ModBlocks.MOON_STONE_SLAB.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.MOON_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.MOON_DEEPSLATE.get());
                        output.accept(ModBlocks.MOON_SAND.get());

                        // Mars Stones
                        output.accept(ModBlocks.MARS_STONE.get());
                        output.accept(ModBlocks.MARS_STONE_STAIRS.get());
                        output.accept(ModBlocks.MARS_STONE_SLAB.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.MARS_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.MARS_SAND.get());
                        output.accept(ModBlocks.CONGLOMERATE.get());
                        output.accept(ModBlocks.POLISHED_CONGLOMERATE.get());

                        // Venus Stones
                        output.accept(ModBlocks.VENUS_STONE.get());
                        output.accept(ModBlocks.VENUS_STONE_STAIRS.get());
                        output.accept(ModBlocks.VENUS_STONE_SLAB.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.VENUS_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.VENUS_SAND.get());
                        output.accept(ModBlocks.VENUS_SANDSTONE.get());

                        // Mercury Stones
                        output.accept(ModBlocks.MERCURY_STONE.get());
                        output.accept(ModBlocks.MERCURY_STONE_STAIRS.get());
                        output.accept(ModBlocks.MERCURY_STONE_SLAB.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.MERCURY_COBBLESTONE_SLAB.get());

                        // Glacio Stones
                        output.accept(ModBlocks.GLACIO_STONE.get());
                        output.accept(ModBlocks.GLACIO_STONE_STAIRS.get());
                        output.accept(ModBlocks.GLACIO_STONE_SLAB.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.GLACIO_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.PERMAFROST.get());

                        // Sky Stone
                        output.accept(ModBlocks.SKY_STONE.get());
                        output.accept(ModBlocks.SKY_STONE_STAIRS.get());
                        output.accept(ModBlocks.SKY_STONE_SLAB.get());

                        // Alien Wood Set
                        output.accept(ModBlocks.GLACIAN_LOG.get());
                        output.accept(ModBlocks.STRIPPED_GLACIAN_LOG.get());
                        output.accept(ModBlocks.GLACIAN_PLANKS.get());
                        output.accept(ModBlocks.GLACIAN_LEAVES.get());
                        output.accept(ModBlocks.AERONOS_MUSHROOM.get());
                        output.accept(ModBlocks.AERONOS_STEM.get());
                        output.accept(ModBlocks.STROPHAR_MUSHROOM.get());
                        output.accept(ModBlocks.STROPHAR_STEM.get());
                    })
                    .build());

    // ========== INDUSTRIAL TAB ==========
    public static final Supplier<CreativeModeTab> INDUSTRIAL_TAB = CREATIVE_MODE_TABS.register("industrial",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.industrial"))
                    .icon(() -> new ItemStack(ModBlocks.STEEL_FACTORY_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        // Factory Blocks
                        output.accept(ModBlocks.IRON_FACTORY_BLOCK.get());
                        output.accept(ModBlocks.STEEL_FACTORY_BLOCK.get());

                        // Plating Blocks
                        output.accept(ModBlocks.IRON_PLATING.get());
                        output.accept(ModBlocks.STEEL_PLATING.get());
                        output.accept(ModBlocks.DESH_PLATING.get());
                        output.accept(ModBlocks.OSTRUM_PLATING.get());
                        output.accept(ModBlocks.CALORITE_PLATING.get());

                        // Sheet Blocks
                        output.accept(ModBlocks.IRON_SHEETBLOCK.get());
                        output.accept(ModBlocks.STEEL_SHEETBLOCK.get());
                        output.accept(ModBlocks.ETRIUM_SHEETBLOCK.get());
                        output.accept(ModBlocks.DESH_SHEETBLOCK.get());
                        output.accept(ModBlocks.OSTRUM_SHEETBLOCK.get());
                        output.accept(ModBlocks.CALORITE_SHEETBLOCK.get());

                        // Panel Blocks
                        output.accept(ModBlocks.IRON_PANEL.get());
                        output.accept(ModBlocks.STEEL_PANEL.get());
                        output.accept(ModBlocks.ETRIUM_PANEL.get());
                        output.accept(ModBlocks.DESH_PANEL.get());
                        output.accept(ModBlocks.OSTRUM_PANEL.get());
                        output.accept(ModBlocks.CALORITE_PANEL.get());

                        // Encased Blocks
                        output.accept(ModBlocks.ENCASED_IRON_BLOCK.get());
                        output.accept(ModBlocks.ENCASED_STEEL_BLOCK.get());

                        // Pillar Blocks
                        output.accept(ModBlocks.IRON_PILLAR.get());
                        output.accept(ModBlocks.STEEL_PILLAR.get());
                        output.accept(ModBlocks.DESH_PILLAR.get());
                        output.accept(ModBlocks.OSTRUM_PILLAR.get());
                        output.accept(ModBlocks.CALORITE_PILLAR.get());
                        output.accept(ModBlocks.MARKED_IRON_PILLAR.get());

                        // Glowing Pillar Blocks
                        output.accept(ModBlocks.GLOWING_IRON_PILLAR.get());
                        output.accept(ModBlocks.GLOWING_STEEL_PILLAR.get());
                        output.accept(ModBlocks.GLOWING_DESH_PILLAR.get());
                        output.accept(ModBlocks.GLOWING_OSTRUM_PILLAR.get());
                        output.accept(ModBlocks.GLOWING_CALORITE_PILLAR.get());

                        // Mekanism-Compatible Workstations
                        output.accept(ModBlocks.OXYGEN_DISTRIBUTOR.get());
                        output.accept(ModBlocks.GRAVITY_NORMALIZER.get());
                        output.accept(ModBlocks.WIRELESS_POWER_RELAY.get());
                        output.accept(ModBlocks.NASA_WORKBENCH.get());
                        // Oxygen Network Monitor removed - incomplete feature
                        output.accept(ModItems.OXYGEN_NETWORK_CONTROLLER.get());

                        // Rocket Components
                        output.accept(ModItems.ROCKET_NOSE_CONE.get());
                        output.accept(ModItems.ROCKET_FIN.get());
                        output.accept(ModItems.FAN.get());
                        output.accept(ModItems.ENGINE_FRAME.get());
                        output.accept(ModItems.GAS_TANK.get());
                        output.accept(ModItems.LARGE_GAS_TANK.get());
                        output.accept(ModItems.STEEL_ENGINE.get());
                        output.accept(ModItems.DESH_ENGINE.get());
                        output.accept(ModItems.OSTRUM_ENGINE.get());
                        output.accept(ModItems.CALORITE_ENGINE.get());
                        output.accept(ModItems.STEEL_TANK.get());
                        output.accept(ModItems.DESH_TANK.get());
                        output.accept(ModItems.OSTRUM_TANK.get());
                        output.accept(ModItems.CALORITE_TANK.get());
                        output.accept(ModItems.OXYGEN_GEAR.get());

                        // Rockets
                        output.accept(ModItems.TIER_1_ROCKET.get());
                        output.accept(ModItems.TIER_2_ROCKET.get());
                        output.accept(ModItems.TIER_3_ROCKET.get());
                        output.accept(ModItems.TIER_4_ROCKET.get());

                        // Rocket Infrastructure
                        output.accept(ModBlocks.LAUNCH_PAD.get());

                        // Redstone Components
                        output.accept(ModItems.ETRIONIC_CAPACITOR.get());
                        output.accept(ModBlocks.REDSTONE_TOGGLE_RELAY.get());

                        // Space Suits (also in Equipment tab)
                        output.accept(ModItems.SPACE_HELMET.get());
                        output.accept(ModItems.SPACE_SUIT.get());
                        output.accept(ModItems.SPACE_PANTS.get());
                        output.accept(ModItems.SPACE_BOOTS.get());
                        output.accept(ModItems.NETHERITE_SPACE_HELMET.get());
                        output.accept(ModItems.NETHERITE_SPACE_SUIT.get());
                        output.accept(ModItems.NETHERITE_SPACE_PANTS.get());
                        output.accept(ModItems.NETHERITE_SPACE_BOOTS.get());
                        output.accept(ModItems.JET_SUIT_HELMET.get());
                        output.accept(ModItems.JET_SUIT.get());
                        output.accept(ModItems.JET_SUIT_PANTS.get());
                        output.accept(ModItems.JET_SUIT_BOOTS.get());

                        // Doors & Access
                        output.accept(ModBlocks.STEEL_DOOR.get());
                        output.accept(ModBlocks.STEEL_TRAPDOOR.get());
                        // output.accept(ModBlocks.AIRLOCK.get()); // TODO: Add when airlock is implemented
                        output.accept(ModBlocks.REINFORCED_DOOR.get());
                        output.accept(ModBlocks.IRON_SLIDING_DOOR.get());
                        output.accept(ModBlocks.STEEL_SLIDING_DOOR.get());
                        output.accept(ModBlocks.DESH_SLIDING_DOOR.get());
                        output.accept(ModBlocks.OSTRUM_SLIDING_DOOR.get());
                        output.accept(ModBlocks.CALORITE_SLIDING_DOOR.get());
                    })
                    .build());

    // ========== EQUIPMENT TAB ==========
    public static final Supplier<CreativeModeTab> EQUIPMENT_TAB = CREATIVE_MODE_TABS.register("equipment",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.equipment"))
                    .icon(() -> new ItemStack(ModItems.JET_SUIT.get()))
                    .displayItems((parameters, output) -> {
                        // Standard Space Suits
                        output.accept(ModItems.SPACE_HELMET.get());
                        output.accept(ModItems.SPACE_SUIT.get());
                        output.accept(ModItems.SPACE_PANTS.get());
                        output.accept(ModItems.SPACE_BOOTS.get());

                        // Netherite Space Suits
                        output.accept(ModItems.NETHERITE_SPACE_HELMET.get());
                        output.accept(ModItems.NETHERITE_SPACE_SUIT.get());
                        output.accept(ModItems.NETHERITE_SPACE_PANTS.get());
                        output.accept(ModItems.NETHERITE_SPACE_BOOTS.get());

                        // Jet Suits
                        output.accept(ModItems.JET_SUIT_HELMET.get());
                        output.accept(ModItems.JET_SUIT.get());
                        output.accept(ModItems.JET_SUIT_PANTS.get());
                        output.accept(ModItems.JET_SUIT_BOOTS.get());

                        // Equipment Items
                        output.accept(ModItems.ASTRONOMER_JOURNAL.get());
                    })
                    .build());

    // ========== DECORATIVE TAB ==========
    public static final Supplier<CreativeModeTab> DECORATIVE_TAB = CREATIVE_MODE_TABS.register("decorative",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.adastramekanized.decorative"))
                    .icon(() -> new ItemStack(ModBlocks.CHEESE_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        // Decorative items will be added in future updates
                    })
                    .build());

    /**
     * Register all creative mode tabs
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered {} creative mode tabs", CREATIVE_MODE_TABS.getEntries().size());
    }
}