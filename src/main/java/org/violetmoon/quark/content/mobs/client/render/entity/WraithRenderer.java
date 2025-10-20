package org.violetmoon.quark.content.mobs.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.model.WraithModel;
import org.violetmoon.quark.content.mobs.entity.Wraith;

public class WraithRenderer extends MobRenderer<Wraith, WraithModel> {

	private static final ResourceLocation TEXTURE = Quark.asResource("textures/model/entity/wraith.png");

	public WraithRenderer(EntityRendererProvider.Context context) {
		super(context, ModelHandler.model(ModelHandler.wraith), 0F);
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull Wraith entity) {
		return TEXTURE;
	}

}
