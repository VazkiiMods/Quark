package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.violetmoon.quark.content.experimental.module.GameNerfsModule;

@Mixin(TripWireHookBlock.class)
public class TripWireHookBlockMixin {

	@WrapWithCondition(method = "calculateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private static boolean fixTripWireDupe(Level instance, BlockPos pos, BlockState state, int flag) {
		if(GameNerfsModule.shouldTripwireHooksCheckForAir() && state.is(Blocks.TRIPWIRE_HOOK))
			return instance.getBlockState(pos).is(Blocks.TRIPWIRE_HOOK);
		return true;
	}

}
