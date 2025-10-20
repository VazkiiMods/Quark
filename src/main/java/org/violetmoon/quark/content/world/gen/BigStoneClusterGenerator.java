package org.violetmoon.quark.content.world.gen;

import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.world.config.AirStoneClusterConfig;
import org.violetmoon.quark.content.world.config.BigStoneClusterConfig;
import org.violetmoon.quark.content.world.module.BigStoneClustersModule;
import org.violetmoon.zeta.world.generator.multichunk.ClusterBasedGenerator;
import org.violetmoon.zeta.world.generator.multichunk.ClusterBasedGenerator.IGenerationContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class BigStoneClusterGenerator extends ClusterBasedGenerator {

	private final BigStoneClusterConfig config;
	private final BlockState placeState;

	public BigStoneClusterGenerator(BigStoneClusterConfig config, BlockState placeState, BooleanSupplier condition) {
		super(config.dimensions, () -> config.enabled && condition.getAsBoolean(), config, Objects.toString(Quark.ZETA.registry.getRegistryName(placeState.getBlock(), BuiltInRegistries.BLOCK)).hashCode());
		this.config = config;
		this.placeState = placeState;
	}

	@Override
	public BlockPos[] getSourcesInChunk(WorldGenRegion world, Random random, ChunkGenerator generator, BlockPos chunkCorner) {
		int chance = config.rarity;

		if(chance > 0 && random.nextInt(chance) == 0) {
			int lower = config.minYLevel;
			int range = Math.abs(config.maxYLevel - config.minYLevel);

			BlockPos pos = chunkCorner.offset(random.nextInt(16), random.nextInt(range) + lower, random.nextInt(16));
			if(config.biomes.canSpawn(getBiome(world, pos, true)))
				return new BlockPos[] { pos };
		}


		return new BlockPos[0];
	}

	@Override
	public String toString() {
		return "BigStoneClusterGenerator[" + placeState + "]";
	}

	@Override
	public IGenerationContext createContext(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, WorldGenRegion world) {
		return new IGenerationContext() {
			@Override
			public boolean canPlaceAt(BlockPos pos) {
				return canPlaceBlock(world, pos);
			}

			@Override
			public void consume(BlockPos pos) {
				world.setBlock(pos, placeState, 0);
			}
		};
	}

	private boolean canPlaceBlock(ServerLevelAccessor world, BlockPos pos) {
		if(config instanceof AirStoneClusterConfig clusterConfig && clusterConfig.generateInAir)
			return world.getBlockState(pos).isAir();

		return BigStoneClustersModule.blockReplacePredicate.test(world.getLevel(), world.getBlockState(pos).getBlock());
	}

}
