/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 12:17 AM (EST)]
 */
package org.violetmoon.quark.content.mobs.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

import org.violetmoon.quark.content.mobs.entity.Foxhound;

import java.util.EnumSet;

public class FoxhoundSleepGoal extends Goal {
    private final Foxhound foxhound;
    private boolean wasSitting;


    public FoxhoundSleepGoal(Foxhound foxhound) {
        this.foxhound = foxhound;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }
    @Override
    public boolean canUse() {
        if (foxhound.isSleeping()) {
            if (!this.foxhound.isTame() || this.foxhound.isInWater() || !this.foxhound.onGround()) {
                return false;
            } else {
                LivingEntity living = this.foxhound.getOwner();

                if (living == null)
                    return true;
                else
                    return (!(this.foxhound.distanceToSqr(living) < 144.0D) || living.getLastHurtByMob() == null);

            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return foxhound.getPose() == Pose.SLEEPING;
    }

    @Override
    public void start() {
        this.foxhound.getNavigation().stop();
        wasSitting = foxhound.isOrderedToSit();
        foxhound.setPose(Pose.SLEEPING);
    }

    @Override
    public void stop() {
        this.foxhound.setOrderedToSit(wasSitting); // setSitting
        if (wasSitting) {
            foxhound.setPose(Pose.SITTING);
        } else {
            foxhound.setPose(Pose.STANDING);
        }
    }
}
