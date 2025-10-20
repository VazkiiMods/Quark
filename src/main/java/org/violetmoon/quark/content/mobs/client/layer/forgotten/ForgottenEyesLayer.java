package org.violetmoon.quark.content.mobs.client.layer.forgotten;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Skeleton;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;

public class ForgottenEyesLayer<T extends Skeleton, M extends SkeletonModel<T>> extends EyesLayer<T, M> {

	private static final ResourceLocation TEXTURE = Quark.asResource("textures/model/entity/forgotten/eye.png");
	private static final RenderType RENDER_TYPE = RenderType.eyes(TEXTURE);

	public ForgottenEyesLayer(RenderLayerParent<T, M> rendererIn) {
		super(rendererIn);
	}

	@NotNull
	@Override
	public RenderType renderType() {
		return RENDER_TYPE;
	}
}
