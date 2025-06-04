package org.violetmoon.quark.content.building.module;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.block.VariantLadderBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZLoadComplete;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;

import java.util.LinkedList;
import java.util.List;

@ZetaLoadModule(category = "building", antiOverlap = { "woodworks", "woodster" })
public class VariantLaddersModule extends ZetaModule {

	@Config
	public static boolean changeNames = true;

	public static List<Block> variantLadders = new LinkedList<>();

	public static boolean moduleEnabled;

	@LoadEvent
	public final void register(ZRegister event) {
		CreativeTabManager.daisyChain();
		for(Wood type : VanillaWoods.NON_OAK)
			variantLadders.add(new VariantLadderBlock(type.name(), this, Block.Properties.ofFullCopy(Blocks.LADDER).sound(type.soundPlanks()), !type.nether()));
		CreativeTabManager.endDaisyChain();
	}

	@LoadEvent
	public void loadComplete(ZLoadComplete e) {
		variantLadders.forEach(Quark.ZETA.fuel::addWood);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		moduleEnabled = isEnabled();
		zeta().nameChanger.changeBlock(Blocks.LADDER, "block.quark.oak_ladder", changeNames && isEnabled());
	}

}
