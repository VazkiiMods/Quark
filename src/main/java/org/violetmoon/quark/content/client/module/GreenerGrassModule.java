package org.violetmoon.quark.content.client.module;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.config.type.ConvulsionMatrixConfig;
import org.violetmoon.zeta.client.event.play.ZFirstClientTick;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.List;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "client")
public class GreenerGrassModule extends ZetaModule {

	private static final String[] GRASS_PRESET_NAMES = { "Dreary", "Vibrant" };
	private static final String GRASS_NAME = "Grass Colors";
	private static final String[] GRASS_BIOMES = { "plains", "forest", "mountains", "jungle", "savanna", "swamp" };
	private static final int[] GRASS_COLORS = { 0xff91bd59, 0xff79c05a, 0xff8ab689, 0xff59c93c, 0xffbfb755, 0xff6a7039 };
	private static final int[] FOLLIAGE_COLORS = { 0xff77ab2f, 0xff59ae30, 0xff6da36b, 0xff30bb0b, 0xffaea42a, 0xff6a7039 };
	private static final double[][] GRASS_PRESETS = {
			{
					1.24, 0.00, 0.00,
					0.00, 0.84, 0.00,
					0.00, 0.16, 0.36
			},
			{
					1.00, 0.00, 0.00,
					0.24, 1.00, 0.24,
					0.00, 0.00, 0.60
			}
	};
	private static final double[] GRASS_DEFAULT = {
			0.89, 0.00, 0.00,
			0.00, 1.11, 0.00,
			0.00, 0.00, 0.89
	};

	private static final String[] WATER_PRESET_NAMES = { "Muddy", "Colder" };
	private static final String WATER_NAME = "Water Colors";
	private static final String[] WATER_BIOMES = { "generic", "swamp", "meadow", "mangrove", "cold", "warm" };
	private static final int[] WATER_COLORS = { 0xff3f76e4, 0xff617B64, 0xff0e4ecf, 0xff3a7a6a, 0xff3d57D6, 0xff43d5ee };
	private static final double[][] WATER_PRESETS = {
			{
					0.76, 0.00, 0.10,
					0.00, 0.80, 0.00,
					0.00, 0.00, 0.70
			},
			{
					1.00, 0.00, 0.00,
					0.24, 0.96, 0.24,
					0.20, 0.52, 1.00
			}
	};
	private static final double[] WATER_DEFAULT = {
			0.86, 0.00, 0.00,
			0.00, 1.00, 0.22,
			0.00, 0.00, 1.22
	};

	private static final ConvulsionMatrixConfig.Params GRASS_PARAMS = new ConvulsionMatrixConfig.Params(GRASS_NAME, GRASS_DEFAULT, GRASS_BIOMES, GRASS_COLORS, FOLLIAGE_COLORS, GRASS_PRESET_NAMES, GRASS_PRESETS);
	private static final ConvulsionMatrixConfig.Params WATER_PARAMS = new ConvulsionMatrixConfig.Params(WATER_NAME, WATER_DEFAULT, WATER_BIOMES, WATER_COLORS, null, WATER_PRESET_NAMES, WATER_PRESETS);

	@Config
	public static boolean affectLeaves = true;
	@Config
	public static boolean affectWater = false;

	@Config
	public static List<String> blockList = Lists.newArrayList(
			"minecraft:large_fern",
			"minecraft:tall_grass",
			"minecraft:grass_block",
			"minecraft:fern",
			"minecraft:short_grass",
			"minecraft:potted_fern",
			"minecraft:sugar_cane",
			"environmental:giant_tall_grass",
			"grassslabs:grass_carpet",
			"grassslabs:grass_slab",
			"grassslabs:grass_stairs",
			"valhelsia_structures:grass_block");

	@Config
	public static List<String> leavesList = Lists.newArrayList(
			"minecraft:spruce_leaves",
			"minecraft:birch_leaves",
			"minecraft:oak_leaves",
			"minecraft:jungle_leaves",
			"minecraft:acacia_leaves",
			"minecraft:dark_oak_leaves",
			"atmospheric:rosewood_leaves",
			"atmospheric:morado_leaves",
			"atmospheric:yucca_leaves",
			"autumnity:maple_leaves",
			"environmental:willow_leaves",
			"environmental:hanging_willow_leaves",
			"minecraft:vine");

	@Config
	public static ConvulsionMatrixConfig colorMatrix = new ConvulsionMatrixConfig(GRASS_PARAMS);
	@Config
	public static ConvulsionMatrixConfig waterMatrix = new ConvulsionMatrixConfig(WATER_PARAMS);

	public int getWaterColor(int orig) {
		return orig;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends GreenerGrassModule {

		// done late, to give other mods a chance to register their blocks
		@LoadEvent
		public void firstClientTick(ZFirstClientTick event) {
			registerGreenerColor(blockList, BooleanSuppliers.TRUE);
			registerGreenerColor(leavesList, () -> affectLeaves);
		}

		private void registerGreenerColor(Iterable<String> ids, BooleanSupplier condition) {
			BlockColors colors = Minecraft.getInstance().getBlockColors();

			for(String id : ids) {
				Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
				if(block == Blocks.AIR)
					continue;

				BlockColor original = QuarkClient.ZETA_CLIENT.getBlockColor(colors, block);

				if(original != null)
					colors.register(getConvulsedColor(original, condition), block);
			}
		}

		private BlockColor getConvulsedColor(BlockColor color, BooleanSupplier condition) {
			return (state, world, pos, tintIndex) -> {
				int originalColor = color.getColor(state, world, pos, tintIndex);
				if(!isEnabled() || !condition.getAsBoolean())
					return originalColor;

				return colorMatrix.convolve(originalColor);
			};
		}

		@Override
		public int getWaterColor(int currColor) {
			if(!isEnabled() || !affectWater)
				return currColor;

			return waterMatrix.convolve(currColor);
		}

	}

}
