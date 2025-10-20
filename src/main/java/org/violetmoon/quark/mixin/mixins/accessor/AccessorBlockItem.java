package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public interface AccessorBlockItem {

	@Invoker("getPlacementState")
	BlockState quark$getPlacementState(BlockPlaceContext context);

}
