package org.violetmoon.quark.addons.oddities.magnetsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import org.violetmoon.quark.api.IMagnetMoveAction;

import java.util.HashMap;

public class DefaultMoveActions {

	public static void addActions(HashMap<Block, IMagnetMoveAction> map) {
		map.put(Blocks.STONECUTTER, DefaultMoveActions::stonecutterMoved);
		map.put(Blocks.HOPPER, DefaultMoveActions::hopperMoved);
	}

	private static void stonecutterMoved(Level world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile) {
		if(!world.isClientSide) {
			BlockPos up = pos.above();
			BlockState breakState = world.getBlockState(up);
			double hardness = breakState.getDestroySpeed(world, up);
			if(hardness > -1 && hardness < 3)
				world.destroyBlock(up, true);
		}
	}

	private static void hopperMoved(Level world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile) {
		if(!world.isClientSide && tile instanceof HopperBlockEntity hopper) {
			hopper.setCooldown(0);

			Direction dir = state.getValue(HopperBlock.FACING);
			BlockPos offPos = pos.relative(dir);
			BlockPos targetPos = pos.relative(direction);
			if(offPos.equals(targetPos))
				return;

			if(world.isEmptyBlock(offPos))
				for(int i = 0; i < hopper.getContainerSize(); i++) {
					ItemStack stack = hopper.getItem(i);
					if(!stack.isEmpty()) {
						ItemStack drop = stack.copy();
						drop.setCount(1);
						hopper.removeItem(i, 1);

						boolean shouldDrop = true;
						if(drop.getItem() instanceof BlockItem) {
							BlockPos farmlandPos = offPos.below();
							if(world.isEmptyBlock(farmlandPos))
								farmlandPos = farmlandPos.below();

							if(world.getBlockState(farmlandPos).getBlock() == Blocks.FARMLAND) {
								Block seedType = ((BlockItem) drop.getItem()).getBlock();
								if(seedType instanceof IPlantable) {
									BlockPos seedPos = farmlandPos.above();
									if(seedType.canSurvive(state, world, seedPos)) {
										BlockState seedState = seedType.defaultBlockState();
										world.playSound(null, seedPos, seedType.getSoundType(seedState).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

										world.setBlockAndUpdate(seedPos, seedState);
										shouldDrop = false;
									}
								}
							}
						}

						if(shouldDrop) {
							double x = pos.getX() + 0.5 + ((double) dir.getStepX() * 0.7);
							double y = pos.getY() + 0.15 + ((double) dir.getStepY() * 0.4);
							double z = pos.getZ() + 0.5 + ((double) dir.getStepZ() * 0.7);
							ItemEntity entity = new ItemEntity(world, x, y, z, drop);
							entity.setDeltaMovement(Vec3.ZERO);
							world.addFreshEntity(entity);
						}

						return;
					}
				}
		}
	}

}
