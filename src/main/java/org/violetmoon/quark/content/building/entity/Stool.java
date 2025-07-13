package org.violetmoon.quark.content.building.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.building.block.StoolBlock;

import java.util.List;

public class Stool extends Entity {

	public Stool(EntityType<?> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
	}

	@Override
	public void tick() {
		super.tick();

		List<Entity> passengers = getPassengers();
		boolean dead = passengers.isEmpty();

		BlockPos pos = blockPosition();
		BlockState state = level().getBlockState(pos);

		if(!dead) {
			if(!(state.getBlock() instanceof StoolBlock)) {
				PistonMovingBlockEntity piston = null;
				boolean didOffset = false;

				BlockEntity tile = level().getBlockEntity(pos);
				if(tile instanceof PistonMovingBlockEntity pistonBE && pistonBE.getMovedState().getBlock() instanceof StoolBlock)
					piston = pistonBE;
				else
					for(Direction d : Direction.values()) {
						BlockPos offPos = pos.relative(d);
						tile = level().getBlockEntity(offPos);

						if(tile instanceof PistonMovingBlockEntity pistonBE && pistonBE.getMovedState().getBlock() instanceof StoolBlock) {
							piston = pistonBE;
							break;
						}
					}

				if(piston != null) {
					Direction dir = piston.getMovementDirection();
					move(MoverType.PISTON, new Vec3((float) dir.getStepX() * 0.33, (float) dir.getStepY() * 0.33, (float) dir.getStepZ() * 0.33));

					didOffset = true;
				}

				dead = !didOffset;
			}
		}

		if(dead && !level().isClientSide) {
			removeAfterChangingDimensions();

			if(state.getBlock() instanceof StoolBlock)
				level().setBlockAndUpdate(pos, state.setValue(StoolBlock.SAT_IN, false));
		}
	}

	@Override
	public Vec3 getPassengerRidingPosition(Entity entity) {
		return super.getPassengerRidingPosition(entity).subtract(0, 0.5, 0);//this.position().add(this.getPassengerAttachmentPoint(entity, entity.getType().getDimensions(), -0.3F));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		// NO-OP
	}

	@Override
	protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
		// NO-OP
	}

	@Override
	protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
		// NO-OP
	}
}
