package com.hecookin.adastramekanized.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Arrays;

/**
 * Custom density function that reads configuration values at runtime.
 * Based on Tectonic's ConfigConstant implementation.
 * Allows planet-specific terrain parameters to be injected into worldgen.
 */
public record ConfigConstant(String planetId, String key, double value) implements DensityFunction {

    // Codec for deserialization from JSON
    public static final MapCodec<ConfigConstant> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("planet_id").forGetter(ConfigConstant::planetId),
            Codec.STRING.fieldOf("key").forGetter(ConfigConstant::key)
        ).apply(instance, ConfigConstant::fromConfig)
    );

    public static final KeyDispatchDataCodec<ConfigConstant> CODEC_HOLDER =
        KeyDispatchDataCodec.of(DATA_CODEC);

    // Factory method that reads from planet config
    public static ConfigConstant fromConfig(String planetId, String key) {
        double value = PlanetConfigHandler.getInstance().getValue(planetId, key);
        return new ConfigConstant(planetId, key, value);
    }

    @Override
    public double compute(FunctionContext context) {
        return value;
    }

    @Override
    public void fillArray(double[] doubles, ContextProvider contextProvider) {
        Arrays.fill(doubles, value);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        // Constants don't reference other functions, so return self
        return this;
    }

    @Override
    public double minValue() {
        return value;
    }

    @Override
    public double maxValue() {
        return value;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}