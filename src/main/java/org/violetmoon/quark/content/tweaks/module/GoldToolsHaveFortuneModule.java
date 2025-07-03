package org.violetmoon.quark.content.tweaks.module;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.Config.Max;
import org.violetmoon.zeta.config.Config.Min;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.*;

@ZetaLoadModule(category = "tweaks")
public class GoldToolsHaveFortuneModule extends ZetaModule {

	private static final Tier[] TIERS = new Tier[] {Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE};

	@Config
	@Min(0)
	public static int fortuneLevel = 2;

	@Config
	@Min(0)
	@Max(4)
	public static int harvestLevel = 2;

	@Config
	public static boolean displayBakedEnchantmentsInTooltip = true;
	@Config
	public static boolean italicTooltip = true;

	@Config(description = "Enchantments other than Gold's Fortune/Looting to bake into items. Format is \"item+enchant@level\", such as \"minecraft:stick+minecraft:sharpness@10\".")
	public static List<String> itemEnchantments = Lists.newArrayList();

	public static final Map<Item, Object2IntMap<String>> BUILTIN_ENCHANTMENTS = new HashMap<>();

	@Hint(key = "gold_tool_fortune", content = "fortuneLevel")
	List<Item> gold_tools = Arrays.asList(Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD);
	@Hint(key = "gold_tool_harvest_level", content = "harvestLevel")
	List<Item> gold_tools_2 = gold_tools;

	private static boolean staticEnabled;

	/**
	 * Full module refactor. Config setting needs testing with other enchantments, including modded ones.
	 * @author BrokenKeyboard
	 */

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
		BUILTIN_ENCHANTMENTS.clear();

		for(String configEntry : itemEnchantments) {
			String[] configPair = configEntry.split("\\+");
			if (configPair.length != 2) continue;

			ResourceLocation itemLocation = ResourceLocation.tryParse(configPair[0]);
			if (itemLocation != null) {
				Item item = BuiltInRegistries.ITEM.get(itemLocation);
				String[] enchantment = configPair[1].split("@");
				if (enchantment.length != 2) continue;

				Object2IntMap<String> entry = BUILTIN_ENCHANTMENTS.computeIfAbsent(item, it -> new Object2IntArrayMap<>());
				entry.computeIfAbsent(enchantment[0], ench -> Integer.parseInt(enchantment[1]));
			}
		}

		if (fortuneLevel > 0) {
			for (Item item : BuiltInRegistries.ITEM) {
				if (item instanceof TieredItem tiered && tiered.getTier() == Tiers.GOLD) {
					String enchantment = item instanceof SwordItem ? Enchantments.LOOTING.location().toString() : Enchantments.FORTUNE.location().toString();
					Object2IntMap<String> entry = BUILTIN_ENCHANTMENTS.computeIfAbsent(item, it -> new Object2IntArrayMap<>());
					entry.computeIfAbsent(enchantment, ench -> fortuneLevel);
				}
			}
		}
	}

	public static boolean shouldOverrideCorrectTool(ItemStack stack, BlockState state) {
		if (!staticEnabled || !(stack.getItem() instanceof TieredItem tiered && tiered.getTier() == Tiers.GOLD)) return false;

		Tool tool = stack.get(DataComponents.TOOL);
		if (tool == null) return false;

		for (Tool.Rule rule : tool.rules()) {
			if (rule.correctForDrops().isPresent() && !state.is(TIERS[harvestLevel].getIncorrectBlocksForDrops())) return true;
		}
		return false;
	}

	public static int getActualEnchantmentLevel(Holder<Enchantment> holder, ItemStack stack, int original) {
		if (!staticEnabled) return original;

		if (GoldToolsHaveFortuneModule.BUILTIN_ENCHANTMENTS.containsKey(stack.getItem())) {
			Object2IntMap<String> enchantmentList = BUILTIN_ENCHANTMENTS.get(stack.getItem());

			if (enchantmentList.containsKey(holder.getRegisteredName())) {
				int level = enchantmentList.getOrDefault(holder.getRegisteredName(), 0);
				return Math.max(level, original);
			}
		}
		return original;
	}

	public static ItemStack createTooltipStack(ItemStack stack, DataComponentType<?> componentType, HolderLookup.Provider provider) {
		if (!staticEnabled || !displayBakedEnchantmentsInTooltip || componentType != DataComponents.ENCHANTMENTS) return stack;

		if (GoldToolsHaveFortuneModule.BUILTIN_ENCHANTMENTS.containsKey(stack.getItem())) {
			ItemStack copy = stack.copy();
			Object2IntMap<String> builtInEnchantments = GoldToolsHaveFortuneModule.BUILTIN_ENCHANTMENTS.get(stack.getItem());
			ItemEnchantments itemEnchantments = Optional.ofNullable(copy.get(DataComponents.ENCHANTMENTS)).orElse(ItemEnchantments.EMPTY);
			ItemEnchantments.Mutable newEnchantments = new ItemEnchantments.Mutable(itemEnchantments);

			for (String enchantmentName : builtInEnchantments.keySet()) {
				Holder<Enchantment> holder = provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantmentName)));
				newEnchantments.set(holder, Math.max(newEnchantments.getLevel(holder), builtInEnchantments.getOrDefault(enchantmentName, 0)));
			}

			copy.set(DataComponents.ENCHANTMENTS, newEnchantments.toImmutable());
			return copy;
		}
		return stack;
	}

	public static void modifyTooltip(ItemStack stack, List<Component> list, HolderLookup.Provider provider) {
		if (!displayBakedEnchantmentsInTooltip || !italicTooltip) return;

		if (GoldToolsHaveFortuneModule.BUILTIN_ENCHANTMENTS.containsKey(stack.getItem())) {
			Object2IntMap<String> builtInEnchantments = GoldToolsHaveFortuneModule.BUILTIN_ENCHANTMENTS.get(stack.getItem());

			for (String enchantmentName : builtInEnchantments.keySet()) {
				Holder<Enchantment> holder = provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantmentName)));
				int level = builtInEnchantments.getInt(enchantmentName);
				Component enchantmentEntry = Enchantment.getFullname(holder, level);
				if (list.contains(enchantmentEntry)) {
					int index = list.indexOf(enchantmentEntry);
					list.set(index, Enchantment.getFullname(holder, level).copy().withStyle(ChatFormatting.ITALIC));
				}
			}
		}
	}
}