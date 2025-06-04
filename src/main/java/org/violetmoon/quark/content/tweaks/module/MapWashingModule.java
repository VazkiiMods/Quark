package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;

import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "tweaks", antiOverlap = "supplementaries")
public class MapWashingModule extends ZetaModule {
	private final CauldronInteraction WASHING_MAP = (state, level, pos, player, hand, stack) -> {
		if (isEnabled()) return ItemInteractionResult.FAIL;

		if(!stack.is(Items.FILLED_MAP)) {
			return ItemInteractionResult.FAIL;
		} else {
			if(!level.isClientSide) {
				player.setItemInHand(hand, new ItemStack(Items.MAP, stack.getCount()));
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
	};

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		CauldronInteraction.WATER.map().put(Items.FILLED_MAP, WASHING_MAP);
	}
}
