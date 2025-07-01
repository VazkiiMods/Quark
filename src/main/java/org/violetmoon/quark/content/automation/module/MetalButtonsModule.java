package org.violetmoon.quark.content.automation.module;

import net.minecraft.world.level.block.Block;

import org.violetmoon.quark.content.automation.block.MetalButtonBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "automation")
public class MetalButtonsModule extends ZetaModule {

	@Config(flag = "iron_metal_button")
	public static boolean enableIron = true;
	@Config(flag = "gold_metal_button")
	public static boolean enableGold = true;

	@Hint("iron_metal_button")
	public static Block iron_button;
	@Hint("gold_metal_button")
	public static Block gold_button;

	@LoadEvent
	public final void register(ZRegister event) {
		iron_button = new MetalButtonBlock("iron_button", this, 100).setCondition(() -> enableIron);
		gold_button = new MetalButtonBlock("gold_button", this, 4).setCondition(() -> enableGold);
	}

}
