package org.violetmoon.quark.addons.oddities.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.TinyPotatoBlock;
import org.violetmoon.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import org.violetmoon.quark.addons.oddities.util.TinyPotatoInfo;
import org.violetmoon.quark.api.IRuneColorProvider;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.zeta.item.ZetaBlockItem;

import java.util.List;

public class TinyPotatoBlockItem extends ZetaBlockItem implements IRuneColorProvider {
	private static final int NOT_MY_NAME = 17;
	private static final List<String> TYPOS = List.of("vaskii", "vazki", "voskii", "vazkkii", "vazkki", "vazzki", "vaskki", "vozkii", "vazkil", "vaskil", "vazkill", "vaskill", "vaski");

	private static final String TICKS = "notMyNameTicks";

	public TinyPotatoBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public boolean canEquipZeta(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
		return armorType == EquipmentSlot.HEAD && (entity instanceof Player player && ContributorRewardHandler.getTier(player) > 0);
	}

	@NotNull
	@Override
	public String getDescriptionId(@NotNull ItemStack stack) {
		if(TinyPotatoBlock.isAngry(stack))
			return super.getDescriptionId(stack) + ".angry";
		return super.getDescriptionId(stack);
	}

	private void updateData(ItemStack stack) {
		CompoundTag tileTag = stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
		// TODO: seems to me like this whole block isnt needed as tater never has that tag
		if(tileTag != null) {
			// this code seems to move angry tag out of blockEntity tag. Maybe it could be simplified
			if(tileTag.contains(TinyPotatoBlockEntity.TAG_ANGRY, Tag.TAG_ANY_NUMERIC)) {
				boolean angry = tileTag.getBoolean(TinyPotatoBlockEntity.TAG_ANGRY);
				if(angry)
					stack.set(QuarkDataComponents.IS_ANGRY, true);
				else if(TinyPotatoBlock.isAngry(stack))
					stack.set(QuarkDataComponents.IS_ANGRY, false);
				tileTag.remove(TinyPotatoBlockEntity.TAG_ANGRY);
			}

			//remove this?
			if(tileTag.contains(TinyPotatoBlockEntity.TAG_NAME, Tag.TAG_STRING)) {
				stack.set(DataComponents.CUSTOM_NAME, (Component.Serializer.fromJson(tileTag.getString(TinyPotatoBlockEntity.TAG_NAME))));
				tileTag.remove(TinyPotatoBlockEntity.TAG_NAME);
			}
		}

		if(Boolean.TRUE.equals(stack.get(QuarkDataComponents.IS_ANGRY)))
			stack.set(QuarkDataComponents.IS_ANGRY, false);
	}

	@Override
	public boolean onEntityItemUpdateZeta(ItemStack stack, ItemEntity entity) {
		updateData(stack);
		return super.onEntityItemUpdateZeta(stack, entity);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity holder, int itemSlot, boolean isSelected) {
		updateData(stack);

		if(!world.isClientSide && holder instanceof Player player && holder.tickCount % 30 == 0 && TYPOS.contains(ChatFormatting.stripFormatting(stack.getDisplayName().getString()))) {
			int ticks = stack.get(QuarkDataComponents.TICKS);
			if(ticks < NOT_MY_NAME) {
				player.sendSystemMessage(Component.translatable("quark.misc.you_came_to_the_wrong_neighborhood." + ticks).withStyle(ChatFormatting.RED));
				stack.set(QuarkDataComponents.TICKS, ticks + 1);
			}
		}
	}

	@Override
	public boolean isFoil(@NotNull ItemStack stack) {
		return stack.has(DataComponents.CUSTOM_NAME) && TinyPotatoInfo.fromComponent(stack.getHoverName()).enchanted() || super.isFoil(stack);
	}

	@Override
	public RuneColor getRuneColor(ItemStack stack) {
		return stack.has(DataComponents.CUSTOM_NAME) ? TinyPotatoInfo.fromComponent(stack.getHoverName()).runeColor() : null;
	}
}
