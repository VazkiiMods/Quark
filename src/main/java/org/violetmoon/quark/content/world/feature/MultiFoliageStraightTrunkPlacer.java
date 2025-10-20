package org.violetmoon.quark.content.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

// for Ancient Saplings
public class MultiFoliageStraightTrunkPlacer extends TrunkPlacer {
	final int foliageDistance;
	final int maxBlobs;

	public MultiFoliageStraightTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, int foliageDistance, int maxBlobs) {
		super(baseHeight, heightRandA, heightRandB);
		this.foliageDistance = foliageDistance;
		this.maxBlobs = maxBlobs;
	}

	public static final MapCodec<MultiFoliageStraightTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(overengineered ->
			trunkPlacerParts(overengineered).and(
			overengineered.group(
					Codec.INT.fieldOf("foliageDistance").forGetter(x -> x.foliageDistance),
					Codec.INT.fieldOf("maxBlobs").forGetter(x -> x.maxBlobs)
			)
	).apply(overengineered, MultiFoliageStraightTrunkPlacer::new));

	//Registered in AncientWoodModule
	public static final TrunkPlacerType<MultiFoliageStraightTrunkPlacer> TYPE = new TrunkPlacerType<>(CODEC);

	@Override
	protected TrunkPlacerType<?> type() {
		return TYPE;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> placer,
															RandomSource random, int idk,
															BlockPos rootPos, TreeConfiguration cfg) {
		setDirtAt(level, placer, random, rootPos.below(), cfg);

		List<BlockPos> folliagePositions = new ArrayList<>();

		int placed = 0;
		int j = 0;
		for(int i = idk; i >= 0; --i) {
			BlockPos target = rootPos.above(i);
			this.placeLog(level, placer, random, target, cfg);

			if(placed < maxBlobs) {
				if(j == 0) {
					folliagePositions.add(target);
					j = foliageDistance;
					placed++;
				} else
					j--;
			}

		}

		return folliagePositions.stream().map(p -> new FoliagePlacer.FoliageAttachment(p, 0, false)).collect(Collectors.toList());
	}
}
