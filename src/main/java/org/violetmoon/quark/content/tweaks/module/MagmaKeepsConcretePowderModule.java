package org.violetmoon.quark.content.tweaks.module;

import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@ZetaLoadModule(category = "tweaks")
public class MagmaKeepsConcretePowderModule extends ZetaModule {

	private static boolean staticEnabled;
	
	@LoadEvent
	public void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}
	
	public static boolean preventSolidify(BlockGetter pLevel, BlockPos pPos, BlockState state) {
		if(!staticEnabled)
			return false;
		
		return pLevel.getBlockState(pPos.below()).is(Blocks.MAGMA_BLOCK);
	}
	
}
