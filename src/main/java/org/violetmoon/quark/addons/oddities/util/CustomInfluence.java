package org.violetmoon.quark.addons.oddities.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.violetmoon.quark.api.IEnchantmentInfluencer;

public record CustomInfluence(int strength, int color, Influence influence) implements IEnchantmentInfluencer {
	@Override
	public int getEnchantmentInfluenceColor(BlockGetter world, BlockPos pos, BlockState state) {
		int r = FastColor.ARGB32.red(color) << 16;
		int g = FastColor.ARGB32.green(color) << 8;
		int b = FastColor.ARGB32.blue(color);
		return r+g+b;
	}

	@Override
	public int getInfluenceStack(BlockGetter world, BlockPos pos, BlockState state) {
		return strength;
	}

	@Override
	public boolean influencesEnchantment(BlockGetter world, BlockPos pos, BlockState state, Holder<Enchantment> enchantment) {
		return influence.boost().contains(enchantment);
	}

	@Override
	public boolean dampensEnchantment(BlockGetter world, BlockPos pos, BlockState state, Holder<Enchantment> enchantment) {
		return influence.dampen().contains(enchantment);
	}
}
