package org.violetmoon.quark.content.building.module;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.CorundumColor;
import org.violetmoon.quark.content.building.block.RainbowLampBlock;
import org.violetmoon.quark.content.world.module.CorundumModule;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZetaLoadModule(category = "building")
public class RainbowLampsModule extends ZetaModule {

	public static List<RainbowLampBlock> lamps = new ArrayList<>();

	public static Map<RainbowLampBlock, CorundumColor> lampMap = new HashMap<>();

	@Config
	public static int lightLevel = 15;

	@Config(description = "Whether Rainbow Lamps should be made from and themed on Corundum if that module is enabled.", flag = "rainbow_lamp_corundum")
	public static boolean useCorundum = true;

	@Hint("crystal_lamp")
	public static TagKey<Block> lampTag;

	public static boolean isCorundum() {
		return CorundumModule.staticEnabled && useCorundum;
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		lampTag = Quark.asTagKey(Registries.BLOCK, "crystal_lamp");
	}

	@LoadEvent
	public final void register(ZRegister event) {
		for(CorundumColor color : CorundumColor.values()){
			RainbowLampBlock lamp = new RainbowLampBlock(color.name + "_crystal_lamp", color.beaconColor, this, color.mapColor);
			lamps.add(lamp);
			lampMap.put(lamp, color);
		}

	}
}
