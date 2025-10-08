package org.violetmoon.quark.content.mobs.client.model;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.mobs.entity.Crab;

import java.util.Set;

public class CrabModel extends EntityModel<Crab> {

	private float wiggleX = 0;
	private float wiggleY = 0;
	private float yungModifier = 0;

	public ModelPart group;
	public ModelPart body;
	public ModelPart rightClaw;
	public ModelPart leftClaw;
	public ModelPart rightLeg1;
	public ModelPart rightLeg2;
	public ModelPart rightLeg3;
	public ModelPart rightLeg4;
	public ModelPart leftLeg1;
	public ModelPart leftLeg2;
	public ModelPart leftLeg3;
	public ModelPart leftLeg4;
	public ModelPart rightEye;
	public ModelPart leftEye;

	private final Set<ModelPart> leftLegs;
	private final Set<ModelPart> rightLegs;

	public CrabModel(ModelPart root) {
		group = root.getChild("group");
		body = group.getChild("body");
		rightClaw = group.getChild("rightClaw");
		leftClaw = group.getChild("leftClaw");
		rightLeg1 = group.getChild("rightLeg1");
		rightLeg2 = group.getChild("rightLeg2");
		rightLeg3 = group.getChild("rightLeg3");
		rightLeg4 = group.getChild("rightLeg4");
		leftLeg1 = group.getChild("leftLeg1");
		leftLeg2 = group.getChild("leftLeg2");
		leftLeg3 = group.getChild("leftLeg3");
		leftLeg4 = group.getChild("leftLeg4");
		rightEye = body.getChild("rightEye");
		leftEye = body.getChild("leftEye");

		leftLegs = ImmutableSet.of(leftLeg1, leftLeg2, leftLeg3, leftLeg4);
		rightLegs = ImmutableSet.of(rightLeg1, rightLeg2, rightLeg3, rightLeg4);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition group = root.addOrReplaceChild("group", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = group.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-4.0F, -2.5F, -3.0F, 8, 5, 6),
				PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		group.addOrReplaceChild("leftLeg4",
				CubeListBuilder.create()
						.mirror()
						.texOffs(0, 19)
						.addBox(0.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(3.0F, 20.0F, -1.0F, 0.0F, 0.4363323129985824F, 0.7853981633974483F));

		group.addOrReplaceChild("leftLeg3",
				CubeListBuilder.create()
						.mirror()
						.texOffs(0, 19)
						.addBox(0.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(3.0F, 20.0F, 0.0F, 0.0F, 0.2181661564992912F, 0.7853981633974483F));

		body.addOrReplaceChild("rightEye",
				CubeListBuilder.create()
						.texOffs(0, 11)
						.addBox(-3.0F, -3.5F, -2.85F, 1, 3, 1),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.39269908169872414F, 0.0F, 0.0F));

		group.addOrReplaceChild("rightLeg4",
				CubeListBuilder.create()
						.texOffs(0, 19)
						.addBox(-6.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(-3.0F, 20.0F, -1.0F, 0.0F, -0.4363323129985824F, -0.7853981633974483F));

		group.addOrReplaceChild("rightClaw",
				CubeListBuilder.create()
						.texOffs(14, 11)
						.addBox(-3.0F, -2.5F, -6.0F, 3, 5, 6),
				PartPose.offsetAndRotation(-3.0F, 20.0F, -4.0F, 0.0F, 0.39269908169872414F, -0.39269908169872414F));

		group.addOrReplaceChild("leftLeg1",
				CubeListBuilder.create()
						.mirror()
						.texOffs(0, 19)
						.addBox(0.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(3.0F, 20.0F, 2.0F, 0.0F, -0.4363323129985824F, 0.7853981633974483F));

		group.addOrReplaceChild("rightLeg2",
				CubeListBuilder.create()
						.mirror()
						.texOffs(0, 19)
						.addBox(-6.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(-3.0F, 20.0F, 0.9F, 0.0F, 0.2181661564992912F, -0.7853981633974483F));

		group.addOrReplaceChild("leftClaw",
				CubeListBuilder.create()
						.mirror()
						.texOffs(14, 11)
						.addBox(0.0F, -2.5F, -6.0F, 3, 5, 6),
				PartPose.offsetAndRotation(3.0F, 20.0F, -4.0F, 0.0F, -0.39269908169872414F, 0.39269908169872414F));

		group.addOrReplaceChild("rightLeg1",
				CubeListBuilder.create()
						.texOffs(0, 19)
						.addBox(-6.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(-3.0F, 20.0F, 2.0F, 0.0F, 0.4363323129985824F, -0.7853981633974483F));

		body.addOrReplaceChild("leftEye",
				CubeListBuilder.create()
						.texOffs(0, 11)
						.addBox(2.0F, -3.5F, -2.85F, 1, 3, 1),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.39269908169872414F, 0.0F, 0.0F));

		group.addOrReplaceChild("leftLeg2",
				CubeListBuilder.create()
						.mirror()
						.texOffs(0, 19)
						.addBox(0.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(3.0F, 20.0F, 0.9F, 0.0F, -0.2181661564992912F, 0.7853981633974483F));

		group.addOrReplaceChild("rightLeg3",
				CubeListBuilder.create()
						.texOffs(0, 19)
						.addBox(-6.0F, -0.5F, -0.5F, 6, 1, 1),
				PartPose.offsetAndRotation(-3.0F, 20.0F, 0.0F, 0.0F, -0.2181661564992912F, -0.7853981633974483F));

		return LayerDefinition.create(mesh, 32, 32);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}

	@Override
	public void setupAnim(Crab crab, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		rightLeg1.zRot = -0.2618F + (-1 + Mth.cos(limbSwing * 0.6662F)) * 0.7F * limbSwingAmount;
		rightLeg2.zRot = -0.5236F + (-1 + Mth.cos(limbSwing * 0.6662F + (float) Math.PI)) * 0.7F * limbSwingAmount;
		rightLeg3.zRot = -0.5236F + (-1 + Mth.cos(limbSwing * 0.6662F)) * 0.7F * limbSwingAmount;
		rightLeg4.zRot = -0.2618F + (-1 + Mth.cos(limbSwing * 0.6662F + (float) Math.PI)) * 0.7F * limbSwingAmount;
		leftLeg1.zRot = 0.2618F + (1 + Mth.cos(limbSwing * 0.6662F + (float) Math.PI)) * 0.7F * limbSwingAmount;
		leftLeg2.zRot = 0.5236F + (1 + Mth.cos(limbSwing * 0.6662F)) * 0.7F * limbSwingAmount;
		leftLeg3.zRot = 0.5236F + (1 + Mth.cos(limbSwing * 0.6662F + (float) Math.PI)) * 0.7F * limbSwingAmount;
		leftLeg4.zRot = 0.2618F + (1 + Mth.cos(limbSwing * 0.6662F)) * 0.7F * limbSwingAmount;

		leftClaw.xRot = 0.0f;
		rightClaw.xRot = 0.0f;
		wiggleX = 0.0f;
		wiggleY = 0.0f;

        yungModifier = 1f;
		if(young)
			yungModifier = 0.5f;

		if(crab.isRaving()) {
			float crabRaveBPM = 125F / 4;
			float freq = (20F / crabRaveBPM);
			float tick = ageInTicks * freq;
			float sin = (float) (Math.sin(tick) * 0.5 + 0.5);

			float legRot = (sin * 0.8F) + 0.6F;
			leftLegs.forEach(l -> l.zRot = legRot);
			rightLegs.forEach(l -> l.zRot = -legRot);

			float maxHeight = -0.05F;
			float horizontalOff = 0.2F;
			wiggleX = (sin - 0.5F) * 2 * maxHeight + maxHeight / 2;

			float slowSin = (float) Math.sin(tick / 2);
			wiggleY = slowSin * horizontalOff;

			float armRot = sin * 0.5F - 1.2F;
			leftClaw.xRot = armRot;
			rightClaw.xRot = armRot;
		}
	}

	@Override
	public void renderToBuffer(PoseStack matrix, @NotNull VertexConsumer vb, int packedLightIn, int packedOverlayIn, int color) {
		matrix.pushPose();
		matrix.translate(0, 1.5 - yungModifier * 1.5, 0);
		matrix.scale(yungModifier, yungModifier, yungModifier);
		matrix.mulPose(Axis.YP.rotationDegrees(90F));
		matrix.translate(wiggleX, wiggleY, 0);
		group.render(matrix, vb, packedLightIn, packedOverlayIn, color);
		matrix.popPose();
	}
}
