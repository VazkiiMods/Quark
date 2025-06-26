package org.violetmoon.quark.addons.oddities.module;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.violetmoon.quark.addons.oddities.block.MatrixEnchantingTableBlock;
import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import org.violetmoon.quark.addons.oddities.client.render.be.MatrixEnchantingTableRenderer;
import org.violetmoon.quark.addons.oddities.client.screen.MatrixEnchantingScreen;
import org.violetmoon.quark.addons.oddities.inventory.MatrixEnchantingMenu;
import org.violetmoon.quark.addons.oddities.util.CustomInfluence;
import org.violetmoon.quark.addons.oddities.util.Influence;
import org.violetmoon.quark.addons.oddities.util.InfluenceLocations;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorMenuScreens;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.ZBlock;
import org.violetmoon.zeta.event.play.ZItemTooltip;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZetaLoadModule(category = "oddities")
public class MatrixEnchantingModule extends ZetaModule {

	public static BlockEntityType<MatrixEnchantingTableBlockEntity> blockEntityType;
	public static MenuType<MatrixEnchantingMenu> menuType;

	@Config(description = "The maximum enchanting power the matrix enchanter can accept")
	public static int maxBookshelves = 15;

	@Config(description = "Should this be X, the price of a piece increase by 1 every X pieces you generate")
	public static int piecePriceScale = 9;

	@Config(description = "The higher this is, the better enchantments you'll get on books")
	public static int bookEnchantability = 12;

	@Config(description = "How many pieces you can generate without any bookshelves")
	public static int baseMaxPieceCount = 3;

	@Config(description = "How many pieces you can generate without any bookshelves (for Books)")
	public static int baseMaxPieceCountBook = 1;

	@Config(description = "At which piece count the calculation for the min level should default to increasing one per piece rather than using the scale factor")
	public static int minLevelCutoff = 8;

	@Config(description = "How many pieces a single Lapis can generate")
	public static int chargePerLapis = 4;

	@Config(description = "How much the min level requirement for adding a new piece should increase for each piece added (up until the value of Min Level Cutoff)")
	public static double minLevelScaleFactor = 1.2;

	@Config(description = "How much the min level requirement for adding a new piece to a book should increase per each bookshelf being used")
	public static double minLevelScaleFactorBook = 2.0;

	@Config(description = "How much to multiply the frequency of pieces where at least one of the same type has been generated")
	public static double dupeMultiplier = 1.4;

	@Config(description = "How much to multiply the frequency of pieces where incompatible pieces have been generated")
	public static double incompatibleMultiplier = 0.0;

	@Config(description = "Set to false to disable the ability to create Enchanted Books")
	public static boolean allowBooks = true;

	@Config(description = "Set this to true to allow treasure enchantments to be rolled as pieces")
	public static boolean allowTreasures = false;

	@Config(description = "Set this to true to allow undiscoverable enchantments to be rolled as pieces")
	public static boolean allowUndiscoverableEnchantments = false;

	@Config(description = "Any treasure enchantment IDs here will be able to appear in books in matrix enchanting")
	public static List<String> treasureWhitelist = Lists.newArrayList();

	@Config(description = "Any undiscoverable enchantment IDs here will be able to appear in matrix enchanting")
	public static List<String> undiscoverableWhitelist = Lists.newArrayList();

	@Config(description = "Set to false to disable the tooltip for items with pending enchantments")
	public static boolean showTooltip = true;

	@Config(description = "By default, enchantment rarities are fuzzed a bit to feel better with the new system. Set this to false to override this behaviour.")
	public static boolean normalizeRarity = true;

	@Config(description = "Matrix Enchanting can be done with water instead of air around the enchanting table. Set this to false to disable this behaviour.")
	public static boolean allowUnderwaterEnchanting = true;

	@Config(description = "Matrix Enchanting can be done with short (<= 3px blocks) instead of air around the enchanting table. Set this to false to disable this behaviour.")
	public static boolean allowShortBlockEnchanting = true;

	@Config(description = "Candles with soul sand below them or below the bookshelves dampen enchantments instead of influence them.")
	public static boolean soulCandlesInvert = true;

	@Config(description = "A list of enchantment IDs you don't want the enchantment table to be able to create")
	public static List<String> disallowedEnchantments = Lists.newArrayList();

	@Config(
		description = "An array of influences each candle should apply. This list must be 16 elements long, and is in order of wool colors.\n" +
				"A minus sign before an enchantment will make the influence decrease the probability of that enchantment."
	)
	private static List<String> influencesList = Lists.newArrayList(
			"minecraft:unbreaking", // White
			"minecraft:fire_protection", // Orange
			"minecraft:knockback,minecraft:punch", // Magenta
			"minecraft:feather_falling", // Light Blue
			"minecraft:looting,minecraft:fortune,minecraft:luck_of_the_sea", // Yellow
			"minecraft:blast_protection", // Lime
			"minecraft:silk_touch,minecraft:channeling", // Pink
			"minecraft:bane_of_arthropods", // Gray
			"minecraft:protection", // Light Gray
			"minecraft:respiration,minecraft:loyalty,minecraft:infinity", // Cyan
			"minecraft:sweeping,minecraft:multishot", // Purple
			"minecraft:efficiency,minecraft:sharpness,minecraft:lure,minecraft:power,minecraft:impaling,minecraft:quick_charge", // Blue
			"minecraft:aqua_affinity,minecraft:depth_strider,minecraft:riptide", //Brown
			"minecraft:thorns,minecraft:piercing", // Green
			"minecraft:fire_aspect,minecraft:flame", // Red
			"minecraft:smite,minecraft:projectile_protection" // Black
	);

