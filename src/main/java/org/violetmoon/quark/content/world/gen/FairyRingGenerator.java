package org.violetmoon.quark.content.world.gen;

import java.util.List;

import org.violetmoon.quark.base.config.type.DimensionConfig;
import org.violetmoon.quark.base.world.generator.Generator;
import org.violetmoon.quark.content.world.module.FairyRingsModule;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;

public class FairyRingGenerator extends Generator {

	public FairyRingGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}

	@Override
	public void generateChunk(WorldGenRegion worldIn, ChunkGenerator generator, RandomSource rand, BlockPos corner) {
		int x = corner.getX() + rand.nextInt(16);
		int z = corner.getZ() + rand.nextInt(16);
		BlockPos center = new BlockPos(x, 128, z);

		Holder<Biome> biome = getBiome(worldIn, center, false);

		double chance = 0;
		if(biome.is(BiomeTags.IS_FOREST))
			chance = FairyRingsModule.forestChance;
		else if(biome.is(Tags.Biomes.IS_PLAINS))
			chance = FairyRingsModule.plainsChance;

		if(rand.nextDouble() < chance) {
			BlockPos pos = center;
			BlockState state = worldIn.getBlockState(pos);

			while(state.getMaterial() != Material.GRASS && pos.getY() > 30) {
				pos = pos.below();
				state = worldIn.getBlockState(pos);
			}

			if(state.getMaterial() == Material.GRASS)
				spawnFairyRing(worldIn, generator, pos.below(), biome, rand);
		}
	}

	public static void spawnFairyRing(WorldGenLevel world, ChunkGenerator generator, BlockPos pos, Holder<Biome> biome, RandomSource rand) {
		List<ConfiguredFeature<?, ?>> features = biome.value().getGenerationSettings().getFlowerFeatures();


		Holder<PlacedFeature> holder = features.isEmpty() ? null : ((RandomPatchConfiguration) features.get(0).config()).feature();
		BlockState flowerState = holder == null ? Blocks.OXEYE_DAISY.defaultBlockState() : null;

		for(int i = -3; i <= 3; i++)
			for(int j = -3; j <= 3; j++) {
				float dist = (i * i) + (j * j);
				if(dist < 7 || dist > 10)
					for (int k = 6; k > -3; k--) {
						BlockPos fpos = pos.offset(i, k, j);
						BlockState state = world.getBlockState(fpos);
						if (state.is(BlockTags.SMALL_FLOWERS)) {
							world.setBlock(fpos, Blocks.AIR.defaultBlockState(), 2);
							break;
						}
					}
				else {
					for (int k = 5; k > -4; k--) {
						BlockPos fpos = pos.offset(i, k, j);
						BlockPos fposUp = fpos.above();
						BlockState state = world.getBlockState(fpos);
						if (state.getMaterial() == Material.GRASS && world.isEmptyBlock(fposUp)) {
							if (flowerState == null) {
								holder.value().place(world, generator, rand, fposUp);
								flowerState = world.getBlockState(fposUp);
							} else
								world.setBlock(fposUp, flowerState, 2);
							break;
						}
					}
				}
			}

		BlockPos orePos = pos.below(rand.nextInt(10) + 25);
		BlockState stoneState = world.getBlockState(orePos);
		int down = 0;
		while(!stoneState.is(Tags.Blocks.STONE) && down < 10) {
			orePos = orePos.below();
			stoneState = world.getBlockState(orePos);
			down++;
		}

		if(stoneState.is(Tags.Blocks.STONE)) {
			BlockState ore = FairyRingsModule.ores.get(rand.nextInt(FairyRingsModule.ores.size()));
			world.setBlock(orePos, ore, 2);
			for(Direction face : Direction.values())
				if(rand.nextBoolean())
					world.setBlock(orePos.relative(face), ore, 2);
		}
	}

}
