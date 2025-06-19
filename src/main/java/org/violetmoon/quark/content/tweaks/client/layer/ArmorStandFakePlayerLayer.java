package org.violetmoon.quark.content.tweaks.client.layer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
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
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
	public void render(@NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int light, @NotNull ArmorStand armor, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
		if(!UsesForCursesModule.staticEnabled || !UsesForCursesModule.bindArmorStandsWithPlayerHeads) return;

		ItemStack head = armor.getItemBySlot(EquipmentSlot.HEAD);
		if(head.is(Items.PLAYER_HEAD) && EnchantmentHelper.hasBindingCurse(head)) {
			CompoundTag skullOwner = ItemNBTHelper.getCompound(head, "SkullOwner", true);
			GameProfile profile = skullOwner != null ? NbtUtils.readGameProfile(skullOwner) : null;
			RenderType rendertype = SkullBlockRenderer.getRenderType(SkullBlock.Types.PLAYER, profile);

			if(rendertype != null) {
				boolean slim = false;
				if(profile != null) {
					MinecraftProfileTexture profileTexture = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(profile).get(Type.SKIN);
					if(profileTexture != null) {
						String modelMeta = profileTexture.getMetadata("model");
						slim = "slim".equals(modelMeta);
					}
				}

				pose.pushPose();

				if(armor.isBaby()) {
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

				model.renderToBuffer(pose, buffer.getBuffer(rendertype), light, OverlayTexture.NO_OVERLAY, 1);

				pose.popPose();
			}

		}
	}

	private void rotateModel(ModelPart part, Rotations rot) {
		part.setRotation(Mth.DEG_TO_RAD * rot.getX(), Mth.DEG_TO_RAD * rot.getY(), Mth.DEG_TO_RAD * rot.getZ());
	}

}
