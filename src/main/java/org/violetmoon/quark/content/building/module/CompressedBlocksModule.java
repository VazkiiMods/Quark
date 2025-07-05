package org.violetmoon.quark.content.building.module;

import java.util.List;
import java.util.function.BooleanSupplier;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.block.ZetaFlammableBlock;
import org.violetmoon.zeta.block.ZetaFlammablePillarBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZLoadComplete;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import com.google.common.collect.Lists;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

@ZetaLoadModule(category = "building")
public class CompressedBlocksModule extends ZetaModule {

	@Config(name = "Charcoal Block and Blaze Lantern Stay On Fire Forever", flag = "compressed_blocks_burn_forever")
	public static boolean burnsForever = true;

	@Config(name = "Charcoal Block Fuel Time")
	@Config.Min(0)
	public static int charcoalBlockFuelTime = 16000;

	@Config(name = "Blaze Lantern Fuel Time")
	@Config.Min(0)
	public static int blazeLanternFuelTime = 24000;

	@Config(name = "Stick Block Fuel Time")
	@Config.Min(0)
	public static int stickBlockFuelTime = 900;

	@Config(flag = "charcoal_block")
	public static boolean enableCharcoalBlock = true;
	@Config(flag = "sugar_cane_block")
	public static boolean enableSugarCaneBlock = true;
	@Config(flag = "cactus_block")
	public static boolean enableCactusBlock = true;
	@Config(flag = "chorus_fruit_block")
	public static boolean enableChorusFruitBlock = true;
	@Config(flag = "stick_block")
	public static boolean enableStickBlock = true;

	@Config(flag = "apple_crate")
	public static boolean enableAppleCrate = true;
	@Config(flag = "golden_apple_crate")
	public static boolean enableGoldenAppleCrate = true;
	@Config(flag = "potato_crate")
	public static boolean enablePotatoCrate = true;
	@Config(flag = "carrot_crate")
	public static boolean enableCarrotCrate = true;
	@Config(flag = "golden_carrot_crate")
	public static boolean enableGoldenCarrotCrate = true;
	@Config(flag = "beetroot_crate")
	public static boolean enableBeetrootCrate = true;

	@Config(flag = "cocoa_beans_sack")
	public static boolean enableCocoaBeanSack = true;
	@Config(flag = "nether_wart_sack")
	public static boolean enableNetherWartSack = true;
	@Config(flag = "gunpowder_sack")
	public static boolean enableGunpowderSack = true;
	@Config(flag = "berry_sack")
	public static boolean enableBerrySack = true;
	@Config(flag = "glowberry_sack")
	public static boolean enableGlowBerrySack = true;

	@Config(flag = "blaze_lantern")
	public static boolean enableBlazeLantern = true;
	@Config(flag = "bonded_leather")
	public static boolean enableBondedLeather = true;
	@Config(flag = "bonded_rabbit_hide")
	public static boolean enableBondedRabbitHide = true;

	@Hint("compressed_blocks_burn_forever")
	public static Block charcoal_block;
	@Hint("compressed_blocks_burn_forever")
	public static Block blaze_lantern;
	@Hint("golden_apple_crate")
	public static Block golden_apple_crate;

	public static Block stick_block;

	public static List<Block> blocks = Lists.newArrayList();
	private final List<Block> compostable = Lists.newArrayList();

	//these are purely for datagen
	public static Block sugarCane, cactus, chorus, apple, potato, carrot, golden_carrot,
			beetroot, cocoa, wart, gunpowder, berry, glowberry, leather, hide;

	@LoadEvent
	public final void register(ZRegister event) {
		charcoal_block = new ZetaBlock("charcoal_block", this,
				Block.Properties.of()
						.instrument(NoteBlockInstrument.BASEDRUM)
						.mapColor(MapColor.COLOR_BLACK)
						.requiresCorrectToolForDrops()
						.strength(5F, 10F)
						.sound(SoundType.STONE))
				.setCondition(() -> enableCharcoalBlock)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.COAL_BLOCK, false);
		blocks.add(charcoal_block);

		sugarCane = pillar("sugar_cane", MapColor.COLOR_LIGHT_GREEN, true, () -> enableSugarCaneBlock, 200);
		blocks.add(sugarCane);
		cactus = pillar("cactus", MapColor.COLOR_GREEN, true, () -> enableCactusBlock, 50);
		blocks.add(cactus);
		chorus = pillar("chorus_fruit", MapColor.COLOR_PURPLE, false, () -> enableChorusFruitBlock, 10);
		blocks.add(chorus);
		stick_block = pillar("stick", MapColor.WOOD, false, () -> enableStickBlock, 300);
		blocks.add(stick_block);

