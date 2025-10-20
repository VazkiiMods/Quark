package org.violetmoon.quark.content.world.feature;

import com.google.common.collect.ImmutableSet;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import org.violetmoon.quark.content.world.module.AncientWoodModule;

import java.util.Comparator;
import java.util.Optional;

public class AncientTreeTopperDecorator extends TreeDecorator {

	public static final MapCodec<AncientTreeTopperDecorator> CODEC = MapCodec.unit(AncientTreeTopperDecorator::new);

	//Registered in AncientWoodModule
	public static final TreeDecoratorType<AncientTreeTopperDecorator> TYPE = new TreeDecoratorType<>(CODEC);

	@Override
	protected TreeDecoratorType<?> type() {
		return TYPE;
	}

	@Override
	public void place(Context ctx) {
		Optional<BlockPos> highestLog = ctx.logs().stream().max(Comparator.comparingInt(Vec3i::getY));
		if(highestLog.isPresent()) {
			BlockPos top = highestLog.get();

			ImmutableSet<BlockPos> leafPos = ImmutableSet.of(
					top.above(), top.east(), top.west(), top.north(), top.south()
			);

			BlockState state = AncientWoodModule.ancient_leaves.defaultBlockState();
			leafPos.forEach(p -> {
				if(ctx.isAir(p))
					ctx.setBlock(p, state);
			});
		}
	}

}
