package org.violetmoon.quark.content.world.gen;

import java.util.Optional;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.FarmBlock;
import org.violetmoon.quark.content.world.config.BlossomTreeConfig;
import org.violetmoon.zeta.world.generator.Generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public class BlossomTreeGenerator extends Generator {

	private final BlossomTreeConfig quarkConfig;
	private final ResourceKey<ConfiguredFeature<?, ?>> treeKey;

	public BlossomTreeGenerator(BlossomTreeConfig quarkConfig, ResourceKey<ConfiguredFeature<?, ?>> treeKey) {
		super(quarkConfig.dimensions);
		this.quarkConfig = quarkConfig;
		this.treeKey = treeKey;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void generateChunk(WorldGenRegion worldIn, ChunkGenerator generator, RandomSource rand, BlockPos pos) {
		BlockPos placePos = pos.offset(rand.nextInt(16), 0, rand.nextInt(16));
        placePos = worldIn.getHeightmapPos(Types.MOTION_BLOCKING, placePos);
		if(quarkConfig.biomeConfig.canSpawn(getBiome(worldIn, placePos, false)) && rand.nextInt(quarkConfig.rarity) == 0) {
			placePos = worldIn.getHeightmapPos(Types.MOTION_BLOCKING, placePos).below();

			BlockState ground = worldIn.getBlockState(placePos);


			if (ground.getBlock().canSustainPlant(ground, worldIn, pos, Direction.UP, Blocks.OAK_SAPLING.defaultBlockState()).isTrue() || ground.is(BlockTags.DIRT) || ground.getBlock() instanceof FarmBlock) {
				BlockPos up = placePos.above();
				BlockState upState = worldIn.getBlockState(up);

				Registry<ConfiguredFeature<?, ?>> cfgFeatureRegistry = worldIn.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
				ConfiguredFeature<?, ?> cfgFeature = cfgFeatureRegistry.get(treeKey);

				if(cfgFeature == null)
					return; // that's weird

				ConfiguredFeature<TreeConfiguration, ?> cool = (ConfiguredFeature<TreeConfiguration, ?>) cfgFeature;
				FeaturePlaceContext<TreeConfiguration> placeCtx = new FeaturePlaceContext<>(Optional.of(cool), worldIn, generator, rand, up, cool.config());

				if(upState.canBeReplaced(Fluids.WATER))
					worldIn.setBlock(up, Blocks.AIR.defaultBlockState(), 0);

				Feature.TREE.place(placeCtx);
			}
		}
	}

}
