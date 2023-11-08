package org.violetmoon.quark.content.world.module;

import org.violetmoon.quark.base.config.Config;
import org.violetmoon.quark.base.handler.VariantHandler;
import org.violetmoon.quark.base.world.WorldGenHandler;
import org.violetmoon.quark.base.world.WorldGenWeights;
import org.violetmoon.quark.content.world.block.ChorusVegetationBlock;
import org.violetmoon.quark.content.world.gen.ChorusVegetationGenerator;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import com.google.common.base.Functions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;

@ZetaLoadModule(category = "world")
public class ChorusVegetationModule extends ZetaModule {
	@Config public static int rarity = 150;
	@Config public static int radius = 7;
	@Config public static int chunkAttempts = 120;
	@Config public static double highlandsChance = 1.0;
	@Config public static double midlandsChance = 0.2;
	@Config public static double otherEndBiomesChance = 0;
	
	@Config public static double passiveTeleportChance = 0.2;
	@Config public static double endermiteSpawnChance = 0.01;
	@Config public static double teleportDuplicationChance = 0.01;

	@Hint public static Block chorus_weeds;
	@Hint(key = "chorus_weeds") public static Block chorus_twist;
	
	@LoadEvent
	public final void register(ZRegister event) {
		chorus_weeds = new ChorusVegetationBlock("chorus_weeds", this, true);
		chorus_twist = new ChorusVegetationBlock("chorus_twist", this, false);
		
		VariantHandler.addFlowerPot(chorus_weeds, "chorus_weeds", Functions.identity());
		VariantHandler.addFlowerPot(chorus_twist, "chorus_twist", Functions.identity());
	}
	
	@LoadEvent
	public final void setup(ZCommonSetup event) {
		WorldGenHandler.addGenerator(this, new ChorusVegetationGenerator(), Decoration.VEGETAL_DECORATION, WorldGenWeights.CHORUS_VEGETATION);
	}
}
