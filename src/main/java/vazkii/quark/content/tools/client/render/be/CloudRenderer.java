package vazkii.quark.content.tools.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import vazkii.quark.base.QuarkClient;
import vazkii.quark.content.tools.block.be.CloudBlockEntity;

import javax.annotation.Nonnull;

public class CloudRenderer implements BlockEntityRenderer<CloudBlockEntity> {

	public CloudRenderer(BlockEntityRendererProvider.Context context) {
		// NO-OP
	}

	@Override
	public void render(CloudBlockEntity te, float partialTicks, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource buffer, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();

		float scale = ((float) (te.liveTime - partialTicks + Math.sin(QuarkClient.ticker.total * 0.2F) * -10F) / 200F) * 0.6F;

		if(scale > 0) {
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(scale, scale, scale);
			mc.getItemRenderer().renderStatic(new ItemStack(Blocks.WHITE_CONCRETE), TransformType.NONE, 240, OverlayTexture.NO_OVERLAY, matrix, buffer, 0);
		}
	}

}
