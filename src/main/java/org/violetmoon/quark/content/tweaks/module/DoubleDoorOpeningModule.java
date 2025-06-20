package org.violetmoon.quark.content.tweaks.module;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.network.message.DoubleDoorMessage;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks", antiOverlap = "utilitix")
public class DoubleDoorOpeningModule extends ZetaModule {

	@Config(flag = "doors_open_together")
	public static boolean enableDoors = true;

	@Config(flag = "fence_gates_open_together")
	public static boolean enableFenceGates = true;

	public static TagKey<Block> nonDoubleDoorTag;
	private static boolean handling = false;

	@Hint(key = "doors_open_together", value = "doors_open_together")
	TagKey<Item> doors = ItemTags.DOORS;

	@Hint(key = "fence_gates_open_together", value = "fence_gates_open_together")
	TagKey<Block> fence_gates = BlockTags.FENCE_GATES;

	@LoadEvent
	public void setup(ZCommonSetup e) {
		nonDoubleDoorTag = Quark.asTagKey(Registries.BLOCK,"non_double_door");
	}

	public boolean openBlock(Level world, Player player, BlockPos pos) {
		if(!this.isEnabled() || world == null)
			return false;

		BlockState state = world.getBlockState(pos);
		if(state.is(nonDoubleDoorTag))
			return false;

		if(enableDoors && state.getBlock() instanceof DoorBlock)
			return openDoor(pos, world, player, state);

		if(enableFenceGates && state.getBlock() instanceof FenceGateBlock)
			return openFenceGate(pos, world, player, state);

		return false;
	}

	private boolean openDoor(BlockPos pos, Level level, Player player, BlockState state) {
		Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
		boolean isOpen = state.getValue(BlockStateProperties.OPEN);
		DoorHingeSide isMirrored = state.getValue(DoorBlock.HINGE);

		BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
		BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();

		return tryOpen(level, player, state, doorPos, direction, isOpen, test -> test.getValue(DoorBlock.HINGE) != isMirrored);
	}

	private boolean openFenceGate(BlockPos pos, Level level, Player player, BlockState state) {
		Direction direction = state.getValue(FenceGateBlock.FACING);
		boolean isOpen = state.getValue(BlockStateProperties.OPEN);

		if(tryOpen(level, player, state, pos.below(), direction, isOpen, Predicates.alwaysTrue()))
			return true;

		return tryOpen(level, player, state, pos.above(), direction, isOpen, Predicates.alwaysTrue());
	}

	private boolean tryOpen(Level level, Player player, BlockState state, BlockPos otherPos, Direction direction, boolean isOpen, Predicate<BlockState> pred) {
		BlockState other = level.getBlockState(otherPos);
		boolean doorCheck = state.getBlock() instanceof DoorBlock doorBlock && doorBlock.type().canOpenByHand();
		boolean fenceGateCheck = state.getBlock() instanceof FenceGateBlock;
		if((doorCheck || fenceGateCheck) && other.getBlock() == state.getBlock() && other.getValue(HorizontalDirectionalBlock.FACING) == direction && other.getValue(BlockStateProperties.OPEN) == isOpen && pred.apply(other)) {
			BlockHitResult res = new BlockHitResult(new Vec3(otherPos.getX() + 0.5, otherPos.getY() + 0.5, otherPos.getZ() + 0.5), direction, otherPos, false);

			if(res.getType() == HitResult.Type.BLOCK) {
				InteractionResult interaction = other.useWithoutItem(level, player, res);
				return interaction != InteractionResult.PASS;
			}
		}

		return false;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends DoubleDoorOpeningModule {
		@PlayEvent
		public void onPlayerInteract(ZRightClickBlock.Low event) {
			Player player = event.getPlayer();
			if(!event.getLevel().isClientSide || player.isDiscrete() || event.isCanceled() || event.getCancellationResult() == InteractionResult.FAIL
					|| event.getUseBlock() == TriState.FALSE || handling)
				return;

			Level world = event.getLevel();
			BlockPos pos = event.getPos();

			/* // TODO: TODO: Wait for Neoforge Flan or remove
			if(!Quark.FLAN_INTEGRATION.canInteract(player, pos))
				return;
			 */

			handling = true;
			boolean opened = openBlock(world, player, pos);
			handling = false;

			if(opened)
				PacketDistributor.sendToServer(new DoubleDoorMessage(pos));
		}
	}
}
