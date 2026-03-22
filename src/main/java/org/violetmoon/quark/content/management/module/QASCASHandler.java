package org.violetmoon.quark.content.management.module;

import curiousarmorstands.CuriousArmorStands;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

//quick armor swapping/curious armor stands module handler.
public class QASCASHandler {
    public static final String SLOT = "curio"; //all armorstand curios go into this slot

    public static void swapCurios(ZPlayerInteract.EntityInteractSpecific event, Player player, ArmorStand armorStand){
        /*
        Since the method returns an Optional by default, developers will need to make sure to use Optional#ifPresent first in order to check that the inventory actually exists:

        CuriosApi.getCuriosInventory(livingEntity).ifPresent(curiosInventory -> {
          // code here - with access to the inventory instance that now definitely exists
        });

        Once a developer has the ICuriosItemHandler instance, they can use the methods from that instance to interact with the Curios inventory.
         */

        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);
        ICuriosItemHandler playerCuriosItemhandler = maybeCuriosInventory.get(); //we can safely assume the player has a curios inventory

        maybeCuriosInventory = CuriosApi.getCuriosInventory(armorStand);
        ICuriosItemHandler standCuriosItemhandler = maybeCuriosInventory.get(); //same for armor stand

        List<ItemStack> playerCurios = new ArrayList<>();
        List<ItemStack> standCurios = new ArrayList<>();

        for(ICurioStacksHandler slot : playerCuriosItemhandler.getCurios().values()) {
            int numberOfSlots = slot.getSlots();
            for (int i = 0; i < numberOfSlots; i++) {
                ItemStack stack = slot.getStacks().getStackInSlot(i);
                playerCurios.add(stack);
            }
        }

        Quark.LOG.info(playerCurios);

        /*
        var standCurios = CuriosApi.getCuriosInventory(armorStand);
        List<SlotResult> standResults = standCurios.get().findCurios();

        for(SlotResult result : standResults) {
            standToPlayer(player, armorStand, result);
        }

         */
    }


    private static void playerToStand(Player player, ArmorStand armorStand, SlotResult playerResult){
        ItemStack playerStack = playerResult.stack();

        CuriosApi.getCuriosInventory(armorStand).flatMap((inv) -> inv.getStacksHandler(SLOT)).map(ICurioStacksHandler::getCosmeticStacks).ifPresent((cosmetics) -> {
                    Optional<ICurio> curio = CuriosApi.getCurio(playerStack);

                    for (int slot = 0; slot < cosmetics.getSlots(); ++slot) {
                        SlotContext slotContext = new SlotContext(SLOT, armorStand, slot, true, true);
                        if (cosmetics.getStackInSlot(slot).isEmpty() && (curio.isEmpty() || (curio.get()).canEquip(slotContext))) {
                            cosmetics.setStackInSlot(slot, playerStack.copy());
                            CuriousArmorStands.Events.enableArmorStandArms(armorStand, playerStack);
                            CuriousArmorStands.Events.playEquipSound(curio, playerResult.slotContext());

                        }
                    }
                }
        );

    }

    private static void standToPlayer(Player player, ArmorStand armorStand, SlotResult playerResult){
        //the rest of the owl
    }
}
