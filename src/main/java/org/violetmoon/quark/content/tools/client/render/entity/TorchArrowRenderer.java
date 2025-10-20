package org.violetmoon.quark.content.tools.client.render.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.entity.TorchArrow;

public class TorchArrowRenderer extends ArrowRenderer<TorchArrow> {

	public static final ResourceLocation TORCH_ARROW_LOCATION = Quark.asResource("textures/model/entity/torch_arrow.png");

	public TorchArrowRenderer(EntityRendererProvider.Context p_174399_) {
		super(p_174399_);
	}

	@Override
	public ResourceLocation getTextureLocation(TorchArrow p_116001_) {
		return TORCH_ARROW_LOCATION;
	}

}
