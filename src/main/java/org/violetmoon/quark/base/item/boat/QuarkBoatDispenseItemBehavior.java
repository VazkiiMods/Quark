package org.violetmoon.quark.base.item.boat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.handler.WoodSetHandler;

// Pretty much just a copy of BoatDispenseItemBehavior but for the quark boat
public class QuarkBoatDispenseItemBehavior extends DefaultDispenseItemBehavior {

	private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
	private final String type;
	private final boolean chest;

	public QuarkBoatDispenseItemBehavior(String type, boolean chest) {
		this.type = type;
		this.chest = chest;
	}

	@NotNull
	@Override
	public ItemStack execute(BlockSource world, @NotNull ItemStack stack) {
		Direction direction = world.state().getValue(DispenserBlock.FACING);
		Level level = world.level();
		double boatX = world.pos().getX() + (double) ((float) direction.getStepX() * 1.125F);
		double boatY = world.pos().getY() + (double) ((float) direction.getStepY() * 1.125F);
		double boatZ = world.pos().getZ() + (double) ((float) direction.getStepZ() * 1.125F);
		BlockPos pos = world.pos().relative(direction);
		double offset;
		if(level.getFluidState(pos).is(FluidTags.WATER)) {
			offset = 1.0D;
		} else {
			if(!level.getBlockState(pos).isAir() || !level.getFluidState(pos.below()).is(FluidTags.WATER)) {
				return this.defaultDispenseItemBehavior.dispense(world, stack);
			}

			offset = 0.0D;
		}

		Boat boat =
				chest ? new QuarkChestBoat(level, boatX, boatY + offset, boatZ)
						: new QuarkBoat(level, boatX, boatY + offset, boatZ);

		((IQuarkBoat) boat).setQuarkBoatTypeObj(WoodSetHandler.getQuarkBoatType(type));
		boat.setYRot(direction.toYRot());
		level.addFreshEntity(boat);
		stack.shrink(1);
		return stack;
	}

	@Override
	protected void playSound(BlockSource world) {
		world.level().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, world.pos(), 0);
	}
}
