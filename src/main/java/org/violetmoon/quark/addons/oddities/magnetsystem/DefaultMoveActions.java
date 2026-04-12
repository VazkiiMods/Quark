package org.violetmoon.quark.addons.oddities.magnetsystem;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.quark.api.IMagnetMoveAction;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorBlockBehavior;

import java.util.HashMap;
import java.util.UUID;

public class DefaultMoveActions {

	public static void addActions(HashMap<Block, IMagnetMoveAction> map) {
		map.put(Blocks.STONECUTTER, DefaultMoveActions::stonecutterMoved);
		map.put(Blocks.HOPPER, DefaultMoveActions::hopperMoved);
	}

	private static void stonecutterMoved(Level world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile) {
		if(world instanceof ServerLevel serverLevel) {
			BlockPos up = pos.above();
			BlockState breakState = world.getBlockState(up);

			double hardness = breakState.getDestroySpeed(world, up);
			if(hardness > -1 && hardness < MagnetsModule.stoneCutterMaxHardness) {
				if (MagnetsModule.stoneCutterSilkTouch) {
					destroyBlockWithSilkTouch(breakState, up, serverLevel, 512);
				}else{
					world.destroyBlock(up, true);
				}
			}
		}
	}

	private static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.randomUUID(), "[MagnetStonecutter]");

	//Like level.removeBlock but with skil touch
	private static boolean destroyBlockWithSilkTouch(BlockState blockstate, BlockPos pPos, ServerLevel level, int pRecursionLeft) {
		if (blockstate.isAir()) {
			return false;
		} else {
			FluidState fluidstate = level.getFluidState(pPos);
			if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
				level.levelEvent(2001, pPos, Block.getId(blockstate));
			}

			FakePlayer player = FakePlayerFactory.get(level, FAKE_PLAYER_PROFILE);
			ItemStack tool = Items.IRON_PICKAXE.getDefaultInstance();
			tool.enchant(level.registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.SILK_TOUCH), 1);
			player.setItemInHand(InteractionHand.MAIN_HAND, tool);

			BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pPos) : null;
			Block.dropResources(blockstate, level, pPos, blockentity, player, tool);


			boolean flag = level.setBlock(pPos, fluidstate.createLegacyBlock(), 3, pRecursionLeft);
			if (flag) {
				level.gameEvent(GameEvent.BLOCK_DESTROY, pPos, GameEvent.Context.of(null, blockstate));
			}

			return flag;
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
						if(drop.getItem() instanceof BlockItem blockItem) {
							BlockPos groundPos = offPos.below();
							if(world.isEmptyBlock(groundPos))
								groundPos = groundPos.below();
							Block seedType = blockItem.getBlock();
							if(blockItem instanceof SpecialPlantable || seedType instanceof BushBlock) {
                                BlockPos seedPos = groundPos.above();
                                if(state.canSurvive(world, seedPos)) {
                                    BlockState seedState = seedType.defaultBlockState();
                                    world.playSound(null, seedPos, ((AccessorBlockBehavior)seedType).invokeGetSoundType(seedState).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

                                    boolean canPlace = true;
                                    if(seedState.getBlock() instanceof DoublePlantBlock) {
                                        canPlace = false;

                                        BlockPos abovePos = seedPos.above();
                                        if(world.isEmptyBlock(abovePos)) {
                                            world.setBlockAndUpdate(abovePos, seedState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
                                            canPlace = true;
                                        }
                                    }

                                    if(canPlace) {
                                        world.setBlockAndUpdate(seedPos, seedState);
                                        shouldDrop = false;
                                    }

                                }

							}
						}

						if(shouldDrop) {
							double x = pos.getX() + 0.5 + (dir.getStepX() * 0.7);
							double y = pos.getY() + 0.15 + (dir.getStepY() * 0.4);
							double z = pos.getZ() + 0.5 + (dir.getStepZ() * 0.7);
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
