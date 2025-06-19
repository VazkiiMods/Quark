package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantMenu.class)
public interface AccessorMerchantMenu {
    @Invoker("moveFromInventoryToPaymentSlot")
    public void invokeMoveFromInventoryToPaymentSlot(int slot, ItemCost cost);
}
