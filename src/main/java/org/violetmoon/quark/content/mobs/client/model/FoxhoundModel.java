package org.violetmoon.quark.content.mobs.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;


import org.violetmoon.quark.content.mobs.entity.Foxhound;

/**
 * ModelFoxhound - McVinnyq
 * Created using Tabula 7.0.0
 */
public class FoxhoundModel extends AgeableListModel<Foxhound> {

	public final ModelPart head;
	public final ModelPart rightFrontLeg;
	public final ModelPart leftFrontLeg;
	public final ModelPart rightBackLeg;
	public final ModelPart leftBackLeg;
	public final ModelPart body;
	public final ModelPart snout;
	public final ModelPart rightEar;
	public final ModelPart leftEar;
	public final ModelPart tail;
	public final ModelPart fluff;

	public FoxhoundModel(ModelPart root) {
		super(false,5, 2.5f);
		head = root.getChild("head");
		rightFrontLeg = root.getChild("rightFrontLeg");
		leftFrontLeg = root.getChild("leftFrontLeg");
		rightBackLeg = root.getChild("rightBackLeg");
		leftBackLeg = root.getChild("leftBackLeg");
		body = root.getChild("body");
		//children
		snout = head.getChild("snout");
		rightEar = head.getChild("rightEar");
		leftEar = head.getChild("leftEar");
		tail = body.getChild("tail");
		fluff = body.getChild("fluff");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		float zOff = 5.5f;

		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 20)
						.addBox(-4.0F, -3.0F, -6.0F, 8, 6, 6, cubeDeformation),
				PartPose.offsetAndRotation(0.0F, 14.5F, -5.5f, 0.0F, 1F, 0.0F));

		head.addOrReplaceChild("rightEar",
				CubeListBuilder.create()
						.texOffs(0, 47)
						.addBox(-4.0F, -5.0F, -5.0F, 2, 2, 3, cubeDeformation),
				PartPose.ZERO);

		head.addOrReplaceChild("leftEar",
				CubeListBuilder.create()
						.texOffs(10, 47)
						.addBox(2.0F, -5.0F, -5.0F, 2, 2, 3, cubeDeformation),
				PartPose.ZERO);

		head.addOrReplaceChild("snout",
				CubeListBuilder.create()
						.texOffs(29, 18)
						.addBox(-2.0F, 1.0F, -10.0F, 4, 2, 4, cubeDeformation),
				PartPose.ZERO);


		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 2)
						.addBox(-4.0F, -12.0F, 0.0F, 8, 12, 6, cubeDeformation),
				PartPose.offsetAndRotation(0.0F, 17.0F, 6.5f, 1.5707963267948966F, 0.0F, 0.0F));

		body.addOrReplaceChild("tail",
				CubeListBuilder.create()
						.texOffs(36, 16)
						.addBox(-2.0F, -4.0F, 0.0F, 4, 5, 10),
				PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, -1.3089969389957472F, 0.0F, 0.0F));

        //CubeDeformation fluffControl = new CubeDeformation(0.0f);

		body.addOrReplaceChild("fluff",
				CubeListBuilder.create()
						.texOffs(28, 0)
						.addBox(-5.0F, 0.0F, -4.0F, 10, 8, 8),
				PartPose.offsetAndRotation(0.0F, -13.0F, 3.0F, 0.0F, 0.0F, 0.0F));


		root.addOrReplaceChild("leftBackLeg",
				CubeListBuilder.create()
						.texOffs(36, 32)
						.addBox(-1.5F, 0.0F, -1.5F, 3, 12, 3, cubeDeformation),
				PartPose.offsetAndRotation(3.0F, 12.0F, 4, 0.0F, 0.0F, 0.0F));

		root.addOrReplaceChild("rightBackLeg",
				CubeListBuilder.create()
						.texOffs(24, 32)
						.addBox(-1.5F, 0.0F, -1.5F, 3, 12, 3, cubeDeformation),
				PartPose.offsetAndRotation(-3.0F, 12.0F, 4, 0.0F, 0.0F, 0.0F));

		root.addOrReplaceChild("rightFrontLeg",
				CubeListBuilder.create()
						.texOffs(0, 32)
						.addBox(-1.5F, 0.0F, -1.5F, 3, 12, 3, cubeDeformation),
				PartPose.offsetAndRotation(-2.0F, 12.0F, -3.5f, 0.0F, 0.0F, 0.0F));


		root.addOrReplaceChild("leftFrontLeg",
				CubeListBuilder.create()
						.texOffs(12, 32)
						.addBox(-1.5F, 0.0F, -1.5F, 3, 12, 3, cubeDeformation),
				PartPose.offsetAndRotation(2.0F, 12.0F, -3.5f, 0.0F, 0.0F, 0.0F));


		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void prepareMobModel(Foxhound hound, float limbSwing, float limbSwingAmount, float partialTickTime) {
		if(hound.getRemainingPersistentAngerTime() > 0)
			this.tail.xRot = -0.6544984695F;
		else
			this.tail.xRot = -1.3089969389957472F + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;

		this.head.yRot = hound.getHeadRollAngle(partialTickTime) - hound.getBodyRollAngle(partialTickTime, 0.0F);
		this.head.xRot = 0;
		this.body.yRot = hound.getBodyRollAngle(partialTickTime, -0.16F);
		this.tail.yRot = hound.getBodyRollAngle(partialTickTime, -0.2F);

        if (this.fluff.visible && hound.hasArmor() )
            this.fluff.visible = false;
        else if (!this.fluff.visible && !hound.hasArmor())
            this.fluff.visible = true;

		if(hound.isSleeping()) {
			this.head.setPos(1.0F, 20.5F, -5.5f);
			this.setAngle(head, 0.0F, 0.7853981633974483F, -0.04363323129985824F);

			this.body.setPos(0.0F, 20.0F, 6.5f);
			this.setAngle(body, 1.5707963267948966F, 0.0F, 1.5707963267948966F);
			this.tail.setPos(0.0F, -1.0F, 1.0F);
			this.setAngle(tail, 2.5497515042385164F, -0.22759093446006054F, 0.0F);
			this.rightFrontLeg.setPos(0.0F, 18.0F, -3.5f);
			this.leftFrontLeg.setPos(2.0F, 21.0F, -4.5f);
			this.rightBackLeg.setPos(0.0F, 22.0F, 5.5f);
			this.leftBackLeg.setPos(3.0F, 20.0F, 4.5f);

			this.setAngle(rightFrontLeg, 0.2181661564992912F, 0.4363323129985824F, 1.3089969389957472F);
			this.setAngle(leftFrontLeg, 0.0F, 0.0F, 1.3962634015954636F);
			this.setAngle(rightBackLeg, -1.0471975511965976F, -0.08726646259971647F, 1.48352986419518F);
			this.setAngle(leftBackLeg, -0.7853981633974483F, 0.0F, 1.2217304763960306F);
		} else if(hound.isInSittingPose()) {
			this.head.setPos(0.0F, 12.0F, -3.5f);
			this.body.setPos(0.0F, 23.0F, 1.5f);
			this.setAngle(body, 0.7853981633974483F, this.body.yRot, 0F);
			this.tail.setPos(0.0F, 0.0F, 2F);
			this.setAngle(tail, -1.1f, -0f, 0F);
			this.rightFrontLeg.setPos(-2.0F, 12.0F, -4.25F);
			this.leftFrontLeg.setPos(2.0F, 12.0F, -4.25F);
			this.rightBackLeg.setPos(-3.0F, 21.0F, 4.5f);
			this.leftBackLeg.setPos(3.0F, 21.0F, 4.5f);

			this.setAngle(rightFrontLeg, 0F, 0F, 0F);
			this.setAngle(leftFrontLeg, 0F, 0F, 0F);
			this.setAngle(rightBackLeg, -1.3089969389957472F, 0.39269908169872414F, 0.0F);
			this.setAngle(leftBackLeg, -1.3089969389957472F, -0.39269908169872414F, 0.0F);
		} else {
			this.head.setPos(0.0F, 14.5F, -5.5f);
			this.body.setPos(0.0F, 17.0F, 6.5f);
			this.setAngle(body, 1.5707963267948966F, this.body.yRot, 0F);
			this.tail.setPos(0.0F, 0.0F, 1.5F);
			this.rightFrontLeg.setPos(-2.0F, 12.0F, -3.5f);
			this.leftFrontLeg.setPos(2.0F, 12.0F, -3.5f);
			this.rightBackLeg.setPos(-3.0F, 12.0F, 4);
			this.leftBackLeg.setPos(3.0F, 12.0F, 4);
			this.setAngle(rightFrontLeg, Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount, 0, 0);
			this.setAngle(leftFrontLeg, Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount, 0, 0);
			this.setAngle(rightBackLeg, Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount, 0, 0);
			this.setAngle(leftBackLeg, Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount, 0, 0);
		}
	}

	@Override
	public void setupAnim(Foxhound entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		if(!entity.isSleeping()) {
			head.yRot += netHeadYaw * 0.017453292F;
			head.xRot += headPitch * 0.017453292F;
		} else
			head.yRot += Mth.cos(entity.tickCount / 30f) / 20;
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body, this.rightBackLeg, this.leftBackLeg, this.rightFrontLeg, this.leftFrontLeg);
	}

	public void setAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
