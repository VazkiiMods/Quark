package org.violetmoon.quark.content.mobs.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.layer.CrabMoldLayer;
import org.violetmoon.quark.content.mobs.client.model.CrabModel;
import org.violetmoon.quark.content.mobs.entity.Crab;

public class CrabRenderer extends MobRenderer<Crab, CrabModel> {

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			Quark.asResource("textures/model/entity/crab/red.png"),
			Quark.asResource("textures/model/entity/crab/blue.png"),
			Quark.asResource("textures/model/entity/crab/green.png")
	};

	public CrabRenderer(EntityRendererProvider.Context context) {
		super(context, ModelHandler.model(ModelHandler.crab), 0.4F);
		addLayer(new CrabMoldLayer(this));
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull Crab entity) {
		return TEXTURES[entity.getVariant() % TEXTURES.length];
	}
}
