package org.violetmoon.quark.mixin.mixins;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;

import java.util.List;

@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {

	@Shadow
	@Final
	private static Component UPGRADE_TITLE;

	/*
	@Inject(method = "appendUpgradeHoverText", at = @At("RETURN"))
	private static void appendRuneText(ItemStack stack, RegistryAccess registry, List<Component> components, CallbackInfo ci) {
		ColorRunesModule.Client.appendRuneText(stack, components, UPGRADE_TITLE);
	}*/

}
