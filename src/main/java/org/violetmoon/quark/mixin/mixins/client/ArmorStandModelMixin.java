package org.violetmoon.quark.mixin.mixins.client;

import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.violetmoon.quark.content.client.module.UsesForCursesModule;

@Mixin(ArmorStandModel.class)
public class ArmorStandModelMixin {

	@Shadow
	@Final
	private ModelPart rightBodyStick;

	@Shadow
	@Final
	private ModelPart leftBodyStick;

	@Shadow
	@Final
	private ModelPart shoulderStick;

	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/decoration/ArmorStand;FFFFF)V", at = @At("HEAD"))
	public void resetModelPartVisibility(ArmorStand armorStand, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		ArmorStandModel model = (ArmorStandModel) (Object) this;

		model.rightLeg.visible = true;
		model.leftLeg.visible = true;
		rightBodyStick.visible = true;
		leftBodyStick.visible = true;
		shoulderStick.visible = true;
		model.head.visible = true;
		model.body.visible = true;
	}

	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/decoration/ArmorStand;FFFFF)V", at = @At("RETURN"))
	public void setModelPartsVisible(ArmorStand armorStand, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		ArmorStandModel model = (ArmorStandModel) (Object) this;

		ItemStack head = armorStand.getItemBySlot(EquipmentSlot.HEAD);
		if(UsesForCursesModule.shouldHideArmorStandModel(head)) {
			model.rightLeg.visible = false;
			model.leftLeg.visible = false;
			rightBodyStick.visible = false;
			leftBodyStick.visible = false;
			shoulderStick.visible = false;
			model.leftArm.visible = false;
			model.rightArm.visible = false;
			model.head.visible = false;
			model.body.visible = false;
		}
	}
}
