package org.violetmoon.quark.base.config;

import java.util.List;

import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.IConfigType;

import com.google.common.collect.Lists;

public class QuarkGeneralConfig {

	public static final QuarkGeneralConfig INSTANCE = new QuarkGeneralConfig();

	/**
	 * Note for future maintainers reading this and thinking "oh why is this referencing
	 * the classes by name I can just replace that with the class object" no you can't
	 * you will break the server.
	 * 
	 * - Vazkii, from the past.
	 */
    // What the fuck
	private static final List<String> STATIC_ALLOWED_SCREENS = Lists.newArrayList(
			"appeng.client.gui.implementations.SkyChestScreen",
			"com.progwml6.ironchest.client.screen.IronChestScreen",
			"net.mehvahdjukaar.supplementaries.client.screens.SackScreen",
			"top.theillusivec4.curios.client.gui.CuriosScreen",
			"org.violetmoon.quark.addons.oddities.client.screen.CrateScreen",
			"org.violetmoon.quark.addons.oddities.client.screen.BackpackInventoryScreen"
	);

	private static final List<String> STATIC_DENIED_SCREENS = Lists.newArrayList(
			"blusunrize.immersiveengineering.client.gui.CraftingTableScreen",
			"com.tfar.craftingstation.client.CraftingStationScreen",
			"com.refinedmods.refinedstorage.screen.grid.GridScreen",
			"appeng.client.gui.me.items.CraftingTermScreen",
			"appeng.client.gui.me.items.PatternTermScreen",
			"com.blakebr0.extendedcrafting.client.screen.EliteTableScreen",
			"com.blakebr0.extendedcrafting.client.screen.EliteAutoTableScreen",
			"com.blakebr0.extendedcrafting.client.screen.UltimateTableScreen",
			"com.blakebr0.extendedcrafting.client.screen.UltimateAutoTableScreen",
			"me.desht.modularrouters.client.gui.filter.GuiFilterScreen",
			"com.resourcefulbees.resourcefulbees.client.gui.screen.CentrifugeScreen",
			"com.resourcefulbees.resourcefulbees.client.gui.screen.MechanicalCentrifugeScreen",
			"com.resourcefulbees.resourcefulbees.client.gui.screen.CentrifugeMultiblockScreen",
			"com.refinedmods.refinedstorage.screen.FilterScreen",
			"de.markusbordihn.dailyrewards.client.screen.RewardScreen"
	);

	@Config(name = "Enable 'q' Button")
	public static boolean enableQButton = true;

	@Config(name = "'q' Button on the Right")
	public static boolean qButtonOnRight = false;

	@Config
	public static boolean disableQMenuEffects = false;

	@Config(description = "How many advancements deep you can see in the advancement screen. Vanilla is 2.")
	@Config.Min(value = 0, exclusive = true)
	public static int advancementVisibilityDepth = 2;

	@Config(description = "Blocks that Quark should treat as Shulker Boxes.")
	public static List<String> shulkerBoxes = SimilarBlockTypeHandler.getBasicShulkerBoxes();

	@Config(description = "Should Quark treat anything with 'shulker_box' in its item identifier as a shulker box?")
	public static boolean interpretShulkerBoxLikeBlocks = true;

	@Config(description = "Set to true if you need to find the class name for a screen that's causing problems")
	public static boolean printScreenClassnames = false;

	@Config(description = "A list of screens that can accept quark's buttons. Use \"Print Screen Classnames\" to find the names of any others you'd want to add.")
	private static List<String> allowedScreens = Lists.newArrayList();

	@Config(description = "If set to true, the 'Allowed Screens' option will work as a Blacklist rather than a Whitelist. WARNING: Use at your own risk as some mods may not support this.")
	private static boolean useScreenListBlacklist = false;

	@Config(description = "If 'true' and TerraBlender is present, Quark will add a TerraBlender region. The region will contain vanilla biomes and the Glimmering Weald.")
	public static boolean terrablenderAddRegion = true;

	@Config(description = "Quark will set this weight for its TerraBlender region.")
	public static int terrablenderRegionWeight = 1;

	@Config(
		description = "If 'true', Quark will modify the `minecraft:overworld` MultiNoiseBiomeSourceParameterList preset, even when Terrablender is installed.\n" +
				"This will have various knock-on effects but might make the Weald more common, or appear closer to modded biomes. Who knows?"
	)
	public static boolean terrablenderModifyVanillaAnyway = false;

	@Config(description = "Set to false to disable the popup message telling you that you can config quark in the q menu")
	public static boolean enableOnboarding = true;
	
	@Config(description = "The amount of slots the chest button system should seek when trying to figure out if a container should be eligible for them.")
	public static int chestButtonSlotTarget = 27;

	@Config(description = "Set this to false to not generate the Quark Programmer Art resource pack")
	public static boolean generateProgrammerArt = true;
	
	@Config
	public static ChestOffsets chestButtonOffsets = new ChestOffsets();
	
	private QuarkGeneralConfig() {
		// NO-OP
	}

	public static boolean isScreenAllowed(Object screen) {
		String clazz = screen.getClass().getName();
		if(clazz.startsWith("net.minecraft."))
			return true;

		if(STATIC_ALLOWED_SCREENS.contains(clazz))
			return true;
		if(STATIC_DENIED_SCREENS.contains(clazz))
			return false;

		return allowedScreens.contains(clazz) != useScreenListBlacklist;
	}
	
	public static class ChestOffsets implements IConfigType {
		@Config public int playerX = 0;
		@Config public int playerY = 0;
		@Config public int topX = 0;
		@Config public int topY = 0;
		@Config public int middleX = 0;
		@Config public int middleY = 0;
	}

}
