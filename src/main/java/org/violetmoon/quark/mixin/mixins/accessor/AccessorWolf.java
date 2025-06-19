package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Wolf.class)
public interface AccessorWolf {
    @Invoker("setCollarColor")
    public void invokeSetCollarColor(DyeColor dyeColor);
}
