package org.violetmoon.quark.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Implement on a Block to make it influence matrix enchanting
 */
public interface IEnchantmentInfluencer {
	@Nullable
	int getEnchantmentInfluenceColor(BlockGetter world, BlockPos pos, BlockState state);

	default int getInfluenceStack(BlockGetter world, BlockPos pos, BlockState state) {
		return 1;
	}

	@Nullable
	default ParticleOptions getExtraParticleOptions(BlockGetter world, BlockPos pos, BlockState state) {
		return null;
	}

	default double getExtraParticleChance(BlockGetter world, BlockPos pos, BlockState state) {
		return 1;
	}

	boolean influencesEnchantment(BlockGetter world, BlockPos pos, BlockState state, Holder<Enchantment> enchantment);

	boolean dampensEnchantment(BlockGetter world, BlockPos pos, BlockState state, Holder<Enchantment> enchantment);
}
