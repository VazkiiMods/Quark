package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.tools.module.TorchArrowModule;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    @WrapOperation(method = "draw", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processProjectileCount(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;I)I"))
    private static int modifyProjectileCount(ServerLevel serverLevel, ItemStack weapon, Entity shooter, int projectileCount, Operation<Integer> original,
                                             @Local(ordinal = 1, argsOnly = true) ItemStack ammo) {
        return ammo.is(TorchArrowModule.ignoreMultishot) ? 1 : original.call(serverLevel, weapon, shooter, projectileCount);
    }
}
