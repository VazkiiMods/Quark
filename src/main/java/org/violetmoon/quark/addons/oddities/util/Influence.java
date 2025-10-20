package org.violetmoon.quark.addons.oddities.util;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public record Influence(List<Holder<Enchantment>> boost, List<Holder<Enchantment>> dampen) {

}
