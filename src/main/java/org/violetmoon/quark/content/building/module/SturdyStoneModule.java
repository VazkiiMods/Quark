package org.violetmoon.quark.content.building.module;

import net.minecraft.world.level.block.Block;

import org.violetmoon.quark.content.building.block.SturdyStoneBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "building")
public class SturdyStoneModule extends ZetaModule {

	@Hint
	public static Block sturdy_stone;

	@LoadEvent
	public final void register(ZRegister event) {
		sturdy_stone = new SturdyStoneBlock(this);
	}

}
