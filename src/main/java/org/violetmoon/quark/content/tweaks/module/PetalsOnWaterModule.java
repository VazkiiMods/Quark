package org.violetmoon.quark.content.tweaks.module;

import org.violetmoon.quark.content.tweaks.block.WaterPetalBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@ZetaLoadModule(category = "tweaks")
public class PetalsOnWaterModule extends ZetaModule {

	public static Block water_pink_petals;

	@LoadEvent
	public final void register(ZRegister event) {
		water_pink_petals = new WaterPetalBlock(Items.PINK_PETALS, "water_pink_petals", this, Properties.ofFullCopy(Blocks.PINK_PETALS));
	}

	@PlayEvent
	public void onUseOnAir(ZRightClickItem event) {
		ItemStack stack = event.getItemStack();

		if(stack.is(Items.PINK_PETALS)) {
			Player player = event.getEntity();
			Level level = event.getLevel();
			InteractionHand hand = event.getHand();

			BlockHitResult blockhitresult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
			BlockPos pos = blockhitresult.getBlockPos();
			BlockState state = level.getBlockState(pos);
			Direction direction = blockhitresult.getDirection();

			if(state.is(Blocks.WATER)
					&& rightClickPetal(player, level, pos, state, direction, hand, stack)) {
				
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
			}
		}
	}

	@PlayEvent
	public void onUseOnBlock(ZRightClickBlock event) {
		ItemStack stack = event.getItemStack();

		if(stack.is(Items.PINK_PETALS)
				&& rightClickPetal(event.getPlayer(), event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()), event.getFace(), event.getHand(), event.getItemStack())) {

			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
		}
	}

	private boolean rightClickPetal(Player player, Level level, BlockPos pos, BlockState state, Direction direction, InteractionHand hand, ItemStack stack) {
		if(direction == Direction.UP && !state.is(Blocks.WATER) && !state.is(water_pink_petals)) {
			pos = pos.above();
			state = level.getBlockState(pos);
		}
		
		boolean ret = tryPlacePetal(player, level, pos, state, direction, hand, stack);
		if(ret) {
			if(!player.isCreative())
				stack.shrink(1);
			
			level.playSound(player, pos, SoundEvents.PINK_PETALS_PLACE, SoundSource.PLAYERS);
		}
		
		return ret;
	}

	private boolean tryPlacePetal(Player player, Level level, BlockPos pos, BlockState state, Direction direction, InteractionHand hand, ItemStack stack) {
		BlockPlaceContext ctx = new BlockPlaceContext(player, hand, stack, new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), direction, pos, false));

		if(state.is(Blocks.WATER)) {
			FluidState fluid = level.getFluidState(pos);

			if(!fluid.isSource())
				return false;

			BlockPos above = pos.above();
			BlockState stateAbove = level.getBlockState(above);
			place: {
				if(stateAbove.is(water_pink_petals)) {
					state = stateAbove;
					pos = above;

					break place; // defer to the water_pink_petals handler below
				}

				if(stateAbove.isAir()) {
					level.setBlock(above, water_pink_petals.getStateForPlacement(ctx), 1|2);
					return true;
				}
			}
		}

		if(state.is(water_pink_petals)) {
			int amt = state.getValue(PinkPetalsBlock.AMOUNT);
			if(amt < 4) {
				level.setBlock(pos, state.setValue(PinkPetalsBlock.AMOUNT, amt + 1), 1|2);
				return true;
			}
		}

		return false;
	}

}
