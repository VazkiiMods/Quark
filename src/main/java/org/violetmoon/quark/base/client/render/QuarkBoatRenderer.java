package org.violetmoon.quark.base.client.render;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.base.item.boat.IQuarkBoat;

import java.util.Map;

public class QuarkBoatRenderer extends EntityRenderer<Boat> {

	private record BoatModelTuple(ResourceLocation resloc, BoatModel model) {
	}

	private final Map<String, BoatModelTuple> boatResources;

	public QuarkBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
		super(context);
		this.shadowRadius = 0.8F;
		boatResources = computeBoatResources(chest, context);
	}

	private static Map<String, BoatModelTuple> computeBoatResources(boolean chest, EntityRendererProvider.Context context) {
		return WoodSetHandler.boatTypes().collect(ImmutableMap.toImmutableMap(Functions.identity(), name -> {
			String folder = chest ? "chest_boat" : "boat";
			ResourceLocation texture = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/" + folder + "/" + name + ".png");
			BoatModel model = (chest) ? new ChestBoatModel(context.bakeLayer(ModelHandler.quark_boat_chest)) : new BoatModel(context.bakeLayer(ModelHandler.quark_boat));

			return new BoatModelTuple(texture, model);
		}));
	}

	// All BoatRenderer copy from here on out =====================================================================================================================

	@Override
	public void render(Boat boat, float yaw, float partialTicks, PoseStack matrix, @NotNull MultiBufferSource buffer, int light) {
		matrix.pushPose();
		matrix.translate(0.0D, 0.375D, 0.0D);
		matrix.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
		float wiggleAngle = (float) boat.getHurtTime() - partialTicks;
		float wiggleMagnitude = boat.getDamage() - partialTicks;
		if(wiggleMagnitude < 0.0F) {
			wiggleMagnitude = 0.0F;
		}

		if(wiggleAngle > 0.0F) {
			matrix.mulPose(Axis.XP.rotationDegrees(Mth.sin(wiggleAngle) * wiggleAngle * wiggleMagnitude / 10.0F * (float) boat.getHurtDir()));
		}

		float f2 = boat.getBubbleAngle(partialTicks);
		if(!Mth.equal(f2, 0.0F)) {
			matrix.mulPose((new Quaternionf()).setAngleAxis(boat.getBubbleAngle(partialTicks) * 0.017453292F, 1.0F,
					0.0F, 1.0F));
		}

		BoatModelTuple tuple = getModelWithLocation(boat);
		ResourceLocation loc = tuple.resloc();
		BoatModel model = tuple.model();

		matrix.scale(-1.0F, -1.0F, 1.0F);
		matrix.mulPose(Axis.YP.rotationDegrees(90.0F));
		model.setupAnim(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
		VertexConsumer vertexconsumer = buffer.getBuffer(model.renderType(loc));
		model.renderToBuffer(matrix, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		if(!boat.isUnderWater()) {
			VertexConsumer waterMask = buffer.getBuffer(RenderType.waterMask());
			model.waterPatch().render(matrix, waterMask, light, OverlayTexture.NO_OVERLAY);
		}

		matrix.popPose();
		super.render(boat, yaw, partialTicks, matrix, buffer, light);
	}

	@NotNull
	@Override
	@Deprecated // forge: override getModelWithLocation to change the texture / model
	public ResourceLocation getTextureLocation(@NotNull Boat boat) {
		return getModelWithLocation(boat).resloc();
	}

	public BoatModelTuple getModelWithLocation(Boat boat) {
		return this.boatResources.get(((IQuarkBoat) boat).getQuarkBoatTypeObj().name());
	}

}