	@Config(
		description = "An array of influences that other blocks should apply.\n" +
				"Format is: \"blockstate;strength;color;enchantments\", i.e. \"minecraft:sea_pickle[pickles=1,waterlogged=false];1;#008000;minecraft:aqua_affinity,minecraft:depth_strider,minecraft:riptide\" (etc) or \"minecraft:anvil[facing=north];#808080;-minecraft:thorns,minecraft:unbreaking\" (etc)"
	)
	private static List<String> statesToInfluences = Lists.newArrayList();

	@Config(description = "Set to false to disable the ability to influence enchantment outcomes with candles", flag = "candle_influencing")
	public static boolean allowInfluencing = true;

	public static boolean candleInfluencingFailed = false;

	@Config(description = "The max amount of candles that can influence a single enchantment")
	public static int influenceMax = 4;

	@Config(description = "How much each candle influences an enchantment. This works as a multiplier to its weight")
	public static double influencePower = 0.125;

	@Config(description = "If you set this to false, the vanilla Enchanting Table will no longer automatically convert to the Matrix Enchanting table. You'll have to add a recipe for the Matrix Enchanting Table to make use of this.", flag = "matrix_enchanting_autoconvert")
	public static boolean automaticallyConvert = true;

	public static Map<DyeColor, InfluenceLocations> candleInfluences = new HashMap<>();
	public static Map<BlockState, CustomInfluence> customInfluences = new HashMap<>();

	public static Block matrixEnchanter;

	public static ManualTrigger influenceTrigger;

	@LoadEvent
	public final void register(ZRegister event) {
		matrixEnchanter = new MatrixEnchantingTableBlock(this);

		menuType = IMenuTypeExtension.create(MatrixEnchantingMenu::fromNetwork);
		Quark.ZETA.registry.register(menuType, "matrix_enchanting", Registries.MENU);

		blockEntityType = BlockEntityType.Builder.of(MatrixEnchantingTableBlockEntity::new, matrixEnchanter).build(null);
		Quark.ZETA.registry.register(blockEntityType, "matrix_enchanting", Registries.BLOCK_ENTITY_TYPE);

		influenceTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("influence");
	}

	@LoadEvent
	public void addAdditionalHints(ZGatherHints event) {
		MutableComponent comp = Component.translatable("quark.jei.hint.matrix_enchanting");
		if(allowInfluencing)
			comp = comp.append(" ").append(Component.translatable("quark.jei.hint.matrix_influencing"));

		event.accept(Items.ENCHANTING_TABLE, comp);
		event.accept(matrixEnchanter.asItem(), comp);
	}

	@PlayEvent
	public void onBlockPlaced(ZBlock.EntityPlace event) {
		if(event.getPlacedBlock().getBlock().equals(Blocks.ENCHANTING_TABLE) && automaticallyConvert)
			event.getLevel().setBlock(event.getPos(), matrixEnchanter.defaultBlockState(), 3);
	}

	@PlayEvent
	public void onRightClick(ZPlayerInteract.RightClickBlock event) {
		if(event.getEntity() instanceof FakePlayer)
			return;

		if(event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.ENCHANTING_TABLE && automaticallyConvert)
			event.getLevel().setBlock(event.getPos(), matrixEnchanter.defaultBlockState(), 3);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		parseInfluences();
	}

	private InfluenceLocations parseEnchantmentList(String enchantmentList) {
		List<ResourceLocation> boost = new ArrayList<>();
		List<ResourceLocation> dampen = new ArrayList<>();
		String[] enchantments = enchantmentList.split(",");
		for(String enchStr : enchantments) {
			boolean damp = enchStr.startsWith("-");
			if(damp)
				enchStr = enchStr.substring(1);

			if(damp)
				dampen.add(ResourceLocation.parse(enchStr));
			else
				boost.add(ResourceLocation.parse(enchStr));

			//Holder<Enchantment> ench = Holder.direct(Quark.proxy.hackilyGetCurrentClientLevelRegistryAccess().registry(Registries.ENCHANTMENT).get().get(ResourceLocation.parse(enchStr)));
		}
		return new InfluenceLocations(boost, dampen);
	}

	private void parseInfluences() {
		candleInfluences.clear();
		customInfluences.clear();

		for(String influence : statesToInfluences) {
			String[] split = influence.split(";");
			if(split.length == 4) {
				int strength, color;
				BlockState state = MiscUtil.fromString(split[0]);
				try {
					strength = Integer.parseInt(split[1]);
					color = Integer.parseInt(split[2], 16);
				} catch (NumberFormatException e) {
					continue;
				}
				InfluenceLocations boosts = parseEnchantmentList(split[3]);

				customInfluences.put(state, new CustomInfluence(strength, color, boosts));
			}
		}

		if(influencesList.size() < 16) {
			(new IllegalArgumentException("Matrix Enchanting Influences must be of at least size 16, please fix this in the config.")).printStackTrace();
			candleInfluencingFailed = true;
		} else {
			for(int i = 0; i < influencesList.size(); i++) {
				if (i < DyeColor.values().length) {
					candleInfluences.put(DyeColor.values()[i], parseEnchantmentList(influencesList.get(i)));
				}
			}
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends MatrixEnchantingModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			//AccessorMenuScreens.invokeRegister(menuType, MatrixEnchantingScreen::new);
			BlockEntityRenderers.register(blockEntityType, MatrixEnchantingTableRenderer::new);
		}

		@PlayEvent
		public void onTooltip(ZItemTooltip event) {
			ItemStack stack = event.getItemStack();
			if(showTooltip && stack.has(QuarkDataComponents.STACK_MATRIX))
				event.getToolTip().add(Component.translatable("quark.gui.enchanting.pending").withStyle(ChatFormatting.AQUA));
		}
	}
}
