package vazkii.quark.content.building.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.hint.Hint;
import vazkii.quark.content.building.client.render.entity.GlassItemFrameRenderer;
import vazkii.quark.content.building.entity.GlassItemFrame;
import vazkii.quark.content.building.item.QuarkItemFrameItem;
import vazkii.zeta.event.ZRegister;
import vazkii.zeta.event.bus.LoadEvent;
import vazkii.zeta.event.client.ZAddModels;
import vazkii.zeta.event.client.ZClientSetup;

/**
 * @author WireSegal
 * Created at 11:00 AM on 8/25/19.
 */
@LoadModule(category = "building")
public class GlassItemFrameModule extends QuarkModule {

	@Config public static boolean glassItemFramesUpdateMaps = true;
	@Config(description = "Set to true for faster map updates. Default is every 3s")
	public static boolean glassItemFramesUpdateMapsEveryTick = false;

	@Hint public static Item glassFrame;
	@Hint public static Item glowingGlassFrame;

	public static EntityType<GlassItemFrame> glassFrameEntity;
	
	@Config(description = "The scale at which items render in the Glass Item Frame. To match the vanilla Item Frame size, set to 1.0")
	public static double itemRenderScale = 1.5;

	@LoadEvent
	public final void register(ZRegister event) {
		glassFrameEntity = EntityType.Builder.<GlassItemFrame>of(GlassItemFrame::new, MobCategory.MISC)
				.sized(0.5F, 0.5F)
				.clientTrackingRange(10)
				.updateInterval(Integer.MAX_VALUE) // update interval
				.setShouldReceiveVelocityUpdates(false)
				.setCustomClientFactory((spawnEntity, world) -> new GlassItemFrame(glassFrameEntity, world))
				.build("glass_frame");
		Quark.ZETA.registry.register(glassFrameEntity, "glass_frame", Registry.ENTITY_TYPE_REGISTRY);

		glassFrame = new QuarkItemFrameItem("glass_item_frame", this, GlassItemFrame::new);
		glowingGlassFrame = new QuarkItemFrameItem("glowing_glass_item_frame", this, 
				(w, p, d) -> {
					GlassItemFrame e = new GlassItemFrame(w, p, d);
					e.getEntityData().set(GlassItemFrame.IS_SHINY, true);
					return e;
				});
	}

	@LoadEvent
	public final void clientSetup(ZClientSetup event) {
		EntityRenderers.register(glassFrameEntity, GlassItemFrameRenderer::new);
	}

	@LoadEvent
	@OnlyIn(Dist.CLIENT)	
	public void registerAdditionalModels(ZAddModels event) {
		event.register(new ModelResourceLocation(Quark.MOD_ID, "extra/glass_item_frame", "inventory"));
	}
}
