package org.violetmoon.quark.content.building.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.common.ItemAbilities;
import org.apache.commons.lang3.tuple.Pair;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.block.QuarkVerticalSlabBlock;
import org.violetmoon.quark.content.building.block.WeatheringCopperVerticalSlabBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.handler.ToolInteractionHandler;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

@ZetaLoadModule(category = "building")
public class VerticalSlabsModule extends ZetaModule {
	public static List<Block> blocks = new ArrayList<>();

	@Config(description = "Should Walls and Panes attempt to connect to the side of Vertical Slabs?")
	public static boolean allowSideConnections = true;

	public static boolean staticEnabled;

	public static TagKey<Block> verticalSlabTag;

	@LoadEvent
	public void postRegister(ZRegister.Post e) {
		ImmutableSet.of(
				// Old
				Blocks.ACACIA_SLAB, Blocks.ANDESITE_SLAB, Blocks.BIRCH_SLAB, Blocks.BRICK_SLAB, Blocks.COBBLESTONE_SLAB,
				Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE_SLAB, Blocks.DARK_OAK_SLAB, Blocks.DARK_PRISMARINE_SLAB, Blocks.DIORITE_SLAB,
				Blocks.END_STONE_BRICK_SLAB, Blocks.GRANITE_SLAB, Blocks.JUNGLE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB,
				Blocks.NETHER_BRICK_SLAB, Blocks.OAK_SLAB, Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_GRANITE_SLAB,
				Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE_BRICK_SLAB, Blocks.PURPUR_SLAB, Blocks.QUARTZ_SLAB, Blocks.RED_NETHER_BRICK_SLAB,
				Blocks.RED_SANDSTONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB,
				Blocks.SMOOTH_STONE_SLAB, Blocks.SPRUCE_SLAB, Blocks.STONE_SLAB, Blocks.STONE_BRICK_SLAB,

				// 1.16
				Blocks.BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.CRIMSON_SLAB, Blocks.WARPED_SLAB,

				// 1.18
				Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_TILE_SLAB,

				// 1.19
				Blocks.MANGROVE_SLAB, Blocks.MUD_BRICK_SLAB,

				// 1.20
				Blocks.CHERRY_SLAB, Blocks.BAMBOO_SLAB, Blocks.BAMBOO_MOSAIC_SLAB
		)
				.forEach(b -> new QuarkVerticalSlabBlock(b, this));

		List<WeatheringCopperVerticalSlabBlock> copperVerticalSlabs = new ArrayList<>();
		ImmutableSet.of(
				Pair.of(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB),
				Pair.of(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB),
				Pair.of(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB),
				Pair.of(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB))
				.forEach(p -> {
					WeatheringCopperVerticalSlabBlock cleanSlab = new WeatheringCopperVerticalSlabBlock(p.getLeft(), this);
					QuarkVerticalSlabBlock waxedSlab = new QuarkVerticalSlabBlock(p.getRight(), this);

					copperVerticalSlabs.add(cleanSlab);
					ToolInteractionHandler.registerWaxedBlock(this, cleanSlab, waxedSlab);
				});

		WeatheringCopperVerticalSlabBlock first = copperVerticalSlabs.get(0);

		int max = copperVerticalSlabs.size();
		for(int i = 0; i < max; i++) {
			WeatheringCopperVerticalSlabBlock prev = i > 0 ? copperVerticalSlabs.get(i - 1) : null;
			WeatheringCopperVerticalSlabBlock current = copperVerticalSlabs.get(i);
			WeatheringCopperVerticalSlabBlock next = i < max - 1 ? copperVerticalSlabs.get(i + 1) : null;
			if(prev != null) {
				ToolInteractionHandler.registerInteraction(ItemAbilities.AXE_SCRAPE, current, prev);
				current.prev = prev;
			}
			if(next != null)
				current.next = next;
			current.first = first;
		}

		Quark.ZETA.variantRegistry.slabs.forEach(b -> {
			if(b instanceof IVerticalSlabProvider provider)
				provider.getVerticalSlab(b, this);
			else
				new QuarkVerticalSlabBlock(b, this);
		});
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		verticalSlabTag = Quark.asTagKey(Registries.BLOCK, "vertical_slabs");
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	public static BlockState messWithPaneState(LevelAccessor level, BlockPos ourPos, BlockState state) {
		if(!staticEnabled || !allowSideConnections)
			return state;

		for(Direction dir : PipeBlock.PROPERTY_BY_DIRECTION.keySet()) {
			if(dir.getAxis().isHorizontal()) {
				BooleanProperty prop = PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
				boolean val = state.getValue(prop);
				if(!val) {
					BlockState adjState = level.getBlockState(ourPos.relative(dir));
					boolean should = shouldWallConnect(adjState, dir, false);

					if(should)
						state = state.setValue(prop, true);
				}
			}
		}

		return state;
	}

	public static boolean shouldWallConnect(BlockState state, Direction dir, boolean prev) {
		if(prev || !staticEnabled || !allowSideConnections)
			return prev;

		if(state.is(verticalSlabTag)) {
			// We use this absolute nonsene instead of a sane check to support mods that copy the VerticalSlabBlock
			// class. If we just checked against VerticalSlabBlock.TYPE, it would type error when another mod
			// has a copy paste enum in there

			Optional<Property<?>> opt = state.getProperties().stream().filter(p -> p.getName() == "type").findFirst();
			if(opt.isPresent()) {
				Property<?> prop = opt.get();

				if(prop instanceof EnumProperty<?> ep) {
					Enum<?> val = (Enum<?>) state.getValue(prop);

					String name = val.name().toLowerCase();
					Direction vsDir = Direction.byName(name);
					return vsDir != null && vsDir.getAxis() != dir.getAxis();
				}
			}
		}

		return false;
	}

	public interface IVerticalSlabProvider {
		QuarkVerticalSlabBlock getVerticalSlab(Block block, ZetaModule module);

	}

}
