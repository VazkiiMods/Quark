package org.violetmoon.quark.content.client.module;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "client")
public class WoolShutsUpMinecartsModule extends ZetaModule {

	private static boolean staticEnabled;

	@Hint(key = "wool_muffling")
	TagKey<Item> dampeners = ItemTags.DAMPENS_VIBRATIONS;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}

	public static boolean canPlay(AbstractMinecart cart) {
		if (!staticEnabled)
			return true;

		AABB bounds = cart.getBoundingBox();
		bounds = bounds.move(0, bounds.minY - bounds.maxY, 0);
		return cart.level().getBlockStates(bounds).noneMatch((it) -> it.is(BlockTags.DAMPENS_VIBRATIONS));
	}

}
