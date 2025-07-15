package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.ZFurnaceFuelBurnTime;
import org.violetmoon.zeta.event.play.ZLevelTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

/**
 * @author WireSegal
 *         Created at 7:34 PM on 9/28/19.
 */
@ZetaLoadModule(category = "tweaks")
public class UtilityRecipesModule extends ZetaModule {

	@Config(description = "Can other stone-like materials be used for crafting stone tools?", flag = "better_stone_tools")
	public static boolean betterStoneToolCrafting = true;

	@Config(description = "Can a dispenser be crafted by adding a bow to a dropper?", flag = "dropper_upgrade")
	public static boolean enableDispenser = true;

	@Config(description = "Can a repeater be crafted with the pattern for a redstone torch?", flag = "repeater_and_torches")
	public static boolean enableRepeater = true;

	@Config(description = "Can you craft a minecart around blocks which can be placed inside?", flag = "minecart_upgrade")
	public static boolean enableMinecarts = true;

	@Config(description = "Can you craft a boat around a chest to directly make a chest boat?", flag = "direct_chest_boat")
	public static boolean enableChestBoats = true;

	@Config(description = "Can you craft four chests at once using logs?", flag = "wood_to_chest_recipes")
	public static boolean logsToChests = true;

	@Config(description = "Can Coral be crafted into dye?", flag = "coral_to_dye")
	public static boolean coralToDye = true;

	// Credit to /u/InfiniteNexus for the idea! https://reddit.com/d5k1s9/
	@Config(description = "Can cookies, paper, and bread be crafted in a 2x2 crafting table?", flag = "bent_recipes")
	public static boolean bentRecipes = true;

	@Config(description = "Can Rotten Flesh and Poisonous Potatoes be composted?")
	public static boolean compostableToxins = true;

	@Config(description = "Does Dragon Breath return a bottle when used as a reagent or material?")
	public static boolean effectiveDragonBreath = true;

	@Config(description = "Can torches can be used as fuel in furnaces?")
	public static boolean torchesBurn = true;

	@Config(description = "Can bones be smelted down to bone meal?", flag = "bone_meal_utility")
	public static boolean boneMealUtility = true;

	@Config(description = "Can Charcoal be crafted into Black Dye?", flag = "charcoal_to_dye")
	public static boolean charcoalToBlackDye = true;

	@Config(description = "Can two Logs be used instead of a Chest to make a Hopper?", flag = "easy_hopper")
	public static boolean easyHopper = true;

	@Config(description = "Can two Logs be used to craft 16 sticks?", flag = "easy_sticks")
	public static boolean easySticks = true;

	@Config(description = "Can raw ore blocks be smelted, taking 9x the time a normal item?", flag = "smelt_ore_blocks")
	public static boolean smeltRawOreBlocks = true;

	private boolean needsChange = false;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		// This has to be defered to a safer thread, making these changes in this thread can result in concurrency errors
		needsChange = true;
	}

	@PlayEvent
	public void worldTick(ZLevelTick.End event) {
		if(needsChange) {
			if(effectiveDragonBreath)
				Items.DRAGON_BREATH.craftingRemainingItem = null;
			else
				Items.DRAGON_BREATH.craftingRemainingItem = Items.GLASS_BOTTLE;

			if(compostableToxins) {
				ComposterBlock.COMPOSTABLES.put(Items.POISONOUS_POTATO, 0.85F);
				ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH, 0.3F);
			} else {
				ComposterBlock.COMPOSTABLES.removeFloat(Items.POISONOUS_POTATO);
				ComposterBlock.COMPOSTABLES.removeFloat(Items.ROTTEN_FLESH);
			}

			needsChange = false;
		}
	}

	@PlayEvent
	public void torchBurnTime(ZFurnaceFuelBurnTime event) {
		if(torchesBurn) {
			Item item = event.getItemStack().getItem();
			if(item == Items.TORCH || item == Items.SOUL_TORCH)
				event.setBurnTime(400);
		}
	}

}
