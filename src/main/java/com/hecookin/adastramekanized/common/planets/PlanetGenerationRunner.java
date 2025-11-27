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
     * Apply Moon mob preset
     */
    private static void applyMoonMobPreset(PlanetMaker.PlanetBuilder planet) {
        // Moon-specific mob spawning - matches the biome_modifier configuration
        planet.clearAllMobSpawns()  // Clear any default spawns
              //.addMobSpawn("monster", "minecraft:silverfish", 1000, 32, 64)  // Silverfish swarms (from biome_modifier)

                //.addMowziesMob("naga", 5, 1, 2)     // Nagas near water

              // Example: Equipped zombie variant (requires PlanetMobSpawnHandler)
              // This would spawn zombies with leather armor on the moon
              // NOTE: Equipment is handled by PlanetMobSpawnHandler event system
              /*.addEquippedMobSpawn("monster", "minecraft:zombie", 30, 1, 3,
                      "minecraft:leather_helmet", "minecraft:leather_chestplate", null, null,
                      "minecraft:stone_sword", null)*/


              // Example: Armored skeleton (like WDA's skeleton_armored variants)
              /*.addEquippedMobSpawn("monster", "minecraft:skeleton", 30, 1, 2,
                      "minecraft:chainmail_helmet", "minecraft:chainmail_chestplate",
                      "minecraft:chainmail_leggings", "minecraft:chainmail_boots",
                      "minecraft:bow", null)*/

              .allowPeacefulMobs(false);  // No peaceful mobs on moon
    }

    /**
     * Apply Mars mob preset
     */
    private static void applyMarsMobPreset(PlanetMaker.PlanetBuilder planet) {
        // Hostile mobs for Mars - harsh environment
        planet.addMobSpawn("monster", "minecraft:husk", 30, 2, 4)
              .addMobSpawn("monster", "minecraft:spider", 20, 1, 2)
              .addMobSpawn("monster", "minecraft:phantom", 10, 1, 2)
              .addMobSpawn("monster", "minecraft:enderman", 5, 1, 1);
    }

    /**
     * Apply test planet mob preset
     */
    private static void applyTestPlanetMobPreset(PlanetMaker.PlanetBuilder planet) {
        // Standard overworld mobs for testing
        planet.addMobSpawn("monster", "minecraft:zombie", 25, 2, 4)
              .addMobSpawn("monster", "minecraft:skeleton", 25, 2, 4)
              .addMobSpawn("monster", "minecraft:creeper", 25, 1, 2)
              .addMobSpawn("monster", "minecraft:spider", 25, 1, 3)
              .addMobSpawn("creature", "minecraft:sheep", 10, 2, 3)
              .addMobSpawn("creature", "minecraft:pig", 10, 2, 3);
    }

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
        // Moon planet - VANILLA-QUALITY TERRAIN with Moon blocks
        // Uses FULL vanilla density function set (offset, factor, jaggedness splines) with coordinate shift
        // This produces terrain with vanilla-quality spline-based terrain shaping, unique to Moon
        PlanetMaker.PlanetBuilder moon = registerPlanet("moon")
            .vanillaQualityCratered()      // Use vanilla-quality cratered terrain preset
            .coordinateShift(5000, 5000)   // Shift coordinates for unique Moon terrain
            // Physical properties
            .gravity(0.166f)  // Moon has 1/6 Earth gravity
            // Surface blocks - this is all we need to customize!
            .surfaceBlock("adastramekanized:moon_sand")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .deepBlock("adastramekanized:moon_stone")
            .defaultBlock("adastramekanized:moon_stone")      // Replaces vanilla stone (above Y=0)
            .deepslateBlock("adastramekanized:moon_stone")    // Replaces vanilla deepslate (below Y=0)
            .bedrockBlock("minecraft:bedrock")
            // World configuration
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Airless moon - no water
            .oreVeinsEnabled(true)
            // Custom Moon biomes (vanilla climate parameters work with vanilla noise!)
            .clearBiomes()
            .addBiome("adastramekanized:moon_highlands", -0.8f, -0.9f, 0.4f, 0.2f, 0.0f, 0.1f, "Lunar Highlands")
            .addBiome("adastramekanized:moon_maria", -0.6f, -0.8f, -0.2f, 0.3f, -0.5f, -0.1f, "Lunar Maria")
            .addBiome("adastramekanized:moon_crater_rim", 0.2f, 0.4f, 0.6f, 0.8f, 0.5f, 0.7f, "Crater Rim")
            .addBiome("adastramekanized:moon_crater_floor", -0.8f, -0.6f, -0.5f, -0.2f, -0.3f, 0.0f, "Crater Floor")
            .addBiome("adastramekanized:moon_polar", -1.0f, -0.75f, 0.0f, 0.2f, 0.0f, 0.3f, "Polar Region")
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
                2.0f, 0x4169E1, 0.5f, 0.8f, true
            )
            .starsVisibleDuringDay(true)
            .starCount(50000)
            .starBrightness(2.5f)
            .cloudsEnabled(false)
            .rainEnabled(false)
            .snowEnabled(false)
            // Moon ores - 4 ores: 2 common, 1 uncommon, 1 rare
            .configureOre("iron", 70)       // common - lunar regolith deposits
            .configureOre("aluminum", 60)   // common - abundant in lunar highlands
            .configureOre("gold", 30)       // uncommon - impact deposits
            .configureOre("platinum", 12);  // rare - deep crater deposits

        // Apply Moon mob preset
        applyMoonMobPreset(moon);
        moon.generate();

        // Mars planet - VANILLA-QUALITY TERRAIN with Mars blocks
        // Uses FULL vanilla density function set (offset, factor, jaggedness splines) with coordinate shift
        // This produces terrain with vanilla-quality spline-based terrain shaping, unique to Mars
        registerPlanet("mars")
            .vanillaQualityMountainous()   // Use vanilla-quality mountainous terrain for dramatic Martian landscape
            .coordinateShift(15000, 15000) // Shift coordinates for unique Mars terrain
            // Physical properties
            .gravity(0.38f)  // Mars has 3/8 Earth gravity
            // Surface blocks - this is all we need to customize!
            .surfaceBlock("adastramekanized:mars_sand")
            .subsurfaceBlock("adastramekanized:mars_stone")
            .deepBlock("adastramekanized:mars_stone")
            .defaultBlock("adastramekanized:mars_stone")      // Replaces vanilla stone (above Y=0)
            .deepslateBlock("adastramekanized:mars_stone")    // Replaces vanilla deepslate (below Y=0)
            .bedrockBlock("minecraft:bedrock")
            // World configuration
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Dry desert planet - no water
            .oreVeinsEnabled(true)
            // Mob spawning - Mars hostile environment
            .addMobSpawn("monster", "minecraft:husk", 50, 2, 4)
            .addMobSpawn("monster", "minecraft:spider", 30, 1, 2)
            .addMobSpawn("monster", "minecraft:phantom", 10, 1, 2)
            .addMobSpawn("monster", "minecraft:enderman", 15, 1, 1)
            // MCDoom demons - Mars red planet
            .addMCDoomDemon("cacodemon", 8, 1, 1)
            .addMCDoomDemon("imp", 12, 1, 2)
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
                0.3f, 0xAA8866, 0.4f, 0.15f, true
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
            // Mars ores - 4 ores: 2 common, 1 uncommon, 1 rare
            .configureOre("iron", 75)       // common - iron oxide rich soil
            .configureOre("copper", 60)     // common - volcanic deposits
            .configureOre("redstone", 35)   // uncommon - thermal energy
            .configureOre("diamond", 12)    // rare - ancient impact sites
            .generate();

        // ==================== SOLAR SYSTEM INNER PLANETS ====================

        // VENUS - Toxic atmosphere with sulfuric acid rain
        // Extremely hostile: acid rain damages players, thick yellow fog, very hot
        registerPlanet("venus")
            .vanillaQualityFlat()
            .coordinateShift(30000, 30000)
            .gravity(0.9f)
            .surfaceBlock("minecraft:yellow_terracotta")
            .subsurfaceBlock("minecraft:orange_terracotta")
            .deepBlock("minecraft:brown_terracotta")
            .defaultBlock("minecraft:orange_terracotta")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3-4 ores: 2 common, 1 uncommon, 1 rare
            .configureOre("copper", 70)    // common - sulfur-rich environment
            .configureOre("coal", 65)      // common
            .configureOre("iron", 35)      // uncommon
            .configureOre("gold", 12)      // rare
            // Sparse hostile mobs - too hostile for most life + MCDoom demons
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:magma_cube", 20, 1, 2)
            .addMobSpawn("monster", "minecraft:blaze", 10, 1, 1)
            // MCDoom demons - hot toxic hellscape
            .addMCDoomDemon("imp", 15, 1, 3)
            .addMCDoomDemon("lost_soul", 10, 1, 2)
            // Reptilian survivors - sparse heat-adapted lizards
            .addReptilianCreature("lizard", 8, 1, 1)
            .allowPeacefulMobs(false)
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
            .surfaceBlock("minecraft:light_gray_concrete")
            .subsurfaceBlock("minecraft:gray_concrete")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:gray_concrete")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3-4 ores: 2 common, 1 uncommon, 1 rare - metal-rich planet
            .configureOre("iron", 80)      // common - very metal-rich
            .configureOre("copper", 70)    // common
            .configureOre("nickel", 35)    // uncommon
            .configureOre("platinum", 12)  // rare
            // Sparse parasitic mobs + cave-dwelling kobolds
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:silverfish", 30, 2, 4)
            .addMobSpawn("monster", "minecraft:endermite", 10, 1, 2)
            // Sparse kobold cave dwellers
            .addKoboldsMobs(8)
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
            .surfaceBlock("minecraft:blue_ice")
            .subsurfaceBlock("minecraft:packed_ice")
            .deepBlock("minecraft:ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(32)
            .defaultFluid("minecraft:water")
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 3-4 ores: sparse but includes precious gems beneath ice
            .configureOre("iron", 65)      // common - basic metal
            .configureOre("copper", 55)    // common
            .configureOre("lapis", 30)     // uncommon - blue gem in blue ice
            .configureOre("diamond", 12)   // rare
            // Very sparse aquatic-themed mobs
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:drowned", 15, 1, 2)
            .addMobSpawn("creature", "minecraft:glow_squid", 10, 1, 3)
            .allowPeacefulMobs(true)
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
            .generate();

        // IO - Volcanic moon of Jupiter
        // Constant volcanic activity, fire damage, sulfur-rich
        registerPlanet("io")
            .vanillaQualityMountainous()
            .coordinateShift(75000, 30000)
            .gravity(0.18f)
            .surfaceBlock("minecraft:yellow_concrete")
            .subsurfaceBlock("minecraft:orange_concrete")
            .deepBlock("minecraft:netherrack")
            .defaultBlock("minecraft:orange_concrete")
            .defaultFluid("minecraft:lava")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(20)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3-4 ores: volcanic - rich in redstone and gold
            .configureOre("redstone", 80)  // common - sulfur-rich volcanic
            .configureOre("gold", 65)      // common - volcanic deposits
            .configureOre("iron", 35)      // uncommon
            .configureOre("diamond", 15)   // rare
            // Nether-type mobs thrive here + MCDoom demons
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:magma_cube", 50, 2, 4)
            .addMobSpawn("monster", "minecraft:blaze", 30, 1, 2)
            .addMobSpawn("monster", "minecraft:zombified_piglin", 20, 2, 4)
            .addMobSpawn("creature", "minecraft:strider", 40, 1, 2)
            // MCDoom demons - volcanic hellscape
            .addMCDoomDemon("imp", 15, 1, 3)
            .addMCDoomDemon("lost_soul", 10, 1, 2)
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
            .surfaceBlock("minecraft:gray_concrete")
            .subsurfaceBlock("minecraft:light_gray_concrete")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Airless moon - no water
            .oreVeinsEnabled(true)
            // 3-4 ores: balanced rocky moon
            .configureOre("iron", 70)      // common
            .configureOre("copper", 60)    // common
            .configureOre("gold", 30)      // uncommon
            .configureOre("diamond", 10)   // rare
            // Moderate mob spawning
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:skeleton", 20, 1, 2)
            .addMobSpawn("monster", "minecraft:spider", 15, 1, 2)
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
            .surfaceBlock("minecraft:gray_terracotta")
            .subsurfaceBlock("minecraft:brown_terracotta")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3-4 ores: ancient cratered moon with heavy metals
            .configureOre("iron", 70)      // common
            .configureOre("lead", 60)      // common - heavy ancient deposits
            .configureOre("gold", 55)      // uncommon
            .configureOre("diamond", 12)   // rare
            // Very sparse mobs + underground kobolds
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:endermite", 10, 1, 1)
            // Sparse kobold underground population
            .addKoboldsMobs(8)
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
            .surfaceBlock("minecraft:orange_terracotta")
            .subsurfaceBlock("minecraft:brown_terracotta")
            .deepBlock("minecraft:gray_terracotta")
            .defaultBlock("minecraft:brown_terracotta")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(50)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 3-4 ores: hydrocarbon-rich moon
            .configureOre("coal", 80)      // common - hydrocarbon rich
            .configureOre("iron", 65)      // common
            .configureOre("copper", 55)    // uncommon
            .configureOre("gold", 12)      // rare
            // Alien-themed mobs
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:slime", 30, 2, 4)
            .addMobSpawn("monster", "minecraft:witch", 15, 1, 1)
            .addMobSpawn("creature", "minecraft:axolotl", 20, 2, 4)
            .allowPeacefulMobs(true)
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
            .generate();

        // ENCELADUS - Ice moon with geysers
        // Frozen, geyser activity, cratered, potential water
        registerPlanet("enceladus")
            .vanillaQualityCratered()
            .coordinateShift(60000, 45000)
            .gravity(0.05f)  // Minimum viable gravity
            .surfaceBlock("minecraft:snow_block")
            .subsurfaceBlock("minecraft:packed_ice")
            .deepBlock("minecraft:blue_ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Airless icy moon - no liquid water
            .oreVeinsEnabled(true)
            // 3 ores: sparse icy moon
            .configureOre("iron", 65)      // common
            .configureOre("copper", 55)    // common
            .configureOre("diamond", 15)   // rare - pristine ice preserves gems
            // Almost no mobs
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:stray", 5, 1, 1)
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
            .surfaceBlock("minecraft:pink_concrete")
            .subsurfaceBlock("minecraft:white_concrete")
            .deepBlock("minecraft:light_gray_concrete")
            .defaultBlock("minecraft:white_concrete")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3 ores: frozen outer moon
            .configureOre("iron", 65)      // common
            .configureOre("copper", 55)    // common
            .configureOre("diamond", 15)    // rare
            // Almost lifeless
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:phantom", 5, 1, 1)
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
            .surfaceBlock("minecraft:gray_concrete")
            .subsurfaceBlock("minecraft:stone")
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 4 ores: metal-rich asteroid - the mining destination
            .configureOre("iron", 80)      // common - very metal rich
            .configureOre("osmium", 70)    // common - rare elsewhere
            .configureOre("nickel", 40)    // uncommon
            .configureOre("platinum", 15)   // rare
            // Minimal life + asteroid cave kobolds
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:silverfish", 20, 2, 4)
            // Sparse kobold miners in asteroid caves
            .addKoboldsMobs(10)
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
            .surfaceBlock("minecraft:white_concrete")
            .subsurfaceBlock("minecraft:light_gray_concrete")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:light_gray_concrete")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3 ores: frozen dwarf planet
            .configureOre("iron", 65)      // common
            .configureOre("gold", 55)      // common - ancient deposits
            .configureOre("diamond", 15)    // rare
            // Very sparse
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:phantom", 3, 1, 1)
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
            .surfaceBlock("minecraft:light_gray_concrete")
            .subsurfaceBlock("minecraft:gray_concrete")
            .deepBlock("minecraft:black_concrete")
            .defaultBlock("minecraft:gray_concrete")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 3 ores: distant dark dwarf planet
            .configureOre("iron", 65)      // common
            .configureOre("copper", 55)    // common
            .configureOre("diamond", 15)    // rare - pristine ancient deposits
            // Almost no life
            .clearAllMobSpawns()
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
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:sand")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(80)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: ocean world with aquatic resources
            .configureOre("iron", 70)      // common
            .configureOre("copper", 65)    // common
            .configureOre("lapis", 35)     // uncommon - ocean gems
            .configureOre("diamond", 12)    // rare
            // Reduced mob spawns for performance
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:dolphin", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:turtle", 6, 1, 2)
            .addMobSpawn("creature", "minecraft:cod", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:salmon", 8, 2, 3)
            .addMobSpawn("creature", "minecraft:tropical_fish", 12, 2, 4)
            .addMobSpawn("monster", "minecraft:drowned", 5, 1, 1)
            .allowPeacefulMobs(true)
            .clearBiomes()
            .addBiome("minecraft:beach", 0.5f, 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, "Kepler Island Shores")
            .addBiome("minecraft:warm_ocean", 0.7f, 0.8f, -0.3f, 0.0f, 0.0f, 0.0f, "Kepler Warm Seas")
            .skyColor(0x6EB5FF)
            .fogColor(0x9ED2FF)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.9f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .generate();

        // KEPLER442B - Forest world, dense life
        // Standard terrain, lush forests, lots of mobs
        registerPlanet("kepler442b")
            .vanillaQualityStandard()
            .coordinateShift(75000, 60000)
            .gravity(1.0f)
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: lush forest world
            .configureOre("coal", 80)      // common - organic rich
            .configureOre("iron", 70)      // common
            .configureOre("copper", 35)    // uncommon
            .configureOre("emerald", 12)    // rare - forest gems
            // Forest life (reduced for performance)
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:cow", 8, 2, 3)
            .addMobSpawn("creature", "minecraft:pig", 8, 2, 3)
            .addMobSpawn("creature", "minecraft:sheep", 8, 2, 3)
            .addMobSpawn("creature", "minecraft:chicken", 10, 2, 4)
            .addMobSpawn("creature", "minecraft:rabbit", 6, 1, 2)
            .addMobSpawn("creature", "minecraft:fox", 4, 1, 1)
            .addMobSpawn("creature", "minecraft:wolf", 3, 1, 2)
            .addMobSpawn("monster", "minecraft:zombie", 8, 1, 2)
            .addMobSpawn("monster", "minecraft:skeleton", 8, 1, 2)
            .addMobSpawn("monster", "minecraft:spider", 5, 1, 1)
            .addMobSpawn("monster", "minecraft:creeper", 4, 1, 1)
            .allowPeacefulMobs(true)
            .clearBiomes()
            .addBiome("minecraft:forest", 0.5f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, "Kepler Woodlands")
            .addBiome("minecraft:birch_forest", 0.4f, 0.5f, 0.1f, 0.0f, 0.0f, 0.0f, "Kepler Birch Groves")
            .addBiome("minecraft:plains", 0.5f, 0.4f, 0.0f, 0.0f, 0.0f, 0.0f, "Kepler Meadows")
            .skyColor(0x87CEEB)
            .fogColor(0xC0E0C0)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.85f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .generate();

        // PROXIMA_B - Tidally locked world
        // One side always facing star (hot), twilight zone habitable
        registerPlanet("proxima_b")
            .vanillaQualityAlien()
            .coordinateShift(90000, 60000)
            .gravity(1.1f)
            .surfaceBlock("minecraft:red_sand")
            .subsurfaceBlock("minecraft:red_sandstone")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(40)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: tidally locked world with thermal activity
            .configureOre("redstone", 80)  // common - thermal energy
            .configureOre("copper", 70)    // common
            .configureOre("gold", 35)      // uncommon
            .configureOre("diamond", 12)    // rare
            // Sparse but adapted life
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:husk", 25, 2, 3)
            .addMobSpawn("monster", "minecraft:cave_spider", 20, 1, 3)
            .addMobSpawn("creature", "minecraft:rabbit", 15, 2, 3)
            .allowPeacefulMobs(true)
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
            .generate();

        // TRAPPIST1E - Red dwarf star system, habitable
        // Smaller red sun, dense life, unique ecosystem
        registerPlanet("trappist1e")
            .vanillaQualityStandard()
            .coordinateShift(30000, 75000)
            .gravity(0.9f)
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: red dwarf system, unique resources
            .configureOre("iron", 70)      // common
            .configureOre("coal", 65)      // common - dense organic life
            .configureOre("lapis", 35)     // uncommon
            .configureOre("diamond", 12)    // rare
            // Lush life (reduced for performance)
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:cow", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:pig", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:sheep", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:chicken", 12, 2, 4)
            .addMobSpawn("creature", "minecraft:bee", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:frog", 6, 1, 2)
            .addMobSpawn("monster", "minecraft:zombie", 6, 1, 1)
            .addMobSpawn("monster", "minecraft:skeleton", 6, 1, 1)
            .allowPeacefulMobs(true)
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
            .generate();

        // GLIESE667C - Super-Earth, mountainous
        // Higher gravity, dramatic mountains, habitable
        registerPlanet("gliese667c")
            .vanillaQualityMountainous()
            .coordinateShift(45000, 75000)
            .gravity(1.4f)
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: super-Earth with rich mountain deposits
            .configureOre("iron", 80)      // common - super-Earth core
            .configureOre("gold", 65)      // common - rich in precious metals
            .configureOre("emerald", 40)   // uncommon - mountain gems
            .configureOre("diamond", 15)    // rare
            // Mountain-adapted life (reduced for performance)
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:goat", 12, 2, 3)
            .addMobSpawn("creature", "minecraft:llama", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:sheep", 8, 1, 2)
            .addMobSpawn("monster", "minecraft:skeleton", 8, 1, 2)
            .addMobSpawn("monster", "minecraft:spider", 5, 1, 1)
            .allowPeacefulMobs(true)
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
            .generate();

        // ==================== ALIEN WORLDS ====================

        // PYRIOS - Volcanic hellscape
        // Constant fire damage, lava everywhere, extreme danger
        registerPlanet("pyrios")
            .vanillaQualityMountainous()
            .coordinateShift(60000, 75000)
            .gravity(1.0f)
            .surfaceBlock("minecraft:blackstone")
            .subsurfaceBlock("minecraft:basalt")
            .deepBlock("minecraft:netherrack")
            .defaultBlock("minecraft:basalt")
            .defaultFluid("minecraft:lava")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(40)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 4 ores: volcanic with precious metals
            .configureOre("gold", 80)      // common - volcanic gold
            .configureOre("redstone", 70)  // common - thermal energy
            .configureOre("iron", 35)      // uncommon
            .configureOre("diamond", 15)    // rare - forged in extreme heat
            // Nether-type mobs + MCDoom demons
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:magma_cube", 15, 1, 2)
            .addMobSpawn("monster", "minecraft:blaze", 10, 1, 2)
            .addMobSpawn("monster", "minecraft:wither_skeleton", 6, 1, 1)
            .addMobSpawn("monster", "minecraft:ghast", 3, 1, 1)
            .addMobSpawn("creature", "minecraft:strider", 12, 1, 2)
            // MCDoom demons - volcanic nether-like world
            .addMCDoomDemon("imp", 15, 1, 3)
            .addMCDoomDemon("lost_soul", 10, 1, 2)
            .allowPeacefulMobs(false)
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
            .surfaceBlock("minecraft:snow_block")
            .subsurfaceBlock("minecraft:packed_ice")
            .deepBlock("minecraft:blue_ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 4 ores: frozen world with hidden deposits
            .configureOre("iron", 70)      // common
            .configureOre("coal", 65)      // common - ancient organic matter
            .configureOre("copper", 30)    // uncommon
            .configureOre("diamond", 12)    // rare
            // Sparse but dangerous ice creatures
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:stray", 30, 2, 3)
            .addMobSpawn("creature", "minecraft:polar_bear", 15, 1, 2)
            .addMobSpawn("monster", "minecraft:skeleton", 20, 1, 2)
            .allowPeacefulMobs(true)
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
            .generate();

        // ARENOS - Desert with twin suns
        // Hot desert, twin suns, sparse life
        registerPlanet("arenos")
            .vanillaQualityFlat()
            .coordinateShift(90000, 75000)
            .gravity(1.0f)
            .surfaceBlock("minecraft:sand")
            .subsurfaceBlock("minecraft:sandstone")
            .deepBlock("minecraft:smooth_sandstone")
            .defaultBlock("minecraft:sandstone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 4 ores: desert world with buried minerals
            .configureOre("copper", 80)    // common - desert deposits
            .configureOre("iron", 65)      // common
            .configureOre("gold", 30)      // uncommon - buried treasure
            .configureOre("diamond", 10)    // rare
            // Desert creatures + Reptilian mobs
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:husk", 35, 2, 4)
            .addMobSpawn("monster", "minecraft:spider", 20, 1, 2)
            .addMobSpawn("creature", "minecraft:rabbit", 15, 2, 3)
            // Reptilian mod - desert lizards thrive here
            .addReptilianCreature("lizard", 12, 1, 2)
            .addReptilianCreature("iguana", 10, 1, 2)
            .allowPeacefulMobs(true)
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
            .generate();

        // PALUDIS - Swamp world
        // Dense fog, murky swamps, lots of hostile life
        registerPlanet("paludis")
            .vanillaQualityFlat()
            .coordinateShift(30000, 90000)
            .gravity(0.9f)
            .surfaceBlock("minecraft:mud")
            .subsurfaceBlock("minecraft:muddy_mangrove_roots")
            .deepBlock("minecraft:clay")
            .defaultBlock("minecraft:clay")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(58)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: swamp world with organic deposits
            .configureOre("coal", 80)      // common - organic matter
            .configureOre("iron", 65)      // common
            .configureOre("copper", 35)    // uncommon
            .configureOre("gold", 12)       // rare - silt deposits
            // Swamp creatures + Ribbits frog civilization
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:slime", 12, 1, 2)
            .addMobSpawn("monster", "minecraft:witch", 6, 1, 1)
            .addMobSpawn("monster", "minecraft:drowned", 8, 1, 2)
            .addMobSpawn("monster", "minecraft:spider", 6, 1, 1)
            .addMobSpawn("monster", "minecraft:cave_spider", 5, 1, 1)
            .addMobSpawn("creature", "minecraft:frog", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:axolotl", 6, 1, 2)
            // Ribbits frog civilization - main inhabitants of the swamp world
            .addMobSpawn("monster", "ribbits:frog_warrior", 30, 2, 4)
            .addMobSpawn("monster", "ribbits:frog_archer", 25, 1, 3)
            .addMobSpawn("monster", "ribbits:frog_mage", 15, 1, 2)
            .addMobSpawn("creature", "ribbits:frog_villager", 20, 2, 4)
            .allowPeacefulMobs(true)
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
            // Enable Ribbits structures (villages, temples, etc.) for this swamp world
            .enableRibbitsStructures()
            .generate();

        // LUXORIA - Alien bioluminescent jungle
        // Toxic to humans, dense alien life, bioluminescent
        registerPlanet("luxoria")
            .vanillaQualityAlien()
            .coordinateShift(45000, 90000)
            .gravity(0.8f)
            .surfaceBlock("minecraft:moss_block")
            .subsurfaceBlock("minecraft:rooted_dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: bioluminescent world with unique gems
            .configureOre("lapis", 80)     // common - bioluminescent crystals
            .configureOre("emerald", 70)   // common - alien gems
            .configureOre("gold", 35)      // uncommon
            .configureOre("diamond", 15)    // rare
            // Alien life (reduced for performance)
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:glow_squid", 10, 1, 2)
            .addMobSpawn("creature", "minecraft:axolotl", 10, 1, 2)
            .addMobSpawn("creature", "minecraft:parrot", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:ocelot", 5, 1, 1)
            .addMobSpawn("monster", "minecraft:spider", 8, 1, 2)
            .addMobSpawn("monster", "minecraft:cave_spider", 6, 1, 1)
            .addMobSpawn("monster", "minecraft:phantom", 4, 1, 1)
            .allowPeacefulMobs(true)
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
            .generate();

        // ==================== ADDITIONAL UNIQUE WORLDS ====================

        // GLACIO - Icy world with thin atmosphere
        registerPlanet("glacio")
            .vanillaQualityStandard()
            .coordinateShift(60000, 90000)
            .gravity(0.8f)
            .surfaceBlock("minecraft:snow_block")
            .subsurfaceBlock("minecraft:ice")
            .deepBlock("minecraft:packed_ice")
            .defaultBlock("minecraft:packed_ice")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(50)
            .defaultFluid("minecraft:water")
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: frozen world
            .configureOre("iron", 70)      // common
            .configureOre("coal", 65)      // common - ancient organics
            .configureOre("copper", 35)    // uncommon
            .configureOre("diamond", 15)    // rare - ice preserves gems
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:stray", 30, 2, 3)
            .addMobSpawn("creature", "minecraft:polar_bear", 20, 1, 2)
            .addMobSpawn("monster", "minecraft:skeleton", 15, 1, 2)
            // Sparse kobold presence in ice caves
            .addKoboldsMobs(8)
            .allowPeacefulMobs(true)
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
            .generate();

        // VULCAN - Volcanic rocky world (more survivable volcanic world)
        registerPlanet("vulcan")
            .vanillaQualityCratered()
            .coordinateShift(75000, 90000)
            .gravity(1.2f)
            .surfaceBlock("minecraft:tuff")
            .subsurfaceBlock("minecraft:basalt")
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:basalt")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(30)
            .defaultFluid("minecraft:lava")
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // 4 ores: volcanic with rich mineral deposits
            .configureOre("iron", 80)      // common
            .configureOre("redstone", 70)  // common - thermal energy
            .configureOre("gold", 35)      // uncommon
            .configureOre("diamond", 15)    // rare
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:magma_cube", 40, 2, 4)
            .addMobSpawn("monster", "minecraft:zombified_piglin", 25, 2, 3)
            .addMobSpawn("creature", "minecraft:strider", 30, 1, 2)
            // MCDoom demons - volcanic rocky world
            .addMCDoomDemon("imp", 15, 1, 3)
            .addMCDoomDemon("lost_soul", 10, 1, 2)
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
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: balanced Earth-like colony world
            .configureOre("iron", 80)      // common
            .configureOre("coal", 70)      // common
            .configureOre("copper", 35)    // uncommon
            .configureOre("emerald", 12)    // rare
            // Earth-like life
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:cow", 25, 2, 4)
            .addMobSpawn("creature", "minecraft:pig", 25, 2, 4)
            .addMobSpawn("creature", "minecraft:sheep", 25, 2, 4)
            .addMobSpawn("creature", "minecraft:chicken", 30, 3, 5)
            .addMobSpawn("creature", "minecraft:horse", 10, 2, 4)
            .addMobSpawn("monster", "minecraft:zombie", 18, 2, 3)
            .addMobSpawn("monster", "minecraft:skeleton", 18, 2, 3)
            .addMobSpawn("monster", "minecraft:spider", 15, 1, 2)
            .addMobSpawn("monster", "minecraft:creeper", 10, 1, 1)
            .allowPeacefulMobs(true)
            .clearBiomes()
            .addBiome("minecraft:plains", 0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, "Terra Nova Plains")
            .addBiome("minecraft:forest", 0.5f, 0.6f, 0.1f, 0.0f, 0.0f, 0.0f, "Terra Nova Forests")
            .addBiome("minecraft:taiga", 0.3f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, "Terra Nova Taiga")
            .skyColor(0x87CEEB)
            .fogColor(0xB0C4DE)
            .hasAtmosphere(true)
            .breathableAtmosphere(true)
            .ambientLight(0.85f)
            .cloudsEnabled(true)
            .rainEnabled(true)
            .starsVisibleDuringDay(false)
            .addSun()
            .addCavePreset("balanced_vanilla")
            .generate();

        // ==================== MOWZIES MOBS PLANETS ====================

        // PRIMORDIUM - Dense jungle world with ancient creatures (Latin: origin/beginning)
        // Mowzie's Mobs jungle creatures (Foliaath, Naga, Ferrous Wroughtnaut)
        registerPlanet("primordium")
            .vanillaQualityStandard()
            .coordinateShift(105000, 90000)
            .gravity(1.0f)
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .defaultFluid("minecraft:water")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(63)
            .disableMobGeneration(false)
            .aquifersEnabled(true)
            .oreVeinsEnabled(true)
            // 4 ores: lush jungle world
            .configureOre("iron", 70)      // common
            .configureOre("coal", 65)      // common - organic matter
            .configureOre("copper", 35)    // uncommon
            .configureOre("emerald", 15)   // rare - jungle temple treasure
            // Mowzie's Mobs jungle creatures
            .clearAllMobSpawns()
            .addMowziesMobsPreset("jungle")
            .addMobSpawn("monster", "minecraft:spider", 20, 1, 2)
            .addMobSpawn("creature", "minecraft:parrot", 15, 2, 3)
            .addMobSpawn("creature", "minecraft:ocelot", 10, 1, 2)
            // Boss-tier mobs (very rare)
            .addMowziesMob("ferrous_wroughtnaut", 2, 1, 1)
            .addMowziesMob("naga", 5, 1, 1)
            .allowPeacefulMobs(true)
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
            .generate();

        // BELLATOR - Savanna world with Umvuthana civilization (Latin: warrior)
        // Mowzie's Mobs savanna creatures (Umvuthana, Barakoa, Chiefs)
        registerPlanet("bellator")
            .vanillaQualityStandard()
            .coordinateShift(105000, 105000)
            .gravity(1.0f)
            .surfaceBlock("minecraft:grass_block")
            .subsurfaceBlock("minecraft:dirt")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(50)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // Dry savanna
            .oreVeinsEnabled(true)
            // 4 ores: savanna world
            .configureOre("iron", 70)      // common
            .configureOre("copper", 65)    // common
            .configureOre("gold", 30)      // uncommon - tribal treasure
            .configureOre("diamond", 12)   // rare
            // Mowzie's Mobs Umvuthana civilization
            .clearAllMobSpawns()
            .addMowziesMobsPreset("savanna")
            .addMobSpawn("monster", "minecraft:husk", 15, 1, 2)
            .addMobSpawn("creature", "minecraft:horse", 20, 2, 4)
            .addMobSpawn("creature", "minecraft:llama", 15, 2, 4)
            // Boss-tier Umvuthi (very rare)
            .addMowziesMob("umvuthi", 2, 1, 1)
            .allowPeacefulMobs(true)
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
            .generate();

        // ==================== PROFUNDUS ====================

        // PROFUNDUS - Underground cave kingdom (Latin: the deep)
        // Cave-heavy terrain, dim lighting, kobold civilization
        registerPlanet("profundus")
            .vanillaQualityAlien()  // Alien terrain with deep caves
            .coordinateShift(120000, 120000)
            .gravity(0.85f)
            // Underground/cave aesthetic - stone and cobblestone surface
            .surfaceBlock("minecraft:cobblestone")
            .subsurfaceBlock("minecraft:stone")
            .deepBlock("minecraft:deepslate")
            .defaultBlock("minecraft:stone")
            .bedrockBlock("minecraft:bedrock")
            .seaLevel(0)
            .disableMobGeneration(false)
            .aquifersEnabled(false)  // No surface water
            .oreVeinsEnabled(true)
            // 4 ores: rich mining world for kobolds
            .configureOre("iron", 80)      // common - kobold smithing
            .configureOre("gold", 70)      // common - kobold hoarding
            .configureOre("copper", 55)    // uncommon
            .configureOre("emerald", 20)   // uncommon - kobold treasure
            // Kobold civilization - high spawn rates
            .clearAllMobSpawns()
            .addKoboldsMobs(50)            // Main kobold variants with high weight
            .addHostileKobolds(15)         // Hostile variants (zombies, skeletons, witherbolds)
            .addMobSpawn("monster", "minecraft:spider", 15, 1, 2)
            .addMobSpawn("monster", "minecraft:cave_spider", 20, 1, 2)
            .addMobSpawn("monster", "minecraft:silverfish", 10, 2, 4)
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
            .generate();
    }
}