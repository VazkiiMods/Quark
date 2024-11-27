package org.violetmoon.quark.content.building.block;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.HollowLogsModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.MiscUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HollowLogBlock extends HollowPillarBlock {

	private final boolean flammable;

	public HollowLogBlock(Block sourceLog, @Nullable ZetaModule module, boolean flammable) {
		this(Quark.ZETA.registryUtil.inherit(sourceLog, "hollow_%s"), sourceLog, module, flammable);
	}

	public HollowLogBlock(String name, Block sourceLog, @Nullable ZetaModule module, boolean flammable) {
		super(name, module,
				MiscUtil.copyPropertySafe(sourceLog)
						.isSuffocating((s, g, p) -> false));

		this.flammable = flammable;
		HollowLogsModule.logMap.put(sourceLog, this);
		
		if(module == null) //auto registration below this line
			return;

		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT_MIPPED);
		setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, sourceLog, false);
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return flammable;
	}
}
