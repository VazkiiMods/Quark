package org.violetmoon.quark.content.mobs.client.layer.shiba;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.model.FoxhoundModel;
import org.violetmoon.quark.content.mobs.client.model.ShibaModel;
import org.violetmoon.quark.content.mobs.entity.Foxhound;
import org.violetmoon.quark.content.mobs.entity.Shiba;

import java.util.Map;

public class ShibaArmorLayer extends RenderLayer<Shiba, ShibaModel>  {
    private final ShibaModel model;
    private static final Map<Crackiness.Level, ResourceLocation> ARMOR_CRACK_LOCATIONS = Map.of(
            Crackiness.Level.LOW,
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_low.png"),
            Crackiness.Level.MEDIUM,
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_medium.png"),
            Crackiness.Level.HIGH,
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_high.png")
    );

    public ShibaArmorLayer(RenderLayerParent<Shiba, ShibaModel> renderer, EntityModelSet models) {
        super(renderer);
        this.model = new ShibaModel(models.bakeLayer(ModelHandler.shiba_armor));
    }

    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            Shiba livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (livingEntity.hasArmor()) {
            ItemStack itemstack = livingEntity.getBodyArmorItem();
            if (itemstack.getItem() instanceof AnimalArmorItem armor && armor.getBodyType() == AnimalArmorItem.BodyType.CANINE) {
                this.getParentModel().copyPropertiesTo(this.model);
                this.model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTick);
                this.model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(Quark.asResource("textures/model/entity/shiba/shiba_armor.png")));
                this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
                //this.maybeRenderColoredLayer(poseStack, bufferSource, packedLight, itemstack, armor);
                //this.maybeRenderCracks(poseStack, bufferSource, packedLight, itemstack);
            }
        }
    }

    private void maybeRenderColoredLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack armorStack, AnimalArmorItem armorItem) {
        if (armorStack.is(ItemTags.DYEABLE)) {
            int itemColor = DyedItemColor.getOrDefault(armorStack, 0);
            if (FastColor.ARGB32.alpha(itemColor) == 0) {
                return;
            }

            ResourceLocation resourcelocation = armorItem.getOverlayTexture();
            if (resourcelocation == null) {
                return;
            }

            this.model
                    .renderToBuffer(
                            poseStack,
                            buffer.getBuffer(RenderType.entityCutoutNoCull(resourcelocation)),
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            FastColor.ARGB32.opaque(itemColor)
                    );
        }
    }

    private void maybeRenderCracks(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack armorStack) {
        Crackiness.Level crackiness$level = Crackiness.WOLF_ARMOR.byDamage(armorStack);
        if (crackiness$level != Crackiness.Level.NONE) {
            ResourceLocation resourcelocation = ARMOR_CRACK_LOCATIONS.get(crackiness$level);
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(resourcelocation));
            this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
