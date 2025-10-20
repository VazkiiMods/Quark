package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.zeta.block.ext.CustomWeatheringCopper;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.Optional;

public class WeatheringCopperVerticalSlabBlock extends QuarkVerticalSlabBlock implements CustomWeatheringCopper {
	private final WeatheringCopper.WeatherState weatherState;
	public WeatheringCopperVerticalSlabBlock first;
	public WeatheringCopperVerticalSlabBlock prev;
	public WeatheringCopperVerticalSlabBlock next;

	public WeatheringCopperVerticalSlabBlock(Block parent, ZetaModule module) {
		super(parent, module);
		weatherState = ((WeatheringCopper) parent).getAge();
	}

	@Override
	public void randomTick(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random) {
		this.changeOverTime(state, world, pos, random);
	}

	@Override
	public boolean isRandomlyTicking(@NotNull BlockState state) {
		return getNext(state).isPresent();
	}

	@NotNull
	@Override
	public Optional<BlockState> getNext(@NotNull BlockState state) {
		return next == null ? Optional.empty() : Optional.of(next.withPropertiesOf(state));
	}

	@NotNull
	@Override
	public Optional<BlockState> getPrevious(@NotNull BlockState state) {
		return prev == null ? Optional.empty() : Optional.of(prev.withPropertiesOf(state));
	}

	@NotNull
	@Override
	public BlockState getFirst(@NotNull BlockState state) {
		return first.withPropertiesOf(state);
	}

	@NotNull
	@Override
	public WeatheringCopper.WeatherState getAge() {
		return weatherState;
	}

}
