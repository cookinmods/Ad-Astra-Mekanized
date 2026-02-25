package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runner to generate planet files using PlanetMaker system.
 *
 * This class serves as the single source of truth for all planet definitions.
 * Planet builders registered here are used for both:
 * - JSON generation (via PlanetMaker)
 * - Dimension effects fallback (via DimensionEffectsHandler)
 */
public class PlanetGenerationRunner {

    /**
     * Static registry of all planet builders.
     * LinkedHashMap preserves insertion order for predictable generation.
     */
    private static final Map<String, PlanetMaker.PlanetBuilder> PLANET_REGISTRY = new LinkedHashMap<>();

    // Static initialization - populate planet registry when class is loaded
    static {
        configurePlanets();
        AdAstraMekanized.LOGGER.info("Planet registry initialized with {} planets", PLANET_REGISTRY.size());
    }

    /**
     * Register a planet builder for both JSON generation and dimension effects.
     *
     * @param planetId The planet identifier (e.g., "moon", "mars")
     * @return The planet builder for method chaining
     */
    public static PlanetMaker.PlanetBuilder registerPlanet(String planetId) {
        PlanetMaker.PlanetBuilder builder = PlanetMaker.planet(planetId);
        PLANET_REGISTRY.put(planetId, builder);
        return builder;
    }

    /**
     * Get all registered planet builders.
     * Used by DimensionEffectsHandler for fallback generation.
     *
     * @return Unmodifiable map of planet ID to builder
     */
    public static Map<String, PlanetMaker.PlanetBuilder> getAllPlanetBuilders() {
        return Map.copyOf(PLANET_REGISTRY);
    }

    /**
     * Clear the planet registry.
     * Used for testing or regeneration.
     */
    public static void clearRegistry() {
        PLANET_REGISTRY.clear();
    }

    /**
     * Regenerate all planet files - call this to update planet configurations
     */
    public static void regeneratePlanets() {
        try {
            AdAstraMekanized.LOGGER.info("Regenerating planet files with PlanetMaker...");

            // Configure planets using builder pattern
            configurePlanets();

            // Generate all configured planets
            PlanetMaker.generateAllPlanets();

            AdAstraMekanized.LOGGER.info("Planet regeneration completed successfully!");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to regenerate planets: ", e);
        }
    }

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");

