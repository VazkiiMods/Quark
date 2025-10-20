package org.violetmoon.quark.content.mobs.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import org.violetmoon.quark.content.mobs.entity.Wraith;

import java.util.Random;

public class WraithModel extends EntityModel<Wraith> {

	public final ModelPart main;
	public final ModelPart body;
	public final ModelPart arms;

	private double offset;
	private float alphaMult;

	public WraithModel(ModelPart root) {
		super(RenderType::entityTranslucent);

		main = root.getChild("main");
		body = main.getChild("body");
		arms = main.getChild("arms");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition main = root.addOrReplaceChild("main",
				CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		main.addOrReplaceChild("arms",
				CubeListBuilder.create()
						.texOffs(36, 6)
						.addBox(-8.5F, 1.0F, -2.0F, 3.0F, 15.0F, 5.0F, new CubeDeformation(0.0F))

						.texOffs(0, 55)
						.addBox(-5.5F, 12.0F, 0.0F, 11.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))

						.texOffs(47, 3)
						.addBox(-8.5F, 11.0F, -2.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.25F))

						.texOffs(36, 6)
						.mirror()
						.addBox(5.5F, 1.0F, -2.0F, 3.0F, 15.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)

						.texOffs(47, 3)
						.mirror()
						.addBox(5.5F, 11.0F, -2.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.25F))

						.mirror(false),
				PartPose.offset(0.0F, -17.0F, -1.0F));

		main.addOrReplaceChild("body",
				CubeListBuilder
						.create()
						.texOffs(0, 0)
						.addBox(-4.5F, -10.0F, -4.0F, 11.0F, 26.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0F, -18.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(Wraith entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		Random rng = new Random(entity.getId());
		int offset1 = rng.nextInt(10000000);
		int offset2 = rng.nextInt(6000000);
		int offset3 = rng.nextInt(8000000);

		float time = ageInTicks + offset1;
		float time2 = ageInTicks + offset2;
		float time3 = ageInTicks + offset3;

		main.xRot = (float) Math.sin(time / 16) * 0.1F - 0.3F;
		main.yRot = (float) Math.sin(time2 / 20) * 0.12F;
		main.zRot = (float) Math.sin(time3 / 12) * 0.07F;

		arms.xRot = (float) Math.sin(time2 / 22) * 0.15F;

		offset = Math.sin(time / 16) * 0.1 - 0.25;
		alphaMult = 0.8F + (float) Math.sin(time2 / 20) * 0.2F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer bufferIn, int packedLight, int packedOverlay, int color) {
		poseStack.pushPose();
		poseStack.translate(0, offset, -0.1); // -0.1 is to ensure the model is inside the hitbox
		main.render(poseStack, bufferIn, packedLight, packedOverlay, color); // TODO: change color alphaMult
		poseStack.popPose();
	}
}
