package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blocks.BlockProperties;
import com.hecookin.adastramekanized.common.blocks.machines.GravityNormalizerBlock;
import com.hecookin.adastramekanized.common.blocks.machines.OxygenDistributorBlock;
import com.hecookin.adastramekanized.common.blocks.machines.WirelessPowerRelayBlock;
import com.hecookin.adastramekanized.common.blocks.RedstoneToggleRelay;
import com.hecookin.adastramekanized.common.blocks.OxygenNetworkMonitorBlock;
import com.hecookin.adastramekanized.common.blocks.SlidingDoorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Central registry for all mod blocks.
 *
 * Organized by category for easy maintenance and debugging.
 * All blocks automatically get corresponding BlockItems.
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AdAstraMekanized.MOD_ID);

    // ========== METAL BLOCKS ==========

    public static final Supplier<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> ETRIUM_BLOCK = registerBlock("etrium_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> DESH_BLOCK = registerBlock("desh_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> OSTRUM_BLOCK = registerBlock("ostrum_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    public static final Supplier<Block> CALORITE_BLOCK = registerBlock("calorite_block",
            () -> new Block(BlockProperties.METAL_BLOCK));

    // ========== RAW MATERIAL BLOCKS ==========

    public static final Supplier<Block> RAW_DESH_BLOCK = registerBlock("raw_desh_block",
            () -> new Block(BlockProperties.RAW_METAL_BLOCK));

    public static final Supplier<Block> RAW_OSTRUM_BLOCK = registerBlock("raw_ostrum_block",
            () -> new Block(BlockProperties.RAW_METAL_BLOCK));

    public static final Supplier<Block> RAW_CALORITE_BLOCK = registerBlock("raw_calorite_block",
            () -> new Block(BlockProperties.RAW_METAL_BLOCK));

    // ========== SPECIAL BLOCKS ==========

    public static final Supplier<Block> CHEESE_BLOCK = registerBlock("cheese_block",
            () -> new Block(BlockProperties.CHEESE_BLOCK));

    public static final Supplier<Block> SKY_STONE = registerBlock("sky_stone",
            () -> new Block(BlockProperties.SKY_STONE));

    // ========== MOON STONES ==========

    public static final Supplier<Block> MOON_STONE = registerBlock("moon_stone",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_COBBLESTONE = registerBlock("moon_cobblestone",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_DEEPSLATE = registerBlock("moon_deepslate",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_SAND = registerBlock("moon_sand",
            () -> new Block(BlockProperties.moonStone()));

    // ========== MARS STONES ==========

    public static final Supplier<Block> MARS_STONE = registerBlock("mars_stone",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_COBBLESTONE = registerBlock("mars_cobblestone",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_SAND = registerBlock("mars_sand",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> CONGLOMERATE = registerBlock("conglomerate",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> POLISHED_CONGLOMERATE = registerBlock("polished_conglomerate",
            () -> new Block(BlockProperties.marsStone()));

    // ========== VENUS STONES ==========

    public static final Supplier<Block> VENUS_STONE = registerBlock("venus_stone",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_COBBLESTONE = registerBlock("venus_cobblestone",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_SAND = registerBlock("venus_sand",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_SANDSTONE = registerBlock("venus_sandstone",
            () -> new Block(BlockProperties.venusStone()));

    // ========== MERCURY STONES ==========

    public static final Supplier<Block> MERCURY_STONE = registerBlock("mercury_stone",
            () -> new Block(BlockProperties.mercuryStone()));

    public static final Supplier<Block> MERCURY_COBBLESTONE = registerBlock("mercury_cobblestone",
            () -> new Block(BlockProperties.mercuryStone()));

    // ========== GLACIO STONES ==========

    public static final Supplier<Block> GLACIO_STONE = registerBlock("glacio_stone",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_COBBLESTONE = registerBlock("glacio_cobblestone",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> PERMAFROST = registerBlock("permafrost",
            () -> new Block(BlockProperties.glacioStone()));

    // ========== MOON STONE VARIANTS ==========

    public static final Supplier<Block> MOON_STONE_STAIRS = registerBlock("moon_stone_stairs",
            () -> new StairBlock(MOON_STONE.get().defaultBlockState(), BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_STONE_SLAB = registerBlock("moon_stone_slab",
            () -> new SlabBlock(BlockProperties.moonStone()));


    public static final Supplier<Block> MOON_COBBLESTONE_STAIRS = registerBlock("moon_cobblestone_stairs",
            () -> new StairBlock(MOON_COBBLESTONE.get().defaultBlockState(), BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_COBBLESTONE_SLAB = registerBlock("moon_cobblestone_slab",
            () -> new SlabBlock(BlockProperties.moonStone()));


    // ========== MARS STONE VARIANTS ==========

    public static final Supplier<Block> MARS_STONE_STAIRS = registerBlock("mars_stone_stairs",
            () -> new StairBlock(MARS_STONE.get().defaultBlockState(), BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_STONE_SLAB = registerBlock("mars_stone_slab",
            () -> new SlabBlock(BlockProperties.marsStone()));


    public static final Supplier<Block> MARS_COBBLESTONE_STAIRS = registerBlock("mars_cobblestone_stairs",
            () -> new StairBlock(MARS_COBBLESTONE.get().defaultBlockState(), BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_COBBLESTONE_SLAB = registerBlock("mars_cobblestone_slab",
            () -> new SlabBlock(BlockProperties.marsStone()));


    // ========== VENUS STONE VARIANTS ==========

    public static final Supplier<Block> VENUS_STONE_STAIRS = registerBlock("venus_stone_stairs",
            () -> new StairBlock(VENUS_STONE.get().defaultBlockState(), BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_STONE_SLAB = registerBlock("venus_stone_slab",
            () -> new SlabBlock(BlockProperties.venusStone()));


    public static final Supplier<Block> VENUS_COBBLESTONE_STAIRS = registerBlock("venus_cobblestone_stairs",
            () -> new StairBlock(VENUS_COBBLESTONE.get().defaultBlockState(), BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_COBBLESTONE_SLAB = registerBlock("venus_cobblestone_slab",
            () -> new SlabBlock(BlockProperties.venusStone()));


    // ========== MERCURY STONE VARIANTS ==========

    public static final Supplier<Block> MERCURY_STONE_STAIRS = registerBlock("mercury_stone_stairs",
            () -> new StairBlock(MERCURY_STONE.get().defaultBlockState(), BlockProperties.mercuryStone()));

    public static final Supplier<Block> MERCURY_STONE_SLAB = registerBlock("mercury_stone_slab",
            () -> new SlabBlock(BlockProperties.mercuryStone()));


    public static final Supplier<Block> MERCURY_COBBLESTONE_STAIRS = registerBlock("mercury_cobblestone_stairs",
            () -> new StairBlock(MERCURY_COBBLESTONE.get().defaultBlockState(), BlockProperties.mercuryStone()));

    public static final Supplier<Block> MERCURY_COBBLESTONE_SLAB = registerBlock("mercury_cobblestone_slab",
            () -> new SlabBlock(BlockProperties.mercuryStone()));


    // ========== GLACIO STONE VARIANTS ==========

    public static final Supplier<Block> GLACIO_STONE_STAIRS = registerBlock("glacio_stone_stairs",
            () -> new StairBlock(GLACIO_STONE.get().defaultBlockState(), BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_STONE_SLAB = registerBlock("glacio_stone_slab",
            () -> new SlabBlock(BlockProperties.glacioStone()));


    public static final Supplier<Block> GLACIO_COBBLESTONE_STAIRS = registerBlock("glacio_cobblestone_stairs",
            () -> new StairBlock(GLACIO_COBBLESTONE.get().defaultBlockState(), BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_COBBLESTONE_SLAB = registerBlock("glacio_cobblestone_slab",
            () -> new SlabBlock(BlockProperties.glacioStone()));


    // ========== SKY STONE VARIANTS ==========

    public static final Supplier<Block> SKY_STONE_STAIRS = registerBlock("sky_stone_stairs",
            () -> new StairBlock(SKY_STONE.get().defaultBlockState(), BlockProperties.SKY_STONE));

    public static final Supplier<Block> SKY_STONE_SLAB = registerBlock("sky_stone_slab",
            () -> new SlabBlock(BlockProperties.SKY_STONE));


    // ========== INDUSTRIAL BLOCKS ==========

    // Factory Blocks
    public static final Supplier<Block> IRON_FACTORY_BLOCK = registerBlock("iron_factory_block",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> STEEL_FACTORY_BLOCK = registerBlock("steel_factory_block",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    // Plating Blocks
    public static final Supplier<Block> IRON_PLATING = registerBlock("iron_plating",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> STEEL_PLATING = registerBlock("steel_plating",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> DESH_PLATING = registerBlock("desh_plating",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> OSTRUM_PLATING = registerBlock("ostrum_plating",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> CALORITE_PLATING = registerBlock("calorite_plating",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    // Sheet Blocks
    public static final Supplier<Block> IRON_SHEETBLOCK = registerBlock("iron_sheetblock",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> STEEL_SHEETBLOCK = registerBlock("steel_sheetblock",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> ETRIUM_SHEETBLOCK = registerBlock("etrium_sheetblock",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> DESH_SHEETBLOCK = registerBlock("desh_sheetblock",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> OSTRUM_SHEETBLOCK = registerBlock("ostrum_sheetblock",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> CALORITE_SHEETBLOCK = registerBlock("calorite_sheetblock",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    // Panel Blocks
    public static final Supplier<Block> IRON_PANEL = registerBlock("iron_panel",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> STEEL_PANEL = registerBlock("steel_panel",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> ETRIUM_PANEL = registerBlock("etrium_panel",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> DESH_PANEL = registerBlock("desh_panel",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> OSTRUM_PANEL = registerBlock("ostrum_panel",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> CALORITE_PANEL = registerBlock("calorite_panel",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    // Encased Blocks
    public static final Supplier<Block> ENCASED_IRON_BLOCK = registerBlock("encased_iron_block",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> ENCASED_STEEL_BLOCK = registerBlock("encased_steel_block",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    // Pillar Blocks (using RotatedPillarBlock for proper orientation)
    public static final Supplier<Block> IRON_PILLAR = registerBlock("iron_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> STEEL_PILLAR = registerBlock("steel_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> DESH_PILLAR = registerBlock("desh_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> OSTRUM_PILLAR = registerBlock("ostrum_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> CALORITE_PILLAR = registerBlock("calorite_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> MARKED_IRON_PILLAR = registerBlock("marked_iron_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK));

    // Glowing Pillar Blocks (emit light level 15)
    public static final Supplier<Block> GLOWING_IRON_PILLAR = registerBlock("glowing_iron_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK.lightLevel(state -> 15)));

    public static final Supplier<Block> GLOWING_STEEL_PILLAR = registerBlock("glowing_steel_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK.lightLevel(state -> 15)));

    public static final Supplier<Block> GLOWING_DESH_PILLAR = registerBlock("glowing_desh_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK.lightLevel(state -> 15)));

    public static final Supplier<Block> GLOWING_OSTRUM_PILLAR = registerBlock("glowing_ostrum_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK.lightLevel(state -> 15)));

    public static final Supplier<Block> GLOWING_CALORITE_PILLAR = registerBlock("glowing_calorite_pillar",
            () -> new RotatedPillarBlock(BlockProperties.INDUSTRIAL_BLOCK.lightLevel(state -> 15)));

    // ========== ORE BLOCKS ==========

    // Standard Ores
    public static final Supplier<Block> DESH_ORE = registerBlock("desh_ore",
            () -> new Block(BlockProperties.ORE_BLOCK));

    public static final Supplier<Block> OSTRUM_ORE = registerBlock("ostrum_ore",
            () -> new Block(BlockProperties.ORE_BLOCK));

    public static final Supplier<Block> CALORITE_ORE = registerBlock("calorite_ore",
            () -> new Block(BlockProperties.ORE_BLOCK));

    public static final Supplier<Block> ETRIUM_ORE = registerBlock("etrium_ore",
            () -> new Block(BlockProperties.ORE_BLOCK));

    // Deepslate Ores
    public static final Supplier<Block> DEEPSLATE_DESH_ORE = registerBlock("deepslate_desh_ore",
            () -> new Block(BlockProperties.DEEPSLATE_ORE_BLOCK));

    public static final Supplier<Block> DEEPSLATE_OSTRUM_ORE = registerBlock("deepslate_ostrum_ore",
            () -> new Block(BlockProperties.DEEPSLATE_ORE_BLOCK));

    public static final Supplier<Block> DEEPSLATE_CALORITE_ORE = registerBlock("deepslate_calorite_ore",
            () -> new Block(BlockProperties.DEEPSLATE_ORE_BLOCK));

    // ========== PLANET-SPECIFIC ORES ==========

    // Moon Ores
    public static final Supplier<Block> MOON_DESH_ORE = registerBlock("moon_desh_ore",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_IRON_ORE = registerBlock("moon_iron_ore",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_ETRIUM_ORE = registerBlock("moon_etrium_ore",
            () -> new Block(BlockProperties.moonStone()));

    public static final Supplier<Block> MOON_CHEESE_ORE = registerBlock("moon_cheese_ore",
            () -> new Block(BlockProperties.moonStone()));

    // Mars Ores
    public static final Supplier<Block> MARS_IRON_ORE = registerBlock("mars_iron_ore",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_DIAMOND_ORE = registerBlock("mars_diamond_ore",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_OSTRUM_ORE = registerBlock("mars_ostrum_ore",
            () -> new Block(BlockProperties.marsStone()));

    public static final Supplier<Block> MARS_ETRIUM_ORE = registerBlock("mars_etrium_ore",
            () -> new Block(BlockProperties.marsStone()));

    // Venus Ores
    public static final Supplier<Block> VENUS_COAL_ORE = registerBlock("venus_coal_ore",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_GOLD_ORE = registerBlock("venus_gold_ore",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_DIAMOND_ORE = registerBlock("venus_diamond_ore",
            () -> new Block(BlockProperties.venusStone()));

    public static final Supplier<Block> VENUS_CALORITE_ORE = registerBlock("venus_calorite_ore",
            () -> new Block(BlockProperties.venusStone()));

    // Mercury Ores
    public static final Supplier<Block> MERCURY_IRON_ORE = registerBlock("mercury_iron_ore",
            () -> new Block(BlockProperties.mercuryStone()));

    // Glacio Ores
    public static final Supplier<Block> GLACIO_COAL_ORE = registerBlock("glacio_coal_ore",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_COPPER_ORE = registerBlock("glacio_copper_ore",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_IRON_ORE = registerBlock("glacio_iron_ore",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_LAPIS_ORE = registerBlock("glacio_lapis_ore",
            () -> new Block(BlockProperties.glacioStone()));

    public static final Supplier<Block> GLACIO_ETRIUM_ORE = registerBlock("glacio_etrium_ore",
            () -> new Block(BlockProperties.glacioStone()));

    // ========== DOORS & ACCESS BLOCKS ==========

    public static final Supplier<Block> STEEL_DOOR = registerBlock("steel_door",
            () -> new net.minecraft.world.level.block.DoorBlock(
                    net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
                    BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> STEEL_TRAPDOOR = registerBlock("steel_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(
                    net.minecraft.world.level.block.state.properties.BlockSetType.IRON,
                    BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> AIRLOCK = registerBlock("airlock",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> REINFORCED_DOOR = registerBlock("reinforced_door",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    // Sliding Doors
    public static final Supplier<Block> IRON_SLIDING_DOOR = registerBlock("iron_sliding_door",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> STEEL_SLIDING_DOOR = registerBlock("steel_sliding_door",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> DESH_SLIDING_DOOR = registerBlock("desh_sliding_door",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> OSTRUM_SLIDING_DOOR = registerBlock("ostrum_sliding_door",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    public static final Supplier<Block> CALORITE_SLIDING_DOOR = registerBlock("calorite_sliding_door",
            () -> new SlidingDoorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    // ========== ALIEN WOOD BLOCKS ==========

    // Glacian Wood Set
    public static final Supplier<Block> GLACIAN_LOG = registerBlock("glacian_log",
            () -> new RotatedPillarBlock(BlockProperties.ALIEN_WOOD));

    public static final Supplier<Block> STRIPPED_GLACIAN_LOG = registerBlock("stripped_glacian_log",
            () -> new RotatedPillarBlock(BlockProperties.ALIEN_WOOD));

    public static final Supplier<Block> GLACIAN_PLANKS = registerBlock("glacian_planks",
            () -> new Block(BlockProperties.ALIEN_WOOD));

    public static final Supplier<Block> GLACIAN_LEAVES = registerBlock("glacian_leaves",
            () -> new LeavesBlock(BlockProperties.ALIEN_LEAVES));

    // Alien Mushroom Set
    public static final Supplier<Block> AERONOS_MUSHROOM = registerBlock("aeronos_mushroom",
            () -> new Block(BlockProperties.ALIEN_WOOD));

    public static final Supplier<Block> AERONOS_STEM = registerBlock("aeronos_stem",
            () -> new RotatedPillarBlock(BlockProperties.ALIEN_WOOD));

    public static final Supplier<Block> STROPHAR_MUSHROOM = registerBlock("strophar_mushroom",
            () -> new Block(BlockProperties.ALIEN_WOOD));

    public static final Supplier<Block> STROPHAR_STEM = registerBlock("strophar_stem",
            () -> new RotatedPillarBlock(BlockProperties.ALIEN_WOOD));


    // ========== MEKANISM-COMPATIBLE WORKSTATIONS ==========

    public static final Supplier<Block> OXYGEN_DISTRIBUTOR = registerBlock("oxygen_distributor",
            () -> new OxygenDistributorBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> GRAVITY_NORMALIZER = registerBlock("gravity_normalizer",
            () -> new GravityNormalizerBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> WIRELESS_POWER_RELAY = registerBlock("wireless_power_relay",
            () -> new WirelessPowerRelayBlock(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> NASA_WORKBENCH = registerBlock("nasa_workbench",
            () -> new com.hecookin.adastramekanized.common.blocks.NasaWorkbenchBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final Supplier<Block> OXYGEN_NETWORK_MONITOR = registerBlock("oxygen_network_monitor",
            () -> new OxygenNetworkMonitorBlock(BlockProperties.INDUSTRIAL_BLOCK.noOcclusion()));

    // Redstone Toggle Relay - pairs with controller and toggles distributors via redstone
    public static final Supplier<Block> REDSTONE_TOGGLE_RELAY = registerBlock("redstone_toggle_relay",
            () -> new RedstoneToggleRelay(BlockProperties.INDUSTRIAL_BLOCK));

    public static final Supplier<Block> ROCKET_ASSEMBLY_STATION = registerBlock("rocket_assembly_station",
            () -> new Block(BlockProperties.INDUSTRIAL_BLOCK));

    // ========== ROCKET INFRASTRUCTURE ==========

    public static final Supplier<Block> LAUNCH_PAD = registerBlock("launch_pad",
            () -> new com.hecookin.adastramekanized.common.blocks.LaunchPadBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    /**
     * Helper method to register a block with its corresponding BlockItem
     */
    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> registeredBlock = BLOCKS.register(name, block);

        // Register the corresponding BlockItem
        ModItems.ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));

        return registeredBlock;
    }


    /**
     * Register all mod blocks
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered {} mod blocks", BLOCKS.getEntries().size());
    }
}