package com.hecookin.adastramekanized.common.planets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Central planet generation system using proven TerraBlender approach.
 * Replaces StaticPlanetGenerator with builder pattern and Moon-based generation.
 */
public class PlanetMaker {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String RESOURCES_PATH = "src/main/resources/data/adastramekanized/";
    private static final List<PlanetBuilder> PLANETS = new ArrayList<>();

    /**
     * Start building a new planet with the given name
     */
    public static PlanetBuilder planet(String name) {
        return new PlanetBuilder(name);
    }

    /**
     * Generate all configured planets using runData integration
     */
    public static void generateAllPlanets() {
        AdAstraMekanized.LOGGER.info("Generating planets using PlanetMaker system...");

        createDirectories();

        for (PlanetBuilder planet : PLANETS) {
            try {
                generatePlanetFiles(planet);
                AdAstraMekanized.LOGGER.info("Generated planet: {}", planet.name);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate planet: {}", planet.name, e);
            }
        }

        AdAstraMekanized.LOGGER.info("Completed generating {} planets", PLANETS.size());

        AdAstraMekanized.LOGGER.info("Planet generation uses multi-noise biome sources for variety");
    }


    /**
     * Create multi-noise biome source for a planet
     */
    private static JsonObject createBiomePreset(PlanetBuilder planet) {
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:multi_noise");

        // Create biome entries for this planet
        JsonArray biomes = new JsonArray();

        // Use custom biomes if defined, otherwise use legacy hardcoded biomes
        if (!planet.customBiomes.isEmpty()) {
            // Use the dynamic biome system
            for (PlanetBuilder.BiomeEntry biomeEntry : planet.customBiomes) {
                // Handle biome naming properly
                String customBiomeName;
                if (biomeEntry.biomeName.startsWith("adastramekanized:")) {
                    // Already has our namespace, just use it directly
                    customBiomeName = biomeEntry.biomeName;
                } else if (biomeEntry.biomeName.startsWith("minecraft:")) {
                    // Use vanilla biomes directly without modification
                    customBiomeName = biomeEntry.biomeName;
                } else {
                    // Add our namespace and planet prefix for custom biomes
                    customBiomeName = "adastramekanized:" + planet.name + "_" + biomeEntry.biomeName;
                }

                biomes.add(createBiomeEntry(
                    customBiomeName,
                    biomeEntry.temperature,
                    biomeEntry.humidity,
                    biomeEntry.continentalness,
                    biomeEntry.erosion,
                    biomeEntry.depth,
                    biomeEntry.weirdness
                ));
            }
        } else {
            // Fallback to hardcoded biomes for legacy planets
            switch (planet.name.toLowerCase()) {
                case "moon":
                    // Moon biomes using existing cold/barren biomes
                    biomes.add(createBiomeEntry("minecraft:frozen_peaks",
                        -0.8f, -0.9f, 0.4f, 0.2f, 0.0f, 0.1f)); // Cold, dry, highlands (lunar highlands)
                    biomes.add(createBiomeEntry("minecraft:snowy_slopes",
                        -0.6f, -0.8f, -0.2f, 0.3f, -0.5f, -0.1f)); // Cold, dry, lowlands (lunar maria)
                    break;
                case "mars":
                    // Mars biomes using existing desert/cold biomes
                    biomes.add(createBiomeEntry("minecraft:badlands",
                        -0.2f, -0.7f, 0.6f, -0.1f, 0.5f, 0.0f)); // Cool, dry, elevated (mars highlands)
                    biomes.add(createBiomeEntry("minecraft:desert",
                        0.1f, -0.6f, 0.2f, 0.4f, -0.3f, -0.1f)); // Warmer, dry, valleys (mars valleys)
                    biomes.add(createBiomeEntry("minecraft:frozen_peaks",
                        -0.9f, -0.8f, 0.8f, 0.0f, 0.2f, 0.3f)); // Very cold, polar (mars polar)
                    break;
                default:
                    // Default single biome for other planets
                    biomes.add(createBiomeEntry("minecraft:plains",
                        0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
                    break;
            }
        }

        biomeSource.add("biomes", biomes);
        return biomeSource;
    }

    /**
     * Create a biome entry with climate parameters
     */
    private static JsonObject createBiomeEntry(String biome, float temperature, float humidity,
                                              float continentalness, float erosion, float depth, float weirdness) {
        JsonObject entry = new JsonObject();
        entry.addProperty("biome", biome);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("temperature", temperature);
        parameters.addProperty("humidity", humidity);
        parameters.addProperty("continentalness", continentalness);
        parameters.addProperty("erosion", erosion);
        parameters.addProperty("depth", depth);
        parameters.addProperty("weirdness", weirdness);
        parameters.addProperty("offset", 0.0f);

        entry.add("parameters", parameters);
        return entry;
    }

    /**
     * Fluent builder for planet configuration
     */
    public static class PlanetBuilder {
        private final String name;

        // Unique seed for this planet's terrain generation
        private long seed;

        // Noise configuration - Primary terrain shaping
        private float continentalScale = 1.0f;
        private float erosionScale = 0.25f;
        private float ridgeScale = 0.25f;
        private float heightVariation1 = 0.4f;
        private float heightVariation2 = 0.05f;
        private float heightVariation3 = 0.1f;
        private float heightVariation4 = 0.05f;

        // Advanced noise parameters
        private float temperatureScale = 1.0f;
        private float humidityScale = 1.0f;
        private float weirdnessScale = 1.0f;
        private float densityFactor = 1.0f;
        private float densityOffset = 0.0f;

        // Noise routing parameters
        private float barrierNoise = 0.0f;
        private float fluidLevelFloodedness = 0.0f;
        private float fluidLevelSpread = 0.0f;
        private float lavaNoise = 0.0f;
        private float temperatureNoise = 0.0f;
        private float vegetationNoise = 0.0f;

        // Hill and mountain generation parameters (based on Ad Astra research)
        private float jaggednessScale = 0.0f;              // Controls mountain peak sharpness
        private float jaggednessNoiseScale = 1500.0f;      // High-scale jagged noise for dramatic terrain
        private float depthFactor = 1.0f;                  // Terrain height scaling factor
        private float depthOffset = 0.0f;                  // Baseline terrain elevation offset
        private float terrainFactor = 1.0f;                // Overall terrain intensity multiplier
        private float base3DNoiseXZScale = 0.25f;          // Horizontal terrain frequency
        private float base3DNoiseYScale = 0.2f;            // Vertical terrain frequency
        private float base3DNoiseXZFactor = 80.0f;         // Horizontal terrain amplitude
        private float base3DNoiseYFactor = 90.0f;          // Vertical terrain amplitude
        private float smearScaleMultiplier = 8.0f;         // Terrain smoothing factor

        // Y-gradient parameters for vertical density
        // NOTE: Vanilla uses complex multi-gradient system, but PlanetMaker uses simplified single gradient
        // This gradient spans most of world height to provide vertical structure
        private int gradientFromY = -64;
        private int gradientToY = 256;  // Span most of world (not full 320 to avoid sky issues)
        private float gradientFromValue = 1.0f;  // Positive at bottom (solid)
        private float gradientToValue = -1.0f;   // Negative at top (air)
        private float gradientMultiplier = 0.64f; // Vanilla-like multiplier for reasonable terrain

        // Advanced terrain shaping
        private float initialDensityOffset = -0.234375f;
        private float terrainShapingFactor = 0.175f;
        private boolean legacyRandomSource = false;

        // Surface configuration
        private String surfaceBlock = "minecraft:stone";
        private String subsurfaceBlock = "minecraft:cobblestone";
        private String deepBlock = "minecraft:stone";
        private String underwaterBlock = "minecraft:stone";
        private String shallowUnderwaterBlock = "minecraft:stone";
        private String deepUnderwaterBlock = "minecraft:stone";
        private String bedrockBlock = "minecraft:bedrock";

        // Advanced surface controls
        private boolean enableCustomSurfaceRules = true;
        private boolean disableDefaultSurfaceGeneration = true;
        private boolean preventGrassGeneration = true;
        private boolean preventGravelGeneration = true;
        private boolean preventSandGeneration = false;

        // Basic planet properties
        private int seaLevel = 63;
        private boolean disableMobGeneration = false;
        private boolean aquifersEnabled = false;
        private boolean oreVeinsEnabled = true;
        private String defaultFluid = "minecraft:air";

        // Sun and spawning settings
        private boolean hasSkylight = true;  // Controls sun damage (false = no sun damage)
        private int monsterSpawnLightLevel = 7;  // Max light level for spawning (15 = spawn in daylight)
        private int monsterSpawnBlockLightLimit = 15;  // Block light limit

        // World height and structure
        private int minY = -64;
        private int worldHeight = 384;
        // VANILLA-ACCURATE: horizontal=1, vertical=2 (opposite of previous default!)
        private int horizontalNoiseSize = 1;
        private int verticalNoiseSize = 2;

        // Surface rules and generation
        private String defaultBlock = "minecraft:stone";
        private boolean hasAbovePreliminaryRule = true;
        private boolean hasWaterRule = true;
        private int surfaceDepthMultiplier = 0;
        private boolean addStoneDepth = false;

        // Vein generation controls
        private float veinToggle = 0.0f;
        private float veinRidged = 0.0f;
        private float veinGap = 0.0f;

        // Enhanced ore vein configuration
        private java.util.List<String> customOreVeins = new java.util.ArrayList<>();
        private java.util.Map<String, Integer> oreVeinCounts = new java.util.HashMap<>();  // Ore type -> veins per chunk
        private float oreVeinDensity = 1.0f;
        private float oreVeinSize = 1.0f;
        private int maxOreVeinCount = 20;
        private boolean enableRareOres = true;
        private boolean enableCommonOres = true;

        // Mob spawning configuration
        private java.util.Map<String, java.util.List<MobSpawnEntry>> mobSpawns = new java.util.HashMap<>();
        private java.util.Map<String, SpawnCost> spawnCosts = new java.util.HashMap<>();
        private java.util.Set<String> usedModNamespaces = new java.util.HashSet<>();  // Track mod namespaces for spawn control
        private boolean allowHostileMobs = true;
        private boolean allowPeacefulMobs = true;
        private boolean enableDeepslateOres = true;

        // Biome distribution controls
        private float biomeContinentalness = 0.0f;
        private float biomeErosion = 0.0f;
        private float biomeDepth = 0.0f;
        private float biomeWeirdness = 0.0f;
        private float biomeTemperature = 0.0f;
        private float biomeHumidity = 0.0f;

        // Dynamic biome system
        private java.util.List<BiomeEntry> customBiomes = new java.util.ArrayList<>();

        // Structure generation system
        private java.util.List<StructureEntry> customStructures = new java.util.ArrayList<>();
        private boolean enableVillages = false;
        private boolean enableStrongholds = false;
        private boolean enableMineshafts = false;
        private boolean enableDungeons = false;

        // Feature placement system (vegetation, rocks, etc.)
        private java.util.List<FeatureEntry> customFeatures = new java.util.ArrayList<>();
        private float treeFrequency = 0.0f;
        private float grassFrequency = 0.0f;
        private float flowerFrequency = 0.0f;
        private float rockFrequency = 0.0f;
        private boolean enableCrystals = false;
        private boolean enableGeysers = false;
        private boolean enableGlowLichen = false;

        // Liquid system configuration
        private String oceanFluid = "minecraft:water";
        private String lakeFluid = "minecraft:water";
        private int oceanLevel = -1; // -1 means use seaLevel
        private float oceanFrequency = 0.3f; // 0.0-1.0, controls ocean size
        private float lakeFrequency = 0.1f; // 0.0-1.0, controls lake generation
        private boolean enableUndergroundLiquids = false;
        private String undergroundLiquid = "minecraft:water";
        private int lavaLakeLevel = -60; // Y level for lava lakes
        private float lavaLakeFrequency = 0.05f;

        // Cave system configuration
        private float caveFrequency = 1.0f; // 0.0-2.0, base cave generation frequency
        private float caveSize = 1.0f; // 0.5-3.0, cave tunnel size multiplier
        private float caveYScale = 0.5f; // 0.1-2.0, vertical stretch of caves
        private float ravineFrequency = 0.1f; // 0.0-1.0, ravine generation chance
        private float ravineDepth = 3.0f; // 1.0-5.0, ravine depth multiplier
        private boolean enableCheeseCaves = true; // Large open cave systems
        private int caveMinY = -64;  // Minimum Y level for cave generation
        private int caveMaxY = 256;  // Maximum Y level for cave generation
        private boolean enableSpaghettiCaves = true; // Winding tunnel systems
        private boolean enableNoodleCaves = true; // Thin winding tunnels
        private String caveFluid = "minecraft:air"; // Fluid that fills caves
        private float caveFluidLevel = -64f; // Y level where caves fill with fluid
        private boolean enableLavaTubes = false; // Special lava tube generation
        private boolean enableCrystalCaves = false; // Crystal-lined cave systems
        private boolean enableIceCaves = false; // Ice-themed cave generation
        private java.util.List<CaveDecorationEntry> caveDecorations = new java.util.ArrayList<>();

        // Dimension properties
        private int skyColor = 0x78A7FF;
        private int fogColor = 0xC0D8FF;
        private boolean hasAtmosphere = true;
        private float ambientLight = 0.1f;

        // Planet physical properties
        private float gravity = 1.0f; // Gravity multiplier (1.0 = Earth gravity)

        // Celestial bodies configuration
        private java.util.List<SunConfig> suns = new java.util.ArrayList<>();
        private java.util.List<MoonConfig> moons = new java.util.ArrayList<>();
        private java.util.List<PlanetConfig> visiblePlanets = new java.util.ArrayList<>();

        // Star configuration
        private boolean starsVisibleDuringDay = false;
        private int starCount = 15000;
        private float starBrightness = 1.0f;

        // Weather configuration
        private boolean cloudsEnabled = true;
        private boolean rainEnabled = true;
        private boolean snowEnabled = true;
        private boolean stormsEnabled = false;
        private boolean acidRainDamage = false;
        private float acidRainDamageAmount = 1.0f;

        private PlanetBuilder(String name) {
            this.name = name;
            // Generate unique seed from planet name hash + constant offset for deterministic results
            this.seed = name.hashCode() + 1000000L; // Offset to avoid negative hash values being too small
        }

        // Noise parameter configuration methods
        public PlanetBuilder seed(long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Set the gravity multiplier for this planet
         * @param gravity Gravity multiplier (1.0 = Earth gravity, 0.166 = Moon, 0.38 = Mars)
         */
        public PlanetBuilder gravity(float gravity) {
            this.gravity = Math.max(0.01f, Math.min(10.0f, gravity)); // Clamp between 0.01 and 10.0
            return this;
        }

        public PlanetBuilder continentalScale(float scale) {
            this.continentalScale = scale;
            return this;
        }

        public PlanetBuilder erosionScale(float scale) {
            this.erosionScale = scale;
            return this;
        }

        public PlanetBuilder ridgeScale(float scale) {
            this.ridgeScale = scale;
            return this;
        }

        public PlanetBuilder heightVariation(float var1, float var2, float var3, float var4) {
            this.heightVariation1 = var1;
            this.heightVariation2 = var2;
            this.heightVariation3 = var3;
            this.heightVariation4 = var4;
            return this;
        }

        // Advanced noise parameter methods
        public PlanetBuilder temperatureScale(float scale) {
            this.temperatureScale = scale;
            return this;
        }

        public PlanetBuilder humidityScale(float scale) {
            this.humidityScale = scale;
            return this;
        }

        public PlanetBuilder weirdnessScale(float scale) {
            this.weirdnessScale = scale;
            return this;
        }

        public PlanetBuilder densityFactor(float factor) {
            this.densityFactor = factor;
            return this;
        }

        public PlanetBuilder densityOffset(float offset) {
            this.densityOffset = offset;
            return this;
        }

        // Noise routing methods
        public PlanetBuilder barrierNoise(float noise) {
            this.barrierNoise = noise;
            return this;
        }

        public PlanetBuilder fluidLevelFloodedness(float floodedness) {
            this.fluidLevelFloodedness = floodedness;
            return this;
        }

        public PlanetBuilder fluidLevelSpread(float spread) {
            this.fluidLevelSpread = spread;
            return this;
        }

        public PlanetBuilder lavaNoise(float noise) {
            this.lavaNoise = noise;
            return this;
        }

        public PlanetBuilder temperatureNoise(float noise) {
            this.temperatureNoise = noise;
            return this;
        }

        public PlanetBuilder vegetationNoise(float noise) {
            this.vegetationNoise = noise;
            return this;
        }

        // Hill and mountain generation methods (based on Ad Astra research)
        public PlanetBuilder jaggednessScale(float scale) {
            this.jaggednessScale = scale;
            return this;
        }

        public PlanetBuilder jaggednessNoiseScale(float scale) {
            this.jaggednessNoiseScale = scale;
            return this;
        }

        public PlanetBuilder depthFactor(float factor) {
            this.depthFactor = factor;
            return this;
        }

        public PlanetBuilder depthOffset(float offset) {
            this.depthOffset = offset;
            return this;
        }

        public PlanetBuilder terrainFactor(float factor) {
            this.terrainFactor = factor;
            return this;
        }

        public PlanetBuilder base3DNoiseScale(float xzScale, float yScale) {
            this.base3DNoiseXZScale = xzScale;
            this.base3DNoiseYScale = yScale;
            return this;
        }

        public PlanetBuilder base3DNoiseFactor(float xzFactor, float yFactor) {
            this.base3DNoiseXZFactor = xzFactor;
            this.base3DNoiseYFactor = yFactor;
            return this;
        }

        public PlanetBuilder smearScaleMultiplier(float multiplier) {
            this.smearScaleMultiplier = multiplier;
            return this;
        }

        // Y-gradient configuration methods
        public PlanetBuilder verticalGradient(int fromY, int toY, float fromValue, float toValue) {
            this.gradientFromY = fromY;
            this.gradientToY = toY;
            this.gradientFromValue = fromValue;
            this.gradientToValue = toValue;
            return this;
        }

        public PlanetBuilder gradientMultiplier(float multiplier) {
            this.gradientMultiplier = multiplier;
            return this;
        }

        // Advanced terrain shaping methods
        public PlanetBuilder initialDensityOffset(float offset) {
            this.initialDensityOffset = offset;
            return this;
        }

        public PlanetBuilder terrainShapingFactor(float factor) {
            this.terrainShapingFactor = factor;
            return this;
        }

        public PlanetBuilder legacyRandomSource(boolean legacy) {
            this.legacyRandomSource = legacy;
            return this;
        }

        // Surface configuration methods
        public PlanetBuilder surfaceBlock(String block) {
            this.surfaceBlock = block;
            return this;
        }

        public PlanetBuilder subsurfaceBlock(String block) {
            this.subsurfaceBlock = block;
            return this;
        }

        public PlanetBuilder deepBlock(String block) {
            this.deepBlock = block;
            return this;
        }

        // Additional surface block configuration
        public PlanetBuilder underwaterBlock(String block) {
            this.underwaterBlock = block;
            return this;
        }

        public PlanetBuilder shallowUnderwaterBlock(String block) {
            this.shallowUnderwaterBlock = block;
            return this;
        }

        public PlanetBuilder deepUnderwaterBlock(String block) {
            this.deepUnderwaterBlock = block;
            return this;
        }

        public PlanetBuilder bedrockBlock(String block) {
            this.bedrockBlock = block;
            return this;
        }

        // Advanced surface generation controls
        public PlanetBuilder preventGrassGeneration(boolean prevent) {
            this.preventGrassGeneration = prevent;
            return this;
        }

        public PlanetBuilder preventGravelGeneration(boolean prevent) {
            this.preventGravelGeneration = prevent;
            return this;
        }

        public PlanetBuilder preventSandGeneration(boolean prevent) {
            this.preventSandGeneration = prevent;
            return this;
        }

        public PlanetBuilder disableDefaultSurfaceGeneration(boolean disable) {
            this.disableDefaultSurfaceGeneration = disable;
            return this;
        }

        // Enhanced ore vein configuration methods
        public PlanetBuilder addCustomOreVein(String oreBlock) {
            this.customOreVeins.add(oreBlock);
            return this;
        }

        /**
         * Configure specific ore type with veins per chunk
         * @param oreType The ore type (e.g., "iron", "diamond", "copper")
         * @param veinsPerChunk Number of veins to generate per chunk
         */
        public PlanetBuilder configureOre(String oreType, int veinsPerChunk) {
            this.oreVeinCounts.put(oreType, veinsPerChunk);
            return this;
        }

        public PlanetBuilder oreVeinDensity(float density) {
            this.oreVeinDensity = density;
            return this;
        }

        public PlanetBuilder oreVeinSize(float size) {
            this.oreVeinSize = size;
            return this;
        }

        public PlanetBuilder maxOreVeinCount(int count) {
            this.maxOreVeinCount = count;
            return this;
        }

        public PlanetBuilder enableRareOres(boolean enable) {
            this.enableRareOres = enable;
            return this;
        }

        public PlanetBuilder enableCommonOres(boolean enable) {
            this.enableCommonOres = enable;
            return this;
        }

        public PlanetBuilder enableDeepslateOres(boolean enable) {
            this.enableDeepslateOres = enable;
            return this;
        }

        // Basic properties
        public PlanetBuilder seaLevel(int level) {
            this.seaLevel = level;
            return this;
        }

        public PlanetBuilder disableMobGeneration(boolean disable) {
            this.disableMobGeneration = disable;
            return this;
        }

        /**
         * Control skylight and sun damage
         * @param hasSkylight false = no sun damage, monsters ignore daylight
         */
        public PlanetBuilder hasSkylight(boolean hasSkylight) {
            this.hasSkylight = hasSkylight;
            return this;
        }

        /**
         * Set monster spawn light level
         * @param maxLightLevel 0-15, where 15 allows spawning in full daylight
         */
        public PlanetBuilder monsterSpawnLightLevel(int maxLightLevel) {
            this.monsterSpawnLightLevel = Math.max(0, Math.min(15, maxLightLevel));
            return this;
        }

        // Mob spawning configuration methods

        /**
         * Add a mob spawn with weight (chance) and group size
         * @param category Spawn category: "monster", "creature", "ambient", "water_creature", "water_ambient", "misc"
         * @param mobId Mob resource location (e.g., "minecraft:zombie")
         * @param weight Spawn weight (higher = more common, typically 1-100)
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addMobSpawn(String category, String mobId, int weight, int minGroup, int maxGroup) {
            mobSpawns.computeIfAbsent(category, k -> new java.util.ArrayList<>())
                    .add(new MobSpawnEntry(mobId, weight, minGroup, maxGroup));

            // Track mod namespace for spawn control (if not minecraft)
            if (mobId.contains(":")) {
                String namespace = mobId.substring(0, mobId.indexOf(":"));
                if (!namespace.equals("minecraft")) {
                    usedModNamespaces.add(namespace);
                }
            }

            return this;
        }

        /**
         * Add an equipped mob spawn with armor and weapons
         * NOTE: This requires custom spawn handling through events or biome modifiers
         * @param category Spawn category: "monster", "creature", etc.
         * @param mobId Mob resource location (e.g., "minecraft:zombie")
         * @param weight Spawn weight (higher = more common)
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         * @param equipment Equipment configuration (helmet, chestplate, leggings, boots, mainhand, offhand)
         */
        public PlanetBuilder addEquippedMobSpawn(String category, String mobId, int weight, int minGroup, int maxGroup,
                                                   String helmet, String chestplate, String leggings, String boots,
                                                   String mainHand, String offHand) {
            // Add the base spawn entry
            MobSpawnEntry entry = new MobSpawnEntry(mobId, weight, minGroup, maxGroup);

            // Store equipment data for later processing
            // This will need to be handled by a custom biome modifier or spawn event
            entry.equipment = new java.util.HashMap<>();
            if (helmet != null) entry.equipment.put("helmet", helmet);
            if (chestplate != null) entry.equipment.put("chestplate", chestplate);
            if (leggings != null) entry.equipment.put("leggings", leggings);
            if (boots != null) entry.equipment.put("boots", boots);
            if (mainHand != null) entry.equipment.put("mainhand", mainHand);
            if (offHand != null) entry.equipment.put("offhand", offHand);

            mobSpawns.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(entry);
            return this;
        }

        /**
         * Add a mob spawn with percentage chance (converted to weight)
         * @param category Spawn category
         * @param mobId Mob resource location
         * @param spawnChance Spawn chance as percentage (0.0 - 100.0)
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addMobSpawnPercentage(String category, String mobId, double spawnChance, int minGroup, int maxGroup) {
            // Convert percentage to weight (100% = weight of 100)
            int weight = (int)(spawnChance);
            return addMobSpawn(category, mobId, weight, minGroup, maxGroup);
        }

        /**
         * Add multiple mob spawns at once
         * @param category Spawn category
         * @param spawns Array of [mobId, weight, minGroup, maxGroup] arrays
         */
        public PlanetBuilder addMobSpawns(String category, Object[][] spawns) {
            for (Object[] spawn : spawns) {
                String mobId = (String) spawn[0];
                int weight = (int) spawn[1];
                int minGroup = (int) spawn[2];
                int maxGroup = (int) spawn[3];
                addMobSpawn(category, mobId, weight, minGroup, maxGroup);
            }
            return this;
        }

        /**
         * Add standard hostile mobs for a planet type
         */
        public PlanetBuilder addHostileMobPreset(String preset) {
            switch (preset) {
                case "overworld":
                    addMobSpawn("monster", "minecraft:zombie", 95, 4, 4);
                    addMobSpawn("monster", "minecraft:skeleton", 100, 4, 4);
                    addMobSpawn("monster", "minecraft:spider", 100, 4, 4);
                    addMobSpawn("monster", "minecraft:creeper", 100, 4, 4);
                    addMobSpawn("monster", "minecraft:enderman", 10, 1, 4);
                    addMobSpawn("monster", "minecraft:witch", 5, 1, 1);
                    break;
                case "nether":
                    addMobSpawn("monster", "minecraft:zombified_piglin", 100, 4, 4);
                    addMobSpawn("monster", "minecraft:magma_cube", 100, 4, 4);
                    addMobSpawn("monster", "minecraft:piglin", 15, 4, 4);
                    break;
                case "alien":
                    // Custom alien mobs (when we add them)
                    addMobSpawn("monster", "minecraft:enderman", 50, 1, 3);
                    addMobSpawn("monster", "minecraft:phantom", 20, 1, 2);
                    addMobSpawn("monster", "minecraft:vex", 10, 2, 4);
                    break;
                case "barren":
                    // Very few mobs on barren worlds
                    addMobSpawn("monster", "minecraft:husk", 20, 1, 2);
                    addMobSpawn("monster", "minecraft:stray", 20, 1, 2);
                    break;
            }
            return this;
        }

        /**
         * Add passive mobs for a planet type
         */
        public PlanetBuilder addPassiveMobPreset(String preset) {
            switch (preset) {
                case "overworld":
                    addMobSpawn("creature", "minecraft:sheep", 12, 4, 4);
                    addMobSpawn("creature", "minecraft:pig", 10, 4, 4);
                    addMobSpawn("creature", "minecraft:chicken", 10, 4, 4);
                    addMobSpawn("creature", "minecraft:cow", 8, 4, 4);
                    addMobSpawn("ambient", "minecraft:bat", 10, 8, 8);
                    break;
                case "alien":
                    addMobSpawn("creature", "minecraft:strider", 60, 1, 2);
                    addMobSpawn("ambient", "minecraft:bat", 20, 4, 6);
                    break;
                case "aquatic":
                    addMobSpawn("water_creature", "minecraft:squid", 10, 4, 4);
                    addMobSpawn("water_ambient", "minecraft:cod", 15, 3, 6);
                    break;
            }
            return this;
        }

        /**
         * Add Mowzie's Mobs preset spawns for different planet themes
         * REQUIRES: Mowzie's Mobs installed and spawn_rate set to 0 in config
         */
        public PlanetBuilder addMowziesMobsPreset(String preset) {
            switch (preset) {
                case "jungle":
                    // Jungle world with Foliaaths
                    addMobSpawn("monster", "mowziesmobs:foliaath", 70, 1, 4);
                    addMobSpawn("monster", "mowziesmobs:baby_foliaath", 40, 2, 3);
                    break;
                case "savanna":
                    // Savanna world with Umvuthana tribes
                    addMobSpawn("monster", "mowziesmobs:umvuthana_raptor", 50, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:umvuthana_crane", 30, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:umvuthana", 40, 1, 2);
                    break;
                case "cave":
                    // Underground world with Grottols
                    addMobSpawn("monster", "mowziesmobs:grottol", 60, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:bluff", 40, 2, 3);
                    break;
                case "mystical":
                    // Mystical forest with Lanterns
                    addMobSpawn("ambient", "mowziesmobs:lantern", 50, 2, 4);
                    break;
                case "coastal":
                    // Coastal/beach world with Nagas
                    addMobSpawn("monster", "mowziesmobs:naga", 60, 1, 2);
                    break;
                case "frozen":
                    // Ice world with Frostmaw (rare boss spawn)
                    addMobSpawn("monster", "mowziesmobs:frostmaw", 1, 1, 1); // Very rare
                    break;
                case "industrial":
                    // Industrial/mechanical world with Wroughtnaut (rare boss spawn)
                    addMobSpawn("monster", "mowziesmobs:ferrous_wroughtnaut", 1, 1, 1); // Very rare
                    break;
                case "mixed":
                    // Mixed biome world with variety of Mowzie's mobs
                    addMobSpawn("monster", "mowziesmobs:foliaath", 30, 1, 2);
                    addMobSpawn("monster", "mowziesmobs:grottol", 20, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:naga", 25, 1, 2);
                    addMobSpawn("ambient", "mowziesmobs:lantern", 30, 2, 4);
                    addMobSpawn("monster", "mowziesmobs:bluff", 15, 2, 3);
                    break;
                case "boss_realm":
                    // Boss-focused dimension (use sparingly!)
                    addMobSpawn("monster", "mowziesmobs:frostmaw", 5, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:ferrous_wroughtnaut", 5, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:umvuthi", 3, 1, 1);
                    addMobSpawn("monster", "mowziesmobs:sculptor", 3, 1, 1);
                    break;
            }
            return this;
        }

        /**
         * Add individual Mowzie's Mobs spawns with full control
         * @param mobName Name from MowziesMobsIntegration (e.g., "foliaath", "naga")
         * @param weight Spawn weight (1-100, higher = more common)
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addMowziesMob(String mobName, int weight, int minGroup, int maxGroup) {
            String mobId = "mowziesmobs:" + mobName.toLowerCase();
            return addMobSpawn("monster", mobId, weight, minGroup, maxGroup);
        }

        /**
         * Enable Kobolds structure generation (kobold dens) for this planet.
         * This will allow kobold_den and kobold_den_pirate structures to generate.
         * Note: Kobolds naturally spawn in forests/taigas at Y=12-32, so ensure your
         * planet has appropriate biomes if you want structures to actually generate.
         */
        public PlanetBuilder enableKoboldsStructures() {
            com.hecookin.adastramekanized.common.events.ModdedStructureController
                .whitelistModStructures(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        AdAstraMekanized.MOD_ID, this.name),
                    "kobolds");
            return this;
        }

        /**
         * Add Kobolds mobs to this planet with default spawning.
         * Adds kobold, kobold_warrior, kobold_enchanter, kobold_engineer variants.
         * @param weight Base spawn weight (recommended: 20-40)
         */
        public PlanetBuilder addKoboldsMobs(int weight) {
            addMobSpawn("monster", "kobolds:kobold", weight, 2, 4);
            addMobSpawn("monster", "kobolds:kobold_warrior", weight / 2, 1, 2);
            addMobSpawn("monster", "kobolds:kobold_enchanter", weight / 3, 1, 1);
            addMobSpawn("monster", "kobolds:kobold_engineer", weight / 2, 1, 2);
            return this;
        }

        /**
         * Add hostile kobold variants (zombies, skeletons, witherbolds).
         * @param weight Base spawn weight (recommended: 10-20)
         */
        public PlanetBuilder addHostileKobolds(int weight) {
            addMobSpawn("monster", "kobolds:kobold_zombie", weight, 2, 4);
            addMobSpawn("monster", "kobolds:kobold_skeleton", weight, 2, 4);
            addMobSpawn("monster", "kobolds:witherbold", weight / 3, 1, 2);
            return this;
        }

        /**
         * Enable Ribbits structure generation (swamp villages) for this planet.
         * This will allow ribbit swamp villages to generate in swamp biomes.
         * Note: Ribbits villages spawn in swamp and mangrove swamp biomes.
         */
        public PlanetBuilder enableRibbitsStructures() {
            com.hecookin.adastramekanized.common.events.ModdedStructureController
                .whitelistModStructures(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        AdAstraMekanized.MOD_ID, this.name),
                    "ribbits");
            return this;
        }

        /**
         * Add Ribbits mobs (frog villagers) to this planet.
         * Ribbits are peaceful creature mobs that live in swamp villages.
         * @param weight Base spawn weight (recommended: 40-60 for dedicated planet)
         */
        public PlanetBuilder addRibbitsMobs(int weight) {
            // Ribbits spawn as passive creatures (they're villagers, not monsters)
            addMobSpawn("creature", "ribbits:ribbit", weight, 2, 5);
            return this;
        }

        /**
         * Add MCDoom demons to this planet.
         * @param preset Choose from: "fodder", "heavy", "super_heavy", "boss", "nether", "end"
         */
        public PlanetBuilder addMCDoomPreset(String preset) {
            switch (preset.toLowerCase()) {
                case "fodder":
                    // Weak demons
                    addMobSpawn("monster", "doom:imp", 20, 2, 4);
                    addMobSpawn("monster", "doom:zombieman", 15, 2, 3);
                    addMobSpawn("monster", "doom:shotgunguy", 10, 1, 2);
                    break;
                case "heavy":
                    // Medium demons
                    addMobSpawn("monster", "doom:pinky", 15, 1, 2);
                    addMobSpawn("monster", "doom:cacodemon", 10, 1, 2);
                    addMobSpawn("monster", "doom:revenant", 8, 1, 2);
                    addMobSpawn("monster", "doom:mancubus", 6, 1, 1);
                    break;
                case "super_heavy":
                    // Strong demons
                    addMobSpawn("monster", "doom:baron", 8, 1, 2);
                    addMobSpawn("monster", "doom:archvile", 4, 1, 1);
                    addMobSpawn("monster", "doom:marauder", 3, 1, 1);
                    break;
                case "boss":
                    // Boss demons (very rare)
                    addMobSpawn("monster", "doom:cyberdemon", 1, 1, 1);
                    addMobSpawn("monster", "doom:spidermastermind", 1, 1, 1);
                    break;
                case "nether":
                    // Nether-themed demons
                    addMobSpawn("monster", "doom:imp", 20, 2, 4);
                    addMobSpawn("monster", "doom:baron", 10, 1, 2);
                    addMobSpawn("monster", "doom:mancubus", 8, 1, 1);
                    addMobSpawn("monster", "doom:lost_soul", 25, 3, 6);
                    break;
                case "end":
                    // End-themed demons (Maykr entities)
                    addMobSpawn("monster", "doom:maykr_drone", 20, 2, 4);
                    addMobSpawn("monster", "doom:blood_maykr", 10, 1, 2);
                    addMobSpawn("monster", "doom:arch_maykr", 2, 1, 1);
                    break;
                default:
                    AdAstraMekanized.LOGGER.warn("Unknown MCDoom preset: {}", preset);
            }
            return this;
        }

        /**
         * Add individual MCDoom demon.
         * @param mobName Demon name (e.g., "imp", "baron", "cyberdemon")
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addMCDoomDemon(String mobName, int weight, int minGroup, int maxGroup) {
            String mobId = "doom:" + mobName.toLowerCase();
            return addMobSpawn("monster", mobId, weight, minGroup, maxGroup);
        }

        /**
         * Add generic modded mob spawn.
         * Use this for any mod that isn't specifically supported with helper methods.
         * @param modNamespace The mod namespace (e.g., "mobs_of_mythology")
         * @param mobName The mob name without namespace (e.g., "cyclops")
         * @param category Spawn category (usually "monster" or "creature")
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addModdedMob(String modNamespace, String mobName, String category, int weight, int minGroup, int maxGroup) {
            String mobId = modNamespace + ":" + mobName.toLowerCase();
            return addMobSpawn(category, mobId, weight, minGroup, maxGroup);
        }

        /**
         * Add Mobs of Mythology creature.
         * Namespace: mobs_of_mythology
         * @param mobName Name like "cyclops", "minotaur", "harpy", etc.
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addMythologyMob(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("mobs_of_mythology", mobName, "monster", weight, minGroup, maxGroup);
        }

        /**
         * Add Luminous World creature.
         * Namespace: luminousworld
         * @param mobName Name of luminous creature
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addLuminousMob(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("luminousworld", mobName, "creature", weight, minGroup, maxGroup);
        }

        /**
         * Add Undead Revamp creature.
         * Namespace: undead_revamp2
         * @param mobName Name of undead variant
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addUndeadRevampMob(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("undead_revamp2", mobName, "monster", weight, minGroup, maxGroup);
        }

        /**
         * Add Rotten Creatures mob.
         * Namespace: rottencreatures
         * @param mobName Name like "burned", "frostbitten", "swampy", "mummy", etc.
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addRottenCreature(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("rottencreatures", mobName, "monster", weight, minGroup, maxGroup);
        }

        /**
         * Add Prehistoric Expansion mod creature to this planet.
         * Namespace: shineals_prehistoric_expansion
         * @param mobName Name like "tyrannosaurus", "velociraptor", "triceratops", etc.
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addPrehistoricCreature(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("shineals_prehistoric_expansion", mobName, "creature", weight, minGroup, maxGroup);
        }

        /**
         * Add Reptilian mod creature to this planet.
         * Namespace: reptilian
         * @param mobName Name like "lizard", "chameleon", "iguana", etc.
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addReptilianCreature(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("reptilian", mobName, "creature", weight, minGroup, maxGroup);
        }

        /**
         * Set spawn cost for a mob (affects spawn cap)
         */
        public PlanetBuilder setSpawnCost(String mobId, double energyBudget, double charge) {
            spawnCosts.put(mobId, new SpawnCost(energyBudget, charge));
            return this;
        }

        /**
         * Clear all mob spawns for a category
         */
        public PlanetBuilder clearMobSpawns(String category) {
            mobSpawns.remove(category);
            return this;
        }

        /**
         * Clear all mob spawns
         */
        public PlanetBuilder clearAllMobSpawns() {
            mobSpawns.clear();
            return this;
        }

        /**
         * Set whether hostile mobs can spawn
         */
        public PlanetBuilder allowHostileMobs(boolean allow) {
            this.allowHostileMobs = allow;
            if (!allow) {
                clearMobSpawns("monster");
            }
            return this;
        }

        /**
         * Set whether peaceful mobs can spawn
         */
        public PlanetBuilder allowPeacefulMobs(boolean allow) {
            this.allowPeacefulMobs = allow;
            if (!allow) {
                clearMobSpawns("creature");
                clearMobSpawns("ambient");
            }
            return this;
        }

        public PlanetBuilder aquifersEnabled(boolean enabled) {
            this.aquifersEnabled = enabled;
            return this;
        }

        public PlanetBuilder oreVeinsEnabled(boolean enabled) {
            this.oreVeinsEnabled = enabled;
            return this;
        }

        public PlanetBuilder defaultFluid(String fluid) {
            this.defaultFluid = fluid;
            return this;
        }

        // Visual properties
        public PlanetBuilder skyColor(int color) {
            this.skyColor = color;
            return this;
        }

        public PlanetBuilder fogColor(int color) {
            this.fogColor = color;
            return this;
        }

        public PlanetBuilder hasAtmosphere(boolean atmosphere) {
            this.hasAtmosphere = atmosphere;
            return this;
        }

        public PlanetBuilder ambientLight(float light) {
            this.ambientLight = light;
            return this;
        }

        // Celestial bodies configuration methods

        /**
         * Add a sun to this planet's sky
         * @param texture Sun texture resource location
         * @param scale Sun size scale (1.0 = vanilla sun size)
         * @param color Sun color tint (RGB hex, 0xFFFFFF = white)
         * @param visible Whether the sun is visible
         */
        public PlanetBuilder addSun(net.minecraft.resources.ResourceLocation texture, float scale, int color, boolean visible) {
            this.suns.add(new SunConfig(texture, scale, color, visible));
            return this;
        }

        /**
         * Add a visible sun with default visibility (true)
         */
        public PlanetBuilder addSun(net.minecraft.resources.ResourceLocation texture, float scale, int color) {
            return addSun(texture, scale, color, true);
        }

        /**
         * Add a vanilla sun (default texture, white color, scale 1.0)
         */
        public PlanetBuilder addSun() {
            return addSun(net.minecraft.resources.ResourceLocation.parse("minecraft:textures/environment/sun.png"), 1.0f, 0xFFFFFF, true);
        }

        /**
         * Add a moon to this planet's sky
         * @param texture Moon texture resource location
         * @param scale Moon size scale
         * @param color Moon color tint (RGB hex)
         * @param horizontalPosition Horizontal position in sky (-1.0 to 1.0)
         * @param verticalPosition Vertical position in sky (-1.0 to 1.0, 0.0 = horizon)
         * @param movesWithTime Whether moon moves across sky with time
         * @param visible Whether moon is visible
         */
        public PlanetBuilder addMoon(net.minecraft.resources.ResourceLocation texture, float scale, int color,
                                    float horizontalPosition, float verticalPosition, boolean movesWithTime, boolean visible) {
            this.moons.add(new MoonConfig(texture, scale, color, horizontalPosition, verticalPosition, movesWithTime, visible));
            return this;
        }

        /**
         * Add a visible moon with default parameters
         */
        public PlanetBuilder addMoon(net.minecraft.resources.ResourceLocation texture, float scale, int color,
                                    float horizontalPosition, float verticalPosition, boolean movesWithTime) {
            return addMoon(texture, scale, color, horizontalPosition, verticalPosition, movesWithTime, true);
        }

        /**
         * Add a visible planet (like Earth from Moon) to this planet's sky
         * @param texture Planet texture resource location
         * @param scale Planet size scale
         * @param color Planet color tint (RGB hex)
         * @param horizontalPosition Horizontal position in sky (-1.0 to 1.0)
         * @param verticalPosition Vertical position in sky (-1.0 to 1.0, 0.0 = horizon)
         * @param movesWithTime Whether planet moves across sky with time
         * @param visible Whether planet is visible
         */
        public PlanetBuilder addVisiblePlanet(net.minecraft.resources.ResourceLocation texture, float scale, int color,
                                             float horizontalPosition, float verticalPosition, boolean movesWithTime, boolean visible) {
            this.visiblePlanets.add(new PlanetConfig(texture, scale, color, horizontalPosition, verticalPosition, movesWithTime, visible));
            return this;
        }

        /**
         * Add a visible planet with default parameters
         */
        public PlanetBuilder addVisiblePlanet(net.minecraft.resources.ResourceLocation texture, float scale, int color,
                                             float horizontalPosition, float verticalPosition, boolean movesWithTime) {
            return addVisiblePlanet(texture, scale, color, horizontalPosition, verticalPosition, movesWithTime, true);
        }

        // Star configuration methods

        /**
         * Set whether stars are visible during the day
         */
        public PlanetBuilder starsVisibleDuringDay(boolean visible) {
            this.starsVisibleDuringDay = visible;
            return this;
        }

        /**
         * Set the number of stars to render
         */
        public PlanetBuilder starCount(int count) {
            this.starCount = Math.max(0, count);
            return this;
        }

        /**
         * Set star brightness multiplier
         */
        public PlanetBuilder starBrightness(float brightness) {
            this.starBrightness = Math.max(0.0f, brightness);
            return this;
        }

        // Weather configuration methods

        /**
         * Enable or disable clouds
         */
        public PlanetBuilder cloudsEnabled(boolean enabled) {
            this.cloudsEnabled = enabled;
            return this;
        }

        /**
         * Enable or disable rain
         */
        public PlanetBuilder rainEnabled(boolean enabled) {
            this.rainEnabled = enabled;
            return this;
        }

        /**
         * Enable or disable snow
         */
        public PlanetBuilder snowEnabled(boolean enabled) {
            this.snowEnabled = enabled;
            return this;
        }

        /**
         * Enable or disable storms
         */
        public PlanetBuilder stormsEnabled(boolean enabled) {
            this.stormsEnabled = enabled;
            return this;
        }

        /**
         * Enable acid rain damage on this planet
         */
        public PlanetBuilder acidRainDamage(boolean damage) {
            this.acidRainDamage = damage;
            return this;
        }

        /**
         * Set acid rain damage amount per tick
         */
        public PlanetBuilder acidRainDamageAmount(float damage) {
            this.acidRainDamageAmount = Math.max(0.0f, damage);
            return this;
        }

        // World structure configuration methods
        public PlanetBuilder worldDimensions(int minY, int height) {
            this.minY = minY;
            this.worldHeight = height;
            return this;
        }

        public PlanetBuilder noiseSize(int horizontal, int vertical) {
            this.horizontalNoiseSize = horizontal;
            this.verticalNoiseSize = vertical;
            return this;
        }

        // Surface rule configuration methods
        public PlanetBuilder defaultBlock(String block) {
            this.defaultBlock = block;
            return this;
        }

        public PlanetBuilder abovePreliminaryRule(boolean enabled) {
            this.hasAbovePreliminaryRule = enabled;
            return this;
        }

        public PlanetBuilder waterRule(boolean enabled) {
            this.hasWaterRule = enabled;
            return this;
        }

        public PlanetBuilder surfaceDepthMultiplier(int multiplier) {
            this.surfaceDepthMultiplier = multiplier;
            return this;
        }

        public PlanetBuilder addStoneDepth(boolean add) {
            this.addStoneDepth = add;
            return this;
        }

        // Vein generation control methods
        public PlanetBuilder veinToggle(float toggle) {
            this.veinToggle = toggle;
            return this;
        }

        public PlanetBuilder veinRidged(float ridged) {
            this.veinRidged = ridged;
            return this;
        }

        public PlanetBuilder veinGap(float gap) {
            this.veinGap = gap;
            return this;
        }

        // Biome distribution control methods
        public PlanetBuilder biomeDistribution(float continentalness, float erosion, float depth, float weirdness, float temperature, float humidity) {
            this.biomeContinentalness = continentalness;
            this.biomeErosion = erosion;
            this.biomeDepth = depth;
            this.biomeWeirdness = weirdness;
            this.biomeTemperature = temperature;
            this.biomeHumidity = humidity;
            return this;
        }

        /**
         * Add a biome to this planet with custom climate parameters
         * @param biomeName Full biome resource location (e.g., "minecraft:plains")
         * @param temperature Temperature parameter (-1.0 to 1.0)
         * @param humidity Humidity parameter (-1.0 to 1.0)
         * @param continentalness Continental parameter (-1.0 to 1.0)
         * @param erosion Erosion parameter (-1.0 to 1.0)
         * @param depth Depth parameter (-1.0 to 1.0)
         * @param weirdness Weirdness parameter (-1.0 to 1.0)
         */
        public PlanetBuilder addBiome(String biomeName, float temperature, float humidity,
                                     float continentalness, float erosion, float depth, float weirdness) {
            return addBiome(biomeName, temperature, humidity, continentalness, erosion, depth, weirdness, null);
        }

        /**
         * Add a biome to this planet with custom climate parameters and translation
         * @param biomeName Full biome resource location (e.g., "minecraft:plains")
         * @param temperature Temperature parameter (-1.0 to 1.0)
         * @param humidity Humidity parameter (-1.0 to 1.0)
         * @param continentalness Continental parameter (-1.0 to 1.0)
         * @param erosion Erosion parameter (-1.0 to 1.0)
         * @param depth Depth parameter (-1.0 to 1.0)
         * @param weirdness Weirdness parameter (-1.0 to 1.0)
         * @param translation English translation for the biome (e.g., "Lunar Plains")
         */
        public PlanetBuilder addBiome(String biomeName, float temperature, float humidity,
                                     float continentalness, float erosion, float depth, float weirdness, String translation) {
            this.customBiomes.add(new BiomeEntry(biomeName, temperature, humidity,
                                                continentalness, erosion, depth, weirdness, translation));
            return this;
        }

        /**
         * Add a biome with simplified parameters (good for common biomes)
         * @param biomeName Full biome resource location
         * @param weight Relative weight/frequency of this biome (0.1-1.0)
         */
        public PlanetBuilder addBiome(String biomeName, float weight) {
            return addBiome(biomeName, weight, null);
        }

        /**
         * Add a biome with simplified parameters and translation
         * @param biomeName Full biome resource location
         * @param weight Relative weight/frequency of this biome (0.1-1.0)
         * @param translation English translation for the biome (e.g., "Lunar Plains")
         */
        public PlanetBuilder addBiome(String biomeName, float weight, String translation) {
            // Generate climate parameters based on weight and biome type
            float baseParam = (weight - 0.5f) * 2; // Convert weight to -1 to 1 range

            // Auto-generate reasonable climate parameters based on biome name
            if (biomeName.contains("desert") || biomeName.contains("badlands")) {
                // Hot, dry biomes
                this.customBiomes.add(new BiomeEntry(biomeName, 0.8f, -0.8f, baseParam, baseParam * 0.5f, 0.0f, 0.0f, translation));
            } else if (biomeName.contains("frozen") || biomeName.contains("snowy") || biomeName.contains("ice")) {
                // Cold biomes
                this.customBiomes.add(new BiomeEntry(biomeName, -0.8f, -0.2f, baseParam, baseParam * 0.3f, 0.0f, 0.1f, translation));
            } else if (biomeName.contains("jungle") || biomeName.contains("swamp")) {
                // Hot, wet biomes
                this.customBiomes.add(new BiomeEntry(biomeName, 0.7f, 0.9f, baseParam, -baseParam * 0.4f, -0.2f, 0.0f, translation));
            } else if (biomeName.contains("forest") || biomeName.contains("taiga")) {
                // Temperate biomes
                this.customBiomes.add(new BiomeEntry(biomeName, 0.2f, 0.3f, baseParam, baseParam * 0.2f, 0.0f, 0.0f, translation));
            } else if (biomeName.contains("ocean") || biomeName.contains("river")) {
                // Water biomes
                this.customBiomes.add(new BiomeEntry(biomeName, 0.5f, 0.5f, baseParam, -0.5f, -0.8f, 0.0f, translation));
            } else if (biomeName.contains("mountain") || biomeName.contains("peaks")) {
                // Mountain biomes
                this.customBiomes.add(new BiomeEntry(biomeName, -0.3f, -0.1f, baseParam * 0.8f, 0.6f, 0.8f, 0.2f, translation));
            } else if (biomeName.contains("plains") || biomeName.contains("meadow")) {
                // Plains biomes
                this.customBiomes.add(new BiomeEntry(biomeName, 0.4f, 0.0f, baseParam, 0.0f, 0.0f, 0.0f, translation));
            } else if (biomeName.contains("savanna")) {
                // Savanna biomes
                this.customBiomes.add(new BiomeEntry(biomeName, 0.9f, -0.5f, baseParam, baseParam * 0.3f, 0.1f, 0.0f, translation));
            } else if (biomeName.contains("basalt") || biomeName.contains("soul") || biomeName.contains("nether")) {
                // Nether-like biomes for volcanic planets
                this.customBiomes.add(new BiomeEntry(biomeName, 1.0f, -1.0f, baseParam, baseParam * 0.7f, 0.3f, 0.5f, translation));
            } else {
                // Default parameters
                this.customBiomes.add(new BiomeEntry(biomeName, 0.0f, 0.0f, baseParam, 0.0f, 0.0f, 0.0f, translation));
            }
            return this;
        }

        /**
         * Clear all biomes (useful before adding custom set)
         */
        public PlanetBuilder clearBiomes() {
            this.customBiomes.clear();
            return this;
        }

        // ========== LIQUID SYSTEM METHODS ==========

        /**
         * Configure ocean generation
         * @param fluid Fluid type (e.g., "minecraft:water", "minecraft:lava")
         * @param level Ocean level (Y coordinate, or -1 to use seaLevel)
         * @param frequency Ocean frequency (0.0-1.0, higher = more ocean)
         */
        public PlanetBuilder oceanConfig(String fluid, int level, float frequency) {
            this.oceanFluid = fluid;
            this.oceanLevel = level;
            this.oceanFrequency = Math.max(0.0f, Math.min(1.0f, frequency));
            return this;
        }

        /**
         * Simple ocean configuration with just fluid type
         * @param fluid Ocean fluid type
         */
        public PlanetBuilder oceanFluid(String fluid) {
            this.oceanFluid = fluid;
            return this;
        }

        /**
         * Set ocean level (useful for planets with different sea levels)
         * @param level Y level for ocean surface
         */
        public PlanetBuilder oceanLevel(int level) {
            this.oceanLevel = level;
            // Also update seaLevel if ocean level is set
            if (level > 0) {
                this.seaLevel = level;
            }
            return this;
        }

        /**
         * Configure lake generation
         * @param fluid Lake fluid type
         * @param frequency Lake frequency (0.0-1.0)
         */
        public PlanetBuilder lakeConfig(String fluid, float frequency) {
            this.lakeFluid = fluid;
            this.lakeFrequency = Math.max(0.0f, Math.min(1.0f, frequency));
            return this;
        }

        /**
         * Configure underground liquid pools (aquifers)
         * @param fluid Underground liquid type
         * @param enabled Whether to generate underground liquids
         */
        public PlanetBuilder undergroundLiquids(String fluid, boolean enabled) {
            this.undergroundLiquid = fluid;
            this.enableUndergroundLiquids = enabled;
            this.aquifersEnabled = enabled; // Sync with aquifer system
            return this;
        }

        /**
         * Configure lava lakes (or other hot liquid pools)
         * @param level Y level for lava lake generation
         * @param frequency Frequency of lava lakes (0.0-1.0)
         */
        public PlanetBuilder lavaLakes(int level, float frequency) {
            this.lavaLakeLevel = level;
            this.lavaLakeFrequency = Math.max(0.0f, Math.min(1.0f, frequency));
            // Increase lava noise proportionally
            this.lavaNoise = frequency;
            return this;
        }

        /**
         * Disable all liquid generation (for dry worlds)
         */
        public PlanetBuilder noLiquids() {
            this.oceanFrequency = 0.0f;
            this.lakeFrequency = 0.0f;
            this.lavaLakeFrequency = 0.0f;
            this.enableUndergroundLiquids = false;
            this.aquifersEnabled = false;
            this.defaultFluid = "minecraft:air";
            return this;
        }

        // ========== STRUCTURE GENERATION METHODS ==========

        /**
         * Add a custom structure to the planet
         * @param structureName Structure resource location (e.g., "minecraft:village")
         * @param spacing Average spacing between structures (chunks)
         * @param separation Minimum separation between structures (chunks)
         * @param salt Random salt for placement
         */
        public PlanetBuilder addStructure(String structureName, int spacing, int separation, int salt) {
            this.customStructures.add(new StructureEntry(structureName, spacing, separation, salt));
            return this;
        }

        /**
         * Add a structure with default spacing
         * @param structureName Structure to add
         */
        public PlanetBuilder addStructure(String structureName) {
            // Default spacing based on structure type
            int spacing, separation, salt;
            if (structureName.contains("village")) {
                spacing = 32; separation = 8; salt = 10387312;
            } else if (structureName.contains("fortress")) {
                spacing = 27; separation = 4; salt = 30084232;
            } else if (structureName.contains("stronghold")) {
                spacing = 64; separation = 32; salt = 165745296;
            } else if (structureName.contains("monument")) {
                spacing = 32; separation = 5; salt = 10387313;
            } else if (structureName.contains("mansion")) {
                spacing = 80; separation = 20; salt = 10387319;
            } else if (structureName.contains("outpost")) {
                spacing = 32; separation = 8; salt = 165745296;
            } else if (structureName.contains("bastion")) {
                spacing = 27; separation = 4; salt = 30084232;
            } else if (structureName.contains("dungeon") || structureName.contains("mineshaft")) {
                spacing = 16; separation = 4; salt = 14357618;
            } else {
                spacing = 24; separation = 6; salt = 14357618;
            }
            return addStructure(structureName, spacing, separation, salt);
        }

        /**
         * Enable vanilla structure presets
         */
        public PlanetBuilder enableVillages() {
            this.enableVillages = true;
            return addStructure("minecraft:village");
        }

        public PlanetBuilder enableStrongholds() {
            this.enableStrongholds = true;
            return addStructure("minecraft:stronghold");
        }

        public PlanetBuilder enableMineshafts() {
            this.enableMineshafts = true;
            return addStructure("minecraft:mineshaft");
        }

        public PlanetBuilder enableDungeons() {
            this.enableDungeons = true;
            // Dungeons are features, not structures in vanilla
            return this;
        }

        /**
         * Add a preset structure set for planet type
         */
        public PlanetBuilder addStructurePreset(String preset) {
            switch(preset.toLowerCase()) {
                case "overworld":
                    enableVillages();
                    enableStrongholds();
                    enableMineshafts();
                    enableDungeons();
                    addStructure("minecraft:desert_pyramid");
                    addStructure("minecraft:jungle_pyramid");
                    addStructure("minecraft:igloo");
                    break;
                case "nether":
                    addStructure("minecraft:fortress");
                    addStructure("minecraft:bastion_remnant");
                    addStructure("minecraft:nether_fossil");
                    break;
                case "end":
                    addStructure("minecraft:end_city");
                    break;
                case "ocean":
                    addStructure("minecraft:ocean_monument");
                    addStructure("minecraft:ocean_ruin");
                    addStructure("minecraft:shipwreck");
                    break;
                case "barren":
                    // Minimal structures for moon-like worlds
                    addStructure("minecraft:ruined_portal");
                    break;
            }
            return this;
        }

        /**
         * Clear all structures
         */
        public PlanetBuilder clearStructures() {
            this.customStructures.clear();
            this.enableVillages = false;
            this.enableStrongholds = false;
            this.enableMineshafts = false;
            this.enableDungeons = false;
            return this;
        }

        // ========== FEATURE PLACEMENT METHODS ==========

        /**
         * Add a custom feature to the planet
         * @param featureName Feature resource location (e.g., "minecraft:oak")
         * @param frequency Spawn frequency (0.0-1.0)
         * @param placementStep Generation step for this feature
         */
        public PlanetBuilder addFeature(String featureName, float frequency, String placementStep) {
            this.customFeatures.add(new FeatureEntry(featureName, frequency, placementStep));
            return this;
        }

        /**
         * Add a feature with default placement step
         * @param featureName Feature to add
         * @param frequency Spawn frequency
         */
        public PlanetBuilder addFeature(String featureName, float frequency) {
            String step = "vegetal_decoration"; // Default step
            if (featureName.contains("ore") || featureName.contains("geode")) {
                step = "underground_ores";
            } else if (featureName.contains("lake") || featureName.contains("spring")) {
                step = "lakes";
            } else if (featureName.contains("rock") || featureName.contains("boulder")) {
                step = "raw_generation";
            }
            return addFeature(featureName, frequency, step);
        }

        /**
         * Configure vegetation frequencies
         */
        public PlanetBuilder vegetation(float trees, float grass, float flowers) {
            this.treeFrequency = Math.max(0.0f, Math.min(1.0f, trees));
            this.grassFrequency = Math.max(0.0f, Math.min(1.0f, grass));
            this.flowerFrequency = Math.max(0.0f, Math.min(1.0f, flowers));

            // Adjust vegetation noise based on frequencies
            this.vegetationNoise = (trees + grass + flowers) / 3.0f;
            return this;
        }

        /**
         * Add tree features with specific types
         */
        public PlanetBuilder addTrees(String treeType, float frequency) {
            this.treeFrequency = frequency;
            return addFeature("minecraft:" + treeType, frequency, "vegetal_decoration");
        }

        /**
         * Add rock/boulder formations
         */
        public PlanetBuilder addRocks(float frequency) {
            this.rockFrequency = frequency;
            addFeature("minecraft:forest_rock", frequency * 0.5f, "raw_generation");
            addFeature("minecraft:iceberg", frequency * 0.3f, "raw_generation");
            return this;
        }

        /**
         * Enable special features
         */
        public PlanetBuilder enableCrystals() {
            this.enableCrystals = true;
            addFeature("minecraft:amethyst_geode", 0.025f, "underground_decoration");
            return this;
        }

        public PlanetBuilder enableGeysers() {
            this.enableGeysers = true;
            addFeature("minecraft:delta_feature", 0.1f, "surface_structures");
            return this;
        }

        public PlanetBuilder enableGlowLichen() {
            this.enableGlowLichen = true;
            addFeature("minecraft:glow_lichen", 0.2f, "underground_decoration");
            return this;
        }

        /**
         * Add feature preset for planet type
         */
        public PlanetBuilder addFeaturePreset(String preset) {
            switch(preset.toLowerCase()) {
                case "forest":
                    vegetation(0.8f, 0.9f, 0.4f);
                    addTrees("oak", 0.4f);
                    addTrees("birch", 0.2f);
                    addTrees("fancy_oak", 0.1f);
                    addFeature("minecraft:flower_default", 0.3f);
                    break;

                case "desert":
                    vegetation(0.05f, 0.1f, 0.02f);
                    addFeature("minecraft:desert_well", 0.001f);
                    addFeature("minecraft:fossil", 0.01f);
                    addRocks(0.2f);
                    break;

                case "volcanic":
                    addFeature("minecraft:basalt_columns", 0.2f);
                    addFeature("minecraft:basalt_pillar", 0.1f);
                    addFeature("minecraft:blackstone_blobs", 0.15f);
                    addFeature("minecraft:glowstone", 0.1f);
                    addFeature("minecraft:fire", 0.3f);
                    enableGeysers();
                    break;

                case "frozen":
                    vegetation(0.1f, 0.2f, 0.0f);
                    addTrees("spruce", 0.1f);
                    addFeature("minecraft:ice_spike", 0.15f);
                    addFeature("minecraft:ice_patch", 0.3f);
                    addFeature("minecraft:freeze_top_layer", 1.0f);
                    break;

                case "lush":
                    vegetation(0.9f, 1.0f, 0.6f);
                    addTrees("jungle", 0.5f);
                    addFeature("minecraft:bamboo", 0.2f);
                    addFeature("minecraft:moss_vegetation", 0.4f);
                    addFeature("minecraft:lush_caves_vegetation", 0.3f);
                    enableGlowLichen();
                    break;

                case "barren":
                    // Minimal features for moon-like worlds
                    addRocks(0.1f);
                    addFeature("minecraft:disk_gravel", 0.05f);
                    break;

                case "ocean":
                    addFeature("minecraft:kelp", 0.3f);
                    addFeature("minecraft:seagrass", 0.5f);
                    addFeature("minecraft:coral_reef", 0.1f);
                    addFeature("minecraft:sea_pickle", 0.05f);
                    break;
            }
            return this;
        }

        /**
         * Clear all features
         */
        public PlanetBuilder clearFeatures() {
            this.customFeatures.clear();
            this.treeFrequency = 0.0f;
            this.grassFrequency = 0.0f;
            this.flowerFrequency = 0.0f;
            this.rockFrequency = 0.0f;
            this.enableCrystals = false;
            this.enableGeysers = false;
            this.enableGlowLichen = false;
            return this;
        }

        // Cave system configuration methods

        /**
         * Configure basic cave generation parameters
         * @param frequency Cave generation frequency (0.0-2.0)
         * @param size Cave size multiplier (0.5-3.0)
         */
        public PlanetBuilder caveConfig(float frequency, float size) {
            this.caveFrequency = Math.max(0.0f, Math.min(2.0f, frequency));
            this.caveSize = Math.max(0.5f, Math.min(3.0f, size));
            return this;
        }

        /**
         * Set cave generation height limits
         */
        public PlanetBuilder caveHeightRange(int minY, int maxY) {
            this.caveMinY = minY;
            this.caveMaxY = maxY;
            return this;
        }

        /**
         * Configure cave vertical scale
         * @param yScale Vertical stretch of caves (0.1-2.0)
         */
        public PlanetBuilder caveYScale(float yScale) {
            this.caveYScale = Math.max(0.1f, Math.min(2.0f, yScale));
            return this;
        }

        /**
         * Configure ravine generation
         * @param frequency Ravine frequency (0.0-1.0)
         * @param depth Ravine depth multiplier (1.0-5.0)
         */
        public PlanetBuilder ravineConfig(float frequency, float depth) {
            this.ravineFrequency = Math.max(0.0f, Math.min(1.0f, frequency));
            this.ravineDepth = Math.max(1.0f, Math.min(5.0f, depth));
            return this;
        }

        /**
         * Enable/disable different cave types
         */
        public PlanetBuilder cheeseCaves(boolean enabled) {
            this.enableCheeseCaves = enabled;
            return this;
        }

        public PlanetBuilder spaghettiCaves(boolean enabled) {
            this.enableSpaghettiCaves = enabled;
            return this;
        }

        public PlanetBuilder noodleCaves(boolean enabled) {
            this.enableNoodleCaves = enabled;
            return this;
        }

        /**
         * Configure flooded caves
         * @param fluid Fluid to fill caves with
         * @param level Y level where caves flood
         */
        public PlanetBuilder floodedCaves(String fluid, float level) {
            this.caveFluid = fluid;
            this.caveFluidLevel = level;
            return this;
        }

        /**
         * Enable special cave types
         */
        public PlanetBuilder lavaTubes(boolean enabled) {
            this.enableLavaTubes = enabled;
            if (enabled) {
                // Lava tubes have specific characteristics
                this.caveSize = 2.5f;
                this.caveYScale = 0.3f; // Flatter
                this.caveFluid = "minecraft:lava";
                this.enableCheeseCaves = false;
                this.enableNoodleCaves = false;
            }
            return this;
        }

        public PlanetBuilder crystalCaves(boolean enabled) {
            this.enableCrystalCaves = enabled;
            if (enabled) {
                // Add crystal decorations
                addCaveDecoration("minecraft:amethyst_cluster", 0.15f, -64, 320, true);
                addCaveDecoration("minecraft:amethyst_cluster", 0.1f, -64, 320, false);
                addCaveDecoration("minecraft:glowstone", 0.05f, -64, 320, true);
            }
            return this;
        }

        public PlanetBuilder iceCaves(boolean enabled) {
            this.enableIceCaves = enabled;
            if (enabled) {
                // Configure for ice-themed caves
                addCaveDecoration("minecraft:ice", 0.3f, -64, 320, false);
                addCaveDecoration("minecraft:packed_ice", 0.2f, -64, 320, true);
                addCaveDecoration("minecraft:blue_ice", 0.1f, -64, 320, false);
            }
            return this;
        }

        /**
         * Add cave decoration
         * @param block Block to use as decoration
         * @param frequency How often it appears (0.0-1.0)
         * @param minHeight Minimum Y level
         * @param maxHeight Maximum Y level
         * @param ceiling True for ceiling, false for floor
         */
        public PlanetBuilder addCaveDecoration(String block, float frequency, int minHeight, int maxHeight, boolean ceiling) {
            this.caveDecorations.add(new CaveDecorationEntry(block, frequency, minHeight, maxHeight, ceiling));
            return this;
        }

        /**
         * Apply cave preset configurations
         */
        public PlanetBuilder addCavePreset(String preset) {
            switch (preset.toLowerCase()) {
                // ========== NEW VANILLA-ACCURATE PRESETS ==========

                case "minimal_airless":
                    // VERY SPARSE caves for airless planets (Moon, Mercury)
                    // With proper thresholding, all cave types can be rare
                    caveConfig(0.1f, 1.0f);    // 10% frequency with high thresholds = sparse caves
                    cheeseCaves(true);         // Large caverns (but rare due to high threshold)
                    spaghettiCaves(true);      // Winding tunnels (rare)
                    noodleCaves(true);         // Thin passages (rare)
                    caveHeightRange(-64, 128); // Throughout most of the world
                    ravineConfig(0.01f, 1.5f); // Rare surface cracks
                    break;

                case "balanced_vanilla":
                    // NORMAL cave generation - not too crazy
                    // Still less than vanilla to avoid swiss cheese planets
                    caveConfig(0.5f, 1.0f);    // 50% of vanilla frequency
                    cheeseCaves(true);
                    spaghettiCaves(true);
                    noodleCaves(true);
                    caveHeightRange(-64, 256);
                    ravineConfig(0.05f, 2.5f); // Reduced ravines
                    break;

                case "dramatic_alien":
                    // Enhanced caves but still reasonable
                    // This is 100% of vanilla frequency (not more!)
                    caveConfig(1.0f, 1.0f);    // Standard vanilla frequency
                    cheeseCaves(true);
                    spaghettiCaves(true);
                    noodleCaves(true);
                    caveHeightRange(-64, 256);
                    ravineConfig(0.1f, 3.0f);  // Standard ravines
                    break;

                case "insane_vertical":
                    // ACTUALLY INSANE - this one should be crazy
                    // Only for dedicated test worlds
                    caveConfig(1.5f, 0.9f);    // 150% frequency
                    caveYScale(0.3f);          // Extreme vertical stretching
                    cheeseCaves(true);
                    spaghettiCaves(true);
                    noodleCaves(true);
                    caveHeightRange(-64, 320);
                    ravineConfig(0.2f, 4.0f);  // Frequent ravines
                    break;

                // ========== LEGACY PRESETS (KEPT FOR COMPATIBILITY) ==========

                case "standard":
                    caveConfig(1.0f, 1.0f);
                    cheeseCaves(true);
                    spaghettiCaves(true);
                    noodleCaves(true);
                    ravineConfig(0.1f, 3.0f);
                    break;

                case "volcanic":
                case "lava_tubes":
                    lavaTubes(true);
                    ravineConfig(0.2f, 4.0f);
                    floodedCaves("minecraft:lava", -32f);
                    addCaveDecoration("minecraft:magma_block", 0.2f, -64, 64, false);
                    addCaveDecoration("minecraft:obsidian", 0.1f, -64, 64, false);
                    break;

                case "underwater":
                case "flooded":
                    caveConfig(1.2f, 1.5f);
                    cheeseCaves(true);
                    spaghettiCaves(true);
                    floodedCaves("minecraft:water", 0f);
                    addCaveDecoration("minecraft:prismarine", 0.1f, -64, 64, true);
                    addCaveDecoration("minecraft:sea_lantern", 0.02f, -64, 64, true);
                    break;

                case "crystal":
                    crystalCaves(true);
                    caveConfig(0.8f, 1.8f);
                    cheeseCaves(true);
                    spaghettiCaves(false);
                    noodleCaves(false);
                    break;

                case "frozen":
                case "ice":
                    iceCaves(true);
                    caveConfig(0.9f, 1.2f);
                    ravineConfig(0.05f, 2.5f);
                    break;

                case "massive":
                    caveConfig(1.5f, 2.5f);
                    cheeseCaves(true);
                    spaghettiCaves(false);
                    noodleCaves(false);
                    ravineConfig(0.3f, 5.0f);
                    break;

                case "dense":
                    caveConfig(2.0f, 0.8f);
                    cheeseCaves(false);
                    spaghettiCaves(true);
                    noodleCaves(true);
                    ravineConfig(0.2f, 2.0f);
                    break;

                case "minimal":
                    caveConfig(0.3f, 0.7f);
                    cheeseCaves(false);
                    spaghettiCaves(true);
                    noodleCaves(false);
                    ravineConfig(0.02f, 1.5f);
                    break;

                case "none":
                    caveConfig(0.0f, 0.0f);
                    cheeseCaves(false);
                    spaghettiCaves(false);
                    noodleCaves(false);
                    ravineConfig(0.0f, 0.0f);
                    break;
            }
            return this;
        }

        /**
         * Clear all cave decorations
         */
        public PlanetBuilder clearCaveDecorations() {
            this.caveDecorations.clear();
            return this;
        }

        // ========== GETTERS FOR DIMENSION EFFECTS FALLBACK ==========

        /**
         * Get the planet name
         * @return Planet identifier
         */
        public String getName() {
            return name;
        }

        /**
         * Check if planet has atmosphere
         * @return True if planet has atmosphere
         */
        public boolean hasAtmosphere() {
            return hasAtmosphere;
        }

        /**
         * Check if clouds are enabled
         * @return True if clouds should render
         */
        public boolean hasClouds() {
            return cloudsEnabled;
        }

        /**
         * Check if rain is enabled
         * @return True if rain can occur
         */
        public boolean hasRain() {
            return rainEnabled;
        }

        /**
         * Get sky color
         * @return Sky color as RGB integer
         */
        public int getSkyColor() {
            return skyColor;
        }

        /**
         * Get fog color
         * @return Fog color as RGB integer
         */
        public int getFogColor() {
            return fogColor;
        }

        /**
         * Generate Patchouli journal entry for this planet
         */
        private void generatePatchouliEntry() {
            try {
                JsonObject entry = new JsonObject();
                entry.addProperty("name", capitalizeWords(name.replace("_", " ")));
                entry.addProperty("icon", getIconForPlanet());
                entry.addProperty("category", "adastramekanized:known_planets");
                entry.addProperty("advancement", "adastramekanized:planets/visited_" + name);

                JsonArray pages = new JsonArray();

                // Page 1: Planetary Data - Stats
                JsonObject statsPage = new JsonObject();
                statsPage.addProperty("type", "patchouli:text");
                statsPage.addProperty("title", "Planetary Data");

                StringBuilder statsText = new StringBuilder();
                statsText.append("$(bold)Gravity:$() ").append(String.format("%.1f", gravity * 100)).append("% of Earth$(br)");
                statsText.append("$(bold)Atmosphere:$() ").append(hasAtmosphere ? "Yes" : "None (requires oxygen)").append("$(br)");
                statsText.append("$(bold)Temperature:$() [Not Yet Implemented]$(br)");
                statsText.append("$(bold)Sky Color:$() #").append(String.format("%06X", skyColor)).append("$(br)");
                statsText.append("$(bold)Ambient Light:$() ").append(String.format("%.1f%%", ambientLight * 100)).append("$(br)");
                statsText.append("$(bold)Clouds:$() ").append(cloudsEnabled ? "Yes" : "No").append("$(br)");
                statsText.append("$(bold)Rain:$() ").append(rainEnabled ? "Yes" : "No");

                statsPage.addProperty("text", statsText.toString());
                pages.add(statsPage);

                // Page 2: Ore/Resource Listings
                if (!oreVeinCounts.isEmpty() || oreVeinsEnabled) {
                    JsonObject oresPage = new JsonObject();
                    oresPage.addProperty("type", "patchouli:text");
                    oresPage.addProperty("title", "Resources");

                    StringBuilder oresText = new StringBuilder();
                    oresText.append("$(bold)Ore Deposits:$()$(br)$(br)");

                    if (!oreVeinCounts.isEmpty()) {
                        List<java.util.Map.Entry<String, Integer>> sortedOres = new ArrayList<>(oreVeinCounts.entrySet());
                        sortedOres.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                        for (java.util.Map.Entry<String, Integer> ore : sortedOres) {
                            String oreName = capitalizeWords(ore.getKey());
                            oresText.append("• ").append(oreName);
                            int veins = ore.getValue();
                            if (veins >= 20) {
                                oresText.append(" (Abundant)");
                            } else if (veins >= 10) {
                                oresText.append(" (Common)");
                            } else if (veins >= 5) {
                                oresText.append(" (Uncommon)");
                            } else {
                                oresText.append(" (Rare)");
                            }
                            oresText.append("$(br)");
                        }
                    } else {
                        oresText.append("Standard ore distribution enabled.");
                    }

                    oresPage.addProperty("text", oresText.toString());
                    pages.add(oresPage);
                }

                // Page 3: Biome Information
                if (!customBiomes.isEmpty()) {
                    JsonObject biomesPage = new JsonObject();
                    biomesPage.addProperty("type", "patchouli:text");
                    biomesPage.addProperty("title", "Biomes");

                    StringBuilder biomesText = new StringBuilder();
                    biomesText.append("$(bold)Terrain Biomes:$()$(br)$(br)");

                    for (BiomeEntry biome : customBiomes) {
                        String biomeName = biome.biomeName;
                        if (biomeName.contains(":")) {
                            biomeName = biomeName.substring(biomeName.indexOf(":") + 1);
                        }
                        biomeName = capitalizeWords(biomeName.replace("_", " "));
                        biomesText.append("• ").append(biomeName).append("$(br)");
                    }

                    biomesPage.addProperty("text", biomesText.toString());
                    pages.add(biomesPage);
                }

                // Page 4: Surface Composition
                JsonObject surfacePage = new JsonObject();
                surfacePage.addProperty("type", "patchouli:text");
                surfacePage.addProperty("title", "Surface Composition");

                StringBuilder surfaceText = new StringBuilder();
                surfaceText.append("$(bold)Surface Layers:$()$(br)$(br)");
                surfaceText.append("• $(bold)Surface:$() ").append(getBlockDisplayName(surfaceBlock)).append("$(br)");
                surfaceText.append("• $(bold)Subsurface:$() ").append(getBlockDisplayName(subsurfaceBlock)).append("$(br)");
                surfaceText.append("• $(bold)Deep:$() ").append(getBlockDisplayName(deepBlock)).append("$(br)");
                surfaceText.append("• $(bold)Bedrock:$() ").append(getBlockDisplayName(bedrockBlock)).append("$(br)$(br)");
                surfaceText.append("$(bold)Sea Level:$() Y=").append(seaLevel);

                surfacePage.addProperty("text", surfaceText.toString());
                pages.add(surfacePage);

                entry.add("pages", pages);

                String patchouliPath = "src/main/resources/assets/adastramekanized/patchouli_books/journal/en_us/entries/known_planets/" + name + ".json";
                writeJsonFile(patchouliPath, entry);
                AdAstraMekanized.LOGGER.info("Generated Patchouli entry for planet: {}", name);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate Patchouli entry for planet: {}", name, e);
            }
        }

        /**
         * Generate unlock advancement for visiting this planet
         */
        private void generateUnlockAdvancement() {
            try {
                JsonObject advancement = new JsonObject();

                JsonObject criteria = new JsonObject();
                JsonObject visitCriteria = new JsonObject();
                visitCriteria.addProperty("trigger", "minecraft:changed_dimension");

                JsonObject conditions = new JsonObject();
                conditions.addProperty("to", "adastramekanized:" + name);
                visitCriteria.add("conditions", conditions);

                criteria.add("visited_" + name, visitCriteria);
                advancement.add("criteria", criteria);

                String advancementPath = RESOURCES_PATH + "advancement/planets/visited_" + name + ".json";
                writeJsonFile(advancementPath, advancement);
                AdAstraMekanized.LOGGER.info("Generated unlock advancement for planet: {}", name);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate unlock advancement for planet: {}", name, e);
            }
        }

        /**
         * Helper: Get a suitable icon for the planet
         */
        private String getIconForPlanet() {
            if (surfaceBlock.contains(":")) {
                return surfaceBlock;
            }
            return "minecraft:stone";
        }

        /**
         * Helper: Get display name for a block
         */
        private String getBlockDisplayName(String blockId) {
            if (blockId.contains(":")) {
                String name = blockId.substring(blockId.indexOf(":") + 1);
                return capitalizeWords(name.replace("_", " "));
            }
            return capitalizeWords(blockId.replace("_", " "));
        }

        /**
         * Helper: Capitalize first letter of each word
         */
        private String capitalizeWords(String str) {
            String[] words = str.split("\\s+");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        result.append(word.substring(1).toLowerCase());
                    }
                    result.append(" ");
                }
            }
            return result.toString().trim();
        }

        /**
         * Generate this planet and add it to the generation queue
         */
        public PlanetBuilder generate() {
            PLANETS.add(this);
            // Generate equipment configuration if any mobs have equipment
            generateMobEquipmentConfig();
            // Generate Patchouli entry and unlock advancement
            generatePatchouliEntry();
            generateUnlockAdvancement();
            // Register modded mob spawn whitelist
            registerModdedMobWhitelist();
            return this;
        }

        /**
         * Register this planet's modded mob whitelist with the spawn controller
         */
        private void registerModdedMobWhitelist() {
            if (usedModNamespaces.isEmpty()) {
                return; // No modded mobs, nothing to register
            }

            net.minecraft.resources.ResourceLocation dimensionId =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    AdAstraMekanized.MOD_ID, this.name);

            // Register each used mod namespace for this dimension
            for (String modNamespace : usedModNamespaces) {
                com.hecookin.adastramekanized.common.events.ModdedMobSpawnController
                    .registerPlanetMobWhitelist(dimensionId, modNamespace);
            }
        }

