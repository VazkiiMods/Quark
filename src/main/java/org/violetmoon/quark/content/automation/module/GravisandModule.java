package org.violetmoon.quark.content.automation.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.block.GravisandBlock;
import org.violetmoon.quark.content.automation.entity.Gravisand;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "automation")
public class GravisandModule extends ZetaModule {

	public static EntityType<Gravisand> gravisandType;

	@Hint public static Block gravisand;

	@LoadEvent
	public final void register(ZRegister event) {
		gravisand = new GravisandBlock("gravisand", this, CreativeModeTab.TAB_REDSTONE, Block.Properties.copy(Blocks.SAND));

		gravisandType = EntityType.Builder.<Gravisand>of(Gravisand::new, MobCategory.MISC)
				.sized(0.98F, 0.98F)
				.clientTrackingRange(10)
				.updateInterval(20) // update interval
				.setCustomClientFactory((spawnEntity, world) -> new Gravisand(gravisandType, world))
				.build("gravisand");
		Quark.ZETA.registry.register(gravisandType, "gravisand", Registry.ENTITY_TYPE_REGISTRY);
	}


	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends GravisandModule {
		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(gravisandType, FallingBlockRenderer::new);
		}
	}
}
