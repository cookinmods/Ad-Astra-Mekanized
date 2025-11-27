package com.hecookin.adastramekanized.worldgen.integration;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.densityfunction.ConfigConstant;
import com.hecookin.adastramekanized.worldgen.densityfunction.ConfigNoise;
import com.hecookin.adastramekanized.worldgen.densityfunction.PlanetConfigHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.Optional;

/**
 * NeoForge integration for Tectonic worldgen system.
 * Registers custom density function types and resource packs.
 */
public class TectonicNeoForgeIntegration {

    /**
     * Initialize the Tectonic worldgen integration.
     * Called from main mod class.
     */
    public static void initialize(IEventBus modEventBus) {
        // Register density function types
        modEventBus.addListener(TectonicNeoForgeIntegration::registerDensityFunctionTypes);

        // NOTE: Custom pack finder disabled - worldgen files are already in src/main/resources/
        // modEventBus.addListener(TectonicNeoForgeIntegration::registerTectonicDataPack);

        // Initialize planet configurations
        PlanetConfigHandler.getInstance().initializePlanets();

        AdAstraMekanized.LOGGER.info("Tectonic worldgen integration initialized");
    }

    /**
     * Register custom density function types for runtime configuration.
     */
    private static void registerDensityFunctionTypes(final RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> {
            // Register ConfigConstant for runtime constant values
            helper.register(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "config_constant"),
                ConfigConstant.CODEC_HOLDER.codec()
            );

            // Register ConfigNoise for runtime-configurable noise
            helper.register(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "config_noise"),
                ConfigNoise.CODEC_HOLDER.codec()
            );

            AdAstraMekanized.LOGGER.info("Registered Tectonic density function types");
        });
    }

    /**
     * Register the Tectonic data pack containing worldgen files.
     * This allows dynamic generation and loading of worldgen data.
     */
    private static void registerTectonicDataPack(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            // Check if Tectonic features are enabled (always true for now)
            if (isTectonicEnabled()) {
                Path resourcePath = ModList.get()
                    .getModFileById(AdAstraMekanized.MOD_ID)
                    .getFile()
                    .findResource("resourcepacks/tectonic");

                // Only add pack if the directory exists
                if (resourcePath != null && resourcePath.toFile().exists()) {
                    Pack tectonicPack = Pack.readMetaAndCreate(
                        new PackLocationInfo(
                            "adastramekanized_tectonic",
                            Component.literal("AdAstra Mekanized Tectonic Worldgen"),
                            PackSource.BUILT_IN,
                            Optional.empty()
                        ),
                        new PathPackResources.PathResourcesSupplier(resourcePath),
                        PackType.SERVER_DATA,
                        new PackSelectionConfig(
                            true,    // Required
                            Pack.Position.TOP,  // Load at top priority
                            false    // Not fixed
                        )
                    );

                    event.addRepositorySource((packConsumer) -> packConsumer.accept(tectonicPack));

                    AdAstraMekanized.LOGGER.info("Registered Tectonic worldgen data pack");
                } else {
                    AdAstraMekanized.LOGGER.warn("Tectonic resource pack directory not found, " +
                        "Tectonic features will use generated files only");
                }
            }
        }
    }

    /**
     * Check if Tectonic worldgen features should be enabled.
     * Can be tied to config later.
     */
    private static boolean isTectonicEnabled() {
        // For now, always enable Tectonic features
        // Later this can check a config value
        return true;
    }

    /**
     * Resource condition types for conditional worldgen loading.
     * Allows features to be enabled/disabled per-planet.
     */
    public static class TectonicResourceConditions {
        // Resource conditions for planet-specific features
        public static final String UNDERGROUND_RIVERS = "underground_rivers";
        public static final String JUNGLE_PILLARS = "jungle_pillars";
        public static final String DESERT_DUNES = "desert_dunes";
        public static final String LAVA_TUNNELS = "lava_tunnels";
        public static final String INCREASED_HEIGHT = "increased_height";
        public static final String ULTRASMOOTH = "ultrasmooth";

        /**
         * Check if a feature is enabled for a planet.
         */
        public static boolean isFeatureEnabled(String planetId, String feature) {
            PlanetConfigHandler handler = PlanetConfigHandler.getInstance();
            double value = handler.getValue(planetId, feature);
            return value > 0.0;
        }
    }
}