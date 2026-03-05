package com.hecookin.adastramekanized.integration;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Integration test class to verify that all required mod APIs are available
 * and accessible during development.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class ModIntegrationTest {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        AdAstraMekanized.LOGGER.info("Testing mod integrations...");

        // Test Mekanism integration
        testMekanismIntegration();

        // Test Immersive Engineering integration
        testImmersiveEngineeringIntegration();

        AdAstraMekanized.LOGGER.info("Mod integration tests completed!");
    }

    private static void testMekanismIntegration() {
        boolean mekanismLoaded = ModList.get().isLoaded("mekanism");
        AdAstraMekanized.LOGGER.info("Mekanism loaded: {}", mekanismLoaded);

        if (mekanismLoaded) {
            try {
                // Test access to Mekanism API classes
                Class.forName("mekanism.api.chemical.Chemical");
                Class.forName("mekanism.api.energy.IEnergyContainer");
                AdAstraMekanized.LOGGER.info("✓ Mekanism API classes accessible");
            } catch (ClassNotFoundException e) {
                AdAstraMekanized.LOGGER.error("✗ Mekanism API classes not accessible: {}", e.getMessage());
            }
        }
    }

    private static void testImmersiveEngineeringIntegration() {
        boolean ieLoaded = ModList.get().isLoaded("immersiveengineering");
        AdAstraMekanized.LOGGER.info("Immersive Engineering loaded: {}", ieLoaded);

        if (ieLoaded) {
            try {
                // Test access to IE API classes
                Class.forName("blusunrize.immersiveengineering.ImmersiveEngineering");
                AdAstraMekanized.LOGGER.info("✓ Immersive Engineering main class accessible");
            } catch (ClassNotFoundException e) {
                AdAstraMekanized.LOGGER.warn("IE API access limited: {}", e.getMessage());
            }
        }
    }
}