        try {
            AdAstraMekanized.LOGGER.info("Starting planet generation with PlanetMaker...");

            // Configure planets using builder pattern
            configurePlanets();

            // Generate all configured planets
            PlanetMaker.generateAllPlanets();

            AdAstraMekanized.LOGGER.info("Planet generation completed successfully!");
        } catch (Exception e) {
            System.err.println("Failed to generate planets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * AVAILABLE MOBS FOR PLANET SPAWNING
     *
     * === VANILLA MINECRAFT HOSTILE MOBS ===
     * minecraft:zombie - Standard zombie
     * minecraft:zombie_villager - Zombified villager
     * minecraft:husk - Desert zombie variant
     * minecraft:drowned - Underwater zombie
     * minecraft:skeleton - Standard skeleton
     * minecraft:stray - Ice skeleton variant
     * minecraft:wither_skeleton - Nether skeleton
     * minecraft:spider - Standard spider
     * minecraft:cave_spider - Smaller poisonous spider
     * minecraft:creeper - Explosive mob
     * minecraft:enderman - Teleporting mob
     * minecraft:endermite - Small end parasite
     * minecraft:silverfish - Stone parasite
     * minecraft:phantom - Flying hostile mob
     * minecraft:blaze - Nether flying mob
     * minecraft:ghast - Large flying nether mob
     * minecraft:magma_cube - Nether slime variant
     * minecraft:slime - Bouncing cube mob
     * minecraft:witch - Potion-throwing mob
     * minecraft:vindicator - Axe-wielding illager
     * minecraft:evoker - Magic-using illager
     * minecraft:vex - Small flying illager summon
     * minecraft:pillager - Crossbow illager
     * minecraft:ravager - Large illager beast
     * minecraft:guardian - Ocean monument mob
     * minecraft:elder_guardian - Boss guardian
     * minecraft:shulker - End city mob
     * minecraft:warden - Deep dark boss
     * minecraft:zoglin - Zombified hoglin
     * minecraft:piglin - Nether pig mob
     * minecraft:piglin_brute - Stronger piglin
     * minecraft:hoglin - Nether beast
     * minecraft:zombified_piglin - Zombie pigman
     * minecraft:wither - Boss mob
     * minecraft:ender_dragon - Boss mob
     *
     * === VANILLA MINECRAFT PASSIVE MOBS ===
     * minecraft:cow - Basic farm animal
     * minecraft:pig - Basic farm animal
     * minecraft:sheep - Wool provider
     * minecraft:chicken - Egg layer
     * minecraft:rabbit - Small hopping animal
     * minecraft:horse - Rideable animal
     * minecraft:donkey - Pack animal
     * minecraft:mule - Pack animal
     * minecraft:llama - Pack animal
     * minecraft:trader_llama - Wandering trader's llama
     * minecraft:cat - Tameable pet
     * minecraft:wolf - Tameable pet
     * minecraft:parrot - Tameable bird
     * minecraft:fox - Forest animal
     * minecraft:bee - Pollinator
     * minecraft:panda - Bamboo eater
     * minecraft:polar_bear - Arctic animal
     * minecraft:turtle - Beach animal
     * minecraft:dolphin - Ocean animal
     * minecraft:cod - Fish
     * minecraft:salmon - Fish
     * minecraft:pufferfish - Poisonous fish
     * minecraft:tropical_fish - Decorative fish
     * minecraft:squid - Ink producer
     * minecraft:glow_squid - Glowing squid
     * minecraft:bat - Cave ambient
     * minecraft:ocelot - Jungle cat
     * minecraft:goat - Mountain animal
     * minecraft:axolotl - Cave aquatic
     * minecraft:frog - Swamp animal
     * minecraft:tadpole - Baby frog
     * minecraft:allay - Helper mob
     * minecraft:strider - Nether walker
     * minecraft:mooshroom - Mushroom cow
     * minecraft:snow_golem - Buildable ally
     * minecraft:iron_golem - Buildable defender
     *
     * === MODDED MOBS ===
     * When Dungeons Arise: Does NOT add custom mobs (structure generation only)
     * DungeonCrawl: Uses vanilla spawners in generated dungeons
     *
     * === WHEN DUNGEONS ARISE SPAWNER MECHANICS ===
     * WDA enhances vanilla spawners with these features:
     * - Randomized spawner types throughout structures (difficulty scaling)
     * - Trial Spawners (1.21+): Wave-based spawning with player scaling
     *   - Spawns increase based on nearby players
     *   - Drops rewards when all mobs defeated
     *   - 30-minute cooldown after completion
     * - No custom mobs - uses vanilla mob types exclusively
     * - Spawners placed strategically to guard loot and passages
     * - Compatible with modded entities when other mods present
     *
     * === WDA STRUCTURES & SPAWNER CONFIGURATIONS (43 total) ===
     * Based on source code analysis:
     *
     * SPECIFIC SPAWNER POOLS FOUND:
     * - Keep Kayra: 9 different spawner variants (keep_kayra_spawner_0 through _8)
     * - Infested Temple: Cave spiders, skeletons, wither skeletons
     * - Bathhouse: Dedicated spawner pool
     * - Bandit Village & Towers: Custom spawner configurations
     * - Illager Fort: Fort-specific spawners
     * - Scorched Mines: Underground hostile spawners
     * - Mining System: Cave-themed spawners
     * - Foundry: Industrial-themed spawners
     * - Shiraz Palace: Desert-themed spawners
     * - Small Blimp: Aerial structure spawners
     *
     * NOTABLE STRUCTURES (without specific spawner pools):
     * - Abandoned Temple, Aviary, Coliseum, Fishing Hut
     * - Giant Mushroom, Greenwood Pub/Towers
     * - Heavenly Challenger/Conqueror/Rider
     * - Illager Campsite/Castle/Corsair/Galley
     * - Jungle Tree House, Library, Lighthouse
     * - Mechanical Nest, Mushroom House/Mines/Village
     * - Monastery, Plague Asylum, Prairie House
     * - Scarlet Monastery, Shiraz Palace, Small Prairie House
     * - Thornborn Towers, Typhon, Undead Pirate Ship
     *
     * LOOT TABLES:
     * - Gladiator loot (coliseum): Diamonds, emeralds, iron, gold, special potions
     * - Each structure has dedicated chest loot tables
     * - No custom entity drops - uses vanilla loot mechanics
     *
     * === HOW WDA CREATES CHALLENGING MOBS ===
     * WDA doesn't modify mob equipment directly through code. Instead:
     *
     * 1. **Pre-equipped Mob Structures (NBT files)**:
     *    - skeleton_armored_0.nbt, skeleton_armored_1.nbt (Abandoned Temple)
     *    - skeleton_juggernaut_0.nbt, skeleton_juggernaut_1.nbt (Undead Pirate Ship)
     *    - wither_skeleton variants (Foundry)
     *    - These NBT structures contain mobs with pre-set equipment saved in the structure
     *
     * 2. **Structure-Specific Mob Variants**:
     *    - Different structures have themed mob setups
     *    - "Juggernaut" variants likely have full armor sets
     *    - "Armored" variants have partial or full equipment
     *    - "Ranged" variants configured with bows/crossbows
     *
     * 3. **Chest Loot with Enchanted Gear**:
     *    - Treasure chests contain enchanted equipment (level 15-25)
     *    - Players can find and use this gear
     *    - Creates progression through structure exploration
     *
     * 4. **No Runtime Modification**:
     *    - No Java code for dynamically equipping mobs
     *    - All mob equipment is pre-configured in structure NBT files
     *    - This approach ensures consistent difficulty per structure type
     *
     * === SUGGESTED PLANET SPAWNING THEMES ===
     * Moon/Barren: silverfish, endermite (parasites)
     * Mars/Desert: husk, spider, stray (dry environment)
     * Ice Planet: stray, polar_bear, snow_golem
     * Toxic/Radioactive: spider, cave_spider, slime, witch
     * Ocean Planet: drowned, guardian, squid, dolphin
     * Jungle Planet: ocelot, parrot, spider, witch
     * Nether-like: blaze, magma_cube, ghast, strider
     * End-like: enderman, endermite, shulker
     * Habitable: Standard overworld mobs
     *
     * Note: Always check if modded mobs exist before using them.
     * Use vanilla alternatives when mods aren't present.
     */

    /**
     * Configure planets using PlanetMaker builder pattern
     *
     * TECTONIC GENERATION MODE:
     * For advanced terrain with continental systems, erosion, ridges, and Tectonic features,
     * use .withTectonicGeneration() to enable the full Tectonic worldgen system.
     *
     * Example with Tectonic preset:
     *   registerPlanet("moon")
     *       .withTectonicGeneration()
     *       .withTectonicConfig(NoiseRouterBuilder.TectonicConfig.moon())
     *       .surfaceBlock("adastramekanized:moon_sand")
     *       .generate();
     *
     * Example with custom Tectonic config:
     *   registerPlanet("mars")
     *       .withTectonicGeneration()
     *       .continentalScale(0.004f)
     *       .erosionScale(0.003f)
     *       .ridgeScale(0.008f)
     *       .withMountainSharpness(1.5f)
     *       .withDesertDunes(15.0f, 200.0f)
     *       .withIslands(true)
     *       .surfaceBlock("adastramekanized:mars_sand")
     *       .generate();
     *
     * Tectonic features available:
     * - .withIslands(true) - Enable island generation
     * - .withMountainSharpness(float) - Control peak sharpness
     * - .withDesertDunes(height, wavelength) - Add desert dunes
     * - .withJunglePillars(height) - Add vertical spires
     * - .undergroundRivers(true) - Underground water channels
     * - .lavaTunnels(true) - Underground lava channels
     */
    private static void configurePlanets() {
        // Moon planet - FLAT PLAINS-LIKE TERRAIN with Moon blocks
        // Uses vanillaQualityFlat preset for gentle rolling hills, no mountains
        // Caves remain enabled for underground exploration
        PlanetMaker.PlanetBuilder moon = registerPlanet("moon")
            .vanillaQualityFlat()          // Flat terrain with gentle rolling hills
            .coordinateShift(5000, 5000)   // Shift coordinates for unique Moon terrain
            // Physical properties
            .gravity(0.166f)  // Moon has 1/6 Earth gravity
            .temperature(-173) // Lunar surface average temperature
            // Surface blocks
            .surfaceBlock("adastramekanized:moon_sand")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("adastramekanized:moon_stone")
            .defaultBlock("adastramekanized:moon_stone")      // Replaces vanilla stone (above Y=0)
            .deepslateBlock("adastramekanized:moon_stone")    // Replaces vanilla deepslate (below Y=0)
            .bedrockBlock("minecraft:bedrock")
            // World configuration
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Airless moon - no water
            .oreVeinsEnabled(true)
            // 3 Moon biomes - plains-like with low erosion/weirdness for flat terrain
            // Removed crater biomes (moon_crater_rim, moon_crater_floor)
            .clearBiomes()
            .addBiome("adastramekanized:moon_highlands", -0.5f, 0.0f, -0.3f, 0.3f, -0.2f, 0.2f, "Lunar Highlands")
            .addBiome("adastramekanized:moon_maria", -0.5f, 0.0f, -0.3f, 0.3f, -0.2f, 0.2f, "Lunar Maria")
            .addBiome("adastramekanized:moon_polar", -1.0f, -0.5f, -0.3f, 0.3f, -0.2f, 0.2f, "Polar Region")
            // Visual properties
            .skyColor(0x0A0A0A)
            .fogColor(0x0A0A0A)
            .hasAtmosphere(false)
            .ambientLight(0.04f)
            // Sun and spawn settings
            .hasSkylight(false)
            .monsterSpawnLightLevel(15)
            // Celestial configuration
            .addSun()
            .addVisiblePlanet(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/celestial/earth.png"),
                0.25f, 0x4169E1, 0.5f, 0.8f, true
            )
            .starsVisibleDuringDay(true)
            .starCount(50000)
            .starBrightness(2.5f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .snowEnabled(false)
            // Moon ores - industrial starter metal
            .configureOre("tin", 8)        // primary - Mekanism ore, industrial starter metal (fallback for silver)
            .configureOre("iron", 10)       // tertiary - lunar regolith deposits
            .configureOre("desh", 5)        // PROGRESSION ORE - needed for Tier 2 rocket
            .configureOre("cheese", 4)      // Moon cheese ore - classic Ad Astra feature
            .configureOre("moon_etrium", 2) // ENDGAME ORE - unchanged
            .configureConditionalOre("silver", 8, "immersiveengineering", "tin"); // IE: silver replaces tin

        moon.clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "30", "2", "4"},
                    {"monster", "minecraft:endermite", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "30", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "15", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
            .generate();

        // EARTH'S ORBIT - Space station dimension with Earth view below
        // This is a VOID DIMENSION - no terrain generation, just a spawn platform
        // Tier 1 destination - accessible with basic rocket
        registerPlanet("earth_orbit")
            .gravity(0.166f)  // Moon-like micro-gravity
            .temperature(-270) // Deep space temperature
            // VOID DIMENSION - flat with no terrain features
            .continentalScale(0.0f)
            .erosionScale(0.0f)
            .ridgeScale(0.0f)
            .heightVariation(0.0f, 0.0f, 0.0f, 0.0f)
            // All blocks are air - void space
            .surfaceBlock("minecraft:air")
            .subsurfaceBlock("minecraft:air")
            .deepBlock("minecraft:air")
            .defaultBlock("minecraft:air")
            .bedrockBlock("minecraft:bedrock")
            // World structure
            .worldDimensions(0, 256)
            .seaLevel(100)
            .disableMobGeneration(true)  // No spawning in orbit
            .aquifersEnabled(false)
            .oreVeinsEnabled(false)
            .addCavePreset("none")  // No caves in void space
            // No liquids in space
            .oceanConfig("minecraft:air", 0, 0.0f)
            .lakeConfig("minecraft:air", 0.0f)
            .lavaLakes(0, 0.0f)
            .undergroundLiquids("minecraft:air", false)
            .waterRule(false)
            // Space visuals - black void with stars
            .skyColor(0x000000)
            .fogColor(0x000000)
            .hasAtmosphere(false)
            .ambientLight(0.0f)
            .hasSkylight(false)
            // Celestial view - Earth below
            .addSun(
                net.minecraft.resources.ResourceLocation.parse("minecraft:textures/environment/sun.png"),
                1.0f,
                0xFFFFFF,
                false  // Sun not visible (in Earth's shadow)
            )
            .addVisiblePlanet(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/celestial/earth.png"),
                1.0f,      // Massive Earth below
                0x4169E1,  // Royal blue
                0.0f,      // Centered
                -0.5f,     // Low (below horizon)
                false      // Static (not moving - we're orbiting it)
            )
            .starsVisibleDuringDay(true)  // Stars always visible in space
            .starCount(100000)            // Ultra dense starfield
            .starBrightness(3.0f)         // Maximum brightness
            .cloudsEnabled(false)
            .rainEnabled(false)
            .snowEnabled(false)
            // Single void biome
            .clearBiomes()
            .addBiome("minecraft:the_void", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, "Earth Orbit")
            .generate();

        // Mars planet - VANILLA-QUALITY TERRAIN with Mars blocks
        // Uses FULL vanilla density function set (offset, factor, jaggedness splines) with coordinate shift
        // This produces terrain with vanilla-quality spline-based terrain shaping, unique to Mars
        registerPlanet("mars")
            .vanillaQualityMountainous()   // Use vanilla-quality mountainous terrain for dramatic Martian landscape
            .coordinateShift(15000, 15000) // Shift coordinates for unique Mars terrain
            // Physical properties
            .gravity(0.38f)  // Mars has 3/8 Earth gravity
            .temperature(-65) // Mars average surface temperature
            // Surface blocks - this is all we need to customize!
            .surfaceBlock("adastramekanized:mars_sand")
            .subsurfaceBlock("adastramekanized:mars_stone")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:basalt")
            .defaultBlock("adastramekanized:mars_stone")      // Replaces vanilla stone (above Y=0)
            .deepslateBlock("minecraft:basalt")    // Replaces vanilla deepslate (below Y=0)
            .bedrockBlock("minecraft:bedrock")
            // World configuration
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Dry desert planet - no water
            .oreVeinsEnabled(true)
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:husk", "30", "1", "3"},
                    {"monster", "minecraft:enderman", "10", "1", "1"},
                    {"monster", "minecraft:skeleton", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:decrepit_skeleton", "12", "1", "2"},
                    {"monster", "born_in_chaos_v1:skeleton_demoman", "7", "1", "1"},
                    {"monster", "born_in_chaos_v1:decaying_zombie", "12", "1", "2"},
                    {"monster", "born_in_chaos_v1:barrel_zombie", "7", "1", "1"},
                    {"monster", "born_in_chaos_v1:bonescaller", "6", "1", "1"},
                    {"monster", "born_in_chaos_v1:bone_imp", "7", "1", "1"}
                }
            )
            // Add custom biomes for Mars (vanilla climate parameters work with vanilla noise!)
            .clearBiomes()
            .addBiome("adastramekanized:mars_highlands", -0.2f, -0.7f, 0.6f, -0.1f, 0.5f, 0.0f, "Martian Highlands")
            .addBiome("adastramekanized:mars_canyons", 0.1f, -0.6f, 0.2f, 0.4f, -0.3f, -0.1f, "Martian Canyons")
            .addBiome("adastramekanized:mars_polar", -0.9f, -0.8f, 0.8f, 0.0f, 0.2f, 0.3f, "Martian Polar Ice Caps")
            // Atmospheric rendering
            .skyColor(0xD2691E)
            .fogColor(0xCD853F)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.2f)
            // Celestial configuration
            .addSun()
            .addMoon(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/celestial/phobos.png"),
                0.3f, 0xAA8866, 0.4f, 1.15f, true
            )
            .addMoon(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/celestial/deimos.png"),
                0.2f, 0x998877, -0.3f, 1.30f, true
            )
            .starsVisibleDuringDay(false)
            .starCount(8000)
            .starBrightness(1.2f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .snowEnabled(false)
            // Mars ores - Plan B: energy-rich desert world with Mekanism osmium
            .configureOre("redstone", 25)   // primary - thermal energy from volcanic activity
            .configureOre("osmium", 15)     // tertiary - Mekanism ore available early
            .configureOre("ostrum", 5)      // PROGRESSION ORE - needed for Tier 3 rocket
            .configureOre("desh", 3)        // rare - secondary desh source
            .configureOre("mars_etrium", 2) // ENDGAME ORE - unchanged
            .generate();

        // ==================== SOLAR SYSTEM INNER PLANETS ====================

        // VENUS - Toxic atmosphere with sulfuric acid rain
        // Extremely hostile: acid rain damages players, thick yellow fog, very hot
        registerPlanet("venus")
            .vanillaQualityFlat()
            .coordinateShift(30000, 30000)
            .gravity(0.9f)
            .temperature(464) // Venus surface - hottest planet
            .surfaceBlock("minecraft:yellow_terracotta")
            .subsurfaceBlock("minecraft:orange_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:brown_terracotta")
            .defaultBlock("minecraft:orange_terracotta")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Venus ores - Plan B: toxic hellscape with lead, coal, and redstone
            .configureOre("coal", 25)      // primary - sulfurous organic deposits
            .configureOre("lead", 20)      // secondary - IE ore, heavy metal deposits
            .configureOre("redstone", 15)  // tertiary - volcanic thermal energy
            .configureOre("calorite", 4)   // PROGRESSION ORE - needed for Tier 4 rocket
            .configureOre("etrium", 2)     // ENDGAME ORE - unchanged
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:husk", "25", "1", "2"},
                    {"monster", "minecraft:witch", "15", "1", "1"},
                    {"monster", "minecraft:slime", "20", "2", "3"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:decrepit_skeleton", "25", "1", "2"},
                    {"monster", "born_in_chaos_v1:decaying_zombie", "20", "1", "2"},
                    {"monster", "born_in_chaos_v1:door_knight", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:skeleton_thrasher", "8", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .addReptilianCreature("gecko", 15, 1, 1)
            .clearBiomes()
            .addBiome("minecraft:desert", 0.9f, -0.9f, 0.0f, 0.0f, 0.0f, 0.0f, "Venusian Wasteland")
            .skyColor(0xFFCC66)
            .fogColor(0xFFAA33)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.3f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .acidRainDamage(true)
            .acidRainDamageAmount(2.0f)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // MERCURY - Airless scorched world closest to sun
        // Extreme temperature, fire damage during day, airless, cratered
        registerPlanet("mercury")
            .vanillaQualityCratered()
            .coordinateShift(45000, 30000)
            .gravity(0.38f)
            .temperature(167) // Mercury average surface temperature
            .surfaceBlock("minecraft:light_gray_terracotta")
            .subsurfaceBlock("minecraft:gray_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:gray_terracotta")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Mercury ores - radioactive scorched world + redstone
            .configureOre("uranium", 25)   // primary - Mekanism ore, radioactive core (fallback for nickel)
            .configureOre("iron", 20)      // secondary - iron-rich regolith
            .configureOre("redstone", 15)  // tertiary - thermal energy from sun proximity
            .configureOre("ostrum", 4)     // PROGRESSION ORE - needed for Tier 3 rocket
            .configureOre("etrium", 2)     // ENDGAME ORE - unchanged
            .configureConditionalOre("nickel", 25, "immersiveengineering", "uranium") // IE: nickel replaces uranium
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "30", "2", "4"},
                    {"monster", "minecraft:endermite", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "30", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "15", "1", "2"},
                    {"monster", "kobolds:kobold_engineer", "10", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:desert", -0.9f, -0.9f, 0.5f, 0.0f, 0.0f, 0.0f, "Mercurian Craters")
            .skyColor(0x000000)
            .fogColor(0x000000)
            .hasAtmosphere(false)
            .ambientLight(0.0f)
            .hasSkylight(true)
            .fireDamage(true)
            .fireDamageAmount(1.0f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(30000)
            .starBrightness(2.0f)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // ==================== JUPITER'S MOONS ====================

        // EUROPA - Ice moon with subsurface ocean
        // Frozen surface, potential for water beneath, very flat
        registerPlanet("europa")
            .vanillaQualityFlat()
            .coordinateShift(60000, 30000)
            .gravity(0.13f)
            .temperature(-160) // Europa ice surface
            .surfaceBlock("minecraft:blue_ice")
            .subsurfaceBlock("minecraft:packed_ice")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(32)
            .defaultFluid("minecraft:water")
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Europa ores - ice moon with tin and gems + iron
            .configureOre("lapis", 25)     // primary - blue gems in blue ice
            .configureOre("tin", 20)       // secondary - Mekanism ore, frozen industrial deposits (fallback for silver)
            .configureOre("iron", 15)      // tertiary - rocky core deposits
            .configureOre("etrium", 6)     // ENDGAME ORE - unchanged
            .configureConditionalOre("silver", 20, "immersiveengineering", "tin") // IE: silver replaces tin
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:drowned", "25", "1", "2"},
                    {"water_creature", "minecraft:glow_squid", "15", "1", "3"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:corpse_fish", "25", "2", "4"},
                    {"monster", "born_in_chaos_v1:glutton_fish", "15", "1", "2"},
                    {"monster", "born_in_chaos_v1:zombie_fisherman", "10", "1", "2"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:frozen_ocean", -1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, "Europan Ice Plains")
            .skyColor(0x1a1a2e)
            .fogColor(0x16213e)
            .hasAtmosphere(false)
            .ambientLight(0.05f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(40000)
            .starBrightness(2.2f)
            .addSun()
            .addVisiblePlanet(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/celestial/jupiter.png"),
                3.0f, 0xE8D4B8, 0.3f, 0.6f, true
            )
            .addCavePreset("frozen")
            // Enable Seven Seas underwater structures (frozen shipwrecks beneath ice)
            .enableSevenSeasStructures()
            .generate();

        // IO - Volcanic moon of Jupiter
        // Constant volcanic activity, fire damage, sulfur-rich
        registerPlanet("io")
            .vanillaQualityMountainous()
            .coordinateShift(75000, 30000)
            .gravity(0.18f)
            .temperature(-130) // Io average (volcanic areas much hotter)
            .surfaceBlock("minecraft:yellow_terracotta")
            .subsurfaceBlock("minecraft:orange_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:netherrack")
            .defaultBlock("minecraft:orange_terracotta")
            .defaultFluid("minecraft:lava")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(20)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Io ores - Plan B: volcanic moon with gold and nickel
            .configureOre("gold", 25)      // primary - volcanic gold deposits
            .configureOre("redstone", 20)  // secondary - thermal energy
            .configureOre("fluorite", 15)  // tertiary - Mekanism ore, chemical processing (fallback for nickel)
            .configureOre("diamond", 5)    // rare - forged in heat
            .configureConditionalOre("nickel", 15, "immersiveengineering", "fluorite") // IE: nickel replaces fluorite
            .clearAllMobSpawns()
            .addModReplacementSpawns("doom",
                new String[][] {
                    {"monster", "minecraft:magma_cube", "30", "1", "3"},
                    {"monster", "minecraft:ghast", "10", "1", "1"},
                    {"monster", "minecraft:blaze", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "doom:imp", "40", "2", "4"},
                    {"monster", "doom:lost_soul", "30", "1", "3"},
                    {"monster", "doom:cacodemon", "15", "1", "1"},
                    {"monster", "doom:pinky", "10", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:basalt_deltas", 1.0f, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, "Io Volcanic Fields")
            .skyColor(0x2a1a0a)
            .fogColor(0x4a2a0a)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.4f)
            .fireDamage(true)
            .fireDamageAmount(0.5f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(false)
            .addSun()
            .addVisiblePlanet(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/celestial/jupiter.png"),
                4.0f, 0xE8D4B8, -0.2f, 0.5f, true
            )
            .addCavePreset("lava_tubes")
            .generate();

        // GANYMEDE - Largest moon in solar system
        // Mixed terrain, some ice, some rock, moderate conditions
        registerPlanet("ganymede")
            .vanillaQualityStandard()
            .coordinateShift(90000, 30000)
            .gravity(0.15f)
            .temperature(-160) // Ganymede surface temperature
            .surfaceBlock("minecraft:gray_terracotta")
            .subsurfaceBlock("minecraft:light_gray_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Airless moon - no water
            .oreVeinsEnabled(true)
            // Ganymede ores - Plan B: Mekanism osmium hub
            .configureOre("osmium", 25)    // primary - Mekanism ore, major source
            .configureOre("iron", 20)      // secondary - rocky moon deposits
            .configureOre("copper", 15)    // tertiary - basic metal
            .configureOre("diamond", 5)    // rare - deep deposits
            .clearAllMobSpawns()
            .addModReplacementSpawns("undead_revamp2",
                new String[][] {
                    {"monster", "minecraft:skeleton", "25", "1", "2"},
                    {"monster", "minecraft:silverfish", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "undead_revamp2:thehunter", "20", "1", "2"},
                    {"monster", "undead_revamp2:theheavy", "15", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:plains", -0.5f, 0.0f, 0.2f, 0.0f, 0.0f, 0.0f, "Ganymedean Plains")
            .skyColor(0x0a0a15)
            .fogColor(0x101020)
            .hasAtmosphere(false)
            .ambientLight(0.03f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(35000)
            .starBrightness(2.0f)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .generate();

        // CALLISTO - Ancient heavily cratered moon
        // Old surface, very cratered, sparse activity
        registerPlanet("callisto")
            .vanillaQualityCratered()
            .coordinateShift(30000, 45000)
            .gravity(0.13f)
            .temperature(-139) // Callisto surface temperature
            .surfaceBlock("minecraft:gray_terracotta")
            .subsurfaceBlock("minecraft:brown_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Callisto ores - Plan B: IE lead-rich ancient moon + iron
            .configureOre("lead", 25)      // primary - IE ore, ancient heavy deposits
            .configureOre("tin", 20)       // secondary - Mekanism ore, industrial metal (fallback for nickel)
            .configureOre("iron", 15)      // tertiary - rocky moon core
            .configureOre("gold", 12)      // uncommon - precious metal seams
            .configureOre("diamond", 5)    // rare - impact diamonds
            .configureConditionalOre("nickel", 20, "immersiveengineering", "tin") // IE: nickel replaces tin
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:endermite", "20", "1", "2"},
                    {"monster", "minecraft:silverfish", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "20", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "10", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:desert", -0.7f, -0.5f, 0.3f, 0.0f, 0.0f, 0.0f, "Callisto Craters")
            .skyColor(0x050508)
            .fogColor(0x080810)
            .hasAtmosphere(false)
            .ambientLight(0.02f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(45000)
            .starBrightness(2.3f)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // ==================== SATURN'S MOONS ====================

        // TITAN - Thick orange atmosphere, methane lakes
        // Dense atmosphere but toxic, methane rain, alien terrain
        registerPlanet("titan")
            .vanillaQualityAlien()
            .coordinateShift(45000, 45000)
            .gravity(0.14f)
            .temperature(-179) // Titan surface temperature
            .surfaceBlock("minecraft:orange_terracotta")
            .subsurfaceBlock("minecraft:brown_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:gray_terracotta")
            .defaultBlock("minecraft:brown_terracotta")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(50)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Titan ores - Plan B: hydrocarbon moon with osmium, lead, and redstone
            .configureOre("coal", 25)      // primary - hydrocarbon deposits
            .configureOre("lead", 18)      // secondary - IE ore, organic sediment metals
            .configureOre("osmium", 15)    // tertiary - Mekanism ore source
            .configureOre("redstone", 12)  // uncommon - chemical energy from hydrocarbons
            .configureOre("calorite", 3)   // rare - Tier 4 progression ore
            .configureOre("etrium", 2)     // ENDGAME ORE - unchanged
            .clearAllMobSpawns()
            .addModReplacementSpawns("shineals_prehistoric_expansion",
                new String[][] {
                    {"monster", "minecraft:slime", "25", "2", "4"},
                    {"monster", "minecraft:witch", "15", "1", "1"},
                    {"creature", "minecraft:axolotl", "20", "2", "4"}
                },
                new String[][] {
                    {"monster", "shineals_prehistoric_expansion:utahraptor", "20", "1", "2"},
                    {"creature", "shineals_prehistoric_expansion:mammoth", "15", "1", "2"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:swamp", 0.3f, 0.8f, 0.0f, 0.0f, 0.0f, 0.0f, "Titan Methane Lakes")
            .skyColor(0xCC8844)
            .fogColor(0xAA6633)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.15f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("dramatic_alien")
            // Enable swamp WhenDungeonsArise structures (mushroom villages, etc.)
            .enableDungeonsAriseStructures("swamp")
            .generate();

        // ENCELADUS - Ice moon with geysers
        // Frozen, geyser activity, cratered, potential water
        registerPlanet("enceladus")
            .vanillaQualityCratered()
            .coordinateShift(60000, 45000)
            .gravity(0.05f)  // Minimum viable gravity
            .temperature(-201) // Enceladus surface temperature
            .surfaceBlock("minecraft:snow_block")
            .subsurfaceBlock("minecraft:packed_ice")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:blue_ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Airless icy moon - no liquid water
            .oreVeinsEnabled(true)
            // Enceladus ores - fluorite-rich geyser moon
            .configureOre("fluorite", 25)  // primary - Mekanism ore, chemical processing deposits (fallback for silver)
            .configureOre("copper", 20)    // secondary - frozen metal seams
            .configureOre("diamond", 6)    // rare - pristine ice preserves gems
            .configureConditionalOre("silver", 25, "immersiveengineering", "fluorite") // IE: silver replaces fluorite
            .clearAllMobSpawns()
            .addModReplacementSpawns("luminousworld",
                new String[][] {
                    {"monster", "minecraft:stray", "10", "1", "1"},
                    {"monster", "minecraft:phantom", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "luminousworld:bone_stalker", "8", "1", "1"},
                    {"monster", "luminousworld:yeti", "6", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:snowy_plains", -1.0f, 0.0f, 0.2f, 0.0f, 0.0f, 0.0f, "Enceladus Ice Fields")
            .skyColor(0x000005)
            .fogColor(0x000008)
            .hasAtmosphere(false)
            .ambientLight(0.02f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(50000)
            .starBrightness(2.5f)
            .addSun()
            .addCavePreset("frozen")
            .generate();

        // ==================== NEPTUNE'S MOON ====================

        // TRITON - Frozen nitrogen world
        // Extremely cold, pink nitrogen ice, retrograde orbit
        registerPlanet("triton")
            .vanillaQualityFlat()
            .coordinateShift(75000, 45000)
            .gravity(0.08f)
            .temperature(-235) // Triton frozen nitrogen
            .surfaceBlock("minecraft:pink_terracotta")
            .subsurfaceBlock("minecraft:white_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:light_gray_terracotta")
            .defaultBlock("minecraft:white_terracotta")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Triton ores - frozen nitrogen world with lapis, iron, and ostrum
            .configureOre("lapis", 25)     // primary - utility ore, frozen nitrogen deposits (fallback for silver)
            .configureOre("iron", 15)      // secondary - rocky core deposits
            .configureOre("copper", 12)    // tertiary - frozen metal seams
            .configureOre("diamond", 6)    // rare - pristine ancient deposits
            .configureOre("ostrum", 3)     // rare - Tier 3 progression ore
            .configureConditionalOre("silver", 25, "immersiveengineering", "lapis") // IE: silver replaces lapis
            .clearAllMobSpawns()
            .addModReplacementSpawns("luminousworld",
                new String[][] {
                    {"monster", "minecraft:phantom", "10", "1", "1"},
                    {"monster", "minecraft:stray", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "luminousworld:bone_stalker", "8", "1", "1"},
                    {"monster", "luminousworld:mummy", "6", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:snowy_plains", -1.0f, -0.8f, 0.0f, 0.0f, 0.0f, 0.0f, "Triton Nitrogen Plains")
            .skyColor(0x000003)
            .fogColor(0x000005)
            .hasAtmosphere(false)
            .ambientLight(0.01f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(60000)
            .starBrightness(2.8f)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // ==================== DWARF PLANETS ====================

        // CERES - Asteroid belt dwarf planet
        // Airless, metal-rich, cratered
        registerPlanet("ceres")
            .vanillaQualityCratered()
            .coordinateShift(90000, 45000)
            .gravity(0.05f)  // Minimum viable gravity
            .temperature(-106) // Ceres average surface
            .surfaceBlock("minecraft:gray_terracotta")
            .subsurfaceBlock("minecraft:stone")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Ceres ores - Plan B: THE mining hub for Mekanism osmium
            .configureOre("osmium", 25)    // primary - Mekanism ore, major source
            .configureOre("uranium", 20)   // secondary - Mekanism ore, asteroid radioactives (fallback for nickel)
            .configureOre("iron", 15)      // tertiary - basic asteroid metal
            .configureOre("fluorite", 4)   // rare - Mekanism ore, mineral deposits (fallback for silver)
            .configureOre("etrium", 4)     // ENDGAME ORE - unchanged
            .configureConditionalOre("silver", 4, "immersiveengineering", "fluorite") // IE: silver replaces fluorite
            .configureConditionalOre("nickel", 20, "immersiveengineering", "uranium") // IE: nickel replaces uranium
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "25", "2", "4"},
                    {"monster", "minecraft:endermite", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "25", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "12", "1", "2"},
                    {"monster", "kobolds:kobold_engineer", "12", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:desert", -0.5f, -0.9f, 0.3f, 0.0f, 0.0f, 0.0f, "Ceres Mining Zone")
            .skyColor(0x000000)
            .fogColor(0x000000)
            .hasAtmosphere(false)
            .ambientLight(0.0f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(55000)
            .starBrightness(2.6f)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // PLUTO - Distant frozen dwarf planet
        // Very cold, nitrogen ice, heart-shaped terrain
        registerPlanet("pluto")
            .vanillaQualityFlat()
            .coordinateShift(30000, 60000)
            .gravity(0.06f)
            .temperature(-229) // Pluto frozen surface
            .surfaceBlock("minecraft:powder_snow")
            .subsurfaceBlock("minecraft:snow_block")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:packed_ice")
            .defaultBlock("minecraft:snow_block")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Pluto ores - frozen dwarf planet with gold, tin, and iron
            .configureOre("gold", 25)      // primary - ancient precious deposits
            .configureOre("tin", 20)       // secondary - Mekanism ore, frozen industrial deposits (fallback for silver)
            .configureOre("iron", 12)      // tertiary - rocky core deposits
            .configureOre("etrium", 5)     // ENDGAME ORE - unchanged
            .configureConditionalOre("silver", 20, "immersiveengineering", "tin") // IE: silver replaces tin
            .clearAllMobSpawns()
            .addModReplacementSpawns("mobs_of_mythology",
                new String[][] {
                    {"monster", "minecraft:phantom", "10", "1", "1"},
                    {"monster", "minecraft:stray", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "mobs_of_mythology:chupacabra", "8", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:snowy_plains", -1.0f, -0.9f, 0.0f, 0.0f, 0.0f, 0.0f, "Pluto Ice Fields")
            .skyColor(0x000002)
            .fogColor(0x000003)
            .hasAtmosphere(false)
            .ambientLight(0.005f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(70000)
            .starBrightness(3.0f)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // ERIS - Most distant dwarf planet
        // Extremely cold and dark, sparse
        registerPlanet("eris")
            .vanillaQualityFlat()
            .coordinateShift(45000, 60000)
            .gravity(0.08f)
            .temperature(-231) // Eris extremely cold
            .surfaceBlock("minecraft:light_gray_terracotta")
            .subsurfaceBlock("minecraft:gray_terracotta")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:black_terracotta")
            .defaultBlock("minecraft:gray_terracotta")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Eris ores - Plan B: distant dark dwarf planet with lead and iron
            .configureOre("lead", 25)      // primary - IE ore, heavy ancient deposits
            .configureOre("copper", 20)    // secondary - frozen metal seams
            .configureOre("iron", 15)      // tertiary - rocky core deposits
            .configureOre("diamond", 6)    // rare - pristine ancient deposits
            .clearAllMobSpawns()
            .addModReplacementSpawns("mobs_of_mythology",
                new String[][] {
                    {"monster", "minecraft:endermite", "8", "1", "1"},
                    {"monster", "minecraft:phantom", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "mobs_of_mythology:automaton", "5", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:snowy_plains", -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, "Eris Dark Plains")
            .skyColor(0x000001)
            .fogColor(0x000001)
            .hasAtmosphere(false)
            .ambientLight(0.002f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)
            .starCount(80000)
            .starBrightness(3.2f)
            .addSun()
            .addCavePreset("minimal_airless")
            .generate();

        // ==================== HABITABLE EXOPLANETS ====================

        // KEPLER22B - Ocean world, habitable
        // Archipelago terrain, lots of water, breathable
        registerPlanet("kepler22b")
            .vanillaQualityArchipelago()
            .coordinateShift(60000, 60000)
            .gravity(1.2f)
            .temperature(22) // Warm ocean world
            .surfaceBlock("minecraft:sand")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(true)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(80)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Kepler22b ores - Plan B: ocean world with osmium for colony tech
            .configureOre("lapis", 25)     // primary - ocean blue gems
            .configureOre("osmium", 20)    // secondary - Mekanism ore, colony technology
            .configureOre("copper", 15)    // tertiary - underwater deposits
            .configureOre("diamond", 5)    // rare - deep ocean floor
            .configureOre("etrium", 2)     // ENDGAME ORE - unchanged
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:dolphin", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:turtle", 6, 1, 2)
            .addMobSpawn("water_ambient", "minecraft:cod", 10, 2, 3)
            .addMobSpawn("water_ambient", "minecraft:salmon", 8, 2, 3)
            .addMobSpawn("water_ambient", "minecraft:tropical_fish", 12, 2, 4)
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:drowned", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:corpse_fish", "15", "2", "4"},
                    {"monster", "born_in_chaos_v1:glutton_fish", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:zombie_fisherman", "12", "1", "2"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:beach", 0.5f, 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, "Kepler Island Shores")
            .addBiome("minecraft:warm_ocean", 0.7f, 0.8f, -0.3f, 0.0f, 0.0f, 0.0f, "Kepler Warm Seas")
            .skyColor(0x6EB5FF)
            .fogColor(0x9ED2FF)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.0f)  // Must be low for night-time mob spawning (vanilla Overworld = 0.0)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures
            // Enable ocean WhenDungeonsArise structures (lighthouses, fishing huts, illager ships)
            .enableDungeonsAriseStructures("ocean")
            // Enable Seven Seas underwater structures (shipwrecks, pirate ships, sea forts)
            .enableSevenSeasStructures()
            .generate();

        // KEPLER442B - Forest world, dense life
        // Standard terrain, lush forests, lots of mobs
        registerPlanet("kepler442b")
            .vanillaQualityStandard()
            .coordinateShift(75000, 60000)
            .gravity(1.0f)
            .temperature(15) // Earth-like forest world
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(true)  // multi-layer surface
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:deepslate")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Kepler442b ores - Plan B: forest world with emerald and osmium
            .configureOre("emerald", 25)   // primary - forest gem deposits
            .configureOre("coal", 20)      // secondary - organic-rich soils
            .configureOre("osmium", 15)    // tertiary - Mekanism ore, colony tech
            .configureOre("copper", 12)    // rare - forest floor deposits
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"creature", "minecraft:cow", "8", "2", "3"},
                    {"creature", "minecraft:pig", "8", "2", "3"},
                    {"creature", "minecraft:sheep", "8", "2", "3"},
                    {"creature", "minecraft:chicken", "10", "2", "4"},
                    {"creature", "minecraft:rabbit", "6", "1", "2"},
                    {"creature", "minecraft:fox", "4", "1", "1"},
                    {"creature", "minecraft:wolf", "3", "1", "2"},
                    {"monster", "minecraft:zombie", "8", "1", "2"},
                    {"monster", "minecraft:skeleton", "8", "1", "2"},
                    {"monster", "minecraft:creeper", "6", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:mother_spider", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:dread_hound", "8", "1", "2"},
                    {"monster", "born_in_chaos_v1:phantom_creeper", "6", "1", "1"},
                    {"monster", "born_in_chaos_v1:swarmer", "8", "2", "3"},
                    {"creature", "minecraft:cow", "8", "2", "3"},
                    {"creature", "minecraft:pig", "8", "2", "3"},
                    {"creature", "minecraft:sheep", "8", "2", "3"},
                    {"creature", "minecraft:chicken", "10", "2", "4"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:forest", 0.5f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, "Kepler Woodlands")
            .addBiome("minecraft:birch_forest", 0.4f, 0.5f, 0.1f, 0.0f, 0.0f, 0.0f, "Kepler Birch Groves")
            .addBiome("minecraft:plains", 0.5f, 0.4f, 0.0f, 0.0f, 0.0f, 0.0f, "Kepler Meadows")
            .skyColor(0x87CEEB)
            .fogColor(0xC0E0C0)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.0f)  // Must be low for night-time mob spawning (vanilla Overworld = 0.0)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            // Enable forest WhenDungeonsArise structures (bandit villages, greenwood pubs, etc.)
            .enableDungeonsAriseStructures("forest")
            .generate();

        // PROXIMA_B - Tidally locked world
        // One side always facing star (hot), twilight zone habitable
        registerPlanet("proxima_b")
            .vanillaQualityAlien()
            .coordinateShift(90000, 60000)
            .gravity(1.1f)
            .temperature(-40) // Tidally locked twilight zone
            .surfaceBlock("minecraft:red_sand")
            .subsurfaceBlock("minecraft:red_sandstone")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(40)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Proxima B ores - Plan B: tidally locked with lead and redstone
            .configureOre("redstone", 25)  // primary - thermal energy from tidal forces
            .configureOre("lead", 20)      // secondary - IE ore, heavy metal deposits
            .configureOre("copper", 15)    // tertiary - twilight zone deposits
            .configureOre("gold", 10)      // rare - precious metal seams
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:husk", "30", "2", "3"},
                    {"creature", "minecraft:rabbit", "18", "2", "3"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:decaying_zombie", "20", "1", "2"},
                    {"monster", "born_in_chaos_v1:bonescaller", "10", "1", "1"},
                    {"creature", "minecraft:rabbit", "18", "2", "3"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:badlands", 0.8f, -0.7f, 0.3f, 0.0f, 0.0f, 0.0f, "Proxima Twilight Zone")
            .skyColor(0xFF6B6B)
            .fogColor(0xCC5555)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.6f)
            .cloudsEnabled(true)
            .rainEnabled(false)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            // Enable badlands WhenDungeonsArise structures (coliseum, ceryneian hind)
            .enableDungeonsAriseStructures("badlands")
            .generate();

        // TRAPPIST1E - Red dwarf star system, habitable
        // Smaller red sun, dense life, unique ecosystem
        registerPlanet("trappist1e")
            .vanillaQualityStandard()
            .coordinateShift(30000, 75000)
            .gravity(0.9f)
            .temperature(12) // Habitable red dwarf world
            .surfaceBlock("minecraft:dirt")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Trappist1E ores - Plan B: red dwarf world with lapis and osmium
            .configureOre("lapis", 25)     // primary - unique crystalline formations
            .configureOre("coal", 20)      // secondary - dense organic life
            .configureOre("osmium", 15)    // tertiary - Mekanism ore, colony tech
            .configureOre("diamond", 5)    // rare - deep geological deposits
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"creature", "minecraft:cow", "10", "2", "3"},
                    {"creature", "minecraft:pig", "10", "2", "3"},
                    {"creature", "minecraft:sheep", "10", "2", "3"},
                    {"creature", "minecraft:chicken", "12", "2", "4"},
                    {"creature", "minecraft:bee", "8", "1", "2"},
                    {"creature", "minecraft:frog", "6", "1", "2"},
                    {"monster", "minecraft:zombie", "6", "1", "1"},
                    {"monster", "minecraft:skeleton", "6", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "8", "1", "2"},
                    {"monster", "born_in_chaos_v1:thornshell_crab", "6", "1", "1"},
                    {"creature", "minecraft:cow", "10", "2", "3"},
                    {"creature", "minecraft:pig", "10", "2", "3"},
                    {"creature", "minecraft:sheep", "10", "2", "3"},
                    {"creature", "minecraft:chicken", "12", "2", "4"},
                    {"creature", "minecraft:bee", "8", "1", "2"},
                    {"creature", "minecraft:frog", "6", "1", "2"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:flower_forest", 0.6f, 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, "Trappist Gardens")
            .addBiome("minecraft:meadow", 0.5f, 0.6f, 0.2f, 0.0f, 0.0f, 0.0f, "Trappist Meadows")
            .skyColor(0xFFAAAA)
            .fogColor(0xFFCCCC)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.75f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            // Enable plains WhenDungeonsArise structures (campsites, windmills fit meadows)
            .enableDungeonsAriseStructures("plains")
            .generate();

        // GLIESE667C - Super-Earth, mountainous
        // Higher gravity, dramatic mountains, habitable
        registerPlanet("gliese667c")
            .temperature(10) // Mountainous super-Earth
            .vanillaQualityMountainous()
            .coordinateShift(45000, 75000)
            .gravity(1.4f)
            .surfaceBlock("minecraft:dirt")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Gliese667C ores - Plan B: super-Earth with emerald, osmium, and redstone
            .configureOre("emerald", 25)   // primary - mountain gem deposits
            .configureOre("osmium", 20)    // secondary - Mekanism ore, super-Earth core
            .configureOre("redstone", 15)  // tertiary - active tectonic energy
            .configureOre("gold", 12)      // uncommon - precious mountain veins
            .configureOre("diamond", 6)    // rare - deep mountain deposits
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:goat", 12, 2, 3)
            .addMobSpawn("creature", "minecraft:llama", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:sheep", 8, 1, 2)
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:skeleton", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:supreme_bonescaller", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:lifestealer", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:krampus", "3", "1", "1"},
                    {"monster", "born_in_chaos_v1:missioner", "5", "1", "1"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:windswept_hills", 0.3f, 0.3f, 0.5f, 0.0f, 0.0f, 0.0f, "Gliese Highlands")
            .addBiome("minecraft:stony_peaks", 0.2f, 0.1f, 0.7f, 0.0f, 0.0f, 0.0f, "Gliese Peaks")
            .skyColor(0x9999FF)
            .fogColor(0xAAAAFF)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.7f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .snowEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures (Dark Tower, etc.)
            // Enable mountain WhenDungeonsArise structures (keep kayra, foundry, mechanical nest)
            .enableDungeonsAriseStructures("mountain")
            .generate();

        // ==================== ALIEN WORLDS ====================

        // PYRIOS - Volcanic hellscape
        // Constant fire damage, lava everywhere, extreme danger
        registerPlanet("pyrios")
            .vanillaQualityMountainous()
            .coordinateShift(60000, 75000)
            .gravity(1.0f)
            .temperature(800) // Volcanic hellscape
            .surfaceBlock("minecraft:blackstone")
            .subsurfaceBlock("minecraft:basalt")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:netherrack")
            .defaultBlock("minecraft:basalt")
            .defaultFluid("minecraft:lava")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(40)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Pyrios ores - volcanic hellscape with gold and uranium
            .configureOre("gold", 25)      // primary - volcanic gold deposits
            .configureOre("uranium", 20)   // secondary - Mekanism ore, radioactive volcanic seams (fallback for nickel)
            .configureOre("redstone", 15)  // tertiary - thermal energy
            .configureOre("calorite", 3)   // rare - Tier 4 progression ore
            .configureConditionalOre("nickel", 20, "immersiveengineering", "uranium") // IE: nickel replaces uranium
            .clearAllMobSpawns()
            .addModReplacementSpawns("rottencreatures",
                new String[][] {
                    {"monster", "minecraft:ghast", "15", "1", "1"},
                    {"monster", "minecraft:magma_cube", "25", "1", "3"}
                },
                new String[][] {
                    {"monster", "rottencreatures:burned", "20", "1", "2"},
                    {"monster", "rottencreatures:immortal", "15", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
            .addMobSpawn("creature", "minecraft:strider", 20, 1, 2)
            .clearBiomes()
            .addBiome("minecraft:basalt_deltas", 1.0f, -0.8f, 0.4f, 0.0f, 0.0f, 0.0f, "Pyrios Lava Fields")
            .skyColor(0x330000)
            .fogColor(0x551100)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.5f)
            .fireDamage(true)
            .fireDamageAmount(1.5f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("lava_tubes")
            .generate();

        // FRIGIDUM - Frozen wasteland
        // Extreme cold, blizzards, sparse hostile life
        registerPlanet("frigidum")
            .vanillaQualityFlat()
            .coordinateShift(75000, 75000)
            .gravity(1.1f)
            .temperature(-80) // Frozen wasteland
            .surfaceBlock("minecraft:snow_block")
            .subsurfaceBlock("minecraft:packed_ice")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:blue_ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Frigidum ores - frozen wasteland with gold
            .configureOre("gold", 25)      // primary - precious deposits in frozen wasteland (fallback for silver)
            .configureOre("coal", 20)      // secondary - ancient organic matter
            .configureOre("copper", 15)    // tertiary - frozen metal seams
            .configureOre("diamond", 5)    // rare - preserved in ice
            .configureConditionalOre("silver", 25, "immersiveengineering", "gold") // IE: silver replaces gold
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:stray", "30", "2", "3"},
                    {"monster", "minecraft:skeleton", "20", "1", "2"},
                    {"creature", "minecraft:polar_bear", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:mother_spider", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:dread_hound", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:dire_hound_leader", "3", "1", "1"},
                    {"monster", "born_in_chaos_v1:nightmare_stalker", "5", "1", "1"},
                    {"creature", "minecraft:polar_bear", "15", "1", "2"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:snowy_plains", -1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, "Frigidum Ice Fields")
            .addBiome("minecraft:ice_spikes", -1.0f, 0.3f, 0.3f, 0.0f, 0.0f, 0.0f, "Frigidum Ice Formations")
            .skyColor(0xDDEEFF)
            .fogColor(0xCCDDEE)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.8f)
            .cloudsEnabled(true)
            .rainEnabled(false)
            .snowEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("frozen")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures
            // Enable snowy WhenDungeonsArise structures
            .enableDungeonsAriseStructures("snowy")
            .generate();

        // ARENOS - Desert with twin suns
        // Hot desert, twin suns, sparse life
        registerPlanet("arenos")
            .vanillaQualityFlat()
            .coordinateShift(90000, 75000)
            .gravity(1.0f)
            .temperature(55) // Hot desert twin suns
            .surfaceBlock("minecraft:sand")
            .subsurfaceBlock("minecraft:sandstone")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:smooth_sandstone")
            .defaultBlock("minecraft:sandstone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Arenos ores - Plan B: twin-sun desert with copper, iron, and redstone
            .configureOre("copper", 25)    // primary - desert metal deposits
            .configureOre("lead", 18)      // secondary - IE ore, buried heavy metals
            .configureOre("iron", 15)      // tertiary - rocky deposits
            .configureOre("redstone", 12)  // uncommon - thermal energy from twin suns
            .configureOre("gold", 10)      // rare - ancient buried treasure
            .configureOre("diamond", 4)    // rare - deep desert veins
            .clearAllMobSpawns()
            .addModReplacementSpawns("reptilian",
                new String[][] {
                    {"monster", "minecraft:husk", "40", "2", "4"},
                    {"creature", "minecraft:rabbit", "15", "2", "3"}
                },
                new String[][] {
                    {"creature", "reptilian:gecko", "12", "1", "2"},
                    {"creature", "reptilian:komodo_dragon", "10", "1", "2"},
                    {"creature", "minecraft:rabbit", "15", "2", "3"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:desert", 1.0f, -0.9f, 0.0f, 0.0f, 0.0f, 0.0f, "Arenos Dunes")
            .skyColor(0xFFDD88)
            .fogColor(0xFFCC77)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.95f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(false)
            .addSun()
            .addSun()
            .addCavePreset("minimal_airless")
            // Enable desert WhenDungeonsArise structures
            .enableDungeonsAriseStructures("desert")
            .generate();

        // PALUDIS - Swamp world
        // Dense fog, murky swamps, lots of hostile life
        registerPlanet("paludis")
            .vanillaQualityFlat()
            .coordinateShift(30000, 90000)
            .gravity(0.9f)
            .temperature(28) // Warm humid swamp
            .surfaceBlock("minecraft:mud")
            .subsurfaceBlock("minecraft:muddy_mangrove_roots")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:clay")
            .defaultBlock("minecraft:clay")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(58)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Paludis ores - Plan B: swamp world with organic deposits and buried heavy metals
            .configureOre("coal", 25)      // primary - organic matter from swamp
            .configureOre("gold", 20)      // secondary - ancient silt deposits
            .configureOre("lead", 15)      // tertiary - IE ore, heavy metals in mud
            .configureOre("copper", 10)    // rare - oxidized deposits
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:slime", 15, 1, 2)
            .addMobSpawn("monster", "minecraft:witch", 8, 1, 1)
            .addMobSpawn("monster", "minecraft:drowned", 10, 1, 2)
            .addMobSpawn("creature", "minecraft:frog", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:axolotl", 6, 1, 2)
            .addRibbitsMobs(50)
            .addBornInChaosAquatic(18)
            .clearBiomes()
            .addBiome("minecraft:swamp", 0.8f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, "Paludis Swamps")
            .addBiome("minecraft:mangrove_swamp", 0.9f, 0.9f, -0.1f, 0.0f, 0.0f, 0.0f, "Paludis Mangroves")
            .skyColor(0x556655)
            .fogColor(0x445544)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.4f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("dramatic_alien")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures
            // Enable Ribbits structures (villages, temples, etc.) for this swamp world
            .enableRibbitsStructures()
            // Enable swamp WhenDungeonsArise structures
            .enableDungeonsAriseStructures("swamp")
            .generate();

        // LUXORIA - Alien bioluminescent jungle
        // Toxic to humans, dense alien life, bioluminescent
        registerPlanet("luxoria")
            .vanillaQualityAlien()
            .coordinateShift(45000, 90000)
            .gravity(0.8f)
            .temperature(32) // Tropical bioluminescent jungle
            .surfaceBlock("minecraft:moss_block")
            .subsurfaceBlock("minecraft:rooted_dirt")
            .singleLayerSurface(true)  // moss_block = single layer
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Luxoria ores - Plan B: bioluminescent jungle with unique gems
            .configureOre("emerald", 25)   // primary - alien gem formations
            .configureOre("lapis", 20)     // secondary - bioluminescent crystals
            .configureOre("gold", 15)      // tertiary - jungle treasure
            .configureOre("diamond", 6)    // rare - deep gem veins
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:glow_squid", 12, 1, 2)
            .addMobSpawn("creature", "minecraft:axolotl", 12, 1, 2)
            .addMobSpawn("creature", "minecraft:parrot", 10, 1, 2)
            .addMobSpawn("creature", "minecraft:ocelot", 6, 1, 1)
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:phantom", "6", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:restless_spirit", "18", "1", "2"},
                    {"monster", "born_in_chaos_v1:seared_spirit", "13", "1", "1"},
                    {"monster", "born_in_chaos_v1:firelight", "13", "1", "1"},
                    {"monster", "born_in_chaos_v1:pumpkin_spirit", "10", "1", "1"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:jungle", 0.9f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, "Luxoria Rainforest")
            .addBiome("minecraft:lush_caves", 0.8f, 0.9f, -0.2f, 0.0f, 0.0f, 0.0f, "Luxoria Glowing Caves")
            .skyColor(0x4466AA)
            .fogColor(0x336688)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.3f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("dramatic_alien")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures
            // Enable jungle WhenDungeonsArise structures (jungle tree houses, infested temples)
            .enableDungeonsAriseStructures("jungle")
            .generate();

        // ==================== ADDITIONAL UNIQUE WORLDS ====================

        // GLACIO - Icy world with thin atmosphere
        registerPlanet("glacio")
            .vanillaQualityStandard()
            .coordinateShift(60000, 90000)
            .gravity(0.8f)
            .temperature(-50) // Icy world with thin atmosphere
            .surfaceBlock("minecraft:snow_block")
            .subsurfaceBlock("minecraft:ice")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:packed_ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(50)
            .defaultFluid("minecraft:water")
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Glacio ores - icy world with tin and calorite
            .configureOre("coal", 25)      // primary - ancient frozen organics
            .configureOre("tin", 20)       // secondary - Mekanism ore, frozen industrial deposits (fallback for silver)
            .configureOre("calorite", 3)   // PROGRESSION ORE - needed for Tier 4 rocket
            .configureOre("glacio_etrium", 3) // ENDGAME ORE - unchanged
            .configureConditionalOre("silver", 20, "immersiveengineering", "tin") // IE: silver replaces tin
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:stray", "30", "2", "3"},
                    {"monster", "minecraft:skeleton", "15", "1", "2"},
                    {"creature", "minecraft:polar_bear", "20", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "20", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "10", "1", "2"},
                    {"monster", "kobolds:kobold_enchanter", "5", "1", "1"},
                    {"monster", "kobolds:kobold_engineer", "10", "1", "2"},
                    {"creature", "minecraft:polar_bear", "20", "1", "2"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:snowy_taiga", -0.8f, 0.4f, 0.0f, 0.0f, 0.0f, 0.0f, "Glacio Tundra")
            .addBiome("minecraft:frozen_river", -0.9f, 0.5f, -0.2f, 0.0f, 0.0f, 0.0f, "Glacio Frozen Rivers")
            .skyColor(0xE6F3FF)
            .fogColor(0xC0E8FF)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.7f)
            .cloudsEnabled(true)
            .rainEnabled(false)
            .snowEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("frozen")
            // Enable snowy WhenDungeonsArise structures (plague asylum, frozen ruins)
            .enableDungeonsAriseStructures("snowy")
            .generate();

        // VULCAN - Volcanic rocky world (more survivable volcanic world)
        registerPlanet("vulcan")
            .vanillaQualityCratered()
            .coordinateShift(75000, 90000)
            .gravity(1.2f)
            .temperature(200) // Volcanic rocky world
            .surfaceBlock("minecraft:tuff")
            .subsurfaceBlock("minecraft:basalt")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:basalt")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(30)
            .defaultFluid("minecraft:lava")
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Vulcan ores - Plan B: volcanic world with thermal energy ores
            .configureOre("redstone", 25)  // primary - thermal energy from volcanic activity
            .configureOre("fluorite", 20)  // secondary - Mekanism ore, chemical processing deposits (fallback for nickel)
            .configureOre("gold", 15)      // tertiary - volcanic treasure
            .configureOre("diamond", 6)    // rare - pressure-formed gems
            .configureConditionalOre("nickel", 20, "immersiveengineering", "fluorite") // IE: nickel replaces fluorite
            .clearAllMobSpawns()
            .addModReplacementSpawns("doom",
                new String[][] {
                    {"monster", "minecraft:magma_cube", "30", "1", "3"},
                    {"monster", "minecraft:ghast", "10", "1", "1"},
                    {"monster", "minecraft:blaze", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "doom:imp", "35", "2", "4"},
                    {"monster", "doom:lost_soul", "25", "1", "3"},
                    {"monster", "doom:cacodemon", "12", "1", "1"},
                    {"monster", "doom:pinky", "8", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:basalt_deltas", 0.8f, -0.6f, 0.3f, 0.0f, 0.0f, 0.0f, "Vulcan Lava Fields")
            .skyColor(0x442211)
            .fogColor(0x553322)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)
            .ambientLight(0.35f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("lava_tubes")
            .generate();

        // TERRA_NOVA - New Earth colony world
        registerPlanet("terra_nova")
            .vanillaQualityStandard()
            .coordinateShift(90000, 90000)
            .gravity(0.95f)
            .temperature(18) // Earth-like colony world
            .surfaceBlock("minecraft:mycelium")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(true)  // grass_block = single layer
            .deepBlock("minecraft:basalt")
            .defaultBlock("minecraft:end_stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Terra Nova ores - Plan B: colony world with Mekanism osmium
            .configureOre("coal", 25)      // primary - organic deposits
            .configureOre("osmium", 20)    // secondary - Mekanism ore, alien geology
            .configureOre("emerald", 15)   // tertiary - terraformed gem deposits
            .configureOre("copper", 10)    // rare - oxidized veins
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"creature", "minecraft:cow", "25", "2", "4"},
                    {"creature", "minecraft:pig", "25", "2", "4"},
                    {"creature", "minecraft:sheep", "25", "2", "4"},
                    {"creature", "minecraft:chicken", "30", "3", "5"},
                    {"creature", "minecraft:horse", "10", "2", "4"},
                    {"monster", "minecraft:zombie", "20", "2", "3"},
                    {"monster", "minecraft:skeleton", "20", "2", "3"},
                    {"monster", "minecraft:creeper", "12", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:mother_spider", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:dread_hound", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:phantom_creeper", "8", "1", "1"},
                    {"monster", "born_in_chaos_v1:swarmer", "8", "2", "3"},
                    {"monster", "born_in_chaos_v1:zombie_clown", "12", "1", "2"},
                    {"creature", "minecraft:cow", "25", "2", "4"},
                    {"creature", "minecraft:pig", "25", "2", "4"},
                    {"creature", "minecraft:sheep", "25", "2", "4"},
                    {"creature", "minecraft:chicken", "30", "3", "5"},
                    {"creature", "minecraft:horse", "10", "2", "4"}
                }
            )
            .clearBiomes()
            .addBiome("minecraft:plains", 0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, "Terra Nova Plains")
            .addBiome("minecraft:forest", 0.5f, 0.6f, 0.1f, 0.0f, 0.0f, 0.0f, "Terra Nova Forests")
            .addBiome("minecraft:taiga", 0.3f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, "Terra Nova Taiga")
            .skyColor(0x87CEEB)
            .fogColor(0xB0C4DE)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.0f)  // Must be low for night-time mob spawning (vanilla Overworld = 0.0)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures (Clown Caravan, etc.)
            // Enable plains and forest WhenDungeonsArise structures (bandit villages, campsites, etc.)
            .enableDungeonsAriseStructures("plains", "forest")
            .generate();

        // ==================== MOWZIES MOBS PLANETS ====================

        // PRIMORDIUM - Dense jungle world with ancient creatures (Latin: origin/beginning)
        // Mowzie's Mobs jungle creatures (Foliaath, Naga, Ferrous Wroughtnaut)
        registerPlanet("primordium")
            .vanillaQualityStandard()
            .coordinateShift(105000, 90000)
            .gravity(1.0f)
            .temperature(30) // Dense tropical jungle
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(true)  // grass_block = single layer
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // Primordium ores - Plan B: ancient jungle with buried heavy metals
            .configureOre("coal", 25)      // primary - organic jungle matter
            .configureOre("lead", 20)      // secondary - IE ore, ancient buried metals
            .configureOre("emerald", 15)   // tertiary - jungle temple treasure
            .configureOre("copper", 12)    // rare - oxidized jungle deposits
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:parrot", 18, 2, 3)
            .addMobSpawn("creature", "minecraft:ocelot", 12, 1, 2)
            .addModReplacementSpawns("mowziesmobs",
                new String[][] {
                    {"monster", "minecraft:spider", "20", "1", "2"},
                    {"monster", "minecraft:skeleton", "10", "1", "1"}
                },
                new String[][] {
                    {"monster", "mowziesmobs:foliaath", "70", "1", "4"},
                    {"monster", "mowziesmobs:baby_foliaath", "40", "2", "3"},
                    {"monster", "mowziesmobs:ferrous_wroughtnaut", "2", "1", "1"},
                    {"monster", "mowziesmobs:naga", "5", "1", "1"}
                }
            )
            .addBornInChaosCreatures(22)
            .clearBiomes()
            .addBiome("minecraft:jungle", 0.95f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, "Primordium Jungle")
            .addBiome("minecraft:bamboo_jungle", 0.95f, 0.85f, -0.1f, 0.0f, 0.0f, 0.0f, "Primordium Bamboo Groves")
            .addBiome("minecraft:sparse_jungle", 0.90f, 0.7f, 0.1f, 0.0f, 0.0f, 0.0f, "Primordium Forest Edge")
            .skyColor(0x55AA55)
            .fogColor(0x77CC77)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.7f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("lush")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures
            // Enable jungle WhenDungeonsArise structures (jungle tree houses, infested temples)
            .enableDungeonsAriseStructures("jungle")
            .generate();

        // BELLATOR - Savanna world with Umvuthana civilization (Latin: warrior)
        // Mowzie's Mobs savanna creatures (Umvuthana, Barakoa, Chiefs)
        registerPlanet("bellator")
            .vanillaQualityStandard()
            .coordinateShift(105000, 105000)
            .gravity(1.0f)
            .temperature(35) // Hot savanna world
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .singleLayerSurface(true)  // grass_block = single layer
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(50)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Dry savanna
            .oreVeinsEnabled(true)
            // Bellator ores - Plan B: warrior savanna with copper, iron, and tribal gold
            .configureOre("copper", 25)    // primary - savanna metal deposits
            .configureOre("gold", 20)      // secondary - tribal treasure hoards
            .configureOre("iron", 15)      // tertiary - tribal metalworking
            .configureOre("lead", 12)      // uncommon - IE ore, heavy metal veins
            .configureOre("diamond", 5)    // rare - deep savanna gems
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:llama", 15, 2, 4)
            .addModReplacementSpawns("mowziesmobs",
                new String[][] {
                    {"monster", "minecraft:husk", "25", "1", "2"},
                    {"monster", "minecraft:skeleton", "15", "1", "1"}
                },
                new String[][] {
                    {"monster", "mowziesmobs:umvuthana_raptor", "50", "1", "1"},
                    {"monster", "mowziesmobs:umvuthana_crane", "30", "1", "1"},
                    {"monster", "mowziesmobs:umvuthana", "40", "1", "2"}
                }
            )
            .addBornInChaosPumpkin(18)
            .clearBiomes()
            .addBiome("minecraft:savanna", 0.95f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, "Bellator Savanna")
            .addBiome("minecraft:savanna_plateau", 0.9f, -0.2f, 0.3f, 0.0f, 0.0f, 0.0f, "Bellator Highlands")
            .addBiome("minecraft:windswept_savanna", 0.85f, -0.3f, 0.4f, 0.0f, 0.0f, 0.0f, "Bellator Windswept")
            .skyColor(0xDDAA66)
            .fogColor(0xCCBB88)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.9f)
            .cloudsEnabled(true)
            .rainEnabled(false)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .enableBornInChaosStructures()   // Enable Born in Chaos structures
            // Enable plains WhenDungeonsArise structures (campsites, windmills fit savanna)
            .enableDungeonsAriseStructures("plains")
            .generate();

        // ==================== PROFUNDUS ====================

        // PROFUNDUS - Underground cave kingdom (Latin: the deep)
        // Cave-heavy terrain, dim lighting, kobold civilization
        registerPlanet("profundus")
            .vanillaQualityAlien()  // Alien terrain with deep caves
            .coordinateShift(120000, 120000)
            .gravity(0.85f)
            .temperature(20) // Stable underground cave temperature
            // Underground/cave aesthetic - stone and cobblestone surface
            .surfaceBlock("minecraft:cobblestone")
            .subsurfaceBlock("minecraft:stone")
            .singleLayerSurface(false)  // multi-layer surface
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // No surface water
            .oreVeinsEnabled(true)
            // Profundus ores - Plan B: underground cave kingdom with osmium and iron
            .configureOre("osmium", 25)    // primary - Mekanism ore, deep cave deposits
            .configureOre("iron", 20)      // secondary - kobold smithing essential
            .configureOre("lead", 15)      // tertiary - IE ore, heavy underground metals
            .configureOre("tin", 12)       // uncommon - Mekanism ore, industrial metal (fallback for nickel)
            .configureOre("emerald", 8)    // rare - kobold treasure hoards
            .configureConditionalOre("nickel", 12, "immersiveengineering", "tin") // IE: nickel replaces tin
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "30", "2", "4"},
                    {"monster", "minecraft:cave_spider", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "50", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "25", "1", "2"},
                    {"monster", "kobolds:kobold_enchanter", "16", "1", "1"},
                    {"monster", "kobolds:kobold_engineer", "25", "1", "2"},
                    {"monster", "kobolds:kobold_zombie", "15", "2", "4"},
                    {"monster", "kobolds:kobold_skeleton", "15", "2", "4"},
                    {"monster", "kobolds:witherbold", "5", "1", "2"}
                }
            )
            .addBornInChaosSpirits(20)
            .allowPeacefulMobs(false)
            .clearBiomes()
            .addBiome("minecraft:dripstone_caves", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, "Profundus Caverns")
            .addBiome("minecraft:lush_caves", 0.3f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, "Profundus Gardens")
            .addBiome("minecraft:deep_dark", -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, "Profundus Depths")
            .skyColor(0x1a1a1a)
            .fogColor(0x2a2a2a)
            .hasAtmosphere(true)
            .breathableAtmosphere(false)  // Stale air, not breathable
            .ambientLight(0.1f)  // Very dim underground
            .cloudsEnabled(false)
            .rainEnabled(false)
            .starsVisibleDuringDay(true)  // Dim enough to see stars
            .starCount(20000)
            .starBrightness(1.5f)
            .addSun()
            .addCavePreset("extreme_caves")  // Maximize cave generation
            .enableKoboldsStructures()       // Enable kobold dens
            .enableBornInChaosStructures()   // Enable Born in Chaos structures (Dark Tower, etc.)
            // Enable underground WhenDungeonsArise structures (mining systems, etc.)
            .enableDungeonsAriseStructures("underground")
            .generate();
    }
}