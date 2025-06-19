package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.content.tweaks.client.item.ClockTimePropertyFunction;
import org.violetmoon.quark.content.tweaks.client.item.CompassAnglePropertyFunction;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class CompassesWorkEverywhereModule extends ZetaModule {

	@Config
	public static boolean enableCompassNerf = true;
	@Config(flag = "clock_nerf")
	public static boolean enableClockNerf = true;

	@Config
	public static boolean enableNether = true;
	@Config
	public static boolean enableEnd = true;

	@Hint("clock_nerf")
	Item clock = Items.CLOCK;

	@PlayEvent
	public void addAdditionalHints(ZGatherHints event) {
		if(!enableNether && !enableEnd && !enableCompassNerf)
			return;

		MutableComponent comp = Component.literal("");
		String pad = "";
		if(enableNether) {
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.compass_nether"));
			pad = " ";
		}
		if(enableEnd) {
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.compass_end"));
			pad = " ";
		}
		if(enableCompassNerf)
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.compass_nerf"));

		event.accept(Items.COMPASS, comp);
	}

	@PlayEvent
	public void onUpdate(ZPlayerTick.Start event) {
		Inventory inventory = event.getPlayer().getInventory();
		for(int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if(stack.getItem() == Items.COMPASS)
				tickCompass(event.getPlayer(), stack);
			else if(stack.getItem() == Items.CLOCK)
				tickClock(stack);
		}
	}

	public static void tickClock(ItemStack stack) {
		boolean calculated = isClockCalculated(stack);
		if(!calculated)
			stack.set(QuarkDataComponents.IS_CLOCK_CALCULATED, true);
	}

	public static boolean isClockCalculated(ItemStack stack) {
		return Boolean.TRUE.equals(stack.get(QuarkDataComponents.IS_COMPASS_CALCULATED));
	}

	public static final String TAG_COMPASS_CALCULATED = "quark:compass_calculated";
	public static final String TAG_WAS_IN_NETHER = "quark:compass_in_nether";
	public static final String TAG_POSITION_SET = "quark:compass_position_set";
	public static final String TAG_NETHER_TARGET_X = "quark:nether_x";
	public static final String TAG_NETHER_TARGET_Z = "quark:nether_z";

	public static void tickCompass(Player player, ItemStack stack) {
		boolean calculated = isCompassCalculated(stack);
		boolean nether = player.level().dimension().location().equals(LevelStem.NETHER.location());

		if(calculated) {
			boolean wasInNether = Boolean.TRUE.equals(stack.get(QuarkDataComponents.WAS_IN_NETHER));
			BlockPos pos = player.blockPosition();
			boolean isInPortal = player.level().getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL;

			if(nether && !wasInNether && isInPortal) {
				stack.set(QuarkDataComponents.NETHER_TARGET_X, pos.getX());
				stack.set(QuarkDataComponents.NETHER_TARGET_Z, pos.getZ());
				stack.set(QuarkDataComponents.WAS_IN_NETHER, true);
				stack.set(QuarkDataComponents.IS_POS_SET, true);
			} else if(!nether && wasInNether) {
				stack.set(QuarkDataComponents.WAS_IN_NETHER, false);
				stack.set(QuarkDataComponents.IS_POS_SET, false);
			}
		} else {
			stack.set(QuarkDataComponents.IS_COMPASS_CALCULATED, true);
			stack.set(QuarkDataComponents.WAS_IN_NETHER, nether);
		}
	}

	public static boolean isCompassCalculated(ItemStack stack) {
		return Boolean.TRUE.equals(stack.get(QuarkDataComponents.WAS_IN_NETHER));
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends CompassesWorkEverywhereModule {

		@LoadEvent
		public void clientSetup(ZClientSetup e) {
			e.enqueueWork(() -> {
				if(!isEnabled()) return;

				if(enableCompassNerf || enableNether || enableEnd)
					ItemProperties.register(Items.COMPASS, Quark.asResource("angle"), new CompassAnglePropertyFunction());

				if(enableClockNerf)
					ItemProperties.register(Items.CLOCK, Quark.asResource("time"), new ClockTimePropertyFunction());
			});
		}

	}

}
