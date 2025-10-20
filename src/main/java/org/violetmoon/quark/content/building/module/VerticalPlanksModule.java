package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;

import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class VerticalPlanksModule extends ZetaModule {
	public static List<Block> blocks = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
		for(Wood type : VanillaWoods.ALL)
			add(type.name(), type.planks(), this);
	}

	public static ZetaBlock add(String name, Block base, ZetaModule module) {
		ZetaBlock block = (ZetaBlock) new ZetaBlock("vertical_" + name + "_planks", module,
				BlockPropertyUtil.copyPropertySafe(base)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, base, false);
		blocks.add(block.getBlock());
		return block;
	}

}
