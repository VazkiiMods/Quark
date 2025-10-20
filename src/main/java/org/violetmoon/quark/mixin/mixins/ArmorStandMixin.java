package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.tweaks.module.ArmedArmorStandsModule;

@Mixin(ArmorStand.class)
public class ArmorStandMixin {
	@ModifyExpressionValue(method = "defineSynchedData", at = @At(value = "CONSTANT", args = "intValue=0"))
	private int quark$armedArmorStands(int original) {
		SynchedEntityData data = ((Entity) (Object) this).getEntityData();
		return !ArmedArmorStandsModule.staticEnabled || (data != null && data.get(ArmorStand.DATA_CLIENT_FLAGS) != null) ? original : original | ArmorStand.CLIENT_FLAG_SHOW_ARMS;
	}
}
