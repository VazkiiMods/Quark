package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.violetmoon.quark.mixin.mixins.ArmorStandMixin;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

/**
 * @author WireSegal
 *         Created at 8:40 AM on 8/27/19.
 *
 *         modified by quat
 * @see ArmorStandMixin
 */
@ZetaLoadModule(category = "tweaks")
public class ArmedArmorStandsModule extends ZetaModule {

	@Hint
	Item armor_stand = Items.ARMOR_STAND;

	public static boolean staticEnabled = true;

	@LoadEvent
	public void configChange(ZConfigChanged e) {
		staticEnabled = enabled;
	}

}
