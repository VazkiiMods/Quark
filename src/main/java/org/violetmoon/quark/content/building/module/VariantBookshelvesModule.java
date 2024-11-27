package org.violetmoon.quark.content.building.module;

import net.minecraft.world.level.block.Blocks;

import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;
import org.violetmoon.quark.content.building.block.VariantBookshelfBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

@ZetaLoadModule(category = "building", antiOverlap = { "woodworks", "woodster" })
public class VariantBookshelvesModule extends ZetaModule {

	@Config
	public static boolean changeNames = true;

	@LoadEvent
	public final void register(ZRegister event) {
		CreativeTabManager.daisyChain();
		for(Wood type : VanillaWoods.NON_OAK)
			new VariantBookshelfBlock(type.name(), this, !type.nether(), type.soundPlanks());
		CreativeTabManager.endDaisyChain();
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		zeta().nameChanger.changeBlock(Blocks.BOOKSHELF, "block.quark.oak_bookshelf", changeNames && enabled);
	}
}
