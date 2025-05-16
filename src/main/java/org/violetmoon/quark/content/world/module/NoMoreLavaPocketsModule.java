package org.violetmoon.quark.content.world.module;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "world")
public class NoMoreLavaPocketsModule extends ZetaModule {

	private static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}

	public static boolean shouldDisable(SpringConfiguration configuration) {
		return staticEnabled && !configuration.requiresBlockBelow && configuration.state.is(FluidTags.LAVA);
	}
}
