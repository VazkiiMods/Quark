package org.violetmoon.quark.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Implement this on an Item to change its behavior with the quark usage ticker.
 */
public interface IUsageTickerOverride {

	default int getUsageTickerCountForItem(ItemStack stack, Predicate<ItemStack> target) {
		return 0;
	}

	default boolean shouldUsageTickerCheckMatchSize(ItemStack stack) {
		return false;
	}

	ItemStack getUsageTickerItem(ItemStack stack, RegistryAccess access);

}
