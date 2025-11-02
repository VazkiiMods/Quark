/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 12:04 AM (EST)]
 */
package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.TinyPotatoBlock;
import org.violetmoon.quark.addons.oddities.module.TinyPotatoModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.ai.FindPlaceToSleepGoal;
import org.violetmoon.quark.content.mobs.ai.SleepGoal;
import org.violetmoon.quark.content.mobs.module.FoxhoundModule;
import org.violetmoon.quark.content.tweaks.ai.WantLoveGoal;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorWolf;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.violetmoon.quark.content.mobs.ai.FindPlaceToSleepGoal.Target.*;

public class Foxhound extends Wolf implements Enemy {

	public static final ResourceKey<LootTable> FOXHOUND_LOOT_TABLE = Quark.asResourceKey(Registries.LOOT_TABLE, "entities/foxhound");
	private static final EntityDataAccessor<Boolean> TEMPTATION = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IS_BLUE = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> TATERING = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> COLLAR_COLOR = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.INT);

	private int timeUntilPotatoEmerges = 0;

	public Foxhound(EntityType<? extends Foxhound> type, Level worldIn) {
		super(type, worldIn);
		this.setPathfindingMalus(PathType.WATER, -1.0F);
		this.setPathfindingMalus(PathType.LAVA, 1.0F);
		this.setPathfindingMalus(PathType.DANGER_FIRE, 1.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, 1.0F); //TODO is there a reason this is here twice?
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);

		//todo: Mixin accessor or accesswidener

		builder.define(COLLAR_COLOR, DyeColor.ORANGE.getId());
		builder.define(TEMPTATION, false);
		builder.define(IS_BLUE, false);
		builder.define(TATERING, false);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 4;
	}

	@Override
	public boolean isPersistenceRequired() {
		return super.isPersistenceRequired();
	}

	@Override
	public boolean requiresCustomPersistence() {
		return isTame();
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isTame();
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull MobSpawnType reason, SpawnGroupData spawnDataIn) {
		Holder<Biome> biome = worldIn.getBiome(BlockPos.containing(position()));
		if(biome.is(Biomes.SOUL_SAND_VALLEY.location()))
			setBlue(true);

		return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
	}

	@Override
	public void tick() {
		super.tick();

		Pose pose = getPose();
		if(isSleeping()) {
			if(pose != Pose.SLEEPING)
				setPose(Pose.SLEEPING);
		} else if(pose == Pose.SLEEPING)
			setPose(Pose.STANDING);


		Level level = level();
		if(!level.isClientSide && level.getDifficulty() == Difficulty.PEACEFUL && !isTame()) {
			discard();
			return;
		}

		if(!level.isClientSide && timeUntilPotatoEmerges > 0) {
			if(--timeUntilPotatoEmerges == 0) {
				setTatering(false);
				ItemStack stack = new ItemStack(TinyPotatoModule.tiny_potato);
				stack.set(QuarkDataComponents.IS_ANGRY, true);

				spawnAtLocation(stack);
				playSound(QuarkSounds.BLOCK_POTATO_HURT, 1f, 1f);
			} else if(!isTatering())
				setTatering(true);
		}

		if(isSleeping()) {
			Optional<BlockPos> sleepPos = getSleepingPos();
			if(sleepPos.isPresent()) {
				BlockPos pos = sleepPos.get();
				if(distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 1)
					stopSleeping();
			}

			AABB aabb = getBoundingBox();
			if(aabb.getYsize() < 0.21)
				setBoundingBox(new AABB(aabb.minX - 0.2, aabb.minY, aabb.minZ - 0.2, aabb.maxX + 0.2, aabb.maxY + 0.5, aabb.maxZ + 0.2));
		}

		if(WantLoveGoal.needsPets(this)) {
			Entity owner = getOwner();
			if(owner != null && owner.distanceToSqr(this) < 1 && !owner.isInWater() && !owner.fireImmune() && (!(owner instanceof Player player) || !player.getAbilities().invulnerable))
				owner.igniteForSeconds(5);
		}

		Vec3 pos = position();
		if(level.isClientSide && (!this.isBaby() ^ random.nextBoolean())) {
			SimpleParticleType particle = ParticleTypes.FLAME;
			if(isSleeping())
				particle = ParticleTypes.SMOKE;
			else if(isBlue())
				particle = ParticleTypes.SOUL_FIRE_FLAME;


			level.addParticle(particle, this.getRandomX(0.5f), getRandomY(), getRandomZ(0.5f), 0.0D, 0.0D, 0.0D);

			if(isTatering() && random.nextDouble() < 0.1) {
				level.addParticle(ParticleTypes.LARGE_SMOKE, getRandomX(0.5f), getRandomY(),  getRandomZ(0.5f), 0.0D, 0.0D, 0.0D);

				level.playLocalSound(pos.x, pos.y, pos.z, QuarkSounds.ENTITY_FOXHOUND_CRACKLE, getSoundSource(), 1.0F, 1.0F, false);
			}

		}

		if(isTame() && FoxhoundModule.foxhoundsSpeedUpFurnaces) {
			BlockPos below = blockPosition().below();
			BlockEntity tile = level.getBlockEntity(below);
			if(tile instanceof AbstractFurnaceBlockEntity furnace) {
				int cookTime = furnace.cookingProgress;
				if(cookTime > 0 && cookTime % 3 == 0) {
					List<Foxhound> foxhounds = level.getEntitiesOfClass(Foxhound.class, new AABB(blockPosition()),
							(fox) -> fox != null && fox.isTame());
					if(!foxhounds.isEmpty() && foxhounds.getFirst() == this) {
						furnace.cookingProgress = furnace.cookingProgress == 3 ? 5 : Math.min(furnace.cookingTotalTime - 1, cookTime + 1);

						if(getOwner() instanceof ServerPlayer sp)
							FoxhoundModule.foxhoundFurnaceTrigger.trigger(sp);
					}
				}
			}
		}
	}

	@Override
	public boolean isInWaterOrRain() {
		return false;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return FOXHOUND_LOOT_TABLE;
	}

	protected SleepGoal sleepGoal;

	@Override
	protected void registerGoals() {
		this.sleepGoal = new SleepGoal(this);
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, this.sleepGoal);
		this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
		this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new FindPlaceToSleepGoal(this, 0.8D, LIT_FURNACE));
		this.goalSelector.addGoal(9, new FindPlaceToSleepGoal(this, 0.8D, FURNACE));
		this.goalSelector.addGoal(10, new FindPlaceToSleepGoal(this, 0.8D, GLOWING));
		this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(12, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(13, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(14, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(4, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false,
				target -> target instanceof Sheep || target instanceof Rabbit));
		this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Player.class, false,
				target -> !isTame()));
