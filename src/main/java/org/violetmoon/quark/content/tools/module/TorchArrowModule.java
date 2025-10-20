package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.client.render.entity.TorchArrowRenderer;
import org.violetmoon.quark.content.tools.entity.TorchArrow;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.item.ZetaArrowItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tools")
public class TorchArrowModule extends ZetaModule {

	@Config
	public static boolean extinguishOnMiss = false;

	public static EntityType<TorchArrow> torchArrowType;

	@Hint
	public static Item torch_arrow;

	public static final TagKey<Item> ignoreMultishot = Quark.asTagKey(Registries.ITEM,"ignores_multishot");

	@LoadEvent
	public final void register(ZRegister event) {
		torch_arrow = new ZetaArrowItem.Impl("torch_arrow", this, TorchArrow::new, TorchArrow::new);

		torchArrowType = EntityType.Builder.<TorchArrow>of(TorchArrow::new, MobCategory.MISC)
				.sized(0.5F, 0.5F)
				.clientTrackingRange(4)
				.updateInterval(20)
				.build("torch_arrow");

		event.getRegistry().register(torchArrowType, "torch_arrow", Registries.ENTITY_TYPE);
		DispenserBlock.registerBehavior(torch_arrow, new ProjectileDispenseBehavior(torch_arrow));
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends TorchArrowModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(torchArrowType, TorchArrowRenderer::new);
		}
	}
}
