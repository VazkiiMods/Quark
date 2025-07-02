package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.building.module.ShearVinesModule;

import net.minecraft.world.item.ShearsItem;

import java.util.ArrayList;
import java.util.List;

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
