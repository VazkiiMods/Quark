package org.violetmoon.quark.content.mobs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.model.ToretoiseModel;
import org.violetmoon.quark.content.mobs.entity.Toretoise;

import javax.annotation.Nonnull;

public class ToretoiseOreLayer extends RenderLayer<Toretoise, ToretoiseModel> {

	private static final String ORE_BASE = Quark.MOD_ID + ":textures/model/entity/toretoise/ore%d.png";

	public ToretoiseOreLayer(RenderLayerParent<Toretoise, ToretoiseModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource buffer, int light, Toretoise entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		int ore = entity.getOreType();
		if(ore != 0 && ore <= Toretoise.ORE_TYPES) {
			ResourceLocation res = new ResourceLocation(String.format(ORE_BASE, ore));
			renderColoredCutoutModel(getParentModel(), res, matrix, buffer, light, entity, 1, 1, 1);
		}
	}

}
