package org.violetmoon.quark.content.tweaks.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.quark.content.tweaks.module.PatTheDogsModule;

import java.util.EnumSet;

/**
 * @author WireSegal
 *         Created at 11:27 AM on 9/2/19.
 */
public class WantLoveGoal extends Goal {

	private static final String PET_TIME = "quark:PetTime";

	public static void setPetTime(TamableAnimal entity) {
		if(PatTheDogsModule.dogsWantLove > 0){
			entity.getPersistentData().putLong(PET_TIME, entity.level().getGameTime());
		}
	}

	public static boolean canPet(TamableAnimal entity) {
		return timeSinceLastPet(entity) > 20;
	}

	public static boolean needsPets(TamableAnimal entity) {
		if(PatTheDogsModule.dogsWantLove <= 0)
			return false;

		return timeSinceLastPet(entity) > PatTheDogsModule.dogsWantLove;
	}

	public static long timeSinceLastPet(TamableAnimal entity) {
		if(!entity.isTame())
			return 0;

		long lastPetAt = entity.getPersistentData().getLong(PET_TIME);
		return entity.level().getGameTime() - lastPetAt;
	}

	private final TamableAnimal creature;
	private LivingEntity leapTarget;
	public final float leapUpMotion;

	public WantLoveGoal(TamableAnimal creature, float leapMotion) {
		this.creature = creature;
		this.leapUpMotion = leapMotion;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		if(!needsPets(creature))
			return false;

		this.leapTarget = this.creature.getOwner();

		if(this.leapTarget == null)
			return false;
		else {
			double distanceToTarget = this.creature.distanceToSqr(this.leapTarget);

			return 4 <= distanceToTarget && distanceToTarget <= 16 &&
					this.creature.onGround() && this.creature.getRandom().nextInt(5) == 0;
		}
	}

	@Override
	public boolean canContinueToUse() {
		if(!WantLoveGoal.needsPets(creature))
			return false;
		return !this.creature.onGround();
	}

	@Override
	public void start() {
		Vec3 leapPos = leapTarget.position();
		Vec3 creaturePos = creature.position();

		double dX = leapPos.x - creaturePos.x;
		double dZ = leapPos.z - creaturePos.z;
		float leapMagnitude = (float) Math.sqrt(dX * dX + dZ * dZ);

		Vec3 motion = this.creature.getDeltaMovement();

		if(leapMagnitude >= 0.0001) {
			motion = motion.add(
					dX / leapMagnitude * 0.4 + motion.x * 0.2,
					0,
					dZ / leapMagnitude * 0.4 + motion.z * 0.2);
		}

		motion = motion.add(0, leapUpMotion, 0);

		this.creature.setDeltaMovement(motion);
	}
}
