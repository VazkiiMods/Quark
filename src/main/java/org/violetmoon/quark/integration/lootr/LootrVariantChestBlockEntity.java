package org.violetmoon.quark.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import org.violetmoon.quark.base.Quark;

/**
 * @author WireSegal
 *         Created at 11:32 AM on 7/3/23.
 */
public class LootrVariantChestBlockEntity extends LootrChestBlockEntity {

	public LootrVariantChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public LootrVariantChestBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		this(Quark.LOOTR_INTEGRATION.chestTE(), pWorldPosition, pBlockState);
	}
}
