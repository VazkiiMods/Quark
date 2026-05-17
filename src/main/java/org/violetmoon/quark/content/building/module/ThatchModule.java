package org.violetmoon.quark.content.building.module;

import org.violetmoon.quark.base.util.CompostManager;
import org.violetmoon.quark.content.building.block.ThatchBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "building", antiOverlap = { "goated", "environmental" })
public class ThatchModule extends ZetaModule {

	@Config.Min(0)
	@Config.Max(1)
	@Config
	public static double fallDamageMultiplier = 0.5;

	public static ThatchBlock thatch;

	@LoadEvent
	public final void register(ZRegister event) {
		thatch = new ThatchBlock(this);
		event.getVariantRegistry().addSlabAndStairs(thatch, null);
	}

	@LoadEvent
	public void setup(ZCommonSetup zCommonSetup){
		if(this.isEnabled()){
			CompostManager.addChance(thatch.asItem(), 0.65F);
		}
	}

}
