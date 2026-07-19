package org.violetmoon.quark.content.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.*;
import org.violetmoon.quark.content.world.block.GlowShroomRingBlock;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.Arrays;
import java.util.List;

public class GlowShroomsFeature extends Feature<GlowShroomsFeatureConfiguration> {

	public GlowShroomsFeature() {
		super(GlowShroomsFeatureConfiguration.CODEC);
	}

	public static List<PlacementModifier> placed() {
		return Arrays.asList(CountPlacement.of(125),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
				RandomOffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome());
	}

	// seed -3443924530208591640
	// /tp 1035 -38 -368

	@Override
	public boolean place(FeaturePlaceContext<GlowShroomsFeatureConfiguration> context) {
		WorldGenLevel worldgenlevel = context.level();
		BlockPos blockpos = context.origin();
		RandomSource rng = context.random();
		GlowShroomsFeatureConfiguration config = context.config();

		MutableBlockPos setPos = new MutableBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
		if(!placeHugeGlowShroom(worldgenlevel, rng, setPos, config))
			worldgenlevel.setBlock(setPos, GlimmeringWealdModule.glow_shroom.defaultBlockState(), 2);

		return true;
	}

	public static boolean placeHugeGlowShroom(LevelAccessor worldIn, RandomSource rand, BlockPos pos, GlowShroomsFeatureConfiguration config) {
		Block block = worldIn.getBlockState(pos.below()).getBlock();
		if(block != Blocks.DEEPSLATE) {
			return false;
		} else {
			BlockPos placePos = pos;

			int stemHeight1 = config.baseHeight();
			int stemHeight2 = rand.nextInt(config.heightRand());
			int capRadius = config.foliageRadius();
			boolean hasBigCap = rand.nextDouble() < 0.6;

			// Check if it has space
			int stemHeight = stemHeight1 + stemHeight2;
			int totalHeight = stemHeight + (hasBigCap ? 2 : 1);

			for(int k = 1; k < totalHeight; k++) { // start at 1 cuz ground layer doesn't matter
				int horizCheck = k < stemHeight ? 2 : capRadius + 1;
				for(int i = -horizCheck; i <= horizCheck; i++)
					for(int j = -horizCheck; j <= horizCheck; j++)
						if(!worldIn.getBlockState(placePos.offset(i, k, j)).isAir())
							return false;
			}

			// Stem #1
			for(int i = 0; i < stemHeight1; i++) {
				worldIn.setBlock(placePos, config.stemProvider().getState(rand, placePos), 2);
				placePos = placePos.above();
			}

			// Offset stem in random direction
			if(stemHeight2 > 0) {
				Direction dir = MiscUtil.HORIZONTALS[rand.nextInt(MiscUtil.HORIZONTALS.length)];
				placePos = placePos.relative(dir);
			}

			// Stem #2
			for(int i = 0; i < stemHeight2; i++) {
				worldIn.setBlock(placePos, config.stemProvider().getState(rand, placePos), 2);
				placePos = placePos.above();
			}

			// Place rings on top of stem
			int ringHeight = Math.min(2, stemHeight2);
			for(int i = 0; i < ringHeight; i++) {
				for(Direction ringDir : MiscUtil.HORIZONTALS) {
					BlockPos relativePos = placePos.relative(ringDir).relative(Direction.DOWN, i + 1);
					BlockState ring = config.ringProvider().getState(rand, relativePos);
					if(ring.hasProperty(GlowShroomRingBlock.FACING))
						ring.setValue(GlowShroomRingBlock.FACING, ringDir);
					worldIn.setBlock(relativePos, ring, 2);
				}
			}

			// Cap
			for(int i = -capRadius; i <= capRadius; i++)
				for(int j = -capRadius; j <= capRadius; j++) {
					BlockPos relativePos = placePos.offset(i, 0, j);
					worldIn.setBlock(relativePos, config.capProvider().getState(rand, relativePos), 2);
				}

			// Triangle cap
			if(hasBigCap) {
				placePos = placePos.above();
				worldIn.setBlock(placePos, config.capProvider().getState(rand, placePos), 2);
			}

			return true;
		}
	}

}
