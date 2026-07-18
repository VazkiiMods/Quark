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
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;

import org.violetmoon.quark.content.world.block.GlowShroomRingBlock;
import org.violetmoon.quark.content.world.block.HugeGlowShroomBlock;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.Arrays;
import java.util.List;

public class GlowShroomsFeature extends Feature<NoneFeatureConfiguration> {

	//TODO: this could use some configs
	public GlowShroomsFeature() {
		super(NoneFeatureConfiguration.CODEC);
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
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> config) {
		WorldGenLevel worldgenlevel = config.level();
		BlockPos blockpos = config.origin();
		RandomSource rng = config.random();

		MutableBlockPos setPos = new MutableBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
		for(int i = -6; i < 7; i++)
			for(int j = -6; j < 7; j++)
				for(int k = -2; k < 3; k++) {
					setPos.set(blockpos.getX() + i, blockpos.getY() + k, blockpos.getZ() + j);
					double dist = blockpos.distSqr(setPos);
					if(dist > 10) {
						double chance = 1F - ((dist - 10) / 10);
						if(chance < 0 || rng.nextDouble() < chance)
							continue;
					}

					if(worldgenlevel.isStateAtPosition(setPos, s -> s.getBlock() == Blocks.DEEPSLATE) && worldgenlevel.isStateAtPosition(setPos.above(), BlockState::isAir)) {
						if(rng.nextDouble() < 0.08) {
							boolean placeSmall = !placeHugeGlowShroom(worldgenlevel, rng, setPos.above());

							if(placeSmall)
								worldgenlevel.setBlock(setPos.above(), GlimmeringWealdModule.glow_shroom.defaultBlockState(), 2);
						}
					}
				}

		return true;
	}

	public static boolean placeHugeGlowShroom(LevelAccessor worldIn, RandomSource rand, BlockPos pos) {
		Block block = worldIn.getBlockState(pos.below()).getBlock();
		if(block != Blocks.DEEPSLATE) {
			return false;
		} else {
			BlockPos placePos = pos;

			BlockState stem = GlimmeringWealdModule.glow_shroom_stem.defaultBlockState();
			BlockState ring = GlimmeringWealdModule.glow_shroom_ring.defaultBlockState();
			BlockState cap = GlimmeringWealdModule.glow_shroom_block.defaultBlockState().setValue(HugeGlowShroomBlock.DOWN, false);

			int stemHeight1 = 2;
			int stemHeight2 = rand.nextInt(4);
			boolean hasBigCap = rand.nextDouble() < 0.6;

			// Check if it has space
			int totalHeight = stemHeight1 + stemHeight2 + (hasBigCap ? 2 : 1);
			int horizCheck = 2;

			for(int i = -horizCheck; i < horizCheck + 1; i++)
				for(int j = -horizCheck; j < horizCheck + 1; j++)
					for(int k = 1; k < totalHeight; k++) // start at 1 cuz ground layer doesn't matter
						if(!worldIn.getBlockState(placePos.offset(i, k, j)).isAir())
							return false;

			// Stem #1
			for(int i = 0; i < stemHeight1; i++) {
				worldIn.setBlock(placePos, stem, 2);
				placePos = placePos.above();
			}

			// Offset stem in random direction
			if(stemHeight2 > 0) {
				Direction dir = MiscUtil.HORIZONTALS[rand.nextInt(MiscUtil.HORIZONTALS.length)];
				placePos = placePos.relative(dir);
			}

			// Stem #2
			for(int i = 0; i < stemHeight2; i++) {
				worldIn.setBlock(placePos, stem, 2);
				placePos = placePos.above();
			}

			// Place rings on top of stem
			int ringHeight = Math.min(2, stemHeight2);
			for(int i = 0; i < ringHeight; i++) {
				for(Direction ringDir : MiscUtil.HORIZONTALS)
					worldIn.setBlock(placePos.relative(ringDir).relative(Direction.DOWN, i + 1), ring.setValue(GlowShroomRingBlock.FACING, ringDir), 2);
			}

			// Cap
			for(int i = -1; i < 2; i++)
				for(int j = -1; j < 2; j++)
					worldIn.setBlock(placePos.offset(i, 0, j), cap, 2);

			// Triangle cap
			if(hasBigCap)
				worldIn.setBlock(placePos.above(), cap, 2);

			return true;
		}
	}

}
