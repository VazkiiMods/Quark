package vazkii.quark.content.client.tooltip;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.ForgeMod;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.handler.MiscUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author WireSegal
 * Created at 10:34 AM on 9/1/19.
 */
public class AttributeTooltips {

	private static final Attribute MAX_HEALTH = Attributes.MAX_HEALTH;
	private static final Attribute KNOCKBACK_RESISTANCE = Attributes.KNOCKBACK_RESISTANCE;
	private static final Attribute MOVEMENT_SPEED = Attributes.MOVEMENT_SPEED;
	private static final Attribute ATTACK_DAMAGE = Attributes.ATTACK_DAMAGE;
	private static final Attribute ATTACK_SPEED = Attributes.ATTACK_SPEED;
	private static final Attribute ARMOR = Attributes.ARMOR;
	private static final Attribute ARMOR_TOUGHNESS = Attributes.ARMOR_TOUGHNESS;
	private static final Attribute LUCK = Attributes.LUCK;
	private static final Attribute ATTACK_KNOCKBACK = Attributes.ATTACK_KNOCKBACK;
	private static final Attribute REACH_DISTANCE = ForgeMod.REACH_DISTANCE.get();
	private static final Attribute SWIM_SPEED = ForgeMod.SWIM_SPEED.get();
	private static final Attribute ENTITY_GRAVITY = ForgeMod.ENTITY_GRAVITY.get();

	public static final ImmutableSet<Attribute> VALID_ATTRIBUTES = ImmutableSet.of(
			ATTACK_DAMAGE,
			ATTACK_SPEED,
			REACH_DISTANCE,
			ARMOR,
			ARMOR_TOUGHNESS,
			KNOCKBACK_RESISTANCE,
			MAX_HEALTH,
			MOVEMENT_SPEED,
			LUCK,
			SWIM_SPEED,
			ENTITY_GRAVITY,
			ATTACK_KNOCKBACK);

	private static final ImmutableSet<Attribute> MULTIPLIER_ATTRIBUTES = ImmutableSet.of(
			MOVEMENT_SPEED,
			SWIM_SPEED,
			ENTITY_GRAVITY);

	private static final ImmutableSet<Attribute> POTION_MULTIPLIER_ATTRIBUTES = ImmutableSet.of(
			ATTACK_SPEED);

	private static final ImmutableSet<Attribute> PERCENT_ATTRIBUTES = ImmutableSet.of(
			KNOCKBACK_RESISTANCE,
			LUCK);

	private static final ImmutableSet<Attribute> DIFFERENCE_ATTRIBUTES = ImmutableSet.of(
			MAX_HEALTH,
			REACH_DISTANCE);

	private static final ImmutableSet<Attribute> NONMAIN_DIFFERENCE_ATTRIBUTES = ImmutableSet.of(
			ATTACK_DAMAGE,
			ATTACK_SPEED,
			ATTACK_KNOCKBACK);

