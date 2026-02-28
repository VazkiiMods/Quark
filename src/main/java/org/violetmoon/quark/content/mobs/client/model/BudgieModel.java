package org.violetmoon.quark.content.mobs.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.violetmoon.quark.content.mobs.entity.Budgie;

public class BudgieModel<T extends Budgie> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public BudgieModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 19.0F, 0.0F));

        body.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F)
            .texOffs(8, 0).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.5F, -0.5F));

        body.addOrReplaceChild("left_wing", CubeListBuilder.create()
            .texOffs(12, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 4.0F, 3.0F), PartPose.offset(1.5F, 0.0F, 0.0F));
        body.addOrReplaceChild("right_wing", CubeListBuilder.create()
            .texOffs(12, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 4.0F, 3.0F), PartPose.offset(-1.5F, 0.0F, 0.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(20, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 1.0F, 0.3F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);

        if (entity.isFlying()) {
            float f = ageInTicks * 0.3F;
            this.leftWing.zRot = f;
            this.rightWing.zRot = -f;
            this.body.xRot = 0.2F;
        } else {
            this.body.y += Mth.cos(ageInTicks * 0.1F) * 0.15F;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
