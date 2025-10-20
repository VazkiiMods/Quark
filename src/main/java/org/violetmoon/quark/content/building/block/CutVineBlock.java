package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.zeta.block.ZetaVineBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.IZetaBlockColorProvider;

public class CutVineBlock extends ZetaVineBlock implements IZetaBlockColorProvider {

	public CutVineBlock(@Nullable ZetaModule module) {
		super(module, "cut_vine", false);
	}

	@Override
	public boolean canSupportAtFace(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction dir) {
		if(dir != Direction.UP) {
			BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(dir);
			BlockState blockstate = level.getBlockState(pos.above());
			return blockstate.is(Blocks.VINE) && blockstate.getValue(booleanproperty);
		}
		return super.canSupportAtFace(level, pos, dir);
	}

	@Override
	public @Nullable String getBlockColorProviderName() {
		return "vine";
	}

	@Override
	public @Nullable String getItemColorProviderName() {
		return "vine";
	}
}
