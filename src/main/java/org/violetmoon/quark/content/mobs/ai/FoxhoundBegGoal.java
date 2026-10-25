package org.violetmoon.quark.content.mobs.ai;

import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.content.mobs.entity.Foxhound;

public class FoxhoundBegGoal extends BegGoal {
    private final Wolf wolf;

    public FoxhoundBegGoal(Wolf wolf, float lookDistance) {
        super(wolf, lookDistance);
        this.wolf = wolf;
    }

    private boolean playerHoldingInteresting(Player player) {
        if(this.wolf instanceof Foxhound foxhound) {
            for (InteractionHand interactionhand : InteractionHand.values()) {
                ItemStack itemstack = player.getItemInHand(interactionhand);
                Level level = player.level();
                if(!foxhound.isTame()){
                    if(itemstack.getItem() == Items.COAL && (level.getDifficulty() == Difficulty.PEACEFUL || player.getAbilities().invulnerable || player.getEffect(MobEffects.FIRE_RESISTANCE) != null) && !level.isClientSide){
                        return true;
                    }
                }
                else{
                    if (this.wolf.isFood(itemstack)) {
                        return true;
                    }
                }

            }
        }

        return false;
    }
}
