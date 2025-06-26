package org.violetmoon.quark.addons.oddities.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.violetmoon.quark.base.Quark;

import java.util.ArrayList;
import java.util.List;

/***
 * Due to the Enchantments being datapack'd, we need something that can act as a temporary place for influenced enchants before the world exists.
 * @param boost Boosted Enchants
 * @param dampen Dampened Enchants
 */
public record InfluenceLocations(List<ResourceLocation> boost, List<ResourceLocation> dampen) {
    public Influence toInfluence() {
        List<Holder<Enchantment>> boostedEnchantments = new ArrayList<>();
        List<Holder<Enchantment>> dampenedEnchantments = new ArrayList<>();

        for (ResourceLocation location : boost) {
            Holder<Enchantment> ench = Holder.direct(Quark.proxy.hackilyGetCurrentClientLevelRegistryAccess().registry(Registries.ENCHANTMENT).get().get(location));
            if (ench != null) {
                boostedEnchantments.add(ench);
            } else {
                Quark.LOG.error("Matrix Enchanting Influencing: Enchantment " + location.toString() + " does not exist!");
            }
        }

        for (ResourceLocation location : dampen) {
            Holder<Enchantment> ench = Holder.direct(Quark.proxy.hackilyGetCurrentClientLevelRegistryAccess().registry(Registries.ENCHANTMENT).get().get(location));
            if (ench != null) {
                dampenedEnchantments.add(ench);
            } else {
                Quark.LOG.error("Matrix Enchanting Influencing: Enchantment " + location.toString() + " does not exist!");
            }
        }

        return new Influence(boostedEnchantments, dampenedEnchantments);
    }
}
