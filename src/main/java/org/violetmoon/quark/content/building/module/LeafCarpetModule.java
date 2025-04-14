package org.violetmoon.quark.content.building.module;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;
import org.violetmoon.quark.content.building.block.LeafCarpetBlock;
import org.violetmoon.quark.content.world.block.BlossomLeavesBlock;
import org.violetmoon.quark.content.world.module.AncientWoodModule;
import org.violetmoon.quark.content.world.module.BlossomTreesModule;
import org.violetmoon.zeta.client.AlikeColorHandler;
import org.violetmoon.zeta.client.event.load.ZAddBlockColorHandlers;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZLoadComplete;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.LinkedList;
import java.util.List;

@ZetaLoadModule(category = "building", antiOverlap = { "woodworks", "immersive_weathering" })
public class LeafCarpetModule extends ZetaModule {

	public static List<LeafCarpetBlock> carpets = new LinkedList<>();

	@LoadEvent
	public final void register(ZRegister event) {
		for(Wood wood : VanillaWoods.OVERWORLD_WITH_TREE)
			carpet(wood.leaf());

		carpet(Blocks.AZALEA_LEAVES);
		carpet(Blocks.FLOWERING_AZALEA_LEAVES);
	}

	@LoadEvent
	public void postRegister(ZRegister.Post e) {
		BlossomTreesModule.blossomTrees.stream().map(t -> t.leaves).forEach(this::blossomCarpet);

		carpetBlock(AncientWoodModule.ancient_leaves).setCondition(() -> Quark.ZETA.modules.isEnabled(AncientWoodModule.class));
	}

	@LoadEvent
	public void loadComplete(ZLoadComplete event) {
		event.enqueueWork(() -> {
			for(LeafCarpetBlock c : carpets) {
				if(c.asItem() != null)
					ComposterBlock.COMPOSTABLES.put(c.asItem(), 0.2F);
			}
		});
	}

	private void carpet(Block base) {
		carpetBlock(base);
	}

	private void blossomCarpet(BlossomLeavesBlock base) {
		carpetBlock(base).setCondition(base::isEnabled);
	}

	private LeafCarpetBlock carpetBlock(Block base) {
		LeafCarpetBlock carpet = new LeafCarpetBlock(Quark.ZETA.registryUtil.inherit(base, s -> s.replaceAll("_leaves", "_leaf_carpet")), base, this);
		carpets.add(carpet);
		return carpet;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends LeafCarpetModule {

		@LoadEvent
		public void blockColorHandlers(ZAddBlockColorHandlers event) {
			event.registerNamed(zeta(),b -> new AlikeColorHandler((LeafCarpetBlock) b, LeafCarpetBlock::getBaseState), "leaf_carpet");
		}

		@LoadEvent
		public void itemColorHandlers(ZAddItemColorHandlers event) {
			event.registerNamed(zeta(), i -> new AlikeColorHandler(i, LeafCarpetBlock::getBaseState), "leaf_carpet");
		}

	}

}
