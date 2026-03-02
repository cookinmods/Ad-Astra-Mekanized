package com.hecookin.adastramekanized.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record CraterConfiguration(
    IntProvider radius,
    IntProvider depth,
    IntProvider rimHeight,
    BlockState floorBlock,
    BlockState rimBlock
) implements FeatureConfiguration {

    public static final Codec<CraterConfiguration> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            IntProvider.codec(1, 40).fieldOf("radius").forGetter(CraterConfiguration::radius),
            IntProvider.codec(1, 20).fieldOf("depth").forGetter(CraterConfiguration::depth),
            IntProvider.codec(0, 10).fieldOf("rim_height").forGetter(CraterConfiguration::rimHeight),
            BlockState.CODEC.fieldOf("floor_block").forGetter(CraterConfiguration::floorBlock),
            BlockState.CODEC.fieldOf("rim_block").forGetter(CraterConfiguration::rimBlock)
        ).apply(instance, CraterConfiguration::new)
    );
}