//		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
	}

	@Override
	public int getRemainingPersistentAngerTime() {
		if(!isTame() && level().getDifficulty() != Difficulty.PEACEFUL)
			return 0;
		return super.getRemainingPersistentAngerTime();
	}

	@Override
	public boolean doHurtTarget(Entity entityIn) {
		if(entityIn.getType().fireImmune()) {
			if(entityIn instanceof Player)
				return false;
			return super.doHurtTarget(entityIn);
		}

		boolean flag = entityIn.hurt(level().damageSources().onFire(),
				((int) this.getAttributeValue(Attributes.ATTACK_DAMAGE)));

		if(flag) {
			entityIn.igniteForSeconds(5);
		}
		return flag;
	}

	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		setWoke();
		return super.hurt(source, amount);
	}

	@NotNull
	@Override
	public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if(itemstack.getItem() == Items.BONE && !isTame())
			return InteractionResult.PASS; //why is there here lol

		Level level = level();
		if(this.isTame()) {
			if(timeUntilPotatoEmerges <= 0 && itemstack.is(TinyPotatoModule.tiny_potato.asItem())) {
				timeUntilPotatoEmerges = 600;

				playSound(QuarkSounds.ENTITY_FOXHOUND_EAT, 1f, 1f);
				if(!player.getAbilities().instabuild)
					itemstack.shrink(1);
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		} else {
			if(!itemstack.isEmpty()) {
				if(itemstack.getItem() == Items.COAL && (level.getDifficulty() == Difficulty.PEACEFUL || player.getAbilities().invulnerable || player.getEffect(MobEffects.FIRE_RESISTANCE) != null) && !level.isClientSide) {
					if(random.nextDouble() < FoxhoundModule.tameChance) {
						this.tame(player);
						this.navigation.stop();
						this.setTarget(null);
						this.setOrderedToSit(true);
						this.setHealth(20.0F);
						level.broadcastEntityEvent(this, (byte) 7);
					} else {
						level.broadcastEntityEvent(this, (byte) 6);
					}

					if(!player.getAbilities().instabuild)
						itemstack.shrink(1);
					return InteractionResult.sidedSuccess(level.isClientSide);
				}
			}
		}

		InteractionResult res = super.mobInteract(player, hand);
		if(res.consumesAction() && !level.isClientSide)
			setWoke();

		return res;
	}

	@Override
	public boolean canMate(@NotNull Animal otherAnimal) {
		return super.canMate(otherAnimal) && otherAnimal instanceof Foxhound;
	}

	@Override // createChild
	public Wolf getBreedOffspring(@NotNull ServerLevel sworld, @NotNull AgeableMob otherParent) {
		Foxhound kid = new Foxhound(FoxhoundModule.foxhoundType, this.level());
		UUID uuid = this.getOwnerUUID();

		if(uuid != null) {
			kid.setOwnerUUID(uuid);
			kid.setTame(true, true);
		}

		if(isBlue())
			kid.setBlue(true);

		return kid;
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		compound.putInt("OhLawdHeComin", timeUntilPotatoEmerges);
		compound.putBoolean("IsSlep", isSleeping());
		compound.putBoolean("IsBlue", isBlue());
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		timeUntilPotatoEmerges = compound.getInt("OhLawdHeComin");
		setInSittingPose(compound.getBoolean("IsSlep"));
		setBlue(compound.getBoolean("IsBlue"));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if(isSleeping()) {
			return null;
		}
		if(this.isAngry()) {
			return QuarkSounds.ENTITY_FOXHOUND_GROWL;
		} else if(this.random.nextInt(3) == 0) {
			return this.isTame() && this.getHealth() < 10.0F ? QuarkSounds.ENTITY_FOXHOUND_WHINE : QuarkSounds.ENTITY_FOXHOUND_PANT;
		} else {
			return QuarkSounds.ENTITY_FOXHOUND_IDLE;
		}
	}

	@Override
	protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_FOXHOUND_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_FOXHOUND_DIE;
	}

	public boolean isBlue() {
		return entityData.get(IS_BLUE);
	}

	public void setBlue(boolean blue) {
		entityData.set(IS_BLUE, blue);
	}

	public boolean isTatering() {
		return entityData.get(TATERING);
	}

	public void setTatering(boolean tatering) {
		entityData.set(TATERING, tatering);
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
		return worldIn.getBlockState(pos.below()).is(FoxhoundModule.foxhoundSpawnableTag) ? 10.0F : worldIn.getRawBrightness(pos, 0) - 0.5F;
	}

	public static boolean spawnPredicate(EntityType<? extends Foxhound> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && world.getBlockState(pos.below()).is(FoxhoundModule.foxhoundSpawnableTag);
	}

	public SleepGoal getSleepGoal() {
		return sleepGoal;
	}

	private void setWoke() {
		SleepGoal sleep = getSleepGoal();
		if(sleep != null) {
			setInSittingPose(false);
			sleep.setSleeping(false);
		}
	}
}
