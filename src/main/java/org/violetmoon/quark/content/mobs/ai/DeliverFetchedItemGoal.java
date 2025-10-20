package org.violetmoon.quark.content.mobs.ai;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

import org.violetmoon.quark.content.mobs.entity.Shiba;

public class DeliverFetchedItemGoal extends FollowOwnerGoal {

	private final Shiba shiba;
	private int timeTilNextJump = 20;

	public DeliverFetchedItemGoal(Shiba shiba, double speed, float minDist, float maxDist) {
		super(shiba, speed, minDist, maxDist);
		this.shiba = shiba;
	}

	@Override
	public void tick() {
		super.tick();

		timeTilNextJump--;
		if(timeTilNextJump <= 0) {
			timeTilNextJump = shiba.level().random.nextInt(5) + 10;

			if(shiba.onGround()) {
				shiba.push(0, 0.3, 0);
				shiba.setJumping(true);
			}
		}
	}

	@Override
	public boolean canUse() {
		return super.canUse() && !shiba.getMouthItem().isEmpty();
	}

	@Override
	public boolean canContinueToUse() {
		return super.canContinueToUse() && !shiba.getMouthItem().isEmpty();
	}

}
