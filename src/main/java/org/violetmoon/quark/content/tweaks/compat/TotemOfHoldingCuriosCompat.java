package org.violetmoon.quark.content.tweaks.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.violetmoon.quark.addons.oddities.entity.TotemOfHoldingEntity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class TotemOfHoldingCuriosCompat {

    public static ItemStack equipCurios(Player player, List<ItemStack> equipedCurios, ItemStack stack) {
        Optional<ICuriosItemHandler> curiosApi = CuriosApi.getCuriosInventory(player);
        if (curiosApi.isPresent()) {
            for (ItemStack curiosItem : equipedCurios) {
                if (stack.is(curiosItem.getItem())) {
                    IItemHandlerModifiable curiosSlot = curiosApi.get().getEquippedCurios();
                    for (int j = 0; j < curiosApi.get().getSlots(); j++) {
                        if (!curiosSlot.getStackInSlot(j).isEmpty()) continue;

                        if (curiosSlot.isItemValid(j, stack)) {
                            curiosSlot.insertItem(j, stack, false);
                            return null;
                        }
                    }
                }
            }
        }
        return stack;
    }

    public static void saveCurios(Player player, TotemOfHoldingEntity totem) {
        Optional<ICuriosItemHandler> curiosApi = CuriosApi.getCuriosInventory(player);
        curiosApi.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((a, b) -> IntStream.range(0, b.getStacks().getSlots()).mapToObj(i -> b.getStacks().getPreviousStackInSlot(i)).forEach(stack -> {
            if (!stack.isEmpty()) {
                totem.addCurios(stack);
            }
        })));
    }
}
