package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.violetmoon.quark.base.handler.QuarkSounds;

public class SoulBead extends Entity {

	private static final EntityDataAccessor<Integer> TARGET_X = SynchedEntityData.defineId(SoulBead.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TARGET_Z = SynchedEntityData.defineId(SoulBead.class, EntityDataSerializers.INT);

	private int liveTicks = 0;
	private static final String TAG_TARGET_X = "targetX";
	private static final String TAG_TARGET_Z = "targetZ";

	public SoulBead(EntityType<? extends SoulBead> type, Level worldIn) {
		super(type, worldIn);
	}

	public void setTarget(int x, int z) {
		entityData.set(TARGET_X, x);
		entityData.set(TARGET_Z, z);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TARGET_X, 0);
		builder.define(TARGET_Z, 0);
	}

	@Override
	public void tick() {
		super.tick();

		double posSpread = 0.4;
		double scale = 0.08;
		double rotateSpread = 1.5;
		double rise = 0.025;
		int maxLiveTime = 6000;
		int particles = 20;
		double trigArg = liveTicks * 0.32;

		if((maxLiveTime - liveTicks) < particles)
			particles = (maxLiveTime - liveTicks);

		double posX = getX();
		double posY = getY();
		double posZ = getZ();

		Vec3 vec = new Vec3((double) entityData.get(TARGET_X), posY, (double) entityData.get(TARGET_Z)).subtract(posX, posY, posZ).normalize().scale(scale);
		double bpx = posX + vec.x * liveTicks + Math.cos(trigArg) * rotateSpread;
		double bpy = posY + vec.y * liveTicks + liveTicks * rise;
		double bpz = posZ + vec.z * liveTicks + Math.sin(trigArg) * rotateSpread;

		for(int i = 0; i < particles; i++) {
			double px = bpx + (Math.random() - 0.5) * posSpread;
			double py = bpy + (Math.random() - 0.5) * posSpread;
			double pz = bpz + (Math.random() - 0.5) * posSpread;
			level().addParticle(new DustParticleOptions(new Vector3f(0.2F, 0.12F, 0.1F), 1F), px, py, pz, 0, 0, 0);
			if(Math.random() < 0.05)
				level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SOUL_SAND.defaultBlockState()), px, py, pz, 0, 0, 0);
		}

		if(Math.random() < 0.1)
			level().playSound(null, bpx, bpy, bpz, QuarkSounds.ENTITY_SOUL_BEAD_IDLE, SoundSource.PLAYERS, 0.2F, 1F);

		liveTicks++;
		if(liveTicks > maxLiveTime)
			removeAfterChangingDimensions();
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag compound) {
		entityData.set(TARGET_X, compound.getInt(TAG_TARGET_X));
		entityData.set(TARGET_Z, compound.getInt(TAG_TARGET_Z));
	}

	@Override
	protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
		compound.putInt(TAG_TARGET_X, entityData.get(TARGET_X));
		compound.putInt(TAG_TARGET_Z, entityData.get(TARGET_Z));
	}
}
