package org.violetmoon.quark.content.mobs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.model.CrabModel;
import org.violetmoon.quark.content.mobs.entity.Crab;

import javax.annotation.Nonnull;

public class CrabMoldLayer extends RenderLayer<Crab, CrabModel> {

	private static final ResourceLocation MOLD_LAYER = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/crab/mold_layer.png");

	public CrabMoldLayer(RenderLayerParent<Crab, CrabModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource buffer, int light, Crab crab, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		if(crab.getVariant() >= Crab.COLORS)
			renderColoredCutoutModel(getParentModel(), MOLD_LAYER, matrix, buffer, light, crab, 1F, 1F, 1F);
	}

}
