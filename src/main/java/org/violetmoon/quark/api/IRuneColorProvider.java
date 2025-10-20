package org.violetmoon.quark.api;

import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.content.tools.base.RuneColor;

/**
 * @author WireSegal
 *         Created at 2:22 PM on 8/17/19.
 */
public interface IRuneColorProvider {
	RuneColor getRuneColor(ItemStack stack);
}
