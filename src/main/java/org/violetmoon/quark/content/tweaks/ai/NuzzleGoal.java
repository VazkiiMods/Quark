/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 30, 2019, 20:50 AM (EST)]
 */
package org.violetmoon.quark.content.tweaks.ai;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;

public class NuzzleGoal extends Goal {

	private final TamableAnimal creature;
	private LivingEntity owner;
	private final double followSpeed;
	private final PathNavigation petPathfinder;
	private int timeUntilRebuildPath;
	private final float maxDist;
	private final float whineDist;
	private int whineCooldown;
	private float oldWaterCost;
	private final SoundEvent whine;

	public NuzzleGoal(TamableAnimal creature, double followSpeed, float maxDist, float whineDist, SoundEvent whine) {
		this.creature = creature;
		this.followSpeed = followSpeed;
		this.petPathfinder = creature.getNavigation();
		this.maxDist = maxDist;
		this.whineDist = whineDist;
		this.whine = whine;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));

		if(!(creature.getNavigation() instanceof GroundPathNavigation) && !(creature.getNavigation() instanceof FlyingPathNavigation))
			throw new IllegalArgumentException("Unsupported mob type for NuzzleOwnerGoal");
	}

	@Override
	public boolean canUse() {
		if(!WantLoveGoal.needsPets(creature))
			return false;

		LivingEntity living = this.creature.getOwner();

		if(living == null || living.isSpectator() ||
				this.creature.isOrderedToSit())
			return false;
		else {
			this.owner = living;
			return true;
		}
	}

	@Override
	public boolean canContinueToUse() {
		if(!WantLoveGoal.needsPets(creature))
			return false;
		return !this.petPathfinder.isDone() && this.creature.distanceToSqr(this.owner) > (this.maxDist * this.maxDist) && !this.creature.isOrderedToSit();
	}

	@Override
	public void start() {
		this.timeUntilRebuildPath = 0;
		this.whineCooldown = 10;
		this.oldWaterCost = this.creature.getPathfindingMalus(PathType.WATER);
		this.creature.setPathfindingMalus(PathType.WATER, 0.0F);
	}

	@Override
	public void stop() {
		this.owner = null;
		this.petPathfinder.stop();
		this.creature.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
	}

	@Override
	public void tick() {
		this.creature.getLookControl().setLookAt(this.owner, 10.0F, this.creature.getMaxHeadXRot());

		if(!this.creature.isOrderedToSit()) {
			if(--this.timeUntilRebuildPath <= 0) {
				this.timeUntilRebuildPath = 10;

				this.petPathfinder.moveTo(this.owner, this.followSpeed);
			}
		}

		if(creature.distanceToSqr(owner) < whineDist) {
			if(--this.whineCooldown <= 0) {
				this.whineCooldown = 80 + creature.getRandom().nextInt(40);
				creature.playSound(whine, 1F, 0.5F + (float) Math.random() * 0.5F);
			}
		}
	}
}
