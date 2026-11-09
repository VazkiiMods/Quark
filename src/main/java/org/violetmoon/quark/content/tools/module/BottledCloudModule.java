package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.block.CloudBlock;
import org.violetmoon.quark.content.tools.block.be.CloudBlockEntity;
import org.violetmoon.quark.content.tools.client.render.be.CloudRenderer;
import org.violetmoon.quark.content.tools.item.BottledCloudItem;
import org.violetmoon.zeta.client.event.load.ZAddModels;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tools")
public class BottledCloudModule extends ZetaModule {

	public static BlockEntityType<CloudBlockEntity> blockEntityType;
	public static Block cloud;
	@Hint
	public static Item bottled_cloud;

	@Config
	public static int cloudLevelBottom = 191;

	@Config
	public static int cloudLevelTop = 196;

	@LoadEvent
	public final void register(ZRegister event) {
		cloud = new CloudBlock(this);
		bottled_cloud = new BottledCloudItem(this);

		blockEntityType = BlockEntityType.Builder.of(CloudBlockEntity::new, cloud).build(null);
		Quark.ZETA.registry.register(blockEntityType, "cloud", Registries.BLOCK_ENTITY_TYPE);
	}

	@PlayEvent
	public void onRightClick(ZPlayerInteract.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		Player player = event.getEntity();
		if(stack.getItem() == Items.GLASS_BOTTLE && player.getY() > cloudLevelBottom && player.getY() < cloudLevelTop) {
			stack.shrink(1);

			ItemStack returnStack = new ItemStack(bottled_cloud);
			if(!player.addItem(returnStack))
				player.drop(returnStack, false);

			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends BottledCloudModule {

		public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(Quark.asResource("extra/cloud"));

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			BlockEntityRenderers.register(blockEntityType, CloudRenderer::new);
		}

		@LoadEvent
		public void registerAdditionalModels(ZAddModels event) {
			event.register(MODEL);
		}

	}

}
