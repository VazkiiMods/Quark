package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.function.BooleanSupplier;

public class VariantLadderBlock extends LadderBlock implements IZetaBlock {

	@Nullable
	private final ZetaModule module;
	private final boolean flammable;

	private BooleanSupplier condition = BooleanSuppliers.TRUE;

	public VariantLadderBlock(String type, @Nullable ZetaModule module, Block.Properties props, boolean flammable) {
		super(props);

		this.module = module;
		this.flammable = flammable;

		if(module == null) //auto registration below this line
			return;

		module.zeta().registry.registerBlock(this, type + "_ladder", true);
		CreativeTabManager.addNextToItem(CreativeModeTabs.FUNCTIONAL_BLOCKS, this, Blocks.LADDER, false);
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return flammable;
	}

	@Nullable
	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public VariantLadderBlock setCondition(BooleanSupplier condition) {
		this.condition = condition;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return condition.getAsBoolean();
	}

}
