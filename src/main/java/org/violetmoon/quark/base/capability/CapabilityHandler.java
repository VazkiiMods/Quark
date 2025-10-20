package org.violetmoon.quark.base.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.violetmoon.quark.api.ICustomSorting;
import org.violetmoon.quark.api.IRuneColorProvider;
import org.violetmoon.quark.api.ITransferManager;
import org.violetmoon.quark.api.QuarkCapabilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.event.bus.PlayEvent;

// TODO: push these event handlers into their respective modules
public class CapabilityHandler {
	private static final ResourceLocation DROPOFF_MANAGER = Quark.asResource("dropoff");
	private static final ResourceLocation SORTING_HANDLER = Quark.asResource("sort");
	private static final ResourceLocation RUNE_COLOR_HANDLER = Quark.asResource("rune_color");

	/*@PlayEvent
	public static void attachItemCapabilities(ZAttachCapabilities.ItemStackCaps event) {
		Item item = event.getObject().getItem();

		if(item instanceof ICustomSorting impl)
			event.addCapability(SORTING_HANDLER, QuarkCapabilities.SORTING, impl);

		if(item instanceof IRuneColorProvider impl)
			event.addCapability(RUNE_COLOR_HANDLER, QuarkCapabilities.RUNE_COLOR, impl);
	}

	@PlayEvent
	public static void attachTileCapabilities(ZAttachCapabilities.BlockEntityCaps event) {
		if(event.getObject() instanceof ITransferManager impl)
			event.addCapability(DROPOFF_MANAGER, QuarkCapabilities.TRANSFER, impl);
	}*/
}
