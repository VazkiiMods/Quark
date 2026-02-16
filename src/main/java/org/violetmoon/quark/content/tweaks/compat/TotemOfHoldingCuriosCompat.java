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
            for (int i = 0; i < equipedCurios.size(); i++) {
                ItemStack curiosItem = equipedCurios.get(i);
                if (stack.is(curiosItem.getItem())) {
                    IItemHandlerModifiable curiosSlot = curiosApi.get().getEquippedCurios();
                    if (!curiosSlot.getStackInSlot(i).isEmpty()) continue;

                    if (curiosSlot.isItemValid(i, stack)) {
                        curiosSlot.insertItem(i, stack, false);
                        return null;
                    }
                }
            }
        }
        return stack;
    }

    public static void saveCurios(Player player, TotemOfHoldingEntity totem) {
        Optional<ICuriosItemHandler> curiosApi = CuriosApi.getCuriosInventory(player);
        curiosApi.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((a, b) ->
                IntStream.range(0, b.getStacks().getSlots()).mapToObj(i ->
                        //saving the empty slots helps not needing an extra loop when equiping them back
                        // ↑ for commit 1151846d68a2a75d552a2b2f35023877c663dd2f
                        b.getStacks().getPreviousStackInSlot(i)).forEach(totem::addCurios)));
    }
}
