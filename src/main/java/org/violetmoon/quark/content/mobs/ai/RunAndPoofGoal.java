/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 11, 2019, 18:35 AM (EST)]
 */
package org.violetmoon.quark.content.mobs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.entity.Stoneling;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class RunAndPoofGoal<T extends Entity> extends Goal {

	private final Predicate<Entity> canBeSeenSelector;
	protected Stoneling entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected T closestLivingEntity;
	private final float avoidDistance;
	private Path path;
	private final PathNavigation navigation;
	private final Class<T> classToAvoid;
	private final Predicate<T> avoidTargetSelector;

	public RunAndPoofGoal(Stoneling entity, Class<T> classToAvoid, float avoidDistance, double farSpeed, double nearSpeed) {
		this(entity, classToAvoid, t -> true, avoidDistance, farSpeed, nearSpeed);
	}

	public RunAndPoofGoal(Stoneling entity, Class<T> classToAvoid, Predicate<T> avoidTargetSelector, float avoidDistance, double farSpeed, double nearSpeed) {
		this.canBeSeenSelector = target -> target != null && target.isAlive() && entity.getSensing().hasLineOfSight(target) && !entity.isAlliedTo(target);
		this.entity = entity;
		this.classToAvoid = classToAvoid;
		this.avoidTargetSelector = avoidTargetSelector;
		this.avoidDistance = avoidDistance;
		this.farSpeed = farSpeed;
		this.nearSpeed = nearSpeed;
		this.navigation = entity.getNavigation();
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if(entity.isPlayerMade() || !entity.isStartled())
			return false;

		List<T> entities = this.entity.level().getEntitiesOfClass(this.classToAvoid, this.entity.getBoundingBox().inflate(this.avoidDistance, 3.0D, this.avoidDistance),
				entity -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && this.canBeSeenSelector.test(entity) && this.avoidTargetSelector.test(entity));

		if(entities.isEmpty())
			return false;
		else {
			this.closestLivingEntity = entities.get(0);
			Vec3 target = DefaultRandomPos.getPosAway(this.entity, 16, 7, this.closestLivingEntity.position());

			if(target != null && this.closestLivingEntity.distanceToSqr(target.x, target.y, target.z) < this.closestLivingEntity.distanceToSqr(this.entity))
				return false;
			else {
				if(target != null)
					this.path = this.navigation.createPath(target.x, target.y, target.z, 0);
				return target == null || this.path != null;
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		if(this.path == null || this.navigation.isDone()) {
			return false;
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Vec3 epos = entity.position();

		for(int i = 0; i < 8; ++i) {
			int j = Mth.floor(epos.x + (i % 2 - 0.5F) * 0.1F + entity.getEyeHeight());
			int k = Mth.floor(epos.y + ((i >> 1) % 2 - 0.5F) * entity.getBbWidth() * 0.8F);
			int l = Mth.floor(epos.z + ((i >> 2) % 2 - 0.5F) * entity.getBbWidth() * 0.8F);

			if(pos.getX() != k || pos.getY() != j || pos.getZ() != l) {
				pos.set(k, j, l);

				if(entity.level().getBlockState(pos).blocksMotion()) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public void start() {
		Vec3 epos = entity.position();

		if(this.path != null)
			this.navigation.moveTo(this.path, this.farSpeed);
		entity.level().playSound(null, epos.x, epos.y, epos.z, QuarkSounds.ENTITY_STONELING_MEEP, SoundSource.NEUTRAL, 1.0F, 1.0F);
	}

	@Override
	public void stop() {
		this.closestLivingEntity = null;

		if(entity.level() instanceof ServerLevel ws) {
			Vec3 epos = entity.position();

			ws.sendParticles(ParticleTypes.CLOUD, epos.x, epos.y, epos.z, 40, 0.5, 0.5, 0.5, 0.1);
			ws.sendParticles(ParticleTypes.EXPLOSION, epos.x, epos.y, epos.z, 20, 0.5, 0.5, 0.5, 0);
		}
		for(Entity passenger : entity.getIndirectPassengers())
			if(!(passenger instanceof Player))
				passenger.discard();
		entity.discard();
	}

	@Override
	public void tick() {
		if(this.entity.distanceToSqr(this.closestLivingEntity) < 49.0D)
			this.entity.getNavigation().setSpeedModifier(this.nearSpeed);
		else
			this.entity.getNavigation().setSpeedModifier(this.farSpeed);
	}

}
