package org.violetmoon.quark.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tweaks.module.MagmaKeepsConcretePowderModule;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Mixin(ConcretePowderBlock.class)
public class ConcretePowderBlockMixin {
	
	@Inject(method = "touchesLiquid", at = @At("HEAD"), cancellable = true, remap = false)
	private static void touchesLiquid(BlockGetter pLevel, BlockPos pPos, BlockState state, CallbackInfoReturnable<Boolean> cbi) {
		if(MagmaKeepsConcretePowderModule.preventSolidify(pLevel, pPos, state)) {
			cbi.setReturnValue(false);
			cbi.cancel();
		}
	}
	
	@Inject(method = "shouldSolidify(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	private static void shouldSolidify(BlockGetter pLevel, BlockPos pPos, BlockState pState, FluidState fluidState, CallbackInfoReturnable<Boolean> cbi) {
		if(MagmaKeepsConcretePowderModule.preventSolidify(pLevel, pPos, pState)) {
			cbi.setReturnValue(false);
			cbi.cancel();
		}
	}

}
