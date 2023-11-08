package org.violetmoon.quark.content.tools.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.violetmoon.quark.content.tools.entity.SkullPike;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class RunAwayFromPikesGoal extends Goal {

	protected final PathfinderMob entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected SkullPike avoidTarget;
	protected final float avoidDistance;
	protected Path path;
	protected final PathNavigation navigation;

	public RunAwayFromPikesGoal(PathfinderMob entityIn, float distance, double nearSpeedIn, double farSpeedIn) {
		entity = entityIn;
		avoidDistance = distance;
		farSpeed = nearSpeedIn;
		nearSpeed = farSpeedIn;
		navigation = entityIn.getNavigation();
		setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		avoidTarget = getClosestEntity(entity.level, entity, entity.getX(), entity.getY(), entity.getZ(), entity.getBoundingBox().inflate(avoidDistance, 3.0D, avoidDistance));
		if(avoidTarget == null)
			return false;

		Vec3 posToMove = DefaultRandomPos.getPosAway(entity, 16, 7, avoidTarget.position());
		if(posToMove == null)
			return false;

		if(avoidTarget.distanceToSqr(posToMove.x, posToMove.y, posToMove.z) < avoidTarget.distanceToSqr(entity))
			return false;


		path = navigation.createPath(posToMove.x, posToMove.y, posToMove.z, 0);
		return path != null;
	}

	@Nullable
	private SkullPike getClosestEntity(Level world, LivingEntity living, double x, double y, double z, AABB bounds) {
		return getClosestEntity(world.getEntitiesOfClass(SkullPike.class, bounds, skullPike -> true), living, x, y, z);
	}

	@Nullable
	private SkullPike getClosestEntity(List<SkullPike> entities, LivingEntity target, double x, double y, double z) {
		double d0 = -1.0D;
		SkullPike t = null;

		for(SkullPike t1 : entities) {
			if(!t1.isVisible(target))
				continue;

			double d1 = t1.distanceToSqr(x, y, z);
			if (d0 == -1.0D || d1 < d0) {
				d0 = d1;
				t = t1;
			}
		}

		return t;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.navigation.isDone();
	}

	@Override
	public void start() {
		this.navigation.moveTo(this.path, this.farSpeed);
	}

	@Override
	public void stop() {
		this.avoidTarget = null;
	}

	@Override
	public void tick() {
		if (this.entity.distanceToSqr(this.avoidTarget) < 49.0D) {
			this.entity.getNavigation().setSpeedModifier(this.nearSpeed);
		} else {
			this.entity.getNavigation().setSpeedModifier(this.farSpeed);
		}

	}
}
