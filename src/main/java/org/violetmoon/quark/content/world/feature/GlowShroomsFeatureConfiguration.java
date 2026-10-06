package org.violetmoon.quark.content.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record GlowShroomsFeatureConfiguration(BlockStateProvider capProvider, BlockStateProvider stemProvider,
                                              BlockStateProvider ringProvider, int foliageRadius, int baseHeight,
                                              int heightRand) implements FeatureConfiguration {
    public static final Codec<GlowShroomsFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(s -> s.capProvider), BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(s -> s.stemProvider), BlockStateProvider.CODEC.fieldOf("ring_provider").forGetter(s -> s.ringProvider), Codec.INT.fieldOf("foliage_radius").forGetter(s -> s.foliageRadius), Codec.INT.fieldOf("base_height").forGetter(s -> s.baseHeight), Codec.INT.fieldOf("height_rand").forGetter(s -> s.heightRand)).apply(instance, GlowShroomsFeatureConfiguration::new));
}
