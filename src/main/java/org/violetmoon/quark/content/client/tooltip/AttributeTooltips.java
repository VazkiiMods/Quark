package org.violetmoon.quark.content.client.tooltip;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.catnip.animation.AnimationTickHolder;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.quark.content.client.resources.AttributeDisplayType;
import org.violetmoon.quark.content.client.resources.AttributeIconEntry;
import org.violetmoon.quark.content.client.resources.AttributeIconEntry.CompareType;
import org.violetmoon.quark.content.client.resources.AttributeSlot;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;

import java.util.*;

/**
 * @author WireSegal
 *         Created at 10:34 AM on 9/1/19.
 */
public class AttributeTooltips {

	public static final ResourceLocation TEXTURE_UPGRADE = Quark.asResource("textures/attribute/upgrade.png");
	public static final ResourceLocation TEXTURE_DOWNGRADE = Quark.asResource("textures/attribute/downgrade.png");

	private static final Map<ResourceLocation, AttributeIconEntry> attributes = new HashMap<>();

	public static void receiveAttributes(Map<String, AttributeIconEntry> map) {
		attributes.clear();
		for(Map.Entry<String, AttributeIconEntry> entry : map.entrySet()) {
			attributes.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
		}
	}

	@Nullable
	private static AttributeIconEntry getIconForAttribute(Holder<Attribute> attribute) {
		ResourceLocation loc = attribute.getKey().location();
		if(loc != null)
			return attributes.get(loc);
		return null;
	}

