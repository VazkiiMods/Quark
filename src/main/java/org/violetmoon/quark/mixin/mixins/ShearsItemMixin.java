package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.Tool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.building.module.ShearVinesModule;

import net.minecraft.world.item.ShearsItem;

import java.util.List;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

	/*@ModifyReturnValue(
			method = "createToolProperties",
			at = @At("RETURN")
	)
    private static Tool modifyToolProperties(Tool original) {
		List<Tool.Rule> rules = original.rules();
		rules.add(Tool.Rule.overrideSpeed(List.of(ShearVinesModule.cut_vine), 2.0f));
		return new Tool(rules, original.defaultMiningSpeed(), original.damagePerBlock());
	}*/
	
}
