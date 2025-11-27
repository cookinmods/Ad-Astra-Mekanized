package com.hecookin.adastramekanized.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction.NoiseHolder;

/**
 * Custom density function for noise with configurable scale.
 * Based on Tectonic's ConfigNoise implementation.
 * Allows runtime configuration of noise frequencies for planet-specific terrain.
 */
public record ConfigNoise(
    String planetId,
    String key,
    NoiseHolder noise,
    DensityFunction shiftX,
    DensityFunction shiftZ,
    double scale
) implements DensityFunction {

    // Codec for deserialization from JSON
    public static final MapCodec<ConfigNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("planet_id").forGetter(ConfigNoise::planetId),
            Codec.STRING.fieldOf("key").forGetter(ConfigNoise::key),
            NoiseHolder.CODEC.fieldOf("noise").forGetter(ConfigNoise::noise),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ConfigNoise::shiftX),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ConfigNoise::shiftZ)
        ).apply(instance, ConfigNoise::fromConfig)
    );

    public static final KeyDispatchDataCodec<ConfigNoise> CODEC_HOLDER =
        KeyDispatchDataCodec.of(DATA_CODEC);

    // Factory method that reads scale from planet config
    public static ConfigNoise fromConfig(
        String planetId,
        String key,
        NoiseHolder noise,
        DensityFunction shiftX,
        DensityFunction shiftZ
    ) {
        double scale = PlanetConfigHandler.getInstance().getValue(planetId, key);
        return new ConfigNoise(planetId, key, noise, shiftX, shiftZ, scale);
    }

    @Override
    public double compute(FunctionContext context) {
        double x = context.blockX() * scale + shiftX.compute(context);
        double y = context.blockY(); // Y is often not scaled for noise
        double z = context.blockZ() * scale + shiftZ.compute(context);
        return noise.getValue(x, y, z);
    }

    @Override
    public void fillArray(double[] doubles, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ConfigNoise(
            planetId,
            key,
            visitor.visitNoise(noise),
            shiftX.mapAll(visitor),
            shiftZ.mapAll(visitor),
            scale
        ));
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}