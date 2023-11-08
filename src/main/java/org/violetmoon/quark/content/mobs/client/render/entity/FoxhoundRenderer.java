/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 13:30 AM (EST)]
 */
package org.violetmoon.quark.content.mobs.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.layer.FoxhoundCollarLayer;
import org.violetmoon.quark.content.mobs.client.model.FoxhoundModel;
import org.violetmoon.quark.content.mobs.entity.Foxhound;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FoxhoundRenderer extends MobRenderer<Foxhound, FoxhoundModel> {

	private static final ResourceLocation FOXHOUND_IDLE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/red/idle.png");
	private static final ResourceLocation FOXHOUND_HOSTILE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/red/hostile.png");
	private static final ResourceLocation FOXHOUND_SLEEPING = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/red/sleeping.png");

	private static final ResourceLocation SOULHOUND_IDLE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/blue/idle.png");
	private static final ResourceLocation SOULHOUND_HOSTILE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/blue/hostile.png");
	private static final ResourceLocation SOULHOUND_SLEEPING = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/blue/sleeping.png");

	private static final ResourceLocation BASALT_FOXHOUND_IDLE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/black/idle.png");
	private static final ResourceLocation BASALT_FOXHOUND_HOSTILE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/black/hostile.png");
	private static final ResourceLocation BASALT_FOXHOUND_SLEEPING = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/black/sleeping.png");

	private static final int SHINY_CHANCE = 256;

	public FoxhoundRenderer(EntityRendererProvider.Context context) {
		super(context, ModelHandler.model(ModelHandler.foxhound), 0.5F);
		addLayer(new FoxhoundCollarLayer(this));
	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull Foxhound entity) {
		if(entity.isBlue())
			return entity.isSleeping() ? SOULHOUND_SLEEPING : (entity.getRemainingPersistentAngerTime() > 0 ? SOULHOUND_HOSTILE : SOULHOUND_IDLE);

		UUID id = entity.getUUID();
		long most = id.getMostSignificantBits();
		if(SHINY_CHANCE > 0 && (most % SHINY_CHANCE) == 0)
			return entity.isSleeping() ? BASALT_FOXHOUND_SLEEPING : (entity.getRemainingPersistentAngerTime() > 0 ? BASALT_FOXHOUND_HOSTILE : BASALT_FOXHOUND_IDLE);

		return entity.isSleeping() ? FOXHOUND_SLEEPING : (entity.getRemainingPersistentAngerTime() > 0 ? FOXHOUND_HOSTILE : FOXHOUND_IDLE);
	}
}
