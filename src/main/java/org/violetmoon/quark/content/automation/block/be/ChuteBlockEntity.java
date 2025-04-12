package org.violetmoon.quark.content.automation.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.automation.block.ChuteBlock;
import org.violetmoon.quark.content.automation.module.ChuteModule;
import org.violetmoon.quark.content.building.module.GrateModule;
import org.violetmoon.zeta.block.be.ZetaBlockEntity;

/**
 * @author WireSegal
 *         Created at 10:18 AM on 9/29/19.
 */
public class ChuteBlockEntity extends ZetaBlockEntity {

	private static final AABB CLEARANCE = new AABB(BlockPos.ZERO).deflate(0.25).move(0, 0.25, 0);

	public ChuteBlockEntity(BlockPos pos, BlockState state) {
		super(ChuteModule.blockEntityType, pos, state);
	}

	private boolean canDropItem() {
		if(level != null && level.getBlockState(worldPosition).getValue(ChuteBlock.ENABLED)) {
			BlockPos below = worldPosition.below();
			BlockState state = level.getBlockState(below);
			if (state.isAir()) return true;
			if (state.is(GrateModule.grate)) return true;
			//this could be cached in a blockstate property. maybe micro optimization...
			var shape = state.getCollisionShape(level, below);
			if (shape.isEmpty() ) return true;
			if (shape.max(Direction.Axis.Y)>1) return false;
			for (AABB box : shape.toAabbs()){
				if (box.intersects(CLEARANCE)) return false;
			}
            return true;
		}

		return false;
	}

	private final IItemHandler handler = new IItemHandler() {

		@Override
		public int getSlots() {
			return 1;
		}

		@NotNull
		@Override
		public ItemStack getStackInSlot(int slot) {
			return ItemStack.EMPTY;
		}

		@NotNull
		@Override
		public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if(!canDropItem())
				return stack;

			if(!simulate && level != null && !stack.isEmpty()) {
				ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5,
						worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5, stack.copy());
				entity.setDeltaMovement(0, 0, 0);
				level.addFreshEntity(entity);
			}

			return ItemStack.EMPTY;
		}

		@NotNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return true;
		}
	};

	/* TODO: Need to use ICapabilityProvider in registration (?)
	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(side != Direction.DOWN && cap == ForgeCapabilities.ITEM_HANDLER)
			return LazyOptional.of(() -> handler).cast();
		return super.getCapability(cap, side);
	}
	 */
}
