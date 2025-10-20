package org.violetmoon.quark.content.mobs.client.layer.forgotten;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;

public class ForgottenClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {

	private static final ResourceLocation TEXTURE = Quark.asResource("textures/model/entity/forgotten/overlay.png");
	private final SkeletonModel<T> layerModel;

	public ForgottenClothingLayer(RenderLayerParent<T, M> parent, EntityModelSet model) {
		super(parent);
		layerModel = new SkeletonModel<>(model.bakeLayer(ModelLayers.STRAY_OUTER_LAYER));
	}

	@Override
	public void render(@NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn, @NotNull T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		coloredCutoutModelCopyLayerRender(getParentModel(), layerModel, TEXTURE, matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, 1);
	}
}
