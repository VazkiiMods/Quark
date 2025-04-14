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

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building", antiOverlap = { "woodworks", "woodster" })
public class VariantBookshelvesModule extends ZetaModule {

	public static List<VariantBookshelfBlock> variantBookshelves = new ArrayList<>();

	@Config
	public static boolean changeNames = true;

	@LoadEvent
	public final void register(ZRegister event) {
		CreativeTabManager.daisyChain();
		for(Wood type : VanillaWoods.NON_OAK){
			VariantBookshelfBlock bookshelf = new VariantBookshelfBlock(type.name(), this, !type.nether(), type.soundPlanks());
			variantBookshelves.add(bookshelf);
		}

		CreativeTabManager.endDaisyChain();
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		zeta().nameChanger.changeBlock(Blocks.BOOKSHELF, "block.quark.oak_bookshelf", changeNames && isEnabled());
	}
}
