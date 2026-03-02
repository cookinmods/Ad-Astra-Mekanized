package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.feature.CraterConfiguration;
import com.hecookin.adastramekanized.worldgen.feature.CraterFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, AdAstraMekanized.MOD_ID);

    public static final Supplier<CraterFeature> CRATER =
            FEATURES.register("crater", () -> new CraterFeature(CraterConfiguration.CODEC));

    public static void register(IEventBus modEventBus) {
        AdAstraMekanized.LOGGER.info("Registering ModFeatures...");
        FEATURES.register(modEventBus);
        AdAstraMekanized.LOGGER.info("ModFeatures registration complete");
    }
}
