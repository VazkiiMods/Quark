package org.violetmoon.quark.content.experimental.module;

import com.google.common.collect.Lists;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.ZAnvilUpdate;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class EnchantmentsBegoneModule extends ZetaModule {

	@Config
	public static List<String> enchantmentsToBegone = Lists.newArrayList();

	private static boolean staticEnabled;

	private static final List<Holder<Enchantment>> enchantments = Lists.newArrayList();

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();

		enchantments.clear();

		for(String enchantKey : enchantmentsToBegone) {
			/*Holder<Enchantment> enchantment = BuiltInRegistries.ENCHANTMENT.get(ResourceLocation.parse(enchantKey));
			if(enchantment != null)
				enchantments.add(enchantment);*/
		}
	}

	@PlayEvent
	public void stripAnvilEnchantments(ZAnvilUpdate.Lowest event) {
		event.setOutput(begoneEnchantmentsFromItem(event.getOutput()));
	}

	public static void begoneItems(NonNullList<ItemStack> stacks) {
		if(!staticEnabled)
			return;

		stacks.removeIf((it) -> {
			if(it.is(Items.ENCHANTED_BOOK)) {
				ItemEnchantments enchants = it.get(DataComponents.ENCHANTMENTS);
				for(Holder<Enchantment> key : enchants.keySet()) {
					if(enchantments.contains(key)) {
						return true;
					}
				}
			}
			return false;
		});
	}

	public static boolean shouldBegone(ResourceKey<Enchantment> resourceKey) {
		List<ResourceKey<Enchantment>> evilAcursedList = new ArrayList<>();
		for (Holder<Enchantment> enchantHeld : enchantments) {
			evilAcursedList.add(enchantHeld.unwrapKey().get());
		}
		return staticEnabled && evilAcursedList.contains(resourceKey);
	}

	public static boolean shouldBegone(Holder<Enchantment> enchantment) {
		return staticEnabled && enchantments.contains(enchantment);
	}

	public static List<Enchantment> begoneEnchantments(List<Enchantment> list) {
		if(!staticEnabled)
			return list;

		return list.stream().filter(Predicate.not(enchantments::contains)).collect(Collectors.toList());
	}

	public static ItemStack begoneEnchantmentsFromItem(ItemStack stack) {
		if(!staticEnabled || stack.isEmpty())
			return stack;

		ItemEnchantments map = EnchantmentHelper.getEnchantmentsForCrafting(stack);
		if(map.keySet().removeIf(enchantments::contains)) {
			ItemStack out = stack.copy();
			EnchantmentHelper.setEnchantments(out, map);
			return out;
		}

		return stack;
	}

	public static List<EnchantmentInstance> begoneEnchantmentInstances(List<EnchantmentInstance> list) {
		if(!staticEnabled)
			return list;

		return list.stream().filter(it -> !enchantments.contains(it.enchantment)).collect(Collectors.toList());
	}
}
