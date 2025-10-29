package org.violetmoon.quark.content.tweaks.client.layer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.client.module.UsesForCursesModule;

public class ArmorStandFakePlayerLayer<M extends EntityModel<ArmorStand>> extends RenderLayer<ArmorStand, M> {

	private final PlayerModel<?> playerModel;
	private final PlayerModel<?> playerModelSlim;

	public ArmorStandFakePlayerLayer(RenderLayerParent<ArmorStand, M> parent, EntityModelSet models) {
		super(parent);

		playerModel = new PlayerModel<>(models.bakeLayer(ModelLayers.PLAYER), false);
		playerModelSlim = new PlayerModel<>(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
	}

	@Override
	public void render(@NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int light, @NotNull ArmorStand armor, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if(!UsesForCursesModule.staticEnabled || !UsesForCursesModule.bindArmorStandsWithPlayerHeads) return;

		ItemStack head = armor.getItemBySlot(EquipmentSlot.HEAD);
		if(head.is(Items.PLAYER_HEAD) && EnchantmentHelper.has(head, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {

            RenderType rendertype;
            boolean slim = true;
            if (head.has(DataComponents.PROFILE)) {
                GameProfile profile = head.get(DataComponents.PROFILE).gameProfile();
                rendertype = SkullBlockRenderer.getRenderType(SkullBlock.Types.PLAYER, new ResolvableProfile(profile));
                PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
                slim = playerSkin.model().equals(PlayerSkin.Model.SLIM);
            } else {
                rendertype =  RenderType.entityTranslucent(DefaultPlayerSkin.getDefaultTexture());
            }
            pose.pushPose();

            if (armor.isBaby()) {
                float s = 1F;
                pose.translate(0F, 0F, 0F);
                pose.scale(s, s, s);
            } else {
                float s = 2F;
                pose.translate(0F, -1.5F, 0F);
                pose.scale(s, s, s);
            }

            PlayerModel<?> model = slim ? playerModelSlim : playerModel;


            model.head.visible = false;
            model.hat.visible = false;

            rotateModel(model.leftArm, armor.getLeftArmPose());
            rotateModel(model.rightArm, armor.getRightArmPose());
            rotateModel(model.leftSleeve, armor.getLeftArmPose());
            rotateModel(model.rightSleeve, armor.getRightArmPose());

            rotateModel(model.leftLeg, armor.getLeftLegPose());
            rotateModel(model.rightLeg, armor.getRightLegPose());
            rotateModel(model.leftPants, armor.getLeftLegPose());
            rotateModel(model.rightPants, armor.getRightLegPose());

            model.renderToBuffer(pose, buffer.getBuffer(rendertype), light, OverlayTexture.NO_OVERLAY, -1);

            pose.popPose();

        }
	}

	private void rotateModel(ModelPart part, Rotations rot) {
		part.setRotation(Mth.DEG_TO_RAD * rot.getX(), Mth.DEG_TO_RAD * rot.getY(), Mth.DEG_TO_RAD * rot.getZ());
	}

}