        /**
         * Generate equipment configuration file for equipped mobs
         */
        private void generateMobEquipmentConfig() {
            JsonObject equipmentConfig = new JsonObject();
            equipmentConfig.addProperty("dimension", "adastramekanized:" + this.name);
            JsonArray mobs = new JsonArray();

            // Process mob spawns that have equipment
            for (java.util.Map.Entry<String, List<MobSpawnEntry>> categoryEntry : mobSpawns.entrySet()) {
                for (MobSpawnEntry spawn : categoryEntry.getValue()) {
                    if (spawn.equipment != null && !spawn.equipment.isEmpty()) {
                        JsonObject mobConfig = new JsonObject();
                        mobConfig.addProperty("mob_id", spawn.mobId);

                        // Add equipment slots
                        JsonObject equipment = new JsonObject();
                        for (java.util.Map.Entry<String, String> equipEntry : spawn.equipment.entrySet()) {
                            equipment.addProperty(equipEntry.getKey(), equipEntry.getValue());
                        }
                        mobConfig.add("equipment", equipment);

                        // Add drop chance (always 0 to prevent farming)
                        mobConfig.addProperty("drop_chance", 0.0f);
                        mobConfig.addProperty("equip_chance", 1.0f); // Always equip

                        mobs.add(mobConfig);
                    }
                }
            }

            if (mobs.size() > 0) {
                equipmentConfig.add("mobs", mobs);
                // Write to a data file that can be loaded at runtime
                try {
                    writeJsonFile(RESOURCES_PATH + "planet_equipment/" + name + "_equipment.json", equipmentConfig);
                    System.out.println("Generated equipment config for " + name + " with " + mobs.size() + " equipped mob types");
                } catch (IOException e) {
                    System.err.println("Failed to write equipment config for " + name + ": " + e.getMessage());
                }
            }
        }

