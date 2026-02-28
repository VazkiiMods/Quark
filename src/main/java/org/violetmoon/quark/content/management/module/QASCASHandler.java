package org.violetmoon.quark.content.management.module;

import curiousarmorstands.CuriousArmorStands;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Optional;

//quick armor swapping/curious armor stands module handler.
public class QASCASHandler {
    public static final String SLOT = "curio"; //all armorstand curios go into this slot

    public static void swapCurios(ZPlayerInteract.EntityInteractSpecific event, Player player, ArmorStand armorStand){
        var playerCurios = CuriosApi.getCuriosInventory(player);
        List<SlotResult> playerResults = playerCurios.get().findCurios();

        for(SlotResult result : playerResults) { //it's size 0 at this point for some reason
            playerToStand(player, armorStand, result);
        }

        var standCurios = CuriosApi.getCuriosInventory(armorStand);
        List<SlotResult> standResults = standCurios.get().findCurios();

        for(SlotResult result : standResults) {
            standToPlayer(player, armorStand, result);
        }
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
