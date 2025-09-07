package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import org.violetmoon.quark.content.building.block.MyalitePillarBlock;
import org.violetmoon.quark.content.world.block.MyaliteBlock;
import org.violetmoon.quark.content.world.module.NewStoneTypesModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.block.ZetaBlockWrapper;
import org.violetmoon.zeta.block.ZetaPillarBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.ConfigFlagManager;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZGatherAdditionalFlags;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "building", loadPhase = 10) //Needs to load after NewStoneTypesModule
public class MoreStoneVariantsModule extends ZetaModule {

	@Config(flag = "stone_bricks")
	public boolean enableBricks = true;
	@Config(flag = "stone_chiseled")
	public boolean enableChiseledBricks = true;
	@Config(flag = "stone_pillar")
	public boolean enablePillar = true;

	public static MoreStoneVariantsModule instance;

	public static Block polishedCalcite;
	public static Block polishedDripstone;

    public static List<Block> blocks = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
		polishedCalcite = expandVanillaStone(event, this, Blocks.CALCITE, "calcite");
		polishedDripstone = expandVanillaStone(event, this, Blocks.DRIPSTONE_BLOCK, "dripstone");

		add(event, "granite", MapColor.DIRT, SoundType.STONE, Blocks.POLISHED_GRANITE, BooleanSuppliers.TRUE);
		add(event, "diorite", MapColor.QUARTZ, SoundType.STONE, Blocks.POLISHED_DIORITE, BooleanSuppliers.TRUE);
		add(event, "andesite", MapColor.STONE, SoundType.STONE, Blocks.POLISHED_ANDESITE, BooleanSuppliers.TRUE);
		add(event, "calcite", MapColor.TERRACOTTA_WHITE, SoundType.CALCITE, polishedCalcite, BooleanSuppliers.TRUE);
		add(event, "dripstone", MapColor.TERRACOTTA_BROWN, SoundType.DRIPSTONE_BLOCK, polishedDripstone, BooleanSuppliers.TRUE);

		add(event, "limestone", MapColor.STONE, SoundType.STONE, NewStoneTypesModule.polishedBlocks.get(NewStoneTypesModule.limestoneBlock), () -> NewStoneTypesModule.enableLimestone);
		add(event, "jasper", MapColor.TERRACOTTA_RED, SoundType.STONE, NewStoneTypesModule.polishedBlocks.get(NewStoneTypesModule.jasperBlock), () -> NewStoneTypesModule.enableJasper);
		add(event, "shale", MapColor.ICE, SoundType.STONE, NewStoneTypesModule.polishedBlocks.get(NewStoneTypesModule.shaleBlock), () -> NewStoneTypesModule.enableShale);

		add(event, "myalite", MapColor.COLOR_PURPLE, SoundType.STONE, NewStoneTypesModule.polishedBlocks.get(NewStoneTypesModule.myaliteBlock), () -> NewStoneTypesModule.enableMyalite, MyaliteBlock::new, MyalitePillarBlock::new);

		instance = this;
	}

	@LoadEvent
	public final void moreFlags(ZGatherAdditionalFlags event) {
		ConfigFlagManager manager = event.flagManager();
		manager.putFlag(this, "granite", true);
		manager.putFlag(this, "diorite", true);
		manager.putFlag(this, "andesite", true);
		manager.putFlag(this, "calcite", true);
		manager.putFlag(this, "dripstone", true);
	}

	public Block expandVanillaStone(ZRegister event, ZetaModule module, Block raw, String name) {
		ZetaBlockWrapper wrap = new ZetaBlockWrapper(raw, this);
		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.BUILDING_BLOCKS, wrap, Blocks.DEEPSLATE, true);

		return NewStoneTypesModule.makeStone(event, module, raw, name, null, null, BooleanSuppliers.TRUE, null, ZetaBlock::new);
	}

	private void add(ZRegister event, String name, MapColor color, SoundType sound, Block basePolished, BooleanSupplier cond) {
		add(event, name, color, sound, basePolished, cond, ZetaBlock::new, ZetaPillarBlock::new);
	}

	private void add(ZRegister event, String name, MapColor color, SoundType sound, Block basePolished, BooleanSupplier cond, ZetaBlock.Constructor<ZetaBlock> constr, ZetaBlock.Constructor<ZetaPillarBlock> pillarConstr) {
		Block.Properties props = Block.Properties.of()
				.requiresCorrectToolForDrops()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.mapColor(color)
				.sound(sound)
				.strength(1.5F, 6.0F);

		CreativeTabManager.daisyChain();
		ZetaBlock bricks = (ZetaBlock) constr.make(name + "_bricks", this, props)
				.setCondition(() -> cond.getAsBoolean() && enableBricks)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, basePolished, false);
        ZetaBlock chiseledBricks = (ZetaBlock) constr.make("chiseled_" + name + "_bricks", this, props)
				.setCondition(() -> cond.getAsBoolean() && enableBricks && enableChiseledBricks)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
        ZetaPillarBlock pillar = (ZetaPillarBlock) pillarConstr.make(name + "_pillar", this, props)
				.setCondition(() -> cond.getAsBoolean() && enablePillar)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		event.getVariantRegistry().addSlabStairsWall(bricks, null);
		CreativeTabManager.endDaisyChain();
        blocks.add(bricks);
        blocks.add(chiseledBricks);
        blocks.add(pillar);
	}

	private void addPillar(ZRegister event, String name, MapColor color, SoundType sound, Block parentBlock, BooleanSupplier cond, ZetaBlock.Constructor<ZetaPillarBlock> pillarConstr) {
		Block.Properties props = Block.Properties.of()
				.requiresCorrectToolForDrops()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.mapColor(color)
				.sound(sound)
				.strength(1.5F, 6.0F);

		pillarConstr.make(name + "_pillar", this, props)
				.setCondition(() -> cond.getAsBoolean() && enablePillar)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, parentBlock, false);

	}

}