        /**
         * Inner class for biome entries
         */
        private static class BiomeEntry {
            final String biomeName;
            final float temperature;
            final float humidity;
            final float continentalness;
            final float erosion;
            final float depth;
            final float weirdness;
            final String translation;

            BiomeEntry(String biomeName, float temperature, float humidity,
                      float continentalness, float erosion, float depth, float weirdness, String translation) {
                this.biomeName = biomeName;
                this.temperature = temperature;
                this.humidity = humidity;
                this.continentalness = continentalness;
                this.erosion = erosion;
                this.depth = depth;
                this.weirdness = weirdness;
                this.translation = translation;
            }
        }

        /**
         * Inner class for structure entries
         */
        private static class StructureEntry {
            final String structureName;
            final int spacing;
            final int separation;
            final int salt;

            StructureEntry(String structureName, int spacing, int separation, int salt) {
                this.structureName = structureName;
                this.spacing = spacing;
                this.separation = separation;
                this.salt = salt;
            }
        }

        /**
         * Inner class for feature entries
         */
        private static class FeatureEntry {
            final String featureName;
            final float frequency;
            final String placementStep;

            FeatureEntry(String featureName, float frequency, String placementStep) {
                this.featureName = featureName;
                this.frequency = frequency;
                this.placementStep = placementStep;
            }
        }

