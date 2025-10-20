/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 13:31 AM (EST)]
 */
package org.violetmoon.quark.content.mobs.client.layer.shiba;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.model.ShibaModel;
import org.violetmoon.quark.content.mobs.entity.Shiba;

public class ShibaCollarLayer extends RenderLayer<Shiba, ShibaModel> {

	private static final ResourceLocation WOLF_COLLAR = Quark.asResource("textures/model/entity/shiba/collar.png");

	public ShibaCollarLayer(RenderLayerParent<Shiba, ShibaModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource buffer, int light, Shiba foxhound, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		if(foxhound.isTame() && !foxhound.isInvisible()) {
			int color = foxhound.getCollarColor().getTextureDiffuseColor();
			renderColoredCutoutModel(getParentModel(), WOLF_COLLAR, matrix, buffer, light, foxhound, color);
		}
	}
}
