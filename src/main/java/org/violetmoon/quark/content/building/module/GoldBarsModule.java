package org.violetmoon.quark.content.building.module;

import org.violetmoon.zeta.block.ZetaPaneBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.handler.StructureBlockReplacementHandler;
import org.violetmoon.zeta.util.handler.StructureBlockReplacementHandler.StructureHolder;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

@ZetaLoadModule(category = "building")
public class GoldBarsModule extends ZetaModule {

	@Config
	public static boolean generateInNetherFortress = true;

	public static boolean staticEnabled;

	public static Block gold_bars;

	@LoadEvent
	public final void register(ZRegister event) {
		gold_bars = new ZetaPaneBlock("gold_bars", this, Properties.ofFullCopy(Blocks.IRON_BARS), RenderLayerRegistry.Layer.CUTOUT).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.GOLD_BLOCK, false);

		StructureBlockReplacementHandler.addReplacement(GoldBarsModule::getGenerationBarBlockState);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	private static BlockState getGenerationBarBlockState(ServerLevelAccessor accessor, BlockState current, StructureHolder structure) {
		if(staticEnabled && generateInNetherFortress
				&& current.getBlock() == Blocks.NETHER_BRICK_FENCE
				&& StructureBlockReplacementHandler.isStructure(accessor, structure, BuiltinStructures.FORTRESS)) {

			return gold_bars.withPropertiesOf(current);
		}

		return null; // no change
	}

}
