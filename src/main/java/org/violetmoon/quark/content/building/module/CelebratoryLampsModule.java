package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.violetmoon.quark.content.building.block.CelebratoryLampBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "building")
public class CelebratoryLampsModule extends ZetaModule {

	@Config
	public static int lightLevel = 15;

	public static Block stone_lamp, stone_brick_lamp;

	@LoadEvent
	public final void register(ZRegister event) {
		stone_lamp = new CelebratoryLampBlock("stone_lamp", this, Block.Properties.ofFullCopy(Blocks.STONE).lightLevel(s -> lightLevel));
		stone_brick_lamp = new CelebratoryLampBlock("stone_brick_lamp", this, Block.Properties.ofFullCopy(Blocks.STONE_BRICKS).lightLevel(s -> lightLevel));
	}

}
