package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tweaks.module.EnhancedLaddersModule;

@Mixin(LadderBlock.class)
public class LadderBlockMixin extends BlockBehaviourMixin {
	@ModifyReturnValue(method = "canSurvive", at = @At("RETURN"))
	private boolean canSurviveTweak(boolean original, BlockState state, LevelReader level, BlockPos pos) {
		return EnhancedLaddersModule.canSurviveTweak(original, state, level, pos);
	}

	/**
	 * In vanilla, the only way to break a ladder is to break the block behind it. So in updateShape,
	 * vanilla only does ladder canSurvive checks if the update came from behind. But with Quark's
	 * freestanding ladder feature, it's possible to break a ladder by breaking a block *above* it.
	 */
	@Inject(method = "updateShape", at = @At(value = "HEAD"), cancellable = true)
	private void updateShapeTweak(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos, CallbackInfoReturnable<BlockState> cir) {
		if(EnhancedLaddersModule.shouldDoUpdateShapeTweak(pState) && pFacing == Direction.UP) {
			//This is just for fun. Makes the ladders break like bamboo instead of instantly.
			pLevel.scheduleTick(pCurrentPos, (LadderBlock) (Object) this, 1);
			cir.setReturnValue(pState);
		}
	}

	/**
	 * Override from BlockBehaviourMixin which does the actual injection
	 */
	@Override
	public void quark$tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
		if(EnhancedLaddersModule.shouldDoUpdateShapeTweak(pState) && !pState.canSurvive(pLevel, pPos)) {
			pLevel.destroyBlock(pPos, true);
		}
	}
}
