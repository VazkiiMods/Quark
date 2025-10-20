package org.violetmoon.quark.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Used primarily for double crops which need extra checks before they are considered ready.
 * Ony fires if said block is not blacklisted
 * Can also be used for blocks that hae other max age requirements as it fires for all crop blocks
 */
//TODO: this can me moved to a zeta event no problem

public class SimpleHarvestEvent extends Event implements ICancellableEvent {

    public final BlockState blockState;
    public final BlockPos pos;
    public final Level level;
    public final @Nullable InteractionHand hand;
    public final @Nullable Entity entity;
    private BlockPos newTarget;
    private ActionType action;

    //Note that entity could be a player or villager
    public SimpleHarvestEvent(BlockState blockState, BlockPos pos, Level level, @Nullable InteractionHand hand,
                              @Nullable Entity entity, ActionType originalActionType) {
        this.blockState = blockState;
        this.pos = pos;
        this.hand = hand;
        this.level = level;
        this.entity = entity;
        this.newTarget = pos;
        this.action = originalActionType;
    }

    /**
     * Used for double crops and the like. Pass a new position which should be broken instead
     *
     * @param pos new target position
     */
    public void setTargetPos(BlockPos pos) {
        this.newTarget = pos;
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (cancel)
            action = ActionType.NONE;
        ICancellableEvent.super.setCanceled(cancel);
    }

    //Click will work just for players!
    public enum ActionType {
        NONE, CLICK, HARVEST;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public BlockPos getTargetPos() {
        return newTarget;
    }


}
