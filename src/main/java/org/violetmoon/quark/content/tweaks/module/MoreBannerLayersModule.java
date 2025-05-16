package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class MoreBannerLayersModule extends ZetaModule {

	@Config
	@Config.Min(1)
	@Config.Max(16)
	public static int layerLimit = 16;

	@Hint(key = "banner_layer_buff", content = "layerLimit")
	public static final TagKey<Item> banners = ItemTags.BANNERS;

	private static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}

	public static int getLimit(int curr) {
		return staticEnabled ? layerLimit : curr;
	}

}
