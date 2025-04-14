package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.content.building.block.be.VariantFurnaceBlockEntity;
import org.violetmoon.quark.content.building.module.VariantFurnacesModule;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.function.BooleanSupplier;

public class VariantFurnaceBlock extends FurnaceBlock implements IZetaBlock {

	private final ZetaModule module;

	public VariantFurnaceBlock(String type, ZetaModule module, Properties props) {
		super(props);

		module.zeta().registry.registerBlock(this, type + "_furnace", true);
		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.FUNCTIONAL_BLOCKS, this, Blocks.FURNACE, false);

		this.module = module;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new VariantFurnaceBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> beType) {
		return createFurnaceTicker(world, beType, VariantFurnacesModule.blockEntityType);
	}

	@Override
	protected void openContainer(Level world, @NotNull BlockPos pos, @NotNull Player player) {
		BlockEntity blockentity = world.getBlockEntity(pos);
		if(blockentity instanceof AbstractFurnaceBlockEntity furnace) {
			player.openMenu(furnace);
			player.awardStat(Stats.INTERACT_WITH_FURNACE);
		}
	}

	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public IZetaBlock setCondition(BooleanSupplier condition) {
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return true;
	}

}
