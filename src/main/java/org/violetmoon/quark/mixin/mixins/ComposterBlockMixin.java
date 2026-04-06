package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.base.util.CompostManager;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {
    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private static float quark$getValue(float original, @Local(argsOnly = true) ItemStack stack){
        if(original == -1.0){
            if(CompostManager.doesItemHaveChance(stack.getItem())){
                return CompostManager.getChance(stack.getItem());
            }
        }
        return original;
    }
}