        /**
         * Inner class for cave decoration entries
         */
        private static class CaveDecorationEntry {
            final String decorationBlock;
            final float frequency;
            final int minHeight;
            final int maxHeight;
            final boolean ceiling; // true for ceiling decorations, false for floor

            CaveDecorationEntry(String decorationBlock, float frequency, int minHeight, int maxHeight, boolean ceiling) {
                this.decorationBlock = decorationBlock;
                this.frequency = frequency;
                this.minHeight = minHeight;
                this.maxHeight = maxHeight;
                this.ceiling = ceiling;
            }
        }

        /**
         * Inner class for mob spawn entries
         */
        private static class MobSpawnEntry {
            final String mobId;
            final int weight;
            final int minCount;
            final int maxCount;
            java.util.Map<String, String> equipment; // Optional equipment data

            MobSpawnEntry(String mobId, int weight, int minCount, int maxCount) {
                this.mobId = mobId;
                this.weight = weight;
                this.minCount = minCount;
                this.maxCount = maxCount;
                this.equipment = null; // Default to no equipment
            }
        }

        /**
         * Inner class for spawn costs
         */
        private static class SpawnCost {
            final double energyBudget;
            final double charge;

            SpawnCost(double energyBudget, double charge) {
                this.energyBudget = energyBudget;
                this.charge = charge;
            }
        }

