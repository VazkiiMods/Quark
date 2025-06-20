package org.violetmoon.quark.content.client.tooltip;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.content.client.hax.PseudoAccessorItemStack;
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
	private static AttributeIconEntry getIconForAttribute(Attribute attribute) {
		ResourceLocation loc = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
		if(loc != null)
			return attributes.get(loc);
		return null;
	}

	private static MutableComponent format(Attribute attribute, double value, AttributeDisplayType displayType) {
		switch(displayType) {
		case DIFFERENCE -> {
			return Component.literal((value > 0 ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((value))
					.formatted(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE));
		}
		case PERCENTAGE -> {
			return Component.literal((value > 0 ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((value * 100) + "%")
					.formatted(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE));
		}
		case MULTIPLIER -> {
			AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
			double scaledValue = value / supplier.getBaseValue(Holder.direct(attribute));
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

		if(!Screen.hasShiftDown()) {
			List<Either<FormattedText, TooltipComponent>> tooltipRaw = event.getTooltipElements();
			Map<AttributeSlot, MutableComponent> attributeTooltips = Maps.newHashMap();

			boolean onlyInvalid = true;
			Multimap<Attribute, AttributeModifier> baseCheck = null;
			boolean allAreSame = true;

			for(AttributeSlot slot : AttributeSlot.values()) {
				if(canShowAttributes(stack, slot)) {
					Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);

					if(baseCheck == null)
						baseCheck = slotAttributes;
					else if(slot.hasCanonicalSlot() && allAreSame && !slotAttributes.equals(baseCheck))
						allAreSame = false;

					if(!slotAttributes.isEmpty() && !slot.hasCanonicalSlot())
						allAreSame = false;

					onlyInvalid = extractAttributeValues(stack, attributeTooltips, onlyInvalid, slot, slotAttributes);
				}
			}

			AttributeSlot primarySlot = getPrimarySlot(stack);

			int i = 1;
			for(AttributeSlot slot : AttributeSlot.values()) {
				if(attributeTooltips.containsKey(slot)) {
					int tooltipSlot = (slot == primarySlot ? 1 : i);
					tooltipRaw.add(tooltipSlot, Either.right(new AttributeComponent(stack, slot)));
					i++;

					if(allAreSame)
						break;
				}
			}
		}
	}

	private static Multimap<Attribute, AttributeModifier> getModifiersOnEquipped(Player player, ItemStack stack, Multimap<Attribute, AttributeModifier> attributes, AttributeSlot slot) {
		if(ImprovedTooltipsModule.showUpgradeStatus && slot.hasCanonicalSlot()) {
			ItemStack equipped = player.getItemBySlot(slot.getCanonicalSlot());
			if(!equipped.equals(stack) && !equipped.isEmpty()) {
				equipped.getTooltipLines(Item.TooltipContext.of(player.level()), player, TooltipFlag.Default.NORMAL);
				return getModifiers(equipped, slot);
			}
		}
		return ImmutableMultimap.of();
	}

	private static Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, AttributeSlot slot) {
		var capturedModifiers = ((PseudoAccessorItemStack) (Object) stack).quark$getCapturedAttributes();

		if(capturedModifiers.containsKey(slot)) {
			var map = capturedModifiers.get(slot);
			if(slot == AttributeSlot.MAINHAND) {
				// if(!map.containsKey(Attributes.ATTACK_DAMAGE) && (map.containsKey(Attributes.ATTACK_SPEED) || EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED) > 0)) map.put(Attributes.ATTACK_DAMAGE.value(), new AttributeModifier(Quark.asResource("ign_id"), 0, AttributeModifier.Operation.ADD_VALUE));

				if(!map.containsKey(Attributes.ATTACK_SPEED) && map.containsKey(Attributes.ATTACK_DAMAGE))
					map.put(Attributes.ATTACK_SPEED.value(), new AttributeModifier(Quark.asResource("ign_id"), 0, AttributeModifier.Operation.ADD_VALUE));

				/*if(!map.containsKey(Attributes.ATTACK_KNOCKBACK) && Quark.ZETA.itemExtensions.get(stack).getEnchantmentLevelZeta(stack, Enchantments.KNOCKBACK) > 0)
					map.put(Attributes.ATTACK_KNOCKBACK.value(), new AttributeModifier(Quark.asResource("ign_id"), 0, AttributeModifier.Operation.ADD_VALUE));*/
			}
			return map;
		}
		return ImmutableMultimap.of();
	}

	private static boolean extractAttributeValues(ItemStack stack, Map<AttributeSlot, MutableComponent> attributeTooltips, boolean onlyInvalid, AttributeSlot slot, Multimap<Attribute, AttributeModifier> slotAttributes) {
		boolean anyInvalid = false;
		for(Attribute attr : slotAttributes.keySet()) {
			AttributeIconEntry entry = getIconForAttribute(attr);
			if(entry != null) {
				onlyInvalid = false;
				Minecraft mc = Minecraft.getInstance();
				double attributeValue = getAttribute(mc.player, slot, stack, slotAttributes, attr);
				if(attributeValue != 0) {
					if(!attributeTooltips.containsKey(slot))
						attributeTooltips.put(slot, Component.literal(""));
					attributeTooltips.get(slot).append(format(attr, attributeValue, entry.displayTypes().get(slot)).getString()).append("/");
				}
			} else if(!anyInvalid) {
				anyInvalid = true;
				if(!attributeTooltips.containsKey(slot))
					attributeTooltips.put(slot, Component.literal(""));
				attributeTooltips.get(slot).append("[+]");
			}
		}
		return onlyInvalid;
	}

	private static int renderAttribute(GuiGraphics guiGraphics, Attribute attribute, AttributeSlot slot, int x, int y, ItemStack stack, Multimap<Attribute, AttributeModifier> slotAttributes, Minecraft mc, boolean forceRenderIfZero, Multimap<Attribute, AttributeModifier> equippedSlotAttributes, @Nullable Set<Attribute> equippedAttrsToRender) {
		AttributeIconEntry entry = getIconForAttribute(attribute);
		if(entry != null) {
			if(equippedAttrsToRender != null)
				equippedAttrsToRender.remove(attribute);

			double value = getAttribute(mc.player, slot, stack, slotAttributes, attribute);
			if(value != 0 || forceRenderIfZero) {

				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				guiGraphics.blit(entry.texture(), x, y, 0, 0, 9, 9, 9, 9);

				MutableComponent valueStr = format(attribute, value, entry.displayTypes().get(slot));

				if(ImprovedTooltipsModule.showUpgradeStatus && slot.hasCanonicalSlot()) {
					CompareType compareType = entry.comparison();
					EquipmentSlot equipSlot = slot.getCanonicalSlot();

					if(mc.player != null) {
						ItemStack equipped = mc.player.getItemBySlot(equipSlot);
						if(!equipped.equals(stack) && !equipped.isEmpty()) {
							if(!equippedSlotAttributes.isEmpty()) {
								double otherValue = getAttribute(mc.player, slot, equipped, equippedSlotAttributes, attribute);

								ChatFormatting color = compareType.getColor(value, otherValue);

								if(color != ChatFormatting.WHITE) {
									int xp = x - 2;
									int yp = y - 2;
									if(ImprovedTooltipsModule.animateUpDownArrows && AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() % 20 < 10)
										yp++;

									guiGraphics.blit(color == ChatFormatting.RED ? TEXTURE_DOWNGRADE : TEXTURE_UPGRADE, xp, yp, 0, 0, 13, 13, 13, 13);
								}

								valueStr = valueStr.withStyle(color);
							}
						}
					}
				}

				guiGraphics.drawString(mc.font, valueStr, x + 12, y + 1, -1);
				x += mc.font.width(valueStr) + 20;
			}
		}

		return x;
	}

	private static AttributeSlot getPrimarySlot(ItemStack stack) {
		if(stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem)
			return AttributeSlot.POTION;
		return AttributeSlot.fromCanonicalSlot(stack.getEquipmentSlot());
	}

	private static boolean canShowAttributes(ItemStack stack, AttributeSlot slot) {
		if(stack.isEmpty())
			return false;

		/*
		 This will be remade in Quark 1.21.6, but I don't know if it will be fully accurate in 1.21.1.
		 So, for future reference, heres the 1.20.1 code and a link to how the tooltip hiding works in 1.20.1
		 https://minecraft.wiki/w/Item_format/Before_1.20.5#Display_Properties

		 if(slot == AttributeSlot.POTION)
		 	return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 32) == 0;

		 return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 2) == 0;
		 */

		if(slot == AttributeSlot.POTION)
			return stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP);

		return stack.has(DataComponents.HIDE_TOOLTIP);
	}

	private static double getAttribute(Player player, AttributeSlot slot, ItemStack stack, Multimap<Attribute, AttributeModifier> map, Attribute key) {
		if(player == null) // apparently this can happen
			return 0;

		Collection<AttributeModifier> collection = map.get(key);
		if(collection.isEmpty())
			return 0;

		double value = 0;

		AttributeIconEntry entry = getIconForAttribute(key);
		if(entry == null)
			return 0;

		AttributeDisplayType displayType = entry.displayTypes().get(slot);

		if(displayType != AttributeDisplayType.PERCENTAGE) {
			if(slot != AttributeSlot.POTION || !key.equals(Attributes.ATTACK_DAMAGE)) { // ATTACK_DAMAGE
				AttributeInstance attribute = player.getAttribute(Holder.direct(key));
				if(attribute != null)
					value = attribute.getBaseValue();
			}
		}

		for(AttributeModifier modifier : collection) {
			if(modifier.operation() == AttributeModifier.Operation.ADD_VALUE)
				value += modifier.amount();
		}

		double rawValue = value;

		for(AttributeModifier modifier : collection) {
			if(modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
				value += rawValue * modifier.amount();
		}

		for(AttributeModifier modifier : collection) {
			if(modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
				value += value * modifier.amount();
		}

		// if(key.equals(Attributes.ATTACK_DAMAGE) && slot == AttributeSlot.MAINHAND) value += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
		if(key.equals(Attributes.ATTACK_KNOCKBACK) && slot == AttributeSlot.MAINHAND)
			value += Quark.ZETA.itemExtensions.get(stack).getEnchantmentLevelZeta(stack, player.level().registryAccess().holderOrThrow(Enchantments.KNOCKBACK));

		if(displayType == AttributeDisplayType.DIFFERENCE) {
			if(slot != AttributeSlot.POTION || !key.equals(Attributes.ATTACK_DAMAGE)) {
				AttributeInstance attribute = player.getAttribute(Holder.direct(key));
				if(attribute != null)
					value -= attribute.getBaseValue();
			}
		}

		return value;
	}

	public record AttributeComponent(ItemStack stack,
			AttributeSlot slot) implements ClientTooltipComponent, TooltipComponent {

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

				AttributeSlot primarySlot = getPrimarySlot(stack);
				boolean showSlots = false;
				int x = tooltipX;

				if(canShowAttributes(stack, slot)) {
					Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);
					Multimap<Attribute, AttributeModifier> presentOnEquipped = getModifiersOnEquipped(mc.player, stack, slotAttributes, slot);
					Set<Attribute> equippedAttrsToRender = new LinkedHashSet<>(presentOnEquipped.keySet());

					for(Attribute attr : slotAttributes.keySet()) {
						if(getIconForAttribute(attr) != null) {
							if(slot != primarySlot) {
								showSlots = true;
								break;
							}
						}
					}

					boolean anyToRender = false;
					for(Attribute attr : slotAttributes.keySet()) {
						double value = getAttribute(mc.player, slot, stack, slotAttributes, attr);
						if(value != 0) {
							anyToRender = true;
							break;
						}
					}

					if(anyToRender) {
						if(showSlots) {
							RenderSystem.setShader(GameRenderer::getPositionTexShader);
							RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
							guiGraphics.blit(ClientUtil.GENERAL_ICONS, x, y, 193 + slot.ordinal() * 9, 35, 9, 9, 256, 256);
							x += 20;
						}

						for(Attribute key : slotAttributes.keySet())
							x = renderAttribute(guiGraphics, key, slot, x, y, stack, slotAttributes, mc, false, presentOnEquipped, equippedAttrsToRender);
						for(Attribute key : equippedAttrsToRender)
							x = renderAttribute(guiGraphics, key, slot, x, y, stack, slotAttributes, mc, true, presentOnEquipped, null);

						for(Attribute key : slotAttributes.keys()) {
							if(getIconForAttribute(key) == null) {
								guiGraphics.drawString(font, "[+]", x + 1, y + 1, 0xFFFF55, true);
								break;
							}
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
			int width = 0;

			if(canShowAttributes(stack, slot)) {
				Minecraft mc = Minecraft.getInstance();
				Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);
				Multimap<Attribute, AttributeModifier> presentOnEquipped = getModifiersOnEquipped(mc.player, stack, slotAttributes, slot);
				Set<Attribute> equippedAttrsToRender = new LinkedHashSet<>(presentOnEquipped.keySet());

				AttributeSlot primarySlot = getPrimarySlot(stack);
				boolean showSlots = false;

				for(Attribute attr : slotAttributes.keySet()) {
					if(getIconForAttribute(attr) != null) {
						if(slot != primarySlot) {
							showSlots = true;
							break;
						}
					}
				}

				boolean anyToRender = false;
				for(Attribute attr : slotAttributes.keySet()) {
					double value = getAttribute(mc.player, slot, stack, slotAttributes, attr);
					if(value != 0) {
						anyToRender = true;
						break;
					}
				}

				if(anyToRender) {
					if(showSlots)
						width += 20;

					for(Attribute key : slotAttributes.keySet()) {
						AttributeIconEntry icons = getIconForAttribute(key);
						if(icons != null) {
							equippedAttrsToRender.remove(key);

							double value = getAttribute(mc.player, slot, stack, slotAttributes, key);

							if(value != 0) {

								MutableComponent valueStr = format(key, value, icons.displayTypes().get(slot));
								width += font.width(valueStr) + 20;
							}
						}
					}

					for(Attribute key : equippedAttrsToRender) {
						AttributeIconEntry icons = getIconForAttribute(key);
						if(icons != null) {
							double value = getAttribute(mc.player, slot, stack, slotAttributes, key);
							MutableComponent valueStr = format(key, value, icons.displayTypes().get(slot));
							width += font.width(valueStr) + 20;
						}
					}

					for(Attribute key : slotAttributes.keys()) {
						if(getIconForAttribute(key) == null) {
							width += font.width("[+]") + 8;
							break;
						}
					}
				}
			}

			return width - 8;
		}

	}

}
