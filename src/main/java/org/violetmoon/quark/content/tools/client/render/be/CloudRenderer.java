package org.violetmoon.quark.content.tools.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.content.tools.block.be.CloudBlockEntity;

public class CloudRenderer implements BlockEntityRenderer<CloudBlockEntity> {

	public CloudRenderer(BlockEntityRendererProvider.Context context) {
		// NO-OP
	}

	@Override
	public void render(CloudBlockEntity te, float partialTicks, @NotNull PoseStack matrix, @NotNull MultiBufferSource buffer, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();

		float scale = ((float) (te.liveTime - partialTicks + Math.sin(AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() * 0.2F) * -10F) / 200F) * 0.6F;

		if(scale > 0) {
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(scale, scale, scale);
			mc.getItemRenderer().renderStatic(new ItemStack(Blocks.WHITE_CONCRETE), ItemDisplayContext.NONE, 240, OverlayTexture.NO_OVERLAY, matrix, buffer, mc.level, 0);
		}
	}

}
