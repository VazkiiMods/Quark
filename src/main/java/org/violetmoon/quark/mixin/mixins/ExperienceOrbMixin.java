package org.violetmoon.quark.mixin.mixins;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.violetmoon.quark.content.experimental.module.GameNerfsModule;

import java.util.function.Predicate;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {

	@ModifyArg(method = "repairPlayerItems", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRandomItemWith(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Optional;"))
	private Predicate<ItemStack> alterPredicateForMending(Predicate<ItemStack> predicate) {
		return GameNerfsModule.limitMendingItems(predicate, ((ExperienceOrb) (Object) this).level().registryAccess());
	}
}
