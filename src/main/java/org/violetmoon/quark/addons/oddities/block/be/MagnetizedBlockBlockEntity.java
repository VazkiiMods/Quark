package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.violetmoon.quark.addons.oddities.magnetsystem.MagnetSystem;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.quark.api.IMagnetMoveAction;
import org.violetmoon.quark.base.Quark;

import java.util.List;

public class MagnetizedBlockBlockEntity extends BlockEntity {
	private BlockState magnetState;
	private CompoundTag subTile;
	private Direction magnetFacing;
	private static final ThreadLocal<Direction> MOVING_ENTITY = ThreadLocal.withInitial(() -> null);
	private float progress;
	private float lastProgress;
	private long lastTicked;

	public MagnetizedBlockBlockEntity(BlockPos pos, BlockState state) {
		super(MagnetsModule.magnetizedBlockType, pos, state);
	}

	public MagnetizedBlockBlockEntity(BlockPos pos, BlockState state, BlockState magnetStateIn, CompoundTag subTileIn, Direction magnetFacingIn) {
		this(pos, state);
		this.magnetState = magnetStateIn;
		this.subTile = subTileIn;
		this.magnetFacing = magnetFacingIn;
	}

	public Direction getFacing() {
		return this.magnetFacing;
	}

	public float getProgress(float ticks) {
		if(ticks > 1.0F) {
			ticks = 1.0F;
		}

		return Mth.lerp(ticks, this.lastProgress, this.progress);
	}

	public float getOffsetX(float ticks) {
		return this.magnetFacing.getStepX() * this.getExtendedProgress(this.getProgress(ticks));
	}

	public float getOffsetY(float ticks) {
		return this.magnetFacing.getStepY() * this.getExtendedProgress(this.getProgress(ticks));
	}

	public float getOffsetZ(float ticks) {
		return this.magnetFacing.getStepZ() * this.getExtendedProgress(this.getProgress(ticks));
	}

	private float getExtendedProgress(float partialTicks) {
		return partialTicks - 1.0F;
	}

	private void moveCollidedEntities(float progress) {
		if(this.level == null)
			return;

		boolean sticky = Quark.ZETA.blockExtensions.get(magnetState).isStickyBlockZeta(magnetState);

		Direction direction = this.magnetFacing;
		double movement = (progress - this.progress);
		VoxelShape collision = magnetState.getCollisionShape(this.level, this.getBlockPos());
		if(!collision.isEmpty()) {
			List<AABB> boundingBoxes = collision.toAabbs();
			AABB containingBox = this.moveByPositionAndProgress(this.getEnclosingBox(boundingBoxes));
			List<Entity> entities = this.level.getEntities(null, this.getMovementArea(containingBox, direction, movement).minmax(containingBox));
			if(!entities.isEmpty()) {

				for(Entity entity : entities) {
					if(entity.getPistonPushReaction() != PushReaction.IGNORE) {
						if(sticky) {
							Vec3 motion = entity.getDeltaMovement();
							double dX = motion.x;
							double dY = motion.y;
							double dZ = motion.z;
							switch(direction.getAxis()) {
							case X -> dX = direction.getStepX();
							case Y -> dY = direction.getStepY();
							case Z -> dZ = direction.getStepZ();
							}

							entity.setDeltaMovement(dX, dY, dZ);
						}

						double motion = 0.0D;

						for(AABB aList : boundingBoxes) {
							AABB movementArea = this.getMovementArea(this.moveByPositionAndProgress(aList), direction, movement);
							AABB entityBox = entity.getBoundingBox();
							if(movementArea.intersects(entityBox)) {
								motion = Math.max(motion, this.getMovement(movementArea, direction, entityBox));
								if(motion >= movement) {
									break;
								}
							}
						}

						if(motion > 0) {
							motion = Math.min(motion, movement) + 0.01D;
							MOVING_ENTITY.set(direction);
							entity.move(MoverType.PISTON, new Vec3(motion * direction.getStepX(), motion * direction.getStepY(), motion * direction.getStepZ()));
							MOVING_ENTITY.set(null);
						}
					}
				}

			}
		}
	}

