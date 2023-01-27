package vazkii.quark.addons.oddities.client.render.be;

import java.util.Iterator;
import java.util.Random;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.quark.addons.oddities.block.be.PipeBlockEntity;
import vazkii.quark.addons.oddities.block.be.PipeBlockEntity.ConnectionType;
import vazkii.quark.addons.oddities.block.be.PipeBlockEntity.PipeItem;
import vazkii.quark.addons.oddities.block.pipe.PipeBlock;
import vazkii.quark.addons.oddities.module.PipesModule;
import vazkii.quark.base.Quark;

public class PipeRenderer implements BlockEntityRenderer<PipeBlockEntity> {

	private static final ModelResourceLocation LOCATION_MODEL = new ModelResourceLocation(new ResourceLocation(Quark.MOD_ID, "pipe_flare"), "inventory");

	private final Random random = new Random();

	public PipeRenderer(BlockEntityRendererProvider.Context context) {

	}

	@Override
	public void render(PipeBlockEntity te, float partialTicks, PoseStack matrix, @Nonnull MultiBufferSource buffer, int light, int overlay) {
		matrix.pushPose();
		matrix.translate(0.5, 0.5, 0.5);
		ItemRenderer render = Minecraft.getInstance().getItemRenderer();
		Iterator<PipeItem> items = te.getItemIterator();

		if(PipesModule.renderPipeItems)
			while(items.hasNext())
				renderItem(items.next(), render, matrix, buffer, partialTicks, light, overlay);

		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelManager modelmanager = blockrendererdispatcher.getBlockModelShaper().getModelManager();
		BakedModel model = modelmanager.getModel(LOCATION_MODEL);
		for(Direction d : Direction.values())
			renderFlare(te, blockrendererdispatcher, model, matrix, buffer, partialTicks, light, overlay, d);

		matrix.popPose();
	}

	private void renderFlare(PipeBlockEntity te, BlockRenderDispatcher disp, BakedModel model, PoseStack matrix, MultiBufferSource buffer, float partial, int light, int overlay, Direction dir) {
		ConnectionType type = te.getConnectionTo(dir);
		if(type.isFlared) {
			matrix.pushPose();
			switch (dir.getAxis()) {
				case X -> matrix.mulPose(Vector3f.YP.rotationDegrees(-dir.toYRot()));
				case Z -> matrix.mulPose(Vector3f.YP.rotationDegrees(dir.toYRot()));
				case Y -> {
					matrix.mulPose(Vector3f.XP.rotationDegrees(90F));
					if (dir == Direction.UP)
						matrix.mulPose(Vector3f.YP.rotationDegrees(180F));
				}
			}

			matrix.translate(-0.5, -0.5, type.getFlareShift(te));
			disp.getModelRenderer().renderModel(matrix.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()), null, model, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
			matrix.popPose();
		}
	}

	private void renderItem(PipeItem item, ItemRenderer render, PoseStack matrix, MultiBufferSource buffer, float partial, int light, int overlay) {
		matrix.pushPose();

		float scale = 0.4F;
		float fract = item.getTimeFract(partial);
		float shiftFract = fract - 0.5F;
		Direction face = item.outgoingFace;
		if(fract < 0.5)
			face = item.incomingFace.getOpposite();

		float offX = (face.getStepX() * 1F);
		float offY = (face.getStepY() * 1F);
		float offZ = (face.getStepZ() * 1F);
		matrix.translate(offX * shiftFract, offY * shiftFract, offZ * shiftFract);

		matrix.scale(scale, scale, scale);

		float speed = 4F;
		matrix.mulPose(Vector3f.YP.rotationDegrees((item.timeInWorld + partial) * speed));

		int seed = item.stack.isEmpty() ? 187 : Item.getId(item.stack.getItem());
		random.setSeed(seed);

		int count = getModelCount(item.stack);
		for(int i = 0; i < count; i++) {
			matrix.pushPose();
			if(i > 0) {
				float spread = 0.15F;
				float x = (this.random.nextFloat() * 2.0F - 1.0F) * spread;
				float y = (this.random.nextFloat() * 2.0F - 1.0F) * spread;
				float z = (this.random.nextFloat() * 2.0F - 1.0F) * spread;
				matrix.translate(x, y, z);
			}

			render.renderStatic(item.stack, ItemTransforms.TransformType.FIXED, light, overlay, matrix, buffer, 0);
			matrix.popPose();
		}
		matrix.popPose();
	}

	// RenderEntityItem copy
	protected int getModelCount(ItemStack stack) {
		if(stack.getCount() > 48)
			return 5;

		if(stack.getCount() > 32)
			return 4;

		if(stack.getCount() > 16)
			return 3;

		if (stack.getCount() > 1)
			return 2;

		return 1;
	}

}
