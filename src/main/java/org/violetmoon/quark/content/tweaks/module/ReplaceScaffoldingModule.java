package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ScaffoldingBlock;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.MiscUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@ZetaLoadModule(category = "tweaks")
public class ReplaceScaffoldingModule extends ZetaModule {

	@Config(description = "How many times the algorithm for finding out where a block would be placed is allowed to turn. If you set this to large values (> 3) it may start producing weird effects.")
	public int maxBounces = 1;

	TagKey<Block> cannotReplaceScaffoldingTag;

	@Hint
	Item scaffold = Items.SCAFFOLDING;

	@PlayEvent
	public void onInteract(ZRightClickBlock event) {
		Level world = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		Player player = event.getPlayer();
		if(state.getBlock() instanceof ScaffoldingBlock && !player.isDiscrete()) {
			Direction dir = event.getFace();
			ItemStack stack = event.getItemStack();
			InteractionHand hand = event.getHand();

			if(stack.getItem() instanceof BlockItem bitem) {
				if(bitem.getBlock().defaultBlockState().is(cannotReplaceScaffoldingTag)){
					return;
				}
				Block block = bitem.getBlock();

				if(!(block instanceof ScaffoldingBlock) && !(block instanceof EntityBlock)) {
					BlockPos last = getLastInLine(world, pos, dir);

					UseOnContext context = new UseOnContext(player, hand, new BlockHitResult(new Vec3(0.5F, 1F, 0.5F), dir, last, false));
					BlockPlaceContext bcontext = new BlockPlaceContext(context);

					BlockState stateToPlace = block.getStateForPlacement(bcontext);

					if(Quark.ZETA.modules.isEnabled(LockRotationModule.class)){
						stateToPlace = LockRotationModule.fixBlockRotation(stateToPlace, bcontext);
					}

					if(stateToPlace != null && stateToPlace.canSurvive(world, last)) {
						BlockState currState = world.getBlockState(last);
						world.setBlockAndUpdate(last, stateToPlace);

						BlockPos testUp = last.above();
						BlockState testUpState = world.getBlockState(testUp);
						if(testUpState.getBlock() instanceof ScaffoldingBlock && !stateToPlace.isFaceSturdy(world, last, Direction.UP)) {
							world.setBlockAndUpdate(last, currState);
							return;
						}

						world.playSound(player, last, stateToPlace.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1F, 1F);

						if(!player.getAbilities().instabuild) {
							stack.shrink(1);

							ItemStack giveStack = new ItemStack(currState.getBlock().asItem());
							if(!player.addItem(giveStack))
								player.drop(giveStack, false);
						}

						event.setCanceled(true);
						event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
					}
				}
			}
		}
	}

	private BlockPos getLastInLine(Level world, BlockPos start, Direction clickDir) {
		BlockPos result = getLastInLineOrNull(world, start, clickDir);
		if(result != null)
			return result;

		if(clickDir != Direction.UP) {
			result = getLastInLineOrNull(world, start, Direction.UP);
			if(result != null)
				return result;
		}

		for(Direction horizontal : MiscUtil.HORIZONTALS)
			if(horizontal != clickDir) {
				result = getLastInLineOrNull(world, start, horizontal);
				if(result != null)
					return result;
			}

		if(clickDir != Direction.DOWN) {
			result = getLastInLineOrNull(world, start, Direction.DOWN);
			if(result != null)
				return result;
		}

		return start;
	}

	private BlockPos getLastInLineOrNull(Level world, BlockPos start, Direction dir) {
		BlockPos last = getLastInLineRecursive(world, start, dir, maxBounces);
		if(last.equals(start))
			return null;

		return last;
	}

	private BlockPos getLastInLineRecursive(Level world, BlockPos start, Direction dir, int bouncesAllowed) {
		BlockPos curr = start;
		BlockState currState = world.getBlockState(start);
		Block currBlock = currState.getBlock();

		while(true) {
			BlockPos test = curr.relative(dir);
			if(!world.isLoaded(test))
				break;

			BlockState testState = world.getBlockState(test);
			if(testState.getBlock() == currBlock)
				curr = test;
			else
				break;
		}

		if(!curr.equals(start) && bouncesAllowed > 0) {
			BlockPos maxDist = null;
			double maxDistVal = -1;

			for(Direction dir2 : Direction.values())
				if(dir.getAxis() != dir2.getAxis()) {
					BlockPos bounceStart = curr.relative(dir2);
					if(world.getBlockState(bounceStart).getBlock() == currBlock) {
						BlockPos testDist = getLastInLineRecursive(world, bounceStart, dir2, bouncesAllowed - 1);
						double testDistVal = testDist.distManhattan(curr);
						if(testDistVal > maxDistVal)
							maxDist = testDist;
					}
				}

			if(maxDist != null)
				curr = maxDist;
		}

		return curr;
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		cannotReplaceScaffoldingTag = Quark.asTagKey(Registries.BLOCK, "cannot_replace_scaffolding");
	}

}
