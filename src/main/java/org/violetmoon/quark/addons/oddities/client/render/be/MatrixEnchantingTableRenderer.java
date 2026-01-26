package org.violetmoon.quark.addons.oddities.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import org.violetmoon.quark.base.util.RotationHelper;

public class MatrixEnchantingTableRenderer implements BlockEntityRenderer<MatrixEnchantingTableBlockEntity> {

	public static final Material TEXTURE_BOOK = EnchantTableRenderer.BOOK_LOCATION;
	private final BookModel modelBook;

	public MatrixEnchantingTableRenderer(BlockEntityRendererProvider.Context context) {
		modelBook = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}

	@Override
	public void render(MatrixEnchantingTableBlockEntity te, float partialTicks, @NotNull PoseStack matrix, @NotNull MultiBufferSource buffer, int light, int overlay) {
		float time = te.tickCount + partialTicks;


		float f1 = te.bookRotation - te.bookRotationPrev;

        f1 = RotationHelper.wrapRadians(f1);

		float rot = te.bookRotationPrev + f1 * partialTicks;
		float bookOpen = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * partialTicks;

		renderBook(te, time, rot, partialTicks, matrix, buffer, light, overlay);

		ItemStack item = te.getItem(0);
		if(!item.isEmpty())
			renderItem(item, time, bookOpen, rot, matrix, buffer, light, overlay, te.getLevel(), te);
	}

	private void renderItem(ItemStack item, float time, float bookOpen, float rot, PoseStack matrix, MultiBufferSource buffer, int light, int overlay, Level level, MatrixEnchantingTableBlockEntity tileEntityIn) {
		matrix.pushPose();
		matrix.translate(0.5F, 0.8F, 0.5F);
		matrix.scale(0.6F, 0.6F, 0.6F);

		rot *= -180F / (float) Math.PI;
		rot -= 90F;
		rot *= bookOpen;

		matrix.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees(rot)));
		matrix.translate(0, bookOpen * 1.4F, Math.sin(bookOpen * Math.PI));
		matrix.mulPose(Axis.XP.rotationDegrees(-90F * (bookOpen - 1F)));

		float trans = (float) Math.sin(time * 0.06) * bookOpen * 0.2F;
		matrix.translate(0F, trans, 0F);

		ItemRenderer render = Minecraft.getInstance().getItemRenderer();
		render.renderStatic(item, ItemDisplayContext.FIXED, light, overlay, matrix, buffer, level, 0);
		matrix.popPose();
	}

	// Copy of vanilla's book render
	private void renderBook(MatrixEnchantingTableBlockEntity be, float time, float bookRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
		poseStack.translate(0.5D, 0.75D, 0.5D);
		float f = time + partialTicks;
		poseStack.translate(0.0D, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0D);

		float rotationDiff = be.bookRotation - be.bookRotationPrev;

        rotationDiff = RotationHelper.wrapRadians(rotationDiff);

		float f2 = be.bookRotationPrev + rotationDiff * partialTicks;
		poseStack.mulPose(Axis.YP.rotation(-f2));
		poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
		float f3 = Mth.lerp(partialTicks, be.pageFlipPrev, be.pageFlip);
		float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
		float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
		float f6 = Mth.lerp(partialTicks, be.bookSpreadPrev, be.bookSpread);

		this.modelBook.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
		VertexConsumer ivertexbuilder = TEXTURE_BOOK.buffer(bufferSource, RenderType::entitySolid);
		this.modelBook.render(poseStack, ivertexbuilder, packedLight, packedOverlay, -1);
		poseStack.popPose();
	}
}
