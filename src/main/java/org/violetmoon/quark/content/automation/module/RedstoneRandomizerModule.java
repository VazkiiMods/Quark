package org.violetmoon.quark.content.automation.module;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;

import org.violetmoon.quark.content.automation.block.RedstoneRandomizerBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

/**
 * @author WireSegal
 *         Created at 10:34 AM on 8/26/19.
 */
@ZetaLoadModule(category = "automation")
public class RedstoneRandomizerModule extends ZetaModule {

	@Hint
	public static Block redstone_randomizer;

	@LoadEvent
	public final void register(ZRegister event) {
		redstone_randomizer = new RedstoneRandomizerBlock("redstone_randomizer", this, Block.Properties.of().pushReaction(PushReaction.DESTROY).strength(0).sound(SoundType.WOOD));
	}
}
