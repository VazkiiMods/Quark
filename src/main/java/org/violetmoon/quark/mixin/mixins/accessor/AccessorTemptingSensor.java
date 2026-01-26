package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(TemptingSensor.class)
public interface AccessorTemptingSensor {
	@Accessor("temptations")
	Predicate<ItemStack> quark$getTemptations();
}
