package org.violetmoon.quark.content.automation.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.automation.module.GravisandModule;

public class Gravisand extends FallingBlockEntity {

	private static final EntityDataAccessor<Float> DIRECTION = SynchedEntityData.defineId(Gravisand.class,
			EntityDataSerializers.FLOAT);

	private static final String TAG_DIRECTION = "fallDirection";

	public Gravisand(EntityType<? extends Gravisand> type, Level world) {
		super(type, world);
		this.blockState = GravisandModule.gravisand.defaultBlockState();
	}

	public Gravisand(Level world, double x, double y, double z, float direction) {
		this(GravisandModule.gravisandType, world);
		this.blockState = GravisandModule.gravisand.defaultBlockState();
		this.blocksBuilding = true;
		this.setPos(x, y + (double) ((1.0F - this.getBbHeight()) / 2.0F), z);
		this.setDeltaMovement(Vec3.ZERO);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		this.setStartPos(new BlockPos(blockPosition()));
		entityData.set(DIRECTION, direction);
	}

	@Override
	public void tick() {
		super.tick();

		// vanilla copy for falling upwards stuff
		BlockPos blockpos1 = this.blockPosition();
		boolean aboveHasCollision = !level().getBlockState(blockpos1.above()).getCollisionShape(level(), blockpos1.above()).isEmpty();
		if(!this.level().isClientSide && getFallDirection() > 0 && !isRemoved() && aboveHasCollision) {
			Block block = this.getBlockState().getBlock();
			BlockState blockstate = this.level().getBlockState(blockpos1);
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, 0.5D, 0.7D));
			boolean flag2 = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level(), blockpos1, Direction.UP, ItemStack.EMPTY, Direction.DOWN));
			boolean flag3 = FallingBlock.isFree(this.level().getBlockState(blockpos1.above()));
			boolean flag4 = this.getBlockState().canSurvive(this.level(), blockpos1) && !flag3;

			if(flag2 && flag4) {
				if(this.level().setBlock(blockpos1, this.getBlockState(), 3)) {
					((ServerLevel) this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos1, this.level().getBlockState(blockpos1)));
					this.discard();
				} else if(this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.discard();
					this.callOnBrokenAfterFall(block, blockpos1);
					this.spawnAtLocation(block);
				}
			} else {
				this.discard();
				if(this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.callOnBrokenAfterFall(block, blockpos1);
					this.spawnAtLocation(block);
				}
			}
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DIRECTION, 0F);
	}

	@Override
	public void move(@NotNull MoverType type, @NotNull Vec3 vec) {
		if(type == MoverType.SELF)
			super.move(type, vec.scale(getFallDirection() * -1));
		else
			super.move(type, vec);
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource source) {
		return false;
	}

	private float getFallDirection() {
		return entityData.get(DIRECTION);
	}

	@Override
	protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat(TAG_DIRECTION, getFallDirection());
	}

	@Override
	protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		entityData.set(DIRECTION, compound.getFloat(TAG_DIRECTION));
	}
}
