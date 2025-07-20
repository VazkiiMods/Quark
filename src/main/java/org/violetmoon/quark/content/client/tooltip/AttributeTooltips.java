package org.violetmoon.quark.content.client.tooltip;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.catnip.animation.AnimationTickHolder;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.quark.content.client.resources.AttributeIconEntry;
import org.violetmoon.quark.content.client.resources.AttributeIconEntry.CompareType;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.ZSkipAttributeTooltip;

import java.util.*;
import java.util.stream.Collectors;

import static org.violetmoon.quark.content.client.module.ImprovedTooltipsModule.attributeTooltips;

/**
 * @author WireSegal
 * Originally made at 10:34 AM on 9/1/19.
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

	private static MutableComponent format(ItemAttributeModifiers.Entry entry, double baseVal) {
		double value = entry.modifier().amount();
		switch(entry.modifier().operation()) {
			case ADD_VALUE -> {
				return Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((value + baseVal))
                        .formatted(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE));
			}
			case ADD_MULTIPLIED_BASE -> {
				return Component.literal((value > 0 ? "+" : "") + (ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value * 100))
						.formatted(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE) + "%");
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
			tooltipRaw.add(1, Either.right(new AttributeComponent(stack)));
		}
	}

	public static void removeAttributeTooltips(ZSkipAttributeTooltip skipAttributeTooltip) {
		skipAttributeTooltip.setSkipAll(!Screen.hasShiftDown());
	}

	private static int renderAttribute(GuiGraphics guiGraphics, ItemAttributeModifiers.Entry entry, int x, int y, Minecraft mc, @Nullable ItemAttributeModifiers.Entry differenceInAttribute) {
		AttributeIconEntry iconEntry = getIconForAttribute(entry.attribute());

		if (iconEntry != null) {
			guiGraphics.blit(iconEntry.texture(), x, y, 0, 0, 9, 9, 9, 9);
			double baseVal = mc.player.getAttributeBaseValue(entry.attribute());

			// Comparison code.
			if (ImprovedTooltipsModule.showUpgradeStatus && differenceInAttribute != null) {
				MutableComponent valueStr = format(entry, baseVal).withStyle((differenceInAttribute.modifier().amount() > 0) ? ChatFormatting.GREEN : (differenceInAttribute.modifier().amount() < 0) ? ChatFormatting.RED : ChatFormatting.WHITE);
				guiGraphics.drawString(mc.font, valueStr, x + 12, y + 1, -1);
				if(differenceInAttribute.modifier().amount() != 0) {
					int xp = x - 2;
					int yp = y - 2;
					if(ImprovedTooltipsModule.animateUpDownArrows && AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() % 20 < 10)
						yp++;

					guiGraphics.blit(differenceInAttribute.modifier().amount() < 0 ? TEXTURE_DOWNGRADE : TEXTURE_UPGRADE, xp, yp, 0, 0, 13, 13, 13, 13);
				}
				x += mc.font.width(valueStr) + 20;
			} else {
				MutableComponent valueStr = format(entry, baseVal);
				guiGraphics.drawString(mc.font, valueStr, x + 12, y + 1, -1);
				x += mc.font.width(valueStr) + 20;
			}
		}

		return x;
	}

	/**
	 * Simplifies the attributes of the stack. Doing calculations to combine modifiers with the same operations.
	 * @param stack The stack whomst is having their attributes simplified.
	 * @return A simplified set of attribute modifiers. Don't wanna return the actual stack (It would probably cause some issues if it actually combined em)
	 */
	public static ItemAttributeModifiers simplifyStackAttributes(ItemStack stack) {
		ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
		List<ItemAttributeModifiers.Entry> simplifiedList = new ArrayList<>();

		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			// Chat I dont like the double loop I made here.
			boolean addedToList = false;
			for (int j = 0; j < simplifiedList.size(); j++) {

				ItemAttributeModifiers.Entry simplifiedEntry = simplifiedList.get(j);

				// This may seem a little much, but really its just checking if everything that would need to be evaluated is evaluated.
				if (simplifiedEntry.attribute().equals(entry.attribute()) &&
						simplifiedEntry.modifier().operation().equals(entry.modifier().operation()) &&
						simplifiedEntry.slot().equals(entry.slot())) {
					double value = entry.modifier().amount();
					switch (entry.modifier().operation()) { // Different operations mean different values
						case ADD_VALUE, ADD_MULTIPLIED_BASE -> value += simplifiedEntry.modifier().amount();
                        case ADD_MULTIPLIED_TOTAL -> value *= simplifiedEntry.modifier().amount();
                    }

					simplifiedList.set(j, new ItemAttributeModifiers.Entry(entry.attribute(),
							new AttributeModifier(entry.modifier().id(), value, entry.modifier().operation()),
							entry.slot()));
					addedToList = true;
					break;
				}
			}

			if (addedToList) continue;
			simplifiedList.add(entry);
		}

		return new ItemAttributeModifiers(simplifiedList, modifiers.showInTooltip());
	}


	/**
	 * Compares two ItemStacks
	 * @param firstStack Base stack being compared, this is what the player is utilizing right at that moment.
	 * @param secondStack Secondary stack being compared, this is what the player is looking at.
	 * @return The difference in attributes, all stored in an AttributeModifiers thingy
	 */
	public static ItemAttributeModifiers compareAttributesOfStacks(ItemStack firstStack, ItemStack secondStack) {
		ItemAttributeModifiers simplifiedFirstStackMod = simplifyStackAttributes(firstStack);
		ItemAttributeModifiers simplifiedSecondStackMod = simplifyStackAttributes(secondStack);

		List<ItemAttributeModifiers.Entry> comparedModifs = new ArrayList<>();
		for (ItemAttributeModifiers.Entry firstEntry : simplifiedFirstStackMod.modifiers()) {
			for (ItemAttributeModifiers.Entry secondEntry : simplifiedSecondStackMod.modifiers()) {
				if (secondEntry.attribute().equals(firstEntry.attribute()) &&
						secondEntry.modifier().operation().equals(firstEntry.modifier().operation()) &&
						secondEntry.slot().equals(firstEntry.slot())) {
					comparedModifs.add(new ItemAttributeModifiers.Entry(secondEntry.attribute(), new AttributeModifier(
							secondEntry.modifier().id(), secondEntry.modifier().amount() - firstEntry.modifier().amount(), secondEntry.modifier().operation()
					), secondEntry.slot()));
					break;
				}
			}
		}

		return new ItemAttributeModifiers(comparedModifs, simplifiedSecondStackMod.showInTooltip());
	}


	/**
	 * The text component that shows what attributes an item has. Will be used in the tooltip.
	 *
	 * @param stack The stack that has the attributes. Should have the attribute modifier data component.
	 */
	public record AttributeComponent(ItemStack stack) implements ClientTooltipComponent, TooltipComponent {

		@Override
		public void renderImage(@NotNull Font font, int tooltipX, int tooltipY, @NotNull GuiGraphics guiGraphics) {
			PoseStack pose = guiGraphics.pose();

			if(!Screen.hasShiftDown()) {
				pose.pushPose();

				pose.translate(0, 0, 500); // Sets the priority of the tooltip that we are making

				// Shader stuff, shouldn't be too crazy
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				Minecraft mc = Minecraft.getInstance();
				Player player = mc.player; // Player will always be available, if it isnt then uhhhh
				ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS); // The attributes of the item.

				// Sorting has some errors, huh.
				/*modifiers.modifiers().sort((entry, entry2) -> {
					if (!entry.attribute().equals(entry2.attribute())) {
						return entry.attribute().getRegisteredName().compareTo(entry2.attribute().getRegisteredName());
					} else if (entry.modifier().operation() != entry2.modifier().operation()) {
						return entry.modifier().operation().id() - entry2.modifier().operation().id();
					} else return 0;
				});*/

				int x = tooltipX;
				int y = tooltipY - 1;

				List<EquipmentSlotGroup> groups = new ArrayList<>();

				for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
					if (!groups.contains(entry.slot())) groups.add(entry.slot());
				}

				boolean shouldSeparateFromMain = groups.size() > 1;
				ItemStack comparedItem = player.getItemBySlot(player.getEquipmentSlotForItem(stack));

				for (EquipmentSlotGroup slotGroup : groups) {
					int groupX = tooltipX;
					if (shouldSeparateFromMain) {
						guiGraphics.blit(ClientUtil.GENERAL_ICONS, groupX, y, 193 + slotGroup.ordinal() * 9, 35, 9, 9, 256, 256);
						groupX += 16;
					}

					if (!comparedItem.isEmpty() && !comparedItem.equals(stack) &&
							comparedItem.has(DataComponents.ATTRIBUTE_MODIFIERS) &&
							!comparedItem.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().isEmpty()) {
						ItemAttributeModifiers differenceInModifiers = compareAttributesOfStacks(comparedItem, stack);

						for (ItemAttributeModifiers.Entry entry : simplifyStackAttributes(stack).modifiers()) {
							if (entry.slot() != slotGroup) {
								continue;
							}

							ItemAttributeModifiers.Entry diffAttribute = null;

							for (ItemAttributeModifiers.Entry potentialDiffAttribute : differenceInModifiers.modifiers()) {
								if (potentialDiffAttribute.modifier().operation().equals(entry.modifier().operation()) &&
										potentialDiffAttribute.slot().equals(entry.slot()) && potentialDiffAttribute.attribute().equals(entry.attribute())) {
									diffAttribute = potentialDiffAttribute;
									break;
								}
							}

							groupX = renderAttribute(guiGraphics, entry, groupX, y, mc, diffAttribute);
						}
					} else {
						for (ItemAttributeModifiers.Entry entry : simplifyStackAttributes(stack).modifiers()) {
							if (entry.slot() != slotGroup) {
								continue;
							}

							groupX = renderAttribute(guiGraphics, entry, groupX, y, mc, null);
						}
					}
					x = groupX;
					y += 10;
				}

				pose.popPose();

			}
		}

		@Override
		public int getHeight() {
			int y = 0;
			ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
			if (modifiers.showInTooltip()) {
				List<EquipmentSlotGroup> groups = new ArrayList<>();
				for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
					if (!groups.contains(entry.slot())) groups.add(entry.slot());
				}
				y = groups.size() * 10;
			}
			return y;
		}

		@Override
		public int getWidth(@NotNull Font font) {
			int width = 8;
			ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
			Minecraft mc = Minecraft.getInstance();

			if (modifiers.showInTooltip()) {
				List<EquipmentSlotGroup> groups = new ArrayList<>();

				for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
					if (!groups.contains(entry.slot())) groups.add(entry.slot());
				}

				int x = 0;
				for (EquipmentSlotGroup slotGroup : groups) {
					int groupX = 0;
					if (groups.size() > 1) {
						groupX += 16;
					}

					for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
						if (entry.slot() != slotGroup) continue;
						double baseVal = mc.player.getAttributeBaseValue(entry.attribute());
						groupX += mc.font.width(format(entry, baseVal)) + 20;
					}
					x = Math.max(x, groupX);
				}
				width = x;
			}
			return width - 8;
		}
	}
}
