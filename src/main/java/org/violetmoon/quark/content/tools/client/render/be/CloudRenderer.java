package org.violetmoon.quark.content.tools.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.catnip.animation.AnimationTickHolder;
import org.violetmoon.quark.content.tools.block.be.CloudBlockEntity;
import org.violetmoon.quark.content.tools.module.BottledCloudModule;

public class CloudRenderer implements BlockEntityRenderer<CloudBlockEntity> {

	public CloudRenderer(BlockEntityRendererProvider.Context context) {
		// NO-OP
	}

	@Override
	public void render(CloudBlockEntity te, float partialTicks, @NotNull PoseStack matrix, @NotNull MultiBufferSource buffer, int light, int overlay) {
		float wobble = (float) Math.sin((AnimationTickHolder.getTicks() + partialTicks) * 0.2f) * -10f;
		float scale = (te.liveTime - partialTicks + wobble) / 200f;

		if (scale > 0) {
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(scale, scale, scale);
			matrix.translate(-0.5, -0.5, -0.5);

			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getModelManager().getModel(BottledCloudModule.Client.MODEL);
			VertexConsumer vc = buffer.getBuffer(Sheets.translucentCullBlockSheet());
			mc.getBlockRenderer().getModelRenderer().renderModel(matrix.last(), vc, null, model,
					1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
		}
	}

}
