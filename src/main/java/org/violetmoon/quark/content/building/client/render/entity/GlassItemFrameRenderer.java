package org.violetmoon.quark.content.building.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.entity.GlassItemFrame;
import org.violetmoon.quark.content.building.entity.GlassItemFrame.SignAttachment;
import org.violetmoon.quark.content.building.module.GlassItemFrameModule;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorEntityRenderDispatcher;

import java.util.List;

/**
 * @author WireSegal
 *         Created at 11:58 AM on 8/25/19.
 */

public class GlassItemFrameRenderer extends EntityRenderer<GlassItemFrame> {

	private static final ModelResourceLocation LOCATION_MODEL = new ModelResourceLocation(Quark.asResource("extra/glass_item_frame"), "standalone");

	private static final List<Direction> SIGN_DIRECTIONS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

	private static final BannerBlockEntity banner = new BannerBlockEntity(BlockPos.ZERO, Blocks.WHITE_BANNER.defaultBlockState());
	private final ModelPart bannerModel;

	private final Minecraft mc = Minecraft.getInstance();
	private final ItemRenderer itemRenderer;
	private final ItemFrameRenderer<?> defaultRenderer;

	public GlassItemFrameRenderer(EntityRendererProvider.Context context) {
		super(context);

		ModelPart part = context.bakeLayer(ModelLayers.BANNER);
		this.bannerModel = part.getChild("flag");

		Minecraft mc = Minecraft.getInstance();
		this.itemRenderer = mc.getItemRenderer();
		this.defaultRenderer = (ItemFrameRenderer<?>) ((AccessorEntityRenderDispatcher)mc.getEntityRenderDispatcher()).getRenderers().get(EntityType.ITEM_FRAME);
	}

	@Override
	public void render(@NotNull GlassItemFrame frame, float yaw, float partialTicks, @NotNull PoseStack matrix, @NotNull MultiBufferSource buffer, int light) {
		super.render(frame, yaw, partialTicks, matrix, buffer, light);
		matrix.pushPose();
		Direction direction = frame.getDirection();
		Vec3 Vector3d = this.getRenderOffset(frame, partialTicks);
		matrix.translate(-Vector3d.x(), -Vector3d.y(), -Vector3d.z());
		matrix.translate((double) direction.getStepX() * 0.46875D, (double) direction.getStepY() * 0.46875D, (double) direction.getStepZ() * 0.46875D);
		matrix.mulPose(Axis.XP.rotationDegrees(frame.getXRot()));
		matrix.mulPose(Axis.YP.rotationDegrees(180.0F - frame.getYRot()));
		BlockRenderDispatcher blockrendererdispatcher = this.mc.getBlockRenderer();
		ModelManager modelmanager = blockrendererdispatcher.getBlockModelShaper().getModelManager();

		ItemStack itemstack = frame.getItem();

		if(frame.getEntityData().get(GlassItemFrame.IS_SHINY))
			light = 0xF000F0;

		if(itemstack.isEmpty()) {
			matrix.pushPose();
			matrix.translate(-0.5D, -0.5D, -0.5D);
			blockrendererdispatcher.getModelRenderer().renderModel(matrix.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()), null, modelmanager.getModel(LOCATION_MODEL), 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
			matrix.popPose();
		} else {
			renderItemStack(frame, matrix, buffer, light, itemstack);
		}

		matrix.popPose();
	}

	@NotNull
	@Override
	public Vec3 getRenderOffset(GlassItemFrame frame, float partialTicks) {
		return new Vec3((float) frame.getDirection().getStepX() * 0.3F, -0.25D, (float) frame.getDirection().getStepZ() * 0.3F);
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull GlassItemFrame frame) {
		return InventoryMenu.BLOCK_ATLAS;
	}

	@Override
	protected boolean shouldShowName(@NotNull GlassItemFrame frame) {
		if(Minecraft.renderNames() && !frame.getItem().isEmpty() && frame.getItem().has(DataComponents.CUSTOM_NAME) && this.entityRenderDispatcher.crosshairPickEntity == frame) {
			double d0 = this.entityRenderDispatcher.distanceToSqr(frame);
			float f = frame.isDiscrete() ? 32.0F : 64.0F;
			return d0 < (double) (f * f);
		} else {
			return false;
		}
	}

