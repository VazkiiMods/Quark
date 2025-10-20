package org.violetmoon.quark.addons.oddities.capability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.violetmoon.quark.api.IMagnetTracker;

import java.util.Collection;

/**
 * @author WireSegal
 *         Created at 4:29 PM on 3/1/20.
 */
public class MagnetTracker implements IMagnetTracker {

	private final Multimap<BlockPos, Force> forcesActing = HashMultimap.create();

	private final Level world;

	public MagnetTracker(Level world) {
		this.world = world;
	}

	@Override
	public Vec3i getNetForce(BlockPos pos) {
		Vec3i net = Vec3i.ZERO;
		for(Force force : forcesActing.get(pos))
			net = force.add(net);
		return net;
	}

	@Override
	public void applyForce(BlockPos pos, int magnitude, boolean pushing, Direction dir, int distance, BlockPos origin) {
		forcesActing.put(pos, new Force(magnitude, pushing, dir, distance, origin));
	}

	@Override
	public void actOnForces(BlockPos pos) {
		Vec3i net = getNetForce(pos);

		if(net.equals(Vec3i.ZERO))
			return;

		Direction target = Direction.getNearest(net.getX(), net.getY(), net.getZ());

		for(Force force : forcesActing.get(pos)) {
			if(force.direction() == target) {
				BlockState origin = world.getBlockState(force.origin());
				//here it assumes that whichever block applied that force is a magnet block... or at least expects these block events
				world.blockEvent(force.origin(), origin.getBlock(), force.pushing() ? 0 : 1, force.distance());
			}
		}
	}

	@Override
	public Collection<BlockPos> getTrackedPositions() {
		return forcesActing.keySet();
	}

	@Override
	public void clear() {
		forcesActing.clear();
	}
}
