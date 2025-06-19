package org.violetmoon.quark.addons.oddities.magnetsystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.MovingMagnetizedBlock;
import org.violetmoon.quark.addons.oddities.block.be.MagnetBlockEntity;
import org.violetmoon.quark.addons.oddities.capability.MagnetTracker;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.quark.api.IMagnetMoveAction;
import org.violetmoon.quark.api.IMagnetTracker;
import org.violetmoon.quark.api.QuarkCapabilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.api.ICollateralMover;
import org.violetmoon.zeta.event.play.ZRecipeCrawl;
import org.violetmoon.zeta.util.RegistryUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class MagnetSystem {

	private static final HashSet<Block> magnetizableBlocks = new HashSet<>();
	private static final HashSet<Item> magnetizableItems = new HashSet<>();

	private static final HashMap<Block, IMagnetMoveAction> BLOCK_MOVE_ACTIONS = new HashMap<>();

	static {
		DefaultMoveActions.addActions(BLOCK_MOVE_ACTIONS);
	}

	public static IMagnetMoveAction getMoveAction(Block block) {
		if (block instanceof IMagnetMoveAction ma)return ma;
		return BLOCK_MOVE_ACTIONS.get(block);
	}

	public static @Nullable IMagnetTracker getTracker(Level level) {
		//Todo: Replaced capability, check if it works.
		return new MagnetTracker(level);
	}

	public static void tick(boolean start, Level level) {
		IMagnetTracker tracker = getTracker(level);
		if(tracker == null)
			return;

		if(!start) {
			for(BlockPos pos : tracker.getTrackedPositions())
				tracker.actOnForces(pos);
		}
		tracker.clear();
	}

	public static void onRecipeReset() {
		magnetizableBlocks.clear();
	}

	public static void onDigest(ZRecipeCrawl.Digest digest) {

		digest.recursivelyFindCraftedItemsFromStrings(MagnetsModule.magneticDerivationList, MagnetsModule.magneticWhitelist, MagnetsModule.magneticBlacklist, i -> {
			if(i instanceof BlockItem bi) {
				magnetizableBlocks.add(bi.getBlock());
			}
			magnetizableItems.add(i);
		});

		//...in favor of manual fixup
		List<Block> magneticBlockWhitelist = RegistryUtil.massRegistryGet(MagnetsModule.magneticWhitelist, BuiltInRegistries.BLOCK);
		List<Block> magneticBlockBlacklist = RegistryUtil.massRegistryGet(MagnetsModule.magneticBlacklist, BuiltInRegistries.BLOCK);

		magnetizableBlocks.addAll(magneticBlockWhitelist);
		magneticBlockBlacklist.forEach(magnetizableBlocks::remove);

		//...and manual fixup for items
		List<Item> magneticItemWhitelist = RegistryUtil.massRegistryGet(MagnetsModule.magneticWhitelist, BuiltInRegistries.ITEM);
		List<Item> magneticItemBlacklist = RegistryUtil.massRegistryGet(MagnetsModule.magneticBlacklist, BuiltInRegistries.ITEM);

		magnetizableItems.addAll(magneticItemWhitelist);
		magneticItemBlacklist.forEach(magnetizableItems::remove);
	}

	public static void applyForce(Level world, BlockPos pos, int magnitude, boolean pushing, Direction dir, int distance, BlockPos origin) {
		IMagnetTracker tracker = getTracker(world);
		if(tracker != null)
			tracker.applyForce(pos, magnitude, pushing, dir, distance, origin);
	}

	public static ICollateralMover.MoveResult getPushAction(MagnetBlockEntity magnet, BlockPos pos, BlockState state, Direction moveDir) {
		if(state.getBlock() instanceof MovingMagnetizedBlock)return ICollateralMover.MoveResult.SKIP;


		Level world = magnet.getLevel();
		if(world != null && canBlockBeMagneticallyMoved(state, pos, world, moveDir, magnet)) {
			BlockPos frontPos = pos.relative(moveDir);
			BlockState frontState = world.getBlockState(frontPos);

			if(state.getBlock() instanceof ICollateralMover cm && cm.isCollateralMover(world, magnet.getBlockPos(), moveDir, pos)){
				//now check if block in front is immovable (probably meaning unbreakable)
				if(MagnetsModule.usePistonLogic && !PistonBaseBlock.isPushable(frontState, world, frontPos, moveDir, true, moveDir.getOpposite())){
					return ICollateralMover.MoveResult.PREVENT;
				}
				return cm.getCollateralMovement(world, magnet.getBlockPos(), moveDir, moveDir, pos);
			}

			if(frontState.isAir())
				return ICollateralMover.MoveResult.MOVE;
			else if(frontState.getPistonPushReaction() == PushReaction.DESTROY)
				return ICollateralMover.MoveResult.BREAK;
		}

		return ICollateralMover.MoveResult.SKIP;
	}

	public static boolean isItemMagnetic(Item item) {
		if(item == Items.AIR)return false;
		return magnetizableItems.contains(item);
	}

	// Just checks if its magnetic. Not if it can be moved
	public static boolean isBlockMagnetic(BlockState state) {
		Block block = state.getBlock();
		if (block == MagnetsModule.magnet || state.isAir()) return false;
		return (magnetizableBlocks.contains(block) || BLOCK_MOVE_ACTIONS.containsKey(block));
	}

	public static boolean canBlockBeMagneticallyMoved(BlockState state, BlockPos pos, Level level, Direction moveDir, BlockEntity magnet) {
		Block block = state.getBlock();

		//cant push stuff that pistons cant push
		if(MagnetsModule.usePistonLogic && !PistonBaseBlock.isPushable(state, level, pos, moveDir, true, moveDir.getOpposite())){
			return false;
		}

		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
			if (state.getValue(PistonBaseBlock.EXTENDED))
				return false;
		}
		if (block == MagnetsModule.magnet || state.isAir()) return false;

		IMagnetMoveAction action = getMoveAction(block);
		if (action != null) return action.canMagnetMove(level, pos, moveDir, state, magnet);
		return (magnetizableBlocks.contains(block));
	}
}
