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

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.entity.Foxhound;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class FindPlaceToSleepGoal extends MoveToBlockGoal {
	private final Foxhound foxhound;

	private final Target target;

	private boolean hadSlept = false;

	public FindPlaceToSleepGoal(Foxhound foxhound, double speed, Target target) {
		super(foxhound, speed, 8);
		this.foxhound = foxhound;
		this.target = target;
	}

	@Override
	public boolean canUse() {
		return this.foxhound.isTame() && !this.foxhound.isOrderedToSit() && super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return (!hadSlept || this.foxhound.isSleeping()) && super.canContinueToUse();
	}

	@Override
	public void start() {
		super.start();
		hadSlept = false;
		this.foxhound.setOrderedToSit(false); // setSitting
		this.foxhound.setInSittingPose(false);
	}

	@Override
	public void stop() {
		super.stop();
		hadSlept = false;
		this.foxhound.setOrderedToSit(false); // setSitting
		this.foxhound.setInSittingPose(false);
	}

	@Override
	public void tick() {
		super.tick();

		Vec3 motion = foxhound.getDeltaMovement();

        if (this.isReachedTarget() && (motion.horizontalDistance() <= 0) && target.test(this.foxhound.level(), foxhound.getOnPos())) {
            if(!this.foxhound.isOrderedToSit()) {
                this.foxhound.setInSittingPose(true);
                foxhound.setSleeping(true);
                hadSlept = true;
            }
        } else {
//this.foxhound.setSleeping(false);
        }
    }

	@Override
	protected boolean isValidTarget(@NotNull LevelReader world, @NotNull BlockPos pos) {
		return target.test(world, pos);
	}

	public enum Target {
		LIT_FURNACE(((level, pos) -> level.getBlockEntity(pos) instanceof FurnaceBlockEntity && level.getLightEmission(pos) > 2)),
		FURNACE(((level, pos) -> level.getBlockEntity(pos) instanceof FurnaceBlockEntity && level.getLightEmission(pos) <= 2)),
		GLOWING((level, pos) -> level.getLightEmission(pos) > 2);

        private final BiFunction<LevelReader, BlockPos, Boolean> check;
        Target(BiFunction<LevelReader, BlockPos, Boolean> check) {
            this.check = check;
        }

        public boolean test(LevelReader level, BlockPos pos) {
            return level.isEmptyBlock(pos.above()) && this.check.apply(level, pos);
        }
	}
}
