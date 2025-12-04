package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers block capabilities for the gravity normalizer
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class GravityNormalizerCapabilityProvider {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        AdAstraMekanized.LOGGER.info("Registering gravity normalizer block capabilities");

        // Register Forge energy capability for the gravity normalizer block
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof GravityNormalizerBlockEntity normalizer) {
                    return normalizer.getEnergyStorage();
                }
                return null;
            },
            ModBlocks.GRAVITY_NORMALIZER.get()
        );

        // Register Mekanism capabilities if available
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");

            // Register CHEMICAL capability for argon gas input
            java.lang.reflect.Field chemicalField = mekCapabilities.getField("CHEMICAL");
            Object chemicalCapObject = chemicalField.get(null);
            java.lang.reflect.Method blockMethod = chemicalCapObject.getClass().getMethod("block");
            Object chemicalBlockCap = blockMethod.invoke(chemicalCapObject);

            if (chemicalBlockCap instanceof net.neoforged.neoforge.capabilities.BlockCapability) {
                @SuppressWarnings("unchecked")
                var blockCap = (net.neoforged.neoforge.capabilities.BlockCapability<Object, Direction>) chemicalBlockCap;

                event.registerBlock(
                    blockCap,
                    (level, pos, state, be, side) -> {
                        if (be instanceof GravityNormalizerBlockEntity normalizer) {
                            return normalizer.getChemicalHandler();
                        }
                        return null;
                    },
                    ModBlocks.GRAVITY_NORMALIZER.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism chemical capability for gravity normalizer");
            }

            // Register ENERGY capability for Mekanism energy cables
            java.lang.reflect.Field energyField = mekCapabilities.getField("ENERGY");
            Object energyCapObject = energyField.get(null);
            java.lang.reflect.Method energyBlockMethod = energyCapObject.getClass().getMethod("block");
            Object energyBlockCap = energyBlockMethod.invoke(energyCapObject);

            if (energyBlockCap instanceof net.neoforged.neoforge.capabilities.BlockCapability) {
                @SuppressWarnings("unchecked")
                var blockCap = (net.neoforged.neoforge.capabilities.BlockCapability<Object, Direction>) energyBlockCap;

                event.registerBlock(
                    blockCap,
                    (level, pos, state, be, side) -> {
                        if (be instanceof GravityNormalizerBlockEntity normalizer) {
                            return normalizer.getStrictEnergyHandler();
                        }
                        return null;
                    },
                    ModBlocks.GRAVITY_NORMALIZER.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism energy capability for gravity normalizer");
            }

            // Also register STRICT_ENERGY capability for full Mekanism compatibility
            java.lang.reflect.Field strictEnergyField = mekCapabilities.getField("STRICT_ENERGY");
            Object strictEnergyCapObject = strictEnergyField.get(null);
            java.lang.reflect.Method strictEnergyBlockMethod = strictEnergyCapObject.getClass().getMethod("block");
            Object strictEnergyBlockCap = strictEnergyBlockMethod.invoke(strictEnergyCapObject);

            if (strictEnergyBlockCap instanceof net.neoforged.neoforge.capabilities.BlockCapability) {
                @SuppressWarnings("unchecked")
                var blockCap = (net.neoforged.neoforge.capabilities.BlockCapability<Object, Direction>) strictEnergyBlockCap;

                event.registerBlock(
                    blockCap,
                    (level, pos, state, be, side) -> {
                        if (be instanceof GravityNormalizerBlockEntity normalizer) {
                            return normalizer.getStrictEnergyHandler();
                        }
                        return null;
                    },
                    ModBlocks.GRAVITY_NORMALIZER.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism strict energy capability for gravity normalizer");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Could not register Mekanism capabilities for gravity normalizer: {}", e.getMessage());
        }
    }
}