	@Override
	protected void renderNameTag(GlassItemFrame frame, Component text, PoseStack matrix, MultiBufferSource buffer, int light, float partialTick) {
		super.renderNameTag(frame, frame.getItem().getHoverName(), matrix, buffer, light, partialTick);
	}

	protected void renderItemStack(GlassItemFrame itemFrame, PoseStack matrix, MultiBufferSource buff, int light, ItemStack stack) {
		if(!stack.isEmpty()) {
			matrix.pushPose();
			MapItemSavedData mapdata = MapItem.getSavedData(stack, itemFrame.level());

			if(itemFrame.isOnSign()) {
				SignAttachment attach = itemFrame.getSignAttachment();

				Direction ourDirection = itemFrame.getDirection().getOpposite();
				int signRotation = itemFrame.getOnSignRotation();

				int ourRotation = SIGN_DIRECTIONS.indexOf(ourDirection) * 4;
				int rotation = signRotation - ourRotation;
				float angle = -rotation * 22.5F;
				float scale = 0.32F;

				switch(attach) {
					case STANDING_BEHIND -> angle += 180F;
					case WALL_SIGN -> {
						angle = 0;
						matrix.translate(0.0, -0.3, 0.44);
					}
					case HANGING_FROM_WALL -> {
						angle = 0;
						matrix.translate(0.0, -0.52, -0.02);
					}
				}

				matrix.translate(0, 0.35, 0.98);
				matrix.scale(scale, scale, scale);
				matrix.mulPose(Axis.YP.rotationDegrees(angle));

				switch(attach) {
					case HANGING_IN_FRONT -> matrix.translate(0.0, -0.52 / scale, -0.075);
					case HANGING_BEHIND -> matrix.translate(0.0, -0.52 / scale, 0.3);
				}

				matrix.translate(0, 0, -0.5);
				matrix.translate(0, 0, -0.085);
			}

			int rotation = mapdata != null ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
			matrix.mulPose(Axis.ZP.rotationDegrees((float) rotation * 360.0F / 8.0F));

			if(!NeoForge.EVENT_BUS.post(new RenderItemInFrameEvent(itemFrame, defaultRenderer, matrix, buff, light)).isCanceled()) {
				if(mapdata != null) {
					matrix.mulPose(Axis.ZP.rotationDegrees(180.0F));
					matrix.scale(0.0078125F, 0.0078125F, 0.0078125F);
					matrix.translate(-64.0F, -64.0F, 62.5F); // <- Use 62.5 instead of 64 to prevent z-fighting

					MapId mapID = stack.get(DataComponents.MAP_ID);
					this.mc.gameRenderer.getMapRenderer().render(matrix, buff, mapID, mapdata, true, light);
				} else {
					float s = (float) GlassItemFrameModule.itemRenderScale;
					if(stack.getItem() instanceof BannerItem bannerItem) {
						banner.fromItem(stack, bannerItem.getColor());
						BannerPatternLayers patterns = banner.getPatterns();

						matrix.pushPose();
						matrix.translate(0.0001F, -0.5001F, 0.55F);
						matrix.scale(0.799999F, 0.399999F, 0.5F);
						BannerRenderer.renderPatterns(matrix, buff, light, OverlayTexture.NO_OVERLAY, bannerModel, ModelBakery.BANNER_BASE, true, bannerItem.getColor(), patterns);
						matrix.popPose();
					} else {
						if(stack.getItem() instanceof ShieldItem) {
							s *= 2.66666667F;
							matrix.translate(-0.25F, 0F, 0.5F);
							matrix.scale(s, s, s);
						} else {
							matrix.translate(0F, 0F, 0.475F);
							matrix.scale(s, s, s);
						}
						matrix.scale(0.5F, 0.5F, 0.5F);
						this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, matrix, buff, mc.level, 0);
					}
				}
			}

			matrix.popPose();
		}
	}
}
