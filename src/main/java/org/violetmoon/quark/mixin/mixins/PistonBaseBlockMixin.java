package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.automation.module.PistonsMoveTileEntitiesModule;
import org.violetmoon.quark.content.experimental.module.GameNerfsModule;

import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

	@ModifyExpressionValue(method = "isPushable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
	private static boolean isPushable(boolean prev, BlockState blockStateIn) {
		return PistonsMoveTileEntitiesModule.shouldMoveTE(prev, blockStateIn);
	}

	@Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToPush()Ljava/util/List;"))
	private void moveBlocks(Level worldIn, BlockPos pos, Direction directionIn, boolean extending, CallbackInfoReturnable<Boolean> callbackInfoReturnable, @Local PistonStructureResolver pistonBlockStructureHelper) {
		PistonsMoveTileEntitiesModule.detachTileEntities(worldIn, pistonBlockStructureHelper, directionIn, extending);
	}

	@ModifyVariable(
		method = "moveBlocks", at = @At(value = "STORE", ordinal = 0), index = 15, ordinal = 2,
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;")
		)
	)
	private BlockPos storeOldPos(BlockPos pos, @Share("oldPos") LocalRef<BlockPos> oldPos) {
		oldPos.set(pos);
		return pos;
	}

	@ModifyVariable(method = "moveBlocks", at = @At(value = "STORE", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"), to = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false)))
	private Map<BlockPos, BlockState> storeMap(Map<BlockPos, BlockState> map, @Share("storedMap") LocalRef<Map<BlockPos, BlockState>> storedMap) {
		storedMap.set(map);
		return map;
	}

	@Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 1, shift = At.Shift.AFTER))
	private void modifyBlockstate(Level worldIn, BlockPos posIn, Direction pistonFacing, boolean extending, CallbackInfoReturnable<Boolean> cir,
			@Share("oldPos") LocalRef<BlockPos> oldPos, @Share("newState") LocalRef<BlockState> newState, @Share("storedMap") LocalRef<Map<BlockPos, BlockState>> storedMap) {
		if(GameNerfsModule.stopPistonPhysicsExploits()) {
			newState.set(worldIn.getBlockState(oldPos.get()));
			storedMap.get().replace(oldPos.get(), newState.get());
		}
	}

	@ModifyArg(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0), index = 2)
	private BlockState modifyMovingBlockEntityState(BlockState state, @Share("newState") LocalRef<BlockState> newState) {
		return GameNerfsModule.stopPistonPhysicsExploits() ? newState.get() : state;
	}

	@Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", ordinal = 0, shift = At.Shift.AFTER))
	private void setOldPosToAir(Level worldIn, BlockPos pos, Direction directionIn, boolean extending, CallbackInfoReturnable<Boolean> cir, @Share("oldPos") LocalRef<BlockPos> oldPos) {
		if(GameNerfsModule.stopPistonPhysicsExploits()) {
			worldIn.setBlock(oldPos.get(), Blocks.AIR.defaultBlockState(), 2 | 4 | 16 | 1024); // paper impl comment: set air to prevent later physics updates from seeing this block
		}
	}
}
