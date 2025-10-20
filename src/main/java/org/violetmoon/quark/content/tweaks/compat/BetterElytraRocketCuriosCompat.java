package org.violetmoon.quark.content.tweaks.compat;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class BetterElytraRocketCuriosCompat {
    public static boolean hasCuriosElytra(Player player) {
        Optional<ICuriosItemHandler> curiosApi = CuriosApi.getCuriosInventory(player);
        if (curiosApi.isPresent()) {
            IItemHandlerModifiable curios = curiosApi.get().getEquippedCurios();
            for (int slot = 0; slot < curios.getSlots(); slot++) {
                if (curios.getStackInSlot(slot).canElytraFly(player)) {
                    return true;
                }
            }
        }
        return false;
    }
}
