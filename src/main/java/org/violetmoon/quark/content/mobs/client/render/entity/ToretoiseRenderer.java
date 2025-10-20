package org.violetmoon.quark.content.mobs.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.layer.ToretoiseOreLayer;
import org.violetmoon.quark.content.mobs.client.model.ToretoiseModel;
import org.violetmoon.quark.content.mobs.entity.Toretoise;

public class ToretoiseRenderer extends MobRenderer<Toretoise, ToretoiseModel> {

	private static final ResourceLocation BASE_TEXTURE = Quark.asResource("textures/model/entity/toretoise/base.png");

	public ToretoiseRenderer(EntityRendererProvider.Context context) {
		super(context, ModelHandler.model(ModelHandler.toretoise), 1F);
		addLayer(new ToretoiseOreLayer(this));
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull Toretoise entity) {
		return BASE_TEXTURE;
	}

}
