package org.violetmoon.quark.content.mobs.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.content.mobs.entity.Toretoise;

import java.util.function.BiConsumer;

public class ToretoiseModel extends EntityModel<Toretoise> {

	private Toretoise entity;
	private float animFrames;

	public ModelPart body;
	public ModelPart head;
	public ModelPart rightFrontLeg;
	public ModelPart leftFrontLeg;
	public ModelPart rightBackLeg;
	public ModelPart leftBackLeg;
	public ModelPart mouth;

	public ModelPart CoalOre1;
	public ModelPart CoalOre2;
	public ModelPart CoalOre3;
	public ModelPart CoalOre4;
	public ModelPart IronOre1;
	public ModelPart IronOre2;
	public ModelPart IronOre3;
	public ModelPart LapisOre1;
	public ModelPart LapisOre2;
	public ModelPart LapisOre3;
	public ModelPart LapisOre4;
	public ModelPart RedstoneOre1;
	public ModelPart RedstoneOre2;
	public ModelPart RedstoneOre3;
	public ModelPart RedstoneOre4;
	public ModelPart RedstoneOre5;

	public ToretoiseModel(ModelPart root) {
		body = root.getChild("body");
		head = root.getChild("head");
		rightFrontLeg = root.getChild("rightFrontLeg");
		leftFrontLeg = root.getChild("leftFrontLeg");
		rightBackLeg = root.getChild("rightBackLeg");
		leftBackLeg = root.getChild("leftBackLeg");
		mouth = head.getChild("mouth");

		CoalOre1 = body.getChild("CoalOre1");
		CoalOre2 = body.getChild("CoalOre2");
		CoalOre3 = body.getChild("CoalOre3");
		CoalOre4 = body.getChild("CoalOre4");
		IronOre1 = body.getChild("IronOre1");
		IronOre2 = body.getChild("IronOre2");
		IronOre3 = body.getChild("IronOre3");
		LapisOre1 = body.getChild("LapisOre1");
		LapisOre2 = body.getChild("LapisOre2");
		LapisOre3 = body.getChild("LapisOre3");
		LapisOre4 = body.getChild("LapisOre4");
		RedstoneOre1 = body.getChild("RedstoneOre1");
		RedstoneOre2 = body.getChild("RedstoneOre2");
		RedstoneOre3 = body.getChild("RedstoneOre3");
		RedstoneOre4 = body.getChild("RedstoneOre4");
		RedstoneOre5 = body.getChild("RedstoneOre5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-11.0F, 0.0F, -13.0F, 22, 12, 26),
				PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 38)
						.addBox(-4.0F, -4.0F, -8.0F, 8, 5, 8),
				PartPose.offset(0.0F, 16.0F, -13.0F));

		head.addOrReplaceChild("mouth",
				CubeListBuilder.create()
						.texOffs(66, 38)
						.addBox(-4.5F, -2.5F, -8.0F, 9, 4, 8),
				PartPose.offset(0.0F, 1.0F, -1.0F));

		root.addOrReplaceChild("leftFrontLeg",
				CubeListBuilder.create()
						.mirror()
						.texOffs(34, 38)
						.addBox(-4.0F, -2.0F, -4.0F, 8, 10, 8),
				PartPose.offsetAndRotation(10.0F, 16.0F, -12.0F, 0.0F, -0.7853981633974483F, 0.0F));

		root.addOrReplaceChild("rightBackLeg",
				CubeListBuilder.create()
						.texOffs(34, 38)
						.addBox(-4.0F, -2.0F, -4.0F, 8, 10, 8),
				PartPose.offsetAndRotation(-10.0F, 16.0F, 12.0F, 0.0F, 0.7853981633974483F, 0.0F));

		root.addOrReplaceChild("rightFrontLeg",
				CubeListBuilder.create()
						.texOffs(34, 38)
						.addBox(-4.0F, -2.0F, -4.0F, 8, 10, 8),
				PartPose.offsetAndRotation(-10.0F, 16.0F, -12.0F, 0.0F, 0.7853981633974483F, 0.0F));

		root.addOrReplaceChild("leftBackLeg",
				CubeListBuilder.create()
						.mirror()
						.texOffs(34, 38)
						.addBox(-4.0F, -2.0F, -4.0F, 8, 10, 8),
				PartPose.offsetAndRotation(10.0F, 16.0F, 12.0F, 0.0F, -0.7853981633974483F, 0.0F));

