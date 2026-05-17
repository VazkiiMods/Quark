package org.violetmoon.quark.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ShearsItem;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

	/*@ModifyReturnValue(
			method = "createToolProperties",
			at = @At("RETURN")
	)
    private static Tool modifyToolProperties(Tool original) {
        List<Block> blocks = new ArrayList<>();
		blocks.add(ShearVinesModule.cut_vine);
        List<Tool.Rule> rules = new ArrayList<>(original.rules());
		rules.add(Tool.Rule.overrideSpeed(blocks, 2.0f));
		return new Tool(rules, original.defaultMiningSpeed(), original.damagePerBlock());
	}*/
	
}
