package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;
import org.violetmoon.quark.content.tweaks.module.GoldToolsHaveFortuneModule;

@Mixin(Item.class)
public class ItemMixin {

	@ModifyReturnValue(method = "overrideStackedOnOther", at = @At("RETURN"))
	public boolean overrideStackedOnOther(boolean prev, ItemStack stack, Slot slot, ClickAction action, Player player) {
		return prev || ExpandedItemInteractionsModule.overrideStackedOnOther(stack, slot, action, player);
	}

	@ModifyReturnValue(method = "overrideOtherStackedOnMe", at = @At("RETURN"))
	public boolean overrideOtherStackedOnMe(boolean prev, ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
		return prev || ExpandedItemInteractionsModule.overrideOtherStackedOnMe(stack, incoming, slot, action, player, accessor);
	}

	@WrapOperation(method = "isCorrectToolForDrops", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/Tool;isCorrectForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	public boolean overrideCorrectTool(Tool tool, BlockState state, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		return stack.getItem() instanceof TieredItem tiered && tiered.getTier() == Tiers.GOLD ? GoldToolsHaveFortuneModule.shouldOverrideCorrectTool(stack, state) : original.call(tool, state);
	}
}
