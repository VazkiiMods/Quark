package org.violetmoon.quark.mixin.mixins;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.client.hax.PseudoAccessorItemStack;
import org.violetmoon.quark.content.client.resources.AttributeSlot;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;
import org.violetmoon.quark.content.tweaks.module.GoldToolsHaveFortuneModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public class ItemStackMixin implements PseudoAccessorItemStack {

	/*@ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
	private Component getHoverName(Component prev) {
		return ItemSharingModule.createStackComponent((ItemStack) (Object) this, (MutableComponent) prev);
	}*/

	@ModifyReturnValue(method = "getRarity", at = @At("RETURN"))
	private Rarity getRarity(Rarity prev) {
		return AncientTomesModule.shiftRarity((ItemStack) (Object) this, prev);
	}

	/*@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private void hasTagIfBaked(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir,
							   @Share("removedEnchantments") LocalBooleanRef ref) {
		ItemStack self = (ItemStack) (Object) this;
		if(!self.hasTag() && GoldToolsHaveFortuneModule.shouldShowEnchantments(self)) {
			ref.set(true);
			self.setTag(new CompoundTag());
		}
	}

	@ModifyExpressionValue(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasTag()Z", ordinal = 0))
	private boolean hasTagIfBaked(boolean hasTag, @Share("removedEnchantments") LocalBooleanRef ref) {
		return hasTag || ref.get();
	}*/

	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void removeTagIfBaked(Item.TooltipContext context, Player p_41652_, TooltipFlag p_41653_, CallbackInfoReturnable<List<Component>> cir, @Share("removedEnchantments") LocalBooleanRef ref) {
		ItemStack self = (ItemStack) (Object) this;
		//if(ref.get()) self.setTag(null);
	}

	/*@ModifyArg(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"))
	private ListTag hideSmallerEnchantments(ListTag tag) {
		return GoldToolsHaveFortuneModule.hideSmallerEnchantments((ItemStack) (Object) this, tag);
	}

	@ModifyArg(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"))
	private List<Component> appendEnchantmentNames(List<Component> components) {
		GoldToolsHaveFortuneModule.fakeEnchantmentTooltip((ItemStack) (Object) this, components);
		return components;
	}*/

	@Unique
	private Map<AttributeSlot, Multimap<Attribute, AttributeModifier>> capturedAttributes = new HashMap<>();

	@Override
	public Map<AttributeSlot, Multimap<Attribute, AttributeModifier>> quark$getCapturedAttributes() {
		return capturedAttributes;
	}

	@Override
	public void quark$capturePotionAttributes(List<Pair<Attribute, AttributeModifier>> attributes) {
		Multimap<Attribute, AttributeModifier> attributeContainer = LinkedHashMultimap.create();
		for(var pair : attributes) {
			attributeContainer.put(pair.getFirst(), pair.getSecond());
		}
		capturedAttributes.put(AttributeSlot.POTION, attributeContainer);
	}

	@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private void clearCapturedTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
		capturedAttributes = new HashMap<>();
	}

	/* TODO: Find where this needs to go
	@ModifyReceiver(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z", remap = false))
	private Multimap<Attribute, AttributeModifier> overrideAttributeTooltips(Multimap<Attribute, AttributeModifier> attributes, @Local EquipmentSlot slot) {
		if(ImprovedTooltipsModule.shouldHideAttributes()) {
			capturedAttributes.put(AttributeSlot.fromCanonicalSlot(slot), LinkedHashMultimap.create(attributes));
			return ImmutableMultimap.of();
		}
		return attributes;
	}
	 */
}
