package org.violetmoon.quark.content.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.world.module.CorundumModule;
import org.violetmoon.quark.content.world.module.SpiralSpiresModule;
import org.violetmoon.zeta.block.ZetaGlassBlock;
import org.violetmoon.zeta.module.ZetaModule;

/**
 * @author WireSegal
 *         Created at 12:31 PM on 9/19/19.
 */
public class CorundumBlock extends ZetaGlassBlock {

	public final int color;
	public final boolean waxed;

	public CorundumClusterBlock cluster;

	public CorundumBlock(String regname, int color, @Nullable ZetaModule module, MapColor mapColor, boolean waxed) {
		super(regname, module, true,
				Block.Properties.of()
						.mapColor(mapColor)
						.strength(0.3F, 0F)
						.sound(SoundType.AMETHYST)
						.lightLevel(b -> 11)
						.requiresCorrectToolForDrops()
						.randomTicks()
						.noOcclusion());

		this.color = color;
		this.waxed = waxed;

		if(module == null) //auto registration below this line
			return;

		setCreativeTab(CreativeModeTabs.COLORED_BLOCKS);
	}

	private boolean canGrow(Level world, BlockPos pos) {
		if(!waxed && CorundumModule.caveCrystalGrowthChance >= 1 && pos.getY() < 24 && world.isEmptyBlock(pos.above())) {
			int i;
			for(i = 1; world.getBlockState(pos.below(i)).getBlock() == this; ++i);

			return i < 4;
		}
		return false;
	}

	@Override
	protected void randomTick(@NotNull BlockState state, @NotNull ServerLevel worldIn, @NotNull BlockPos pos, @NotNull RandomSource random) {
		if(canGrow(worldIn, pos) && random.nextInt(CorundumModule.caveCrystalGrowthChance) == 0) {
			BlockState down = worldIn.getBlockState(pos.below());
			BlockPos up = pos.above();
			worldIn.setBlockAndUpdate(up, state);

			if(down.getBlock() == SpiralSpiresModule.myalite_crystal && Quark.ZETA.modules.isEnabled(SpiralSpiresModule.class) && SpiralSpiresModule.renewableMyalite)
				worldIn.setBlockAndUpdate(pos, SpiralSpiresModule.myalite_crystal.defaultBlockState());
			else
				for(Direction d : Direction.values()) {
					BlockPos offPos = up.relative(d);
					if(worldIn.isEmptyBlock(offPos) && random.nextInt(3) == 0)
						worldIn.setBlockAndUpdate(offPos, cluster.defaultBlockState().setValue(CorundumClusterBlock.FACING, d));
				}
		}
	}

	@Override
	public void animateTick(@NotNull BlockState stateIn, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull RandomSource rand) {
        float red = ((color >> 16) & 255)/255f;
        float green = ((color >> 8) & 255) / 255f;
        float blue = (color & 255) / 255f;
		if(canGrow(worldIn, pos)) {
			double x = (double) pos.getX() + rand.nextDouble();
			double y = (double) pos.getY() + rand.nextDouble();
			double z = (double) pos.getZ() + rand.nextDouble();

			//Unsure if this is correct behavior.
			worldIn.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, red, green, blue), x, y, z, rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
		}

		if(!waxed)
			for(int i = 0; i < 4; i++) {
				double range = 5;

				double ox = rand.nextDouble() * range - (range / 2);
				double oy = rand.nextDouble() * range - (range / 2);
				double oz = rand.nextDouble() * range - (range / 2);

				double x = (double) pos.getX() + 0.5 + ox;
				double y = (double) pos.getY() + 0.5 + oy;
				double z = (double) pos.getZ() + 0.5 + oz;

				float size = 0.4F + rand.nextFloat() * 0.5F;

				if(rand.nextDouble() < 0.1) {
					double ol = ((ox * ox) + (oy * oy) + (oz * oz)) * -2;
					if(ol == 0)
						ol = 0.0001;
                    worldIn.addParticle(ParticleTypes.END_ROD, x, y, z, ox / ol, oy / ol, oz / ol);
				}

				worldIn.addParticle(new DustParticleOptions(new Vector3f(red, green, blue), size), x, y, z, 0, 0, 0);
			}
	}

	@Nullable
	@Override
	public Integer getBeaconColorMultiplierZeta(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
		return color;
	}
}
