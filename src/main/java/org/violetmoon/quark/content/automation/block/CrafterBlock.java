/*
 * The Cool MIT License (CMIT)
 *
 * Copyright (c) 2023 Emi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, as long as the person is cool, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * The person is cool.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.violetmoon.quark.content.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.automation.block.be.CrafterBlockEntity;
import org.violetmoon.quark.content.automation.module.CrafterModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

public class CrafterBlock extends ZetaBlock implements EntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   public static final EnumProperty<PowerState> POWER = EnumProperty.create("power", PowerState.class);

	public CrafterBlock(String regname, @Nullable ZetaModule module, Properties properties) {
		super(regname, module, properties);
		registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWER, PowerState.OFF));

		if(module == null) //auto registration below this line
			return;

		setCreativeTab(CreativeModeTabs.REDSTONE_BLOCKS);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrafterBlockEntity(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof CrafterBlockEntity cbe) {
				player.openMenu(cbe);
			}

			return InteractionResult.CONSUME;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.is(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof CrafterBlockEntity cbe) {
				Containers.dropContents(world, pos, cbe);
				world.updateNeighbourForOutputSignal(pos, this);
			}
			super.onRemove(state, world, pos, newState, moved);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		boolean bl = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
		world.setBlock(pos, state.setValue(POWER, bl ? PowerState.ON : PowerState.OFF), 2);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		PowerState powerState = state.getValue(POWER);
		boolean bl = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
		boolean bl2 = powerState.powered();
		
		if (bl && !bl2) {
			world.scheduleTick(pos, this, 6);
			((CrafterBlockEntity) world.getBlockEntity(pos)).craft();
			world.setBlock(pos, state.setValue(POWER, PowerState.TRIGGERED), 2);
		} else if (!bl && state.getValue(POWER) != PowerState.OFF) {
			world.setBlock(pos, state.setValue(POWER, PowerState.OFF), 2);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return ((CrafterBlockEntity) world.getBlockEntity(pos)).getComparatorOutput();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, CrafterModule.blockEntityType, CrafterBlockEntity::tick);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWER);
	}

	public enum PowerState implements StringRepresentable {
		OFF("off"),
		TRIGGERED("triggered"),
		ON("on");

		private final String name;

		PowerState(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.getSerializedName();
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public boolean powered() {
			return this != OFF;
		}
	}
}
