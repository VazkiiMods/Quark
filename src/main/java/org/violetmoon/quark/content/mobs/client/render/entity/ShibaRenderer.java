package org.violetmoon.quark.content.mobs.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.layer.shiba.ShibaArmorLayer;
import org.violetmoon.quark.content.mobs.client.layer.shiba.ShibaCollarLayer;
import org.violetmoon.quark.content.mobs.client.layer.shiba.ShibaMouthItemLayer;
import org.violetmoon.quark.content.mobs.client.model.ShibaModel;
import org.violetmoon.quark.content.mobs.entity.Shiba;

public class ShibaRenderer extends MobRenderer<Shiba, ShibaModel> {

	private static final ResourceLocation[] SHIBA_BASES = {
			Quark.asResource("textures/model/entity/shiba/shiba0.png"),
			Quark.asResource("textures/model/entity/shiba/shiba1.png"),
			Quark.asResource("textures/model/entity/shiba/shiba2.png")
	};

	private static final ResourceLocation SHIBA_RARE = Quark.asResource("textures/model/entity/shiba/shiba_rare.png");
	private static final ResourceLocation SHIBA_DOGE = Quark.asResource("textures/model/entity/shiba/shiba_doge.png");

	public ShibaRenderer(EntityRendererProvider.Context context) {
		super(context, ModelHandler.model(ModelHandler.shiba), 0.5F);
		addLayer(new ShibaCollarLayer(this));
		addLayer(new ShibaMouthItemLayer(this, context.getItemInHandRenderer()));
        addLayer(new ShibaArmorLayer(this, context.getModelSet()));
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(Shiba entity) {
		if(entity.hasCustomName() && entity.getCustomName().getString().trim().equalsIgnoreCase("doge"))
			return SHIBA_DOGE;

		long least = Math.abs(entity.getUUID().getLeastSignificantBits());
		if((least % 200) == 0)
			return SHIBA_RARE;

		int type = (int) (least % SHIBA_BASES.length);
		return SHIBA_BASES[type];
	}

}