	private static MutableComponent format(ItemAttributeModifiers.Entry entry) {
		double value = entry.modifier().amount();
		switch(entry.modifier().operation()) {
			case ADD_VALUE -> {
				return Component.literal((value > 0 ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((value))
						.formatted(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE));
			}
			case ADD_MULTIPLIED_BASE -> {
				return Component.literal((value > 0 ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((value * 100) + "%")
						.formatted(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE));
			}
			case ADD_MULTIPLIED_TOTAL -> {
				AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
				double scaledValue = value / supplier.getBaseValue(entry.attribute());
				return Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(scaledValue) + "x")
						.withStyle(scaledValue < 1 ? ChatFormatting.RED : ChatFormatting.WHITE);
			}
		default -> {
				return Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value))
						.withStyle(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE);
			}
		}
	}

	public static void makeTooltip(ZGatherTooltipComponents event) {
		ItemStack stack = event.getItemStack();

		if (!Screen.hasShiftDown() && stack.has(DataComponents.ATTRIBUTE_MODIFIERS) && !stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().isEmpty()) {
			if (Minecraft.getInstance().player == null) return;

			List<Either<FormattedText, TooltipComponent>> tooltipRaw = event.getTooltipElements();
			ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
			Player player = Minecraft.getInstance().player;

			tooltipRaw.add(1, Either.right(new AttributeComponent(stack)));
		}
	}

	public record ModifierData(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
	}

	private static Multimap<Attribute, AttributeModifier> getModifiersOnEquipped(Player player, ItemStack stack, Multimap<Attribute, AttributeModifier> attributes, AttributeSlot slot) {
		return ImmutableMultimap.of();
	}

	private static Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, AttributeSlot slot) {
		return ImmutableMultimap.of();
	}

	private static int renderAttribute(GuiGraphics guiGraphics, ItemAttributeModifiers.Entry entry, int x, int y, ItemStack stack, ItemAttributeModifiers slotAttributes, Minecraft mc, boolean forceRenderIfZero, ItemAttributeModifiers equippedSlotAttributes, @Nullable Set<ItemAttributeModifiers.Entry> equippedAttrsToRender) {
		AttributeIconEntry iconEntry = getIconForAttribute(entry.attribute());
		if(iconEntry != null) {
			if(equippedAttrsToRender != null)
				equippedAttrsToRender.remove(entry);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.blit(iconEntry.texture(), x, y, 0, 0, 9, 9, 9, 9);

			MutableComponent valueStr = format(entry);

			if(ImprovedTooltipsModule.showUpgradeStatus) {
				CompareType compareType = iconEntry.comparison();

				if(mc.player != null) {
					ItemStack equipped = mc.player.getItemBySlot(mc.player.getEquipmentSlotForItem(stack));
					if(!equipped.equals(stack) && !equipped.isEmpty()) {
						if(!equippedSlotAttributes.modifiers().isEmpty()) {
							ItemAttributeModifiers.Entry comparedAttribute = null;

							for (ItemAttributeModifiers.Entry real : equippedSlotAttributes.modifiers()) {
								if (real.attribute().equals(entry.attribute())) {
									comparedAttribute = real;
								}
							}

							if (comparedAttribute != null) {
								ChatFormatting color = compareType.getColor(entry.modifier().amount(), comparedAttribute.modifier().amount());

								if (color != ChatFormatting.WHITE) {
									int xp = x - 2;
									int yp = y - 2;
									if (ImprovedTooltipsModule.animateUpDownArrows && AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() % 20 < 10)
										yp++;

									guiGraphics.blit(color == ChatFormatting.RED ? TEXTURE_DOWNGRADE : TEXTURE_UPGRADE, xp, yp, 0, 0, 13, 13, 13, 13);
								}

								valueStr = valueStr.withStyle(color);
							}
						}
					}
				}
			}

			guiGraphics.drawString(mc.font, valueStr, x + 12, y + 1, -1);
			x += mc.font.width(valueStr) + 20;
		}

		return x;
	}

	private static AttributeSlot getPrimarySlot(ItemStack stack) {
		if(stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem)
			return AttributeSlot.POTION;
		return AttributeSlot.fromCanonicalSlot(stack.getEquipmentSlot());
	}

	private static boolean canShowAttributes(ItemStack stack) {
		if(stack.isEmpty() || !stack.has(DataComponents.ATTRIBUTE_MODIFIERS) || stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().isEmpty())
			return false;

		/*
		 This will be remade in 1.21.4+, but I don't know if it will be fully accurate in 1.21.1.
		 So, for future reference, heres the 1.20.1 code and a link to how the tooltip hiding works in 1.20.1
		 https://minecraft.wiki/w/Item_format/Before_1.20.5#Display_Properties

		 if(slot == AttributeSlot.POTION)
		 	return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 32) == 0;

		 return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 2) == 0;
		 */

		/*if(slot == AttributeSlot.POTION)
			return !stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP);*/

		return !stack.has(DataComponents.HIDE_TOOLTIP);
	}

	public record AttributeComponent(ItemStack stack) implements ClientTooltipComponent, TooltipComponent {

		@Override
		public void renderImage(@NotNull Font font, int tooltipX, int tooltipY, @NotNull GuiGraphics guiGraphics) {
			PoseStack pose = guiGraphics.pose();

			if(!Screen.hasShiftDown()) {
				pose.pushPose();
				pose.translate(0, 0, 500);

				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				Minecraft mc = Minecraft.getInstance();
				//fixme port 1.20 check if this even does anything
//				pose.translate(0F, 0F, mc.getItemRenderer().blitOffset);

				int y = tooltipY - 1;

				EquipmentSlot primarySlot = mc.player.getEquipmentSlotForItem(stack);
				boolean showSlots = false;
				int x = tooltipX;

				ItemAttributeModifiers attributeModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

				ItemStack itemInSlot = mc.player.getItemBySlot(primarySlot);
				ItemAttributeModifiers modifiersOnItemInSlot = itemInSlot.get(DataComponents.ATTRIBUTE_MODIFIERS);

				Set<Holder<Attribute>> temporaryLookAtMe = new LinkedHashSet<>();
				modifiersOnItemInSlot.modifiers().forEach((entry -> temporaryLookAtMe.add(entry.attribute())));
				Set<ItemAttributeModifiers.Entry> equippedAttrsToRender = new LinkedHashSet<>();

				for (ItemAttributeModifiers.Entry entry : attributeModifiers.modifiers()) {
					if (temporaryLookAtMe.contains(entry.attribute())) {
						equippedAttrsToRender.add(entry);
					}
				}

				//Multimap<Attribute, AttributeModifier> presentOnEquipped = getModifiersOnEquipped(mc.player, stack, slotAttributes, slot);
				//Set<Attribute> equippedAttrsToRender = new LinkedHashSet<>(presentOnEquipped.keySet());

				for(ItemAttributeModifiers.Entry entry : attributeModifiers.modifiers()) {
					if(getIconForAttribute(entry.attribute()) != null) {
						if(entry.slot().test(primarySlot)) {
							showSlots = true;
							break;
						}
					}
				}

				boolean anyToRender = false;
				for(ItemAttributeModifiers.Entry entry : attributeModifiers.modifiers()) {
					if(entry.modifier().amount() != 0) {
						anyToRender = true;
						break;
					}
				}

				if(anyToRender) {
					if(showSlots) {
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						guiGraphics.blit(ClientUtil.GENERAL_ICONS, x, y, 193 + primarySlot.ordinal() * 9, 35, 9, 9, 256, 256);
						x += 20;
					}

					for(ItemAttributeModifiers.Entry entry : attributeModifiers.modifiers())
						x = renderAttribute(guiGraphics, entry,  x, y, stack, attributeModifiers, mc, false, modifiersOnItemInSlot, equippedAttrsToRender);
					for(ItemAttributeModifiers.Entry entry : equippedAttrsToRender)
						x = renderAttribute(guiGraphics, entry, x, y, stack, attributeModifiers, mc, true, modifiersOnItemInSlot, null);

					for(ItemAttributeModifiers.Entry entry : attributeModifiers.modifiers()) {
						if(getIconForAttribute(entry.attribute()) == null) {
							guiGraphics.drawString(font, "[+]", x + 1, y + 1, 0xFFFF55, true);
							break;
						}
					}
				}


				pose.popPose();

			}
		}

		@Override
		public int getHeight() {
			return 10;
		}

		@Override
		public int getWidth(@NotNull Font font) {
			return 128;
		}

	}

}