	private static String format(Attribute attribute, double value, EquipmentSlot slot) {
		if (PERCENT_ATTRIBUTES.contains(attribute))
			return (value > 0 ? "+" : "") + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value * 100) + "%";
		else if (MULTIPLIER_ATTRIBUTES.contains(attribute) || (slot == null && POTION_MULTIPLIER_ATTRIBUTES.contains(attribute)))
			return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value / baseValue(attribute)) + "x";
		else if (DIFFERENCE_ATTRIBUTES.contains(attribute) || (slot != EquipmentSlot.MAINHAND && NONMAIN_DIFFERENCE_ATTRIBUTES.contains(attribute)))
			return (value > 0 ? "+" : "") + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value);
		else
			return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value);
	}

	private static double baseValue(Attribute attribute) {
		if(attribute == MOVEMENT_SPEED)
			return 0.1;
		else if(attribute == ATTACK_SPEED)
			return 4;
		else if(attribute == MAX_HEALTH)
			return 20;
		return 1;
	}

	private static int renderPosition(Attribute attribute) {
		if(attribute == ATTACK_DAMAGE)
			return 1;
		else if(attribute == ATTACK_SPEED)
			return 2;
		else if(attribute == REACH_DISTANCE)
			return 3;
		else if(attribute == ARMOR)
			return 4;
		else if(attribute == ARMOR_TOUGHNESS)
			return 5;
		else if(attribute == KNOCKBACK_RESISTANCE)
			return 6;
		else if(attribute == MOVEMENT_SPEED)
			return 7;
		else if(attribute == LUCK)
			return 8;
		else if(attribute == SWIM_SPEED)
			return 9;
		else if(attribute == ENTITY_GRAVITY)
			return 10;
		else if(attribute == MAX_HEALTH)
			return 11;
		else if(attribute == ATTACK_KNOCKBACK)
			return 12;
		return 0;
	}

	@OnlyIn(Dist.CLIENT)
	public static void makeTooltip(RenderTooltipEvent.GatherComponents event) {
		Minecraft mc = Minecraft.getInstance();
		ItemStack stack = event.getItemStack();

		if(!Screen.hasShiftDown()) {
			List<Either<FormattedText, TooltipComponent>> tooltipRaw = event.getTooltipElements();
			Map<EquipmentSlot, StringBuilder> attributeTooltips = Maps.newHashMap();

			boolean onlyInvalid = true;
			Multimap<Attribute, AttributeModifier> baseCheck = null;
			boolean allAreSame = true;

			EquipmentSlot[] slots = EquipmentSlot.values();
			slots = Arrays.copyOf(slots, slots.length + 1);

			for(EquipmentSlot slot : slots) {
				if (canStripAttributes(stack, slot)) {
					Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);

					if (baseCheck == null)
						baseCheck = slotAttributes;
					else if (slot != null && allAreSame && !slotAttributes.equals(baseCheck))
						allAreSame = false;

					if (!slotAttributes.isEmpty()) {
						if (slot == null)
							allAreSame = false;

						String slotDesc = slot == null ? "potion.whenDrank" : "item.modifiers." + slot.getName();

						int index = -1;
						for (int i = 0; i < tooltipRaw.size(); i++) {
							Either<FormattedText, TooltipComponent> component = tooltipRaw.get(i);
							Optional<FormattedText> left = component.left();
							if (left.isPresent() && equalsOrSibling(left.get(), slotDesc)) {
								index = i;
								break;
							}
						}

						if (index < 0)
							continue;

						tooltipRaw.remove(index - 1); // Remove blank space
						tooltipRaw.remove(index - 1); // Remove actual line
					}

					onlyInvalid = extractAttributeValues(stack, tooltipRaw, attributeTooltips, onlyInvalid, slot, slotAttributes);
				}
			}

			EquipmentSlot primarySlot = Mob.getEquipmentSlotForItem(stack);
			boolean showSlots = !allAreSame && (onlyInvalid ||
					(attributeTooltips.size() == 1 && attributeTooltips.containsKey(primarySlot)));

			for (int i = 0; i < slots.length; i++) {
				EquipmentSlot slot = slots[slots.length - (i + 1)];
				if (attributeTooltips.containsKey(slot)) {
					String stringForSlot = attributeTooltips.get(slot).toString();

					int len = 16;
					if(stringForSlot.contains("/")) {
						stringForSlot = stringForSlot.substring(0, stringForSlot.length() - 1);
						String[] toks = stringForSlot.split("/");
						for(String tok : toks)
							len += mc.font.width(tok) + 5;
					}

					if (showSlots)
						len += 20;

					tooltipRaw.add(1, Either.right(new AttributeComponent(stack, len, 10)));

					if(allAreSame)
						break;
				}
			}
		}
	}

	private static final UUID DUMMY_UUID = new UUID(0, 0);
	private static final AttributeModifier DUMMY_MODIFIER = new AttributeModifier(DUMMY_UUID, "NO-OP", 0.0, AttributeModifier.Operation.ADDITION);

	public static Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, EquipmentSlot slot) {
		if (slot == null) {
			List<MobEffectInstance> potions = PotionUtils.getMobEffects(stack);
			Multimap<Attribute, AttributeModifier> out = HashMultimap.create();

			for (MobEffectInstance effect : potions) {
				MobEffect potion = effect.getEffect();
				Map<Attribute, AttributeModifier> map = potion.getAttributeModifiers();

				for (Attribute attribute : map.keySet()) {
					AttributeModifier baseModifier = map.get(attribute);
					AttributeModifier amplified = new AttributeModifier(baseModifier.getName(), potion.getAttributeModifierValue(effect.getAmplifier(), baseModifier), baseModifier.getOperation());
					out.put(attribute, amplified);
				}
			}

			return out;
		}

		Multimap<Attribute, AttributeModifier> out = stack.getAttributeModifiers(slot);
		if(out.isEmpty()) {
			out = HashMultimap.create();
		} else {
			// convert to our own map
			Multimap<Attribute, AttributeModifier> filtered = HashMultimap.create();
			for (Map.Entry<Attribute, AttributeModifier> entry : out.entries()) {
				AttributeModifier modifier = entry.getValue();
				if (modifier.getId() != null) {
					filtered.put(entry.getKey(), modifier);
				}
			}
			out = filtered;
		}

		if (slot == EquipmentSlot.MAINHAND) {
			if (EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED) > 0)
				out.put(ATTACK_DAMAGE, DUMMY_MODIFIER);

			if (out.containsKey(ATTACK_DAMAGE) && !out.containsKey(ATTACK_SPEED))
				out.put(ATTACK_SPEED, DUMMY_MODIFIER);
			else if (out.containsKey(ATTACK_SPEED) && !out.containsKey(ATTACK_DAMAGE))
				out.put(ATTACK_DAMAGE, DUMMY_MODIFIER);
		}

		return out;
	}

	public static boolean extractAttributeValues(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltip, Map<EquipmentSlot, StringBuilder> attributeTooltips, boolean onlyInvalid, EquipmentSlot slot, Multimap<Attribute, AttributeModifier> slotAttributes) {
		boolean anyInvalid = false;
		for(Attribute attr : slotAttributes.keySet()) {
			if(VALID_ATTRIBUTES.contains(attr)) {
				onlyInvalid = false;
				Minecraft mc = Minecraft.getInstance();
				double attributeValue = getAttribute(mc.player, slot, stack, slotAttributes, attr);
				if (attributeValue != 0) {
					if (!attributeTooltips.containsKey(slot))
						attributeTooltips.put(slot, new StringBuilder());
					attributeTooltips.get(slot).append(format(attr, attributeValue, slot)).append("/");
				}
			} else if (!anyInvalid) {
				anyInvalid = true;
				if (!attributeTooltips.containsKey(slot))
					attributeTooltips.put(slot, new StringBuilder());
				attributeTooltips.get(slot).append("[+]");
			}

			for (int i = 1; i < tooltip.size(); i++) {
				Either<FormattedText, TooltipComponent> either = tooltip.get(i);
				if(either != null && either.left().isPresent() && isAttributeLine(either.left().get(), attr)) {
					tooltip.remove(i);
					break;
				}
			}
		}
		return onlyInvalid;
	}

	private static TranslatableComponent getMatchingOrSibling(FormattedText component, String key) {
		if (component instanceof TranslatableComponent translate)
			return key.equals(translate.getKey()) ?
					translate : null;

		if(component instanceof Component cmp)
			for (Component sibling : cmp.getSiblings()) {
				if (sibling instanceof TranslatableComponent)
					return getMatchingOrSibling(sibling, key);
			}

		return null;
	}

	private static boolean equalsOrSibling(FormattedText component, String key) {
		return getMatchingOrSibling(component, key) != null;
	}

	private static final ImmutableSet<String> ATTRIBUTE_FORMATS = ImmutableSet.of("plus", "take", "equals");

	@OnlyIn(Dist.CLIENT)
	private static boolean isAttributeLine(FormattedText lineRaw, Attribute attr) {
		String attNamePattern = attr.getDescriptionId();

		for (String att : ATTRIBUTE_FORMATS) {
			for (int mod = 0; mod < 3; mod++) {
				String pattern = "attribute.modifier." + att + "." + mod;
				TranslatableComponent line = getMatchingOrSibling(lineRaw, pattern);
				if (line != null) {
					Object[] formatArgs = line.getArgs();
					if (formatArgs.length > 1) {
						Object formatArg = formatArgs[1];
						if (formatArg instanceof Component &&
								equalsOrSibling((Component) formatArg, attNamePattern))
							return true;
					}
				}
			}
		}

		return false;
	}

	@OnlyIn(Dist.CLIENT)
	private static int renderAttribute(PoseStack matrix, Attribute attribute, EquipmentSlot slot, int x, int y, ItemStack stack, Multimap<Attribute, AttributeModifier> slotAttributes, Minecraft mc) {
		double value = getAttribute(mc.player, slot, stack, slotAttributes, attribute);
		if (value != 0) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, MiscUtil.ATTRIBUTE_ICONS);
			int renderPosition = renderPosition(attribute);
			int renderX = renderPosition % 28;
			int renderY = renderPosition / 28;
			GuiComponent.blit(matrix, x, y, renderX * 9, renderY * 9, 9, 9, 256, 256);

			String valueStr = format(attribute, value, slot);

			int color = value < 0 || (valueStr.endsWith("x") && value / baseValue(attribute) < 1) ? 0xFF5555 : 0xFFFFFF;

			mc.font.drawShadow(matrix, valueStr, x + 12, y + 1, color);
			x += mc.font.width(valueStr) + 20;
		}

		return x;
	}

	private static EquipmentSlot getPrimarySlot(ItemStack stack) {
		if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem)
			return null;
		return Mob.getEquipmentSlotForItem(stack);
	}

	private static boolean canStripAttributes(ItemStack stack, @Nullable EquipmentSlot slot) {
		if (stack.isEmpty())
			return false;

		if (slot == null)
			return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 32) == 0;

		return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 2) == 0;
	}

	private static double getAttribute(Player player, EquipmentSlot slot, ItemStack stack, Multimap<Attribute, AttributeModifier> map, Attribute key) {
		if(player == null) // apparently this can happen
			return 0;

		Collection<AttributeModifier> collection = map.get(key);
		if(collection.isEmpty())
			return 0;

		double value = 0;

		if (!PERCENT_ATTRIBUTES.contains(key)) {
			if (slot != null || !key.equals(ATTACK_DAMAGE)) { // ATTACK_DAMAGE
				AttributeInstance attribute = player.getAttribute(key);
				if (attribute != null)
					value = attribute.getBaseValue();
			}
		}

		for (AttributeModifier modifier : collection) {
			if (modifier.getOperation() == AttributeModifier.Operation.ADDITION)
				value += modifier.getAmount();
		}

		double rawValue = value;

		for (AttributeModifier modifier : collection) {
			if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
				value += rawValue * modifier.getAmount();
		}

		for (AttributeModifier modifier : collection) {
			if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
				value += value * modifier.getAmount();
		}


		if (key.equals(ATTACK_DAMAGE) && slot == EquipmentSlot.MAINHAND)
			value += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
		if (key.equals(ATTACK_KNOCKBACK) && slot == EquipmentSlot.MAINHAND)
			value += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);

		if (DIFFERENCE_ATTRIBUTES.contains(key) || (slot != EquipmentSlot.MAINHAND && NONMAIN_DIFFERENCE_ATTRIBUTES.contains(key))) {
			if (slot != null || !key.equals(ATTACK_DAMAGE)) {
				AttributeInstance attribute = player.getAttribute(key);
				if (attribute != null)
					value -= attribute.getBaseValue();
			}
		}

		return value;
	}


	public record AttributeComponent(ItemStack stack, int width,
									 int height) implements ClientTooltipComponent, TooltipComponent {

		@Override
		public void renderImage(@Nonnull Font font, int tooltipX, int tooltipY, @Nonnull PoseStack pose, @Nonnull ItemRenderer itemRenderer, int something) {
			if (!Screen.hasShiftDown()) {
				pose.pushPose();
				pose.translate(0, 0, 500);

				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				Minecraft mc = Minecraft.getInstance();
				pose.translate(0F, 0F, mc.getItemRenderer().blitOffset);

				int y = tooltipY - 1;

				EquipmentSlot primarySlot = getPrimarySlot(stack);
				boolean onlyInvalid = true;
				boolean showSlots = false;
				int attributeHash = 0;

				boolean allAreSame = true;

				EquipmentSlot[] slots = EquipmentSlot.values();
				slots = Arrays.copyOf(slots, slots.length + 1);

				shouldShow:
				for (EquipmentSlot slot : slots) {
					if (canStripAttributes(stack, slot)) {
						Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);
						if (slot == EquipmentSlot.MAINHAND)
							attributeHash = slotAttributes.hashCode();
						else if (allAreSame && attributeHash != slotAttributes.hashCode())
							allAreSame = false;

						for (Attribute attr : slotAttributes.keys()) {
							if (VALID_ATTRIBUTES.contains(attr)) {
								onlyInvalid = false;
								if (slot != primarySlot) {
									showSlots = true;
									break shouldShow;
								}
							}
						}
					}
				}

				if (allAreSame)
					showSlots = false;
				else if (onlyInvalid)
					showSlots = true;


				for (EquipmentSlot slot : slots) {
					if (canStripAttributes(stack, slot)) {
						int x = tooltipX;

						Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);

						boolean anyToRender = false;
						for (Attribute attr : slotAttributes.keys()) {
							double value = getAttribute(mc.player, slot, stack, slotAttributes, attr);
							if (value != 0) {
								anyToRender = true;
								break;
							}
						}

						if (!anyToRender)
							continue;

						if (showSlots) {
							RenderSystem.setShader(GameRenderer::getPositionTexShader);
							RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
							RenderSystem.setShaderTexture(0, MiscUtil.GENERAL_ICONS);
							GuiComponent.blit(pose, x, y, 202 + (slot == null ? -1 : slot.ordinal()) * 9, 35, 9, 9, 256, 256);
							x += 20;
						}

						for (Attribute key : VALID_ATTRIBUTES)
							x = renderAttribute(pose, key, slot, x, y, stack, slotAttributes, mc);

						for (Attribute key : slotAttributes.keys()) {
							if (!VALID_ATTRIBUTES.contains(key)) {
								mc.font.drawShadow(pose, "[+]", x + 1, y + 1, 0xFFFF55);
								break;
							}
						}


						y += 10;

						if (allAreSame)
							break;
					}
				}

				pose.popPose();
			}
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getWidth(@Nonnull Font font) {
			return width;
		}

	}

}
