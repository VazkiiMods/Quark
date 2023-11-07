package vazkii.quark.content.world.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import vazkii.quark.base.block.QuarkGlassBlock;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.zeta.module.ZetaModule;
import vazkii.quark.content.world.module.CorundumModule;
import vazkii.quark.content.world.module.SpiralSpiresModule;

/**
 * @author WireSegal
 * Created at 12:31 PM on 9/19/19.
 */
public class CorundumBlock extends QuarkGlassBlock {

	public final float[] colorComponents;
	public final boolean waxed;

	public CorundumClusterBlock cluster;

	public CorundumBlock(String regname, int color, ZetaModule module, MaterialColor materialColor, boolean waxed) {
		super(regname, module, CreativeModeTab.TAB_DECORATIONS, true,
				Block.Properties.of(Material.GLASS, materialColor)
				.strength(0.3F, 0F)
				.sound(SoundType.AMETHYST)
				.lightLevel(b -> 11)
				.requiresCorrectToolForDrops()
				.randomTicks()
				.noOcclusion());

		float r = ((color >> 16) & 0xff) / 255f;
		float g = ((color >> 8) & 0xff) / 255f;
		float b = (color & 0xff) / 255f;
		colorComponents = new float[]{r, g, b};
		this.waxed = waxed;
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
	public void tick(@Nonnull BlockState state, @Nonnull ServerLevel worldIn, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
		if(canGrow(worldIn, pos) && random.nextInt(CorundumModule.caveCrystalGrowthChance) == 0) {
			BlockState down = worldIn.getBlockState(pos.below());
			BlockPos up = pos.above();
			worldIn.setBlockAndUpdate(up, state);

			if(down.getBlock() == SpiralSpiresModule.myalite_crystal && ModuleLoader.INSTANCE.isModuleEnabled(SpiralSpiresModule.class) && SpiralSpiresModule.renewableMyalite)
				worldIn.setBlockAndUpdate(pos, SpiralSpiresModule.myalite_crystal.defaultBlockState());
			else for(Direction d : Direction.values()) {
				BlockPos offPos = up.relative(d);
				if(worldIn.isEmptyBlock(offPos) && random.nextInt(3) == 0)
					worldIn.setBlockAndUpdate(offPos, cluster.defaultBlockState().setValue(CorundumClusterBlock.FACING, d));
			}
		}
	}

	@Override
	public void animateTick(@Nonnull BlockState stateIn, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull RandomSource rand) {
		if(canGrow(worldIn, pos)) {
			double x = (double)pos.getX() + rand.nextDouble();
			double y = (double)pos.getY() + rand.nextDouble();
			double z = (double)pos.getZ() + rand.nextDouble();

			worldIn.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, x, y, z, colorComponents[0], colorComponents[1], colorComponents[2]);
		}

		if(!waxed)
			for(int i = 0; i < 4; i++) {
				double range = 5;

				double ox = rand.nextDouble() * range - (range / 2);
				double oy = rand.nextDouble() * range - (range / 2);
				double oz = rand.nextDouble() * range - (range / 2);

				double x = (double)pos.getX() + 0.5 + ox;
				double y = (double)pos.getY() + 0.5 + oy;
				double z = (double)pos.getZ() + 0.5 + oz;

				float size = 0.4F + rand.nextFloat() * 0.5F;

				if(rand.nextDouble() < 0.1) {
					double ol = ((ox * ox) + (oy * oy) + (oz * oz)) * -2;
					if(ol == 0)
						ol = 0.0001;
					worldIn.addParticle(ParticleTypes.END_ROD, x, y, z, ox / ol, oy / ol, oz / ol);
				}

				worldIn.addParticle(new DustParticleOptions(new Vector3f(colorComponents[0], colorComponents[1], colorComponents[2]), size), x, y, z, 0, 0, 0);
			}
	}

	@Nullable
	@Override
	public float[] getBeaconColorMultiplierZeta(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
		return colorComponents;
	}

}
