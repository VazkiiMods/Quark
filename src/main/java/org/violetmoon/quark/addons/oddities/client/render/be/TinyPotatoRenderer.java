package org.violetmoon.quark.addons.oddities.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import org.violetmoon.quark.addons.oddities.module.TinyPotatoModule;
import org.violetmoon.quark.addons.oddities.util.TinyPotatoInfo;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorModelManager;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class TinyPotatoRenderer implements BlockEntityRenderer<TinyPotatoBlockEntity> {
	public static final String DEFAULT = "default";
	public static final String HALLOWEEN = "halloween";
	public static final String ANGRY = "angry";
	private static final Pattern ESCAPED = Pattern.compile("[^a-z0-9/._-]");
	private final BlockRenderDispatcher blockRenderDispatcher;

	public static boolean isTheSpookDay() {
		Calendar calendar = Calendar.getInstance();

		return calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DAY_OF_MONTH) == 31;
	}

	public TinyPotatoRenderer(BlockEntityRendererProvider.Context ctx) {
		this.blockRenderDispatcher = ctx.getBlockRenderDispatcher();
	}

	public static BakedModel getModelFromDisplayName(Component displayName, boolean angry) {
		TinyPotatoInfo info = TinyPotatoInfo.fromComponent(displayName);
		return getModel(info.name(), angry);
	}

	private static BakedModel getModel(String name, boolean angry) {
		ModelManager bmm = Minecraft.getInstance().getModelManager();
		Map<ResourceLocation, BakedModel> mm = ((AccessorModelManager) bmm).getBakedRegistry();
		BakedModel missing = bmm.getMissingModel();
		ResourceLocation location = taterLocation(name);
		BakedModel model = mm.get(location);
		if(model == null) {
			if(isTheSpookDay()) {
				return mm.getOrDefault(taterLocation(HALLOWEEN), missing);
			} else if(angry) {
				return mm.getOrDefault(taterLocation(ANGRY), missing);
			} else {
				return mm.getOrDefault(taterLocation(DEFAULT), missing);
			}
		}
		return model;
	}

	private static ResourceLocation taterLocation(String name) {
		return Quark.asResource("tiny_potato/" + normalizeName(name));
	}

	private static String normalizeName(String name) {
		return ESCAPED.matcher(name).replaceAll("_").toLowerCase(Locale.ROOT);
	}

	@Override
	public void render(@NotNull TinyPotatoBlockEntity potato, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int light, int overlay) {
		poseStack.pushPose();

		TinyPotatoInfo info = TinyPotatoInfo.fromComponent(potato.name);
		RenderType layer = Sheets.translucentCullBlockSheet();
		BakedModel model = getModel(info.name(), potato.angry);

		poseStack.translate(0.5F, 0F, 0.5F);
		Direction potatoFacing = potato.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
		float rotY = 0;
		switch (potatoFacing) {
			default:
			case SOUTH:
				rotY = 180F;
				break;
			case NORTH:
				break;
			case EAST:
				rotY = 90F;
				break;
			case WEST:
				rotY = 270F;
				break;
		}
		poseStack.mulPose(com.mojang.math.Axis.YN.rotationDegrees(rotY));

		float jump = potato.jumpTicks;
		if(jump > 0) {
			jump -= partialTicks;
		}

		float up = (float) Math.abs(Math.sin(jump / 10 * Math.PI)) * 0.2F;
		float rotZ = (float) Math.sin(jump / 10 * Math.PI) * 2;
		float wiggle = (float) Math.sin(jump / 10 * Math.PI) * 0.05F;

		poseStack.translate(wiggle, up, 0F);
		poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotZ));

		boolean render = !(info.name().equals("mami") || info.name().equals("soaryn") || info.name().equals("eloraam") && jump != 0);
		if(render) {
			poseStack.pushPose();
			poseStack.translate(-0.5F, 0, -0.5F);
			RuneColor runeColor = info.runeColor();
			if(runeColor != null)
				ColorRunesModule.setTargetColor(runeColor);

			VertexConsumer buffer = ItemRenderer.getFoilBuffer(buffers, layer, true, info.enchanted());

			renderModel(poseStack, buffer, light, overlay, model);
			poseStack.popPose();
		}

		poseStack.translate(0F, 1.5F, 0F);
		poseStack.pushPose();
		poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180F));
		renderItems(potato, potatoFacing, poseStack, buffers, light, overlay);
		poseStack.popPose();

		poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-rotZ));
		poseStack.mulPose(com.mojang.math.Axis.YN.rotationDegrees(-rotY));

		renderName(potato, info.name(), poseStack, buffers, light);
		poseStack.popPose();
	}

	private void renderName(TinyPotatoBlockEntity potato, String name, PoseStack ms, MultiBufferSource buffers, int light) {
		Minecraft mc = Minecraft.getInstance();
		HitResult pos = mc.hitResult;

		if(Minecraft.renderNames()
				&& !name.isEmpty() && pos != null && pos.getType() == HitResult.Type.BLOCK
				&& potato.getBlockPos().equals(((BlockHitResult) pos).getBlockPos())) {
			ms.pushPose();
			ms.translate(0F, -0.6F, 0F);
			ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
			float f1 = 0.016666668F * 1.6F;
			ms.scale(-f1, -f1, f1);
			int halfWidth = mc.font.width(potato.name.getString()) / 2;

			float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			int opacityRGB = (int) (opacity * 255.0F) << 24;
			mc.font.drawInBatch(potato.name, -halfWidth, 0, 0x20FFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, opacityRGB, light);
			mc.font.drawInBatch(potato.name, -halfWidth, 0, 0xFFFFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
			if(name.equals("pahimar") || name.equals("soaryn")) {
				ms.translate(0F, 14F, 0F);
				String str = name.equals("pahimar") ? "[WIP]" : "(soon)";
				halfWidth = mc.font.width(str) / 2;

				mc.font.drawInBatch(str, -halfWidth, 0, 0x20FFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, opacityRGB, light);
				mc.font.drawInBatch(str, -halfWidth, 0, 0xFFFFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, light);
			}

			ms.popPose();
		}
	}

	private void renderItems(TinyPotatoBlockEntity potato, Direction facing, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		ms.pushPose();
		ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180F));
		ms.translate(0F, -1F, 0F);
		float s = 1F / 3.5F;
		ms.scale(s, s, s);

		for(int i = 0; i < potato.getContainerSize(); i++) {
			ItemStack stack = potato.getItem(i);
			if(stack.isEmpty()) {
				continue;
			}

			ms.pushPose();
			Direction side = Direction.values()[i];
			if(side.getAxis() != Axis.Y) {
				float sideAngle = side.toYRot() - facing.toYRot();
				side = Direction.fromYRot(sideAngle);
			}

			boolean block = stack.getItem() instanceof BlockItem;
			boolean mySon = stack.getItem() == TinyPotatoModule.tiny_potato.asItem();

			switch(side) {
			case UP -> {
				if(mySon) {
					ms.translate(0F, -0.375F, 0.5F);
				} else if(block) {
					ms.translate(0F, 0.3F, 0.5F);
				}
				ms.translate(0F, -0.5F, -0.4F);
			}
			case DOWN -> {
				ms.translate(0, -2.3F, -0.88F);
				if(mySon) {
					ms.translate(0, 1.25F, 0.5F);
				} else if(block) {
					ms.translate(0, 1, 0.6F);
				}
			}
			case NORTH -> {
				ms.translate(0, -1.9F, 0.02F);
				if(mySon) {
					ms.translate(0, -0.29F, 0.6F);
				} else if(block) {
					ms.translate(0, 1, 0.6F);
				}
			}
			case SOUTH -> {
				ms.translate(0, -1.6F, -0.89F);
				if(mySon) {
					ms.translate(0, -0.59F, 0.26F);
				} else if(block) {
					ms.translate(0, 1.0F, 0.5F);
				}
			}
			case EAST -> {
				if(mySon) {
					ms.translate(-0.35F, -0.29F, -0.06F);
				} else if(block) {
					ms.translate(-0.4F, 0.8F, 0F);
				} else {
					ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90F));
				}
				ms.translate(-0.3F, -1.9F, 0.04F);
			}
			case WEST -> {
				if(mySon) {
					ms.translate(0.95F, -0.29F, 0.9F);
					if(stack.getComponents().has(DataComponents.CUSTOM_NAME)) {
						TinyPotatoInfo info = TinyPotatoInfo.fromComponent(stack.getHoverName());
						if(info.name().equals("kingdaddydmac")) {
							ms.translate(0.55F, 0, 0);
						}
					}
				} else if(block) {
					ms.translate(1F, 0.8F, 1F);
				} else {
					ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90F));
				}
				ms.translate(-0.3F, -1.9F, -0.92F);
			}
			}

			if(mySon) {
				ms.scale(1.1F, 1.1F, 1.1F);
			} else if(block) {
				ms.scale(0.5F, 0.5F, 0.5F);
			}
			if(block && side == Direction.NORTH) {
				ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
			}
			renderItem(ms, buffers, light, overlay, stack);
			ms.popPose();
		}
		ms.popPose();
	}

	private void renderModel(PoseStack ms, VertexConsumer buffer, int light, int overlay, BakedModel model) {
		blockRenderDispatcher.getModelRenderer().renderModel(ms.last(), buffer, null, model, 1, 1, 1, light, overlay);
	}

	private void renderItem(PoseStack ms, MultiBufferSource buffers, int light, int overlay, ItemStack stack) {
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD,
				light, overlay, ms, buffers, Minecraft.getInstance().level, 0);
	}
}
