package org.violetmoon.quark.content.tools.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.catnip.animation.AnimationTickHolder;
import org.violetmoon.quark.content.tools.block.be.CloudBlockEntity;
import org.violetmoon.quark.content.tools.module.BottledCloudModule;

public class CloudRenderer implements BlockEntityRenderer<CloudBlockEntity> {

	private static final RandomSource RANDOM = RandomSource.create();

	public CloudRenderer(BlockEntityRendererProvider.Context context) {
		// NO-OP
	}

	@Override
	public void render(CloudBlockEntity te, float partialTicks, @NotNull PoseStack matrix, @NotNull MultiBufferSource buffer, int light, int overlay) {
		float wobble = (float) Math.sin((AnimationTickHolder.getTicks() + partialTicks) * 0.2f) * -10f;
		float scale = (te.liveTime - partialTicks + wobble) / CloudBlockEntity.LIFETIME;

		if (scale > 0) {
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(scale, scale, scale);
			matrix.translate(-0.5, -0.5, -0.5);

			Minecraft mc = Minecraft.getInstance();
			Vec3 color = mc.level.getCloudColor(partialTicks);

			BakedModel model = mc.getModelManager().getModel(BottledCloudModule.Client.MODEL);
			VertexConsumer vc = new VertexConsumerWithUpNormal(buffer.getBuffer(Sheets.translucentCullBlockSheet()));
			for (BakedQuad quad : model.getQuads(null, null, RANDOM, ModelData.EMPTY, null)) {

				float shade = getCloudShade(quad);
				vc.putBulkData(matrix.last(), quad, (float) color.x * shade, (float) color.y * shade, (float) color.z * shade, 1f,
						LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			}
		}
	}

	private static float getCloudShade(BakedQuad quad) {
		//vanilla block shade is 1, 0.5, 0.8, 0.6...
		//probably unnecessary but this matches clouds exactly
		return switch (quad.getDirection()) {
			case UP -> 1f;
			case DOWN -> 0.7f;
			case EAST, WEST -> 0.9f;
			case NORTH, SOUTH -> 0.8f;
		};
	}

	//hack to remove normal shade. we use our own
	private record VertexConsumerWithUpNormal(VertexConsumer inner) implements VertexConsumer {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			inner.addVertex(x, y, z);
			return this;
		}
		@Override
		public VertexConsumer setColor(int r, int g, int b, int a) {
			inner.setColor(r, g, b, a);
			return this;
		}
		@Override
		public VertexConsumer setUv(float u, float v) {
			inner.setUv(u, v);
			return this;
		}
		@Override
		public VertexConsumer setUv1(int u, int v) {
			inner.setUv1(u, v);
			return this;
		}
		@Override
		public VertexConsumer setUv2(int u, int v) {
			inner.setUv2(u, v);
			return this;
		}
		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			inner.setNormal(0, 1, 0);
			return this;
		}
	}

}