        /**
         * Inner class for sun configuration
         */
        private static class SunConfig {
            final net.minecraft.resources.ResourceLocation texture;
            final float scale;
            final int color;
            final boolean visible;

            SunConfig(net.minecraft.resources.ResourceLocation texture, float scale, int color, boolean visible) {
                this.texture = texture;
                this.scale = scale;
                this.color = color;
                this.visible = visible;
            }
        }

        /**
         * Inner class for moon configuration
         */
        private static class MoonConfig {
            final net.minecraft.resources.ResourceLocation texture;
            final float scale;
            final int color;
            final float horizontalPosition;
            final float verticalPosition;
            final boolean movesWithTime;
            final boolean visible;

            MoonConfig(net.minecraft.resources.ResourceLocation texture, float scale, int color,
                      float horizontalPosition, float verticalPosition, boolean movesWithTime, boolean visible) {
                this.texture = texture;
                this.scale = scale;
                this.color = color;
                this.horizontalPosition = horizontalPosition;
                this.verticalPosition = verticalPosition;
                this.movesWithTime = movesWithTime;
                this.visible = visible;
            }
        }

        /**
         * Inner class for visible planet configuration
         */
        private static class PlanetConfig {
            final net.minecraft.resources.ResourceLocation texture;
            final float scale;
            final int color;
            final float horizontalPosition;
            final float verticalPosition;
            final boolean movesWithTime;
            final boolean visible;

