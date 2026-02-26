package com.hecookin.adastramekanized;

import com.hecookin.adastramekanized.common.capabilities.MekanismCapabilityProvider;
import com.hecookin.adastramekanized.common.commands.ModCommands;
import com.hecookin.adastramekanized.common.teleportation.PlanetTeleportationSystem;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.hecookin.adastramekanized.common.planets.PlanetNetworking;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModChunkGenerators;
import com.hecookin.adastramekanized.common.registry.ModCreativeTabs;
import com.hecookin.adastramekanized.common.registry.ModEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModItems;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import com.hecookin.adastramekanized.common.registry.ModRecipeTypes;
import com.hecookin.adastramekanized.common.registry.ModRecipeSerializers;
import com.hecookin.adastramekanized.config.AdAstraMekanizedConfig;
import com.hecookin.adastramekanized.common.events.ModdedMobSpawnController;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import com.hecookin.adastramekanized.worldgen.biome.MoonBiomes;
import com.hecookin.adastramekanized.worldgen.biome.PlanetBiomeRegistry;
import com.hecookin.adastramekanized.worldgen.integration.TectonicNeoForgeIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ad Astra Mekanized - Space Exploration with Mekanism Integration
 *
 * This mod combines the best features of Ad Astra (space exploration, planets, rockets)
 * with Mekanism's infrastructure (energy, chemicals, pipes, machines).
 *
 * Key Features:
 * - Rockets and space travel using Mekanism energy
 * - Oxygen system integrated with Mekanism chemical pipes
 * - Fuel system integrated with Immersive Engineering
 * - Planet generation and exploration
 * - Space stations and orbital mechanics
 */
@Mod(AdAstraMekanized.MOD_ID)
public class AdAstraMekanized {
    public static final String MOD_ID = "adastramekanized";
    public static final Logger LOGGER = LogManager.getLogger(AdAstraMekanized.class);

    private static ModIntegrationManager integrationManager;

    public AdAstraMekanized(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Ad Astra Mekanized - Space exploration with Mekanism integration!");

        // Register configuration
        AdAstraMekanizedConfig.registerConfigs(modContainer);
        modEventBus.addListener(AdAstraMekanizedConfig::onConfigLoad);

        // Register items, blocks, and creative tabs
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register chunk generators
        ModChunkGenerators.register(modEventBus);

        // Register planet biome system
        PlanetBiomeRegistry.register(modEventBus);

        // Initialize Tectonic worldgen integration
        TectonicNeoForgeIntegration.initialize(modEventBus);

        // Register mod setup
        modEventBus.addListener(this::commonSetup);

        // Register networking
        modEventBus.addListener(this::registerNetworking);

        // Register capabilities for Mekanism integration
        modEventBus.addListener(MekanismCapabilityProvider::registerCapabilities);

        // Register planet manager events
        NeoForge.EVENT_BUS.register(PlanetManager.class);

        // Register command handlers
        NeoForge.EVENT_BUS.register(ModCommands.class);

        // Register server events for planet services
        NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);

        // PlanetMaker system - run './gradlew makePlanets' to generate planets

        LOGGER.info("Ad Astra Mekanized initialization complete!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ad Astra Mekanized common setup started");

        event.enqueueWork(() -> {
            // Initialize mod integrations
            integrationManager = ModIntegrationManager.getInstance();
            LOGGER.info("Integration manager initialized: {}", integrationManager.getIntegrationStatus());

            // Configure modded mob spawn control
            // Controlled mods: born_in_chaos_v1, kobolds, ribbits
            // By default, controlled mobs ONLY spawn in whitelisted dimensions/planets
            // Enable vanilla dimension spawning for mods that should spawn on Earth:
            ModdedMobSpawnController.allowInVanillaDimensions("born_in_chaos_v1"); // Spawn on Earth, Nether, End - blocked elsewhere
            ModdedMobSpawnController.allowInVanillaDimensions("kobolds");         // Spawn on Earth, Nether, End - blocked elsewhere
            ModdedMobSpawnController.allowInVanillaDimensions("ribbits");         // Spawn on Earth, Nether, End - blocked elsewhere

            // To add more mob mods to spawn control:
            // ModdedMobSpawnController.addControlledMod("alexsmobs");
            // ModdedMobSpawnController.allowInOverworld("alexsmobs");

            // Initialize rocket tier properties
            ModEntityTypes.initRocketTiers();
            LOGGER.info("Rocket tiers initialized");

            // Planets are now generated using PlanetMaker system
            LOGGER.info("Using PlanetMaker for planet generation - run './gradlew makePlanets' to regenerate");

            // Initialize planet spawn whitelists by loading PlanetGenerationRunner
            // This triggers the static block which registers all modded mob whitelists
            int planetCount = com.hecookin.adastramekanized.common.planets.PlanetGenerationRunner.getAllPlanetBuilders().size();
            LOGGER.info("Initialized spawn whitelists for {} planets", planetCount);

            // Initialize planet system components
            LOGGER.info("Planet system initialization complete");

            // Register planet biomes
            MoonBiomes.register();
            LOGGER.info("Planet-specific biomes registered");


            LOGGER.info("Ad Astra Mekanized common setup complete");
        });
    }

    /**
     * Register networking handlers
     */
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        LOGGER.info("Registering planet networking handlers...");
        PlanetNetworking.register(event);
        LOGGER.info("Planet networking registration complete");
    }

    /**
     * Server startup logging - planet generation now handled by PlanetMaker
     */
    private void onServerAboutToStart(final ServerAboutToStartEvent event) {
        LOGGER.info("PlanetMaker system active - planets loaded from generated datapacks");
        LOGGER.info("To regenerate planets, run: ./gradlew makePlanets");
    }

    /**
     * Initialize planet services when server starts
     */
    private void onServerStarted(final ServerStartedEvent event) {
        LOGGER.info("Initializing planet services...");

        // Initialize planet manager
        PlanetManager planetManager = PlanetManager.getInstance();
        planetManager.initialize(event.getServer());

        // Load planet data from JSON files
        planetManager.loadPlanetData();

        // Initialize teleportation system
        PlanetTeleportationSystem teleportSystem = PlanetTeleportationSystem.getInstance();
        teleportSystem.initialize(event.getServer());

        LOGGER.info("Planet services initialization complete");
    }

    /**
     * Clean up planet services when server stops
     */
    private void onServerStopped(final ServerStoppedEvent event) {
        LOGGER.info("Shutting down planet services...");

        // Clear teleportation cache
        PlanetTeleportationSystem teleportSystem = PlanetTeleportationSystem.getInstance();
        teleportSystem.clearCache();

        LOGGER.info("Planet services shutdown complete");
    }

    /**
     * Get the integration manager instance
     * @return ModIntegrationManager instance, or null if not yet initialized
     */
    public static ModIntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}