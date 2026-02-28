package org.violetmoon.quark.content.management.module;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

//quick armor swapping/curious armor stands module handler.
public class QASCASHandler {
    public static final String SLOT = "curio"; //all armorstand curios go into this slot

    public static void swapCurios(ZPlayerInteract.EntityInteractSpecific event, Player player, ArmorStand armorStand){
        //untested, ignore all this
        var curios = CuriosApi.getCuriosInventory(player);
        List<SlotResult> results = curios.get().findCurios();
        for(SlotResult result : results) {
            swapCurioSlot(player, armorStand, result);
        }

    }


    private static void swapCurioSlot(Player player, ArmorStand armorStand, SlotResult result){
        ItemStack playerStack = result.stack();
        //the rest of the owl.
    }
}