            PlanetConfig(net.minecraft.resources.ResourceLocation texture, float scale, int color,
                        float horizontalPosition, float verticalPosition, boolean movesWithTime, boolean visible) {
                this.texture = texture;
                this.scale = scale;
                this.color = color;
                this.horizontalPosition = horizontalPosition;
                this.verticalPosition = verticalPosition;
                this.movesWithTime = movesWithTime;
                this.visible = visible;
            }
        }
    }

    private static void createDirectories() {
        new File(RESOURCES_PATH + "planets").mkdirs();
        new File(RESOURCES_PATH + "dimension").mkdirs();
        new File(RESOURCES_PATH + "dimension_type").mkdirs();
        new File(RESOURCES_PATH + "worldgen/noise_settings").mkdirs();
        new File(RESOURCES_PATH + "worldgen/configured_feature").mkdirs();
        new File(RESOURCES_PATH + "worldgen/placed_feature").mkdirs();
        new File(RESOURCES_PATH + "worldgen/biome").mkdirs();
        new File(RESOURCES_PATH + "advancement/planets").mkdirs();
        new File("src/main/resources/assets/adastramekanized/patchouli_books/journal/en_us/entries/known_planets").mkdirs();
    }

    /**
     * Generate all files for a planet using Moon's proven TerraBlender approach
     */
    private static void generatePlanetFiles(PlanetBuilder planet) throws IOException {
        generatePlanetData(planet);
        generateDimensionData(planet);
        generateDimensionType(planet);
        generateNoiseSettings(planet);

        // Generate ore features if enabled
        if (planet.oreVeinsEnabled || planet.customOreVeins.size() > 0) {
            generateOreFeatures(planet);
        }

        // Generate custom biomes with features
        generateCustomBiomes(planet);
    }

    /**
     * Generate planet data JSON (basic planet properties)
     */
    private static void generatePlanetData(PlanetBuilder planet) throws IOException {
        JsonObject planetJson = new JsonObject();

        planetJson.addProperty("id", "adastramekanized:" + planet.name);
        planetJson.addProperty("display_name", capitalizeFirst(planet.name));

        // Basic properties
        JsonObject properties = new JsonObject();
        properties.addProperty("gravity", planet.gravity);
        properties.addProperty("temperature", 20.0f);
        properties.addProperty("day_length", 24.0f);
        properties.addProperty("orbit_distance", 1000);
        properties.addProperty("has_rings", false);
        properties.addProperty("moon_count", 0);
        planetJson.add("properties", properties);

        // Atmosphere
        JsonObject atmosphere = new JsonObject();
        atmosphere.addProperty("has_atmosphere", planet.hasAtmosphere);
        atmosphere.addProperty("breathable", planet.hasAtmosphere);
        atmosphere.addProperty("pressure", planet.hasAtmosphere ? 1.0f : 0.0f);
        atmosphere.addProperty("oxygen_level", planet.hasAtmosphere ? 0.21f : 0.0f);
        atmosphere.addProperty("type", planet.hasAtmosphere ? "NORMAL" : "NONE");
        planetJson.add("atmosphere", atmosphere);

        // Dimension reference
        JsonObject dimension = new JsonObject();
        dimension.addProperty("dimension_type", "adastramekanized:" + planet.name);
        dimension.addProperty("biome_source", planet.name + "_biome_source");
        dimension.addProperty("chunk_generator", planet.name + "_generator");
        dimension.addProperty("is_orbital", false);
        dimension.addProperty("sky_color", planet.skyColor);
        dimension.addProperty("fog_color", planet.fogColor);
        dimension.addProperty("ambient_light", planet.ambientLight);
        planetJson.add("dimension", dimension);

        // Atmospheric rendering configuration
        JsonObject atmosphericRendering = new JsonObject();

        // Sky configuration
        JsonObject sky = new JsonObject();
        sky.addProperty("sky_color", planet.skyColor);
        // Airless planets have no atmospheric sunrise/sunset effects
        sky.addProperty("sunrise_color", planet.hasAtmosphere ? 16777087 : 0);
        sky.addProperty("custom_sky", true);
        sky.addProperty("has_stars", true);
        sky.addProperty("star_count", planet.starCount);
        sky.addProperty("star_brightness", planet.starBrightness);
        sky.addProperty("star_visibility", planet.starsVisibleDuringDay ? "constant" : "night_only");
        atmosphericRendering.add("sky", sky);

        // Fog configuration
        JsonObject fog = new JsonObject();
        fog.addProperty("fog_color", planet.fogColor);
        fog.addProperty("has_fog", planet.hasAtmosphere);
        fog.addProperty("fog_density", planet.hasAtmosphere ? 0.5f : 0.0f);
        fog.addProperty("near_plane", 192.0f);
        fog.addProperty("far_plane", 512.0f);
        atmosphericRendering.add("fog", fog);

        // Celestial bodies configuration
        JsonObject celestialBodies = new JsonObject();

        // Suns
        JsonArray sunsArray = new JsonArray();
        if (planet.suns.isEmpty()) {
            // Add default sun if none specified
            JsonObject defaultSun = new JsonObject();
            defaultSun.addProperty("texture", "minecraft:textures/environment/sun.png");
            defaultSun.addProperty("scale", 1.0f);
            defaultSun.addProperty("color", 0xFFFFFF);
            defaultSun.addProperty("visible", planet.hasSkylight);
            sunsArray.add(defaultSun);
        } else {
            for (PlanetBuilder.SunConfig sun : planet.suns) {
                JsonObject sunJson = new JsonObject();
                sunJson.addProperty("texture", sun.texture.toString());
                sunJson.addProperty("scale", sun.scale);
                sunJson.addProperty("color", sun.color);
                sunJson.addProperty("visible", sun.visible);
                sunsArray.add(sunJson);
            }
        }
        celestialBodies.add("sun", sunsArray.size() == 1 ? sunsArray.get(0) : sunsArray);

        // Moons
        JsonArray moonsArray = new JsonArray();
        for (PlanetBuilder.MoonConfig moon : planet.moons) {
            JsonObject moonJson = new JsonObject();
            moonJson.addProperty("texture", moon.texture.toString());
            moonJson.addProperty("scale", moon.scale);
            moonJson.addProperty("color", moon.color);
            moonJson.addProperty("horizontal_position", moon.horizontalPosition);
            moonJson.addProperty("vertical_position", moon.verticalPosition);
            moonJson.addProperty("moves_with_time", moon.movesWithTime);
            moonJson.addProperty("visible", moon.visible);
            moonsArray.add(moonJson);
        }
        celestialBodies.add("moons", moonsArray);

        // Visible planets
        JsonArray planetsArray = new JsonArray();
        for (PlanetBuilder.PlanetConfig planet2 : planet.visiblePlanets) {
            JsonObject planetJson2 = new JsonObject();
            planetJson2.addProperty("texture", planet2.texture.toString());
            planetJson2.addProperty("scale", planet2.scale);
            planetJson2.addProperty("color", planet2.color);
            planetJson2.addProperty("horizontal_position", planet2.horizontalPosition);
            planetJson2.addProperty("vertical_position", planet2.verticalPosition);
            planetJson2.addProperty("moves_with_time", planet2.movesWithTime);
            planetJson2.addProperty("visible", planet2.visible);
            planetsArray.add(planetJson2);
        }
        celestialBodies.add("visible_planets", planetsArray);

        atmosphericRendering.add("celestial_bodies", celestialBodies);

        // Weather configuration
        JsonObject weather = new JsonObject();
        weather.addProperty("has_clouds", planet.cloudsEnabled);
        weather.addProperty("has_rain", planet.rainEnabled);
        weather.addProperty("has_snow", planet.snowEnabled);
        weather.addProperty("has_storms", planet.stormsEnabled);
        weather.addProperty("rain_acidity", planet.acidRainDamage ? planet.acidRainDamageAmount : 0.0f);
        atmosphericRendering.add("weather", weather);

        // Particle configuration
        JsonObject particles = new JsonObject();
        particles.addProperty("has_dust", false);
        particles.addProperty("has_ash", false);
        particles.addProperty("has_spores", false);
        particles.addProperty("has_snowfall", false);
        particles.addProperty("particle_density", 0.0f);
        particles.addProperty("particle_color", 16777215); // White
        atmosphericRendering.add("particles", particles);

        planetJson.add("rendering", atmosphericRendering);

        writeJsonFile(RESOURCES_PATH + "planets/" + planet.name + ".json", planetJson);

        // Update translations for biomes
        updateBiomeTranslations(planet);
    }

    /**
     * Update en_us.json with biome translations
     */
    private static void updateBiomeTranslations(PlanetBuilder planet) {
        if (planet.customBiomes.isEmpty()) {
            return; // No custom biomes to translate
        }

        String langPath = "src/main/resources/assets/adastramekanized/lang/en_us.json";
        File langFile = new File(langPath);

        try {
            // Read existing translations
            JsonObject translations;
            if (langFile.exists()) {
                String content = Files.readString(langFile.toPath());
                translations = new Gson().fromJson(content, JsonObject.class);
            } else {
                translations = new JsonObject();
            }

            // Add biome translations
            for (PlanetBuilder.BiomeEntry biome : planet.customBiomes) {
                if (biome.translation != null && !biome.translation.isEmpty()) {
                    // Extract namespace and path from biome name (e.g., "adastramekanized:lunar_plains")
                    String[] parts = biome.biomeName.split(":");
                    if (parts.length == 2) {
                        String translationKey = "biome." + parts[0] + "." + parts[1];
                        translations.addProperty(translationKey, biome.translation);
                    }
                }
            }

            // Write updated translations back to file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(translations);
            Files.writeString(langFile.toPath(), json);

            System.out.println("Updated biome translations in " + langPath);

        } catch (IOException e) {
            System.err.println("Failed to update biome translations: " + e.getMessage());
        }
    }

    /**
     * Generate dimension JSON following Moon's TerraBlender pattern
     */
    private static void generateDimensionData(PlanetBuilder planet) throws IOException {
        JsonObject dimensionJson = new JsonObject();

        dimensionJson.addProperty("type", "adastramekanized:" + planet.name);

        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("settings", "adastramekanized:" + planet.name);

        // Use multi-noise biome source for planet-specific biome variety
        JsonObject biomeSource = createBiomePreset(planet);
        generator.add("biome_source", biomeSource);

        dimensionJson.add("generator", generator);

        writeJsonFile(RESOURCES_PATH + "dimension/" + planet.name + ".json", dimensionJson);
    }

    /**
     * Generate dimension type following Moon's pattern
     */
    private static void generateDimensionType(PlanetBuilder planet) throws IOException {
        JsonObject dimensionType = new JsonObject();

        dimensionType.addProperty("ambient_light", planet.ambientLight);
        dimensionType.addProperty("effects", "adastramekanized:" + planet.name);
        dimensionType.addProperty("has_ceiling", false);
        dimensionType.addProperty("has_skylight", planet.hasSkylight);
        dimensionType.addProperty("height", 384);
        dimensionType.addProperty("min_y", -64);
        dimensionType.addProperty("natural", true);
        dimensionType.addProperty("coordinate_scale", 1.0f);
        dimensionType.addProperty("bed_works", true);
        dimensionType.addProperty("respawn_anchor_works", false);
        dimensionType.addProperty("has_raids", planet.hasAtmosphere);
        dimensionType.addProperty("logical_height", 384);
        dimensionType.addProperty("infiniburn", "#minecraft:infiniburn_overworld");
        dimensionType.addProperty("ultrawarm", false);
        dimensionType.addProperty("piglin_safe", false);

        // Monster spawn light settings - controls when monsters can spawn
        if (planet.monsterSpawnLightLevel > 7) {
            // Use uniform distribution for higher light levels (allows day spawning)
            JsonObject spawnLightLevel = new JsonObject();
            spawnLightLevel.addProperty("type", "minecraft:uniform");
            spawnLightLevel.addProperty("min_inclusive", 0);
            spawnLightLevel.addProperty("max_inclusive", planet.monsterSpawnLightLevel);
            dimensionType.add("monster_spawn_light_level", spawnLightLevel);
        } else {
            // Use simple integer for standard spawning
            dimensionType.addProperty("monster_spawn_light_level", planet.monsterSpawnLightLevel);
        }

        dimensionType.addProperty("monster_spawn_block_light_limit", planet.monsterSpawnBlockLightLimit);

        writeJsonFile(RESOURCES_PATH + "dimension_type/" + planet.name + ".json", dimensionType);
    }

    /**
     * Generate noise settings using exact Moon pattern with configurable parameters
     */
    private static void generateNoiseSettings(PlanetBuilder planet) throws IOException {
        JsonObject noiseSettings = new JsonObject();

        // All configurable basic properties
        // Use ocean level if configured, otherwise use default sea level
        int effectiveSeaLevel = (planet.oceanLevel > 0) ? planet.oceanLevel : planet.seaLevel;
        noiseSettings.addProperty("sea_level", effectiveSeaLevel);
        noiseSettings.addProperty("disable_mob_generation", planet.disableMobGeneration);
        // Always enable aquifers for proper cave generation
        boolean enableAquifers = planet.aquifersEnabled ||
                                (planet.caveFrequency > 0) ||
                                (planet.enableCheeseCaves || planet.enableSpaghettiCaves || planet.enableNoodleCaves);
        // Force aquifers on for all planets to ensure cave generation
        noiseSettings.addProperty("aquifers_enabled", true);
        noiseSettings.addProperty("ore_veins_enabled", planet.oreVeinsEnabled);
        noiseSettings.addProperty("legacy_random_source", planet.legacyRandomSource);

        // Configurable default blocks
        JsonObject defaultBlock = new JsonObject();
        defaultBlock.addProperty("Name", planet.defaultBlock);
        noiseSettings.add("default_block", defaultBlock);

        // Use ocean fluid as default fluid if oceans are enabled
        JsonObject defaultFluid = new JsonObject();
        String fluidToUse = planet.oceanFrequency > 0 ? planet.oceanFluid : planet.defaultFluid;
        defaultFluid.addProperty("Name", fluidToUse);
        noiseSettings.add("default_fluid", defaultFluid);

        // Configurable noise/world structure
        JsonObject noise = new JsonObject();
        noise.addProperty("min_y", planet.minY);
        noise.addProperty("height", planet.worldHeight);
        noise.addProperty("size_horizontal", planet.horizontalNoiseSize);
        noise.addProperty("size_vertical", planet.verticalNoiseSize);
        noiseSettings.add("noise", noise);

        // Noise router using Moon's exact working pattern with configurable parameters
        JsonObject noiseRouter = createNoiseRouter(planet);
        noiseSettings.add("noise_router", noiseRouter);

        // Surface rule using Moon's pattern with configurable blocks
        JsonObject surfaceRule = createSurfaceRule(planet);
        noiseSettings.add("surface_rule", surfaceRule);

        // Empty spawn target
        noiseSettings.add("spawn_target", new JsonArray());

        writeJsonFile(RESOURCES_PATH + "worldgen/noise_settings/" + planet.name + ".json", noiseSettings);
    }

    /**
     * Get seed-based variation for a parameter
     */
    private static float getSeedVariation(PlanetBuilder planet, String parameterName, float baseValue, float variationRange) {
        // DISABLED: Seed variation causes terrain instability
        // Return base value directly for stable, predictable terrain
        return baseValue;
    }

    /**
     * Create noise router using Moon's exact working pattern with configurable parameters and seed variations
     */
    private static JsonObject createNoiseRouter(PlanetBuilder planet) {
        JsonObject router = new JsonObject();

        // Noise routing parameters with liquid system integration
        router.addProperty("barrier", planet.barrierNoise);

        // Fluid level controls based on liquid system settings
        router.addProperty("fluid_level_floodedness",
            planet.oceanFrequency > 0 ? planet.fluidLevelFloodedness * planet.oceanFrequency : 0.0f);
        router.addProperty("fluid_level_spread",
            planet.lakeFrequency > 0 ? planet.fluidLevelSpread + (planet.lakeFrequency * 0.2f) : planet.fluidLevelSpread);

        // Lava controlled by lava lake settings
        router.addProperty("lava", planet.lavaLakeFrequency > 0 ? planet.lavaNoise : 0.0f);

        router.addProperty("temperature", planet.temperatureNoise);
        router.addProperty("vegetation", planet.vegetationNoise);

        // Continents with configurable scale and seed variation
        JsonObject continents = new JsonObject();
        continents.addProperty("type", "minecraft:shifted_noise");
        continents.addProperty("noise", "minecraft:continentalness");
        continents.addProperty("xz_scale", getSeedVariation(planet, "continental_scale", planet.continentalScale, planet.continentalScale * 0.3f));
        continents.addProperty("y_scale", 1);
        continents.addProperty("shift_x", "minecraft:shift_x");
        continents.addProperty("shift_y", 0);
        continents.addProperty("shift_z", "minecraft:shift_z");
        router.add("continents", continents);

        // Erosion with configurable scale and seed variation
        JsonObject erosion = new JsonObject();
        erosion.addProperty("type", "minecraft:shifted_noise");
        erosion.addProperty("noise", "minecraft:erosion");
        erosion.addProperty("xz_scale", getSeedVariation(planet, "erosion_scale", planet.erosionScale, planet.erosionScale * 0.4f));
        erosion.addProperty("y_scale", 0);
        erosion.addProperty("shift_x", "minecraft:shift_x");
        erosion.addProperty("shift_y", 0);
        erosion.addProperty("shift_z", "minecraft:shift_z");
        router.add("erosion", erosion);

        router.addProperty("depth", 0);

        // Ridges with configurable scale and seed variation
        JsonObject ridges = new JsonObject();
        ridges.addProperty("type", "minecraft:shifted_noise");
        ridges.addProperty("noise", "minecraft:ridge");
        ridges.addProperty("xz_scale", getSeedVariation(planet, "ridge_scale", planet.ridgeScale, planet.ridgeScale * 0.5f));
        ridges.addProperty("y_scale", 0);
        ridges.addProperty("shift_x", "minecraft:shift_x");
        ridges.addProperty("shift_y", 0);
        ridges.addProperty("shift_z", "minecraft:shift_z");
        router.add("ridges", ridges);

        // Jaggedness for mountain peak generation (based on Ad Astra research)
        if (planet.jaggednessScale > 0.0f) {
            JsonObject jaggedness = new JsonObject();
            jaggedness.addProperty("type", "minecraft:shifted_noise");
            jaggedness.addProperty("noise", "minecraft:jagged");
            jaggedness.addProperty("xz_scale", planet.jaggednessNoiseScale);
            jaggedness.addProperty("y_scale", 0);
            jaggedness.addProperty("shift_x", "minecraft:shift_x");
            jaggedness.addProperty("shift_y", 0);
            jaggedness.addProperty("shift_z", "minecraft:shift_z");
            router.add("jaggedness", jaggedness);
        }

        // Initial density with configurable height variations
        router.add("initial_density_without_jaggedness", createInitialDensity(planet));

        // Final density with configurable height variations
        router.add("final_density", createFinalDensity(planet));

        // Configurable ore vein settings
        router.addProperty("vein_toggle", planet.veinToggle);
        router.addProperty("vein_ridged", planet.veinRidged);
        router.addProperty("vein_gap", planet.veinGap);

        return router;
    }

    /**
     * Create initial density using Moon's pattern with configurable parameters
     */
    private static JsonObject createInitialDensity(PlanetBuilder planet) {
        JsonObject initialDensity = new JsonObject();
        initialDensity.addProperty("type", "minecraft:add");

        JsonObject argument1 = new JsonObject();
        argument1.addProperty("type", "minecraft:mul");

        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_y", planet.gradientFromY);
        yGradient.addProperty("to_y", planet.gradientToY);
        yGradient.addProperty("from_value", planet.gradientFromValue);
        yGradient.addProperty("to_value", planet.gradientToValue);
        argument1.add("argument1", yGradient);

        argument1.addProperty("argument2", planet.gradientMultiplier);
        initialDensity.add("argument1", argument1);

        JsonObject argument2 = new JsonObject();
        argument2.addProperty("type", "minecraft:add");
        argument2.addProperty("argument1", planet.initialDensityOffset + planet.depthOffset);

        JsonObject mulArgument2 = new JsonObject();
        mulArgument2.addProperty("type", "minecraft:mul");

        JsonObject addArgument1 = new JsonObject();
        addArgument1.addProperty("type", "minecraft:add");

        JsonObject mulContinentalness = new JsonObject();
        mulContinentalness.addProperty("type", "minecraft:mul");

        JsonObject continentalnessNoise = new JsonObject();
        continentalnessNoise.addProperty("type", "minecraft:shifted_noise");
        continentalnessNoise.addProperty("noise", "minecraft:continentalness");
        continentalnessNoise.addProperty("xz_scale", 0.25);
        continentalnessNoise.addProperty("y_scale", 0);
        continentalnessNoise.addProperty("shift_x", "minecraft:shift_x");
        continentalnessNoise.addProperty("shift_y", 0);
        continentalnessNoise.addProperty("shift_z", "minecraft:shift_z");
        mulContinentalness.add("argument1", continentalnessNoise);

        mulContinentalness.addProperty("argument2", getSeedVariation(planet, "height_var1", planet.heightVariation1 * planet.depthFactor * planet.terrainFactor, planet.heightVariation1 * 0.25f));
        addArgument1.add("argument2", mulContinentalness);

        JsonObject mulErosion = new JsonObject();
        mulErosion.addProperty("type", "minecraft:mul");

        JsonObject erosionNoise = new JsonObject();
        erosionNoise.addProperty("type", "minecraft:shifted_noise");
        erosionNoise.addProperty("noise", "minecraft:erosion");
        erosionNoise.addProperty("xz_scale", 0.25);
        erosionNoise.addProperty("y_scale", 0);
        erosionNoise.addProperty("shift_x", "minecraft:shift_x");
        erosionNoise.addProperty("shift_y", 0);
        erosionNoise.addProperty("shift_z", "minecraft:shift_z");
        mulErosion.add("argument1", erosionNoise);

        mulErosion.addProperty("argument2", getSeedVariation(planet, "height_var2", planet.heightVariation2 * planet.depthFactor * planet.terrainFactor, planet.heightVariation2 * 0.25f));
        addArgument1.add("argument1", mulErosion);

        mulArgument2.add("argument1", addArgument1);
        mulArgument2.addProperty("argument2", planet.terrainShapingFactor);

        argument2.add("argument2", mulArgument2);
        initialDensity.add("argument2", argument2);

        return initialDensity;
    }

    /**
     * Create cave density functions for carving (vanilla-accurate implementation)
     * Cave density should be NEGATIVE where caves form, positive where solid.
     * The final_density uses min() operation to carve caves into terrain.
     */
    private static JsonObject createCaveDensity(PlanetBuilder planet) {
        if (planet.caveFrequency <= 0) {
            // No caves - return very high positive value (always solid)
            JsonObject noCaves = new JsonObject();
            noCaves.addProperty("type", "minecraft:constant");
            noCaves.addProperty("argument", 64.0);  // High positive = no carving
            return noCaves;
        }

        // Build cave system using vanilla-style min/max combinations
        JsonObject caveSystem = new JsonObject();

        // Start with the innermost cave type and work outward
        JsonObject currentLayer = null;

        // Cheese caves (large open caverns) - vanilla-accurate
        if (planet.enableCheeseCaves) {
            JsonObject cheese = new JsonObject();
            cheese.addProperty("type", "minecraft:add");

            // Base threshold for cheese caves (vanilla uses 0.27)
            // IMPORTANT: Higher threshold = FEWER caves (noise must exceed threshold to create air)
            // For sparse caves, push threshold very close to 1.0 (noise range is -1 to 1)
            double cheeseThreshold = 0.27 + (1.0 - planet.caveFrequency) * 0.7;  // 0.27 at freq=1.0, up to 0.97 at freq=0.0
            cheese.addProperty("argument1", cheeseThreshold);

            // Cheese noise - VANILLA-ACCURATE y_scale = 0.6666...
            // Apply caveYScale for vertical stretching effects (lower = more vertical)
            JsonObject cheeseNoise = new JsonObject();
            cheeseNoise.addProperty("type", "minecraft:noise");
            cheeseNoise.addProperty("noise", "minecraft:cave_cheese");
            // FIXED: xz_scale should NOT be affected by frequency (only size)
            // Frequency affects threshold, not scale!
            cheeseNoise.addProperty("xz_scale", 1.0 / planet.caveSize);
            double cheeseYScale = 0.6666666666666666 / planet.caveSize;
            // Apply vertical scaling for non-vanilla presets (lower caveYScale = more vertical stretching)
            if (planet.caveYScale != 0.5f) {
                cheeseYScale = cheeseYScale * (planet.caveYScale / 0.5);
            }
            cheeseNoise.addProperty("y_scale", cheeseYScale);
            cheese.add("argument2", cheeseNoise);

            // Clamp cheese caves between -1 and 1
            JsonObject clampedCheese = new JsonObject();
            clampedCheese.addProperty("type", "minecraft:clamp");
            clampedCheese.add("input", cheese);
            clampedCheese.addProperty("min", -1.0);
            clampedCheese.addProperty("max", 1.0);

            currentLayer = clampedCheese;
        }

        // Spaghetti caves (winding tunnels) - combine with cheese using min
        if (planet.enableSpaghettiCaves) {
            JsonObject spaghettiOuter = new JsonObject();
            spaghettiOuter.addProperty("type", "minecraft:add");

            // Add threshold for spaghetti caves (vanilla has implicit threshold through combination)
            // For rare caves, push threshold toward positive values
            double spaghettiThreshold = -0.05 + (1.0 - planet.caveFrequency) * 0.15;  // -0.05 at freq=1.0, 0.10 at freq=0.0
            spaghettiOuter.addProperty("argument1", spaghettiThreshold);

            JsonObject spaghetti = new JsonObject();
            spaghetti.addProperty("type", "minecraft:add");

            // VANILLA-ACCURATE spaghetti_2d noise
            // Apply caveYScale for vertical stretching effects
            JsonObject spaghettiNoise = new JsonObject();
            spaghettiNoise.addProperty("type", "minecraft:noise");
            spaghettiNoise.addProperty("noise", "minecraft:spaghetti_2d");
            // FIXED: xz_scale should NOT be affected by frequency (only size)
            spaghettiNoise.addProperty("xz_scale", 1.0 / planet.caveSize);
            double spaghettiYScale = 2.0 / planet.caveSize;  // Vanilla uses 2.0
            if (planet.caveYScale != 0.5f) {
                spaghettiYScale = spaghettiYScale * (planet.caveYScale / 0.5);
            }
            spaghettiNoise.addProperty("y_scale", spaghettiYScale);
            spaghetti.add("argument1", spaghettiNoise);

            // VANILLA-ACCURATE spaghetti_roughness noise
            JsonObject spaghettiRoughness = new JsonObject();
            spaghettiRoughness.addProperty("type", "minecraft:noise");
            spaghettiRoughness.addProperty("noise", "minecraft:spaghetti_roughness");
            // FIXED: xz_scale should NOT be affected by frequency (only size)
            spaghettiRoughness.addProperty("xz_scale", 1.0 / planet.caveSize);
            spaghettiRoughness.addProperty("y_scale", spaghettiYScale);  // Same as spaghetti_2d
            spaghetti.add("argument2", spaghettiRoughness);

            spaghettiOuter.add("argument2", spaghetti);

            // Combine with previous layer
            if (currentLayer != null) {
                JsonObject combined = new JsonObject();
                combined.addProperty("type", "minecraft:min");
                combined.add("argument1", currentLayer);
                combined.add("argument2", spaghettiOuter);
                currentLayer = combined;
            } else {
                currentLayer = spaghettiOuter;
            }
        }

        // Noodle caves (thin winding tunnels) - vanilla-accurate with ridge noise
        if (planet.enableNoodleCaves) {
            JsonObject noodle = new JsonObject();
            noodle.addProperty("type", "minecraft:add");

            // Base thickness (vanilla: -0.075)
            // IMPORTANT: More negative = EASIER to form caves (inverse logic!)
            // For rare caves, push toward positive values
            double noodleThreshold = -0.075 + (1.0 - planet.caveFrequency) * 0.15;  // -0.075 at freq=1.0, 0.075 at freq=0.0
            noodle.addProperty("argument1", noodleThreshold);

            // Ridge-based noodle generation (vanilla uses two ridge noises)
            JsonObject noodleRidges = new JsonObject();
            noodleRidges.addProperty("type", "minecraft:mul");
            noodleRidges.addProperty("argument1", 1.5);  // Vanilla multiplier

            JsonObject ridgeA = new JsonObject();
            ridgeA.addProperty("type", "minecraft:abs");
            // VANILLA-ACCURATE noodle noise (y_scale: 5.333...)
            // Apply caveYScale for vertical stretching effects
            JsonObject ridgeANoise = new JsonObject();
            ridgeANoise.addProperty("type", "minecraft:noise");
            ridgeANoise.addProperty("noise", "minecraft:noodle");
            // FIXED: xz_scale should NOT be affected by frequency (only size)
            ridgeANoise.addProperty("xz_scale", 2.6666666666666665 / planet.caveSize);  // Vanilla xz_scale
            double noodleYScale = 5.333333333333333 / planet.caveSize;  // Vanilla y_scale (2x xz_scale)
            if (planet.caveYScale != 0.5f) {
                noodleYScale = noodleYScale * (planet.caveYScale / 0.5);
            }
            ridgeANoise.addProperty("y_scale", noodleYScale);
            ridgeA.add("argument", ridgeANoise);

            noodleRidges.add("argument2", ridgeA);
            noodle.add("argument2", noodleRidges);

            // Combine with previous layer
            if (currentLayer != null) {
                JsonObject combined = new JsonObject();
                combined.addProperty("type", "minecraft:min");
                combined.add("argument1", currentLayer);
                combined.add("argument2", noodle);
                currentLayer = combined;
            } else {
                currentLayer = noodle;
            }
        }

        // Apply Y-range limits if configured
        if (planet.caveMinY > -64 || planet.caveMaxY < 320) {
            JsonObject rangeChoice = new JsonObject();
            rangeChoice.addProperty("type", "minecraft:range_choice");
            rangeChoice.addProperty("input", "minecraft:y");
            rangeChoice.addProperty("min_inclusive", (double) planet.caveMinY);
            rangeChoice.addProperty("max_exclusive", (double) planet.caveMaxY);
            rangeChoice.add("when_in_range", currentLayer != null ? currentLayer : new JsonObject());
            rangeChoice.addProperty("when_out_of_range", 64.0);  // No caves outside range
            currentLayer = rangeChoice;
        }

        return currentLayer != null ? currentLayer : createNoCaveDensity();
    }

    /**
     * Create a density function that prevents all cave generation
     */
    private static JsonObject createNoCaveDensity() {
        JsonObject noCaves = new JsonObject();
        noCaves.addProperty("type", "minecraft:constant");
        noCaves.addProperty("argument", 64.0);
        return noCaves;
    }

    /**
     * Create final density using Moon's pattern with configurable parameters and cave carving
     */
    private static JsonObject createFinalDensity(PlanetBuilder planet) {
        JsonObject finalDensity = new JsonObject();
        finalDensity.addProperty("type", "minecraft:interpolated");

        JsonObject argument = new JsonObject();
        argument.addProperty("type", "minecraft:blend_density");

        // Create base terrain density (without caves)
        JsonObject blendArgument = new JsonObject();
        blendArgument.addProperty("type", "minecraft:add");

        JsonObject argument1 = new JsonObject();
        argument1.addProperty("type", "minecraft:mul");

        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_y", planet.gradientFromY);
        yGradient.addProperty("to_y", planet.gradientToY);
        yGradient.addProperty("from_value", planet.gradientFromValue);
        yGradient.addProperty("to_value", planet.gradientToValue);
        argument1.add("argument1", yGradient);

        argument1.addProperty("argument2", planet.gradientMultiplier);
        blendArgument.add("argument1", argument1);

        JsonObject argument2 = new JsonObject();
        argument2.addProperty("type", "minecraft:add");
        argument2.addProperty("argument1", planet.initialDensityOffset + planet.depthOffset);

        JsonObject mulArgument2 = new JsonObject();
        mulArgument2.addProperty("type", "minecraft:mul");

        JsonObject addArgument1 = new JsonObject();
        addArgument1.addProperty("type", "minecraft:add");

        JsonObject mulContinentalness = new JsonObject();
        mulContinentalness.addProperty("type", "minecraft:mul");

        JsonObject continentalnessNoise = new JsonObject();
        continentalnessNoise.addProperty("type", "minecraft:shifted_noise");
        continentalnessNoise.addProperty("noise", "minecraft:continentalness");
        continentalnessNoise.addProperty("xz_scale", 0.25);
        continentalnessNoise.addProperty("y_scale", 0);
        continentalnessNoise.addProperty("shift_x", "minecraft:shift_x");
        continentalnessNoise.addProperty("shift_y", 0);
        continentalnessNoise.addProperty("shift_z", "minecraft:shift_z");
        mulContinentalness.add("argument1", continentalnessNoise);

        mulContinentalness.addProperty("argument2", getSeedVariation(planet, "height_var3", planet.heightVariation3, planet.heightVariation3 * 0.25f));
        addArgument1.add("argument2", mulContinentalness);

        JsonObject mulErosion = new JsonObject();
        mulErosion.addProperty("type", "minecraft:mul");

        JsonObject erosionNoise = new JsonObject();
        erosionNoise.addProperty("type", "minecraft:shifted_noise");
        erosionNoise.addProperty("noise", "minecraft:erosion");
        erosionNoise.addProperty("xz_scale", 0.25);
        erosionNoise.addProperty("y_scale", 0);
        erosionNoise.addProperty("shift_x", "minecraft:shift_x");
        erosionNoise.addProperty("shift_y", 0);
        erosionNoise.addProperty("shift_z", "minecraft:shift_z");
        mulErosion.add("argument1", erosionNoise);

        mulErosion.addProperty("argument2", getSeedVariation(planet, "height_var4", planet.heightVariation4, planet.heightVariation4 * 0.25f));
        addArgument1.add("argument1", mulErosion);

        mulArgument2.add("argument1", addArgument1);
        mulArgument2.addProperty("argument2", planet.terrainShapingFactor);

        argument2.add("argument2", mulArgument2);
        blendArgument.add("argument2", argument2);

        // Carve caves using vanilla min() pattern
        if (planet.caveFrequency > 0 &&
            (planet.enableCheeseCaves || planet.enableSpaghettiCaves || planet.enableNoodleCaves)) {

            // Use min operation to carve caves (vanilla pattern)
            // Cave density is already negative where caves form
            JsonObject withCaves = new JsonObject();
            withCaves.addProperty("type", "minecraft:min");
            withCaves.add("argument1", blendArgument);  // Terrain density
            withCaves.add("argument2", createCaveDensity(planet));  // Cave density (negative = air)

            argument.add("argument", withCaves);
        } else {
            argument.add("argument", blendArgument);
        }

        finalDensity.add("argument", argument);

        return finalDensity;
    }

    /**
     * Create surface rule using Moon's proven pattern with configurable blocks
     */
    private static JsonObject createSurfaceRule(PlanetBuilder planet) {
        JsonObject surfaceRule = new JsonObject();
        surfaceRule.addProperty("type", "minecraft:sequence");

        JsonArray sequence = new JsonArray();

        // Bedrock floor generation - place bedrock at the bottom of the world
        JsonObject bedrockFloor = new JsonObject();
        bedrockFloor.addProperty("type", "minecraft:condition");

        JsonObject bedrockCondition = new JsonObject();
        bedrockCondition.addProperty("type", "minecraft:vertical_gradient");
        bedrockCondition.addProperty("random_name", "minecraft:bedrock_floor");

        // Wrap Y values in absolute objects
        JsonObject trueAtAndBelow = new JsonObject();
        trueAtAndBelow.addProperty("absolute", planet.minY);
        bedrockCondition.add("true_at_and_below", trueAtAndBelow);

        JsonObject falseAtAndAbove = new JsonObject();
        falseAtAndAbove.addProperty("absolute", planet.minY + 5);
        bedrockCondition.add("false_at_and_above", falseAtAndAbove);

        bedrockFloor.add("if_true", bedrockCondition);

        JsonObject bedrockResult = new JsonObject();
        bedrockResult.addProperty("type", "minecraft:block");
        JsonObject bedrockState = new JsonObject();
        bedrockState.addProperty("Name", planet.bedrockBlock);
        bedrockResult.add("result_state", bedrockState);
        bedrockFloor.add("then_run", bedrockResult);

        sequence.add(bedrockFloor);

        // Block prevention rules - prevent unwanted default blocks
        if (planet.preventGrassGeneration) {
            addBlockPreventionRule(sequence, "minecraft:grass_block", planet.surfaceBlock);
            addBlockPreventionRule(sequence, "minecraft:dirt", planet.surfaceBlock);
        }

        if (planet.preventGravelGeneration) {
            addBlockPreventionRule(sequence, "minecraft:gravel", planet.subsurfaceBlock);
        }

        if (planet.preventSandGeneration) {
            addBlockPreventionRule(sequence, "minecraft:sand", planet.surfaceBlock);
            addBlockPreventionRule(sequence, "minecraft:red_sand", planet.surfaceBlock);
        }

        // Note: Ore generation should happen through configured features, not surface rules
        // Surface rules are only for surface/subsurface blocks

        // Surface layer (top block) - above preliminary surface
        JsonObject surfaceLayer = new JsonObject();
        surfaceLayer.addProperty("type", "minecraft:condition");

        JsonObject surfaceCondition = new JsonObject();
        surfaceCondition.addProperty("type", "minecraft:above_preliminary_surface");
        surfaceLayer.add("if_true", surfaceCondition);

        JsonObject surfaceResult = new JsonObject();
        surfaceResult.addProperty("type", "minecraft:block");
        JsonObject surfaceState = new JsonObject();
        surfaceState.addProperty("Name", planet.surfaceBlock);
        surfaceResult.add("result_state", surfaceState);
        surfaceLayer.add("then_run", surfaceResult);

        sequence.add(surfaceLayer);

        // Underwater/subsurface layer
        JsonObject subsurfaceLayer = new JsonObject();
        subsurfaceLayer.addProperty("type", "minecraft:condition");

        JsonObject subsurfaceCondition = new JsonObject();
        subsurfaceCondition.addProperty("type", "minecraft:water");
        subsurfaceCondition.addProperty("offset", -1);
        subsurfaceCondition.addProperty("surface_depth_multiplier", 0);
        subsurfaceCondition.addProperty("add_stone_depth", false);
        subsurfaceLayer.add("if_true", subsurfaceCondition);

        JsonObject subsurfaceResult = new JsonObject();
        subsurfaceResult.addProperty("type", "minecraft:block");
        JsonObject subsurfaceState = new JsonObject();
        subsurfaceState.addProperty("Name", planet.underwaterBlock);
        subsurfaceResult.add("result_state", subsurfaceState);
        subsurfaceLayer.add("then_run", subsurfaceResult);

        sequence.add(subsurfaceLayer);

        // Only add deep layer as a surface rule if it's different from defaultBlock
        // This allows the defaultBlock to be used for the main stone layer
        if (!planet.deepBlock.equals(planet.defaultBlock)) {
            // Deep layer - only for actual deep regions (below Y=0 or similar)
            JsonObject deepLayer = new JsonObject();
            deepLayer.addProperty("type", "minecraft:condition");

            JsonObject deepCondition = new JsonObject();
            deepCondition.addProperty("type", "minecraft:vertical_gradient");
            deepCondition.addProperty("random_name", "minecraft:deep_layer");

            // Deep layer starts below Y=0
            JsonObject deepBelow = new JsonObject();
            deepBelow.addProperty("absolute", 0);
            deepCondition.add("true_at_and_below", deepBelow);

            JsonObject deepAbove = new JsonObject();
            deepAbove.addProperty("absolute", 8);
            deepCondition.add("false_at_and_above", deepAbove);

            deepLayer.add("if_true", deepCondition);

            JsonObject deepResult = new JsonObject();
            deepResult.addProperty("type", "minecraft:block");
            JsonObject deepState = new JsonObject();
            deepState.addProperty("Name", planet.deepBlock);
            deepResult.add("result_state", deepState);
            deepLayer.add("then_run", deepResult);

            sequence.add(deepLayer);
        }

        surfaceRule.add("sequence", sequence);
        return surfaceRule;
    }

    /**
     * Helper method to add block prevention rules that override default generation
     * Creates a universal rule that always replaces with the specified block
     */
    private static void addBlockPreventionRule(JsonArray sequence, String blockToPrevent, String replacementBlock) {
        JsonObject preventionRule = new JsonObject();
        preventionRule.addProperty("type", "minecraft:condition");

        // Create a surface depth condition with proper surface_type
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "minecraft:stone_depth");
        condition.addProperty("offset", 0);
        condition.addProperty("surface_type", "floor");
        condition.addProperty("add_surface_depth", false);
        condition.addProperty("secondary_depth_range", 0);
        preventionRule.add("if_true", condition);

        // Replace with our desired block
        JsonObject result = new JsonObject();
        result.addProperty("type", "minecraft:block");
        JsonObject state = new JsonObject();
        state.addProperty("Name", replacementBlock);
        result.add("result_state", state);
        preventionRule.add("then_run", result);

        sequence.add(preventionRule);
    }

    /**
     * Helper method to add ore vein rules for custom ore placement with noise-based rarity
     */
    private static void addOreVeinRule(JsonArray sequence, String oreBlock, PlanetBuilder planet) {
        JsonObject oreRule = new JsonObject();
        oreRule.addProperty("type", "minecraft:condition");

        // Create complex condition using noise for realistic ore distribution
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "minecraft:noise_threshold");
        condition.addProperty("noise", "minecraft:ore_gap"); // Use ore gap noise for realistic distribution
        condition.addProperty("min_threshold", getOreThreshold(oreBlock, planet)); // Rarity based on ore type
        condition.addProperty("max_threshold", 1.0);
        oreRule.add("if_true", condition);

        // Create nested condition for depth requirement
        JsonObject depthCondition = new JsonObject();
        depthCondition.addProperty("type", "minecraft:condition");

        JsonObject stoneDepth = new JsonObject();
        stoneDepth.addProperty("type", "minecraft:stone_depth");
        stoneDepth.addProperty("offset", getOreDepth(oreBlock)); // Different depths for different ores
        stoneDepth.addProperty("surface_type", "floor");
        stoneDepth.addProperty("add_surface_depth", false);
        stoneDepth.addProperty("secondary_depth_range", (int)(16 * planet.oreVeinSize)); // Smaller vein range
        depthCondition.add("if_true", stoneDepth);

        // Place the ore block
        JsonObject result = new JsonObject();
        result.addProperty("type", "minecraft:block");
        JsonObject state = new JsonObject();
        state.addProperty("Name", oreBlock);
        result.add("result_state", state);
        depthCondition.add("then_run", result);

        oreRule.add("then_run", depthCondition);
        sequence.add(oreRule);
    }

    /**
     * Get ore rarity threshold - higher values = rarer ores
     */
    private static double getOreThreshold(String oreBlock, PlanetBuilder planet) {
        double baseThreshold = switch (oreBlock) {
            case "minecraft:diamond_ore" -> 0.92; // Very rare
            case "minecraft:emerald_ore" -> 0.94; // Very rare
            case "minecraft:ancient_debris" -> 0.96; // Extremely rare
            case "minecraft:gold_ore" -> 0.85; // Rare
            case "minecraft:iron_ore" -> 0.7; // Common
            case "minecraft:copper_ore" -> 0.65; // Common
            case "minecraft:redstone_ore" -> 0.75; // Uncommon
            case "minecraft:lapis_ore" -> 0.8; // Uncommon
            default -> 0.9; // Default rare
        };

        // Adjust for planet ore density setting
        return Math.max(0.1, baseThreshold - (planet.oreVeinDensity - 1.0) * 0.1);
    }

    /**
     * Get ore depth offset - different ores at different depths
     */
    private static int getOreDepth(String oreBlock) {
        return switch (oreBlock) {
            case "minecraft:diamond_ore", "minecraft:ancient_debris" -> 16; // Deep
            case "minecraft:emerald_ore", "minecraft:gold_ore" -> 12; // Medium-deep
            case "minecraft:iron_ore", "minecraft:copper_ore" -> 8; // Medium
            case "minecraft:redstone_ore", "minecraft:lapis_ore" -> 10; // Medium
            default -> 8; // Default medium depth
        };
    }

    private static String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Get the proper ore block name for a given ore type
     * Handles both vanilla and modded ores
     */
    private static String getOreBlockName(String oreType) {
        return switch (oreType.toLowerCase()) {
            // Vanilla ores - these are always available
            case "iron", "gold", "copper", "coal", "diamond", "emerald", "lapis", "redstone" ->
                "minecraft:" + oreType + "_ore";

            // Mekanism ores - these ARE available since Mekanism is installed
            case "osmium" -> "mekanism:osmium_ore";
            case "tin" -> "mekanism:tin_ore";
            case "uranium" -> "mekanism:uranium_ore";
            case "fluorite" -> "mekanism:fluorite_ore";
            case "lead" -> "mekanism:lead_ore";

            // Immersive Engineering ores - NOT installed, use vanilla substitutes
            case "aluminum", "bauxite" -> "minecraft:iron_ore";  // Common like iron
            case "silver" -> "minecraft:gold_ore";   // Precious like gold
            case "nickel" -> "minecraft:copper_ore"; // Similar to copper

            // Other modded ores - use vanilla substitutes
            case "zinc" -> "minecraft:iron_ore";
            case "platinum" -> "minecraft:diamond_ore";
            case "tungsten" -> "minecraft:iron_ore";

            // Default fallback
            default -> "minecraft:" + oreType + "_ore";
        };
    }

    private static void writeJsonFile(String path, JsonObject json) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            GSON.toJson(json, writer);
        }
    }

    /**
     * Generate ore feature configurations for a planet
     */
    private static void generateOreFeatures(PlanetBuilder planet) throws IOException {
        AdAstraMekanized.LOGGER.info("Generating ore features for planet: {}", planet.name);

        // Generate ore features for all configured ore types
        // If specific ores are configured, use those; otherwise use defaults
        java.util.Set<String> oreTypes;
        if (!planet.oreVeinCounts.isEmpty()) {
            oreTypes = planet.oreVeinCounts.keySet();
            AdAstraMekanized.LOGGER.info("Generating configured ores: {}", oreTypes);
        } else {
            // Comprehensive list of vanilla and modded ores
            // This includes vanilla ores plus common modded ores from Mekanism, IE, etc.
            oreTypes = java.util.Set.of(
                // Vanilla ores
                "iron", "copper", "gold", "diamond", "coal", "redstone", "lapis", "emerald",
                // Mekanism ores
                "osmium", "tin", "uranium", "fluorite", "lead",
                // Immersive Engineering ores
                "aluminum", "silver", "nickel", "bauxite",
                // Other common modded ores
                "zinc", "platinum", "tungsten"
            );
            AdAstraMekanized.LOGGER.info("No ores configured, using comprehensive ore list: {}", oreTypes);
        }

        for (String ore : oreTypes) {
            // Generate simplified configured feature
            JsonObject configuredFeature = createSimplifiedOreConfiguredFeature(ore, planet);
            String configuredPath = RESOURCES_PATH + "worldgen/configured_feature/" + planet.name + "_ore_" + ore + "_simple.json";
            AdAstraMekanized.LOGGER.debug("Writing configured feature to: {}", configuredPath);
            writeJsonFile(configuredPath, configuredFeature);

            // Generate simplified placed feature with high spawn rates for testing
            JsonObject placedFeature = createSimplifiedOrePlacedFeature(ore, planet);
            String placedPath = RESOURCES_PATH + "worldgen/placed_feature/" + planet.name + "_ore_" + ore + "_simple.json";
            AdAstraMekanized.LOGGER.debug("Writing placed feature to: {}", placedPath);
            writeJsonFile(placedPath, placedFeature);
        }

        AdAstraMekanized.LOGGER.info("Completed generating ore features for planet: {}", planet.name);
    }

    /**
     * Create simplified configured feature following Dimension Expansion's pattern
     */
    private static JsonObject createSimplifiedOreConfiguredFeature(String oreType, PlanetBuilder planet) {
        JsonObject feature = new JsonObject();
        feature.addProperty("type", "minecraft:ore");

        JsonObject config = new JsonObject();
        JsonArray targets = new JsonArray();

        String oreBlockName = getOreBlockName(oreType);

        // Target solid blocks using tag - this excludes air, water, lava etc.
        JsonObject solidTarget = new JsonObject();
        JsonObject solidPredicate = new JsonObject();
        solidPredicate.addProperty("predicate_type", "minecraft:tag_match");
        solidPredicate.addProperty("tag", "minecraft:stone_ore_replaceables");
        solidTarget.add("target", solidPredicate);
        JsonObject solidState = new JsonObject();
        solidState.addProperty("Name", oreBlockName);
        solidTarget.add("state", solidState);
        targets.add(solidTarget);

        // Add deepslate ore replaceables
        JsonObject deepslateTarget = new JsonObject();
        JsonObject deepslatePredicate = new JsonObject();
        deepslatePredicate.addProperty("predicate_type", "minecraft:tag_match");
        deepslatePredicate.addProperty("tag", "minecraft:deepslate_ore_replaceables");
        deepslateTarget.add("target", deepslatePredicate);
        JsonObject deepslateState = new JsonObject();
        deepslateState.addProperty("Name", oreBlockName);
        deepslateTarget.add("state", deepslateState);
        targets.add(deepslateTarget);

        // Now add specific blocks that aren't in the standard tags
        // This gives us the flexibility we want - ores can spawn in any terrain type
        String[] additionalBlocks = {
            "minecraft:dirt", "minecraft:gravel", "minecraft:sand", "minecraft:sandstone",
            "minecraft:terracotta", "minecraft:white_terracotta", "minecraft:orange_terracotta",
            "minecraft:magenta_terracotta", "minecraft:light_blue_terracotta", "minecraft:yellow_terracotta",
            "minecraft:lime_terracotta", "minecraft:pink_terracotta", "minecraft:gray_terracotta",
            "minecraft:light_gray_terracotta", "minecraft:cyan_terracotta", "minecraft:purple_terracotta",
            "minecraft:blue_terracotta", "minecraft:brown_terracotta", "minecraft:green_terracotta",
            "minecraft:red_terracotta", "minecraft:black_terracotta",
            "minecraft:netherrack", "minecraft:basalt", "minecraft:blackstone", "minecraft:end_stone",
            "minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium", "minecraft:coarse_dirt",
            "minecraft:red_sand", "minecraft:red_sandstone", "minecraft:clay", "minecraft:packed_ice",
            "minecraft:blue_ice", "minecraft:snow_block", "minecraft:powder_snow"
        };

        for (String block : additionalBlocks) {
            JsonObject target = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("predicate_type", "minecraft:block_match");
            predicate.addProperty("block", block);
            target.add("target", predicate);
            JsonObject state = new JsonObject();
            state.addProperty("Name", oreBlockName);
            target.add("state", state);
            targets.add(target);
        }

        // Add planet-specific blocks if they exist and aren't already covered
        if (!planet.subsurfaceBlock.isEmpty() && !planet.subsurfaceBlock.contains("air")) {
            JsonObject subsurfaceTarget = new JsonObject();
            JsonObject subsurfacePredicate = new JsonObject();
            subsurfacePredicate.addProperty("predicate_type", "minecraft:block_match");
            subsurfacePredicate.addProperty("block", planet.subsurfaceBlock);
            subsurfaceTarget.add("target", subsurfacePredicate);
            JsonObject subsurfaceState = new JsonObject();
            subsurfaceState.addProperty("Name", oreBlockName);
            subsurfaceTarget.add("state", subsurfaceState);
            targets.add(subsurfaceTarget);
        }

        if (!planet.deepBlock.isEmpty() && !planet.deepBlock.contains("air")) {
            JsonObject deepTarget = new JsonObject();
            JsonObject deepPredicate = new JsonObject();
            deepPredicate.addProperty("predicate_type", "minecraft:block_match");
            deepPredicate.addProperty("block", planet.deepBlock);
            deepTarget.add("target", deepPredicate);
            JsonObject deepState = new JsonObject();
            deepState.addProperty("Name", oreBlockName);
            deepTarget.add("state", deepState);
            targets.add(deepTarget);
        }

        if (!planet.defaultBlock.isEmpty() && !planet.defaultBlock.contains("air")) {
            JsonObject defaultTarget = new JsonObject();
            JsonObject defaultPredicate = new JsonObject();
            defaultPredicate.addProperty("predicate_type", "minecraft:block_match");
            defaultPredicate.addProperty("block", planet.defaultBlock);
            defaultTarget.add("target", defaultPredicate);
            JsonObject defaultState = new JsonObject();
            defaultState.addProperty("Name", oreBlockName);
            defaultTarget.add("state", defaultState);
            targets.add(defaultTarget);
        }

        config.add("targets", targets);
        config.addProperty("size", oreType.equals("diamond") ? 8 : 12);
        config.addProperty("discard_chance_on_air_exposure", 0.99f);  // 99% chance to skip when exposed to air

        feature.add("config", config);
        return feature;
    }

    /**
     * Create simplified placed feature with configurable spawn rates
     */
    private static JsonObject createSimplifiedOrePlacedFeature(String oreType, PlanetBuilder planet) {
        JsonObject feature = new JsonObject();
        feature.addProperty("feature", "adastramekanized:" + planet.name + "_ore_" + oreType + "_simple");

        JsonArray placement = new JsonArray();

        // Count placement - use configured count or default
        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:count");

        // Use custom vein count if configured, otherwise use defaults
        int veinCount;
        if (planet.oreVeinCounts.containsKey(oreType)) {
            veinCount = planet.oreVeinCounts.get(oreType);
        } else {
            // Default vein counts per ore type
            switch (oreType) {
                case "diamond":
                    veinCount = 1;  // Rare
                    break;
                case "gold":
                    veinCount = 2;  // Uncommon
                    break;
                case "iron":
                    veinCount = 10; // Common
                    break;
                case "copper":
                    veinCount = 8;  // Common
                    break;
                case "coal":
                    veinCount = 12; // Very common
                    break;
                case "redstone":
                    veinCount = 4;  // Uncommon
                    break;
                case "lapis":
                    veinCount = 2;  // Rare
                    break;
                case "emerald":
                    veinCount = 1;  // Very rare
                    break;
                // Mekanism ores
                case "osmium":
                    veinCount = 8;  // Common like iron
                    break;
                case "tin":
                    veinCount = 6;  // Fairly common
                    break;
                case "uranium":
                    veinCount = 2;  // Rare
                    break;
                case "fluorite":
                    veinCount = 4;  // Uncommon
                    break;
                case "lead":
                    veinCount = 5;  // Medium
                    break;
                // Immersive Engineering ores
                case "aluminum":
                case "bauxite":
                    veinCount = 7;  // Common
                    break;
                case "silver":
                    veinCount = 3;  // Uncommon
                    break;
                case "nickel":
                    veinCount = 5;  // Medium
                    break;
                default:
                    veinCount = 5;  // Default for unknown ores
            }
        }

        count.addProperty("count", veinCount);
        placement.add(count);

        // In square placement
        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);

        // Height range
        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");
        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:trapezoid");

        JsonObject minHeight = new JsonObject();
        minHeight.addProperty("absolute", planet.minY);
        height.add("min_inclusive", minHeight);

        JsonObject maxHeight = new JsonObject();
        maxHeight.addProperty("absolute", oreType.equals("diamond") ? 16 : 256);
        height.add("max_inclusive", maxHeight);

        heightRange.add("height", height);
        placement.add(heightRange);

        // Biome filter
        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);

        feature.add("placement", placement);
        return feature;
    }

    /**
     * OLD: Create configured feature for an ore type (keeping for reference)
     */
    private static JsonObject createOreConfiguredFeature_OLD(String oreType, PlanetBuilder planet) {
        JsonObject feature = new JsonObject();
        feature.addProperty("type", "minecraft:ore");

        JsonObject config = new JsonObject();

        // Ore targets with size
        JsonArray targets = new JsonArray();

        // Check if planet uses custom stone blocks (not vanilla stone/dirt/etc)
        boolean useCustomBlocks = (!planet.subsurfaceBlock.equals("minecraft:stone") &&
                                   !planet.subsurfaceBlock.equals("minecraft:dirt") &&
                                   !planet.subsurfaceBlock.equals("minecraft:grass_block")) ||
                                  (!planet.deepBlock.equals("minecraft:stone") &&
                                   !planet.deepBlock.equals("minecraft:deepslate"));

        if (useCustomBlocks) {
            // For custom planet blocks, directly target the specific block types
            // Target the subsurface block (like moon_stone, mars_stone, etc.)
            // Skip dirt/grass blocks for ore replacement
            if (!planet.subsurfaceBlock.isEmpty() &&
                !planet.subsurfaceBlock.equals("minecraft:dirt") &&
                !planet.subsurfaceBlock.equals("minecraft:grass_block")) {
                JsonObject target = new JsonObject();
                JsonObject targetPredicate = new JsonObject();
                targetPredicate.addProperty("predicate_type", "minecraft:block_match");
                targetPredicate.addProperty("block", planet.subsurfaceBlock);
                target.add("target", targetPredicate);

                JsonObject state = new JsonObject();
                state.addProperty("Name", "minecraft:" + oreType + "_ore");
                target.add("state", state);
                targets.add(target);
            }

            // Target the deep block (like moon_deepslate, etc.)
            if (!planet.deepBlock.isEmpty() && !oreType.equals("coal") && !oreType.equals("emerald")) {
                JsonObject deepTarget = new JsonObject();
                JsonObject deepPredicate = new JsonObject();
                deepPredicate.addProperty("predicate_type", "minecraft:block_match");
                deepPredicate.addProperty("block", planet.deepBlock);
                deepTarget.add("target", deepPredicate);

                JsonObject deepState = new JsonObject();
                deepState.addProperty("Name", "minecraft:deepslate_" + oreType + "_ore");
                deepTarget.add("state", deepState);
                targets.add(deepTarget);
            }

            // Also target the default block if different
            if (!planet.defaultBlock.isEmpty() && !planet.defaultBlock.equals(planet.subsurfaceBlock)) {
                JsonObject defaultTarget = new JsonObject();
                JsonObject defaultPredicate = new JsonObject();
                defaultPredicate.addProperty("predicate_type", "minecraft:block_match");
                defaultPredicate.addProperty("block", planet.defaultBlock);
                defaultTarget.add("target", defaultPredicate);

                JsonObject defaultState = new JsonObject();
                // Use deepslate ore variant if the default block is a deepslate-like block
                if (planet.defaultBlock.contains("deepslate") && !oreType.equals("coal") && !oreType.equals("emerald")) {
                    defaultState.addProperty("Name", "minecraft:deepslate_" + oreType + "_ore");
                } else {
                    defaultState.addProperty("Name", "minecraft:" + oreType + "_ore");
                }
                defaultTarget.add("state", defaultState);
                targets.add(defaultTarget);
            }
        } else {
            // For vanilla blocks, use the standard ore replaceable tags
            JsonObject target = new JsonObject();
            JsonObject targetPredicate = new JsonObject();
            targetPredicate.addProperty("predicate_type", "minecraft:tag_match");
            targetPredicate.addProperty("tag", "minecraft:stone_ore_replaceables");
            target.add("target", targetPredicate);

            JsonObject state = new JsonObject();
            state.addProperty("Name", "minecraft:" + oreType + "_ore");
            target.add("state", state);
            targets.add(target);

            // Add deepslate variant for deeper ores
            if (!oreType.equals("coal") && !oreType.equals("emerald")) {
                JsonObject deepslateTarget = new JsonObject();
                JsonObject deepslatePredicate = new JsonObject();
                deepslatePredicate.addProperty("predicate_type", "minecraft:tag_match");
                deepslatePredicate.addProperty("tag", "minecraft:deepslate_ore_replaceables");
                deepslateTarget.add("target", deepslatePredicate);

                JsonObject deepslateState = new JsonObject();
                deepslateState.addProperty("Name", "minecraft:deepslate_" + oreType + "_ore");
                deepslateTarget.add("state", deepslateState);
                targets.add(deepslateTarget);
            }
        }

        config.add("targets", targets);
        config.addProperty("size", getOreVeinSize(oreType, planet));
        config.addProperty("discard_chance_on_air_exposure", 0.5f);

        feature.add("config", config);
        return feature;
    }

    /**
     * Generate placed features for different Y levels
     */
    private static void generateOrePlacedFeatures(String oreType, PlanetBuilder planet) throws IOException {
        // Different placement strategies for different ore types
        switch (oreType) {
            case "coal":
                createPlacedFeature(oreType, planet, "upper", 136, 320, 30);
                createPlacedFeature(oreType, planet, "lower", 0, 192, 20);
                break;
            case "iron":
                createPlacedFeature(oreType, planet, "upper", 80, 320, 10);
                createPlacedFeature(oreType, planet, "middle", -24, 56, 10);
                createPlacedFeature(oreType, planet, "lower", -64, -24, 30);
                break;
            case "gold":
                createPlacedFeature(oreType, planet, "extra", -64, 32, 50);
                createPlacedFeature(oreType, planet, "lower", -64, -48, 4);
                break;
            case "diamond":
                createPlacedFeature(oreType, planet, "large", -64, -4, 7);
                createPlacedFeature(oreType, planet, "small", -64, -4, 4);
                break;
            case "redstone":
                createPlacedFeature(oreType, planet, "lower", -64, 15, 8);
                createPlacedFeature(oreType, planet, "deep", -64, -32, 2);
                break;
            case "lapis":
                createPlacedFeature(oreType, planet, "normal", -64, 64, 4);
                createPlacedFeature(oreType, planet, "deep", -64, -32, 2);
                break;
            case "copper":
                createPlacedFeature(oreType, planet, "normal", -16, 112, 16);
                createPlacedFeature(oreType, planet, "large", -16, 112, 8);
                break;
            case "emerald":
                createPlacedFeature(oreType, planet, "normal", -16, 320, 100);
                break;
        }
    }

    /**
     * Create a placed feature for ore generation
     */
    private static void createPlacedFeature(String oreType, PlanetBuilder planet,
                                           String variant, int minY, int maxY, int count) throws IOException {
        JsonObject placed = new JsonObject();
        placed.addProperty("feature", "adastramekanized:" + planet.name + "_ore_" + oreType);

        JsonArray placement = new JsonArray();

        // Count placement
        JsonObject countPlacement = new JsonObject();
        countPlacement.addProperty("type", "minecraft:count");
        countPlacement.addProperty("count", (int)(count * planet.oreVeinDensity));
        placement.add(countPlacement);

        // Square spread
        JsonObject spread = new JsonObject();
        spread.addProperty("type", "minecraft:in_square");
        placement.add(spread);

        // Height range with absolute values
        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");
        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:uniform");

        // Min Y with absolute wrapper
        JsonObject minInclusive = new JsonObject();
        minInclusive.addProperty("absolute", minY);
        height.add("min_inclusive", minInclusive);

        // Max Y with absolute wrapper
        JsonObject maxInclusive = new JsonObject();
        maxInclusive.addProperty("absolute", maxY);
        height.add("max_inclusive", maxInclusive);

        heightRange.add("height", height);
        placement.add(heightRange);

        // Biome check
        JsonObject biomeCheck = new JsonObject();
        biomeCheck.addProperty("type", "minecraft:biome");
        placement.add(biomeCheck);

        placed.add("placement", placement);

        String filename = planet.name + "_ore_" + oreType + "_" + variant + ".json";
        writeJsonFile(RESOURCES_PATH + "worldgen/placed_feature/" + filename, placed);
    }

    /**
     * Get ore vein size based on type
     */
    private static int getOreVeinSize(String oreType, PlanetBuilder planet) {
        int baseSize;
        switch (oreType) {
            case "coal": baseSize = 17; break;
            case "iron": baseSize = 9; break;
            case "gold": baseSize = 9; break;
            case "diamond": baseSize = 8; break;
            case "redstone": baseSize = 8; break;
            case "lapis": baseSize = 7; break;
            case "copper": baseSize = 10; break;
            case "emerald": baseSize = 3; break;
            default: baseSize = 8;
        }
        return (int)(baseSize * planet.oreVeinSize);
    }

    /**
     * Generate custom biomes with ore features
     */
    private static void generateCustomBiomes(PlanetBuilder planet) throws IOException {
        // For each biome in the planet, create a custom version with features
        for (PlanetBuilder.BiomeEntry biomeEntry : planet.customBiomes) {
            JsonObject biome = createCustomBiome(biomeEntry, planet);
            // Extract just the biome name for the filename
            String biomeName;
            if (biomeEntry.biomeName.contains(":")) {
                // Remove namespace for filename
                biomeName = biomeEntry.biomeName.substring(biomeEntry.biomeName.lastIndexOf(":") + 1);
            } else {
                biomeName = biomeEntry.biomeName;
            }

            // Create filename without colons (filesystem safe)
            String filename = biomeName + ".json";
            writeJsonFile(RESOURCES_PATH + "worldgen/biome/" + filename, biome);
        }
    }

    /**
     * Create a custom biome with features
     */
    private static JsonObject createCustomBiome(PlanetBuilder.BiomeEntry biomeEntry, PlanetBuilder planet) {
        JsonObject biome = new JsonObject();

        // Climate parameters
        biome.addProperty("temperature", biomeEntry.temperature);
        biome.addProperty("downfall", biomeEntry.humidity);
        biome.addProperty("has_precipitation", biomeEntry.humidity > 0);

        // Effects
        JsonObject effects = new JsonObject();
        effects.addProperty("fog_color", planet.fogColor);
        effects.addProperty("sky_color", planet.skyColor);
        effects.addProperty("water_color", 4159204);
        effects.addProperty("water_fog_color", 329011);
        biome.add("effects", effects);

        // Mob spawners - populate with configured mob spawns
        JsonObject spawners = new JsonObject();
        for (String category : new String[]{"monster", "creature", "ambient", "water_creature", "water_ambient", "misc"}) {
            JsonArray categorySpawns = new JsonArray();

            // Add configured spawns for this category
            if (planet.mobSpawns.containsKey(category)) {
                for (PlanetBuilder.MobSpawnEntry mob : planet.mobSpawns.get(category)) {
                    JsonObject spawn = new JsonObject();
                    spawn.addProperty("type", mob.mobId);
                    spawn.addProperty("minCount", mob.minCount);
                    spawn.addProperty("maxCount", mob.maxCount);
                    spawn.addProperty("weight", mob.weight);
                    categorySpawns.add(spawn);
                }
            }

            spawners.add(category, categorySpawns);
        }
        biome.add("spawners", spawners);

        // Spawn costs
        JsonObject spawnCostsObj = new JsonObject();
        for (java.util.Map.Entry<String, PlanetBuilder.SpawnCost> entry : planet.spawnCosts.entrySet()) {
            JsonObject costEntry = new JsonObject();
            costEntry.addProperty("energy_budget", entry.getValue().energyBudget);
            costEntry.addProperty("charge", entry.getValue().charge);
            spawnCostsObj.add(entry.getKey(), costEntry);
        }
        biome.add("spawn_costs", spawnCostsObj);

        // Features by generation step
        JsonArray features = new JsonArray();

        // Generation steps in order
        String[] steps = {
            "raw_generation", "lakes", "local_modifications", "underground_structures",
            "surface_structures", "strongholds", "underground_ores", "underground_decoration",
            "fluid_springs", "vegetal_decoration", "top_layer_modification"
        };

        for (String step : steps) {
            JsonArray stepFeatures = new JsonArray();

            if (step.equals("underground_ores") && planet.oreVeinsEnabled) {
                // Add ore features for all configured ore types
                if (!planet.oreVeinCounts.isEmpty()) {
                    // Add only configured ore types
                    for (String oreType : planet.oreVeinCounts.keySet()) {
                        stepFeatures.add("adastramekanized:" + planet.name + "_ore_" + oreType + "_simple");
                    }
                } else {
                    // Default ore features if none configured
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_iron_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_copper_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_gold_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_diamond_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_coal_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_redstone_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_lapis_simple");
                    stepFeatures.add("adastramekanized:" + planet.name + "_ore_emerald_simple");
                }
            }

            features.add(stepFeatures);
        }

        biome.add("features", features);

        // Carvers
        JsonObject carvers = new JsonObject();
        carvers.add("air", new JsonArray());
        biome.add("carvers", carvers);

        return biome;
    }
}