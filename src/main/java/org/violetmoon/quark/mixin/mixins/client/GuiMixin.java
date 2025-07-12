package org.violetmoon.quark.mixin.mixins.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.module.ElytraIndicatorModule;
import org.violetmoon.quark.content.client.module.UsesForCursesModule;

@Mixin(Gui.class)
public class GuiMixin {

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "CONSTANT", args = "intValue=10", ordinal = 1))
	private static int renderArmor(int original) {
		ElytraIndicatorModule module = Quark.ZETA.modules.get(ElytraIndicatorModule.class);
		return module == null ? original : module.getArmorLimit(original);
	}

	@Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
	public void changeArmorItem(GuiGraphics guiGraphics, ResourceLocation location, float alpha, CallbackInfo ci) {
		Player player = Minecraft.getInstance().player;
		if(player != null) {
			if(UsesForCursesModule.shouldHidePumpkinOverlay(location, player))
				ci.cancel();
		}
	}
}
