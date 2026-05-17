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
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.animal.*;
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
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.module.TinyPotatoModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.ai.FoxhoundPlaceToRestGoal;
import org.violetmoon.quark.content.mobs.module.FoxhoundModule;
import org.violetmoon.quark.content.tweaks.ai.WantLoveGoal;

import java.util.List;
import java.util.UUID;

import static org.violetmoon.quark.content.mobs.ai.FoxhoundPlaceToRestGoal.Target.*;

public class Foxhound extends Wolf implements Enemy {


    public static final ResourceKey<LootTable> FOXHOUND_LOOT_TABLE = Quark.asResourceKey(Registries.LOOT_TABLE, "entities/foxhound");
	private static final EntityDataAccessor<Boolean> TEMPTATION = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IS_BLUE = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> TATERING = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> COLLAR_COLOR = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_RESTING = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);

	private int timeUntilPotatoEmerges = 0;
    private int ticksUntilICanSleep = 0;

    public Foxhound(EntityType<? extends Foxhound> type, Level worldIn) {
		super(type, worldIn);
		this.setPathfindingMalus(PathType.WATER, -1.0F);
		this.setPathfindingMalus(PathType.LAVA, 1.0F);
		this.setPathfindingMalus(PathType.DANGER_FIRE, 4.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, 4.0F); // IT DOESNT SAY DAMAGE TWICE ITS DANGER AND THEN DAMAGE
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);

		//todo: Mixin accessor or accesswidener

		builder.define(COLLAR_COLOR, DyeColor.ORANGE.getId());
		builder.define(TEMPTATION, false);
		builder.define(IS_BLUE, false);
		builder.define(TATERING, false);
        builder.define(IS_RESTING, false);
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

		Level level = level();

        if (ticksUntilICanSleep > 0) {
            ticksUntilICanSleep--;
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

		/*if(isResting()) {
			AABB aabb = getBoundingBox();
			if(aabb.getYsize() < 0.21)
				setBoundingBox(new AABB(aabb.minX - 0.2, aabb.minY, aabb.minZ - 0.2, aabb.maxX + 0.2, aabb.maxY + 0.5, aabb.maxZ + 0.2));
		}
		 */

		if(WantLoveGoal.needsPets(this)) {
			Entity owner = getOwner();
			if(owner != null && owner.distanceToSqr(this) < 1 && !owner.isInWater() && !owner.fireImmune() && (!(owner instanceof Player player) || !player.getAbilities().invulnerable))
				owner.igniteForSeconds(5);
		}

		Vec3 pos = position();
		if(level.isClientSide && (!this.isBaby() ^ random.nextBoolean())) {
			SimpleParticleType particle = ParticleTypes.FLAME;
			if(isResting())
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


	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
        //this.goalSelector.addGoal(2, new FoxhoundSleepGoal(this));
		this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new FoxhoundPlaceToRestGoal(this, 0.8D, LIT_FURNACE));
		this.goalSelector.addGoal(7, new FoxhoundPlaceToRestGoal(this, 0.8D, FURNACE));
		this.goalSelector.addGoal(8, new FoxhoundPlaceToRestGoal(this, 0.8D, GLOWING));
        this.goalSelector.addGoal(9, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
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
				target -> !isTame() && target.level().getDifficulty() != Difficulty.PEACEFUL));
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
        if (level().getDifficulty() == Difficulty.PEACEFUL && (entityIn instanceof Player)) {
            return false;
        }

		if(!entityIn.fireImmune()) { //unless the entity overrides fireImmune, this will only check the entitytype
			if(entityIn instanceof LivingEntity le && (le.hasEffect(MobEffects.FIRE_RESISTANCE) ||
					(le.getAttribute(Attributes.BURNING_TIME) != null) && le.getAttributeValue(Attributes.BURNING_TIME) <= 0)){
				return false;
			}
			if(entityIn instanceof ServerPlayer player && player.isDamageSourceBlocked(level().damageSources().mobAttack(this))){
				return super.doHurtTarget(entityIn); //hurt without igniting, causes shield to block
			}
			entityIn.igniteForSeconds(5);
			return super.doHurtTarget(entityIn);
		}

		return false;
	}

	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		if (super.hurt(source, amount)) {
            setResting(false);
            return true;
        }
        return false;
	}

	@NotNull
	@Override
	public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		//debugging code
		if(itemstack.is(Items.DEBUG_STICK) && !player.level().isClientSide()){
            if(isResting()){
                // #5327 - isSleeping is always returning false.
                //vanilla foxes have their own isSleeping method instead of using the LivingEntity method,
                //but that uses entity data which is synced, ai goals (which we are using for sleep) are not.
                player.sendSystemMessage(Component.literal("setResting(false)"));
                setResting(false);
            }
            else{
                player.sendSystemMessage(Component.literal("setSleeping(true)"));
                setResting(true);
                return InteractionResult.CONSUME;
            }
		}

		if(itemstack.getItem() == Items.BONE && !isTame())
			return InteractionResult.PASS; //prevent bone being passed to superclass

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
						this.setInSittingPose(true);
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
		if(res.consumesAction()) {
            setResting(false);
            if (isOrderedToSit()) {
                setInSittingPose(true);
            }
        }

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
		compound.putBoolean("IsSlep", isResting());
		compound.putBoolean("IsBlue", isBlue());
        compound.putInt("ticksUntilICanSleep", ticksUntilICanSleep);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		timeUntilPotatoEmerges = compound.getInt("OhLawdHeComin");
		setResting(compound.getBoolean("IsSlep"));
		setBlue(compound.getBoolean("IsBlue"));
        ticksUntilICanSleep = compound.getInt("ticksUntilICanSleep");
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if(isResting()) {
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

	@Override
	public boolean isSleeping() {
		return isResting();
	}

    public boolean isStanding() {
        return getPose() == Pose.STANDING;
    }

    public void setStanding(boolean standing) {
        setPose(Pose.STANDING);
    }

    @Override
    public void startSleeping(BlockPos pos) {
        super.startSleeping(pos);
        setResting(true);
    }

    @Override
    public void stopSleeping() {
        super.stopSleeping();
        setResting(false);
    }

    public boolean canTeleportTo(@NotNull BlockPos teleportPos) {
        if (!super.canTeleportTo(teleportPos)) {
            PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(this, teleportPos);
            if (pathtype != PathType.DAMAGE_FIRE && pathtype != PathType.DANGER_FIRE) {
                return false;
            } else {
                if (!this.canFlyToOwner() && this.level().getBlockState(teleportPos.below()).getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockDistance = teleportPos.subtract(this.blockPosition());
                    return this.level().noCollision(this, this.getBoundingBox().move(blockDistance));
                }
            }
        } else return super.canTeleportTo(teleportPos);
    }

    @Override
    public void tryToTeleportToOwner() {
        if (!isStanding()) {
            setStanding(true);
        }
        super.tryToTeleportToOwner();
    }
    
    public boolean isResting() {
        return entityData.get(IS_RESTING);
    }

    public void setResting(boolean resting) {
        entityData.set(IS_RESTING, resting);
        if (!resting) {
            ticksUntilICanSleep = 100;
        }
    }

    public boolean canRest() {
        return ticksUntilICanSleep <= 0;
    }

    //Notes:
    // Poses are supposed to be MC's state machine for entities in a sense.
    // But, poses can be a bit tricky to deal with? Is it really that hard?

    // If a goal is being done, none of the goals of "higher" priority are done. Thus why mobs cant walk when they are sat down for instance.
}
