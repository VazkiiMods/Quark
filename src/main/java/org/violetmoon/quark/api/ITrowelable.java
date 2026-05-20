package org.violetmoon.quark.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Implement this on an Item to allow finer control over whether it can be troweled or not
 * Note that the quark:trowel_blacklist and quark:trowel_whitelist tags WILL have priority over this and WILL override its result
 */
public interface ITrowelable {

    /**
     * @param stack   The stack being troweled
     * @param context The context of the trowel use
     * @return Override for more control over where stuff can be troweled
     */
    default boolean canBeTroweled(ItemStack stack, UseOnContext context) {
        return true;
    }
}
