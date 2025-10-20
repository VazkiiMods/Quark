package org.violetmoon.quark.content.building.module;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;
import org.violetmoon.quark.content.building.block.HedgeBlock;
import org.violetmoon.quark.content.world.module.AncientWoodModule;
import org.violetmoon.quark.content.world.module.BlossomTreesModule;
import org.violetmoon.zeta.client.AlikeColorHandler;
import org.violetmoon.zeta.client.event.load.ZAddBlockColorHandlers;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class HedgesModule extends ZetaModule {

	public static TagKey<Block> hedgesTag;
	public static List<HedgeBlock> hedges = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
		for(Wood wood : VanillaWoods.OVERWORLD_WITH_TREE){
			HedgeBlock hedgeBlock = new HedgeBlock(wood.name() + "_hedge", this, wood.fence(), wood.leaf());
			hedges.add(hedgeBlock); //0-7
		}


		HedgeBlock azaleaHedge = new HedgeBlock("azalea_hedge", this, Blocks.OAK_FENCE, Blocks.AZALEA_LEAVES);
		hedges.add(azaleaHedge); //8
		HedgeBlock floweringAzaleaHedge = new HedgeBlock("flowering_azalea_hedge", this, Blocks.OAK_FENCE, Blocks.FLOWERING_AZALEA_LEAVES);
		hedges.add(floweringAzaleaHedge); //9
	}

	@LoadEvent
	public void postRegister(ZRegister.Post e) {
		for(BlossomTreesModule.BlossomTree tree : BlossomTreesModule.blossomTrees){
			HedgeBlock blossomHedge = (HedgeBlock) new HedgeBlock(tree.name + "_hedge", this, BlossomTreesModule.woodSet.fence, tree.leaves).setCondition(tree.sapling::isEnabled);
			hedges.add(blossomHedge); //10-14
		}

		HedgeBlock ancientHedge = (HedgeBlock) new HedgeBlock("ancient_hedge", this, AncientWoodModule.woodSet.fence, AncientWoodModule.ancient_leaves)
			.setCondition(() -> Quark.ZETA.modules.isEnabled(AncientWoodModule.class));
		hedges.add(ancientHedge); //15
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		hedgesTag = Quark.asTagKey(Registries.BLOCK,"hedges");
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends HedgesModule {

		@LoadEvent
		public void blockColorProviders(ZAddBlockColorHandlers event) {
			event.registerNamed(zeta(), b -> new AlikeColorHandler((HedgeBlock) b, HedgeBlock::getLeaf), "hedge");
		}

		@LoadEvent
		public void itemColorProviders(ZAddItemColorHandlers event) {
			event.registerNamed(zeta(), i -> new AlikeColorHandler(i, HedgeBlock::getLeaf), "hedge");
		}

	}

}
