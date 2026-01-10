package org.violetmoon.quark.content.mobs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.violetmoon.quark.content.mobs.entity.Foxhound;

import java.util.function.BiFunction;

public class FoxhoundPlaceToRestGoal extends MoveToBlockGoal {
    private final Foxhound foxhound;
    private final Target target;

    public FoxhoundPlaceToRestGoal(Foxhound foxhound, double speed, Target target) {
        super(foxhound, speed, 8);
        this.foxhound = foxhound;
        this.target = target;
    }

    public double acceptedDistance() {
        return 0.85d;
    }

    @Override
    public boolean canUse() {
        return this.foxhound.isTame() && !this.foxhound.isOrderedToSit() && !this.foxhound.isResting() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.foxhound.setInSittingPose(false);
    }

    @Override
    protected int nextStartTick(PathfinderMob creature) {
        return 40;
    }

    @Override
    public void stop() {
        super.stop();
        this.foxhound.setResting(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.foxhound.setInSittingPose(false);
        if (!this.isReachedTarget()) {
            this.foxhound.setResting(false);
        } else if (!this.foxhound.isResting() && foxhound.canRest()) {
            this.foxhound.setResting(true);
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        return target.test(level, pos);
    }

    public enum Target {
        LIT_FURNACE(((level, pos) -> level.getBlockEntity(pos) instanceof FurnaceBlockEntity && level.getLightEmission(pos) > 2)),
        FURNACE(((level, pos) -> level.getBlockEntity(pos) instanceof FurnaceBlockEntity && level.getLightEmission(pos) <= 2)),
        GLOWING((level, pos) -> level.getLightEmission(pos) > 2 && level.getBlockState(pos).getFluidState().isEmpty());

        private final BiFunction<LevelReader, BlockPos, Boolean> check;
        Target(BiFunction<LevelReader, BlockPos, Boolean> check) {
            this.check = check;
        }

        public boolean test(LevelReader level, BlockPos pos) {
            return level.isEmptyBlock(pos.above()) && this.check.apply(level, pos);
        }
    }
}
