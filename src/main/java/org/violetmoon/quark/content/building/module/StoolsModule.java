package org.violetmoon.quark.content.building.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.block.StoolBlock;
import org.violetmoon.quark.content.building.client.render.entity.StoolEntityRenderer;
import org.violetmoon.quark.content.building.entity.Stool;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class StoolsModule extends ZetaModule {
	public static List<StoolBlock> stools = new ArrayList<>();

	public static EntityType<Stool> stoolEntity;

	@Hint
	TagKey<Item> stoolsTag;

	@LoadEvent
	public final void register(ZRegister event) {

		CreativeTabManager.startChain(CreativeModeTabs.COLORED_BLOCKS, false, false, Blocks.PINK_BED);
		for(DyeColor dye : MiscUtil.CREATIVE_COLOR_ORDER)
			stools.add(new StoolBlock(this, dye));
		CreativeTabManager.endChain();

        CreativeTabManager.startChain(CreativeModeTabs.FUNCTIONAL_BLOCKS, false, false, Blocks.PINK_BED);
        for(StoolBlock s : stools)
			s.setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, Blocks.PINK_BED, false);
		CreativeTabManager.endChain();

		stoolEntity = EntityType.Builder.of(Stool::new, MobCategory.MISC)
				.sized(6F / 16F, 0.5F)
				.clientTrackingRange(3)
				.updateInterval(Integer.MAX_VALUE) // update interval
				.setShouldReceiveVelocityUpdates(false)
				.build("stool");
		Quark.ZETA.registry.register(stoolEntity, "stool", Registries.ENTITY_TYPE);
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		stoolsTag = Quark.asTagKey(Registries.ITEM, "stools");
	}

	@PlayEvent
	public void itemUsed(ZRightClickBlock event) {
		if(event.getEntity().isShiftKeyDown() && event.getItemStack().getItem() instanceof BlockItem && event.getFace() == Direction.UP) {
			BlockState state = event.getLevel().getBlockState(event.getPos());
			if(state.getBlock() instanceof StoolBlock stool)
				stool.blockClicked(event.getLevel(), event.getPos());
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends StoolsModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(stoolEntity, StoolEntityRenderer::new);
		}
	}
}
