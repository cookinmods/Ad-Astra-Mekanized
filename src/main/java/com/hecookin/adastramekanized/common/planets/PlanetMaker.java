package com.hecookin.adastramekanized.common.planets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.config.*;

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

    // Vanilla Overworld density function templates for custom terrain generation
    // These are used as base templates and modified to reference planet-specific noise
    private static final String VANILLA_FINAL_DENSITY_TEMPLATE = """
{
  "type": "minecraft:min",
  "argument1": {
    "type": "minecraft:squeeze",
    "argument": {
      "type": "minecraft:mul",
      "argument1": 0.64,
      "argument2": {
        "type": "minecraft:interpolated",
        "argument": {
          "type": "minecraft:blend_density",
          "argument": {
            "type": "minecraft:add",
            "argument1": 0.1171875,
            "argument2": {
              "type": "minecraft:mul",
              "argument1": {
                "type": "minecraft:y_clamped_gradient",
                "from_value": 0.0,
                "from_y": -64,
                "to_value": 1.0,
                "to_y": -40
              },
              "argument2": {
                "type": "minecraft:add",
                "argument1": -0.1171875,
                "argument2": {
                  "type": "minecraft:add",
                  "argument1": -0.078125,
                  "argument2": {
                    "type": "minecraft:mul",
                    "argument1": {
                      "type": "minecraft:y_clamped_gradient",
                      "from_value": 1.0,
                      "from_y": 240,
                      "to_value": 0.0,
                      "to_y": 256
                    },
                    "argument2": {
                      "type": "minecraft:add",
                      "argument1": 0.078125,
                      "argument2": {
                        "type": "minecraft:range_choice",
                        "input": "minecraft:overworld/sloped_cheese",
                        "max_exclusive": 1.5625,
                        "min_inclusive": -1000000.0,
                        "when_in_range": {
                          "type": "minecraft:min",
                          "argument1": "minecraft:overworld/sloped_cheese",
                          "argument2": {
                            "type": "minecraft:mul",
                            "argument1": 5.0,
                            "argument2": "minecraft:overworld/caves/entrances"
                          }
                        },
                        "when_out_of_range": {
                          "type": "minecraft:max",
                          "argument1": {
                            "type": "minecraft:min",
                            "argument1": {
                              "type": "minecraft:min",
                              "argument1": {
                                "type": "minecraft:add",
                                "argument1": {
                                  "type": "minecraft:mul",
                                  "argument1": 4.0,
                                  "argument2": {
                                    "type": "minecraft:square",
                                    "argument": {
                                      "type": "minecraft:noise",
                                      "noise": "minecraft:cave_layer",
                                      "xz_scale": 1.0,
                                      "y_scale": 8.0
                                    }
                                  }
                                },
                                "argument2": {
                                  "type": "minecraft:add",
                                  "argument1": {
                                    "type": "minecraft:clamp",
                                    "input": {
                                      "type": "minecraft:add",
                                      "argument1": 0.27,
                                      "argument2": {
                                        "type": "minecraft:noise",
                                        "noise": "minecraft:cave_cheese",
                                        "xz_scale": 1.0,
                                        "y_scale": 0.6666666666666666
                                      }
                                    },
                                    "max": 1.0,
                                    "min": -1.0
                                  },
                                  "argument2": {
                                    "type": "minecraft:clamp",
                                    "input": {
                                      "type": "minecraft:add",
                                      "argument1": 1.5,
                                      "argument2": {
                                        "type": "minecraft:mul",
                                        "argument1": -0.64,
                                        "argument2": "minecraft:overworld/sloped_cheese"
                                      }
                                    },
                                    "max": 0.5,
                                    "min": 0.0
                                  }
                                }
                              },
                              "argument2": "minecraft:overworld/caves/entrances"
                            },
                            "argument2": {
                              "type": "minecraft:add",
                              "argument1": "minecraft:overworld/caves/spaghetti_2d",
                              "argument2": "minecraft:overworld/caves/spaghetti_roughness_function"
                            }
                          },
                          "argument2": {
                            "type": "minecraft:range_choice",
                            "input": "minecraft:overworld/caves/pillars",
                            "max_exclusive": 0.03,
                            "min_inclusive": -1000000.0,
                            "when_in_range": -1000000.0,
                            "when_out_of_range": "minecraft:overworld/caves/pillars"
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  },
  "argument2": "minecraft:overworld/caves/noodle"
}
""";

    private static final String VANILLA_INITIAL_DENSITY_TEMPLATE = """
{
  "type": "minecraft:add",
  "argument1": 0.1171875,
  "argument2": {
    "type": "minecraft:mul",
    "argument1": {
      "type": "minecraft:y_clamped_gradient",
      "from_value": 0.0,
      "from_y": -64,
      "to_value": 1.0,
      "to_y": -40
    },
    "argument2": {
      "type": "minecraft:add",
      "argument1": -0.1171875,
      "argument2": {
        "type": "minecraft:add",
        "argument1": -0.078125,
        "argument2": {
          "type": "minecraft:mul",
          "argument1": {
            "type": "minecraft:y_clamped_gradient",
            "from_value": 1.0,
            "from_y": 240,
            "to_value": 0.0,
            "to_y": 256
          },
          "argument2": {
            "type": "minecraft:add",
            "argument1": 0.078125,
            "argument2": {
              "type": "minecraft:clamp",
              "input": {
                "type": "minecraft:add",
                "argument1": -0.703125,
                "argument2": {
                  "type": "minecraft:mul",
                  "argument1": 4.0,
                  "argument2": {
                    "type": "minecraft:quarter_negative",
                    "argument": {
                      "type": "minecraft:mul",
                      "argument1": "minecraft:overworld/depth",
                      "argument2": {
                        "type": "minecraft:cache_2d",
                        "argument": "minecraft:overworld/factor"
                      }
                    }
                  }
                }
              },
              "max": 64.0,
              "min": -64.0
            }
          }
        }
      }
    }
  }
}
""";

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
                    // Convert vanilla biomes to planet-specific custom biomes
                    // This prevents Mekanism's ore injection since our biomes aren't in #minecraft:is_overworld
                    String vanillaBiomeName = biomeEntry.biomeName.substring("minecraft:".length());
                    customBiomeName = "adastramekanized:" + planet.name + "_" + vanillaBiomeName;
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
    /**
     * Terrain tweaks for per-planet terrain character customization
     * These multipliers adjust the vanilla-style density functions to create unique terrain personalities
     */
    public static class TerrainTweaks {
        private PlanetBuilder parent;

        public double jaggednessMultiplier = 1.0;    // Mountain sharpness (Moon: 1.5 for craters, Mars: 0.8 for gentle)
        public double erosionIntensity = 1.0;        // Erosion effect (Moon: 0.7 less erosion, Mars: 1.3 more canyons)
        public double heightVariation = 1.0;         // Overall height range (Moon: 0.8 for flatter, Mars: 1.2 for dramatic)
        public double ridgeStrength = 1.0;           // Ridge prominence (affects mountain ridges)
        public double factorScale = 1.0;             // Terrain variation scale (affects hills vs plains ratio)

        TerrainTweaks(PlanetBuilder parent) {
            this.parent = parent;
        }

        public TerrainTweaks jaggedness(double mult) {
            this.jaggednessMultiplier = mult;
            return this;
        }

        public TerrainTweaks erosion(double intensity) {
            this.erosionIntensity = intensity;
            return this;
        }

        public TerrainTweaks height(double variation) {
            this.heightVariation = variation;
            return this;
        }

        public TerrainTweaks ridges(double strength) {
            this.ridgeStrength = strength;
            return this;
        }

        public TerrainTweaks factor(double scale) {
            this.factorScale = scale;
            return this;
        }

        // Return to parent builder after configuring tweaks
        public PlanetBuilder done() {
            return parent;
        }
    }

    public static class PlanetBuilder {
        private final String name;

        // Unique seed for this planet's terrain generation
        private long seed;

        // Terrain character tweaks for vanilla-quality generation
        private TerrainTweaks terrainTweaks;

        // Vanilla noise reference system (NEW: use proven vanilla generation)
        private boolean useVanillaNoise = false;
        private String vanillaNoiseReference = "minecraft:overworld"; // Default to Overworld terrain

        // Identical vanilla terrain mode - bypasses ALL custom density function generation
        // When true, uses direct vanilla noise router references for terrain identical to vanilla
        private boolean useIdenticalVanillaTerrain = false;

        // Vanilla-QUALITY terrain mode - copies full vanilla density function set with coordinate shifting
        // This produces terrain with vanilla quality (splines, factor, offset, jaggedness) but unique per planet
        // When true, generates full vanilla density function files with planet-specific references
        private boolean useVanillaQualityTerrain = false;

        // Flat splines mode - uses constant values instead of vanilla splines for truly flat terrain
        // When true with useVanillaQualityTerrain, uses flat_offset.json, flat_factor.json, flat_jaggedness.json
        private boolean useFlatSplines = false;

        // Vanilla caves mode - when true, adds vanilla carvers to biomes for full cave generation
        // Automatically enabled when useIdenticalVanillaTerrain is true
        private boolean useVanillaCaves = false;

        // Vanilla underground features - when true, adds vanilla ore/geode/dungeon features to biomes
        private boolean useVanillaUndergroundFeatures = false;

        // Frequency modulation for vanilla noise (multiply vanilla noise values to create distinct terrain)
        private double continentsMultiplier = 1.0;
        private double erosionMultiplier = 1.0;
        private double ridgesMultiplier = 1.0;
        private double depthMultiplier = 1.0;

        // Noise offsets to shift sampling position (makes planets sample different parts of noise space)
        private double continentsNoiseOffset = 0.0;
        private double erosionNoiseOffset = 0.0;
        private double ridgesNoiseOffset = 0.0;
        private double depthNoiseOffset = 0.0;

        // Coordinate transformation for unique terrain (NEW: proper coordinate shifting)
        private int coordinateShiftX = 0;
        private int coordinateShiftZ = 0;
        private double noiseScaleXZ = 1.0;
        private double noiseScaleY = 1.0;
        private Integer customSalt = null;  // If null, auto-generate from planet ID

        // Noise configuration - Primary terrain shaping (LEGACY: only used if useVanillaNoise = false)
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
        private String deepslateBlock = "minecraft:deepslate";  // Block to use below Y=0 (replaces vanilla deepslate)

        // Advanced surface controls
        private boolean enableCustomSurfaceRules = true;
        private boolean disableDefaultSurfaceGeneration = true;
        // NOTE: Prevention flags disabled - they were causing surface rules to fire
        // BEFORE the above_preliminary_surface wrapper, resulting in single-layer surfaces
        private boolean preventGrassGeneration = false;
        private boolean preventGravelGeneration = false;
        private boolean preventSandGeneration = false;
        // Surface layer depth control: true = 1 block (like grass), false = 3-8 blocks (like mars sand)
        // Default: null (auto-detect based on surface block type)
        private Boolean singleLayerSurface = null;

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

        // Modded structure support - these generate biome tags for structure spawning
        private boolean enableRibbitsStructures = false;  // Swamp frog villages
        private boolean enableKoboldsStructures = false;  // Underground kobold dens
        private boolean enableDungeonsAriseStructures = false;  // WhenDungeonsArise structures
        private boolean enableSevenSeasStructures = false;  // WhenDungeonsArise Seven Seas ships
        private java.util.Set<String> dungeonsAriseStructureTypes = new java.util.HashSet<>();  // Specific structure types to enable

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

        // Tectonic-inspired terrain features
        private float verticalTerrainScale = 1.0f; // 0.5-2.0, stretches terrain vertically (Tectonic default: 1.125)
        private boolean undergroundRivers = false; // Generate underground water channels
        private boolean rollingHills = false; // Add gentle terrain variation
        private boolean junglePillars = false; // Generate dramatic vertical spires
        private boolean lavaTunnels = false; // Generate underground lava channels
        private float flatTerrainSkew = 0.0f; // 0.0-1.0, controls amount of flat terrain (0=none, 1=mostly flat)
        private float oceanOffset = -0.8f; // -1.0 to 1.0, controls ocean vs land ratio (negative=more ocean)
        private float caveDepthCutoffStart = 0.1f; // 0.0-1.0, where caves start to fade out
        private float caveDepthCutoffSize = 0.1f; // 0.0-1.0, size of cave cutoff gradient
        private float cheeseCaveAdditive = 0.27f; // -1.0 to 1.0, controls large cave intensity
        private float noodleCaveAdditive = -0.075f; // -1.0 to 1.0, controls small cave intensity
        private boolean enableIncreasedHeight = false; // Increase world height limits (experimental)
        private boolean enableUltrasmooth = false; // Ultra-smooth terrain generation (less dramatic)
        private int snowStartOffset = 128; // Y level offset where snow starts forming

        // Full Tectonic generation mode
        private boolean useTectonicGeneration = false; // Enable complete Tectonic worldgen system
        private boolean enableIslands = false; // Enable island generation in Tectonic mode
        private float mountainSharpness = 1.0f; // Mountain sharpness multiplier for Tectonic
        private boolean enableDesertDunes = false; // Enable desert dune features
        private float duneHeight = 10.0f; // Height of desert dunes
        private float duneWavelength = 200.0f; // Wavelength of dune patterns
        private float pillarHeight = 30.0f; // Height of jungle pillars

        // Dimension properties
        private int skyColor = 0x78A7FF;
        private int fogColor = 0xC0D8FF;
        private boolean hasAtmosphere = true;
        private boolean atmosphereBreathable = true;  // Separate flag for breathable atmosphere
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
        private boolean fireDamage = false;
        private float fireDamageAmount = 1.0f;
        private int surfaceTemperature = 15;  // Surface temperature in Celsius (Earth default)

        // Tectonic worldgen configuration system
        private CraterConfig craterConfig = null;
        private CanyonConfig canyonConfig = null;
        private VolcanoConfig volcanoConfig = null;
        private PolarCapConfig polarCapConfig = null;
        private DuneConfig duneConfig = null;
        private MariaConfig mariaConfig = null;
        private AtmosphereConfig atmosphereEffectsConfig = null;
        private ScarpConfig scarpConfig = null;
        private BasinConfig basinConfig = null;
        private RegolithConfig regolithConfig = null;
        private java.util.List<BiomeConfigEntry> biomeConfigs = new java.util.ArrayList<>();
        private BiomeZoneConfig biomeZoneConfig = null;
        private TectonicNoiseConfig tectonicNoiseConfig = null;
        private SurfaceRuleConfig surfaceRuleConfig = null;

        private PlanetBuilder(String name) {
            this.name = name;
            // Generate unique seed from planet name hash + constant offset for deterministic results
            this.seed = name.hashCode() + 1000000L; // Offset to avoid negative hash values being too small
            // Initialize terrain tweaks with reference to this builder for fluent chaining
            this.terrainTweaks = new TerrainTweaks(this);
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

        /**
         * Use vanilla Overworld terrain generation (continents, mountains, oceans, caves)
         * This is the RECOMMENDED approach - proven, tested, and works perfectly.
         * Only customize surface blocks, not terrain shape.
         */
        public PlanetBuilder useOverworldTerrain() {
            this.useVanillaNoise = true;
            this.vanillaNoiseReference = "minecraft:overworld";
            return this;
        }

        /**
         * Use vanilla Nether terrain generation (floating islands, lava lakes, pillars)
         */
        public PlanetBuilder useNetherTerrain() {
            this.useVanillaNoise = true;
            this.vanillaNoiseReference = "minecraft:nether";
            return this;
        }

        /**
         * Use vanilla End terrain generation (single large island with void)
         */
        public PlanetBuilder useEndTerrain() {
            this.useVanillaNoise = true;
            this.vanillaNoiseReference = "minecraft:the_end";
            return this;
        }

        /**
         * Use IDENTICAL vanilla Overworld terrain generation.
         * This uses direct references to all vanilla noise router entries including:
         * - continents, erosion, depth, ridges (terrain shape)
         * - initial_density_without_jaggedness, final_density (caves and 3D terrain)
         *
         * The terrain will be byte-for-byte identical to vanilla Overworld.
         * Only surface blocks, biomes, and spawning differ.
         *
         * This is the RECOMMENDED approach for planets that should have vanilla-quality terrain.
         */
        public PlanetBuilder useIdenticalVanillaTerrain() {
            this.useVanillaNoise = true;
            this.useIdenticalVanillaTerrain = true;
            this.vanillaNoiseReference = "minecraft:overworld";
            // Enable vanilla caves and underground features for full vanilla experience
            this.useVanillaCaves = true;
            this.useVanillaUndergroundFeatures = true;
            // Reset any coordinate shifting that would create custom density functions
            this.coordinateShiftX = 0;
            this.coordinateShiftZ = 0;
            this.noiseScaleXZ = 1.0;
            this.noiseScaleY = 1.0;
            this.customSalt = null;
            // Reset noise modulation
            this.continentsMultiplier = 1.0;
            this.erosionMultiplier = 1.0;
            this.ridgesMultiplier = 1.0;
            this.depthMultiplier = 1.0;
            this.continentsNoiseOffset = 0.0;
            this.erosionNoiseOffset = 0.0;
            this.ridgesNoiseOffset = 0.0;
            this.depthNoiseOffset = 0.0;
            return this;
        }

        /**
         * Use vanilla-QUALITY terrain generation with coordinate shifting.
         * This copies the full vanilla density function set (offset, factor, jaggedness splines)
         * and replaces references to create planet-specific versions.
         *
         * Unlike useIdenticalVanillaTerrain(), this produces UNIQUE terrain per planet
         * that still has vanilla-quality spline-based terrain shaping.
         *
         * Configurable parameters:
         * - coordinateShift(x, z): Shifts noise sampling position for unique terrain
         * - base3dNoiseScale(xz, y): Controls terrain frequency
         * - base3dNoiseFactor(xz, y): Controls terrain amplitude
         * - slopedCheeseMultiplier(mult): Controls overall terrain scale (default 4.0)
         * - jaggedNoiseScale(scale): Controls mountain peak spacing (default 1500.0)
         */
        public PlanetBuilder useVanillaQualityTerrain() {
            this.useVanillaNoise = true;
            this.useVanillaQualityTerrain = true;
            this.useIdenticalVanillaTerrain = false;  // Mutually exclusive
            this.vanillaNoiseReference = "minecraft:overworld";
            // Enable vanilla caves and underground features for full vanilla experience
            this.useVanillaCaves = true;
            this.useVanillaUndergroundFeatures = true;
            // Set default coordinate shift based on planet name hash
            if (this.coordinateShiftX == 0 && this.coordinateShiftZ == 0) {
                this.coordinateShiftX = generateCoordinateShift(this.name, 0);
                this.coordinateShiftZ = generateCoordinateShift(this.name, 1);
            }
            return this;
        }

        /**
         * Configure sloped_cheese terrain multiplier (default 4.0).
         * Higher values create more dramatic terrain.
         */
        public PlanetBuilder slopedCheeseMultiplier(float multiplier) {
            this.terrainFactor = multiplier;
            return this;
        }

        /**
         * Configure jagged noise scale for mountain peaks (default 1500.0).
         * Higher values create larger, more spaced mountains.
         */
        public PlanetBuilder jaggedNoiseScale(float scale) {
            this.jaggednessNoiseScale = scale;
            return this;
        }

        /**
         * Enable vanilla cave carvers in biomes (cave, cave_extra_underground, canyon)
         */
        public PlanetBuilder useVanillaCaves(boolean enabled) {
            this.useVanillaCaves = enabled;
            return this;
        }

        /**
         * Enable vanilla underground features in biomes (ores, geodes, monster rooms, etc.)
         */
        public PlanetBuilder useVanillaUndergroundFeatures(boolean enabled) {
            this.useVanillaUndergroundFeatures = enabled;
            return this;
        }

        // ========== TERRAIN PRESETS ==========
        // These configure the noise multipliers to create different terrain characters
        // while still using vanilla-quality algorithms. Requires coordinateShift() to take effect.

        /**
         * FLAT terrain - minimal height variation, good for plains-style planets
         * continents: 0.3x, erosion: 0.3x, ridges: 0.2x
         */
        public PlanetBuilder terrainFlat() {
            this.continentsMultiplier = 0.3;
            this.erosionMultiplier = 0.3;
            this.ridgesMultiplier = 0.2;
            return this;
        }

        /**
         * ROLLING HILLS terrain - gentle undulation, pastoral feel
         * continents: 0.6x, erosion: 0.5x, ridges: 0.4x
         */
        public PlanetBuilder terrainRollingHills() {
            this.continentsMultiplier = 0.6;
            this.erosionMultiplier = 0.5;
            this.ridgesMultiplier = 0.4;
            return this;
        }

        /**
         * NORMAL terrain - vanilla-like balance (default)
         * continents: 1.0x, erosion: 1.0x, ridges: 1.0x
         */
        public PlanetBuilder terrainNormal() {
            this.continentsMultiplier = 1.0;
            this.erosionMultiplier = 1.0;
            this.ridgesMultiplier = 1.0;
            return this;
        }

        /**
         * MOUNTAINOUS terrain - dramatic peaks and valleys
         * continents: 1.2x, erosion: 0.6x, ridges: 2.5x
         */
        public PlanetBuilder terrainMountainous() {
            this.continentsMultiplier = 1.2;
            this.erosionMultiplier = 0.6;  // Less erosion = taller features
            this.ridgesMultiplier = 2.5;   // More ridges = more peaks
            return this;
        }

        /**
         * CANYON terrain - deep valleys and plateaus
         * continents: 0.8x, erosion: 2.5x, ridges: 1.5x
         */
        public PlanetBuilder terrainCanyons() {
            this.continentsMultiplier = 0.8;
            this.erosionMultiplier = 2.5;   // High erosion = carved terrain
            this.ridgesMultiplier = 1.5;
            return this;
        }

        /**
         * CHAOTIC terrain - extreme, alien landscapes
         * continents: 2.0x, erosion: 2.0x, ridges: 3.0x
         */
        public PlanetBuilder terrainChaotic() {
            this.continentsMultiplier = 2.0;
            this.erosionMultiplier = 2.0;
            this.ridgesMultiplier = 3.0;
            return this;
        }

        /**
         * ARCHIPELAGO terrain - many islands and water bodies
         * continents: 0.4x (less land), erosion: 0.8x, ridges: 0.6x
         */
        public PlanetBuilder terrainArchipelago() {
            this.continentsMultiplier = 0.4;  // Less continental = more ocean
            this.erosionMultiplier = 0.8;
            this.ridgesMultiplier = 0.6;
            return this;
        }

        /**
         * VOLCANIC terrain - tall peaks with flat surroundings
         * continents: 0.5x, erosion: 0.3x, ridges: 4.0x
         */
        public PlanetBuilder terrainVolcanic() {
            this.continentsMultiplier = 0.5;
            this.erosionMultiplier = 0.3;
            this.ridgesMultiplier = 4.0;  // Extreme ridges = isolated peaks
            return this;
        }

        /**
         * CRATERED terrain - good for moons, pockmarked surface
         * continents: 0.7x, erosion: 1.8x, ridges: 0.8x
         */
        public PlanetBuilder terrainCratered() {
            this.continentsMultiplier = 0.7;
            this.erosionMultiplier = 1.8;   // Erosion creates depressions
            this.ridgesMultiplier = 0.8;
            return this;
        }

        /**
         * Custom terrain multipliers - full control
         * @param continents Continental influence (0.1-3.0, default 1.0)
         * @param erosion Erosion influence (0.1-3.0, default 1.0)
         * @param ridges Ridge/peak influence (0.1-5.0, default 1.0)
         */
        public PlanetBuilder terrainCustom(double continents, double erosion, double ridges) {
            this.continentsMultiplier = continents;
            this.erosionMultiplier = erosion;
            this.ridgesMultiplier = ridges;
            return this;
        }

        // ========== VANILLA-QUALITY TERRAIN PRESETS ==========
        // These configure the sloped_cheese and base_3d_noise parameters for vanilla-quality mode.
        // Use with .useVanillaQualityTerrain() for best results.

        /**
         * VANILLA-QUALITY: Standard Overworld terrain (default settings)
         * Uses vanilla's exact terrain algorithm with coordinate shifting for unique terrain.
         */
        public PlanetBuilder vanillaQualityStandard() {
            this.useVanillaQualityTerrain = true;
            this.useVanillaNoise = true;
            this.useVanillaCaves = true;
            this.useVanillaUndergroundFeatures = true;
            this.terrainFactor = 4.0f;
            this.jaggednessNoiseScale = 1500.0f;
            this.base3DNoiseXZFactor = 80.0f;
            this.base3DNoiseYFactor = 160.0f;
            this.smearScaleMultiplier = 8.0f;
            return this;
        }

        /**
         * VANILLA-QUALITY: Flat terrain with gentle rolling hills
         * Reduced terrain factor and amplitude for a calmer landscape.
         */
        public PlanetBuilder vanillaQualityFlat() {
            vanillaQualityStandard();
            this.terrainFactor = 2.0f;       // Less dramatic terrain
            this.base3DNoiseXZFactor = 40.0f; // Smaller features
            this.jaggednessNoiseScale = 2000.0f;  // Larger mountain spacing
            return this;
        }

        /**
         * VANILLA-QUALITY: Ultra-flat terrain like plains biome
         * Uses constant spline values instead of vanilla's complex splines.
         * Produces truly flat terrain with minimal height variation.
         */
        public PlanetBuilder vanillaQualityUltraFlat() {
            vanillaQualityStandard();
            this.useFlatSplines = true;      // Use flat_offset.json, flat_factor.json, flat_jaggedness.json
            this.terrainFactor = 1.0f;       // Minimal terrain scale
            this.base3DNoiseXZFactor = 20.0f; // Very small horizontal features
            this.base3DNoiseYFactor = 40.0f;  // Reduced vertical variation
            this.jaggednessNoiseScale = 5000.0f;  // Mountains very far apart (effectively none)
            return this;
        }

        /**
         * Enable flat splines for truly flat terrain.
         * Uses constant values instead of vanilla's complex spline system.
         */
        public PlanetBuilder useFlatSplines() {
            this.useFlatSplines = true;
            return this;
        }

        /**
         * VANILLA-QUALITY: Mountainous terrain with dramatic peaks
         * Increased terrain factor and jaggedness for extreme landscapes.
         */
        public PlanetBuilder vanillaQualityMountainous() {
            vanillaQualityStandard();
            this.terrainFactor = 6.0f;        // More dramatic terrain
            this.jaggednessNoiseScale = 1000.0f;  // Closer mountain peaks
            this.base3DNoiseXZFactor = 120.0f;    // Larger features
            return this;
        }

        /**
         * VANILLA-QUALITY: Alien terrain with shifted noise and modified parameters
         * Coordinate shifted with adjusted noise for otherworldly feel.
         */
        public PlanetBuilder vanillaQualityAlien() {
            vanillaQualityStandard();
            this.terrainFactor = 5.0f;
            this.base3DNoiseXZFactor = 60.0f;
            this.base3DNoiseYFactor = 140.0f;
            this.smearScaleMultiplier = 6.0f;  // Less smooth terrain
            this.jaggednessNoiseScale = 1200.0f;
            return this;
        }

        /**
         * VANILLA-QUALITY: Cratered terrain for moons
         * Good for airless bodies with impact crater-like features.
         */
        public PlanetBuilder vanillaQualityCratered() {
            vanillaQualityStandard();
            this.terrainFactor = 3.0f;
            this.base3DNoiseXZFactor = 100.0f;
            this.jaggednessNoiseScale = 800.0f;  // Frequent peaks (crater rims)
            this.smearScaleMultiplier = 4.0f;    // Sharper features
            return this;
        }

        /**
         * VANILLA-QUALITY: Archipelago terrain with lots of water
         * Good for ocean worlds with island chains.
         */
        public PlanetBuilder vanillaQualityArchipelago() {
            vanillaQualityStandard();
            this.terrainFactor = 4.0f;
            this.base3DNoiseXZFactor = 50.0f;  // Smaller landmasses
            return this;
        }

        /**
         * Configure terrain character tweaks for vanilla-quality generation
         * Returns TerrainTweaks object for chaining tweak configuration
         * Example: .terrainTweaks().jaggedness(1.5).erosion(0.7).height(0.8)
         */
        public TerrainTweaks terrainTweaks() {
            return this.terrainTweaks;
        }

        /**
         * Modulate vanilla continents noise by multiplier (e.g., 1.3 for more dramatic landmasses)
         */
        public PlanetBuilder modulateContinents(double multiplier) {
            this.continentsMultiplier = multiplier;
            return this;
        }

        /**
         * Modulate vanilla erosion noise by multiplier (e.g., 1.4 for more erosion features)
         */
        public PlanetBuilder modulateErosion(double multiplier) {
            this.erosionMultiplier = multiplier;
            return this;
        }

        /**
         * Modulate vanilla ridges noise by multiplier (e.g., 1.5 for sharper ridges)
         */
        public PlanetBuilder modulateRidges(double multiplier) {
            this.ridgesMultiplier = multiplier;
            return this;
        }

        /**
         * Modulate vanilla depth noise by multiplier (e.g., 0.8 for less vertical variation)
         */
        public PlanetBuilder modulateDepth(double multiplier) {
            this.depthMultiplier = multiplier;
            return this;
        }

        /**
         * Offset continents noise sampling (shifts where planet samples the noise space)
         */
        public PlanetBuilder offsetContinents(double offset) {
            this.continentsNoiseOffset = offset;
            return this;
        }

        /**
         * Offset erosion noise sampling (shifts where planet samples the noise space)
         */
        public PlanetBuilder offsetErosion(double offset) {
            this.erosionNoiseOffset = offset;
            return this;
        }

        /**
         * Offset ridges noise sampling (shifts where planet samples the noise space)
         */
        public PlanetBuilder offsetRidges(double offset) {
            this.ridgesNoiseOffset = offset;
            return this;
        }

        /**
         * Offset depth noise sampling (shifts where planet samples the noise space)
         */
        public PlanetBuilder offsetDepth(double offset) {
            this.depthNoiseOffset = offset;
            return this;
        }

        /**
         * Set coordinate shift for sampling different regions of noise space.
         * This makes each planet sample a different "location" in the noise function,
         * creating unique terrain as if using a different world seed.
         *
         * @param x X-axis shift (recommended range: 0-5000)
         * @param z Z-axis shift (recommended range: 0-5000)
         */
        public PlanetBuilder coordinateShift(int x, int z) {
            this.coordinateShiftX = x;
            this.coordinateShiftZ = z;
            return this;
        }

        /**
         * Set noise scaling factors for terrain frequency control.
         *
         * @param xz Horizontal (XZ) scale factor (0.8-1.3 recommended)
         * @param y Vertical (Y) scale factor (1.5-2.5 recommended)
         */
        public PlanetBuilder noiseScale(double xz, double y) {
            this.noiseScaleXZ = xz;
            this.noiseScaleY = y;
            return this;
        }

        /**
         * Set custom salt for noise generation. If not set, salt will be
         * automatically generated from planet ID hash.
         *
         * @param salt Unique integer salt for this planet's noise
         */
        public PlanetBuilder terrainSalt(int salt) {
            this.customSalt = salt;
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

        /**
         * Set the block to use below Y=0 (replaces vanilla deepslate layer).
         * This creates a transition from defaultBlock (above Y=0) to deepslateBlock (below Y=0),
         * mimicking vanilla's stone->deepslate transition.
         */
        public PlanetBuilder deepslateBlock(String block) {
            this.deepslateBlock = block;
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

        /**
         * Control surface layer depth behavior.
         * - true: Single layer surface (1 block on top, like grass/moss - subsurface shows immediately below)
         * - false: Multi-layer surface (3-8 blocks of surface material, like mars sand)
         * - null (default): Auto-detect based on surface block type (grass_block/moss_block = single, others = multi)
         */
        public PlanetBuilder singleLayerSurface(boolean single) {
            this.singleLayerSurface = single;
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
         * Automatically generates biome tags to add this planet's biomes to kobold den requirements.
         */
        public PlanetBuilder enableKoboldsStructures() {
            this.enableKoboldsStructures = true;
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
         * Automatically generates biome tags to add this planet's biomes to ribbit village requirements.
         */
        public PlanetBuilder enableRibbitsStructures() {
            this.enableRibbitsStructures = true;
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

        // ========== BORN IN CHAOS MOB HELPERS ==========

        /**
         * Enable Born in Chaos structures for this planet.
         * Includes: Dark Tower, Clown Caravan, Mound of Hounds, Firewell, Lookout Tower, Farm
         */
        public PlanetBuilder enableBornInChaosStructures() {
            com.hecookin.adastramekanized.common.events.ModdedStructureController
                .whitelistModStructures(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        AdAstraMekanized.MOD_ID, this.name),
                    "born_in_chaos_v1");
            return this;
        }

        /**
         * Add a single Born in Chaos mob to the planet.
         * @param mobName The mob name (e.g., "zombie_clown", "restless_spirit")
         * @param weight Spawn weight
         * @param minGroup Minimum group size
         * @param maxGroup Maximum group size
         */
        public PlanetBuilder addBornInChaosMob(String mobName, int weight, int minGroup, int maxGroup) {
            return addModdedMob("born_in_chaos_v1", mobName, "monster", weight, minGroup, maxGroup);
        }

        /**
         * Add Born in Chaos Spirit mobs (ghosts, spectral entities).
         * Recommended for: profundus, luxoria (dark/mystical worlds)
         * @param weight Base spawn weight (recommended: 15-30)
         */
        public PlanetBuilder addBornInChaosSpirits(int weight) {
            addMobSpawn("monster", "born_in_chaos_v1:restless_spirit", weight, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:seared_spirit", weight - 5, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:firelight", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:pumpkin_spirit", weight - 8, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:infernal_spirit", weight - 10, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:dark_vortex", weight / 3, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:spiritof_chaos", weight / 4, 1, 1);
            return this;
        }

        /**
         * Add Born in Chaos Undead mobs (zombies, skeletons).
         * Recommended for: mars, venus, io (harsh/barren worlds)
         * @param weight Base spawn weight (recommended: 20-40)
         */
        public PlanetBuilder addBornInChaosUndead(int weight) {
            addMobSpawn("monster", "born_in_chaos_v1:decrepit_skeleton", weight, 2, 4);
            addMobSpawn("monster", "born_in_chaos_v1:skeleton_demoman", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:decaying_zombie", weight, 2, 4);
            addMobSpawn("monster", "born_in_chaos_v1:barrel_zombie", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:door_knight", weight - 10, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:skeleton_thrasher", weight - 8, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:bonescaller", weight / 2, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:bone_imp", weight - 5, 1, 3);
            addMobSpawn("monster", "born_in_chaos_v1:zombie_bruiser", weight - 10, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:siamese_skeletons", weight / 3, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:fallen_chaos_knight", weight / 4, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:scarlet_persecutor", weight / 4, 1, 1);
            return this;
        }

        /**
         * Add Born in Chaos Pumpkin/Halloween mobs.
         * Recommended for: terra_nova, bellator (Halloween-themed areas)
         * @param weight Base spawn weight (recommended: 20-35)
         */
        public PlanetBuilder addBornInChaosPumpkin(int weight) {
            addMobSpawn("monster", "born_in_chaos_v1:pumpkinhead", weight, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:mr_pumpkin", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:mrs_pumpkin", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:pumpkin_dunce", weight - 3, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:pumpkin_bruiser", weight - 8, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:senor_pumpkin", weight / 2, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:sir_pumpkinhead", weight / 3, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:lord_pumpkinhead", weight / 5, 1, 1);
            return this;
        }

        /**
         * Add Born in Chaos Clown mob (with Clown Caravan structure).
         * Recommended for: terra_nova (strange/circus themed)
         * @param weight Base spawn weight (recommended: 10-20)
         */
        public PlanetBuilder addBornInChaosClowns(int weight) {
            addMobSpawn("monster", "born_in_chaos_v1:zombie_clown", weight, 1, 2);
            return this;
        }

        /**
         * Add Born in Chaos creature mobs (spiders, hounds, crabs).
         * Recommended for: primordium, frigidum (creature-heavy worlds)
         * @param weight Base spawn weight (recommended: 15-30)
         */
        public PlanetBuilder addBornInChaosCreatures(int weight) {
            addMobSpawn("monster", "born_in_chaos_v1:baby_spider", weight, 2, 4);
            addMobSpawn("monster", "born_in_chaos_v1:mother_spider", weight / 3, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:dread_hound", weight - 5, 1, 3);
            addMobSpawn("monster", "born_in_chaos_v1:dire_hound_leader", weight / 3, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:phantom_creeper", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:swarmer", weight - 5, 2, 4);
            addMobSpawn("monster", "born_in_chaos_v1:thornshell_crab", weight - 8, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:maggot", weight, 3, 6);
            addMobSpawn("monster", "born_in_chaos_v1:corpse_fly", weight - 5, 2, 4);
            addMobSpawn("monster", "born_in_chaos_v1:bloody_gadfly", weight - 8, 1, 3);
            addMobSpawn("monster", "born_in_chaos_v1:nightmare_stalker", weight / 4, 1, 1);
            return this;
        }

        /**
         * Add Born in Chaos aquatic undead (for water-containing worlds).
         * Recommended for: paludis, kepler22b (swamp/ocean worlds)
         * @param weight Base spawn weight (recommended: 15-25)
         */
        public PlanetBuilder addBornInChaosAquatic(int weight) {
            addMobSpawn("water_creature", "born_in_chaos_v1:corpse_fish", weight, 2, 4);
            addMobSpawn("water_creature", "born_in_chaos_v1:glutton_fish", weight - 5, 1, 2);
            addMobSpawn("monster", "born_in_chaos_v1:zombie_fisherman", weight - 3, 1, 2);
            return this;
        }

        /**
         * Add Born in Chaos special/boss mobs.
         * Recommended for: kepler22b, gliese667c (advanced difficulty worlds)
         * @param weight Base spawn weight (recommended: 5-15)
         */
        public PlanetBuilder addBornInChaosBosses(int weight) {
            addMobSpawn("monster", "born_in_chaos_v1:supreme_bonescaller", weight / 2, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:lifestealer", weight / 2, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:krampus", weight / 3, 1, 1);
            addMobSpawn("monster", "born_in_chaos_v1:missioner", weight / 2, 1, 1);
            return this;
        }

        /**
         * Add Born in Chaos preset for different world types.
         * @param preset Choose from: "spirits", "undead", "pumpkin"/"halloween", "creatures", "aquatic", "boss"/"hard", "full"
         */
        public PlanetBuilder addBornInChaosPreset(String preset) {
            switch (preset.toLowerCase()) {
                case "spirits":
                    addBornInChaosSpirits(25);
                    break;
                case "undead":
                    addBornInChaosUndead(30);
                    break;
                case "pumpkin":
                case "halloween":
                    addBornInChaosPumpkin(25);
                    addBornInChaosClowns(15);
                    break;
                case "creatures":
                    addBornInChaosCreatures(25);
                    break;
                case "aquatic":
                    addBornInChaosAquatic(20);
                    break;
                case "boss":
                case "hard":
                    addBornInChaosBosses(10);
                    break;
                case "full":
                    addBornInChaosSpirits(20);
                    addBornInChaosUndead(25);
                    addBornInChaosPumpkin(20);
                    addBornInChaosCreatures(20);
                    break;
                default:
                    AdAstraMekanized.LOGGER.warn("Unknown Born in Chaos preset: {}", preset);
            }
            return this;
        }

        // ========== END BORN IN CHAOS HELPERS ==========

        /**
         * Enable WhenDungeonsArise structures for this planet.
         * Adds biome tags to allow various dungeon structures to spawn.
         * @param structureTypes Types to enable: "plains", "forest", "desert", "jungle", "ocean", "swamp", "mountain", "snowy"
         */
        public PlanetBuilder enableDungeonsAriseStructures(String... structureTypes) {
            this.enableDungeonsAriseStructures = true;
            for (String type : structureTypes) {
                this.dungeonsAriseStructureTypes.add(type.toLowerCase());
            }
            return this;
        }

        /**
         * Enable WhenDungeonsArise Seven Seas structures (ships) for ocean planets.
         * Adds biome tags to allow pirate ships and other naval structures to spawn.
         */
        public PlanetBuilder enableSevenSeasStructures() {
            this.enableSevenSeasStructures = true;
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
            // If no atmosphere, it can't be breathable
            if (!atmosphere) {
                this.atmosphereBreathable = false;
            }
            return this;
        }

        public PlanetBuilder breathableAtmosphere(boolean breathable) {
            this.atmosphereBreathable = breathable;
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

        /**
         * Enable fire damage on this planet (constant heat damage)
         */
        public PlanetBuilder fireDamage(boolean damage) {
            this.fireDamage = damage;
            return this;
        }

        /**
         * Set fire damage amount per tick
         */
        public PlanetBuilder fireDamageAmount(float damage) {
            this.fireDamageAmount = Math.max(0.0f, damage);
            return this;
        }

        /**
         * Set surface temperature in Celsius
         * Reference values: Earth=15, Moon=-173, Mars=-65, Venus=464, Mercury=167
         */
        public PlanetBuilder temperature(int celsius) {
            this.surfaceTemperature = celsius;
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

        // ========== TECTONIC-INSPIRED TERRAIN FEATURES ==========

        /**
         * Set vertical terrain scaling (Tectonic-inspired)
         * Stretches terrain vertically for more dramatic height variation
         * @param scale Vertical scale multiplier (0.5-2.0, default 1.0, Tectonic default 1.125)
         */
        public PlanetBuilder verticalTerrainScale(float scale) {
            this.verticalTerrainScale = Math.max(0.5f, Math.min(2.0f, scale));
            return this;
        }

        /**
         * Enable underground river generation (Tectonic-inspired)
         * Creates underground water channels for interesting cave systems
         * @param enabled True to enable underground rivers
         */
        public PlanetBuilder undergroundRivers(boolean enabled) {
            this.undergroundRivers = enabled;
            return this;
        }

        /**
         * Enable rolling hills terrain variation (Tectonic-inspired)
         * Adds gentle undulating terrain for more natural landscapes
         * @param enabled True to enable rolling hills
         */
        public PlanetBuilder rollingHills(boolean enabled) {
            this.rollingHills = enabled;
            return this;
        }

        /**
         * Enable jungle pillar generation (Tectonic-inspired)
         * Creates dramatic vertical stone spires in suitable biomes
         * @param enabled True to enable jungle pillars
         */
        public PlanetBuilder junglePillars(boolean enabled) {
            this.junglePillars = enabled;
            return this;
        }

        /**
         * Enable lava tunnel generation (Tectonic-inspired)
         * Creates underground lava channels and tubes
         * @param enabled True to enable lava tunnels
         */
        public PlanetBuilder lavaTunnels(boolean enabled) {
            this.lavaTunnels = enabled;
            return this;
        }

        /**
         * Set flat terrain skew (Tectonic-inspired)
         * Controls the amount of flat terrain vs varied terrain
         * @param skew Flat terrain amount (0.0=no flat terrain, 1.0=mostly flat, default 0.0)
         */
        public PlanetBuilder flatTerrainSkew(float skew) {
            this.flatTerrainSkew = Math.max(0.0f, Math.min(1.0f, skew));
            return this;
        }

        /**
         * Set ocean offset (Tectonic-inspired)
         * Controls the ocean vs land ratio
         * @param offset Ocean amount (-1.0=all ocean, 1.0=all land, default -0.8)
         */
        public PlanetBuilder oceanOffset(float offset) {
            this.oceanOffset = Math.max(-1.0f, Math.min(1.0f, offset));
            return this;
        }

        /**
         * Configure cave depth cutoff (Tectonic-inspired)
         * Controls where caves fade out vertically
         * @param start Where cave cutoff starts (0.0-1.0, default 0.1)
         * @param size Size of cutoff gradient (0.0-1.0, default 0.1)
         */
        public PlanetBuilder caveDepthCutoff(float start, float size) {
            this.caveDepthCutoffStart = Math.max(0.0f, Math.min(1.0f, start));
            this.caveDepthCutoffSize = Math.max(0.0f, Math.min(1.0f, size));
            return this;
        }

        /**
         * Configure cheese cave intensity (Tectonic-inspired)
         * Controls large open cave system generation
         * @param additive Cave intensity (-1.0 to 1.0, default 0.27)
         */
        public PlanetBuilder cheeseCaveIntensity(float additive) {
            this.cheeseCaveAdditive = Math.max(-1.0f, Math.min(1.0f, additive));
            return this;
        }

        /**
         * Configure noodle cave intensity (Tectonic-inspired)
         * Controls small winding tunnel generation
         * @param additive Cave intensity (-1.0 to 1.0, default -0.075)
         */
        public PlanetBuilder noodleCaveIntensity(float additive) {
            this.noodleCaveAdditive = Math.max(-1.0f, Math.min(1.0f, additive));
            return this;
        }

        /**
         * Enable increased world height (Tectonic-inspired)
         * Experimental feature to increase world height limits
         * @param enabled True to enable increased height
         */
        public PlanetBuilder increasedHeight(boolean enabled) {
            this.enableIncreasedHeight = enabled;
            if (enabled) {
                // Automatically adjust world height when enabled
                this.minY = -128;
                this.worldHeight = 512;
            }
            return this;
        }

        /**
         * Enable ultrasmooth terrain (Tectonic-inspired)
         * Creates smoother, less dramatic terrain
         * @param enabled True to enable ultrasmooth
         */
        public PlanetBuilder ultrasmooth(boolean enabled) {
            this.enableUltrasmooth = enabled;
            return this;
        }

        /**
         * Set snow start offset (Tectonic-inspired)
         * Y level offset where snow begins forming
         * @param offset Snow start Y offset (default 128)
         */
        public PlanetBuilder snowStartOffset(int offset) {
            this.snowStartOffset = offset;
            return this;
        }

        /**
         * Apply a Tectonic-inspired preset configuration
         * Combines multiple features for common terrain styles
         * @param preset Preset name: "enhanced_earth", "alien_world", "volcanic", "frozen"
         */
        public PlanetBuilder tectonicPreset(String preset) {
            switch (preset.toLowerCase()) {
                case "enhanced_earth":
                    // Earth-like with Tectonic enhancements
                    verticalTerrainScale(1.125f);
                    undergroundRivers(true);
                    rollingHills(true);
                    flatTerrainSkew(0.1f);
                    oceanOffset(-0.8f);
                    break;

                case "alien_world":
                    // Dramatic alien terrain
                    verticalTerrainScale(1.4f);
                    rollingHills(false);
                    flatTerrainSkew(0.0f);
                    oceanOffset(-0.5f);
                    junglePillars(true);
                    cheeseCaveIntensity(0.4f);
                    break;

                case "volcanic":
                    // Volcanic world with lava features
                    verticalTerrainScale(1.3f);
                    lavaTunnels(true);
                    undergroundRivers(false);
                    flatTerrainSkew(0.05f);
                    oceanOffset(-0.3f);
                    cheeseCaveIntensity(0.35f);
                    break;

                case "frozen":
                    // Icy world with smooth terrain
                    verticalTerrainScale(1.1f);
                    ultrasmooth(true);
                    rollingHills(true);
                    flatTerrainSkew(0.2f);
                    oceanOffset(-0.9f);
                    snowStartOffset(64);
                    break;

                case "extreme_mountains":
                    // Maximum vertical drama
                    verticalTerrainScale(1.8f);
                    rollingHills(false);
                    flatTerrainSkew(0.0f);
                    oceanOffset(0.3f);
                    junglePillars(true);
                    break;

                case "flat_plains":
                    // Mostly flat with gentle variation
                    verticalTerrainScale(0.8f);
                    ultrasmooth(true);
                    rollingHills(true);
                    flatTerrainSkew(0.7f);
                    oceanOffset(-0.6f);
                    break;
            }
            return this;
        }

        /**
         * Enable full Tectonic worldgen system with advanced density functions.
         * This uses the complete NoiseRouterBuilder for Tectonic-quality terrain.
         * @return this builder for chaining
         */
        public PlanetBuilder withTectonicGeneration() {
            this.useTectonicGeneration = true;
            return this;
        }

        /**
         * Configure islands in Tectonic generation mode.
         * @param enable Whether to generate islands
         * @return this builder for chaining
         */
        public PlanetBuilder withIslands(boolean enable) {
            this.enableIslands = enable;
            return this;
        }

        /**
         * Configure mountain sharpness for Tectonic generation.
         * @param sharpness Sharpness multiplier (0.1-2.0, default 1.0)
         * @return this builder for chaining
         */
        public PlanetBuilder withMountainSharpness(float sharpness) {
            this.mountainSharpness = sharpness;
            return this;
        }

        /**
         * Enable desert dune generation.
         * @param height Dune height in blocks
         * @param wavelength Dune wavelength in blocks
         * @return this builder for chaining
         */
        public PlanetBuilder withDesertDunes(float height, float wavelength) {
            this.enableDesertDunes = true;
            this.duneHeight = height;
            this.duneWavelength = wavelength;
            return this;
        }

        /**
         * Enable jungle pillars with specified height.
         * @param height Pillar height in blocks
         * @return this builder for chaining
         */
        public PlanetBuilder withJunglePillars(float height) {
            this.junglePillars = true;
            this.pillarHeight = height;
            return this;
        }

        /**
         * Configure complete Tectonic terrain using preset.
         * @param config TectonicConfig preset
         * @return this builder for chaining
         */
        public PlanetBuilder withTectonicConfig(com.hecookin.adastramekanized.worldgen.builder.NoiseRouterBuilder.TectonicConfig config) {
            this.useTectonicGeneration = true;
            this.continentalScale = config.continentScale;
            this.erosionScale = config.erosionScale;
            this.ridgeScale = config.ridgeScale;
            this.mountainSharpness = config.mountainSharpness;
            this.enableIslands = config.enableIslands;
            this.enableCheeseCaves = config.cheeseCaves;
            this.enableNoodleCaves = config.noodleCaves;
            this.undergroundRivers = config.undergroundRivers;
            this.lavaTunnels = config.lavaTunnels;
            this.enableDesertDunes = config.desertDunes;
            this.duneHeight = config.duneHeight;
            this.duneWavelength = config.duneWavelength;
            this.junglePillars = config.junglePillars;
            this.pillarHeight = config.pillarHeight;
            return this;
        }

        /**
         * Check if this planet uses full Tectonic generation.
         * @return true if Tectonic generation is enabled
         */
        public boolean usesTectonicGeneration() {
            return useTectonicGeneration;
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
         * Check if planet atmosphere is breathable
         * @return True if atmosphere is breathable
         */
        public boolean isAtmosphereBreathable() {
            return atmosphereBreathable;
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
                // Atmosphere display: simple breathable/toxic/none
                String atmosphereText;
                if (!hasAtmosphere) {
                    atmosphereText = "None";
                } else if (atmosphereBreathable) {
                    atmosphereText = "Breathable";
                } else {
                    atmosphereText = "Toxic";
                }
                statsText.append("$(bold)Atmosphere:$() ").append(atmosphereText).append("$(br)");
                statsText.append("$(bold)Temperature:$() ").append(surfaceTemperature).append(" C$(br)");
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
                            String oreKey = ore.getKey();
                            // Planet-specific etrium ores should just display as "Etrium"
                            if (oreKey.endsWith("_etrium")) {
                                oreKey = "etrium";
                            }
                            String oreName = capitalizeWords(oreKey.replace("_", " "));
                            oresText.append("• ").append(oreName);
                            int veins = ore.getValue();
                            // Only three rarity levels: Common, Uncommon, Rare
                            if (veins >= 20) {
                                oresText.append(" (Common)");
                            } else if (veins >= 10) {
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

        // Tectonic worldgen configuration methods

        /**
         * Configure crater generation for this planet
         * @param config Crater configuration with frequency, size, depth, rim properties
         * @return this builder for chaining
         */
        public PlanetBuilder withCraters(CraterConfig config) {
            this.craterConfig = config;
            return this;
        }

        /**
         * Configure canyon generation for this planet
         * @param config Canyon configuration with depth, width, sinuosity, branching
         * @return this builder for chaining
         */
        public PlanetBuilder withCanyons(CanyonConfig config) {
            this.canyonConfig = config;
            return this;
        }

        /**
         * Configure volcano generation for this planet
         * @param config Volcano configuration with height, slope, caldera, lava flows
         * @return this builder for chaining
         */
        public PlanetBuilder withVolcanoes(VolcanoConfig config) {
            this.volcanoConfig = config;
            return this;
        }

        /**
         * Configure polar cap generation for this planet
         * @param config Polar cap configuration with size, thickness, composition
         * @return this builder for chaining
         */
        public PlanetBuilder withPolarCaps(PolarCapConfig config) {
            this.polarCapConfig = config;
            return this;
        }

        /**
         * Configure dune generation for this planet
         * @param config Dune configuration with wavelength, height, orientation
         * @return this builder for chaining
         */
        public PlanetBuilder withDunes(DuneConfig config) {
            this.duneConfig = config;
            return this;
        }

        /**
         * Configure maria (dark plains) generation for this planet
         * @param config Maria configuration with size, depth, composition
         * @return this builder for chaining
         */
        public PlanetBuilder withMaria(MariaConfig config) {
            this.mariaConfig = config;
            return this;
        }

        /**
         * Configure atmospheric visual effects for this planet
         * @param config Atmosphere configuration with haze, fog, color effects
         * @return this builder for chaining
         */
        public PlanetBuilder withAtmosphericEffects(AtmosphereConfig config) {
            this.atmosphereEffectsConfig = config;
            return this;
        }

        /**
         * Configure scarp (cliff face) generation for this planet
         * @param config Scarp configuration with height, steepness, frequency
         * @return this builder for chaining
         */
        public PlanetBuilder withScarps(ScarpConfig config) {
            this.scarpConfig = config;
            return this;
        }

        /**
         * Configure basin generation for this planet
         * @param config Basin configuration with size, depth, features
         * @return this builder for chaining
         */
        public PlanetBuilder withBasins(BasinConfig config) {
            this.basinConfig = config;
            return this;
        }

        /**
         * Configure regolith layer depth for this planet
         * @param config Regolith configuration with min/max depth, variation
         * @return this builder for chaining
         */
        public PlanetBuilder withRegolithDepth(RegolithConfig config) {
            this.regolithConfig = config;
            return this;
        }

        /**
         * Add a custom biome with detailed configuration
         * @param biomeName Biome identifier (will be prefixed with planet name)
         * @param config Biome configuration with temperature, colors, blocks, features
         * @return this builder for chaining
         */
        public PlanetBuilder addBiome(String biomeName, BiomeConfig config) {
            this.biomeConfigs.add(new BiomeConfigEntry(biomeName, config));
            return this;
        }

        /**
         * Configure biome distribution zones (altitude, noise-based, feature-based)
         * @param config Biome zone configuration with zone definitions
         * @return this builder for chaining
         */
        public PlanetBuilder withBiomeZones(BiomeZoneConfig config) {
            this.biomeZoneConfig = config;
            return this;
        }

        /**
         * Configure Tectonic noise integration
         * @param config Tectonic noise configuration with continents, erosion, ridges
         * @return this builder for chaining
         */
        public PlanetBuilder withTectonicNoise(TectonicNoiseConfig config) {
            this.tectonicNoiseConfig = config;
            return this;
        }

        /**
         * Configure custom surface rules for terrain generation
         * @param config Surface rule configuration with biome-specific rules
         * @return this builder for chaining
         */
        public PlanetBuilder withSurfaceRules(SurfaceRuleConfig config) {
            this.surfaceRuleConfig = config;
            return this;
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
         * Inner class for biome configuration entries
         */
        private static class BiomeConfigEntry {
            final String biomeName;
            final BiomeConfig config;

            BiomeConfigEntry(String biomeName, BiomeConfig config) {
                this.biomeName = biomeName;
                this.config = config;
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
        new File(RESOURCES_PATH + "tags/worldgen/biome").mkdirs();  // For biome tags per planet
        new File(RESOURCES_PATH + "neoforge/biome_modifier").mkdirs();  // For NeoForge biome modifiers
        new File("src/main/resources/assets/adastramekanized/patchouli_books/journal/en_us/entries/known_planets").mkdirs();
    }

    /**
     * Generate all files for a planet using Moon's proven TerraBlender approach
     */
    private static void generatePlanetFiles(PlanetBuilder planet) throws IOException {
        generatePlanetData(planet);
        generateDimensionData(planet);
        generateDimensionType(planet);

        // Generate custom noise and density functions if coordinate shifting is enabled
        // This must happen BEFORE generateNoiseSettings, as noise settings reference these files
        boolean useCustomDensityFunctions = planet.coordinateShiftX != 0 ||
                                            planet.coordinateShiftZ != 0 ||
                                            planet.customSalt != null ||
                                            planet.noiseScaleXZ != 1.0 ||
                                            planet.noiseScaleY != 1.0;

        if (useCustomDensityFunctions) {
            AdAstraMekanized.LOGGER.info("Generating custom terrain for planet '{}' with coordinate-shifted noise", planet.name);
            generateCustomNoiseFiles(planet);           // Generate custom noise sources
            generateCustomDensityFunctions(planet);     // Generate shifted_noise wrappers
        }

        generateNoiseSettings(planet);

        // Generate ore features if enabled
        if (planet.oreVeinsEnabled || planet.customOreVeins.size() > 0) {
            generateOreFeatures(planet);
        }

        // Generate custom biomes with features
        generateCustomBiomes(planet);

        // Generate biome tag for this planet (groups all planet biomes for biome_modifiers)
        generateBiomeTag(planet);

        // Generate biome modifier for mob spawning (NeoForge system)
        generateBiomeModifier(planet);

        // Generate biome tags for modded structure support
        generateStructureBiomeTags(planet);
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
        atmosphere.addProperty("breathable", planet.atmosphereBreathable);
        atmosphere.addProperty("pressure", planet.hasAtmosphere ? 1.0f : 0.0f);
        atmosphere.addProperty("oxygen_level", planet.atmosphereBreathable ? 0.21f : 0.0f);
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
     * Generate a deterministic salt value from planet ID for unique noise generation.
     * Uses hash code to ensure each planet gets a unique but reproducible salt.
     *
     * @param planetId The planet identifier
     * @return Positive integer salt value
     */
    private static int generateSaltFromPlanetId(String planetId) {
        // Use hash code for deterministic salt generation
        int hash = planetId.hashCode();
        // Ensure positive value and keep in reasonable range
        return Math.abs(hash % 100000);
    }

    /**
     * Get the salt for a planet, using custom salt if set, otherwise auto-generate.
     *
     * @param planet The planet builder
     * @return Salt value for noise generation
     */
    private static int getPlanetSalt(PlanetBuilder planet) {
        if (planet.customSalt != null) {
            return planet.customSalt;
        }
        return generateSaltFromPlanetId(planet.name);
    }

    /**
     * Generate coordinate shift for a planet. If not explicitly set, derive from planet ID hash.
     * Ensures each planet samples a different region of noise space.
     *
     * @param planetId The planet identifier
     * @param axis Which axis (0 for X, 1 for Z)
     * @return Coordinate shift value (0-5000 range)
     */
    private static int generateCoordinateShift(String planetId, int axis) {
        // Combine planet ID with axis to get different values for X and Z
        int hash = (planetId + "_axis" + axis).hashCode();
        // Map to 0-5000 range
        return Math.abs(hash % 5000);
    }

    /**
     * Create a simplified final_density that directly uses planet-specific noise
     * instead of vanilla's complex intermediate functions
     */
    private static com.google.gson.JsonElement createSimplifiedFinalDensity(String planetId) {
        JsonObject finalDensity = new JsonObject();
        finalDensity.addProperty("type", "minecraft:add");

        // Base terrain from depth * factor
        JsonObject terrainBase = new JsonObject();
        terrainBase.addProperty("type", "minecraft:mul");
        terrainBase.addProperty("argument1", "adastramekanized:" + planetId + "/depth");
        terrainBase.addProperty("argument2", "adastramekanized:" + planetId + "/factor");

        // Y-gradient for height limits
        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_value", 1.0);
        yGradient.addProperty("from_y", -64);
        yGradient.addProperty("to_value", -1.0);
        yGradient.addProperty("to_y", 320);

        // Combine terrain with Y gradient
        JsonObject combined = new JsonObject();
        combined.addProperty("type", "minecraft:mul");
        combined.add("argument1", terrainBase);
        combined.add("argument2", yGradient);

        finalDensity.add("argument1", combined);
        finalDensity.addProperty("argument2", 0.0);

        return finalDensity;
    }

    /**
     * Create simplified initial_density using planet-specific depth and factor
     */
    private static com.google.gson.JsonElement createSimplifiedInitialDensity(String planetId) {
        JsonObject initialDensity = new JsonObject();
        initialDensity.addProperty("type", "minecraft:mul");
        initialDensity.addProperty("argument1", "adastramekanized:" + planetId + "/depth");

        // Scale by factor
        JsonObject scaled = new JsonObject();
        scaled.addProperty("type", "minecraft:mul");
        scaled.addProperty("argument1", 4.0);
        scaled.addProperty("argument2", "adastramekanized:" + planetId + "/factor");

        initialDensity.add("argument2", scaled);
        return initialDensity;
    }

    // ========== VANILLA-QUALITY TERRAIN SYSTEM ==========
    // Based on vanilla's terrain shaping architecture but with planet-specific noise

    /**
     * Create enhanced factor function using continents, erosion, and ridges
     * This replaces the simple continents × erosion multiplication with
     * vanilla-inspired multi-layer composition for better terrain variety
     */
    private static com.google.gson.JsonElement createEnhancedFactor(String planetId, TerrainTweaks tweaks) {
        // Factor combines multiple noise sources for terrain variation
        // Base: continents × erosion (continental shape + weathering)
        JsonObject base = new JsonObject();
        base.addProperty("type", "minecraft:mul");
        base.addProperty("argument1", "adastramekanized:" + planetId + "/continents");

        // Apply erosion intensity tweak
        JsonObject erosionScaled = new JsonObject();
        erosionScaled.addProperty("type", "minecraft:mul");
        erosionScaled.addProperty("argument1", tweaks.erosionIntensity);
        erosionScaled.addProperty("argument2", "adastramekanized:" + planetId + "/erosion");

        base.add("argument2", erosionScaled);

        // Add ridges influence for mountain/valley variation
        JsonObject withRidges = new JsonObject();
        withRidges.addProperty("type", "minecraft:add");
        withRidges.add("argument1", base);

        // Scale ridges with tweak and add to base
        JsonObject scaledRidges = new JsonObject();
        scaledRidges.addProperty("type", "minecraft:mul");
        scaledRidges.addProperty("argument1", 0.3 * tweaks.ridgeStrength);  // Ridge influence with tweak
        scaledRidges.addProperty("argument2", "adastramekanized:" + planetId + "/ridges");

        withRidges.add("argument2", scaledRidges);

        // Apply factor scale tweak and clamp
        JsonObject scaled = new JsonObject();
        scaled.addProperty("type", "minecraft:mul");
        scaled.addProperty("argument1", tweaks.factorScale);
        scaled.add("argument2", withRidges);

        // Clamp to reasonable range
        JsonObject clamped = new JsonObject();
        clamped.addProperty("type", "minecraft:clamp");
        clamped.add("input", scaled);
        clamped.addProperty("min", -1.0);
        clamped.addProperty("max", 1.0);

        return clamped;
    }

    /**
     * Create vanilla-style initial_density_without_jaggedness using enhanced factor
     * Based on upside_down.json lines 181-240 structure
     */
    private static com.google.gson.JsonElement createVanillaStyleInitialDensity(String planetId, TerrainTweaks tweaks) {
        // Vanilla's initial_density structure with planet-specific noise
        JsonObject initialDensity = new JsonObject();
        initialDensity.addProperty("type", "minecraft:add");
        initialDensity.addProperty("argument1", 0.1171875);

        // Vertical gradient from bedrock to surface
        JsonObject lowerGradient = new JsonObject();
        lowerGradient.addProperty("type", "minecraft:y_clamped_gradient");
        lowerGradient.addProperty("from_value", 0.0);
        lowerGradient.addProperty("from_y", -64);
        lowerGradient.addProperty("to_value", 1.0);
        lowerGradient.addProperty("to_y", -40);

        // Upper gradient for build limit
        JsonObject upperGradient = new JsonObject();
        upperGradient.addProperty("type", "minecraft:y_clamped_gradient");
        upperGradient.addProperty("from_value", 1.0);
        upperGradient.addProperty("from_y", 240);
        upperGradient.addProperty("to_value", 0.0);
        upperGradient.addProperty("to_y", 256);

        // Terrain shape from depth × factor
        JsonObject terrainShape = new JsonObject();
        terrainShape.addProperty("type", "minecraft:mul");
        terrainShape.addProperty("argument1", "adastramekanized:" + planetId + "/depth");

        // Cache factor for performance (vanilla uses cache_2d)
        JsonObject cachedFactor = new JsonObject();
        cachedFactor.addProperty("type", "minecraft:cache_2d");
        cachedFactor.add("argument", createEnhancedFactor(planetId, tweaks));
        terrainShape.add("argument2", cachedFactor);

        // Apply quarter_negative (vanilla terrain shaping operation)
        JsonObject quarterNeg = new JsonObject();
        quarterNeg.addProperty("type", "minecraft:quarter_negative");
        quarterNeg.add("argument", terrainShape);

        // Scale terrain shape
        JsonObject scaledShape = new JsonObject();
        scaledShape.addProperty("type", "minecraft:mul");
        scaledShape.addProperty("argument1", 4.0);
        scaledShape.add("argument2", quarterNeg);

        // Offset and clamp
        JsonObject offset = new JsonObject();
        offset.addProperty("type", "minecraft:add");
        offset.addProperty("argument1", -0.703125);
        offset.add("argument2", scaledShape);

        JsonObject clamped = new JsonObject();
        clamped.addProperty("type", "minecraft:clamp");
        clamped.add("input", offset);
        clamped.addProperty("min", -64.0);
        clamped.addProperty("max", 64.0);

        // Combine with upper gradient
        JsonObject withUpper = new JsonObject();
        withUpper.addProperty("type", "minecraft:add");
        withUpper.addProperty("argument1", 0.078125);
        withUpper.add("argument2", clamped);

        JsonObject mulUpper = new JsonObject();
        mulUpper.addProperty("type", "minecraft:mul");
        mulUpper.add("argument1", upperGradient);
        mulUpper.add("argument2", withUpper);

        // Combine lower gradient
        JsonObject subUpper = new JsonObject();
        subUpper.addProperty("type", "minecraft:add");
        subUpper.addProperty("argument1", -0.078125);
        subUpper.add("argument2", mulUpper);

        JsonObject subLower = new JsonObject();
        subLower.addProperty("type", "minecraft:add");
        subLower.addProperty("argument1", -0.1171875);
        subLower.add("argument2", subUpper);

        JsonObject mulLower = new JsonObject();
        mulLower.addProperty("type", "minecraft:mul");
        mulLower.add("argument1", lowerGradient);
        mulLower.add("argument2", subLower);

        initialDensity.add("argument2", mulLower);

        return initialDensity;
    }

    /**
     * Create vanilla-style final_density with planet-specific noise and universal caves
     * Simplified from vanilla to avoid external cave function dependencies while maintaining quality
     */
    private static com.google.gson.JsonElement createVanillaStyleFinalDensity(String planetId, TerrainTweaks tweaks) {
        // Use vanilla cave functions (universal) with custom terrain base
        JsonObject finalDensity = new JsonObject();
        finalDensity.addProperty("type", "minecraft:min");

        // Terrain base (squeeze → interpolated → blend_density for smoothness)
        JsonObject squeeze = new JsonObject();
        squeeze.addProperty("type", "minecraft:squeeze");

        JsonObject interpolated = new JsonObject();
        interpolated.addProperty("type", "minecraft:interpolated");

        JsonObject blendDensity = new JsonObject();
        blendDensity.addProperty("type", "minecraft:blend_density");
        blendDensity.add("argument", createVanillaStyleInitialDensity(planetId, tweaks));

        interpolated.add("argument", blendDensity);

        // Scale for terrain amplitude
        JsonObject scaled = new JsonObject();
        scaled.addProperty("type", "minecraft:mul");
        scaled.addProperty("argument1", 0.64);
        scaled.add("argument2", interpolated);

        squeeze.add("argument", scaled);

        finalDensity.add("argument1", squeeze);

        // Universal vanilla noodle caves
        finalDensity.addProperty("argument2", "minecraft:overworld/caves/noodle");

        return finalDensity;
    }

    /**
     * Create a modulated noise density function with optional offset and multiplier
     */
    private static com.google.gson.JsonElement createModulatedNoise(String baseNoise, double multiplier, double offset) {
        com.google.gson.JsonElement result;

        // Start with base noise reference
        if (multiplier != 1.0) {
            // Wrap in multiplication
            JsonObject mulFunc = new JsonObject();
            mulFunc.addProperty("type", "minecraft:mul");

            // Create constant for multiplier
            JsonObject constMul = new JsonObject();
            constMul.addProperty("type", "minecraft:constant");
            constMul.addProperty("argument", multiplier);

            mulFunc.add("argument1", constMul);
            mulFunc.addProperty("argument2", baseNoise);
            result = mulFunc;
        } else {
            // No multiplication, use base noise as string
            result = new JsonObject();
            ((JsonObject)result).addProperty("ref", baseNoise);
            result = new com.google.gson.JsonPrimitive(baseNoise);
        }

        // Apply offset if set
        if (offset != 0.0) {
            JsonObject addFunc = new JsonObject();
            addFunc.addProperty("type", "minecraft:add");

            // Create constant for offset
            JsonObject constOffset = new JsonObject();
            constOffset.addProperty("type", "minecraft:constant");
            constOffset.addProperty("argument", offset);

            addFunc.add("argument1", result);
            addFunc.add("argument2", constOffset);
            result = addFunc;
        }

        return result;
    }

    /**
     * Generate custom noise file for a planet. Creates a unique noise source with
     * planet-specific salt to ensure different terrain generation.
     *
     * @param planetId The planet identifier
     * @param noiseType The noise type (continents, erosion, depth, ridges)
     * @param salt Unique salt value for this noise
     * @throws IOException If file writing fails
     */
    private static void generateCustomNoiseFile(String planetId, String noiseType, int salt) throws IOException {
        JsonObject noise = new JsonObject();

        // Standard vanilla noise parameters
        noise.addProperty("firstOctave", -9);

        // Amplitudes array - vanilla-like settings
        JsonArray amplitudes = new JsonArray();
        amplitudes.add(1.0);
        amplitudes.add(1.0);
        amplitudes.add(1.0);
        amplitudes.add(1.0);
        amplitudes.add(1.0);
        amplitudes.add(0.0);
        amplitudes.add(0.0);
        noise.add("amplitudes", amplitudes);

        // Custom salt for unique noise per planet
        noise.addProperty("salt", salt);

        // Write to worldgen/noise/ directory
        String filename = planetId + "_" + noiseType;
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + filename + ".json", noise);

        AdAstraMekanized.LOGGER.info("Generated custom noise: worldgen/noise/{}.json (salt: {})", filename, salt);
    }

    /**
     * Generate all custom noise files for a planet (continents, erosion, depth, ridges).
     * Each noise file gets a unique salt derived from the planet ID and noise type.
     *
     * @param planet The planet builder
     * @throws IOException If file writing fails
     */
    private static void generateCustomNoiseFiles(PlanetBuilder planet) throws IOException {
        int baseSalt = getPlanetSalt(planet);

        // Generate 4 key noise files with unique salts
        String[] noiseTypes = {"continents", "erosion", "depth", "ridges"};
        for (int i = 0; i < noiseTypes.length; i++) {
            String noiseType = noiseTypes[i];
            // Offset salt by index to ensure each type is unique
            int noiseSalt = baseSalt + (i * 1000);
            generateCustomNoiseFile(planet.name, noiseType, noiseSalt);
        }
    }

    /**
     * Generate a shifted_noise density function that applies coordinate transformation
     * to a VANILLA noise source. This creates unique terrain by sampling different
     * regions of the noise space while maintaining vanilla terrain quality.
     *
     * @param planetId The planet identifier
     * @param noiseType The noise type (continents, erosion, depth, ridges)
     * @param shiftX X-axis coordinate shift
     * @param shiftZ Z-axis coordinate shift
     * @param scaleXZ Horizontal noise scale
     * @param scaleY Vertical noise scale
     * @throws IOException If file writing fails
     */
    private static void generateShiftedNoiseDensityFunction(String planetId, String noiseType,
                                                             int shiftX, int shiftZ,
                                                             double scaleXZ, double scaleY) throws IOException {
        JsonObject densityFunc = new JsonObject();

        densityFunc.addProperty("type", "minecraft:shifted_noise");

        // Map noise type to vanilla noise source for vanilla-quality terrain
        // These are the same noise sources used by vanilla Overworld
        String vanillaNoise = switch (noiseType) {
            case "continents" -> "minecraft:continentalness";
            case "erosion" -> "minecraft:erosion";
            case "depth" -> "minecraft:ridge";  // Depth uses ridge noise in vanilla
            case "ridges" -> "minecraft:ridge";
            default -> "minecraft:continentalness";
        };
        densityFunc.addProperty("noise", vanillaNoise);

        // Noise scaling - use vanilla-like values
        densityFunc.addProperty("xz_scale", scaleXZ);
        densityFunc.addProperty("y_scale", scaleY);

        // Shift X - use vanilla shift_x reference plus our coordinate offset
        // This applies our offset on top of vanilla's shift mechanism
        JsonObject shiftXObj = new JsonObject();
        shiftXObj.addProperty("type", "minecraft:add");
        shiftXObj.addProperty("argument1", "minecraft:shift_x");
        shiftXObj.addProperty("argument2", (double) shiftX);
        densityFunc.add("shift_x", shiftXObj);

        // Shift Y - always 0 (no vertical shift needed)
        densityFunc.addProperty("shift_y", 0);

        // Shift Z - use vanilla shift_z reference plus our coordinate offset
        JsonObject shiftZObj = new JsonObject();
        shiftZObj.addProperty("type", "minecraft:add");
        shiftZObj.addProperty("argument1", "minecraft:shift_z");
        shiftZObj.addProperty("argument2", (double) shiftZ);
        densityFunc.add("shift_z", shiftZObj);

        // Write to worldgen/density_function/<planetId>/ directory
        String path = RESOURCES_PATH + "worldgen/density_function/" + planetId + "/";
        writeJsonFile(path + noiseType + ".json", densityFunc);

        AdAstraMekanized.LOGGER.info("Generated vanilla-quality density function: {}{}.json (shift: {}, {}) using {}",
                path.replace(RESOURCES_PATH, ""), noiseType, shiftX, shiftZ, vanillaNoise);
    }

    /**
     * Generate coordinate-shifted terrain density functions (initial_density_without_jaggedness and final_density).
     * These are self-contained terrain generators that use ONLY our shifted noise, not vanilla references.
     *
     * @param planet The planet builder
     * @param shiftX X coordinate shift
     * @param shiftZ Z coordinate shift
     * @throws IOException If file writing fails
     */
    private static void generateCoordinateShiftedTerrainDensityFunctions(PlanetBuilder planet, int shiftX, int shiftZ) throws IOException {
        String path = RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/";

        // Generate initial_density_without_jaggedness
        // This combines depth and continents to create the basic terrain shape
        JsonObject initialDensity = new JsonObject();
        initialDensity.addProperty("type", "minecraft:cache_2d");

        // Combine depth with continents-based factor
        JsonObject depthMul = new JsonObject();
        depthMul.addProperty("type", "minecraft:mul");
        depthMul.addProperty("argument1", "adastramekanized:" + planet.name + "/depth");

        // Factor based on continents (higher continents = more terrain variation)
        JsonObject factor = new JsonObject();
        factor.addProperty("type", "minecraft:add");

        JsonObject continentsMul = new JsonObject();
        continentsMul.addProperty("type", "minecraft:mul");
        continentsMul.addProperty("argument1", "adastramekanized:" + planet.name + "/continents");
        continentsMul.addProperty("argument2", 10.0);  // Scale factor

        factor.add("argument1", continentsMul);
        factor.addProperty("argument2", 1.0);  // Base factor

        depthMul.add("argument2", factor);
        initialDensity.add("argument", depthMul);

        writeJsonFile(path + "initial_density_without_jaggedness.json", initialDensity);

        // Generate final_density
        // This is a self-contained terrain generator using ONLY our shifted noise
        // No references to vanilla overworld functions
        JsonObject finalDensity = new JsonObject();
        finalDensity.addProperty("type", "minecraft:interpolated");

        // Combine multiple noise sources for terrain
        JsonObject terrainCombined = new JsonObject();
        terrainCombined.addProperty("type", "minecraft:add");

        // Y-gradient for base terrain shape (solid below, air above)
        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_y", -64);
        yGradient.addProperty("to_y", 320);
        yGradient.addProperty("from_value", 1.5);   // Solid at bottom
        yGradient.addProperty("to_value", -1.5);   // Air at top

        terrainCombined.add("argument1", yGradient);

        // Add our shifted noise for terrain variation
        JsonObject noiseContribution = new JsonObject();
        noiseContribution.addProperty("type", "minecraft:add");

        // Continents noise - large-scale terrain features
        // Use configurable multiplier (default 0.4, higher = more continental influence)
        double continentsScale = 0.4 * planet.continentsMultiplier;
        JsonObject continentsScaled = new JsonObject();
        continentsScaled.addProperty("type", "minecraft:mul");
        continentsScaled.addProperty("argument1", "adastramekanized:" + planet.name + "/continents");
        continentsScaled.addProperty("argument2", continentsScale);

        noiseContribution.add("argument1", continentsScaled);

        // Erosion noise - medium-scale features (valleys, plateaus)
        // Use configurable multiplier (default 0.2, higher = more erosion features)
        double erosionScale = 0.2 * planet.erosionMultiplier;
        JsonObject erosionScaled = new JsonObject();
        erosionScaled.addProperty("type", "minecraft:mul");
        erosionScaled.addProperty("argument1", "adastramekanized:" + planet.name + "/erosion");
        erosionScaled.addProperty("argument2", erosionScale);

        noiseContribution.add("argument2", erosionScaled);

        terrainCombined.add("argument2", noiseContribution);

        // Add ridges for mountain peaks
        // Use configurable multiplier (default 0.15, higher = more dramatic peaks)
        double ridgesScale = 0.15 * planet.ridgesMultiplier;
        JsonObject withRidges = new JsonObject();
        withRidges.addProperty("type", "minecraft:add");
        withRidges.add("argument1", terrainCombined);

        JsonObject ridgesScaled = new JsonObject();
        ridgesScaled.addProperty("type", "minecraft:mul");
        ridgesScaled.addProperty("argument1", "adastramekanized:" + planet.name + "/ridges");
        ridgesScaled.addProperty("argument2", ridgesScale);

        withRidges.add("argument2", ridgesScaled);

        finalDensity.add("argument", withRidges);

        writeJsonFile(path + "final_density.json", finalDensity);

        AdAstraMekanized.LOGGER.info("Generated terrain for '{}': shift({},{}), continents={:.2f}, erosion={:.2f}, ridges={:.2f}",
            planet.name, shiftX, shiftZ, continentsScale, erosionScale, ridgesScale);
    }

    /**
     * Generate VANILLA-QUALITY density functions for a planet.
     * This copies the full vanilla density function set (offset, factor, jaggedness splines)
     * and replaces 'minecraft:overworld/' references with planet-specific references.
     *
     * This produces terrain with vanilla-quality spline-based terrain shaping,
     * but unique per planet due to coordinate shifting.
     *
     * Files generated:
     * - continents.json (coordinate-shifted)
     * - erosion.json (coordinate-shifted)
     * - ridges.json (coordinate-shifted)
     * - ridges_folded.json (references ridges)
     * - offset.json (copied from vanilla, references replaced)
     * - factor.json (copied from vanilla, references replaced)
     * - jaggedness.json (copied from vanilla, references replaced)
     * - depth.json (references offset)
     * - base_3d_noise.json (configurable parameters)
     * - sloped_cheese.json (configurable parameters)
     *
     * @param planet The planet builder
     * @param shiftX X coordinate shift
     * @param shiftZ Z coordinate shift
     * @throws IOException If file operations fail
     */
    private static void generateVanillaQualityDensityFunctions(PlanetBuilder planet, int shiftX, int shiftZ) throws IOException {
        String path = RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/";
        String templatePath = RESOURCES_PATH + "worldgen/density_function_templates/";
        String planetRef = "adastramekanized:" + planet.name;

        // Create directory
        new File(path).mkdirs();

        // 1. Generate coordinate-shifted continents
        JsonObject continents = new JsonObject();
        continents.addProperty("type", "minecraft:flat_cache");
        JsonObject continentsInner = new JsonObject();
        continentsInner.addProperty("type", "minecraft:shifted_noise");
        continentsInner.addProperty("noise", "minecraft:continentalness");
        // Add coordinate shift by using offset in shift_x/shift_z
        JsonObject shiftXObj = new JsonObject();
        shiftXObj.addProperty("type", "minecraft:add");
        shiftXObj.addProperty("argument1", "minecraft:shift_x");
        shiftXObj.addProperty("argument2", (double) shiftX);
        continentsInner.add("shift_x", shiftXObj);
        continentsInner.addProperty("shift_y", 0.0);
        JsonObject shiftZObj = new JsonObject();
        shiftZObj.addProperty("type", "minecraft:add");
        shiftZObj.addProperty("argument1", "minecraft:shift_z");
        shiftZObj.addProperty("argument2", (double) shiftZ);
        continentsInner.add("shift_z", shiftZObj);
        continentsInner.addProperty("xz_scale", 0.25);
        continentsInner.addProperty("y_scale", 0.0);
        continents.add("argument", continentsInner);
        writeJsonFile(path + "continents.json", continents);

        // 2. Generate coordinate-shifted erosion
        JsonObject erosion = new JsonObject();
        erosion.addProperty("type", "minecraft:flat_cache");
        JsonObject erosionInner = new JsonObject();
        erosionInner.addProperty("type", "minecraft:shifted_noise");
        erosionInner.addProperty("noise", "minecraft:erosion");
        erosionInner.add("shift_x", shiftXObj.deepCopy());
        erosionInner.addProperty("shift_y", 0.0);
        erosionInner.add("shift_z", shiftZObj.deepCopy());
        erosionInner.addProperty("xz_scale", 0.25);
        erosionInner.addProperty("y_scale", 0.0);
        erosion.add("argument", erosionInner);
        writeJsonFile(path + "erosion.json", erosion);

        // 3. Generate coordinate-shifted ridges
        JsonObject ridges = new JsonObject();
        ridges.addProperty("type", "minecraft:flat_cache");
        JsonObject ridgesInner = new JsonObject();
        ridgesInner.addProperty("type", "minecraft:shifted_noise");
        ridgesInner.addProperty("noise", "minecraft:ridge");
        ridgesInner.add("shift_x", shiftXObj.deepCopy());
        ridgesInner.addProperty("shift_y", 0.0);
        ridgesInner.add("shift_z", shiftZObj.deepCopy());
        ridgesInner.addProperty("xz_scale", 0.25);
        ridgesInner.addProperty("y_scale", 0.0);
        ridges.add("argument", ridgesInner);
        writeJsonFile(path + "ridges.json", ridges);

        // 4. Generate ridges_folded (references planet's ridges)
        JsonObject ridgesFolded = new JsonObject();
        ridgesFolded.addProperty("type", "minecraft:mul");
        ridgesFolded.addProperty("argument1", -3.0);
        JsonObject rfAdd = new JsonObject();
        rfAdd.addProperty("type", "minecraft:add");
        rfAdd.addProperty("argument1", -0.3333333333333333);
        JsonObject rfAbs = new JsonObject();
        rfAbs.addProperty("type", "minecraft:abs");
        JsonObject rfInnerAdd = new JsonObject();
        rfInnerAdd.addProperty("type", "minecraft:add");
        rfInnerAdd.addProperty("argument1", -0.6666666666666666);
        JsonObject rfInnerAbs = new JsonObject();
        rfInnerAbs.addProperty("type", "minecraft:abs");
        rfInnerAbs.addProperty("argument", planetRef + "/ridges");
        rfInnerAdd.add("argument2", rfInnerAbs);
        rfAbs.add("argument", rfInnerAdd);
        rfAdd.add("argument2", rfAbs);
        ridgesFolded.add("argument2", rfAdd);
        writeJsonFile(path + "ridges_folded.json", ridgesFolded);

        // 5. Copy and transform offset.json (use flat or vanilla based on useFlatSplines)
        if (planet.useFlatSplines) {
            // Use flat constant value for truly flat terrain
            String offsetContent = readFileToString(templatePath + "flat_offset.json");
            writeStringToFile(path + "offset.json", offsetContent);
            AdAstraMekanized.LOGGER.info("Planet '{}' using FLAT offset spline (constant value)", planet.name);
        } else {
            String offsetContent = readFileToString(templatePath + "vanilla_offset.json");
            offsetContent = offsetContent.replace("minecraft:overworld/", planetRef + "/");
            writeStringToFile(path + "offset.json", offsetContent);
        }

        // 6. Copy and transform factor.json (use flat or vanilla based on useFlatSplines)
        if (planet.useFlatSplines) {
            // Use minimal constant factor for flat terrain
            String factorContent = readFileToString(templatePath + "flat_factor.json");
            writeStringToFile(path + "factor.json", factorContent);
            AdAstraMekanized.LOGGER.info("Planet '{}' using FLAT factor spline (minimal value)", planet.name);
        } else {
            String factorContent = readFileToString(templatePath + "vanilla_factor.json");
            factorContent = factorContent.replace("minecraft:overworld/", planetRef + "/");
            writeStringToFile(path + "factor.json", factorContent);
        }

        // 7. Copy and transform jaggedness.json (use flat or vanilla based on useFlatSplines)
        if (planet.useFlatSplines) {
            // Use zero jaggedness for no mountains
            String jaggednessContent = readFileToString(templatePath + "flat_jaggedness.json");
            writeStringToFile(path + "jaggedness.json", jaggednessContent);
            AdAstraMekanized.LOGGER.info("Planet '{}' using FLAT jaggedness spline (zero value)", planet.name);
        } else {
            String jaggednessContent = readFileToString(templatePath + "vanilla_jaggedness.json");
            jaggednessContent = jaggednessContent.replace("minecraft:overworld/", planetRef + "/");
            writeStringToFile(path + "jaggedness.json", jaggednessContent);
        }

        // 8. Generate depth.json (references planet's offset)
        JsonObject depth = new JsonObject();
        depth.addProperty("type", "minecraft:add");
        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_value", 1.5);
        yGradient.addProperty("from_y", -64);
        yGradient.addProperty("to_value", -1.5);
        yGradient.addProperty("to_y", 320);
        depth.add("argument1", yGradient);
        depth.addProperty("argument2", planetRef + "/offset");
        writeJsonFile(path + "depth.json", depth);

        // 9. Generate base_3d_noise.json (configurable)
        JsonObject base3dNoise = new JsonObject();
        base3dNoise.addProperty("type", "minecraft:old_blended_noise");
        base3dNoise.addProperty("smear_scale_multiplier", planet.smearScaleMultiplier);
        base3dNoise.addProperty("xz_factor", planet.base3DNoiseXZFactor);
        base3dNoise.addProperty("xz_scale", planet.base3DNoiseXZScale);
        base3dNoise.addProperty("y_factor", planet.base3DNoiseYFactor);
        base3dNoise.addProperty("y_scale", planet.base3DNoiseYScale);
        writeJsonFile(path + "base_3d_noise.json", base3dNoise);

        // 10. Generate sloped_cheese.json (configurable multiplier)
        JsonObject slopedCheese = new JsonObject();
        slopedCheese.addProperty("type", "minecraft:add");
        // First argument: 4.0 * quarter_negative(...)
        JsonObject terrainMul = new JsonObject();
        terrainMul.addProperty("type", "minecraft:mul");
        terrainMul.addProperty("argument1", (double) planet.terrainFactor);  // Configurable (default 4.0)
        JsonObject quarterNeg = new JsonObject();
        quarterNeg.addProperty("type", "minecraft:quarter_negative");
        JsonObject innerMul = new JsonObject();
        innerMul.addProperty("type", "minecraft:mul");
        // (depth + jaggedness * half_negative(jagged_noise))
        JsonObject depthPlusJagged = new JsonObject();
        depthPlusJagged.addProperty("type", "minecraft:add");
        depthPlusJagged.addProperty("argument1", planetRef + "/depth");
        JsonObject jaggedMul = new JsonObject();
        jaggedMul.addProperty("type", "minecraft:mul");
        jaggedMul.addProperty("argument1", planetRef + "/jaggedness");
        JsonObject halfNeg = new JsonObject();
        halfNeg.addProperty("type", "minecraft:half_negative");
        JsonObject jaggedNoise = new JsonObject();
        jaggedNoise.addProperty("type", "minecraft:noise");
        jaggedNoise.addProperty("noise", "minecraft:jagged");
        jaggedNoise.addProperty("xz_scale", (double) planet.jaggednessNoiseScale);  // Configurable (default 1500.0)
        jaggedNoise.addProperty("y_scale", 0.0);
        halfNeg.add("argument", jaggedNoise);
        jaggedMul.add("argument2", halfNeg);
        depthPlusJagged.add("argument2", jaggedMul);
        innerMul.add("argument1", depthPlusJagged);
        innerMul.addProperty("argument2", planetRef + "/factor");
        quarterNeg.add("argument", innerMul);
        terrainMul.add("argument2", quarterNeg);
        slopedCheese.add("argument1", terrainMul);
        // Second argument: base_3d_noise
        slopedCheese.addProperty("argument2", planetRef + "/base_3d_noise");
        writeJsonFile(path + "sloped_cheese.json", slopedCheese);

        // 11. Generate final_density.json with vanilla-accurate cave support
        // Uses VANILLA_FINAL_DENSITY_TEMPLATE with proper depth masking and cave integration
        // The template includes:
        //   - y_clamped_gradient for depth masking (Y=-64 to -40) - CRITICAL for preventing surface carving
        //   - y_clamped_gradient for top fade (Y=240 to 256) - prevents terrain at build limit
        //   - range_choice for proper terrain/cave branching based on sloped_cheese threshold
        //   - Full vanilla cave system: cheese caves, entrances, spaghetti_2d, pillars, noodle
        // Only replaces sloped_cheese references with planet-specific version; cave functions use vanilla
        String finalDensityJson = VANILLA_FINAL_DENSITY_TEMPLATE
            .replace("minecraft:overworld/sloped_cheese", planetRef + "/sloped_cheese");
        writeStringToFile(path + "final_density.json", finalDensityJson);

        AdAstraMekanized.LOGGER.info("Generated VANILLA-QUALITY density functions for planet '{}' with shift ({}, {})",
            planet.name, shiftX, shiftZ);
        AdAstraMekanized.LOGGER.info("  - terrainFactor: {}, jaggedNoiseScale: {}", planet.terrainFactor, planet.jaggednessNoiseScale);
        AdAstraMekanized.LOGGER.info("  - base3D: xzScale={}, yScale={}, xzFactor={}, yFactor={}, smear={}",
            planet.base3DNoiseXZScale, planet.base3DNoiseYScale, planet.base3DNoiseXZFactor, planet.base3DNoiseYFactor, planet.smearScaleMultiplier);
    }

    /**
     * Read a file to a string.
     */
    private static String readFileToString(String filePath) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
    }

    /**
     * Write a string to a file.
     */
    private static void writeStringToFile(String filePath, String content) throws IOException {
        java.nio.file.Files.write(java.nio.file.Paths.get(filePath), content.getBytes());
    }

    /**
     * Generate all custom density functions for a planet. Creates shifted_noise wrappers
     * for the 4 key terrain functions: continents, erosion, depth, ridges.
     * Also generates initial_density_without_jaggedness and final_density for actual terrain generation.
     *
     * @param planet The planet builder
     * @throws IOException If file writing fails
     */
    private static void generateCustomDensityFunctions(PlanetBuilder planet) throws IOException {
        // Use explicit shifts if set, otherwise auto-generate from planet ID
        int shiftX = planet.coordinateShiftX != 0 ? planet.coordinateShiftX :
                     generateCoordinateShift(planet.name, 0);
        int shiftZ = planet.coordinateShiftZ != 0 ? planet.coordinateShiftZ :
                     generateCoordinateShift(planet.name, 1);

        // VANILLA-QUALITY MODE: Generate full vanilla density function set with coordinate shifting
        // This produces terrain with vanilla-quality spline-based terrain shaping
        if (planet.useVanillaQualityTerrain) {
            generateVanillaQualityDensityFunctions(planet, shiftX, shiftZ);
            return;
        }

        // LEGACY MODE: Simplified terrain generation (less vanilla-accurate)
        // Generate 4 key density function files (for biome selection)
        String[] noiseTypes = {"continents", "erosion", "depth", "ridges"};
        for (String noiseType : noiseTypes) {
            generateShiftedNoiseDensityFunction(
                planet.name,
                noiseType,
                shiftX,
                shiftZ,
                planet.noiseScaleXZ,
                planet.noiseScaleY
            );
        }

        // Generate initial_density_without_jaggedness and final_density for actual 3D terrain
        // These use coordinate-shifted noise to produce unique terrain per planet
        generateCoordinateShiftedTerrainDensityFunctions(planet, shiftX, shiftZ);

        AdAstraMekanized.LOGGER.info("Generated {} custom density functions for planet '{}' with coordinate shift ({}, {})",
                noiseTypes.length, planet.name, shiftX, shiftZ);
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

        // Check if using vanilla noise reference (RECOMMENDED)
        if (planet.useVanillaNoise) {
            // Reference vanilla noise density functions - proven and tested!
            // This creates a noise_router object that references vanilla density functions
            JsonObject noiseRouter = new JsonObject();
            noiseRouter.addProperty("barrier", planet.vanillaNoiseReference + "/noise_router/barrier");
            noiseRouter.addProperty("fluid_level_floodedness", planet.vanillaNoiseReference + "/noise_router/fluid_level_floodedness");
            noiseRouter.addProperty("fluid_level_spread", planet.vanillaNoiseReference + "/noise_router/fluid_level_spread");
            noiseRouter.addProperty("lava", planet.vanillaNoiseReference + "/noise_router/lava");
            noiseRouter.addProperty("temperature", planet.vanillaNoiseReference + "/noise_router/temperature");
            noiseRouter.addProperty("vegetation", planet.vanillaNoiseReference + "/noise_router/vegetation");

            // Check if coordinate shifting is enabled (for unique terrain per planet)
            boolean hasCoordinateShift = planet.coordinateShiftX != 0 || planet.coordinateShiftZ != 0;

            // IDENTICAL VANILLA TERRAIN MODE - Use direct vanilla references for everything
            // This produces terrain that is byte-for-byte identical to vanilla Overworld
            // UNLESS coordinate shifting is enabled, in which case we generate unique terrain
            if (planet.useIdenticalVanillaTerrain && !hasCoordinateShift) {
                // All 4 terrain shape functions - direct vanilla references
                noiseRouter.addProperty("continents", planet.vanillaNoiseReference + "/noise_router/continents");
                noiseRouter.addProperty("erosion", planet.vanillaNoiseReference + "/noise_router/erosion");
                noiseRouter.addProperty("depth", planet.vanillaNoiseReference + "/noise_router/depth");
                noiseRouter.addProperty("ridges", planet.vanillaNoiseReference + "/noise_router/ridges");
                // 3D terrain and caves - direct vanilla references (includes full cave system)
                noiseRouter.addProperty("initial_density_without_jaggedness", planet.vanillaNoiseReference + "/noise_router/initial_density_without_jaggedness");
                noiseRouter.addProperty("final_density", planet.vanillaNoiseReference + "/noise_router/final_density");

                AdAstraMekanized.LOGGER.info("Planet '{}' using IDENTICAL vanilla terrain from '{}'", planet.name, planet.vanillaNoiseReference);
            } else if (planet.useIdenticalVanillaTerrain && hasCoordinateShift) {
                // COORDINATE-SHIFTED VANILLA TERRAIN MODE (LEGACY - simplified terrain)
                // Uses custom density functions that wrap vanilla noise with coordinate offsets
                // This gives vanilla-quality terrain but at different "locations" in noise space
                noiseRouter.addProperty("continents", "adastramekanized:" + planet.name + "/continents");
                noiseRouter.addProperty("erosion", "adastramekanized:" + planet.name + "/erosion");
                noiseRouter.addProperty("depth", "adastramekanized:" + planet.name + "/depth");
                noiseRouter.addProperty("ridges", "adastramekanized:" + planet.name + "/ridges");
                // CRITICAL: Use custom initial_density and final_density that reference our shifted noise
                // The vanilla functions have hardcoded noise references that don't use our custom entries!
                noiseRouter.addProperty("initial_density_without_jaggedness", "adastramekanized:" + planet.name + "/initial_density_without_jaggedness");
                noiseRouter.addProperty("final_density", "adastramekanized:" + planet.name + "/final_density");

                AdAstraMekanized.LOGGER.info("Planet '{}' using vanilla terrain with coordinate shift ({}, {})",
                    planet.name, planet.coordinateShiftX, planet.coordinateShiftZ);
            } else if (planet.useVanillaQualityTerrain) {
                // VANILLA-QUALITY TERRAIN MODE - Full vanilla density function set with coordinate shifting
                // This uses the complete vanilla spline system (offset, factor, jaggedness) for authentic terrain
                // with unique results per planet due to coordinate shifting
                String planetRef = "adastramekanized:" + planet.name;
                noiseRouter.addProperty("continents", planetRef + "/continents");
                noiseRouter.addProperty("erosion", planetRef + "/erosion");
                noiseRouter.addProperty("depth", planetRef + "/depth");
                noiseRouter.addProperty("ridges", planetRef + "/ridges");
                // Use the full final_density which includes:
                // - sloped_cheese terrain formula
                // - y_clamped_gradient depth masking (Y=-64 to -40) to protect bedrock layer
                // - range_choice terrain/cave branching
                // - Full vanilla cave system (cheese, spaghetti, entrances, pillars, noodle)
                noiseRouter.addProperty("initial_density_without_jaggedness", planetRef + "/depth");
                noiseRouter.addProperty("final_density", planetRef + "/final_density");

                AdAstraMekanized.LOGGER.info("Planet '{}' using VANILLA-QUALITY terrain (full spline system) with shift ({}, {})",
                    planet.name, planet.coordinateShiftX, planet.coordinateShiftZ);
            } else {
                // Determine if we should use custom density functions (coordinate-shifted terrain)
                // Use custom if: coordinate shifts set, custom salt set, or non-default noise scaling
                boolean useCustomDensityFunctions = planet.coordinateShiftX != 0 ||
                                                    planet.coordinateShiftZ != 0 ||
                                                    planet.customSalt != null ||
                                                    planet.noiseScaleXZ != 1.0 ||
                                                    planet.noiseScaleY != 1.0;

                // For the 4 key terrain functions, use custom or vanilla references
                if (useCustomDensityFunctions) {
                    // Use our custom coordinate-shifted density functions
                    noiseRouter.addProperty("continents", "adastramekanized:" + planet.name + "/continents");
                    noiseRouter.addProperty("erosion", "adastramekanized:" + planet.name + "/erosion");
                    noiseRouter.addProperty("depth", "adastramekanized:" + planet.name + "/depth");
                    noiseRouter.addProperty("ridges", "adastramekanized:" + planet.name + "/ridges");

                    // Use vanilla-quality terrain for proof-of-concept planets (Moon, Mars)
                    // Other planets use simplified approach
                    boolean useVanillaQuality = planet.name.equals("moon") || planet.name.equals("mars");

                    if (useVanillaQuality) {
                        // Vanilla-style composition for quality terrain with per-planet tweaks
                        noiseRouter.add("initial_density_without_jaggedness", createVanillaStyleInitialDensity(planet.name, planet.terrainTweaks));
                        noiseRouter.add("final_density", createVanillaStyleFinalDensity(planet.name, planet.terrainTweaks));
                    } else {
                        // Simplified approach for other planets
                        noiseRouter.add("initial_density_without_jaggedness", createSimplifiedInitialDensity(planet.name));
                        noiseRouter.add("final_density", createSimplifiedFinalDensity(planet.name));
                    }
                } else {
                    // Use vanilla references (backwards compatible with old system)
                    // Apply legacy frequency modulation if set
                    if (planet.continentsMultiplier != 1.0 || planet.continentsNoiseOffset != 0.0) {
                        noiseRouter.add("continents", createModulatedNoise(
                            planet.vanillaNoiseReference + "/noise_router/continents",
                            planet.continentsMultiplier,
                            planet.continentsNoiseOffset
                        ));
                    } else {
                        noiseRouter.addProperty("continents", planet.vanillaNoiseReference + "/noise_router/continents");
                    }

                    if (planet.erosionMultiplier != 1.0 || planet.erosionNoiseOffset != 0.0) {
                        noiseRouter.add("erosion", createModulatedNoise(
                            planet.vanillaNoiseReference + "/noise_router/erosion",
                            planet.erosionMultiplier,
                            planet.erosionNoiseOffset
                        ));
                    } else {
                        noiseRouter.addProperty("erosion", planet.vanillaNoiseReference + "/noise_router/erosion");
                    }

                    if (planet.depthMultiplier != 1.0 || planet.depthNoiseOffset != 0.0) {
                        noiseRouter.add("depth", createModulatedNoise(
                            planet.vanillaNoiseReference + "/noise_router/depth",
                            planet.depthMultiplier,
                            planet.depthNoiseOffset
                        ));
                    } else {
                        noiseRouter.addProperty("depth", planet.vanillaNoiseReference + "/noise_router/depth");
                    }

                    if (planet.ridgesMultiplier != 1.0 || planet.ridgesNoiseOffset != 0.0) {
                        noiseRouter.add("ridges", createModulatedNoise(
                            planet.vanillaNoiseReference + "/noise_router/ridges",
                            planet.ridgesMultiplier,
                            planet.ridgesNoiseOffset
                        ));
                    } else {
                        noiseRouter.addProperty("ridges", planet.vanillaNoiseReference + "/noise_router/ridges");
                    }

                    // For vanilla terrain path, use vanilla references for density functions
                    noiseRouter.addProperty("initial_density_without_jaggedness", planet.vanillaNoiseReference + "/noise_router/initial_density_without_jaggedness");
                    noiseRouter.addProperty("final_density", planet.vanillaNoiseReference + "/noise_router/final_density");
                }
            }

            // Common noise router properties for both custom and vanilla terrain
            noiseRouter.addProperty("vein_toggle", planet.vanillaNoiseReference + "/noise_router/vein_toggle");
            noiseRouter.addProperty("vein_ridged", planet.vanillaNoiseReference + "/noise_router/vein_ridged");
            noiseRouter.addProperty("vein_gap", planet.vanillaNoiseReference + "/noise_router/vein_gap");
            noiseSettings.add("noise_router", noiseRouter);
        } else {
            // Legacy custom noise generation (complex and error-prone)
            JsonObject noiseRouter;
            if (planet.useTectonicGeneration) {
                // Use advanced Tectonic noise router
                noiseRouter = createTectonicNoiseRouter(planet);
                // Generate additional Tectonic files
                generateTectonicFiles(planet);
            } else {
                // Use simple noise router
                noiseRouter = createNoiseRouter(planet);
            }
            noiseSettings.add("noise_router", noiseRouter);
        }

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
        // Apply ocean offset (Tectonic-inspired) to continentalness
        if (planet.oceanOffset != -0.8f) {
            JsonObject offsetContinentalness = new JsonObject();
            offsetContinentalness.addProperty("type", "minecraft:add");
            offsetContinentalness.add("argument1", continentalnessNoise);
            offsetContinentalness.addProperty("argument2", planet.oceanOffset);
            mulContinentalness.add("argument1", offsetContinentalness);
        } else {
            mulContinentalness.add("argument1", continentalnessNoise);
        }

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
     * Create base terrain (Tectonic's "sloped_cheese" equivalent)
     * Combines continentalness and erosion noise for core terrain shape
     */
    private static JsonObject createBaseTerrain(PlanetBuilder planet) {
        // Base: add(continentalness + oceanOffset, erosion)
        JsonObject baseTerrain = new JsonObject();
        baseTerrain.addProperty("type", "minecraft:add");

        // Continentalness with ocean offset
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

        if (planet.oceanOffset != -0.8f) {
            JsonObject offsetContinentalness = new JsonObject();
            offsetContinentalness.addProperty("type", "minecraft:add");
            offsetContinentalness.add("argument1", continentalnessNoise);
            offsetContinentalness.addProperty("argument2", planet.oceanOffset);
            mulContinentalness.add("argument1", offsetContinentalness);
        } else {
            mulContinentalness.add("argument1", continentalnessNoise);
        }

        mulContinentalness.addProperty("argument2", getSeedVariation(planet, "height_var3",
            planet.heightVariation3, planet.heightVariation3 * 0.25f));
        baseTerrain.add("argument2", mulContinentalness);

        // Erosion
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
        mulErosion.addProperty("argument2", getSeedVariation(planet, "height_var4",
            planet.heightVariation4, planet.heightVariation4 * 0.25f));

        baseTerrain.add("argument1", mulErosion);

        return baseTerrain;
    }

    /**
     * Create upper slope factor for terrain shaping
     * Applies vertical terrain scaling and shaping factors
     */
    private static float getUpperSlopeFactor(PlanetBuilder planet) {
        float effectiveShaping = planet.terrainShapingFactor * (1.0f - (planet.flatTerrainSkew * 0.5f));
        return effectiveShaping * planet.verticalTerrainScale;
    }

    /**
     * Create underground rivers carving (Tectonic methodology)
     * Returns density function that carves rivers (negative values)
     */
    private static JsonObject createUndergroundRivers(PlanetBuilder planet) {
        if (!planet.undergroundRivers) {
            JsonObject noop = new JsonObject();
            noop.addProperty("type", "minecraft:constant");
            noop.addProperty("argument", 0.0);
            return noop;
        }

        JsonObject riverDensity = new JsonObject();
        riverDensity.addProperty("type", "minecraft:mul");

        JsonObject riverNoise = new JsonObject();
        riverNoise.addProperty("type", "minecraft:shifted_noise");
        riverNoise.addProperty("noise", "minecraft:ridge");
        riverNoise.addProperty("xz_scale", 1.0);
        riverNoise.addProperty("y_scale", 0.3);
        riverNoise.addProperty("shift_x", "minecraft:shift_x");
        riverNoise.addProperty("shift_y", 0);
        riverNoise.addProperty("shift_z", "minecraft:shift_z");
        riverDensity.add("argument1", riverNoise);
        riverDensity.addProperty("argument2", -0.05);

        return riverDensity;
    }

    /**
     * Create lava tunnels carving (Tectonic methodology)
     * Returns density function that carves tunnels (negative values)
     */
    private static JsonObject createLavaTunnels(PlanetBuilder planet) {
        if (!planet.lavaTunnels) {
            JsonObject noop = new JsonObject();
            noop.addProperty("type", "minecraft:constant");
            noop.addProperty("argument", 0.0);
            return noop;
        }

        JsonObject tunnelDensity = new JsonObject();
        tunnelDensity.addProperty("type", "minecraft:mul");

        JsonObject tunnelNoise = new JsonObject();
        tunnelNoise.addProperty("type", "minecraft:shifted_noise");
        tunnelNoise.addProperty("noise", "minecraft:ridge");
        tunnelNoise.addProperty("xz_scale", 0.8);
        tunnelNoise.addProperty("y_scale", 0.1);
        tunnelNoise.addProperty("shift_x", "minecraft:shift_x");
        tunnelNoise.addProperty("shift_y", 0);
        tunnelNoise.addProperty("shift_z", "minecraft:shift_z");
        tunnelDensity.add("argument1", tunnelNoise);
        tunnelDensity.addProperty("argument2", -0.08);

        return tunnelDensity;
    }

    /**
     * Create rolling hills variation (additive, not carving)
     */
    private static JsonObject createRollingHills(PlanetBuilder planet) {
        if (!planet.rollingHills) {
            JsonObject noop = new JsonObject();
            noop.addProperty("type", "minecraft:constant");
            noop.addProperty("argument", 0.0);
            return noop;
        }

        JsonObject hillsDensity = new JsonObject();
        hillsDensity.addProperty("type", "minecraft:mul");

        JsonObject hillsNoise = new JsonObject();
        hillsNoise.addProperty("type", "minecraft:shifted_noise");
        hillsNoise.addProperty("noise", "minecraft:continentalness");
        hillsNoise.addProperty("xz_scale", 8.0);
        hillsNoise.addProperty("y_scale", 0);
        hillsNoise.addProperty("shift_x", "minecraft:shift_x");
        hillsNoise.addProperty("shift_y", 0);
        hillsNoise.addProperty("shift_z", "minecraft:shift_z");
        hillsDensity.add("argument1", hillsNoise);
        hillsDensity.addProperty("argument2", 0.05);

        return hillsDensity;
    }

    /**
     * Create final density using Tectonic's modular composition methodology
     *
     * Structure: add(
     *   min(
     *     squeeze(mul(0.64, interpolated(blend_density(
     *       y_gradient + (-1 + upper_slope * (1 + min(base_terrain, caves)))
     *     )))),
     *     noodle
     *   ),
     *   add(underground_rivers, lava_tunnels, rolling_hills)
     * )
     */
    private static JsonObject createFinalDensity(PlanetBuilder planet) {
        // Outermost: add(terrain_with_caves, carving_features)
        JsonObject outerAdd = new JsonObject();
        outerAdd.addProperty("type", "minecraft:add");

        // Left side: min(squeezed_terrain, noodle_caves)
        JsonObject minWithNoodle = new JsonObject();
        minWithNoodle.addProperty("type", "minecraft:min");

        // Squeezed terrain: squeeze(mul(0.64, interpolated(blend_density(...))))
        JsonObject squeezed = new JsonObject();
        squeezed.addProperty("type", "minecraft:squeeze");

        JsonObject mulBy064 = new JsonObject();
        mulBy064.addProperty("type", "minecraft:mul");
        mulBy064.addProperty("argument1", planet.gradientMultiplier);

        JsonObject interpolated = new JsonObject();
        interpolated.addProperty("type", "minecraft:interpolated");

        JsonObject blendDensity = new JsonObject();
        blendDensity.addProperty("type", "minecraft:blend_density");

        // Core composition: y_gradient + (-1 + upper_slope * (1 + min(base_terrain, caves)))
        JsonObject coreAdd = new JsonObject();
        coreAdd.addProperty("type", "minecraft:add");

        // Y gradient for bottom cutoff
        JsonObject yGradientBottom = new JsonObject();
        yGradientBottom.addProperty("type", "minecraft:mul");

        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_y", planet.gradientFromY);
        yGradient.addProperty("to_y", planet.minY + 24);  // Tectonic uses -40 for bottom cutoff
        yGradient.addProperty("from_value", 0.0);
        yGradient.addProperty("to_value", 1.0);
        yGradientBottom.add("argument1", yGradient);
        yGradientBottom.addProperty("argument2", planet.gradientMultiplier);
        coreAdd.add("argument1", yGradientBottom);

        // Terrain composition: -1 + upper_slope * (1 + min(base_terrain, caves))
        JsonObject terrainComp = new JsonObject();
        terrainComp.addProperty("type", "minecraft:add");
        terrainComp.addProperty("argument1", -1.0 + planet.initialDensityOffset + planet.depthOffset);

        JsonObject upperSlopeMul = new JsonObject();
        upperSlopeMul.addProperty("type", "minecraft:mul");
        upperSlopeMul.addProperty("argument1", getUpperSlopeFactor(planet));

        JsonObject onePlusTerrain = new JsonObject();
        onePlusTerrain.addProperty("type", "minecraft:add");
        onePlusTerrain.addProperty("argument1", 1.0);

        // min(base_terrain, caves) - caves carve into base terrain
        if (planet.caveFrequency > 0 &&
            (planet.enableCheeseCaves || planet.enableSpaghettiCaves || planet.enableNoodleCaves)) {
            JsonObject minTerrainCaves = new JsonObject();
            minTerrainCaves.addProperty("type", "minecraft:min");
            minTerrainCaves.add("argument1", createBaseTerrain(planet));
            minTerrainCaves.add("argument2", createCaveDensity(planet));
            onePlusTerrain.add("argument2", minTerrainCaves);
        } else {
            onePlusTerrain.add("argument2", createBaseTerrain(planet));
        }

        upperSlopeMul.add("argument2", onePlusTerrain);
        terrainComp.add("argument2", upperSlopeMul);
        coreAdd.add("argument2", terrainComp);

        blendDensity.add("argument", coreAdd);
        interpolated.add("argument", blendDensity);
        mulBy064.add("argument2", interpolated);
        squeezed.add("argument", mulBy064);
        minWithNoodle.add("argument1", squeezed);

        // Noodle caves (applied separately, Tectonic style)
        if (planet.enableNoodleCaves) {
            minWithNoodle.add("argument2", createCaveDensity(planet));
        } else {
            JsonObject noNoodle = new JsonObject();
            noNoodle.addProperty("type", "minecraft:constant");
            noNoodle.addProperty("argument", 64.0);  // Large positive = no carving
            minWithNoodle.add("argument2", noNoodle);
        }

        outerAdd.add("argument1", minWithNoodle);

        // Right side: add(underground_rivers, lava_tunnels, rolling_hills)
        JsonObject carvingFeatures = new JsonObject();
        carvingFeatures.addProperty("type", "minecraft:add");

        JsonObject riversAndTunnels = new JsonObject();
        riversAndTunnels.addProperty("type", "minecraft:add");
        riversAndTunnels.add("argument1", createUndergroundRivers(planet));
        riversAndTunnels.add("argument2", createLavaTunnels(planet));

        carvingFeatures.add("argument1", riversAndTunnels);
        carvingFeatures.add("argument2", createRollingHills(planet));

        outerAdd.add("argument2", carvingFeatures);

        return outerAdd;
    }

    /**
     * Create Tectonic noise router using NoiseRouterBuilder for advanced terrain.
     */
    private static JsonObject createTectonicNoiseRouter(PlanetBuilder planet) {
        AdAstraMekanized.LOGGER.info("Generating Tectonic noise router for planet: {}", planet.name);

        // Create Tectonic configuration
        com.hecookin.adastramekanized.worldgen.builder.NoiseRouterBuilder.TectonicConfig config =
            new com.hecookin.adastramekanized.worldgen.builder.NoiseRouterBuilder.TectonicConfig();

        // Apply planet settings to config
        config.continentScale = planet.continentalScale;
        config.erosionScale = planet.erosionScale;
        config.ridgeScale = planet.ridgeScale;
        config.mountainSharpness = planet.mountainSharpness;
        config.enableIslands = planet.enableIslands;
        config.cheeseCaves = planet.enableCheeseCaves;
        config.noodleCaves = planet.enableNoodleCaves;
        config.undergroundRivers = planet.undergroundRivers;
        config.lavaTunnels = planet.lavaTunnels;
        config.desertDunes = planet.enableDesertDunes;
        config.duneHeight = planet.duneHeight;
        config.duneWavelength = planet.duneWavelength;
        config.junglePillars = planet.junglePillars;
        config.pillarHeight = planet.pillarHeight;

        // Build the complete Tectonic noise router
        com.hecookin.adastramekanized.worldgen.builder.NoiseRouterBuilder builder =
            com.hecookin.adastramekanized.worldgen.builder.NoiseRouterBuilder.createTectonicTerrain(
                planet.name,
                planet.minY,
                planet.minY + planet.worldHeight,
                planet.seaLevel,
                config
            );

        return builder.build();
    }

    /**
     * Generate additional Tectonic files (noise definitions and density functions).
     */
    private static void generateTectonicFiles(PlanetBuilder planet) {
        try {
            AdAstraMekanized.LOGGER.info("Generating Tectonic worldgen files for planet: {}", planet.name);

            // Create directories for Tectonic files
            new File(RESOURCES_PATH + "worldgen/noise").mkdirs();
            new File(RESOURCES_PATH + "worldgen/density_function/" + planet.name).mkdirs();

            // Generate noise definitions
            generateTectonicNoiseDefinitions(planet);

            // Generate density functions
            generateTectonicDensityFunctions(planet);

            AdAstraMekanized.LOGGER.info("Successfully generated Tectonic files for: {}", planet.name);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate Tectonic files for planet: {}", planet.name, e);
        }
    }

    /**
     * Helper method to create a JsonArray from double values.
     */
    private static com.google.gson.JsonArray createAmplitudesArray(double... values) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (double value : values) {
            array.add(value);
        }
        return array;
    }

    /**
     * Generate Tectonic noise definitions for a planet.
     */
    private static void generateTectonicNoiseDefinitions(PlanetBuilder planet) throws IOException {
        // Generate continents noise (matching Tectonic's exact settings)
        JsonObject continentsNoise = new JsonObject();
        continentsNoise.addProperty("firstOctave", -10);  // Was -9, now -10 for larger scale
        continentsNoise.add("amplitudes", createAmplitudesArray(1.75, 1.0, 2.0, 3.0, 2.0, 2.0, 1.0, 1.0, 1.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_continents.json", continentsNoise);

        // Generate islands noise (always create for noise router compatibility)
        JsonObject islandsNoise = new JsonObject();
        islandsNoise.addProperty("firstOctave", -9);
        islandsNoise.add("amplitudes", createAmplitudesArray(1.0, 1.0, 0.0, 0.0, 0.0, 0.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_islands.json", islandsNoise);

        // Generate erosion noise (matching Tectonic's exact settings)
        JsonObject erosionNoise = new JsonObject();
        erosionNoise.addProperty("firstOctave", -10);  // Was -9, now -10 for larger scale
        erosionNoise.add("amplitudes", createAmplitudesArray(2.0, 1.75, 1.5, 1.5, 1.3, 1.0, 1.0, 1.0, 1.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_erosion.json", erosionNoise);

        // Generate ridges noise (matching Tectonic's exact settings)
        JsonObject ridgesNoise = new JsonObject();
        ridgesNoise.addProperty("firstOctave", -8);  // Was -7, now -8 for larger scale
        ridgesNoise.add("amplitudes", createAmplitudesArray(1.0, 2.0, 1.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_ridges.json", ridgesNoise);

        // Generate temperature noise
        JsonObject temperatureNoise = new JsonObject();
        temperatureNoise.addProperty("firstOctave", -10);
        temperatureNoise.add("amplitudes", createAmplitudesArray(1.5, 0.0, 1.0, 0.0, 0.0, 0.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_temperature.json", temperatureNoise);

        // Generate vegetation noise
        JsonObject vegetationNoise = new JsonObject();
        vegetationNoise.addProperty("firstOctave", -8);
        vegetationNoise.add("amplitudes", createAmplitudesArray(1.0, 1.0, 0.0, 0.0, 0.0, 0.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_vegetation.json", vegetationNoise);

        // Generate jaggedness noise (always create for noise router compatibility)
        JsonObject jaggednessNoise = new JsonObject();
        jaggednessNoise.addProperty("firstOctave", -15);
        jaggednessNoise.add("amplitudes", createAmplitudesArray(1.0, 1.0, 1.0, 0.0));
        writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_jagged.json", jaggednessNoise);

        // Generate cave noises
        if (planet.enableCheeseCaves) {
            JsonObject cheeseNoise = new JsonObject();
            cheeseNoise.addProperty("firstOctave", -8);
            cheeseNoise.add("amplitudes", createAmplitudesArray(1.0, 0.5, 0.5, 0.5));
            writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_cave_cheese.json", cheeseNoise);
        }

        // Generate underground river noise
        if (planet.undergroundRivers) {
            JsonObject riverNoise = new JsonObject();
            riverNoise.addProperty("firstOctave", -6);
            riverNoise.add("amplitudes", createAmplitudesArray(1.0, 1.0, 1.0));
            writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_underground_river.json", riverNoise);
        }

        // Generate lava tunnel noise
        if (planet.lavaTunnels) {
            JsonObject tunnelNoise = new JsonObject();
            tunnelNoise.addProperty("firstOctave", -7);
            tunnelNoise.add("amplitudes", createAmplitudesArray(1.0, 1.0));
            writeJsonFile(RESOURCES_PATH + "worldgen/noise/" + planet.name + "_lava_tunnel.json", tunnelNoise);
        }

        AdAstraMekanized.LOGGER.info("Generated Tectonic noise definitions for: {}", planet.name);
    }

    /**
     * Generate Tectonic density function files for a planet.
     */
    private static void generateTectonicDensityFunctions(PlanetBuilder planet) throws IOException {
        // Generate raw continents density function
        JsonObject rawContinents = new JsonObject();
        rawContinents.addProperty("type", "minecraft:noise");
        rawContinents.addProperty("noise", "adastramekanized:" + planet.name + "_continents");
        rawContinents.addProperty("xz_scale", planet.continentalScale);
        rawContinents.addProperty("y_scale", 0.0);
        writeJsonFile(RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/noise/raw_continents.json", rawContinents);

        // Generate raw erosion density function
        JsonObject rawErosion = new JsonObject();
        rawErosion.addProperty("type", "minecraft:noise");
        rawErosion.addProperty("noise", "adastramekanized:" + planet.name + "_erosion");
        rawErosion.addProperty("xz_scale", planet.erosionScale);
        rawErosion.addProperty("y_scale", 0.0);
        writeJsonFile(RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/noise/raw_erosion.json", rawErosion);

        // Generate raw ridges density function
        JsonObject rawRidges = new JsonObject();
        rawRidges.addProperty("type", "minecraft:noise");
        rawRidges.addProperty("noise", "adastramekanized:" + planet.name + "_ridges");
        rawRidges.addProperty("xz_scale", planet.ridgeScale);
        rawRidges.addProperty("y_scale", 0.0);
        writeJsonFile(RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/noise/raw_ridges.json", rawRidges);

        // Generate vegetation density function (for spline coordinates)
        JsonObject vegetationDF = new JsonObject();
        vegetationDF.addProperty("type", "minecraft:noise");
        vegetationDF.addProperty("noise", "adastramekanized:" + planet.name + "_vegetation");
        vegetationDF.addProperty("xz_scale", 0.25);
        vegetationDF.addProperty("y_scale", 0.0);
        writeJsonFile(RESOURCES_PATH + "worldgen/density_function/" + planet.name + "_vegetation.json", vegetationDF);

        AdAstraMekanized.LOGGER.info("Generated Tectonic density functions for: {}", planet.name);
    }

    /**
     * Create surface rule using Moon's proven pattern with configurable blocks.
     *
     * GRASS-SPECIFIC HANDLING: When the surface block is grass_block or moss_block,
     * we use stone_depth conditions to ensure:
     * - Surface block (grass) appears ONLY on the top block
     * - Subsurface block (dirt) appears in layers below
     * This prevents grass from stacking vertically in the subsurface.
     *
     * For non-grass blocks (sand, concrete, terracotta, etc.), we use the simpler
     * above_preliminary_surface approach which works fine for uniform surface blocks.
     */
    private static JsonObject createSurfaceRule(PlanetBuilder planet) {
        JsonObject surfaceRule = new JsonObject();
        surfaceRule.addProperty("type", "minecraft:sequence");

        JsonArray sequence = new JsonArray();

        // DEBUG: Log surface rule generation
        AdAstraMekanized.LOGGER.info("Creating surface rule for {} - surface: {}, subsurface: {}",
            planet.name, planet.surfaceBlock, planet.subsurfaceBlock);

        // Determine surface layer behavior: single layer (like grass) vs multi-layer (like sand)
        // Manual override takes precedence, otherwise auto-detect based on surface block type
        boolean useSingleLayerSurface;
        if (planet.singleLayerSurface != null) {
            useSingleLayerSurface = planet.singleLayerSurface;  // Manual override
            AdAstraMekanized.LOGGER.info("Planet {} using {} surface (manual override)",
                planet.name, useSingleLayerSurface ? "single-layer" : "multi-layer");
        } else {
            // Auto-detect: grass-like blocks get single layer, others get multi-layer
            useSingleLayerSurface = planet.surfaceBlock.equals("minecraft:grass_block")
                || planet.surfaceBlock.equals("minecraft:moss_block");
        }

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

        // Create the main surface rules wrapped in above_preliminary_surface condition
        // This is how vanilla does it - all surface block placement happens inside this wrapper
        JsonObject abovePreliminaryWrapper = new JsonObject();
        abovePreliminaryWrapper.addProperty("type", "minecraft:condition");

        JsonObject abovePreliminaryCondition = new JsonObject();
        abovePreliminaryCondition.addProperty("type", "minecraft:above_preliminary_surface");
        abovePreliminaryWrapper.add("if_true", abovePreliminaryCondition);

        // Create inner sequence for surface rules
        JsonObject innerSequence = new JsonObject();
        innerSequence.addProperty("type", "minecraft:sequence");
        JsonArray innerSeq = new JsonArray();

        if (useSingleLayerSurface) {
            // SINGLE-LAYER SURFACE: 1 block of surface on top, subsurface immediately below
            // Used for grass-like blocks or when manually specified via .singleLayerSurface(true)
            // Pattern: ON_FLOOR → surface block, UNDER_FLOOR → subsurface block

            // ON_FLOOR wrapper with grass/dirt sequence inside
            JsonObject onFloorWrapper = new JsonObject();
            onFloorWrapper.addProperty("type", "minecraft:condition");

            JsonObject onFloorCondition = new JsonObject();
            onFloorCondition.addProperty("type", "minecraft:stone_depth");
            onFloorCondition.addProperty("offset", 0);
            onFloorCondition.addProperty("surface_type", "floor");
            onFloorCondition.addProperty("add_surface_depth", false);  // ON_FLOOR
            onFloorCondition.addProperty("secondary_depth_range", 0);
            onFloorWrapper.add("if_true", onFloorCondition);

            // Inner sequence: if water check → grass, else → dirt (fallback)
            JsonObject innerGrassDirtSeq = new JsonObject();
            innerGrassDirtSeq.addProperty("type", "minecraft:sequence");
            JsonArray grassDirtArray = new JsonArray();

            // Grass rule with water condition
            JsonObject grassRule = new JsonObject();
            grassRule.addProperty("type", "minecraft:condition");
            JsonObject waterCondition = new JsonObject();
            waterCondition.addProperty("type", "minecraft:water");
            waterCondition.addProperty("offset", 0);
            waterCondition.addProperty("surface_depth_multiplier", 0);
            waterCondition.addProperty("add_stone_depth", false);
            grassRule.add("if_true", waterCondition);
            JsonObject grassResult = new JsonObject();
            grassResult.addProperty("type", "minecraft:block");
            JsonObject grassState = new JsonObject();
            grassState.addProperty("Name", planet.surfaceBlock);
            grassResult.add("result_state", grassState);
            grassRule.add("then_run", grassResult);
            grassDirtArray.add(grassRule);

            // Dirt fallback (unconditional - fires if water check fails)
            JsonObject dirtFallback = new JsonObject();
            dirtFallback.addProperty("type", "minecraft:block");
            JsonObject dirtState = new JsonObject();
            dirtState.addProperty("Name", planet.subsurfaceBlock);
            dirtFallback.add("result_state", dirtState);
            grassDirtArray.add(dirtFallback);

            innerGrassDirtSeq.add("sequence", grassDirtArray);
            onFloorWrapper.add("then_run", innerGrassDirtSeq);
            innerSeq.add(onFloorWrapper);

            // UNDER_FLOOR wrapper for dirt layers below (3-4 blocks deep)
            if (!planet.subsurfaceBlock.equals(planet.surfaceBlock)) {
                JsonObject underFloorWrapper = new JsonObject();
                underFloorWrapper.addProperty("type", "minecraft:condition");

                JsonObject underFloorCondition = new JsonObject();
                underFloorCondition.addProperty("type", "minecraft:stone_depth");
                underFloorCondition.addProperty("offset", 0);
                underFloorCondition.addProperty("surface_type", "floor");
                underFloorCondition.addProperty("add_surface_depth", true);  // UNDER_FLOOR
                underFloorCondition.addProperty("secondary_depth_range", 3);  // Add 3 extra blocks of depth
                underFloorWrapper.add("if_true", underFloorCondition);

                JsonObject dirtResult = new JsonObject();
                dirtResult.addProperty("type", "minecraft:block");
                JsonObject subsurfaceState = new JsonObject();
                subsurfaceState.addProperty("Name", planet.subsurfaceBlock);
                dirtResult.add("result_state", subsurfaceState);
                underFloorWrapper.add("then_run", dirtResult);

                innerSeq.add(underFloorWrapper);
            }
        } else {
            // MULTI-LAYER SURFACE: 3-8 blocks of surface material before subsurface
            // Used for sand, concrete, terracotta, etc. or when manually specified via .singleLayerSurface(false)
            // Pattern: ON_FLOOR → surface, UNDER_FLOOR(6 depth) → surface, UNDER_FLOOR(12 depth) → subsurface

            // Rule 1: ON_FLOOR - guarantees surface block on TOP block only
            // add_surface_depth=false makes this deterministic (not noise-dependent)
            JsonObject onFloorWrapper = new JsonObject();
            onFloorWrapper.addProperty("type", "minecraft:condition");

            JsonObject onFloorCondition = new JsonObject();
            onFloorCondition.addProperty("type", "minecraft:stone_depth");
            onFloorCondition.addProperty("offset", 0);
            onFloorCondition.addProperty("surface_type", "floor");
            onFloorCondition.addProperty("add_surface_depth", false);  // ON_FLOOR - top block only
            onFloorCondition.addProperty("secondary_depth_range", 0);
            onFloorWrapper.add("if_true", onFloorCondition);

            JsonObject onFloorResult = new JsonObject();
            onFloorResult.addProperty("type", "minecraft:block");
            JsonObject onFloorState = new JsonObject();
            onFloorState.addProperty("Name", planet.surfaceBlock);
            onFloorResult.add("result_state", onFloorState);
            onFloorWrapper.add("then_run", onFloorResult);

            innerSeq.add(onFloorWrapper);

            // Rule 2: UNDER_FLOOR - places surface block in 6-8 layers below top
            // add_surface_depth=true with secondary_depth_range=6 gives variable 3-8 block depth
            JsonObject underFloorWrapper = new JsonObject();
            underFloorWrapper.addProperty("type", "minecraft:condition");

            JsonObject underFloorCondition = new JsonObject();
            underFloorCondition.addProperty("type", "minecraft:stone_depth");
            underFloorCondition.addProperty("offset", 0);
            underFloorCondition.addProperty("surface_type", "floor");
            underFloorCondition.addProperty("add_surface_depth", true);  // UNDER_FLOOR - layers below
            underFloorCondition.addProperty("secondary_depth_range", 6);  // 6 extra blocks of depth
            underFloorWrapper.add("if_true", underFloorCondition);

            JsonObject underFloorResult = new JsonObject();
            underFloorResult.addProperty("type", "minecraft:block");
            JsonObject underFloorState = new JsonObject();
            underFloorState.addProperty("Name", planet.surfaceBlock);
            underFloorResult.add("result_state", underFloorState);
            underFloorWrapper.add("then_run", underFloorResult);

            innerSeq.add(underFloorWrapper);

            // Rule 3: Subsurface layer - even deeper (12+ blocks total)
            if (!planet.subsurfaceBlock.equals(planet.surfaceBlock)) {
                JsonObject subsurfaceLayer = new JsonObject();
                subsurfaceLayer.addProperty("type", "minecraft:condition");

                JsonObject subsurfaceCondition = new JsonObject();
                subsurfaceCondition.addProperty("type", "minecraft:stone_depth");
                subsurfaceCondition.addProperty("offset", 0);
                subsurfaceCondition.addProperty("surface_type", "floor");
                subsurfaceCondition.addProperty("add_surface_depth", true);
                subsurfaceCondition.addProperty("secondary_depth_range", 12);  // Deeper than surface layer
                subsurfaceLayer.add("if_true", subsurfaceCondition);

                JsonObject subsurfaceResult = new JsonObject();
                subsurfaceResult.addProperty("type", "minecraft:block");
                JsonObject subsurfaceState = new JsonObject();
                subsurfaceState.addProperty("Name", planet.subsurfaceBlock);
                subsurfaceResult.add("result_state", subsurfaceState);
                subsurfaceLayer.add("then_run", subsurfaceResult);

                innerSeq.add(subsurfaceLayer);
            }
        }

        // Underwater floor - use water condition for blocks under water (applies to all planets)
        JsonObject underwaterLayer = new JsonObject();
        underwaterLayer.addProperty("type", "minecraft:condition");

        JsonObject underwaterCondition = new JsonObject();
        underwaterCondition.addProperty("type", "minecraft:water");
        underwaterCondition.addProperty("offset", -1);
        underwaterCondition.addProperty("surface_depth_multiplier", 0);
        underwaterCondition.addProperty("add_stone_depth", false);
        underwaterLayer.add("if_true", underwaterCondition);

        JsonObject underwaterResult = new JsonObject();
        underwaterResult.addProperty("type", "minecraft:block");
        JsonObject underwaterState = new JsonObject();
        // Use subsurface block for underwater areas (like dirt under water in vanilla)
        underwaterState.addProperty("Name", planet.subsurfaceBlock);
        underwaterResult.add("result_state", underwaterState);
        underwaterLayer.add("then_run", underwaterResult);

        innerSeq.add(underwaterLayer);

        innerSequence.add("sequence", innerSeq);
        abovePreliminaryWrapper.add("then_run", innerSequence);
        sequence.add(abovePreliminaryWrapper);

        // Deepslate layer replacement - mimics vanilla's stone->deepslate transition below Y=0
        // This replaces vanilla deepslate with the planet's deepslateBlock
        if (!planet.deepslateBlock.equals("minecraft:deepslate")) {
            // Create deepslate replacement rule using vertical gradient (like vanilla)
            // Vanilla deepslate transitions from Y=-8 (100% deepslate) to Y=0 (100% stone)
            JsonObject deepslateLayer = new JsonObject();
            deepslateLayer.addProperty("type", "minecraft:condition");

            JsonObject deepslateCondition = new JsonObject();
            deepslateCondition.addProperty("type", "minecraft:vertical_gradient");
            deepslateCondition.addProperty("random_name", "adastramekanized:deepslate_" + planet.name);

            // Below Y=-8 is 100% deepslate equivalent
            JsonObject deepslateBelow = new JsonObject();
            deepslateBelow.addProperty("absolute", -8);
            deepslateCondition.add("true_at_and_below", deepslateBelow);

            // Above Y=0 is 100% stone equivalent
            JsonObject deepslateAbove = new JsonObject();
            deepslateAbove.addProperty("absolute", 0);
            deepslateCondition.add("false_at_and_above", deepslateAbove);

            deepslateLayer.add("if_true", deepslateCondition);

            JsonObject deepslateResult = new JsonObject();
            deepslateResult.addProperty("type", "minecraft:block");
            JsonObject deepslateState = new JsonObject();
            deepslateState.addProperty("Name", planet.deepslateBlock);
            deepslateResult.add("result_state", deepslateState);
            deepslateLayer.add("then_run", deepslateResult);

            sequence.add(deepslateLayer);
        }

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

            // Ad Astra Mekanized space ores - ROCKET PROGRESSION MATERIALS
            // These are the key ores for building higher-tier rockets
            case "desh" -> "adastramekanized:desh_ore";           // Tier 2 rocket material (Moon)
            case "ostrum" -> "adastramekanized:ostrum_ore";       // Tier 3 rocket material (Mars)
            case "calorite" -> "adastramekanized:calorite_ore";   // Tier 4 rocket material (Venus)
            case "etrium" -> "adastramekanized:etrium_ore";       // Generic etrium ore (for planets without specific variant)
            case "moon_etrium" -> "adastramekanized:moon_etrium_ore";     // Etrium ore (Moon variant)
            case "mars_etrium" -> "adastramekanized:mars_etrium_ore";     // Etrium ore (Mars variant)
            case "glacio_etrium" -> "adastramekanized:glacio_etrium_ore"; // Etrium ore (Glacio variant)
            case "cheese" -> "adastramekanized:moon_cheese_ore";  // Moon cheese ore

            // Mekanism ores - required mod
            case "osmium" -> "mekanism:osmium_ore";
            case "tin" -> "mekanism:tin_ore";
            case "uranium" -> "mekanism:uranium_ore";
            case "fluorite" -> "mekanism:fluorite_ore";
            case "lead" -> "mekanism:lead_ore";

            // Immersive Engineering ores - required mod (uses ore_<metal> naming)
            case "aluminum", "bauxite" -> "immersiveengineering:ore_bauxite";
            case "silver" -> "immersiveengineering:ore_silver";
            case "nickel" -> "immersiveengineering:ore_nickel";

            // Create ores - required mod
            case "zinc" -> "create:zinc_ore";

            // Ores without blocks in required mods - use vanilla substitutes
            // NOTE: platinum and tungsten don't exist in Mekanism/IE/Create
            case "platinum" -> "minecraft:diamond_ore";  // FIXME: No platinum ore exists - replace in planet configs
            case "tungsten" -> "minecraft:iron_ore";     // FIXME: No tungsten ore exists - replace in planet configs

            // Default fallback
            default -> "minecraft:" + oreType + "_ore";
        };
    }

    private static void writeJsonFile(String path, JsonObject json) throws IOException {
        // Create parent directories if they don't exist
        java.nio.file.Path filePath = java.nio.file.Paths.get(path);
        java.nio.file.Files.createDirectories(filePath.getParent());

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
        // Vein size: etrium gets small veins (4) like ancient debris, diamond gets 8, others get 12
        int veinSize = switch (oreType) {
            case "etrium", "moon_etrium", "mars_etrium", "glacio_etrium" -> 4;  // Small veins - rare endgame ore
            case "diamond" -> 8;
            default -> 12;
        };
        config.addProperty("size", veinSize);
        config.addProperty("discard_chance_on_air_exposure", 0.0f);  // Don't discard ores near caves - makes them findable

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
                // Ad Astra Mekanized space ores - ROCKET PROGRESSION
                // Intentionally rare to require exploration before tier upgrade
                case "desh":
                    veinCount = 3;  // Rare - Tier 2 material (Moon)
                    break;
                case "ostrum":
                    veinCount = 3;  // Rare - Tier 3 material (Mars)
                    break;
                case "calorite":
                    veinCount = 2;  // Very rare - Tier 4 material (Venus)
                    break;
                case "etrium":
                case "moon_etrium":
                case "mars_etrium":
                case "glacio_etrium":
                    veinCount = 2;  // Very rare - endgame etrium ore
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
        // Max Y level: etrium and diamond spawn deep (Y=16), others up to Y=48
        boolean isDeepOre = switch (oreType) {
            case "diamond", "etrium", "moon_etrium", "mars_etrium", "glacio_etrium" -> true;
            default -> false;
        };
        maxHeight.addProperty("absolute", isDeepOre ? 16 : 48);
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
            // Generate filename matching the biome ID used in dimension json
            // Uses same logic as createBiomePreset and generateBiomeTag
            String biomeName;
            if (biomeEntry.biomeName.startsWith("adastramekanized:")) {
                // Already has our namespace, extract just the name part
                biomeName = biomeEntry.biomeName.substring("adastramekanized:".length());
            } else if (biomeEntry.biomeName.startsWith("minecraft:")) {
                // Convert vanilla biomes to planet-specific name
                String vanillaBiomeName = biomeEntry.biomeName.substring("minecraft:".length());
                biomeName = planet.name + "_" + vanillaBiomeName;
            } else {
                // Add planet prefix
                biomeName = planet.name + "_" + biomeEntry.biomeName;
            }

            // Create filename without colons (filesystem safe)
            String filename = biomeName + ".json";
            writeJsonFile(RESOURCES_PATH + "worldgen/biome/" + filename, biome);
        }
    }

    /**
     * Generate biome tag file that groups all biomes for this planet.
     * This tag is used by biome_modifiers to target all planet biomes at once.
     * File: tags/worldgen/biome/[planet]_biomes.json
     */
    private static void generateBiomeTag(PlanetBuilder planet) throws IOException {
        JsonObject tagFile = new JsonObject();
        JsonArray values = new JsonArray();

        // Add all custom biomes for this planet
        // Uses same conversion logic as createBiomePreset to ensure consistency
        for (PlanetBuilder.BiomeEntry biomeEntry : planet.customBiomes) {
            String fullBiomeName;
            if (biomeEntry.biomeName.startsWith("adastramekanized:")) {
                // Already has our namespace, use directly
                fullBiomeName = biomeEntry.biomeName;
            } else if (biomeEntry.biomeName.startsWith("minecraft:")) {
                // Convert vanilla biomes to planet-specific to avoid Mekanism ore injection
                String vanillaBiomeName = biomeEntry.biomeName.substring("minecraft:".length());
                fullBiomeName = "adastramekanized:" + planet.name + "_" + vanillaBiomeName;
            } else {
                // Add our namespace and planet prefix
                fullBiomeName = "adastramekanized:" + planet.name + "_" + biomeEntry.biomeName;
            }
            values.add(fullBiomeName);
        }

        tagFile.add("values", values);
        writeJsonFile(RESOURCES_PATH + "tags/worldgen/biome/" + planet.name + "_biomes.json", tagFile);

        AdAstraMekanized.LOGGER.debug("Generated biome tag for planet '{}' with {} biomes", planet.name, values.size());
    }

    /**
     * Generate biome tags for modded structure spawning.
     * Creates tag files in other mods' namespaces to add planet biomes to structure requirements.
     *
     * Structure mods check biome tags to determine where structures can spawn:
     * - Ribbits: #ribbits:has_structure/ribbit_village (needs swamp biomes)
     * - Kobolds: #kobolds:kobold_den_biomes (needs forest/taiga biomes)
     * - WhenDungeonsArise: #dungeons_arise:has_structure/*_biomes (various biome types)
     * - Seven Seas: #dungeons_arise_seven_seas:has_structure/*_biomes (ocean biomes)
     */
    private static void generateStructureBiomeTags(PlanetBuilder planet) throws IOException {
        // Collect all biome names for this planet
        java.util.List<String> planetBiomes = new java.util.ArrayList<>();
        for (PlanetBuilder.BiomeEntry biomeEntry : planet.customBiomes) {
            String fullBiomeName;
            if (biomeEntry.biomeName.startsWith("adastramekanized:")) {
                fullBiomeName = biomeEntry.biomeName;
            } else if (biomeEntry.biomeName.startsWith("minecraft:")) {
                String vanillaBiomeName = biomeEntry.biomeName.substring("minecraft:".length());
                fullBiomeName = "adastramekanized:" + planet.name + "_" + vanillaBiomeName;
            } else {
                fullBiomeName = "adastramekanized:" + planet.name + "_" + biomeEntry.biomeName;
            }
            planetBiomes.add(fullBiomeName);
        }

        if (planetBiomes.isEmpty()) return;

        // Generate Ribbits structure biome tag
        if (planet.enableRibbitsStructures) {
            generateModdedStructureBiomeTag("ribbits", "tags/worldgen/biome/has_structure", "ribbit_village", planetBiomes);
            AdAstraMekanized.LOGGER.info("Generated Ribbits village biome tag for planet '{}'", planet.name);
        }

        // Generate Kobolds structure biome tag
        if (planet.enableKoboldsStructures) {
            generateModdedStructureBiomeTag("kobolds", "tags/worldgen/biome", "kobold_den_biomes", planetBiomes);
            AdAstraMekanized.LOGGER.info("Generated Kobolds den biome tag for planet '{}'", planet.name);
        }

        // Generate WhenDungeonsArise structure biome tags
        if (planet.enableDungeonsAriseStructures) {
            generateDungeonsAriseBiomeTags(planet, planetBiomes);
        }

        // Generate Seven Seas structure biome tags
        if (planet.enableSevenSeasStructures) {
            generateSevenSeasBiomeTags(planet, planetBiomes);
        }
    }

    /**
     * Generate a biome tag file in a mod's namespace to add biomes to structure requirements.
     * Merges with existing file if present (accumulates biomes from multiple planets).
     */
    private static void generateModdedStructureBiomeTag(String modNamespace, String tagPath, String tagName,
            java.util.List<String> biomes) throws IOException {
        // Create directory structure: data/[modNamespace]/[tagPath]/
        String dirPath = RESOURCES_PATH.replace("adastramekanized", modNamespace) + tagPath;
        new File(dirPath).mkdirs();

        String filePath = dirPath + "/" + tagName + ".json";
        java.util.Set<String> allBiomes = new java.util.LinkedHashSet<>();

        // Read existing file if present and extract biomes
        File existingFile = new File(filePath);
        if (existingFile.exists()) {
            try (java.io.FileReader reader = new java.io.FileReader(existingFile)) {
                JsonObject existing = new com.google.gson.Gson().fromJson(reader, JsonObject.class);
                if (existing != null && existing.has("values")) {
                    JsonArray existingValues = existing.getAsJsonArray("values");
                    for (int i = 0; i < existingValues.size(); i++) {
                        allBiomes.add(existingValues.get(i).getAsString());
                    }
                }
            } catch (Exception e) {
                // If reading fails, start fresh
            }
        }

        // Add new biomes
        allBiomes.addAll(biomes);

        // Write merged result
        JsonObject tagFile = new JsonObject();
        tagFile.addProperty("replace", false);  // Append to existing tag, don't replace
        JsonArray values = new JsonArray();
        for (String biome : allBiomes) {
            values.add(biome);
        }
        tagFile.add("values", values);

        writeJsonFile(filePath, tagFile);
    }

    /**
     * Write a biome tag file, merging with existing content if present.
     * Used to accumulate biomes from multiple planets in the same tag file.
     */
    private static void writeMergedBiomeTagFile(String filePath, java.util.List<String> newBiomes) throws IOException {
        java.util.Set<String> allBiomes = new java.util.LinkedHashSet<>();

        // Read existing file if present and extract biomes
        File existingFile = new File(filePath);
        if (existingFile.exists()) {
            try (java.io.FileReader reader = new java.io.FileReader(existingFile)) {
                JsonObject existing = new com.google.gson.Gson().fromJson(reader, JsonObject.class);
                if (existing != null && existing.has("values")) {
                    JsonArray existingValues = existing.getAsJsonArray("values");
                    for (int i = 0; i < existingValues.size(); i++) {
                        allBiomes.add(existingValues.get(i).getAsString());
                    }
                }
            } catch (Exception e) {
                // If reading fails, start fresh
            }
        }

        // Add new biomes
        allBiomes.addAll(newBiomes);

        // Write merged result
        JsonObject tagFile = new JsonObject();
        tagFile.addProperty("replace", false);
        JsonArray values = new JsonArray();
        for (String biome : allBiomes) {
            values.add(biome);
        }
        tagFile.add("values", values);

        writeJsonFile(filePath, tagFile);
    }

    /**
     * Generate WhenDungeonsArise structure biome tags based on enabled structure types.
     * Merges with existing files to accumulate biomes from multiple planets.
     */
    private static void generateDungeonsAriseBiomeTags(PlanetBuilder planet, java.util.List<String> planetBiomes) throws IOException {
        String basePath = RESOURCES_PATH.replace("adastramekanized", "dungeons_arise") + "tags/worldgen/biome/has_structure";
        new File(basePath).mkdirs();

        // Map structure types to their biome tag files
        java.util.Map<String, String[]> structuresByType = new java.util.HashMap<>();
        structuresByType.put("plains", new String[]{"illager_campsite_biomes", "merchant_campsite_biomes", "illager_windmill_biomes", "small_blimp_biomes"});
        structuresByType.put("forest", new String[]{"bandit_village_biomes", "greenwood_pub_biomes", "aviary_biomes", "bandit_towers_biomes"});
        structuresByType.put("desert", new String[]{"scorched_mines_biomes", "monastery_biomes", "shiraz_palace_biomes"});
        structuresByType.put("jungle", new String[]{"jungle_tree_house_biomes", "infested_temple_biomes"});
        structuresByType.put("ocean", new String[]{"illager_galley_biomes", "illager_corsair_biomes", "lighthouse_biomes", "fishing_hut_biomes"});
        structuresByType.put("swamp", new String[]{"mushroom_village_biomes", "mushroom_house_biomes", "mushroom_mines_biomes"});
        structuresByType.put("mountain", new String[]{"keep_kayra_biomes", "foundry_biomes", "mechanical_nest_biomes", "heavenly_rider_biomes", "heavenly_challenger_biomes", "heavenly_conqueror_biomes"});
        structuresByType.put("snowy", new String[]{"plague_asylum_biomes"});
        structuresByType.put("badlands", new String[]{"coliseum_biomes", "ceryneian_hind_biomes"});
        structuresByType.put("underground", new String[]{"mining_system_biomes"});

        // Create tag file for enabled structure types
        for (String structureType : planet.dungeonsAriseStructureTypes) {
            String[] structureTags = structuresByType.get(structureType);
            if (structureTags != null) {
                for (String tagName : structureTags) {
                    String filePath = basePath + "/" + tagName + ".json";
                    writeMergedBiomeTagFile(filePath, planetBiomes);
                }
                AdAstraMekanized.LOGGER.info("Generated WhenDungeonsArise '{}' structure tags for planet '{}'", structureType, planet.name);
            }
        }
    }

    /**
     * Generate WhenDungeonsArise Seven Seas structure biome tags for ocean structures.
     * Merges with existing files to accumulate biomes from multiple planets.
     */
    private static void generateSevenSeasBiomeTags(PlanetBuilder planet, java.util.List<String> planetBiomes) throws IOException {
        String basePath = RESOURCES_PATH.replace("adastramekanized", "dungeons_arise_seven_seas") + "tags/worldgen/biome/has_structure";
        new File(basePath).mkdirs();

        // All Seven Seas structures need ocean biomes
        String[] sevenSeasTags = {"corsair_corvette_biomes", "pirate_junk_biomes", "small_yacht_biomes", "unicorn_galleon_biomes", "victory_frigate_biomes"};

        for (String tagName : sevenSeasTags) {
            String filePath = basePath + "/" + tagName + ".json";
            writeMergedBiomeTagFile(filePath, planetBiomes);
        }
        AdAstraMekanized.LOGGER.info("Generated Seven Seas structure biome tags for planet '{}'", planet.name);
    }

    /**
     * Generate NeoForge biome modifier files for mob spawning control.
     * Creates separate files for vanilla and modded mobs:
     * - add_spawns.json: Vanilla (minecraft:) mobs, always active
     * - add_spawns_[modid].json: Modded mobs with neoforge:mod_loaded condition
     *
     * File location: neoforge/biome_modifier/[planet]/
     */
    private static void generateBiomeModifier(PlanetBuilder planet) throws IOException {
        // Create planet-specific directory
        new File(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name).mkdirs();

        // Collect vanilla mob spawns
        JsonArray vanillaSpawners = new JsonArray();

        // Group modded mobs by their mod ID
        java.util.Map<String, JsonArray> moddedSpawnersByMod = new java.util.HashMap<>();

        for (java.util.Map.Entry<String, java.util.List<PlanetBuilder.MobSpawnEntry>> entry : planet.mobSpawns.entrySet()) {
            for (PlanetBuilder.MobSpawnEntry mob : entry.getValue()) {
                String modid = mob.mobId.contains(":") ? mob.mobId.split(":")[0] : "minecraft";

                JsonObject spawner = new JsonObject();
                spawner.addProperty("type", mob.mobId);
                spawner.addProperty("weight", mob.weight);
                spawner.addProperty("minCount", mob.minCount);
                spawner.addProperty("maxCount", mob.maxCount);

                if (modid.equals("minecraft")) {
                    vanillaSpawners.add(spawner);
                } else {
                    // Group by mod ID for conditional loading
                    moddedSpawnersByMod.computeIfAbsent(modid, k -> new JsonArray()).add(spawner);
                }
            }
        }

        // Generate vanilla spawns file (no conditions needed)
        if (vanillaSpawners.size() > 0) {
            JsonObject biomeModifier = new JsonObject();
            biomeModifier.addProperty("type", "neoforge:add_spawns");
            biomeModifier.addProperty("biomes", "#adastramekanized:" + planet.name + "_biomes");
            biomeModifier.add("spawners", vanillaSpawners);

            writeJsonFile(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name + "/add_spawns.json", biomeModifier);

            AdAstraMekanized.LOGGER.debug("Generated biome modifier for planet '{}' with {} vanilla spawn entries",
                planet.name, vanillaSpawners.size());
        }

        // Generate conditional modded spawns files (one per mod)
        for (java.util.Map.Entry<String, JsonArray> moddedEntry : moddedSpawnersByMod.entrySet()) {
            String modid = moddedEntry.getKey();
            JsonArray moddedSpawners = moddedEntry.getValue();

            if (moddedSpawners.size() > 0) {
                JsonObject biomeModifier = new JsonObject();

                // Add NeoForge condition for mod to be loaded
                JsonArray conditions = new JsonArray();
                JsonObject modLoadedCondition = new JsonObject();
                modLoadedCondition.addProperty("type", "neoforge:mod_loaded");
                modLoadedCondition.addProperty("modid", modid);
                conditions.add(modLoadedCondition);
                biomeModifier.add("neoforge:conditions", conditions);

                biomeModifier.addProperty("type", "neoforge:add_spawns");
                biomeModifier.addProperty("biomes", "#adastramekanized:" + planet.name + "_biomes");
                biomeModifier.add("spawners", moddedSpawners);

                writeJsonFile(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name + "/add_spawns_" + modid + ".json", biomeModifier);

                AdAstraMekanized.LOGGER.debug("Generated conditional biome modifier for planet '{}' with {} {} spawn entries",
                    planet.name, moddedSpawners.size(), modid);
            }
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

        // Mob spawners - only add VANILLA mobs directly to biome JSON
        // Modded mobs are handled by biome_modifier with NeoForge conditions (see generateBiomeModifier)
        JsonObject spawners = new JsonObject();
        for (String category : new String[]{"monster", "creature", "ambient", "water_creature", "water_ambient", "misc"}) {
            JsonArray categorySpawns = new JsonArray();

            // Add configured spawns for this category, but ONLY vanilla (minecraft) entities
            // Modded entities will be added via biome_modifier which supports neoforge:conditions
            if (planet.mobSpawns.containsKey(category)) {
                for (PlanetBuilder.MobSpawnEntry mob : planet.mobSpawns.get(category)) {
                    // Only add vanilla minecraft entities to biome JSON
                    // Modded entities are handled by generateBiomeModifier() with NeoForge conditions
                    String modid = mob.mobId.contains(":") ? mob.mobId.split(":")[0] : "minecraft";
                    if (modid.equals("minecraft")) {
                        JsonObject spawn = new JsonObject();
                        spawn.addProperty("type", mob.mobId);
                        spawn.addProperty("minCount", mob.minCount);
                        spawn.addProperty("maxCount", mob.maxCount);
                        spawn.addProperty("weight", mob.weight);
                        categorySpawns.add(spawn);
                    }
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

        // Generation steps in order (11 steps total, indices 0-10)
        // 0=raw_generation, 1=lakes, 2=local_modifications, 3=underground_structures,
        // 4=surface_structures, 5=strongholds, 6=underground_ores, 7=underground_decoration,
        // 8=fluid_springs, 9=vegetal_decoration, 10=top_layer_modification
        String[] steps = {
            "raw_generation", "lakes", "local_modifications", "underground_structures",
            "surface_structures", "strongholds", "underground_ores", "underground_decoration",
            "fluid_springs", "vegetal_decoration", "top_layer_modification"
        };

        for (int i = 0; i < steps.length; i++) {
            String step = steps[i];
            JsonArray stepFeatures = new JsonArray();

            // Check if custom ores are configured - if so, use custom ore features instead of vanilla
            boolean useCustomOres = planet.oreVeinsEnabled && !planet.oreVeinCounts.isEmpty();

            if (planet.useVanillaUndergroundFeatures) {
                // Add vanilla underground features based on generation step
                switch (i) {
                    case 1: // lakes
                        stepFeatures.add("minecraft:lake_lava_underground");
                        stepFeatures.add("minecraft:lake_lava_surface");
                        break;
                    case 2: // local_modifications
                        stepFeatures.add("minecraft:amethyst_geode");
                        break;
                    case 3: // underground_structures
                        stepFeatures.add("minecraft:monster_room");
                        stepFeatures.add("minecraft:monster_room_deep");
                        break;
                    case 6: // underground_ores
                        if (useCustomOres) {
                            // Use custom planet-specific ore features when configured
                            for (String oreType : planet.oreVeinCounts.keySet()) {
                                stepFeatures.add("adastramekanized:" + planet.name + "_ore_" + oreType + "_simple");
                            }
                        } else {
                            // Fall back to vanilla ore set
                            stepFeatures.add("minecraft:ore_dirt");
                            stepFeatures.add("minecraft:ore_gravel");
                            stepFeatures.add("minecraft:ore_granite_upper");
                            stepFeatures.add("minecraft:ore_granite_lower");
                            stepFeatures.add("minecraft:ore_diorite_upper");
                            stepFeatures.add("minecraft:ore_diorite_lower");
                            stepFeatures.add("minecraft:ore_andesite_upper");
                            stepFeatures.add("minecraft:ore_andesite_lower");
                            stepFeatures.add("minecraft:ore_tuff");
                            stepFeatures.add("minecraft:ore_coal_upper");
                            stepFeatures.add("minecraft:ore_coal_lower");
                            stepFeatures.add("minecraft:ore_iron_upper");
                            stepFeatures.add("minecraft:ore_iron_middle");
                            stepFeatures.add("minecraft:ore_iron_small");
                            stepFeatures.add("minecraft:ore_gold");
                            stepFeatures.add("minecraft:ore_gold_lower");
                            stepFeatures.add("minecraft:ore_redstone");
                            stepFeatures.add("minecraft:ore_redstone_lower");
                            stepFeatures.add("minecraft:ore_diamond");
                            stepFeatures.add("minecraft:ore_diamond_medium");
                            stepFeatures.add("minecraft:ore_diamond_large");
                            stepFeatures.add("minecraft:ore_diamond_buried");
                            stepFeatures.add("minecraft:ore_lapis");
                            stepFeatures.add("minecraft:ore_lapis_buried");
                            stepFeatures.add("minecraft:ore_copper");
                            stepFeatures.add("minecraft:ore_copper_large");
                            stepFeatures.add("minecraft:ore_emerald");
                        }
                        break;
                    case 7: // underground_decoration
                        stepFeatures.add("minecraft:ore_infested");
                        break;
                    case 8: // fluid_springs
                        stepFeatures.add("minecraft:spring_water");
                        stepFeatures.add("minecraft:spring_lava");
                        break;
                    case 9: // vegetal_decoration
                        // Add vegetation based on biome type
                        String biomeLower = biomeEntry.biomeName.toLowerCase();

                        // Swamp biomes - lily pads, seagrass, vines, swamp trees
                        if (biomeLower.contains("swamp") || biomeLower.contains("mangrove")) {
                            stepFeatures.add("minecraft:trees_swamp");
                            stepFeatures.add("minecraft:flower_swamp");
                            stepFeatures.add("minecraft:patch_grass_normal");
                            stepFeatures.add("minecraft:patch_waterlily");
                            stepFeatures.add("minecraft:seagrass_swamp");
                            stepFeatures.add("minecraft:vines");
                            stepFeatures.add("minecraft:brown_mushroom_swamp");
                            stepFeatures.add("minecraft:red_mushroom_swamp");
                            if (biomeLower.contains("mangrove")) {
                                stepFeatures.add("minecraft:trees_mangrove");
                            }
                        }
                        // Jungle biomes - jungle trees, bamboo, vines, melons
                        else if (biomeLower.contains("jungle")) {
                            stepFeatures.add("minecraft:trees_jungle");
                            stepFeatures.add("minecraft:bamboo_vegetation");
                            stepFeatures.add("minecraft:vines");
                            stepFeatures.add("minecraft:patch_grass_jungle");
                            stepFeatures.add("minecraft:patch_melon");
                            stepFeatures.add("minecraft:flower_warm");
                            if (biomeLower.contains("bamboo")) {
                                stepFeatures.add("minecraft:bamboo");
                            }
                        }
                        // Forest biomes - various trees, flowers, grass
                        else if (biomeLower.contains("forest")) {
                            stepFeatures.add("minecraft:trees_birch_and_oak");
                            stepFeatures.add("minecraft:trees_birch");
                            stepFeatures.add("minecraft:flower_forest_flowers");
                            stepFeatures.add("minecraft:patch_grass_forest");
                            if (biomeLower.contains("dark")) {
                                stepFeatures.add("minecraft:dark_forest_vegetation");
                                stepFeatures.add("minecraft:brown_mushroom_normal");
                                stepFeatures.add("minecraft:red_mushroom_normal");
                            }
                        }
                        // Plains biomes - grass, flowers, occasional trees
                        else if (biomeLower.contains("plain") || biomeLower.contains("meadow")) {
                            stepFeatures.add("minecraft:trees_plains");
                            stepFeatures.add("minecraft:flower_plains");
                            stepFeatures.add("minecraft:patch_grass_plain");
                            if (biomeLower.contains("sunflower")) {
                                stepFeatures.add("minecraft:patch_sunflower");
                            }
                        }
                        // Savanna biomes - acacia trees, grass
                        else if (biomeLower.contains("savanna")) {
                            stepFeatures.add("minecraft:trees_savanna");
                            stepFeatures.add("minecraft:patch_grass_savanna");
                            stepFeatures.add("minecraft:patch_tall_grass");
                        }
                        // Desert biomes - cacti, dead bushes
                        else if (biomeLower.contains("desert")) {
                            stepFeatures.add("minecraft:patch_cactus");
                            stepFeatures.add("minecraft:patch_dead_bush");
                        }
                        // Taiga biomes - spruce trees, ferns
                        else if (biomeLower.contains("taiga")) {
                            stepFeatures.add("minecraft:trees_taiga");
                            stepFeatures.add("minecraft:patch_large_fern");
                            stepFeatures.add("minecraft:patch_grass_taiga");
                            stepFeatures.add("minecraft:brown_mushroom_taiga");
                            stepFeatures.add("minecraft:red_mushroom_taiga");
                        }
                        // Ocean biomes - seagrass, kelp
                        else if (biomeLower.contains("ocean") || biomeLower.contains("beach")) {
                            stepFeatures.add("minecraft:seagrass_simple");
                            stepFeatures.add("minecraft:kelp_warm");
                        }
                        // Lush caves biome
                        else if (biomeLower.contains("lush")) {
                            stepFeatures.add("minecraft:lush_caves_vegetation");
                            stepFeatures.add("minecraft:lush_caves_ceiling_vegetation");
                            stepFeatures.add("minecraft:spore_blossom");
                            stepFeatures.add("minecraft:glow_lichen");
                        }
                        // Default - minimal vegetation
                        else {
                            stepFeatures.add("minecraft:glow_lichen");
                        }

                        // Add any custom features from the planet's customFeatures list
                        for (PlanetBuilder.FeatureEntry feature : planet.customFeatures) {
                            if (feature.placementStep.equals("vegetal_decoration")) {
                                stepFeatures.add(feature.featureName);
                            }
                        }
                        break;
                }
            } else if (step.equals("underground_ores") && planet.oreVeinsEnabled) {
                // Add custom ore features for all configured ore types (legacy behavior)
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

        // Carvers - add vanilla carvers for full cave generation when enabled
        JsonObject carvers = new JsonObject();
        JsonArray airCarvers = new JsonArray();
        if (planet.useVanillaCaves) {
            // Vanilla carvers for full cave system
            airCarvers.add("minecraft:cave");
            airCarvers.add("minecraft:cave_extra_underground");
            airCarvers.add("minecraft:canyon");
        }
        carvers.add("air", airCarvers);
        biome.add("carvers", carvers);

        return biome;
    }
}