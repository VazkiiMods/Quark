package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "tweaks")
public class CoralOnCactusModule extends ZetaModule {

	private static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}

	public static boolean scanForWater(BlockState state, BlockGetter world, BlockPos pos, boolean prevValue) {
		if(prevValue || !staticEnabled)
			return prevValue;

		if(state.getBlock() instanceof CoralFanBlock)
			return world.getBlockState(pos.below()).getBlock() == Blocks.CACTUS;

		return false;
	}

}
