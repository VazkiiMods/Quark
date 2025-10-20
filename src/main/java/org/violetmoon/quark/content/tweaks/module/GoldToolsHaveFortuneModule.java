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

	public static final Map<Item, Object2IntMap<ResourceKey<Enchantment>>> BUILTIN_ENCHANTMENTS = new HashMap<>();

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

				ResourceKey<Enchantment> enchantmentKey = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantment[0]));
				Object2IntMap<ResourceKey<Enchantment>> entry = BUILTIN_ENCHANTMENTS.computeIfAbsent(item, it -> new Object2IntArrayMap<>());
				entry.computeIfAbsent(enchantmentKey, ench -> Integer.parseInt(enchantment[1]));
			}
		}

		if (fortuneLevel > 0) {
			for (Item item : BuiltInRegistries.ITEM) {
				if (item instanceof TieredItem tiered && tiered.getTier() == Tiers.GOLD) {
					Object2IntMap<ResourceKey<Enchantment>> entry = BUILTIN_ENCHANTMENTS.computeIfAbsent(item, it -> new Object2IntArrayMap<>());
					entry.computeIfAbsent(item instanceof SwordItem ? Enchantments.LOOTING : Enchantments.FORTUNE, ench -> fortuneLevel);
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

		if (BUILTIN_ENCHANTMENTS.containsKey(stack.getItem())) {
			Object2IntMap<ResourceKey<Enchantment>> enchantmentList = BUILTIN_ENCHANTMENTS.get(stack.getItem());

			if (enchantmentList.containsKey(holder.getKey())) {
				int level = enchantmentList.getOrDefault(holder.getKey(), 0);
				return Math.max(level, original);
			}
		}
		return original;
	}

	public static ItemStack createTooltipStack(ItemStack stack, DataComponentType<?> componentType, HolderLookup.Provider provider) {
		if (!staticEnabled || !displayBakedEnchantmentsInTooltip || componentType != DataComponents.ENCHANTMENTS) return stack;

		if (BUILTIN_ENCHANTMENTS.containsKey(stack.getItem())) {
			ItemStack copy = stack.copy();
			Object2IntMap<ResourceKey<Enchantment>> builtInEnchantments = BUILTIN_ENCHANTMENTS.get(stack.getItem());
			ItemEnchantments itemEnchantments = Optional.ofNullable(copy.get(DataComponents.ENCHANTMENTS)).orElse(ItemEnchantments.EMPTY);
			ItemEnchantments.Mutable newEnchantments = new ItemEnchantments.Mutable(itemEnchantments);

			for (ResourceKey<Enchantment> enchantmentKey : builtInEnchantments.keySet()) {
				Holder<Enchantment> holder = provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey);
				newEnchantments.set(holder, Math.max(newEnchantments.getLevel(holder), builtInEnchantments.getOrDefault(enchantmentKey, 0)));
			}

			copy.set(DataComponents.ENCHANTMENTS, newEnchantments.toImmutable());
			return copy;
		}
		return stack;
	}

	public static void modifyTooltip(ItemStack stack, List<Component> list, HolderLookup.Provider provider) {
		if (!displayBakedEnchantmentsInTooltip || !italicTooltip) return;

		if (BUILTIN_ENCHANTMENTS.containsKey(stack.getItem())) {
			Object2IntMap<ResourceKey<Enchantment>> builtInEnchantments = BUILTIN_ENCHANTMENTS.get(stack.getItem());

			for (ResourceKey<Enchantment> enchantmentKey : builtInEnchantments.keySet()) {
				Holder<Enchantment> holder = provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey);
				int level = builtInEnchantments.getInt(enchantmentKey);
				Component enchantmentEntry = Enchantment.getFullname(holder, level);
				if (list.contains(enchantmentEntry)) {
					int index = list.indexOf(enchantmentEntry);
					list.set(index, Enchantment.getFullname(holder, level).copy().withStyle(ChatFormatting.ITALIC));
				}
			}
		}
	}
}