package org.violetmoon.quark.content.mobs.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.quark.content.mobs.entity.Shiba;

import java.util.EnumSet;

public class BarkAtDarknessGoal extends Goal {

	private final Shiba shiba;
	private final PathNavigation navigator;

	public BarkAtDarknessGoal(Shiba shiba) {
		this.shiba = shiba;
		this.navigator = shiba.getNavigation();

		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public void tick() {
		if(shiba.currentHyperfocus != null) {
			navigator.moveTo(navigator.createPath(shiba.currentHyperfocus, 1), 1.1);

			if(shiba.level() instanceof ServerLevel slevel && shiba.tickCount % 5 == 0) {
				Vec3 pos = shiba.position();
				slevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.x, pos.y + 0.5, pos.z, 1, 0.25F, 0.1F, 0.25F, 0);
				shiba.lookAt(Anchor.EYES, new Vec3(shiba.currentHyperfocus.getX() + 0.5, shiba.currentHyperfocus.getY(), shiba.currentHyperfocus.getZ() + 0.5));
				shiba.playAmbientSound();
			}
		}
	}

	@Override
	public boolean canUse() {
		return shiba.currentHyperfocus != null;
	}

}
