package org.violetmoon.quark.content.building.module;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
import org.violetmoon.quark.content.building.block.VariantLadderBlock;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.block.ZetaPillarBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "building")
public class IndustrialPaletteModule extends ZetaModule {

	public static List<Block> blocks = new ArrayList<>();

	private static final SoundType IRON_LADDER_SOUND_TYPE = new SoundType(1.0F, 1.0F,
			SoundEvents.METAL_BREAK,
			SoundEvents.LADDER_STEP,
			SoundEvents.METAL_PLACE,
			SoundEvents.METAL_HIT,
			SoundEvents.LADDER_FALL);

	@Config(flag = "iron_plates")
	public static boolean enableIronPlates = true;

	@Config(flag = "iron_ladder")
	public static boolean enableIronLadder = true;

	@LoadEvent
	public final void register(ZRegister event) {
		CreativeTabManager.daisyChain();
		Block.Properties props = Block.Properties.ofFullCopy(Blocks.IRON_BLOCK);

		BooleanSupplier ironPlateCond = () -> enableIronPlates;
		BooleanSupplier ironLadderCond = () -> enableIronLadder;

		Block ironPlate = new ZetaBlock("iron_plate", this, props).setCondition(ironPlateCond).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.CHAIN, true);
		Block rustyIronPlate = new ZetaBlock("rusty_iron_plate", this, props).setCondition(ironPlateCond).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		Block ironPillar = new ZetaPillarBlock("iron_pillar", this, props).setCondition(ironPlateCond).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		event.getVariantRegistry().addSlabAndStairs((IZetaBlock) ironPlate, null);
		event.getVariantRegistry().addSlabAndStairs((IZetaBlock) rustyIronPlate, null);
		CreativeTabManager.endDaisyChain();

		Block ironLadder = new VariantLadderBlock("iron", this, Block.Properties.of()
				.strength(0.8F)
				.sound(IRON_LADDER_SOUND_TYPE)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY), false
		).setCondition(ironLadderCond);

        blocks.add(ironPlate);
        blocks.add(rustyIronPlate);
        blocks.add(ironPillar);
        blocks.add(ironLadder);
	}
}
