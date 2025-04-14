package org.violetmoon.quark.content.client.module;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.resources.AttributeTooltipManager;
import org.violetmoon.quark.content.client.tooltip.AttributeTooltips;
import org.violetmoon.quark.content.client.tooltip.EnchantedBookTooltips;
import org.violetmoon.quark.content.client.tooltip.FoodTooltips;
import org.violetmoon.quark.content.client.tooltip.FuelTooltips;
import org.violetmoon.quark.content.client.tooltip.MapTooltips;
import org.violetmoon.quark.content.client.tooltip.ShulkerBoxTooltips;
import org.violetmoon.zeta.client.event.load.ZRegisterClientReloadListener;
import org.violetmoon.zeta.client.event.load.ZTooltipComponents;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.ItemNBTHelper;

import java.util.List;

/**
 * @author WireSegal
 *         Created at 6:19 PM on 8/31/19.
 */
@ZetaLoadModule(category = "client")
public class ImprovedTooltipsModule extends ZetaModule {

	@Config
	public static boolean attributeTooltips = true;
	@Config
	public static boolean foodTooltips = true;
	@Config
	public static boolean shulkerTooltips = true;
	@Config
	public static boolean mapTooltips = true;
	@Config
	public static boolean enchantingTooltips = true;
	@Config
	public static boolean fuelTimeTooltips = true;

	@Config
	public static boolean shulkerBoxUseColors = true;
	@Config
	public static boolean shulkerBoxRequireShift = false;
	@Config
	public static boolean mapRequireShift = false;

	@Config(
		description = "The value of each shank of food.\n" +
				"Tweak this when using mods like Hardcore Hunger which change that value."
	)
	public static int foodDivisor = 2;

	@Config
	public static boolean showSaturation = true;
	@Config
	public static int foodCompressionThreshold = 4;

	@Config
	public static int fuelTimeDivisor = 200;

	@Config(
		description = "Should item attributes be colored relative to your current equipped item?\n"
				+ "e.g. if wearing an Iron Helmet, the armor value in a Diamond Helmet will show as green, and vice versa would be red.\n"
				+ "If set to false, item attributes will show in white or red if they're negative values."
	)
	public static boolean showUpgradeStatus = true;
	@Config
	public static boolean animateUpDownArrows = true;

	@Config
	public static List<String> enchantingStacks = Lists.newArrayList("minecraft:diamond_sword", "minecraft:diamond_pickaxe", "minecraft:diamond_shovel", "minecraft:diamond_axe", "minecraft:diamond_hoe",
			"minecraft:diamond_helmet", "minecraft:diamond_chestplate", "minecraft:diamond_leggings", "minecraft:diamond_boots",
			"minecraft:shears", "minecraft:bow", "minecraft:fishing_rod", "minecraft:crossbow", "minecraft:trident", "minecraft:elytra", "minecraft:shield",
			"quark:pickarang", "supplementaries:slingshot", "supplementaries:bubble_blower", "farmersdelight:diamond_knife", "the_bumblezone:stinger_spear", 
			"the_bumblezone:crystal_cannon", "the_bumblezone:honey_crystal_shield", "the_bumblezone:honey_bee_leggings_2");

	@Config(
		description = "A list of additional stacks to display on each enchantment\n"
				+ "The format is as follows:\n"
				+ "enchant_id=item1,item2,item3...\n"
				+ "So to display a carrot on a stick on a mending book, for example, you use:\n"
				+ "minecraft:mending=minecraft:carrot_on_a_stick"
	)
	public static List<String> enchantingAdditionalStacks = Lists.newArrayList();

	private static final String IGNORE_TAG = "quark:no_tooltip";

	public static boolean staticEnabled;

	//put out here for itemstack mixin haxx required for this to work
	public static boolean shouldHideAttributes() {
		return staticEnabled && attributeTooltips && !Quark.proxy.isClientPlayerHoldingShift();
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ImprovedTooltipsModule {

		@LoadEvent
		public void registerClientTooltipComponentFactories(ZTooltipComponents event) {
			event.register(AttributeTooltips.AttributeComponent.class);
			event.register(FoodTooltips.FoodComponent.class);
			event.register(ShulkerBoxTooltips.ShulkerComponent.class);
			event.register(MapTooltips.MapComponent.class);
			event.register(EnchantedBookTooltips.EnchantedBookComponent.class);
			event.register(FuelTooltips.FuelComponent.class);
		}

		@LoadEvent
		public void registerReloadListeners(ZRegisterClientReloadListener registry) {
			registry.accept(new AttributeTooltipManager());
		}

		@LoadEvent
		public final void configChanged(ZConfigChanged event) {
			staticEnabled = isEnabled();
			EnchantedBookTooltips.reloaded();
		}

		private static boolean ignore(ItemStack stack) {
			return ItemNBTHelper.getBoolean(stack, IGNORE_TAG, false);
		}

		@PlayEvent
		public void makeTooltip(ZGatherTooltipComponents event) {
			if(ignore(event.getItemStack()))
				return;

			if(attributeTooltips)
				AttributeTooltips.makeTooltip(event);
			if(foodTooltips || showSaturation)
				FoodTooltips.makeTooltip(event, foodTooltips, showSaturation);
			if(shulkerTooltips)
				ShulkerBoxTooltips.makeTooltip(event);
			if(mapTooltips)
				MapTooltips.makeTooltip(event);
			if(enchantingTooltips)
				EnchantedBookTooltips.makeTooltip(event);
			if(fuelTimeTooltips)
				FuelTooltips.makeTooltip(event);
		}
	}
}
