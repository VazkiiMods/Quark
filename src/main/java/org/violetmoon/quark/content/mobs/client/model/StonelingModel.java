package org.violetmoon.quark.content.mobs.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.content.mobs.entity.Stoneling;

public class StonelingModel extends EntityModel<Stoneling> {

	private final ModelPart body;
	private final ModelPart arm_right;
	private final ModelPart arm_left;
	private final ModelPart leg_right;
	private final ModelPart leg_left;

	public StonelingModel(ModelPart root) {
		body = root.getChild("body");
		arm_right = root.getChild("arm_right");
		arm_left = root.getChild("arm_left");
		leg_right = root.getChild("leg_right");
		leg_left = root.getChild("leg_left");
	}

	// Made with Blockbench 4.1.5
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -9.0F, -3.0F, 8.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(36, 13).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(44, 7).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(23, 0).addBox(-2.0F, -12.0F, -1.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(30, 7).addBox(-2.0F, -9.0F, -6.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(25, 24).addBox(-2.0F, -12.0F, -5.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(36, 17).addBox(-2.0F, -11.0F, 3.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 27).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.0F, 0.0F));

		body.addOrReplaceChild("lychen", CubeListBuilder.create()
				.texOffs(10, 12).addBox(0.0F, -4.0F, -2.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(10, 16).addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, -9.0F, 3.0F, 0.0F, 0.7854F, 0.0F));

		body.addOrReplaceChild("dripstone", CubeListBuilder.create()
				.texOffs(14, 16).addBox(0.0F, -5.0F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(14, 22).addBox(-3.0F, -5.0F, 0.0F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -9.0F, 1.0F, 0.0F, 0.7854F, 0.0F));

		root.addOrReplaceChild("leg_left", CubeListBuilder.create()
				.texOffs(27, 13).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.25F, 19.0F, 0.5F));

		root.addOrReplaceChild("leg_right", CubeListBuilder.create()
				.texOffs(27, 13).mirror().addBox(-1.5F, 1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-2.25F, 19.0F, 0.5F));

		root.addOrReplaceChild("arm_right", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-4.0F, 15.0F, 0.5F));

		root.addOrReplaceChild("arm_left", CubeListBuilder.create()
				.texOffs(0, 16).mirror().addBox(0.0F, 0.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(4.0F, 15.0F, 0.5F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(Stoneling stoneling, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		leg_right.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
		leg_left.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;

		ItemStack carry = stoneling.getCarryingItem();
		if(carry.isEmpty() && !stoneling.isVehicle()) {
			arm_right.xRot = 0F;
			arm_left.xRot = 0F;
		} else {
			arm_right.xRot = 3.1416F;
			arm_left.xRot = 3.1416F;
		}
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vb, int packedLightIn, int packedOverlayIn, int color) {
		body.render(matrix, vb, packedLightIn, packedOverlayIn, color);
		arm_right.render(matrix, vb, packedLightIn, packedOverlayIn, color);
		arm_left.render(matrix, vb, packedLightIn, packedOverlayIn, color);
		leg_right.render(matrix, vb, packedLightIn, packedOverlayIn, color);
		leg_left.render(matrix, vb, packedLightIn, packedOverlayIn, color);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
