package org.violetmoon.quark.addons.oddities.inventory.slot;

import net.minecraft.world.Container;

@Deprecated(forRemoval = true)
public class SlotCachingItemHandler extends CachedItemHandlerSlot {
	public SlotCachingItemHandler(Container container, int index, int xPosition, int yPosition) {
		super(container, index, xPosition, yPosition);
	}
}