		golden_apple_crate = crate("golden_apple", MapColor.GOLD, false, () -> enableGoldenAppleCrate);
		blocks.add(golden_apple_crate);
		apple = crate("apple", MapColor.COLOR_RED, true, () -> enableAppleCrate);
		blocks.add(apple);
		potato = crate("potato", MapColor.COLOR_ORANGE, true, () -> enablePotatoCrate);
		blocks.add(potato);
		carrot = crate("carrot", MapColor.TERRACOTTA_ORANGE, true, () -> enableCarrotCrate);
		blocks.add(carrot);
		golden_carrot = crate("golden_carrot", MapColor.GOLD, false, () -> enableGoldenCarrotCrate);
		blocks.add(golden_carrot);
		beetroot = crate("beetroot", MapColor.COLOR_RED, true, () -> enableBeetrootCrate);
		blocks.add(beetroot);

		cocoa = sack("cocoa_beans", MapColor.COLOR_BROWN, true, () -> enableCocoaBeanSack);
		blocks.add(cocoa);
		wart = sack("nether_wart", MapColor.COLOR_RED, true, () -> enableNetherWartSack);
		blocks.add(wart);
		gunpowder = sack("gunpowder", MapColor.COLOR_GRAY, false, () -> enableGunpowderSack);
		blocks.add(gunpowder);
		berry = sack("berry", MapColor.COLOR_RED, true, () -> enableBerrySack);
		blocks.add(berry);
		glowberry = sack("glowberry", MapColor.COLOR_YELLOW, 14, true, () -> enableGlowBerrySack);
		blocks.add(glowberry);

		blaze_lantern = new ZetaBlock("blaze_lantern", this,
				Block.Properties.of()
						.mapColor(DyeColor.YELLOW)
						.strength(0.3F)
						.sound(SoundType.GLASS)
						.lightLevel(b -> 15))
				.setCondition(() -> enableBlazeLantern)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.NETHERRACK, true);
		blocks.add(blaze_lantern);

		leather = new ZetaBlock("bonded_leather", this, Block.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.ignitedByLava()
				.strength(0.4F)
				.sound(SoundType.WOOL))
				.setCondition(() -> enableBondedLeather)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		blocks.add(leather);

		hide = new ZetaBlock("bonded_rabbit_hide", this, Block.Properties.of()
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.ignitedByLava()
				.strength(0.4F)
				.sound(SoundType.WOOL))
				.setCondition(() -> enableBondedRabbitHide)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		blocks.add(hide);
	}

	@LoadEvent
	public void loadComplete(ZLoadComplete event) {
		event.enqueueWork(() -> {
			for(Block block : compostable)
				if(block.asItem() != null)
					ComposterBlock.COMPOSTABLES.put(block.asItem(), 1F);
		});

		Quark.ZETA.fuel.addFuel(stick_block, stickBlockFuelTime);
		Quark.ZETA.fuel.addFuel(charcoal_block, charcoalBlockFuelTime);
		Quark.ZETA.fuel.addFuel(blaze_lantern, blazeLanternFuelTime);
	}

	private Block pillar(String name, MapColor color, boolean compost, BooleanSupplier cond, int flammability) {
		Block block = new ZetaFlammablePillarBlock(name + "_block", this, flammability,
				Block.Properties.of()
						.mapColor(color)
						.ignitedByLava()
						.strength(0.5F)
						.sound(SoundType.WOOD))
				.setCondition(cond)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		if(compost)
			compostable.add(block);
		return block;
	}

	private Block crate(String name, MapColor color, boolean compost, BooleanSupplier cond) {
		Block block = new ZetaFlammableBlock(name + "_crate", this, 150,
				Block.Properties.of()
						.mapColor(color)
						.ignitedByLava()
						.strength(1.5F)
						.sound(SoundType.WOOD))
				.setCondition(cond)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		if(compost)
			compostable.add(block);
		return block;
	}

	private Block sack(String name, MapColor color, boolean compost, BooleanSupplier cond) {
		return sack(name, color, 0, compost, cond);
	}

	private Block sack(String name, MapColor color, int light, boolean compost, BooleanSupplier cond) {
		Block block = new ZetaFlammableBlock(name + "_sack", this, 150,
				Block.Properties.of()
						.mapColor(color)
						.ignitedByLava()
						.strength(0.5F)
						.lightLevel(s -> light)
						.sound(SoundType.WOOL))
				.setCondition(cond)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		if(compost)
			compostable.add(block);
		return block;
	}

}