	private AABB getEnclosingBox(List<AABB> boxes) {
		double minX = 0.0D;
		double minY = 0.0D;
		double minZ = 0.0D;
		double maxX = 1.0D;
		double maxY = 1.0D;
		double maxZ = 1.0D;

		for(AABB bb : boxes) {
			minX = Math.min(bb.minX, minX);
			minY = Math.min(bb.minY, minY);
			minZ = Math.min(bb.minZ, minZ);
			maxX = Math.max(bb.maxX, maxX);
			maxY = Math.max(bb.maxY, maxY);
			maxZ = Math.max(bb.maxZ, maxZ);
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private double getMovement(AABB bb1, Direction facing, AABB bb2) {
		return switch(facing.getAxis()) {
		case X -> getDeltaX(bb1, facing, bb2);
		case Z -> getDeltaZ(bb1, facing, bb2);
		default -> getDeltaY(bb1, facing, bb2);
		};
	}

	private AABB moveByPositionAndProgress(AABB bb) {
		double progress = this.getExtendedProgress(this.progress);
		return bb.move(this.worldPosition.getX() + progress * this.magnetFacing.getStepX(), this.worldPosition.getY() + progress * this.magnetFacing.getStepY(), this.worldPosition.getZ() + progress * this.magnetFacing.getStepZ());
	}

	private AABB getMovementArea(AABB bb, Direction dir, double movement) {
		double d0 = movement * dir.getAxisDirection().getStep();
		double d1 = Math.min(d0, 0.0D);
		double d2 = Math.max(d0, 0.0D);
		return switch(dir) {
		case WEST -> new AABB(bb.minX + d1, bb.minY, bb.minZ, bb.minX + d2, bb.maxY, bb.maxZ);
		case EAST -> new AABB(bb.maxX + d1, bb.minY, bb.minZ, bb.maxX + d2, bb.maxY, bb.maxZ);
		case DOWN -> new AABB(bb.minX, bb.minY + d1, bb.minZ, bb.maxX, bb.minY + d2, bb.maxZ);
		case NORTH -> new AABB(bb.minX, bb.minY, bb.minZ + d1, bb.maxX, bb.maxY, bb.minZ + d2);
		case SOUTH -> new AABB(bb.minX, bb.minY, bb.maxZ + d1, bb.maxX, bb.maxY, bb.maxZ + d2);
		default -> new AABB(bb.minX, bb.maxY + d1, bb.minZ, bb.maxX, bb.maxY + d2, bb.maxZ);
		};
	}

	private static double getDeltaX(AABB bb1, Direction facing, AABB bb2) {
		return facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? bb1.maxX - bb2.minX : bb2.maxX - bb1.minX;
	}

	private static double getDeltaY(AABB bb1, Direction facing, AABB bb2) {
		return facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? bb1.maxY - bb2.minY : bb2.maxY - bb1.minY;
	}

	private static double getDeltaZ(AABB bb1, Direction facing, AABB bb2) {
		return facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? bb1.maxZ - bb2.minZ : bb2.maxZ - bb1.minZ;
	}

	public BlockState getMagnetState() {
		return this.magnetState;
	}

	private IMagnetMoveAction getMoveAction() {
		Block block = magnetState.getBlock();
		if(block instanceof IMagnetMoveAction moveAction)
			return moveAction;

		return MagnetSystem.getMoveAction(block);
	}

	public void finalizeContents(BlockState blockState) {
		if(level == null || level.isClientSide)
			return;

		SoundType soundType = blockState.getSoundType();
		level.playSound(null, worldPosition, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1) * 0.05F, soundType.getPitch() * 0.8F);

		BlockEntity newTile = getSubTile(worldPosition);
		if(newTile != null) {
            level.setBlockEntity(newTile);
            level.updateNeighbourForOutputSignal(worldPosition, blockState.getBlock());
        }

		IMagnetMoveAction action = getMoveAction();
		if(action != null)
			action.onMagnetMoved(level, worldPosition, magnetFacing, blockState, newTile);
	}

	public BlockEntity getSubTile(BlockPos pos) {
		if(subTile != null && !subTile.isEmpty()) {
			CompoundTag tileData = subTile.copy();
			tileData.putInt("x", this.worldPosition.getX());
			tileData.putInt("y", this.worldPosition.getY());
			tileData.putInt("z", this.worldPosition.getZ());
			return BlockEntity.loadStatic(pos, magnetState, subTile, level.registryAccess());
		}
		return null;
	}

	public void clearMagnetTileEntity() {
		if(this.lastProgress < 1.0F && this.level != null) {
			this.progress = 1.0F;
			this.lastProgress = this.progress;

			this.level.removeBlockEntity(this.worldPosition);
			this.setRemoved();
			if(this.level.getBlockState(this.worldPosition).getBlock() == MagnetsModule.magnetized_block) {
				BlockState blockstate = Block.updateFromNeighbourShapes(this.magnetState, this.level, this.worldPosition);
				setAndUpdateBlock(blockstate, 3);
			}
		}

	}

	private void setAndUpdateBlock(BlockState blockstate, int flag) {
		if(this.level == null)
			return;
		this.level.setBlock(this.worldPosition, blockstate, flag);
		this.level.neighborChanged(this.worldPosition, blockstate.getBlock(), this.worldPosition);
		if((blockstate.getBlock() instanceof ButtonBlock || blockstate.getBlock() instanceof BasePressurePlateBlock) &&
				this.level instanceof ServerLevel serverLevel) {
			blockstate.tick(serverLevel, this.worldPosition, serverLevel.random);
			blockstate = this.level.getBlockState(this.worldPosition);
		}

		finalizeContents(blockstate);
    }

	public static void tick(Level level, BlockPos pos, BlockState state, MagnetizedBlockBlockEntity be) {
		be.tick();
	}

	public void tick() {
		if(this.level == null)
			return;
		this.lastTicked = this.level.getGameTime();
		this.lastProgress = this.progress;
		if(this.lastProgress >= 1.0F) {
			this.level.removeBlockEntity(this.worldPosition);
			this.setRemoved();
			if(this.magnetState != null && this.level.getBlockState(this.worldPosition).getBlock() == MagnetsModule.magnetized_block) {
				BlockState blockstate = Block.updateFromNeighbourShapes(this.magnetState, this.level, this.worldPosition);
				if(blockstate.isAir()) {
					this.level.setBlock(this.worldPosition, this.magnetState, 84);
					Block.updateOrDestroy(this.magnetState, blockstate, this.level, this.worldPosition, 3);
				} else {
					if(blockstate.getValues().containsKey(BlockStateProperties.WATERLOGGED) && blockstate.getValue(BlockStateProperties.WATERLOGGED)) {
						blockstate = blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);
					}

					setAndUpdateBlock(blockstate, 67);
				}
			}

		} else {
			float newProgress = this.progress + 0.5F;
			this.moveCollidedEntities(newProgress);
			this.progress = newProgress;
			if(this.progress >= 1.0F) {
				this.progress = 1.0F;
			}

		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		this.magnetState = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag.getCompound("blockState"));
		this.magnetFacing = Direction.from3DDataValue(tag.getInt("facing"));
		this.progress = tag.getFloat("progress");
		this.lastProgress = this.progress;
		this.subTile = tag.getCompound("subTile");
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return writeNBTData(serializeAttachments(provider), false);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		writeNBTData(tag, true);
	}

	private CompoundTag writeNBTData(CompoundTag compound, boolean includeSubTile) {
		if (compound == null) {
			compound = new CompoundTag();
		}
		compound.put("blockState", NbtUtils.writeBlockState(this.magnetState));
		if(includeSubTile)
			compound.put("subTile", subTile);
		compound.putInt("facing", this.magnetFacing.get3DDataValue());
		compound.putFloat("progress", this.lastProgress);
		return compound;
	}

	public VoxelShape getCollisionShape(BlockGetter world, BlockPos pos) {
		Direction direction = MOVING_ENTITY.get();
		if(this.progress < 1.0D && direction == this.magnetFacing) {
			return Shapes.empty();
		} else {

			float progress = this.getExtendedProgress(this.progress);
			double dX = this.magnetFacing.getStepX() * progress;
			double dY = this.magnetFacing.getStepY() * progress;
			double dZ = this.magnetFacing.getStepZ() * progress;
			return magnetState.getCollisionShape(world, pos).move(dX, dY, dZ);
		}
	}

	public long getLastTicked() {
		return this.lastTicked;
	}
}
