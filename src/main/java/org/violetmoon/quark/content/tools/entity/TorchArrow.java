package org.violetmoon.quark.content.tools.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.tools.module.TorchArrowModule;

public class TorchArrow extends AbstractArrow {

	public TorchArrow(EntityType<TorchArrow> type, Level level) {
		super(type, level);
	}

	public TorchArrow(Level level, LivingEntity owner, ItemStack pickupStack, @Nullable ItemStack weapon) {
		super(TorchArrowModule.torchArrowType, owner, level, pickupStack, weapon);
	}

	public TorchArrow(Level level, double x, double y, double z, ItemStack pickupStack, @Nullable ItemStack weapon) {
		super(TorchArrowModule.torchArrowType, x, y, z, level, pickupStack, weapon);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
	}

	@Override
	public void tick() {
		super.tick();

		if(!inGround && level().isClientSide && tickCount > 2) {
			Vec3 motion = getDeltaMovement();
			double rs = 0.03;
			double ms = 0.08;
			double sprd = 0.1;

			int parts = 6;
			for(int i = 0; i < parts; i++) {
				double px = getX() - motion.x * ((float) i / parts) + (Math.random() - 0.5) * sprd;
				double py = getY() - motion.y * ((float) i / parts) + (Math.random() - 0.5) * sprd;
				double pz = getZ() - motion.z * ((float) i / parts) + (Math.random() - 0.5) * sprd;

				double mx = (Math.random() - 0.5) * rs - motion.x * ms;
				double my = (Math.random() - 0.5) * rs - motion.y * ms;
				double mz = (Math.random() - 0.5) * rs - motion.z * ms;

				level().addParticle(ParticleTypes.FLAME, px, py, pz, mx, my, mz);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		if (level().isClientSide) return;

		BlockPos pos = result.getBlockPos();
		Direction direction = result.getDirection();
		BlockPos finalPos = pos.relative(direction);
		BlockState state = level().getBlockState(finalPos);

		if ((state.isAir() || state.canBeReplaced()) && direction != Direction.DOWN) {
			// if(this.getOwner() instanceof Player p && !Quark.FLAN_INTEGRATION.canPlace(p, finalPos)) return;

			BlockState setState = direction == Direction.UP
					? Blocks.TORCH.defaultBlockState()
					: Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, direction);

			if (setState.canSurvive(level(), finalPos)) {
				level().setBlock(finalPos, setState, 2);
				playSound(setState.getSoundType().getPlaceSound());
				discard();
				return;
			}
		}
		super.onHitBlock(result);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		// incredible hack to ensure we still set entities on fire without rendering the fire texture
		igniteForSeconds(1);
		super.onHitEntity(result);
		igniteForSeconds(0);
	}

	@Override
	protected @NotNull ItemStack getPickupItem() {
		return new ItemStack(TorchArrowModule.extinguishOnMiss ? Items.ARROW : TorchArrowModule.torch_arrow);
	}

	@Override
	protected @NotNull ItemStack getDefaultPickupItem() {
		return new ItemStack(TorchArrowModule.extinguishOnMiss ? Items.ARROW : TorchArrowModule.torch_arrow);
	}
}