		body.addOrReplaceChild("CoalOre1",
				CubeListBuilder.create()
						.texOffs(36, 56)
						.addBox(0.0F, -7.0F, -6.0F, 3, 3, 3),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("CoalOre2",
				CubeListBuilder.create()
						.texOffs(42, 56)
						.addBox(7.0F, -2.0F, -10.0F, 6, 6, 6),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("CoalOre3",
				CubeListBuilder.create()
						.texOffs(66, 50)
						.addBox(-2.0F, -7.0F, -4.0F, 7, 7, 7),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("CoalOre4",
				CubeListBuilder.create()
						.texOffs(60, 64)
						.addBox(-15.0F, 0.0F, 1.0F, 4, 6, 6),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("IronOre1",
				CubeListBuilder.create()
						.texOffs(36, 89)
						.addBox(1.0F, -3.0F, 1.0F, 8, 3, 8),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("IronOre2",
				CubeListBuilder.create()
						.texOffs(32, 81)
						.addBox(-7.0F, -2.0F, -11.0F, 6, 2, 6),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("IronOre3",
				CubeListBuilder.create()
						.texOffs(30, 76)
						.addBox(-9.0F, -1.0F, 6.0F, 4, 1, 4),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("LapisOre1",
				CubeListBuilder.create()
						.texOffs(0, 51)
						.addBox(-5.0F, -8.0F, 0.0F, 8, 8, 0),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("LapisOre2",
				CubeListBuilder.create()
						.texOffs(0, 53)
						.addBox(-1.0F, -8.0F, -4.0F, 0, 8, 8),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("LapisOre3",
				CubeListBuilder.create()
						.texOffs(18, 51)
						.addBox(-10.0F, -8.0F, 8.0F, 8, 8, 0),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("LapisOre4",
				CubeListBuilder.create()
						.texOffs(18, 53)
						.addBox(-6.0F, -8.0F, 4.0F, 0, 8, 8),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("RedstoneOre1",
				CubeListBuilder.create()
						.texOffs(0, 83)
						.addBox(-8.0F, -12.0F, -6.0F, 5, 12, 5),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("RedstoneOre2",
				CubeListBuilder.create()
						.texOffs(0, 74)
						.addBox(6.0F, -6.0F, -1.0F, 3, 6, 3),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("RedstoneOre3",
				CubeListBuilder.create()
						.texOffs(12, 76)
						.addBox(-7.0F, -4.0F, 2.0F, 2, 4, 2),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("RedstoneOre4",
				CubeListBuilder.create()
						.texOffs(20, 87)
						.addBox(1.0F, -9.0F, -9.0F, 4, 9, 4),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("RedstoneOre5",
				CubeListBuilder.create()
						.texOffs(15, 77)
						.addBox(-1.0F, -5.0F, 5.0F, 5, 5, 5),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(@NotNull Toretoise entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.entity = entity;
		animFrames = limbSwing;
	}

	@Override
	public void renderToBuffer(PoseStack matrix, @NotNull VertexConsumer vb, int packedLightIn, int packedOverlayIn, int color) {
		matrix.pushPose();
		int bufferTime = 10;
		if(entity.angeryTicks > 0 && entity.angeryTicks < Toretoise.ANGERY_TIME - bufferTime) {
			double angeryTime = (entity.angeryTicks - Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true)) / (Toretoise.ANGERY_TIME - bufferTime) * Math.PI;
			angeryTime = Math.sin(angeryTime) * -20;

			matrix.translate(0, 1., 1);
			matrix.mulPose(Axis.XP.rotationDegrees((float) angeryTime));
			matrix.translate(0, -1, -1);
		}

		float animSpeed = 30;
		float animPause = 12;

		float actualFrames = animFrames * 10;

		float doubleAnimSpeed = animSpeed * 2;
		float animBuff = animSpeed - animPause;

		float scale = 0.02F;
		float bodyTrans = (float) (Math.sin(actualFrames / doubleAnimSpeed * Math.PI) + 1F) * scale;

		float rideMultiplier = 0;

		if(entity.rideTime > 0)
			rideMultiplier = Math.min(30, entity.rideTime - 1 + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true)) / 30.0F;

		bodyTrans *= (1F - rideMultiplier);

		matrix.translate(0, bodyTrans, 0);
		matrix.mulPose(Axis.ZP.rotation((bodyTrans - scale) * 0.5F));

		body.render(matrix, vb, packedLightIn, packedOverlayIn, color);

		matrix.pushPose();
		matrix.translate(0, bodyTrans, rideMultiplier * 0.3);
		head.xRot = bodyTrans * 2;
		head.render(matrix, vb, packedLightIn, packedOverlayIn, color);
		matrix.popPose();

		float finalRideMultiplier = rideMultiplier;
		BiConsumer<ModelPart, Float> draw = (renderer, frames) -> {
			float time = Math.min(animBuff, frames % doubleAnimSpeed);
			float trans = ((float) (Math.sin(time / animBuff * Math.PI) + 1.0) / -2F) * 0.12F + 0.06F;

			float rotTime = (frames % doubleAnimSpeed);
			float rot = ((float) Math.sin(rotTime / doubleAnimSpeed * Math.PI) + 1F) * -0.25F;

			trans *= (1F - finalRideMultiplier);
			rot *= (1F - finalRideMultiplier);
			trans += (finalRideMultiplier * -0.2F);

			matrix.pushPose();

			Cube box = renderer.getRandomCube(entity.getRandom());
			double spread = (1F / 16F) * -1.8 * finalRideMultiplier;
			double x = (renderer.x + box.minX);
			double z = (renderer.z + box.minZ);
			x *= (spread / Math.abs(x));
			z *= (spread / Math.abs(z));
			matrix.translate(x, 0, z);

			matrix.translate(0, trans, 0);
			float yRot = renderer.yRot;
			renderer.xRot = rot;
			renderer.yRot *= (1F - finalRideMultiplier);
			renderer.render(matrix, vb, packedLightIn, packedOverlayIn, color);
			renderer.yRot = yRot;
			matrix.popPose();
		};

		draw.accept(leftFrontLeg, actualFrames);
		draw.accept(rightFrontLeg, actualFrames + animSpeed);
		draw.accept(leftBackLeg, actualFrames + animSpeed * 0.5F);
		draw.accept(rightBackLeg, actualFrames + animSpeed * 1.5F);
		matrix.popPose();
	}

	public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}

}
