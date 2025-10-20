package org.violetmoon.quark.content.building.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.violetmoon.quark.content.building.module.VariantChestsModule;

public class VariantChestBlockEntity extends ChestBlockEntity {

	protected VariantChestBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	public VariantChestBlockEntity(BlockPos pos, BlockState state) {
		super(VariantChestsModule.chestTEType, pos, state);
	}
}
