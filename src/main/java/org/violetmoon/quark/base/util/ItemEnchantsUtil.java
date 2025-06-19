package org.violetmoon.quark.base.util;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ItemEnchantsUtil {
    /**
     * Adds a given enchantment to an ItemEnchantments object.
     * @param itemEnchant The ItemEnchantments object that you want to be modified.
     * @param enchantment The Holder that contains the enchantment you are needing.
     * @param lvl The level of the enchantment.
     * @return Returns an ItemEnchantments object that contains the old enchantments alongside this one. You must reassign the variable for it as well.
     */
    public static ItemEnchantments addEnchantmentToList(ItemEnchantments itemEnchant, Holder<Enchantment> enchantment, int lvl) {
        ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(itemEnchant);
        mutableEnchants.set(enchantment, lvl);
        return mutableEnchants.toImmutable();
    }
}
